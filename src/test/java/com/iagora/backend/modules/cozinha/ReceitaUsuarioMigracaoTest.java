package com.iagora.backend.modules.cozinha;

import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.*;

class ReceitaUsuarioMigracaoTest {

  @Test
  void migraAndamentoAntigoSemPublicarReceitasNemDuplicarRelacoes() throws Exception {
    try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:migracao;MODE=MySQL", "sa", "")) {
      try (var sql = connection.createStatement()) {
        sql.execute("CREATE TABLE users (id INT PRIMARY KEY)");
        sql.execute("CREATE TABLE receitas (id INT PRIMARY KEY, user_id INT, status VARCHAR(255), publica BOOLEAN)");
        sql.execute("INSERT INTO users VALUES (1), (2)");
        sql.execute("INSERT INTO receitas VALUES (10, 1, 'fazendo', true), (11, 2, 'feita', false), " +
          "(12, null, 'fazer', true), (13, 1, 'publicada', true)");
      }

      // H2 inicia a transação via JDBC; o START TRANSACTION do arquivo é sintaxe MySQL.
      connection.setAutoCommit(false);
      String sqlMigracao = Files.readString(Path.of("docs/sql/separar-status-receita.sql"))
        .replace("START TRANSACTION;", "");
      var script = new ByteArrayResource(sqlMigracao.getBytes(StandardCharsets.UTF_8));
      ScriptUtils.executeSqlScript(connection, script);
      ScriptUtils.executeSqlScript(connection, script);

      try (var sql = connection.createStatement()) {
        try (var rows = sql.executeQuery("SELECT user_id, receita_id, status FROM receita_usuario ORDER BY receita_id")) {
          assertTrue(rows.next());
          assertEquals(1, rows.getInt("user_id"));
          assertEquals(10, rows.getInt("receita_id"));
          assertEquals("fazendo", rows.getString("status"));
          assertTrue(rows.next());
          assertEquals(2, rows.getInt("user_id"));
          assertEquals(11, rows.getInt("receita_id"));
          assertEquals("feita", rows.getString("status"));
          assertFalse(rows.next());
        }
        try (var rows = sql.executeQuery("SELECT status FROM receitas ORDER BY id")) {
          for (int i = 0; i < 3; i++) {
            assertTrue(rows.next());
            assertEquals("rascunho", rows.getString("status"));
          }
          assertTrue(rows.next());
          assertEquals("publicada", rows.getString("status"));
        }
        // Uma execução posterior não deve sobrescrever o andamento pessoal existente.
        sql.execute("UPDATE receitas SET status = 'feita' WHERE id = 10");
      }
      ScriptUtils.executeSqlScript(connection, script);
      try (var sql = connection.createStatement();
           var rows = sql.executeQuery("SELECT status FROM receita_usuario WHERE receita_id = 10")) {
        assertTrue(rows.next());
        assertEquals("fazendo", rows.getString("status"));
      }

      String sqlRemocao = Files.readString(Path.of("docs/sql/remover-status-receita.sql"))
        .replace("START TRANSACTION;", "");
      ScriptUtils.executeSqlScript(connection,
        new ByteArrayResource(sqlRemocao.getBytes(StandardCharsets.UTF_8)));
      try (var sql = connection.createStatement()) {
        try (var rows = sql.executeQuery("SELECT publica FROM receitas ORDER BY id")) {
          for (int i = 0; i < 3; i++) {
            assertTrue(rows.next());
            assertFalse(rows.getBoolean("publica"));
          }
          assertTrue(rows.next());
          assertTrue(rows.getBoolean("publica"));
        }
        try (var rows = sql.executeQuery("SELECT COUNT(*) FROM information_schema.columns " +
          "WHERE table_name = 'RECEITAS' AND column_name = 'STATUS'")) {
          assertTrue(rows.next());
          assertEquals(0, rows.getInt(1));
        }
        try (var rows = sql.executeQuery("SELECT status FROM receita_usuario WHERE receita_id = 10")) {
          assertTrue(rows.next());
          assertEquals("fazendo", rows.getString("status"));
        }
      }
    }
  }
}
