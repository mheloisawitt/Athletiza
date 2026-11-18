-- Athletiza - atualização de um banco já existente (MySQL 8)
-- Execute UMA VEZ, com o banco athletiza selecionado, se o banco foi criado antes desta versão.
-- Quem está criando o banco do zero não precisa: o 01_estrutura.sql já tem estas colunas.
--
-- O que muda:
--   - demanda.solicitante_id: quem registrou a demanda (RN38). Só ele, a presidência e o
--     administrador podem cancelar ou excluir a demanda (RN40).

/*!40101 SET NAMES utf8mb4 */;

ALTER TABLE demanda ADD COLUMN solicitante_id INTEGER NULL;
ALTER TABLE demanda ADD CONSTRAINT fk_demanda_solicitante
    FOREIGN KEY (solicitante_id) REFERENCES usuario (id) ON DELETE SET NULL;

-- Demandas antigas ficam com a presidente dos dados de exemplo como solicitante (se ela existir).
UPDATE demanda SET solicitante_id = (SELECT id FROM usuario WHERE login = 'mariana') WHERE solicitante_id IS NULL;
