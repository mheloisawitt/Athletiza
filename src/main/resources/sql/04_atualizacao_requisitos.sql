/*!40101 SET NAMES utf8mb4 */;

ALTER TABLE demanda ADD COLUMN solicitante_id INTEGER NULL;
ALTER TABLE demanda ADD CONSTRAINT fk_demanda_solicitante
    FOREIGN KEY (solicitante_id) REFERENCES usuario (id) ON DELETE SET NULL;

UPDATE demanda SET solicitante_id = (SELECT id FROM usuario WHERE login = 'mariana') WHERE solicitante_id IS NULL;
