-- Athletiza - criação do banco de dados (MySQL 8)
-- Execute uma vez, conectado como root (no Workbench: abra a conexão e rode este script).
-- Depois execute 01_estrutura.sql e 02_dados_exemplo.sql.
--
-- utf8mb4 guarda qualquer caractere (acentos, ç, emojis). A comparação utf8mb4_0900_ai_ci ignora
-- maiúsculas/minúsculas e acentos: "vôlei" e "Volei" são considerados iguais nas buscas e nos campos únicos.

CREATE DATABASE IF NOT EXISTS athletiza
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE athletiza;
