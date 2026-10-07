-- Migração da branch 0012: senha provisória com troca obrigatória no próximo acesso.
-- Pode ser executado mais de uma vez.

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS deve_trocar_senha BOOLEAN NOT NULL DEFAULT FALSE;

-- O administrador inicial passa a ser obrigado a trocar a senha padrão "admin123".
-- (Se a senha dele já foi trocada, basta trocar de novo no próximo acesso.)
UPDATE usuario SET deve_trocar_senha = TRUE
WHERE login = 'admin'
  AND senha_hash = 'pbkdf2$120000$Ldd7cZo0DiIlLjcaDg7IQA==$5P2ymAUQH3VcPGeVFtv9FO2dlfMmNCUjABieDm9OjSY=';
