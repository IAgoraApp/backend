package com.iagora.backend.modules.marketplace.servicoContratado;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.perfil.PerfilRepository;
import com.iagora.backend.modules.marketplace.propostaAtendimento.PropostaAtendimentoRepository;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ServicoContratadoService {
    private final ServicoContratadoRepository repository;
    private final PerfilRepository perfilRepository;
    private final ServicoService servicoService;
    private final EntityManager entityManager;
    private final PropostaAtendimentoRepository propostaRepository;

    public ServicoContratadoService(ServicoContratadoRepository repository, PerfilRepository perfilRepository,
                                    ServicoService servicoService, EntityManager entityManager, PropostaAtendimentoRepository propostaRepository) {
        this.repository = repository;
        this.perfilRepository = perfilRepository;
        this.servicoService = servicoService;
        this.entityManager = entityManager;
        this.propostaRepository = propostaRepository;
    }

    public ServicoContratado buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ServicoContratadoResponse contratar(Integer servicoId, ServicoContratadoRequest request, Integer userId) {
        var perfil = perfilRepository.findByUser_Id(userId)
            .orElseThrow(() -> new RegraNegocioException("Somente um usuário comum pode contratar serviços", HttpStatus.FORBIDDEN));
        if (!Integer.valueOf(1).equals(perfil.getUser().getTipo())) {
            throw new RegraNegocioException("Somente um usuário comum pode contratar serviços", HttpStatus.FORBIDDEN);
        }

        var servico = servicoService.buscarVisivel(servicoId);
        var item = new ServicoContratado();
        item.setPerfil(perfil);
        item.setServico(servico);
        item.setDataContratacao(LocalDateTime.now());
        item.setValor(servico.getPreco() != null ? servico.getPreco() : BigDecimal.ZERO);
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public List<ServicoContratadoResponse> minhas(Integer userId) {
        return repository.findByPerfil_User_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public List<ServicoContratadoResponse> recebidas(Integer userId) {
        return repository.findByServico_Empresa_User_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public ServicoContratadoResponse consultar(Integer id, Integer userId) {
        return resposta(buscarParticipante(id, userId, false));
    }

    @Transactional
    public void atualizar(Integer id, ServicoContratadoRequest request, Integer userId) {
        var item = buscarParticipante(id, userId, true);
        exigirCliente(item, userId);
        exigirStatus(item, "pendente");
        aplicar(item, request);
    }

    @Transactional
    public void cancelar(Integer id, Integer userId) {
        var item = buscarParticipante(id, userId, true);
        exigirCliente(item, userId);
        exigirStatus(item, "pendente");
        encerrarProposta(id);
        item.setStatus("cancelado");
    }

    @Transactional
    public void aceitar(Integer id, Integer userId) {
        mudarStatus(id, userId, "pendente", "aceito");
    }

    @Transactional
    public void recusar(Integer id, Integer userId) {
        mudarStatus(id, userId, "pendente", "recusado");
    }

    @Transactional
    public void iniciar(Integer id, Integer userId) {
        mudarStatus(id, userId, "aceito", "em_andamento");
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        var item = mudarStatus(id, userId, "em_andamento", "concluido");
        item.setDataConclusao(LocalDateTime.now());
    }

    public ServicoContratado buscarParticipante(Integer id, Integer userId, boolean bloquear) {
        var item = buscar(id);
        if (bloquear) {
            entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        }
        if (!cliente(item, userId) && !item.getServico().getEmpresa().getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Acesso restrito aos participantes da contratação", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    public void exigirCliente(ServicoContratado item, Integer userId) {
        if (!cliente(item, userId)) {
            throw new RegraNegocioException("Operação exclusiva do contratante", HttpStatus.FORBIDDEN);
        }
    }

    public void exigirStatus(ServicoContratado item, String status) {
        if (!status.equals(item.getStatus())) {
            throw new RegraNegocioException("Operação incompatível com o estado da contratação", HttpStatus.CONFLICT);
        }
    }

    private boolean cliente(ServicoContratado item, Integer userId) {
        return item.getPerfil().getUser().getId().equals(userId);
    }

    private ServicoContratado mudarStatus(Integer id, Integer userId, String anterior, String proximo) {
        var item = buscarParticipante(id, userId, true);
        if (!item.getServico().getEmpresa().getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Operação exclusiva da empresa", HttpStatus.FORBIDDEN);
        }
        exigirStatus(item, anterior);
        if ("pendente".equals(anterior)) {
            encerrarProposta(id);
        }
        item.setStatus(proximo);
        return item;
    }

    private void encerrarProposta(Integer contratacaoId) {
        propostaRepository.findByContratado_IdAndStatus(contratacaoId, "pendente")
            .ifPresent(proposta -> proposta.setStatus("encerrada"));
    }

    private void aplicar(ServicoContratado item, ServicoContratadoRequest request) {
        if (request.enderecoId() != null) {
            item.setEnderecoId(request.enderecoId());
        }
        if (request.descricao() != null) {
            item.setDescricao(request.descricao().strip());
        }
        if (request.dataAgendamento() != null) {
            item.setDataAgendamento(request.dataAgendamento());
        }
        if (request.valor() != null) {
            if (request.valor().signum() < 0 || request.valor().scale() > 2 || request.valor().precision() - request.valor().scale() > 8) {
                throw new RegraNegocioException("Campo valor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValor(request.valor());
        }
        if (item.getValor() == null) {
            throw new RegraNegocioException("Campo valor obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    public ServicoContratadoResponse resposta(ServicoContratado item) {
        return new ServicoContratadoResponse(
            item.getId(),
            item.getServico().getId(),
            item.getPerfil().getId(),
            item.getEnderecoId(),
            item.getDescricao(),
            item.getDataAgendamento(),
            item.getValor(),
            item.getStatus(),
            item.getDataContratacao(),
            item.getDataConclusao()
        );
    }
}

