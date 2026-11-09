-- Athletiza - dados de exemplo extras para o Dashboard (opcional) - MySQL 8
-- Execute UMA VEZ, depois do 02_dados_exemplo.sql, com o banco athletiza selecionado.
-- Acrescenta atletas, treinos e amistosos de outras modalidades, listas de presença
-- e tarefas com responsáveis de vários setores, para os gráficos do dashboard terem o que mostrar.
-- Para voltar ao início: execute de novo o 01_estrutura.sql e o 02_dados_exemplo.sql.

-- Lê este arquivo como UTF-8 (acentos corretos também pelo terminal; o Workbench já usa UTF-8).
/*!40101 SET NAMES utf8mb4 */;

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
             UNION ALL SELECT 'TREINO', 'Treino Atletismo (M)', '2026-10-17', '07:00', 'Pista', 'Atletismo', 'MASCULINO', NULL)
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

-- Mais membros na gestão atual
INSERT INTO membro (nome, matricula, contato) VALUES
    ('Tiago Moreira', '202111', 'tiago.moreira@email.com');

INSERT INTO gestao_membro_cargo (gestao_id, membro_id, cargo_id)
    SELECT g.id, m.id, c.id
    FROM gestao g, membro m, cargo c
    WHERE g.nome = 'Gestão 2025-2026' AND m.matricula = '202111' AND c.nome = 'Secretário';

-- Tarefas (demandas) de eventos e competições, com responsáveis de vários setores
INSERT INTO tarefa (evento_id, descricao, responsavel_id, prazo, situacao)
    SELECT e.id, v.descricao, m.id, CAST(v.prazo AS DATE), v.situacao
    FROM (SELECT 'Ação Social' AS evento, 'Arrecadar alimentos' AS descricao, '201933' AS matricula,
                 '2026-10-18' AS prazo, 'CONCLUIDA' AS situacao
             UNION ALL SELECT 'Ação Social', 'Divulgar nas redes sociais', '202111', '2026-10-15', 'CONCLUIDA'
             UNION ALL SELECT 'Workshop', 'Convidar palestrante', '201933', '2026-11-05', 'EM_ANDAMENTO'
             UNION ALL SELECT 'Workshop', 'Reservar auditório', '202111', '2026-11-10', 'PENDENTE'
             UNION ALL SELECT 'Workshop', 'Comprar coffee break', '202010', '2026-11-12', 'PENDENTE'
             UNION ALL SELECT 'Workshop', 'Emitir certificados', '202111', '2026-11-20', 'PENDENTE'
             UNION ALL SELECT 'Desafio das Atléticas', 'Prestação de contas', '202010', '2026-11-04', 'PENDENTE'
             UNION ALL SELECT 'Desafio das Atléticas', 'Relatório para a reitoria', '201801', '2026-11-30', 'PENDENTE') AS v,
         evento e, membro m
    WHERE e.titulo = v.evento AND m.matricula = v.matricula;

INSERT INTO tarefa (competicao_id, descricao, responsavel_id, prazo, situacao)
    SELECT c.id, v.descricao, m.id, CAST(v.prazo AS DATE), v.situacao
    FROM (SELECT 'JIUDESC 2026' AS competicao, 'Inscrever atletas' AS descricao, '201902' AS matricula,
                 '2026-10-15' AS prazo, 'CONCLUIDA' AS situacao
             UNION ALL SELECT 'JIUDESC 2026', 'Reservar ônibus', '201902', '2026-10-25', 'CONCLUIDA'
             UNION ALL SELECT 'Campeonato Regional', 'Inscrever atletas', '201902', '2026-11-01', 'EM_ANDAMENTO'
             UNION ALL SELECT 'Campeonato Regional', 'Conseguir uniformes', '201902', '2026-11-06', 'PENDENTE'
             UNION ALL SELECT 'Campeonato Regional', 'Pagar taxa de inscrição', '202010', '2026-11-08', 'PENDENTE'
             UNION ALL SELECT 'Campeonato Regional', 'Organizar hospedagem', '201902', '2026-11-12', 'PENDENTE'
             UNION ALL SELECT 'Copa Alto Vale', 'Montar as equipes', '201902', '2026-11-25', 'PENDENTE'
             UNION ALL SELECT 'Copa Alto Vale', 'Agendar exames médicos', '201902', '2026-11-18', 'PENDENTE'
             UNION ALL SELECT 'Copa Alto Vale', 'Pedir apoio da prefeitura', '201845', '2026-11-28', 'PENDENTE') AS v,
         competicao c, membro m
    WHERE c.titulo = v.competicao AND m.matricula = v.matricula;

-- Uma demanda ainda sem responsável
INSERT INTO tarefa (competicao_id, descricao, prazo, situacao)
    SELECT id, 'Definir fotógrafo', CAST('2026-12-01' AS DATE), 'PENDENTE' FROM competicao WHERE titulo = 'Copa Alto Vale';
