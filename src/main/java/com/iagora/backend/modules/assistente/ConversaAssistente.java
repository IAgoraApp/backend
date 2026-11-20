package com.iagora.backend.modules.assistente;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "assistente_conversas", uniqueConstraints =
  @UniqueConstraint(columnNames = {"usuario_id", "chave"}))
class ConversaAssistente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User usuario;

  @Column(nullable = false)
  private UUID chave;

  @Column(nullable = false, length = 70)
  private String titulo;

  @Column(nullable = false, columnDefinition = "longtext")
  private String mensagens;

  @Column(nullable = false)
  private Instant atualizada;

  protected ConversaAssistente() {}

  ConversaAssistente(User usuario, UUID chave) {
    this.usuario = usuario;
    this.chave = chave;
  }

  void atualizar(String titulo, String mensagens) {
    this.titulo = titulo;
    this.mensagens = mensagens;
    this.atualizada = Instant.now();
  }

  UUID chave() { return chave; }
  String titulo() { return titulo; }
  String mensagens() { return mensagens; }
}
