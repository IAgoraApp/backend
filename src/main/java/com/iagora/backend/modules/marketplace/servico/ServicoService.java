package com.iagora.backend.modules.marketplace.servico;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class ServicoService {
    private final ServicoRepository repository;
    private final PerfilEmpresaService empresaService;
    private final EntityManager entityManager;

    public ServicoService(ServicoRepository repository, PerfilEmpresaService empresaService, EntityManager entityManager) {
        this.repository = repository;
        this.empresaService = empresaService;
        this.entityManager = entityManager;
    }

    public Servico buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ServicoResponse cadastrar(ServicoRequest request, Integer userId) {
        var empresa = empresaService.buscarDoUsuario(userId);
        if (request.empresaId() != null && !empresa.getId().equals(request.empresaId())) {
            throw new RegraNegocioException("Empresa inválida para esta operação", HttpStatus.FORBIDDEN);
        }
        var item = new Servico();
        item.setEmpresa(empresa);
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    public Servico buscarDoUsuario(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getEmpresa().getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Serviço pertence a outra empresa", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    public boolean visivel(Servico item, LocalDateTime momento) {
        return item.getAtivo() && item.getValidado() && item.getEmpresa().getValidado()
            && "publicado".equals(item.getStatusPublicacao())
            && item.getExpiraEm() != null && item.getExpiraEm().isAfter(momento);
    }

    public Servico buscarVisivel(Integer id) {
        var item = buscar(id);
        if (!visivel(item, LocalDateTime.now())) {
            throw new RegraNegocioException("Serviço não encontrado no catálogo", HttpStatus.NOT_FOUND);
        }
        return item;
    }

    @Transactional
    public List<ServicoResponse> listar() {
        LocalDateTime agora = LocalDateTime.now();
        return repository.findByAtivoTrueAndValidadoTrueAndExpiraEmAfterOrderByIdAsc(agora).stream()
            .filter(item -> visivel(item, agora)).map(this::resposta).toList();
    }

    @Transactional
    public List<ServicoResponse> meusServicos(Integer userId) {
        return repository.findByEmpresa_User_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public ServicoResponse detalhar(Integer id, Integer userId) {
        return resposta(buscarAcessivel(id, userId));
    }

    public Servico buscarAcessivel(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getEmpresa().getUser().getId().equals(userId) && !visivel(item, LocalDateTime.now())) {
            throw new RegraNegocioException("Serviço não encontrado no catálogo", HttpStatus.NOT_FOUND);
        }
        return item;
    }

    @Transactional
    public void atualizar(Integer id, ServicoRequest request, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (request.empresaId() != null && !request.empresaId().equals(item.getEmpresa().getId())) {
            throw new RegraNegocioException("A empresa do serviço é fixa", HttpStatus.BAD_REQUEST);
        }
        if (!"rascunho".equals(item.getStatusPublicacao()) && !"reprovado".equals(item.getStatusPublicacao())) {
            throw new RegraNegocioException("Somente serviços em rascunho ou reprovados podem ser editados", HttpStatus.CONFLICT);
        }
        aplicar(item, request);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!"rascunho".equals(item.getStatusPublicacao()) && !"reprovado".equals(item.getStatusPublicacao())) {
            item.setAtivo(false);
            item.setStatusPublicacao("arquivado");
            return;
        }
        repository.delete(item);
    }

    @Transactional
    public void enviarValidacao(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!item.getEmpresa().getValidado()) {
            throw new RegraNegocioException("Valide o perfil da empresa antes de enviar um serviço", HttpStatus.CONFLICT);
        }
        if ("rascunho".equals(item.getStatusPublicacao()) || "reprovado".equals(item.getStatusPublicacao())) {
            item.setStatusPublicacao("pendente");
        }
    }

    @Transactional
    public void ativar(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        if ("arquivado".equals(item.getStatusPublicacao())) {
            throw new RegraNegocioException("Serviço arquivado", HttpStatus.CONFLICT);
        }
        item.setAtivo(true);
    }

    @Transactional
    public void desativar(Integer id, Integer userId) {
        buscarDoUsuario(id, userId).setAtivo(false);
    }

    @Transactional
    public List<ServicoResponse> pendentes() {
        return repository.findByStatusPublicacaoOrderByIdAsc("pendente").stream().map(this::resposta).toList();
    }

    @Transactional
    public void aprovar(Integer id) {
        decidir(id, true);
    }

    @Transactional
    public void reprovar(Integer id) {
        decidir(id, false);
    }

    private void decidir(Integer id, boolean aprovado) {
        var item = buscar(id);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!"pendente".equals(item.getStatusPublicacao()) || !item.getEmpresa().getValidado()) {
            throw new RegraNegocioException("Serviço fora da fila de validação", HttpStatus.CONFLICT);
        }
        item.setValidado(aprovado);
        item.setStatusPublicacao(aprovado ? "aprovado" : "reprovado");
    }

    private boolean fotosValidas(JsonNode fotos) {
        if (!fotos.isArray() || fotos.size() > 8) {
            return false;
        }
        for (var foto : fotos) {
            if (!foto.isString() || foto.asText().isBlank() || foto.asText().length() > 500) {
                return false;
            }
        }
        return true;
    }

    private void aplicar(Servico item, ServicoRequest request) {
        if (request.titulo() != null) {
            if (request.titulo().isBlank() || request.titulo().strip().length() > 150) {
                throw new RegraNegocioException("Campo titulo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTitulo(request.titulo().strip());
        }
        if (item.getTitulo() == null) {
            throw new RegraNegocioException("Campo titulo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.descricao() != null) {
            if (request.descricao().isBlank()) {
                throw new RegraNegocioException("Campo descricao inválido", HttpStatus.BAD_REQUEST);
            }
            item.setDescricao(request.descricao().strip());
        }
        if (item.getDescricao() == null) {
            throw new RegraNegocioException("Campo descricao obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.fotos() != null) {
            if (!fotosValidas(request.fotos())) {
                throw new RegraNegocioException("Campo fotos inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFotos(request.fotos());
        }
        if (request.categoria() != null) {
            if (request.categoria().isBlank() || request.categoria().strip().length() > 100) {
                throw new RegraNegocioException("Campo categoria inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(request.categoria().strip());
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoria obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.preco() != null) {
            if (request.preco().signum() < 0 || request.preco().scale() > 2 || request.preco().precision() - request.preco().scale() > 8) {
                throw new RegraNegocioException("Campo preco inválido", HttpStatus.BAD_REQUEST);
            }
            item.setPreco(request.preco());
        }
        if (request.tipoPreco() != null) {
            if (request.tipoPreco().isBlank() || request.tipoPreco().strip().length() > 30) {
                throw new RegraNegocioException("Campo tipoPreco inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTipoPreco(request.tipoPreco().strip());
            if ("orcamento".equals(item.getTipoPreco())) item.setPreco(null);
        }
        if (item.getTipoPreco() == null) {
            throw new RegraNegocioException("Campo tipoPreco obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.disponibilidade() != null) {
            item.setDisponibilidade(request.disponibilidade());
        }
    }

    public ServicoResponse resposta(Servico item) {
        return new ServicoResponse(
            item.getId(),
            item.getEmpresa().getId(),
            item.getTitulo(),
            item.getDescricao(),
            item.getFotos(),
            item.getCategoria(),
            item.getPreco(),
            item.getTipoPreco(),
            item.getAtivo(),
            item.getDisponibilidade(),
            item.getValidado(),
            item.getNota(),
            item.getStatusPublicacao(),
            item.getPublicadoEm(),
            item.getExpiraEm()
        );
    }
}

