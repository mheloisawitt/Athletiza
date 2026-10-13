-- Athletiza - dados de exemplo (baseados no protótipo das telas) - MySQL 8
-- Execute depois do 01_estrutura.sql. O USE abaixo seleciona o banco athletiza.
-- Os relacionamentos usam subconsultas (em vez de ids fixos) para funcionar em qualquer banco recém-criado.

USE athletiza;

-- Usuário administrador inicial: login "admin", senha "admin123".
-- A senha é provisória: o sistema exige a troca no primeiro acesso.
-- O hash foi gerado com: java -cp target/classes br.com.athletiza.util.Senha admin123
INSERT INTO usuario (nome, login, senha_hash, administrador, deve_trocar_senha) VALUES
    ('Administrador', 'admin', 'pbkdf2$120000$Ldd7cZo0DiIlLjcaDg7IQA==$5P2ymAUQH3VcPGeVFtv9FO2dlfMmNCUjABieDm9OjSY=', TRUE, TRUE);

-- Modalidades
INSERT INTO modalidade (nome, genero) VALUES
    ('Futsal', 'MASCULINO'), ('Futsal', 'FEMININO'),
    ('Vôlei', 'MASCULINO'), ('Vôlei', 'FEMININO'),
    ('Atletismo', 'MASCULINO'), ('Atletismo', 'FEMININO'),
    ('Handebol', 'MASCULINO'), ('Handebol', 'FEMININO'),
    ('Basquete', 'MASCULINO'), ('Basquete', 'FEMININO');

-- Gestões
INSERT INTO gestao (nome, data_inicio, data_fim, situacao) VALUES
    ('Gestão 2020-2021', '2020-03-01', '2021-02-28', 'ENCERRADA'),
    ('Gestão 2022-2023', '2022-03-01', '2023-02-28', 'ENCERRADA'),
    ('Gestão 2023-2024', '2023-03-01', '2024-02-29', 'ENCERRADA'),
    ('Gestão 2025-2026', '2025-03-01', '2026-12-31', 'EM_ANDAMENTO');

-- Cargos (ordem = nível no organograma; setor = acesso ao sistema de quem ocupa o cargo)
INSERT INTO cargo (nome, ordem, setor) VALUES ('Presidente', 1, 'PRESIDENCIA');
INSERT INTO cargo (nome, ordem, setor, cargo_superior_id)
    SELECT 'Vice-presidente', 2, 'PRESIDENCIA', id FROM cargo WHERE nome = 'Presidente';
INSERT INTO cargo (nome, ordem, setor, cargo_superior_id)
    SELECT c.nome, 3, c.setor, p.id
    FROM (SELECT 'Diretor de Esportes' AS nome, 'ESPORTES' AS setor
             UNION ALL SELECT 'Diretor de Eventos', 'EVENTOS'
             UNION ALL SELECT 'Tesoureiro', 'FINANCEIRO'
             UNION ALL SELECT 'Secretário', 'SECRETARIA') AS c,
         cargo p
    WHERE p.nome = 'Vice-presidente';
-- Subordinado ao Diretor de Esportes e do mesmo setor: é assessor de Esportes
INSERT INTO cargo (nome, ordem, setor, cargo_superior_id)
    SELECT 'Coordenador de Modalidade', 4, 'ESPORTES', id FROM cargo WHERE nome = 'Diretor de Esportes';

-- Membros
INSERT INTO membro (nome, matricula, contato) VALUES
    ('Mariana Rocha', '201801', 'mariana.rocha@email.com'),
    ('Lucas Pereira', '201845', '(47) 99999-1001'),
    ('Juliana Martins', '201902', 'juliana.martins@email.com'),
    ('Rafael Teixeira', '201933', '(47) 99999-1002'),
    ('Paula Ribeiro', '202010', 'paula.ribeiro@email.com'),
    ('Bruno Costa', '202105', '(47) 99999-1003');

INSERT INTO gestao_membro_cargo (gestao_id, membro_id, cargo_id)
    SELECT g.id, m.id, c.id
    FROM (SELECT '201801' AS matricula, 'Presidente' AS cargo
             UNION ALL SELECT '201845', 'Vice-presidente'
             UNION ALL SELECT '201902', 'Diretor de Esportes'
             UNION ALL SELECT '201933', 'Diretor de Eventos'
             UNION ALL SELECT '202010', 'Tesoureiro'
             UNION ALL SELECT '202105', 'Coordenador de Modalidade') AS v,
         gestao g, membro m, cargo c
    WHERE g.nome = 'Gestão 2025-2026' AND m.matricula = v.matricula AND c.nome = v.cargo;

-- Usuários de exemplo, um de cada perfil (senha "athletiza"). O perfil vem do cargo na gestão em andamento.
INSERT INTO usuario (nome, matricula, login, senha_hash, membro_id)
    SELECT m.nome, m.matricula, v.login, 'pbkdf2$120000$BU/kNNz8vIHtNWaxrAtuRQ==$KaIvcqN1E4L+se+OTjQ4JCSgnVAAJEBKC2tfNXvLL5U=', m.id
    FROM (SELECT '201801' AS matricula, 'mariana' AS login
             UNION ALL SELECT '201902', 'juliana'
             UNION ALL SELECT '201933', 'rafael'
             UNION ALL SELECT '202105', 'bruno') AS v,
         membro m
    WHERE m.matricula = v.matricula;

-- Atletas
INSERT INTO atleta (nome, matricula, contato, data_nascimento) VALUES
    ('Ana Souza', '202001', 'ana.souza@email.com', '2003-05-12'),
    ('Beatriz Lima', '202034', '(47) 98888-2001', '2002-11-03'),
    ('Carlos Mendes', '202067', 'carlos.mendes@email.com', '2001-08-25'),
    ('Daniela Costa', '202102', '(47) 98888-2002', '2004-01-17'),
    ('Eduardo Silva', '202145', 'eduardo.silva@email.com', '2003-09-30'),
    ('Fernanda Alves', '202178', '(47) 98888-2003', '2004-04-08'),
    ('Carla Oliveira', '202190', 'carla.oliveira@email.com', '2005-02-21');

INSERT INTO atleta_modalidade (atleta_id, modalidade_id, situacao)
    SELECT a.id, m.id, 'ATIVO'
    FROM (SELECT '202001' AS matricula, 'Futsal' AS modalidade, 'FEMININO' AS genero
             UNION ALL SELECT '202034', 'Vôlei', 'FEMININO'
             UNION ALL SELECT '202067', 'Atletismo', 'MASCULINO'
             UNION ALL SELECT '202102', 'Futsal', 'FEMININO'
             UNION ALL SELECT '202145', 'Handebol', 'MASCULINO'
             UNION ALL SELECT '202178', 'Vôlei', 'FEMININO'
             UNION ALL SELECT '202190', 'Futsal', 'FEMININO'
             UNION ALL SELECT '202034', 'Futsal', 'FEMININO') AS v,
         atleta a, modalidade m
    WHERE a.matricula = v.matricula AND m.nome = v.modalidade AND m.genero = v.genero;

-- Treinos e amistosos (Futsal Feminino, outubro/2026)
INSERT INTO treino (tipo, titulo, data, horario, local, modalidade_id, responsavel_id, adversario)
    SELECT v.tipo, v.titulo, CAST(v.data AS DATE), CAST(v.horario AS TIME), v.local, m.id, r.id, v.adversario
    FROM (SELECT 'TREINO' AS tipo, 'Treino Futsal (F)' AS titulo, '2026-10-01' AS data, '19:00' AS horario, 'Quadra 1' AS local, NULL AS adversario
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-10-03', '19:00', 'Quadra 1', NULL
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-10-06', '19:00', 'Quadra 1', NULL
             UNION ALL SELECT 'AMISTOSO', 'Amistoso Futsal (F) x Atlética Furiosa', '2026-10-08', '20:00', 'Quadra 1', 'Atlética Furiosa'
             UNION ALL SELECT 'TREINO', 'Treino Futsal (F)', '2026-10-10', '19:00', 'Quadra 1', NULL) AS v,
         modalidade m, membro r
    WHERE m.nome = 'Futsal' AND m.genero = 'FEMININO' AND r.matricula = '201902';

INSERT INTO presenca_treino (treino_id, atleta_id, presente)
    SELECT t.id, a.id, a.matricula <> '202190'
    FROM treino t, atleta a
    WHERE t.data = '2026-10-01' AND a.matricula IN ('202001', '202102', '202190', '202034');

-- Competições
INSERT INTO competicao (titulo, data, data_fim, local, situacao) VALUES
    ('JIUDESC 2026', '2026-10-30', '2026-11-02', 'Blumenau', 'CONFIRMADA'),
    ('Campeonato Regional', '2026-11-15', NULL, 'Rio do Sul', 'CONFIRMADA'),
    ('Copa Alto Vale', '2026-12-05', NULL, 'Ibirama', 'PLANEJADA'),
    ('Torneio Interno', '2026-12-20', NULL, 'Ibirama', 'PLANEJADA');

INSERT INTO competicao_modalidade (competicao_id, modalidade_id)
    SELECT c.id, m.id
    FROM competicao c, modalidade m
    WHERE c.titulo = 'JIUDESC 2026'
      AND ((m.nome IN ('Futsal', 'Vôlei', 'Atletismo')) OR (m.nome = 'Handebol' AND m.genero = 'MASCULINO'));

INSERT INTO competicao_atleta (competicao_id, modalidade_id, atleta_id)
    SELECT cm.competicao_id, cm.modalidade_id, am.atleta_id
    FROM competicao_modalidade cm
    JOIN atleta_modalidade am ON am.modalidade_id = cm.modalidade_id;

-- Eventos
INSERT INTO evento (titulo, tipo_evento, data, horario, local, situacao) VALUES
    ('Festa Atlética', 'FESTA', '2026-10-12', '20:00', 'Ibirama', 'CONFIRMADO'),
    ('Ação Social', 'ACAO_SOCIAL', '2026-10-21', '19:00', 'Ibirama', 'CONFIRMADO'),
    ('Desafio das Atléticas', 'COMPETICAO', '2026-10-30', NULL, 'Blumenau', 'CONFIRMADO'),
    ('Workshop', 'OUTRO', '2026-11-15', '14:00', 'Ibirama', 'PLANEJADO');

INSERT INTO evento_responsavel (evento_id, membro_id)
    SELECT e.id, m.id FROM evento e, membro m
    WHERE e.titulo = 'Festa Atlética' AND m.matricula IN ('201933', '202010');

INSERT INTO tarefa (evento_id, descricao, responsavel_id, prazo, situacao)
    SELECT e.id, v.descricao, m.id, CAST(v.prazo AS DATE), v.situacao
    FROM (SELECT 'Reservar o espaço' AS descricao, '201933' AS matricula, '2026-09-30' AS prazo, 'CONCLUIDA' AS situacao
             UNION ALL SELECT 'Contratar DJ', '201933', '2026-10-05', 'EM_ANDAMENTO'
             UNION ALL SELECT 'Vender ingressos', '202010', '2026-10-11', 'PENDENTE') AS v,
         evento e, membro m
    WHERE e.titulo = 'Festa Atlética' AND m.matricula = v.matricula;

-- Demandas (a primeira sem relação, as outras ligadas a um evento ou a uma competição)
INSERT INTO demanda (titulo, descricao, prioridade, situacao, prazo) VALUES
    ('Renovar o estoque de camisetas', 'Orçar com dois fornecedores antes de comprar.', 'MEDIA', 'ABERTA', '2026-11-10');

INSERT INTO demanda (titulo, prioridade, situacao, prazo, evento_id)
    SELECT 'Arrecadar alimentos', 'ALTA', 'EM_ANDAMENTO', '2026-10-20', id FROM evento WHERE titulo = 'Ação Social';

INSERT INTO demanda (titulo, prioridade, situacao, prazo, competicao_id)
    SELECT 'Reservar ônibus para os atletas', 'ALTA', 'ABERTA', '2026-10-25', id FROM competicao WHERE titulo = 'JIUDESC 2026';

-- Responsáveis: somente membros com cargo na gestão em andamento (Gestão 2025-2026)
INSERT INTO demanda_responsavel (demanda_id, membro_id)
    SELECT d.id, m.id
    FROM (SELECT 'Renovar o estoque de camisetas' AS titulo, '202010' AS matricula
             UNION ALL SELECT 'Arrecadar alimentos', '201933'
             UNION ALL SELECT 'Arrecadar alimentos', '201801'
             UNION ALL SELECT 'Reservar ônibus para os atletas', '201902') AS v,
         demanda d, membro m
    WHERE d.titulo = v.titulo AND m.matricula = v.matricula
      AND EXISTS (SELECT 1 FROM gestao_membro_cargo gmc JOIN gestao g ON g.id = gmc.gestao_id
                  WHERE gmc.membro_id = m.id AND g.situacao = 'EM_ANDAMENTO');

-- Compromissos
INSERT INTO compromisso (titulo, data, horario, local) VALUES
    ('Reunião Gestão', '2026-10-08', '14:00', 'Sala da atlética');

-- Pastas da galeria de registros (os arquivos são adicionados pelo sistema)
INSERT INTO pasta (nome) VALUES ('Festas'), ('Ações Sociais'), ('Competições'), ('Treinos'), ('Outros');
