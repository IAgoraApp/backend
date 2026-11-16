package com.iagora.backend.modules.endereco;

import com.iagora.backend.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EnderecoService {

  private final EnderecoRepository enderecoRepository;

  public EnderecoService(EnderecoRepository enderecoRepository) {
    this.enderecoRepository = enderecoRepository;
  }

  @Transactional
  public EnderecoResponse cadastrar(EnderecoRequest request) {
    Endereco endereco = new Endereco();

    endereco.setEstado(request.estado());
    endereco.setRua(request.rua());
    endereco.setBairro(request.bairro());
    if (request.pais() != null) {
      endereco.setPais(request.pais());
    }
    endereco.setCep(request.cep());
    endereco.setCidade(request.cidade());
    endereco.setNumero(request.numero());
    endereco.setComplemento(request.complemento());

    validar(endereco);
    endereco = enderecoRepository.save(endereco);

    return new EnderecoResponse(
      endereco.getId(),
      endereco.getEstado(),
      endereco.getRua(),
      endereco.getBairro(),
      endereco.getPais(),
      endereco.getCep(),
      endereco.getCidade(),
      endereco.getNumero(),
      endereco.getComplemento()
    );
  }

  public EnderecoResponse buscarPorId(Integer id) {
    Endereco endereco = enderecoRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Endereço não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    return new EnderecoResponse(
      endereco.getId(),
      endereco.getEstado(),
      endereco.getRua(),
      endereco.getBairro(),
      endereco.getPais(),
      endereco.getCep(),
      endereco.getCidade(),
      endereco.getNumero(),
      endereco.getComplemento()
    );
  }

  @Transactional
  public void atualizar(Integer id, EnderecoRequest request) {
    Endereco endereco = enderecoRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Endereço não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    if (request.estado() != null) {
      endereco.setEstado(request.estado());
    }

    if (request.rua() != null) {
      endereco.setRua(request.rua());
    }

    if (request.bairro() != null) {
      endereco.setBairro(request.bairro());
    }

    if (request.pais() != null) {
      endereco.setPais(request.pais());
    }

    if (request.cep() != null) {
      endereco.setCep(request.cep());
    }

    if (request.cidade() != null) {
      endereco.setCidade(request.cidade());
    }

    if (request.numero() != null) {
      endereco.setNumero(request.numero());
    }

    if (request.complemento() != null) {
      endereco.setComplemento(request.complemento());
    }

    validar(endereco);
    enderecoRepository.save(endereco);
  }

  @Transactional
  public void apagarEndereco(Integer id) {
    Endereco endereco = enderecoRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Endereço não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    enderecoRepository.delete(endereco);
  }

  private void validar(Endereco endereco) {
    if (
      endereco.getEstado() == null || endereco.getEstado().isBlank() ||
      endereco.getRua() == null || endereco.getRua().isBlank() ||
      endereco.getBairro() == null || endereco.getBairro().isBlank() ||
      endereco.getPais() == null || endereco.getPais().isBlank() ||
      endereco.getCep() == null || endereco.getCep().isBlank() ||
      endereco.getCidade() == null || endereco.getCidade().isBlank()
    ) {
      throw new RegraNegocioException(
        "Estado, rua, bairro, país, CEP e cidade são obrigatórios",
        HttpStatus.BAD_REQUEST
      );
    }

  }
}
