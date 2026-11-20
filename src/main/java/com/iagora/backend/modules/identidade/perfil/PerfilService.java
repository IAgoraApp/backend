package com.iagora.backend.modules.identidade.perfil;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.endereco.EnderecoRequest;
import com.iagora.backend.modules.endereco.EnderecoResponse;
import com.iagora.backend.modules.endereco.EnderecoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PerfilService {

  private final PerfilRepository perfilRepository;
  private final EnderecoService enderecos;
  private final EntityManager entityManager;

  public PerfilService(PerfilRepository perfilRepository, EnderecoService enderecos, EntityManager entityManager) {
    this.perfilRepository = perfilRepository;
    this.enderecos = enderecos;
    this.entityManager = entityManager;
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
    Perfil perfil = buscar(id);

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
      if (!request.enderecoId().equals(perfil.getEnderecoId())) {
        enderecos.verificarProprietario(request.enderecoId(), perfil.getUser().getId());
      }
      perfil.setEnderecoId(request.enderecoId());
    }

    perfilRepository.save(perfil);
  }

  @Transactional
  public EnderecoResponse consultarEndereco(Integer id) {
    return enderecos.buscarSeExistir(buscar(id).getEnderecoId());
  }

  @Transactional
  public void salvarEndereco(Integer id, EnderecoRequest endereco) {
    Perfil perfil = buscar(id);
    entityManager.refresh(perfil, LockModeType.PESSIMISTIC_WRITE);
    perfil.setEnderecoId(enderecos.salvarResidencia(perfil.getEnderecoId(), perfil.getUser().getId(), endereco));
  }

  @Transactional
  public void removerEndereco(Integer id) {
    Perfil perfil = buscar(id);
    entityManager.refresh(perfil, LockModeType.PESSIMISTIC_WRITE);
    perfil.setEnderecoId(null);
  }

  private Perfil buscar(Integer id) {
    Perfil perfil = perfilRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Perfil não encontrado",
          HttpStatus.NOT_FOUND
        )
      );
    return perfil;
  }

  @Transactional
  public void apagarPorUserId(Integer userId) {
    perfilRepository.deleteByUser_Id(userId);
  }

}
