package com.iagora.backend.modules.identidade.perfil;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PerfilService {

  private final PerfilRepository perfilRepository;

  public PerfilService(PerfilRepository perfilRepository) {
    this.perfilRepository = perfilRepository;
  }

  public void criar(User user) {
    if (perfilRepository.existsByUser_Id(user.getId())) {
      throw new RegraNegocioException("Usuário já possui perfil", HttpStatus.CONFLICT);
    }

    String nome = "Usuário " + UUID.randomUUID().toString().substring(0, 8);
    Perfil perfil = new Perfil(user, nome, 2);
    perfil.setFoto("/images/perfil-padrao.svg");
    perfilRepository.save(perfil);
  }

  @Transactional
  public PerfilResponse buscarPorId(Integer id) {
    Perfil perfil = perfilRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Perfil não encontrado",
          HttpStatus.NOT_FOUND
        )
      );
    return new PerfilResponse(
      perfil.getId(),
      perfil.getUser().getId(),
      perfil.getEnderecoId(),
      perfil.getNome(),
      perfil.getFoto(),
      perfil.getTipo()
    );
  }

  @Transactional
  public PerfilResponse buscarPorUserId(Integer userId) {
    Perfil perfil = perfilRepository.findByUser_Id(userId)
      .orElseThrow(() -> new RegraNegocioException(
        "Perfil não encontrado", HttpStatus.NOT_FOUND
      ));

    return new PerfilResponse(
      perfil.getId(),
      perfil.getUser().getId(),
      perfil.getEnderecoId(),
      perfil.getNome(),
      perfil.getFoto(),
      perfil.getTipo()
    );
  }

  @Transactional
  public void atualizar(Integer id, PerfilRequest request) {
    Perfil perfil = perfilRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Perfil não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    if (request.nome() != null) {
      if (request.nome().isBlank() || request.nome().length() > 150) {
        throw new RegraNegocioException(
          "Nome deve ter entre 1 e 150 caracteres", HttpStatus.BAD_REQUEST
        );
      }
      perfil.setNome(request.nome());
    }

    if (request.foto() != null) {
      if (request.foto().length() > 500) {
        throw new RegraNegocioException(
          "Foto deve ter no máximo 500 caracteres", HttpStatus.BAD_REQUEST
        );
      }
      perfil.setFoto(request.foto());
    }

    if (request.tipo() != null) {
      if (request.tipo() != 1 && request.tipo() != 2) {
        throw new RegraNegocioException(
          "Tipo do perfil deve ser 1 ou 2", HttpStatus.BAD_REQUEST
        );
      }
      perfil.setTipo(request.tipo());
    }

    if (request.enderecoId() != null) {
      if (request.enderecoId() <= 0) {
        throw new RegraNegocioException(
          "Endereço deve possuir um ID válido", HttpStatus.BAD_REQUEST
        );
      }
      perfil.setEnderecoId(request.enderecoId());
    }

    perfilRepository.save(perfil);
  }

  @Transactional
  public void apagarPorUserId(Integer userId) {
    perfilRepository.deleteByUser_Id(userId);
  }

}
