package com.iagora.backend.modules.endereco;

import com.iagora.backend.exception.RegraNegocioException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnderecoServiceTest {

  private EnderecoRepository enderecoRepository;
  private EnderecoService enderecoService;

  @BeforeEach
  void configurar() {
    enderecoRepository = mock(EnderecoRepository.class);
    enderecoService = new EnderecoService(enderecoRepository);
  }

  @Test
  void cadastrarRetornaIdEPaisPadrao() {
    when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> {
      Endereco endereco = spy((Endereco) invocation.getArgument(0));
      when(endereco.getId()).thenReturn(1);
      return endereco;
    });

    EnderecoResponse response = enderecoService.cadastrar(new EnderecoRequest(
      "SP", "Rua das Flores", "Centro", null, "01001-000", "São Paulo", null, null
    ));

    assertEquals(1, response.id());
    assertEquals("Brasil", response.pais());
    assertEquals("Rua das Flores", response.rua());
  }

  @Test
  void cadastrarRejeitaCamposObrigatoriosAusentesOuEmBranco() {
    for (int campo = 0; campo < 6; campo++) {
      for (String invalido : new String[] { null, "", " " }) {
        if (campo == 3 && invalido == null) {
          continue;
        }
        String[] valores = { "SP", "Rua das Flores", "Centro", "Brasil", "01001-000", "São Paulo" };
        valores[campo] = invalido;
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () ->
          enderecoService.cadastrar(new EnderecoRequest(
            valores[0], valores[1], valores[2], valores[3], valores[4], valores[5], null, null
          ))
        );
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
      }
    }
    verify(enderecoRepository, never()).save(any());
  }

  @Test
  void atualizarPreservaCamposNaoInformados() {
    Endereco endereco = new Endereco();
    endereco.setCidade("São Paulo");
    endereco.setEstado("SP");
    endereco.setBairro("Centro");
    endereco.setCep("01001-000");
    endereco.setRua("Rua das Flores");
    when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

    enderecoService.atualizar(1, new EnderecoRequest(
      null, null, null, null, null, null, "123", "Apartamento 2"
    ));

    assertEquals("São Paulo", endereco.getCidade());
    assertEquals("Rua das Flores", endereco.getRua());
    assertEquals("123", endereco.getNumero());
    assertEquals("Apartamento 2", endereco.getComplemento());
    verify(enderecoRepository).save(endereco);
  }

  @Test
  void atualizarRejeitaCampoObrigatorioEmBranco() {
    Endereco endereco = new Endereco();
    endereco.setEstado("SP");
    endereco.setRua("Rua das Flores");
    endereco.setBairro("Centro");
    endereco.setCep("01001-000");
    endereco.setCidade("São Paulo");
    when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

    RegraNegocioException vazio = assertThrows(RegraNegocioException.class, () ->
      enderecoService.atualizar(1, new EnderecoRequest(
        null, " ", null, null, null, null, null, null
      ))
    );

    assertEquals(HttpStatus.BAD_REQUEST, vazio.getStatus());
    verify(enderecoRepository, never()).save(any());
  }

  @Test
  void operacoesRetornamNaoEncontradoParaIdInexistente() {
    when(enderecoRepository.findById(1)).thenReturn(Optional.empty());

    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () -> enderecoService.buscarPorId(1)).getStatus());
    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () -> enderecoService.atualizar(1,
        new EnderecoRequest(null, null, null, null, null, null, null, null))).getStatus());
    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () -> enderecoService.apagarEndereco(1)).getStatus());
    verify(enderecoRepository, never()).delete(any());
  }

  @Test
  void apagarRemoveEnderecoEncontrado() {
    Endereco endereco = new Endereco();
    when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

    enderecoService.apagarEndereco(1);

    verify(enderecoRepository).delete(endereco);
  }
}
