-- Dados da migração histórica, usados somente pelo teste.
START TRANSACTION;
UPDATE receitas SET publica = false WHERE status <> 'publicada';
COMMIT;
ALTER TABLE receitas DROP COLUMN status;
