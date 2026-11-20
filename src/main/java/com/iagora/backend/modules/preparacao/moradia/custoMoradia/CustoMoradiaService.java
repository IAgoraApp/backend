package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustoMoradiaService {
    private final CustoMoradiaRepository repository;
    private final PlanoMoradiaService planoService;

    public CustoMoradiaService(CustoMoradiaRepository repository, PlanoMoradiaService planoService) {
        this.repository = repository;
        this.planoService = planoService;
    }

    public Integer proprietarioId(CustoMoradia item) {
        return planoService.proprietarioId(item.getPlano());
    }

    @Transactional
    public CustoMoradia buscarDoUsuario(Integer id, Integer userId) {
        CustoMoradia item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public CustoMoradiaResponse cadastrar(Integer planoId, CustoMoradiaRequest request, Integer userId) {
        CustoMoradia item = new CustoMoradia();
        item.setPlano(planoService.buscarDoUsuario(planoId, userId));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public CustoMoradiaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }


    @Transactional
    public List<CustoMoradiaResponse> listarPorPlano(Integer planoId, Integer userId) {
        planoService.buscarDoUsuario(planoId, userId);
        return repository.findByPlano_IdOrderByIdAsc(planoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, CustoMoradiaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        CustoMoradia item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(CustoMoradia item, CustoMoradiaRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.categoria() != null) {
            if (request.categoria().isBlank() || request.categoria().strip().length() > 50) {
                throw new RegraNegocioException("Campo categoria inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(request.categoria().strip());
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoria obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.tipo() != null) {
            if (request.tipo().isBlank() || request.tipo().strip().length() > 30) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("mensal", "inicial").contains(request.tipo())) {
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
    }

    private CustoMoradiaResponse resposta(CustoMoradia item) {
        return new CustoMoradiaResponse(
            item.getId(),
            item.getPlano().getId(),
            item.getNome(),
            item.getCategoria(),
            item.getTipo(),
            item.getValor()
        );
    }
}
