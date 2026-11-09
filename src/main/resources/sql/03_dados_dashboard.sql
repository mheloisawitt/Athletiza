-- Athletiza - dados de exemplo extras para o Dashboard (opcional) - MySQL 8
-- Execute UMA VEZ, depois do 02_dados_exemplo.sql, com o banco athletiza selecionado.
-- Acrescenta atletas, treinos e amistosos de outras modalidades, listas de presença
-- e demandas com responsáveis de vários setores, para os gráficos do dashboard terem o que mostrar.
-- Para voltar ao início: execute de novo o 01_estrutura.sql e o 02_dados_exemplo.sql.

-- Lê este arquivo como UTF-8 (acentos corretos também pelo terminal; o Workbench já usa UTF-8).
/*!40101 SET NAMES utf8mb4 */;

USE athletiza;

-- Atletas
INSERT INTO atleta (nome, matricula, contato, data_nascimento) VALUES
    ('Gabriel Santos', '202201', 'gabriel.santos@email.com', '2004-03-14'),
    ('Henrique Dias', '202212', '(47) 98888-3001', '2003-07-02'),
    ('Igor Ramos', '202223', 'igor.ramos@email.com', '2005-01-28'),
    ('João Pedro Nunes', '202234', '(47) 98888-3002', '2002-12-09'),
    ('Larissa Melo', '202245', 'larissa.melo@email.com', '2004-06-19'),
    ('Marina Duarte', '202256', '(47) 98888-3003', '2003-10-11'),
    ('Bruno Farias', '202267', 'bruno.farias@email.com', '2004-08-23');

INSERT INTO atleta_modalidade (atleta_id, modalidade_id, situacao)
    SELECT a.id, m.id, v.situacao
    FROM (SELECT '202201' AS matricula, 'Futsal' AS modalidade, 'MASCULINO' AS genero, 'ATIVO' AS situacao
             UNION ALL SELECT '202212', 'Futsal', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202223', 'Futsal', 'MASCULINO', 'LESIONADO'
             UNION ALL SELECT '202223', 'Handebol', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202267', 'Handebol', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202067', 'Futsal', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202234', 'Vôlei', 'MASCULINO', 'ATIVO'
             UNION ALL SELECT '202245', 'Vôlei', 'FEMININO', 'ATIVO'
             UNION ALL SELECT '202256', 'Atletismo', 'FEMININO', 'ATIVO') AS v,
         atleta a, modalidade m
    WHERE a.matricula = v.matricula AND m.nome = v.modalidade AND m.genero = v.genero;

-- Treinos e amistosos de outras modalidades (setembro a novembro/2026)
INSERT INTO treino (tipo, titulo, data, horario, local, modalidade_id, responsavel_id, adversario)
    SELECT v.tipo, v.titulo, CAST(v.data AS DATE), CAST(v.horario AS TIME), v.local, m.id, r.id, v.adversario
    FROM (SELECT 'TREINO' AS tipo, 'Treino Vôlei (F)' AS titulo, '2026-09-15' AS data, '18:00' AS horario,
                 'Ginásio' AS local, 'Vôlei' AS modalidade, 'FEMININO' AS genero, NULL AS adversario
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-09-22', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-10-13', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Vôlei (F) x Atlética Leões', '2026-10-24', '15:00', 'Ginásio',
                              'Vôlei', 'FEMININO', 'Atlética Leões'
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-11-03', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-09-16', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-09-23', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-10-14', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-10-21', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Futsal (M) x Atlética Furiosa', '2026-10-25', '16:00', 'Quadra 1',
                              'Futsal', 'MASCULINO', 'Atlética Furiosa'
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Futsal (M) x Atlética Tubarões', '2026-11-07', '16:00', 'Quadra 1',
                              'Futsal', 'MASCULINO', 'Atlética Tubarões'
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-09-18', '19:30', 'Ginásio', 'Handebol', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-10-16', '19:30', 'Ginásio', 'Handebol', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-10-17', '07:00', 'Pista', 'Atletismo', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-07-08', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-07-22', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-07-15', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-07-21', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-05', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-08-19', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-08-12', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-08-26', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-08-18', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Handebol (M)', '2026-08-20', '19:30', 'Ginásio', 'Handebol', 'MASCULINO', NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Vôlei (F) x Atlética Leões', '2026-08-29', '15:00', 'Ginásio',
                              'Vôlei', 'FEMININO', 'Atlética Leões'
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-09-09', '19:00', 'Quadra 1', 'Futsal', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-09-12', '07:00', 'Pista', 'Atletismo', 'MASCULINO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Vôlei (F)', '2026-11-12', '18:00', 'Ginásio', 'Vôlei', 'FEMININO', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (M)', '2026-11-18', '21:00', 'Quadra 1', 'Futsal', 'MASCULINO', NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Handebol (M) x Atlética Feras', '2026-11-21', '16:00', 'Ginásio',
                              'Handebol', 'MASCULINO', 'Atlética Feras')
         AS v, modalidade m, membro r
    WHERE m.nome = v.modalidade AND m.genero = v.genero AND r.matricula = '201902';

-- Listas de presença dos treinos já realizados que ainda não têm lista.
-- Algumas faltas variam pelo número do treino; Carla e Henrique faltam mais, para aparecerem no gráfico.
INSERT INTO presenca_treino (treino_id, atleta_id, presente)
    SELECT t.id, a.id,
           CASE WHEN a.matricula = '202190' THEN MOD(t.id, 3) = 0
                WHEN a.matricula = '202212' THEN MOD(t.id, 2) = 0
                ELSE MOD(t.id + a.id, 5) <> 0 END
    FROM treino t
    JOIN atleta_modalidade am ON am.modalidade_id = t.modalidade_id AND am.situacao = 'ATIVO'
    JOIN atleta a ON a.id = am.atleta_id
    WHERE t.data <= '2026-11-08'
      AND NOT EXISTS (SELECT 1 FROM presenca_treino p WHERE p.treino_id = t.id);

-- Eventos já realizados (para a linha do tempo) e mais um agendado
INSERT INTO evento (titulo, tipo_evento, data, horario, local, situacao) VALUES
    ('Festa Julina da Atlética', 'FESTA', '2026-07-11', '20:00', 'Ibirama', 'REALIZADO'),
    ('Plantio de mudas', 'ACAO_AMBIENTAL', '2026-08-22', '09:00', 'Parque Municipal', 'REALIZADO'),
    ('Campanha do agasalho', 'ACAO_SOCIAL', '2026-07-25', '14:00', 'Ibirama', 'REALIZADO'),
    ('Calourada', 'FESTA', '2026-08-15', '21:00', 'Ibirama', 'REALIZADO'),
    ('Limpeza do rio', 'ACAO_AMBIENTAL', '2026-11-28', '08:00', 'Rio Itajaí', 'PLANEJADO');

-- Competição já encerrada e resultados (pódios)
INSERT INTO competicao (titulo, data, data_fim, local, situacao) VALUES
    ('Copa de Inverno', '2026-07-18', '2026-07-19', 'Ibirama', 'ENCERRADA');

INSERT INTO competicao_modalidade (competicao_id, modalidade_id)
    SELECT c.id, m.id FROM competicao c, modalidade m
    WHERE c.titulo = 'Copa de Inverno'
      AND ((m.nome IN ('Futsal', 'Vôlei')) OR (m.nome = 'Handebol' AND m.genero = 'MASCULINO'));

INSERT INTO resultado (competicao_id, modalidade_id, colocacao, placar)
    SELECT c.id, m.id, v.colocacao, v.placar
    FROM (SELECT 'Copa de Inverno' AS competicao, 'Futsal' AS modalidade, 'FEMININO' AS genero, 1 AS colocacao, '3 x 1' AS placar
             UNION ALL SELECT 'Copa de Inverno', 'Futsal', 'MASCULINO', 2, '2 x 4'
             UNION ALL SELECT 'Copa de Inverno', 'Vôlei', 'FEMININO', 1, '3 x 0'
             UNION ALL SELECT 'Copa de Inverno', 'Handebol', 'MASCULINO', 3, '25 x 22'
             UNION ALL SELECT 'JIUDESC 2026', 'Futsal', 'FEMININO', 1, '2 x 0'
             UNION ALL SELECT 'JIUDESC 2026', 'Futsal', 'MASCULINO', 3, '1 x 0'
             UNION ALL SELECT 'JIUDESC 2026', 'Vôlei', 'FEMININO', 2, '1 x 3'
             UNION ALL SELECT 'JIUDESC 2026', 'Vôlei', 'MASCULINO', 3, '3 x 2'
             UNION ALL SELECT 'JIUDESC 2026', 'Handebol', 'MASCULINO', 2, '20 x 24') AS v,
         competicao c, modalidade m
    WHERE c.titulo = v.competicao AND m.nome = v.modalidade AND m.genero = v.genero;

-- Resultados individuais do atletismo no JIUDESC
INSERT INTO resultado (competicao_id, modalidade_id, atleta_id, colocacao, placar)
    SELECT c.id, m.id, a.id, v.colocacao, v.placar
    FROM (SELECT 'Atletismo' AS modalidade, 'MASCULINO' AS genero, '202067' AS matricula, 1 AS colocacao, '100 m: 11,2 s' AS placar
             UNION ALL SELECT 'Atletismo', 'FEMININO', '202256', 2, '200 m: 26,8 s'
             UNION ALL SELECT 'Atletismo', 'FEMININO', '202256', 1, 'Salto em distância: 5,40 m') AS v,
         competicao c, modalidade m, atleta a
    WHERE c.titulo = 'JIUDESC 2026' AND m.nome = v.modalidade AND m.genero = v.genero AND a.matricula = v.matricula;

-- Mais membros na gestão atual
INSERT INTO membro (nome, matricula, contato) VALUES
    ('Tiago Moreira', '202111', 'tiago.moreira@email.com');

INSERT INTO gestao_membro_cargo (gestao_id, membro_id, cargo_id)
    SELECT g.id, m.id, c.id
    FROM gestao g, membro m, cargo c
    WHERE g.nome = 'Gestão 2025-2026' AND m.matricula = '202111' AND c.nome = 'Secretário';

-- Demandas (as abertas e em andamento entram no gráfico de setores)
INSERT INTO demanda (titulo, prioridade, situacao, prazo, evento_id, competicao_id)
    SELECT v.titulo, v.prioridade, v.situacao, CAST(v.prazo AS DATE), e.id, c.id
    FROM (SELECT 'Inscrever atletas no Regional' AS titulo, 'ALTA' AS prioridade, 'EM_ANDAMENTO' AS situacao,
                 '2026-11-01' AS prazo, NULL AS evento, 'Campeonato Regional' AS competicao
             UNION ALL SELECT 'Conseguir uniformes', 'MEDIA', 'ABERTA', '2026-11-06', NULL, 'Campeonato Regional'
             UNION ALL SELECT 'Organizar hospedagem', 'ALTA', 'ABERTA', '2026-11-12', NULL, 'Campeonato Regional'
             UNION ALL SELECT 'Agendar exames médicos', 'MEDIA', 'ABERTA', '2026-11-18', NULL, 'Copa Alto Vale'
             UNION ALL SELECT 'Montar as equipes', 'MEDIA', 'ABERTA', '2026-11-25', NULL, 'Copa Alto Vale'
             UNION ALL SELECT 'Definir fotógrafo', 'BAIXA', 'ABERTA', '2026-12-01', NULL, 'Copa Alto Vale'
             UNION ALL SELECT 'Convidar palestrante', 'ALTA', 'EM_ANDAMENTO', '2026-11-05', 'Workshop', NULL
             UNION ALL SELECT 'Reservar auditório', 'MEDIA', 'ABERTA', '2026-11-10', 'Workshop', NULL
             UNION ALL SELECT 'Emitir certificados', 'BAIXA', 'ABERTA', '2026-11-20', 'Workshop', NULL
             UNION ALL SELECT 'Pagar taxa de inscrição', 'ALTA', 'ABERTA', '2026-11-08', NULL, 'Campeonato Regional'
             UNION ALL SELECT 'Prestação de contas do Desafio', 'MEDIA', 'ABERTA', '2026-11-04', 'Desafio das Atléticas', NULL
             UNION ALL SELECT 'Relatório para a reitoria', 'MEDIA', 'ABERTA', '2026-11-30', NULL, NULL
             UNION ALL SELECT 'Atualizar a ata das reuniões', 'BAIXA', 'ABERTA', '2026-11-15', NULL, NULL
             UNION ALL SELECT 'Organizar a sala da atlética', 'BAIXA', 'ABERTA', NULL, NULL, NULL
             UNION ALL SELECT 'Divulgar a ação social', 'MEDIA', 'CONCLUIDA', '2026-10-15', 'Ação Social', NULL) AS v
    LEFT JOIN evento e ON e.titulo = v.evento
    LEFT JOIN competicao c ON c.titulo = v.competicao;

-- Responsáveis (membros com cargo na gestão em andamento). "Definir fotógrafo" fica sem
-- responsável e conta para Esportes, pela competição; "Organizar a sala" não tem setor.
INSERT INTO demanda_responsavel (demanda_id, membro_id)
    SELECT d.id, m.id
    FROM (SELECT 'Inscrever atletas no Regional' AS titulo, '201902' AS matricula
             UNION ALL SELECT 'Conseguir uniformes', '201902'
             UNION ALL SELECT 'Organizar hospedagem', '201902'
             UNION ALL SELECT 'Agendar exames médicos', '202105'
             UNION ALL SELECT 'Montar as equipes', '201902'
             UNION ALL SELECT 'Convidar palestrante', '201933'
             UNION ALL SELECT 'Reservar auditório', '201933'
             UNION ALL SELECT 'Emitir certificados', '202111'
             UNION ALL SELECT 'Pagar taxa de inscrição', '202010'
             UNION ALL SELECT 'Prestação de contas do Desafio', '202010'
             UNION ALL SELECT 'Relatório para a reitoria', '201801'
             UNION ALL SELECT 'Atualizar a ata das reuniões', '202111'
             UNION ALL SELECT 'Divulgar a ação social', '201933') AS v,
         demanda d, membro m
    WHERE d.titulo = v.titulo AND m.matricula = v.matricula;
