package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.endereco.EnderecoService;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CasaDesejadaService {
    private final CasaDesejadaRepository repository;
    private final UserRepository userRepository;
    private final EnderecoService enderecos;

    public CasaDesejadaService(CasaDesejadaRepository repository, UserRepository userRepository, EnderecoService enderecos) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.enderecos = enderecos;
    }

    public Integer proprietarioId(CasaDesejada item) {
        return item.getUser().getId();
    }

    @Transactional
    public CasaDesejada buscarDoUsuario(Integer id, Integer userId) {
        CasaDesejada item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public CasaDesejadaResponse cadastrar(CasaDesejadaRequest request, Integer userId) {
        CasaDesejada item = new CasaDesejada();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public CasaDesejadaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<CasaDesejadaResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, CasaDesejadaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        CasaDesejada item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(CasaDesejada item, CasaDesejadaRequest request, Integer userId) {
        if (request.endereco() != null) {
            item.setEnderecoId(enderecos.salvarResidencia(item.getEnderecoId(), userId, request.endereco()));
        } else if (request.enderecoId() != null && !request.enderecoId().equals(item.getEnderecoId())) {
            enderecos.verificarProprietario(request.enderecoId(), userId);
            item.setEnderecoId(request.enderecoId());
        }
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
            if (request.descricao().strip().length() > 500) {
                throw new RegraNegocioException("Campo descricao inválido", HttpStatus.BAD_REQUEST);
            }
            item.setDescricao(request.descricao().strip());
        }
        if (request.tipo() != null) {
            if (request.tipo().isBlank() || request.tipo().strip().length() > 150) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTipo(request.tipo().strip());
        }
        if (item.getTipo() == null) {
            throw new RegraNegocioException("Campo tipo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.valor() != null) {
            if (request.valor().scale() > 2 || request.valor().precision() - request.valor().scale() > 10 || request.valor().signum() < 0) {
                throw new RegraNegocioException("Campo valor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValor(request.valor());
        }
        if (request.valorTipo() != null) {
            if (request.valorTipo().strip().length() > 20) {
                throw new RegraNegocioException("Campo valorTipo inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("mensal", "total").contains(request.valorTipo())) {
                throw new RegraNegocioException("Campo valorTipo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValorTipo(request.valorTipo().strip());
        }
        if (request.mobilia() != null) {
            if (request.mobilia().strip().length() > 30) {
                throw new RegraNegocioException("Campo mobilia inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("mobiliado", "parcial", "sem-moveis").contains(request.mobilia())) {
                throw new RegraNegocioException("Campo mobilia inválido", HttpStatus.BAD_REQUEST);
            }
            item.setMobilia(request.mobilia().strip());
        }
    }

    private CasaDesejadaResponse resposta(CasaDesejada item) {
        return new CasaDesejadaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getEnderecoId(),
            item.getTitulo(),
            item.getDescricao(),
            item.getTipo(),
            item.getValor(),
            item.getValorTipo(),
            item.getMobilia(),
            enderecos.buscarSeExistir(item.getEnderecoId())
        );
    }
}
