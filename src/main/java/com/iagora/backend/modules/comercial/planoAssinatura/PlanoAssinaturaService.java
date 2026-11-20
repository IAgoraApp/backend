package com.iagora.backend.modules.comercial.planoAssinatura;

import com.iagora.backend.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PlanoAssinaturaService {
    private final PlanoAssinaturaRepository repository;

    public PlanoAssinaturaService(PlanoAssinaturaRepository repository) {
        this.repository = repository;
    }

    public PlanoAssinatura buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    public List<PlanoAssinaturaResponse> listar() {
        return repository.findByAtivoTrueOrderByIdAsc().stream().map(this::resposta).toList();
    }

    public PlanoAssinaturaResponse detalhar(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public PlanoAssinaturaResponse cadastrar(PlanoAssinaturaRequest request) {
        var item = new PlanoAssinatura();
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, PlanoAssinaturaRequest request) {
        aplicar(buscar(id), request);
    }

    @Transactional
    public void ativar(Integer id) {
        buscar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Integer id) {
        buscar(id).setAtivo(false);
    }

    private void aplicar(PlanoAssinatura item, PlanoAssinaturaRequest request) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 100) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.descricao() != null) {
            item.setDescricao(request.descricao().strip());
        }
        if (request.preco() != null) {
            if (request.preco().signum() < 0 || request.preco().scale() > 2 || request.preco().precision() - request.preco().scale() > 8) {
                throw new RegraNegocioException("Campo preco inválido", HttpStatus.BAD_REQUEST);
            }
            item.setPreco(request.preco());
        }
        if (item.getPreco() == null) {
            throw new RegraNegocioException("Campo preco obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.beneficios() != null) {
            item.setBeneficios(request.beneficios());
        }
    }

    public PlanoAssinaturaResponse resposta(PlanoAssinatura item) {
        return new PlanoAssinaturaResponse(
            item.getId(),
            item.getNome(),
            item.getDescricao(),
            item.getPreco(),
            item.getBeneficios(),
            item.getAtivo()
        );
    }
}

