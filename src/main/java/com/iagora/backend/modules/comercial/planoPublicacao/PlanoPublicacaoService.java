package com.iagora.backend.modules.comercial.planoPublicacao;

import com.iagora.backend.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PlanoPublicacaoService {
    private final PlanoPublicacaoRepository repository;

    public PlanoPublicacaoService(PlanoPublicacaoRepository repository) {
        this.repository = repository;
    }

    public PlanoPublicacao buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    public List<PlanoPublicacaoResponse> listar() {
        return repository.findByAtivoTrueOrderByIdAsc().stream().map(this::resposta).toList();
    }

    public PlanoPublicacaoResponse detalhar(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public PlanoPublicacaoResponse cadastrar(PlanoPublicacaoRequest request) {
        var item = new PlanoPublicacao();
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, PlanoPublicacaoRequest request) {
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

    private void aplicar(PlanoPublicacao item, PlanoPublicacaoRequest request) {
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
        if (request.duracaoDias() != null) {
            if (request.duracaoDias() < 1 || request.duracaoDias() > 3650) {
                throw new RegraNegocioException("Campo duracaoDias inválido", HttpStatus.BAD_REQUEST);
            }
            item.setDuracaoDias(request.duracaoDias());
        }
        if (item.getDuracaoDias() == null) {
            throw new RegraNegocioException("Campo duracaoDias obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    public PlanoPublicacaoResponse resposta(PlanoPublicacao item) {
        return new PlanoPublicacaoResponse(
            item.getId(),
            item.getNome(),
            item.getDescricao(),
            item.getPreco(),
            item.getDuracaoDias(),
            item.getAtivo()
        );
    }
}

