package com.iagora.backend.modules.preparacao.aprendizado.aula;

import com.iagora.backend.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AulaService {
    private final AulaRepository repository;

    public AulaService(AulaRepository repository) {
        this.repository = repository;
    }

    public Aula buscar(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public AulaResponse buscarPorId(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public List<AulaResponse> listar() {
        return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::resposta).toList();
    }

    @Transactional
    public AulaResponse cadastrar(AulaRequest request) {
        var item = new Aula();
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, AulaRequest request) {
        aplicar(buscar(id), request);
    }

    @Transactional
    public void apagar(Integer id) {
        repository.delete(buscar(id));
    }

    private void aplicar(Aula item, AulaRequest request) {
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
            item.setDescricao(request.descricao().strip());
        }
        if (request.conteudo() != null) {
            item.setConteudo(request.conteudo().strip());
        }
        if (request.videoUrl() != null) {
            if (request.videoUrl().strip().length() > 500) {
                throw new RegraNegocioException("Campo videoUrl inválido", HttpStatus.BAD_REQUEST);
            }
            item.setVideoUrl(request.videoUrl().strip());
        }
        if (request.imagem() != null) {
            if (request.imagem().strip().length() > 500) {
                throw new RegraNegocioException("Campo imagem inválido", HttpStatus.BAD_REQUEST);
            }
            item.setImagem(request.imagem().strip());
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
    }

    private AulaResponse resposta(Aula item) {
        return new AulaResponse(item.getId(), item.getTitulo(), item.getDescricao(), item.getConteudo(), item.getVideoUrl(), item.getImagem(), item.getCategoria());
    }
}
