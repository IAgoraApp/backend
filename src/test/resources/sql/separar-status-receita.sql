-- Dados da migração histórica, usados somente pelo teste.
CREATE TABLE IF NOT EXISTS receita_usuario (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  receita_id INT NOT NULL,
  status VARCHAR(255) NOT NULL DEFAULT 'fazer',
  PRIMARY KEY (id),
  UNIQUE KEY uk_receita_usuario (user_id, receita_id),
  CONSTRAINT fk_receita_usuario_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_receita_usuario_receita FOREIGN KEY (receita_id) REFERENCES receitas(id)
);
START TRANSACTION;
INSERT INTO receita_usuario (user_id, receita_id, status)
SELECT r.user_id, r.id, r.status
FROM receitas r
LEFT JOIN receita_usuario ru ON ru.user_id = r.user_id AND ru.receita_id = r.id
WHERE r.user_id IS NOT NULL
  AND r.status IN ('fazer', 'fazendo', 'feita')
  AND ru.id IS NULL;
UPDATE receitas SET status = 'rascunho'
WHERE status IN ('fazer', 'fazendo', 'feita');
COMMIT;
