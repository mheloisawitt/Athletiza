-- Athletiza - histórico de exemplo para o Dashboard (MySQL 8) - OPCIONAL
-- Execute depois do 02_dados_exemplo.sql, com o banco athletiza selecionado.
-- Acrescenta atletas, treinos com presença, competições com resultados e eventos dos últimos meses
-- (e de gestões anteriores), para que os gráficos do Dashboard tenham o que mostrar.
-- Apenas insere dados: as tabelas não são alteradas.
-- Os testes automáticos usam só os scripts 01 e 02; este é usado apenas no teste do dashboard.

-- Mais atletas
INSERT INTO atleta (nome, matricula, contato, data_nascimento, situacao) VALUES
    ('Gabriel Rocha', '202203', 'gabriel.rocha@email.com', '2004-06-14', 'ATIVO'),
    ('Helena Duarte', '202217', '(47) 98888-2004', '2005-03-09', 'ATIVO'),
    ('Igor Batista', '202231', 'igor.batista@email.com', '2003-12-01', 'ATIVO'),
    ('Julia Ferraz', '202248', '(47) 98888-2005', '2008-07-22', 'ATIVO'),
    ('Kauã Moreira', '202256', 'kaua.moreira@email.com', '2002-10-30', 'INATIVO');

INSERT INTO atleta_modalidade (atleta_id, modalidade_id, situacao)
    SELECT a.id, m.id, v.situacao
    FROM (SELECT '202203' AS matricula, 'Basquete' AS modalidade, 'MASCULINO' AS genero, 'ATIVO' AS situacao
             UNION ALL SELECT '202203', 'Handebol', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202217', 'Vôlei', 'FEMININO', 'ATIVO'
             UNION ALL SELECT '202217', 'Atletismo', 'FEMININO', 'LESIONADO'
             UNION ALL SELECT '202231', 'Basquete', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202231', 'Atletismo', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202248', 'Futsal', 'FEMININO', 'ATIVO'
             UNION ALL SELECT '202256', 'Handebol', 'MASCULINO', 'INATIVO') AS v,
         atleta a, modalidade m
    WHERE a.matricula = v.matricula AND m.nome = v.modalidade AND m.genero = v.genero;

-- Treinos e amistosos: últimos meses, gestões anteriores e a semana atual
INSERT INTO treino (tipo, titulo, data, horario, local, modalidade_id, responsavel_id, adversario, resultado)
    SELECT v.tipo, v.titulo, CAST(v.data AS DATE), CAST(v.horario AS TIME), v.local, m.id, r.id, v.adversario, v.resultado
    FROM (SELECT 'TREINO' AS tipo, 'Treino Vôlei (F)' AS titulo, '2025-11-03' AS data, '19:00' AS horario, 'Ginásio' AS local, 'Vôlei' AS modalidade, 'FEMININO' AS genero, NULL AS adversario, NULL AS resultado
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-11-06', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2025-11-06', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2025-11-08', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2025-11-10', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-11-11', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2025-11-12', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2025-11-13', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2025-11-15', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-11-18', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2025-11-19', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-11-20', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2025-11-24', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-11-27', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2025-11-27', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2025-11-29', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-02', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2025-12-03', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-04', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2025-12-08', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-11', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2025-12-11', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2025-12-13', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2025-12-15', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-16', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2025-12-17', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2025-12-18', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2025-12-20', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-23', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2025-12-24', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2025-12-25', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2025-12-29', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-01-01', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-01-01', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-01-03', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-01-05', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-01-06', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-01-07', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-01-08', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-01-10', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-03', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-02-04', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-02-09', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-12', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-02-12', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-02-14', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-02-16', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-17', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-02-18', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-02-19', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-02-21', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-24', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-02-25', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-02-26', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-03-02', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-03-05', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-03-07', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-03-09', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-10', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-03-11', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-03-12', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-03-14', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-17', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-03-18', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-19', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-03-23', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-26', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-03-26', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-03-28', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-03-30', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-03-31', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-04-01', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-04-02', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-04-04', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-07', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-04-08', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-09', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-04-13', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-16', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-04-16', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-04-18', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-04-20', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-21', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-04-22', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-04-23', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-04-25', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-28', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-04-29', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-04-30', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-05-02', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-05-04', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-05-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-05-06', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-05-07', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-05-09', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-05-12', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-05-13', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-05-14', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-05-18', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-05-21', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-05-21', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-05-23', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-05-25', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-05-26', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-05-27', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-05-28', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-05-30', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-02', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-06-03', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-04', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-06-08', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-11', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-06-11', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-06-13', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-06-15', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-16', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-06-17', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-06-18', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-06-20', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-23', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-06-24', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-06-25', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-06-29', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-07-01', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-07-02', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-07-04', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-07-07', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-07-08', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-07-09', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-08-03', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-06', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-08-06', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-08-08', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-08-10', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-11', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-08-12', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-08-13', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-08-15', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-18', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-08-19', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-20', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-08-24', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-27', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-08-27', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-08-29', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-08-31', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-01', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-09-02', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-09-03', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-09-05', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-08', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-09-09', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-10', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-09-14', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-17', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-09-17', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-09-19', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-09-21', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-22', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-09-23', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Basquete (M)', '2026-09-24', '19:00', 'Quadra 2', 'Basquete', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-09-26', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-29', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-09-30', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Futsal (F) x Atlética Feroz', '2026-03-21', '15:00', 'Quadra 1', 'Futsal', 'FEMININO', 'Atlética Feroz', '3 x 1'
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Vôlei (F) x Atlética Tubarões', '2026-05-09', '15:00', 'Quadra 1', 'Vôlei', 'FEMININO', 'Atlética Tubarões', '2 x 3'
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Handebol (M) x Atlética Lobos', '2026-06-13', '15:00', 'Quadra 1', 'Handebol', 'MASCULINO', 'Atlética Lobos', '25 x 22'
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Basquete (M) x Atlética Pantera', '2026-08-29', '15:00', 'Quadra 1', 'Basquete', 'MASCULINO', 'Atlética Pantera', '71 x 64'
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Futsal (F) x Atlética Furiosa', '2025-12-06', '15:00', 'Quadra 1', 'Futsal', 'FEMININO', 'Atlética Furiosa', '2 x 2'
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-03-02', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-03-14', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-03-26', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-04-07', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-04-19', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-05-01', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-05-13', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-05-25', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-06-06', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-06-18', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-06-30', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-07-12', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-07-24', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-08-05', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-08-17', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-08-29', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-09-10', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-09-22', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-10-04', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-10-16', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2020-10-28', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2020-11-09', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2020-11-21', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-03-07', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-03-16', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-03-25', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-04-03', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-04-12', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-04-21', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-04-30', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-05-09', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-05-18', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-05-27', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-06-05', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-06-14', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-06-23', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-07-02', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-07-11', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-07-20', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-07-29', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-08-07', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-08-16', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-08-25', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-09-03', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-09-12', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-09-21', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-09-30', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-10-09', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-10-18', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-10-27', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-11-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-11-14', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-11-23', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-12-02', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2022-12-11', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2022-12-20', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2022-12-29', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-01-07', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-01-16', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-01-25', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-02-03', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-02-12', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-03-06', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-03-13', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-03-20', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-03-27', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-04-03', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-04-10', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-04-17', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-04-24', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-05-01', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-05-08', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-05-15', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-05-22', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-05-29', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-06-05', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-06-12', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-06-19', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-06-26', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-07-03', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-07-10', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-07-17', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-07-24', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-07-31', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-08-07', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-08-14', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-08-21', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-08-28', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-09-04', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-09-11', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-09-18', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-09-25', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-10-02', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-10-09', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-10-16', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-10-23', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-10-30', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-11-06', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-11-13', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-11-20', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-11-27', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-12-04', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2023-12-11', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2023-12-18', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2023-12-25', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2024-01-01', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2024-01-08', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2024-01-15', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2024-01-22', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2024-01-29', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2024-02-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2024-02-12', '19:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2024-02-19', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2024-02-26', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-10-15', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-10-16', '18:30', 'Ginásio', 'Vôlei', 'FEMININO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-10-17', '08:00', 'Pista', 'Atletismo', 'MASCULINO', NULL, NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-10-20', '19:00', 'Quadra 2', 'Handebol', 'MASCULINO', NULL, NULL) AS v,
         modalidade m, membro r
    WHERE m.nome = v.modalidade AND m.genero = v.genero AND r.matricula = '201902';

-- Presença nos treinos já realizados (até setembro/2026): cerca de 3 em cada 4 atletas presentes
INSERT INTO presenca_treino (treino_id, atleta_id, presente)
    SELECT t.id, am.atleta_id, MOD(t.id + am.atleta_id, 4) <> 0
    FROM treino t
    JOIN atleta_modalidade am ON am.modalidade_id = t.modalidade_id
    JOIN atleta a ON a.id = am.atleta_id AND a.situacao = 'ATIVO'
    WHERE t.data < '2026-10-01'
      AND NOT EXISTS (SELECT 1 FROM presenca_treino p WHERE p.treino_id = t.id);

-- Competições já encerradas (desta gestão e das anteriores)
INSERT INTO competicao (titulo, data, data_fim, local, situacao) VALUES
    ('Interatléticas 2022', '2022-09-17', '2022-09-18', 'Rio do Sul', 'ENCERRADA'),
    ('Copa Vale 2023', '2023-05-13', NULL, 'Ibirama', 'ENCERRADA'),
    ('Jogos de Inverno 2023', '2023-08-05', '2023-08-06', 'Blumenau', 'ENCERRADA'),
    ('JIUDESC 2025', '2025-10-18', '2025-10-21', 'Joinville', 'ENCERRADA'),
    ('Copa Integração', '2026-04-18', NULL, 'Ibirama', 'ENCERRADA'),
    ('Jogos Universitários Regionais', '2026-06-05', '2026-06-07', 'Rio do Sul', 'ENCERRADA'),
    ('Festival de Atletismo', '2026-08-22', NULL, 'Blumenau', 'ENCERRADA'),
    ('Copa Cancelada', '2026-09-12', NULL, 'Ibirama', 'CANCELADA');

INSERT INTO competicao_modalidade (competicao_id, modalidade_id)
    SELECT c.id, m.id
    FROM (SELECT 'Interatléticas 2022' AS titulo, 'Futsal' AS modalidade, 'FEMININO' AS genero
             UNION ALL SELECT 'Copa Vale 2023', 'Futsal', 'FEMININO'
             UNION ALL SELECT 'Jogos de Inverno 2023', 'Vôlei', 'FEMININO'
             UNION ALL SELECT 'JIUDESC 2025', 'Futsal', 'FEMININO'
             UNION ALL SELECT 'JIUDESC 2025', 'Vôlei', 'FEMININO'
             UNION ALL SELECT 'JIUDESC 2025', 'Atletismo', 'MASCULINO'
             UNION ALL SELECT 'Copa Integração', 'Futsal', 'FEMININO'
             UNION ALL SELECT 'Copa Integração', 'Handebol', 'MASCULINO'
             UNION ALL SELECT 'Copa Integração', 'Basquete', 'MASCULINO'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Futsal', 'FEMININO'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Vôlei', 'FEMININO'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Basquete', 'MASCULINO'
             UNION ALL SELECT 'Festival de Atletismo', 'Atletismo', 'MASCULINO'
             UNION ALL SELECT 'Festival de Atletismo', 'Atletismo', 'FEMININO') AS v,
         competicao c, modalidade m
    WHERE c.titulo = v.titulo AND m.nome = v.modalidade AND m.genero = v.genero;

-- Todos os atletas ativos de cada modalidade inscrita são inscritos (só nas competições deste script)
INSERT INTO competicao_atleta (competicao_id, modalidade_id, atleta_id)
    SELECT cm.competicao_id, cm.modalidade_id, am.atleta_id
    FROM competicao_modalidade cm
    JOIN competicao c ON c.id = cm.competicao_id AND c.situacao = 'ENCERRADA'
    JOIN atleta_modalidade am ON am.modalidade_id = cm.modalidade_id AND am.situacao = 'ATIVO';

-- Resultados: placar com os gols/pontos da atlética primeiro; colocação final na modalidade
INSERT INTO resultado (competicao_id, modalidade_id, atleta_id, colocacao, placar, observacao)
    SELECT c.id, m.id, a.id, v.colocacao, v.placar, v.observacao
    FROM (SELECT 'Interatléticas 2022' AS titulo, 'Futsal' AS modalidade, 'FEMININO' AS genero, CAST(NULL AS CHAR(20)) AS matricula,
                 2 AS colocacao, '1 x 2' AS placar, 'Final' AS observacao
             UNION ALL SELECT 'Copa Vale 2023', 'Futsal', 'FEMININO', NULL, 1, '4 x 0', 'Final'
             UNION ALL SELECT 'Jogos de Inverno 2023', 'Vôlei', 'FEMININO', NULL, 3, '3 x 1', 'Disputa do 3º lugar'
             UNION ALL SELECT 'JIUDESC 2025', 'Futsal', 'FEMININO', NULL, NULL, '2 x 2', 'Fase de grupos'
             UNION ALL SELECT 'JIUDESC 2025', 'Futsal', 'FEMININO', NULL, 2, '0 x 1', 'Final'
             UNION ALL SELECT 'JIUDESC 2025', 'Vôlei', 'FEMININO', NULL, NULL, '1 x 3', 'Quartas de final'
             UNION ALL SELECT 'JIUDESC 2025', 'Atletismo', 'MASCULINO', '202067', 1, NULL, '100 m rasos'
             UNION ALL SELECT 'Copa Integração', 'Futsal', 'FEMININO', NULL, 1, '3 x 1', 'Final'
             UNION ALL SELECT 'Copa Integração', 'Handebol', 'MASCULINO', NULL, NULL, '18 x 18', 'Fase de grupos'
             UNION ALL SELECT 'Copa Integração', 'Handebol', 'MASCULINO', NULL, 3, '22 x 19', 'Disputa do 3º lugar'
             UNION ALL SELECT 'Copa Integração', 'Basquete', 'MASCULINO', NULL, NULL, '58 x 66', 'Semifinal'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Futsal', 'FEMININO', NULL, NULL, '5 x 2', 'Semifinal'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Futsal', 'FEMININO', NULL, 1, '2 x 1', 'Final'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Vôlei', 'FEMININO', NULL, 2, '2 x 3', 'Final'
             UNION ALL SELECT 'Jogos Universitários Regionais', 'Basquete', 'MASCULINO', NULL, 3, '70 x 61', 'Disputa do 3º lugar'
             UNION ALL SELECT 'Festival de Atletismo', 'Atletismo', 'MASCULINO', '202067', 2, NULL, '200 m rasos'
             UNION ALL SELECT 'Festival de Atletismo', 'Atletismo', 'MASCULINO', '202231', 3, NULL, 'Salto em distância'
             UNION ALL SELECT 'Festival de Atletismo', 'Atletismo', 'FEMININO', NULL, 2, NULL, 'Revezamento 4x100') AS v
         JOIN competicao c ON c.titulo = v.titulo
         JOIN modalidade m ON m.nome = v.modalidade AND m.genero = v.genero
         LEFT JOIN atleta a ON a.matricula = v.matricula;

-- Eventos já realizados (desta gestão e das anteriores)
INSERT INTO evento (titulo, tipo_evento, data, horario, local, situacao) VALUES
    ('Calourada 2020', 'FESTA', '2020-03-20', '21:00', 'Ibirama', 'REALIZADO'),
    ('Calourada 2022', 'FESTA', '2022-03-18', '21:00', 'Ibirama', 'REALIZADO'),
    ('Campanha do Agasalho 2022', 'ACAO_SOCIAL', '2022-06-11', '09:00', 'Ibirama', 'REALIZADO'),
    ('Calourada 2023', 'FESTA', '2023-03-17', '21:00', 'Ibirama', 'REALIZADO'),
    ('Plantio de Mudas 2023', 'ACAO_AMBIENTAL', '2023-06-05', '08:00', 'Ibirama', 'REALIZADO'),
    ('Festa Junina 2023', 'FESTA', '2023-06-24', '19:00', 'Ibirama', 'REALIZADO'),
    ('Doação de Sangue 2023', 'ACAO_SOCIAL', '2023-09-14', '08:00', 'Rio do Sul', 'REALIZADO'),
    ('Festa de Halloween', 'FESTA', '2025-10-31', '22:00', 'Ibirama', 'REALIZADO'),
    ('Arrecadação de Natal', 'ACAO_SOCIAL', '2025-12-13', '09:00', 'Ibirama', 'REALIZADO'),
    ('Calourada 2026', 'FESTA', '2026-03-13', '21:00', 'Ibirama', 'REALIZADO'),
    ('Gincana dos Calouros', 'COMPETICAO', '2026-03-28', '14:00', 'Ibirama', 'REALIZADO'),
    ('Mutirão de Limpeza do Rio', 'ACAO_AMBIENTAL', '2026-04-25', '08:00', 'Ibirama', 'REALIZADO'),
    ('Festa Junina', 'FESTA', '2026-06-20', '19:00', 'Ibirama', 'REALIZADO'),
    ('Campanha do Agasalho', 'ACAO_SOCIAL', '2026-07-11', '09:00', 'Ibirama', 'REALIZADO'),
    ('Torneio de Truco', 'OUTRO', '2026-08-15', '15:00', 'Sala da atlética', 'REALIZADO'),
    ('Doação de Sangue', 'ACAO_SOCIAL', '2026-09-10', '08:00', 'Rio do Sul', 'REALIZADO'),
    ('Festa Neon', 'FESTA', '2026-09-26', '22:00', 'Ibirama', 'CANCELADO');
