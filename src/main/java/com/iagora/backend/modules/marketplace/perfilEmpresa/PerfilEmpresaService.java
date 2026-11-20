package com.iagora.backend.modules.marketplace.perfilEmpresa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.endereco.EnderecoRequest;
import com.iagora.backend.modules.endereco.EnderecoResponse;
import com.iagora.backend.modules.endereco.EnderecoService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PerfilEmpresaService {
    private final PerfilEmpresaRepository repository;
    private final EntityManager entityManager;
    private final EnderecoService enderecos;

    public PerfilEmpresaService(PerfilEmpresaRepository repository, EntityManager entityManager, EnderecoService enderecos) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.enderecos = enderecos;
    }

    public PerfilEmpresa buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PerfilEmpresaResponse cadastrar(PerfilEmpresaRequest request, Integer userId) {
        var user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null || !Integer.valueOf(2).equals(user.getTipo())) {
            throw new RegraNegocioException("Somente uma conta empresarial pode criar um perfil", HttpStatus.FORBIDDEN);
        }
        if (repository.findByUser_Id(userId).isPresent()) {
            throw new RegraNegocioException("A conta já possui um perfil empresarial", HttpStatus.CONFLICT);
        }
        var item = new PerfilEmpresa();
        item.setUser(user);
        item.setDataCriacao(LocalDateTime.now());
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    public PerfilEmpresa buscarDoUsuario(Integer userId) {
        return repository.findByUser_Id(userId)
            .orElseThrow(() -> new RegraNegocioException("Perfil empresarial não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PerfilEmpresaResponse meuPerfil(Integer userId) {
        return resposta(buscarDoUsuario(userId));
    }

    @Transactional
    public void atualizar(PerfilEmpresaRequest request, Integer userId) {
        var item = buscarDoUsuario(userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if ((request.cnpj() != null && !request.cnpj().equals(item.getCnpj()))
            || (request.nome() != null && !request.nome().strip().equals(item.getNome()))) {
            item.setValidado(false);
            item.setStatusValidacao("rascunho");
        }
        aplicar(item, request);
    }

    @Transactional
    public EnderecoResponse consultarEndereco(Integer usuario) {
        return enderecos.buscarSeExistir(buscarDoUsuario(usuario).getEnderecoId());
    }

    @Transactional
    public void editarEndereco(Integer usuario, EnderecoRequest endereco) {
        var empresa = buscarDoUsuario(usuario);
        entityManager.refresh(empresa, LockModeType.PESSIMISTIC_WRITE);
        empresa.setEnderecoId(enderecos.salvarResidencia(empresa.getEnderecoId(), usuario, endereco));
    }

    @Transactional
    public void enviarValidacao(Integer userId) {
        var item = buscarDoUsuario(userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!item.getValidado()) {
            item.setStatusValidacao("pendente");
        }
    }

    @Transactional
    public List<PerfilEmpresaResponse> listar() {
        return repository.findByValidadoTrueOrderByIdAsc().stream().map(this::resposta).toList();
    }

    @Transactional
    public PerfilEmpresaResponse detalhar(Integer id) {
        var item = buscar(id);
        if (!item.getValidado()) {
            throw new RegraNegocioException("Perfil empresarial não encontrado", HttpStatus.NOT_FOUND);
        }
        return resposta(item);
    }

    @Transactional
    public List<PerfilEmpresaResponse> pendentes() {
        return repository.findByStatusValidacaoOrderByIdAsc("pendente").stream().map(this::resposta).toList();
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
        if (!"pendente".equals(item.getStatusValidacao())) {
            throw new RegraNegocioException("Perfil fora da fila de validação", HttpStatus.CONFLICT);
        }
        item.setValidado(aprovado);
        item.setStatusValidacao(aprovado ? "aprovado" : "reprovado");
    }

    private void aplicar(PerfilEmpresa item, PerfilEmpresaRequest request) {
        if (request.enderecoId() != null && !request.enderecoId().equals(item.getEnderecoId())) {
            enderecos.verificarProprietario(request.enderecoId(), item.getUser().getId());
            item.setEnderecoId(request.enderecoId());
        }
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.cnpj() != null) {
            if (request.cnpj().isBlank() || request.cnpj().strip().length() > 14 || !request.cnpj().matches("[0-9]{14}")) {
                throw new RegraNegocioException("Campo cnpj inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCnpj(request.cnpj().strip());
        }
        if (item.getCnpj() == null) {
            throw new RegraNegocioException("Campo cnpj obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.descricao() != null) {
            item.setDescricao(request.descricao().strip());
        }
        if (request.categoria() != null) {
            if (request.categoria().isBlank() || request.categoria().strip().length() > 150) {
                throw new RegraNegocioException("Campo categoria inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(request.categoria().strip());
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoria obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.logo() != null) {
            if (request.logo().strip().length() > 500) {
                throw new RegraNegocioException("Campo logo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setLogo(request.logo().strip());
        }
        if (request.banner() != null) {
            if (request.banner().strip().length() > 500) {
                throw new RegraNegocioException("Campo banner inválido", HttpStatus.BAD_REQUEST);
            }
            item.setBanner(request.banner().strip());
        }
        if (request.telefone() != null) {
            if (request.telefone().isBlank() || request.telefone().strip().length() > 20) {
                throw new RegraNegocioException("Campo telefone inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTelefone(request.telefone().strip());
        }
        if (item.getTelefone() == null) {
            throw new RegraNegocioException("Campo telefone obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.localAtendimento() != null) {
            item.setLocalAtendimento(request.localAtendimento());
        }
        if (request.horarioAtendimento() != null) {
            item.setHorarioAtendimento(request.horarioAtendimento());
        }
        if (request.emailComercial() != null) {
            if (request.emailComercial().isBlank() || request.emailComercial().strip().length() > 255 || !request.emailComercial().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
                throw new RegraNegocioException("Campo emailComercial inválido", HttpStatus.BAD_REQUEST);
            }
            item.setEmailComercial(request.emailComercial().strip());
        }
        if (item.getEmailComercial() == null) {
            throw new RegraNegocioException("Campo emailComercial obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.site() != null) {
            if (request.site().strip().length() > 500) {
                throw new RegraNegocioException("Campo site inválido", HttpStatus.BAD_REQUEST);
            }
            item.setSite(request.site().strip());
        }
    }

    public PerfilEmpresaResponse resposta(PerfilEmpresa item) {
        return new PerfilEmpresaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getEnderecoId(),
            item.getNome(),
            item.getCnpj(),
            item.getDescricao(),
            item.getCategoria(),
            item.getLogo(),
            item.getBanner(),
            item.getTelefone(),
            item.getLocalAtendimento(),
            item.getHorarioAtendimento(),
            item.getEmailComercial(),
            item.getSite(),
            item.getDataCriacao(),
            item.getValidado(),
            item.getStatusValidacao()
        );
    }
}

