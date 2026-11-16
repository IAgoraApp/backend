package com.iagora.backend.modules.casa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoRepository;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRepository;
import com.iagora.backend.modules.casa.limpeza.execucaoLimpeza.ExecucaoLimpezaRepository;
import com.iagora.backend.modules.casa.casa.Casa;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.casa.CasaRequest;
import com.iagora.backend.modules.casa.casa.CasaResponse;
import com.iagora.backend.modules.casa.casa.CasaService;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRepository;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasRepository;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemRepository;
import com.iagora.backend.modules.endereco.EnderecoService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CasaServiceTest {

  private CasaRepository casaRepository;
  private EnderecoService enderecoService;
  private CasaService casaService;
  private User user;

  @BeforeEach
  void configurar() {
    casaRepository = mock(CasaRepository.class);
    enderecoService = mock(EnderecoService.class);
    casaService = new CasaService(casaRepository, enderecoService, mock(ComprasRepository.class),
      mock(CompraItemRepository.class), mock(EstoqueItemRepository.class), mock(ComodoRepository.class),
      mock(LimpezaRepository.class), mock(ExecucaoLimpezaRepository.class));
    user = mock(User.class);
    when(user.getId()).thenReturn(1);
  }

  @Test
  void criarCasaComValoresPadrao() {
    casaService.criar(user);

    var captor = org.mockito.ArgumentCaptor.forClass(Casa.class);
    verify(casaRepository).save(captor.capture());
    assertEquals(user, captor.getValue().getUser());
    assertEquals("Minha casa", captor.getValue().getNome());
    assertEquals("Casa", captor.getValue().getTipo());
    assertNull(captor.getValue().getEnderecoId());
  }

  @Test
  void naoCriarCasaDuplicada() {
    when(casaRepository.existsByUser_Id(1)).thenReturn(true);

    assertEquals(HttpStatus.CONFLICT,
      assertThrows(RegraNegocioException.class, () -> casaService.criar(user)).getStatus());
    verify(casaRepository, never()).save(any());
  }

  @Test
  void buscarSomenteCasasDoUsuario() {
    when(casaRepository.findByUser_Id(1)).thenReturn(List.of(new Casa(user, "Minha casa", "Casa")));

    List<CasaResponse> casas = casaService.buscarPorUserId(1);

    assertEquals(1, casas.size());
    assertEquals(1, casas.getFirst().userId());
    assertTrue(casaService.buscarPorUserId(2).isEmpty());
  }

  @Test
  void atualizarSomenteCamposInformados() {
    Casa casa = new Casa(user, "Minha casa", "Casa");
    when(casaRepository.findById(10)).thenReturn(Optional.of(casa));

    casaService.atualizar(10, new CasaRequest(null, "Apartamento", 3));

    assertEquals("Minha casa", casa.getNome());
    assertEquals("Apartamento", casa.getTipo());
    assertEquals(3, casa.getEnderecoId());
    verify(enderecoService).buscarPorId(3);
    verify(casaRepository).save(casa);
  }

  @Test
  void rejeitarCamposEmBrancoEEnderecoInexistente() {
    Casa casa = new Casa(user, "Minha casa", "Casa");
    when(casaRepository.findById(10)).thenReturn(Optional.of(casa));
    when(enderecoService.buscarPorId(3)).thenThrow(
      new RegraNegocioException("Endereço não encontrado", HttpStatus.NOT_FOUND)
    );

    for (CasaRequest request : List.of(
      new CasaRequest(" ", null, null), new CasaRequest(null, " ", null)
    )) {
      assertEquals(HttpStatus.BAD_REQUEST,
        assertThrows(RegraNegocioException.class, () -> casaService.atualizar(10, request)).getStatus());
    }
    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () ->
        casaService.atualizar(10, new CasaRequest(null, null, 3))).getStatus());
    verify(casaRepository, never()).save(any());
  }

  @Test
  void retornarNaoEncontradoParaCasaInexistente() {
    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () -> casaService.buscarPorId(10)).getStatus());
    assertEquals(HttpStatus.NOT_FOUND,
      assertThrows(RegraNegocioException.class, () ->
        casaService.atualizar(10, new CasaRequest("Casa", null, null))).getStatus());
  }
}
