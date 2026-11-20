package com.iagora.backend.modules.marketplace.chatServico;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.perfil.PerfilRepository;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaService;
import com.iagora.backend.modules.marketplace.servicoContratado.ServicoContratadoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChatServicoService {
    private final ChatServicoRepository repository;
    private final ServicoContratadoService contratacaoService;
    private final PerfilEmpresaService empresaService;
    private final PerfilRepository perfilRepository;
    private final EntityManager entityManager;

    public ChatServicoService(ChatServicoRepository repository, ServicoContratadoService contratacaoService,
                              PerfilEmpresaService empresaService, PerfilRepository perfilRepository,
                              EntityManager entityManager) {
        this.repository = repository;
        this.contratacaoService = contratacaoService;
        this.empresaService = empresaService;
        this.perfilRepository = perfilRepository;
        this.entityManager = entityManager;
    }

    public ChatServico buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ChatServicoResponse cadastrar(ChatServicoRequest request, Integer userId) {
        var item = new ChatServico();
        if (request.contratadoId() != null) {
            var contratado = contratacaoService.buscarParticipante(request.contratadoId(), userId, true);
            var empresa = contratado.getServico().getEmpresa();
            if (request.empresaId() != null && !request.empresaId().equals(empresa.getId())) {
                throw new RegraNegocioException("A empresa deve pertencer à contratação", HttpStatus.BAD_REQUEST);
            }
            var existente = repository.findByContratado_Id(contratado.getId());
            if (existente.isPresent()) {
                return resposta(existente.get());
            }
            item.setContratado(contratado);
            item.setPerfil(contratado.getPerfil());
            item.setEmpresa(empresa);
        } else {
            var empresa = empresaService.buscar(request.empresaId());
            if (!empresa.getValidado() || empresa.getUser().getId().equals(userId)) {
                throw new RegraNegocioException("Empresa indisponível para iniciar conversa", HttpStatus.CONFLICT);
            }
            var perfil = perfilRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RegraNegocioException("Perfil pessoal não encontrado", HttpStatus.NOT_FOUND));
            item.setEmpresa(empresa);
            item.setPerfil(perfil);
        }
        return resposta(repository.save(item));
    }

    public ChatServico buscarParticipante(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getPerfil().getUser().getId().equals(userId) && !item.getEmpresa().getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Acesso restrito aos participantes da conversa", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public List<ChatServicoResponse> listar(Integer userId) {
        return repository.findByPerfil_User_IdOrEmpresa_User_IdOrderByIdDesc(userId, userId)
            .stream().filter(item -> !oculto(item, userId)).map(this::resposta).toList();
    }

    @Transactional
    public ChatServicoResponse consultar(Integer id, Integer userId) {
        return resposta(buscarParticipante(id, userId));
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        var item = buscarParticipante(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (item.getPerfil().getUser().getId().equals(userId)) {
            item.setOcultoCliente(true);
        } else {
            item.setOcultoEmpresa(true);
        }
    }

    private boolean oculto(ChatServico item, Integer userId) {
        return item.getPerfil().getUser().getId().equals(userId) ? item.isOcultoCliente() : item.isOcultoEmpresa();
    }

    public ChatServicoResponse resposta(ChatServico item) {
        return new ChatServicoResponse(
            item.getId(),
            item.getPerfil().getId(),
            item.getEmpresa().getId(),
            item.getContratado() == null ? null : item.getContratado().getId()
        );
    }
}

