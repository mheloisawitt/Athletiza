-- Athletiza - estrutura do banco de dados (MySQL 8)
-- Execute com o banco athletiza selecionado (no Workbench: USE athletiza; ou Default Schema = athletiza).
-- Pode ser executado novamente: apaga e recria todas as tabelas (os dados são perdidos).
--
-- As tabelas são apagadas das dependentes para as principais, para não violar as chaves estrangeiras.
--
-- Regras de exclusão (RN16):
--   RESTRICT -> impede excluir um registro que ainda é usado em outro lugar
--   CASCADE  -> apaga junto as linhas que só existem por causa do registro principal
--               (ex.: presenças de um treino, tarefas de um evento)

DROP TABLE IF EXISTS arquivo;
DROP TABLE IF EXISTS pasta;
DROP TABLE IF EXISTS tarefa;
DROP TABLE IF EXISTS evento_responsavel;
DROP TABLE IF EXISTS evento;
DROP TABLE IF EXISTS compromisso;
DROP TABLE IF EXISTS resultado;
DROP TABLE IF EXISTS competicao_atleta;
DROP TABLE IF EXISTS competicao_modalidade;
DROP TABLE IF EXISTS competicao;
DROP TABLE IF EXISTS presenca_treino;
DROP TABLE IF EXISTS treino;
DROP TABLE IF EXISTS gestao_membro_cargo;
DROP TABLE IF EXISTS cargo;
DROP TABLE IF EXISTS gestao;
DROP TABLE IF EXISTS atleta_modalidade;
DROP TABLE IF EXISTS atleta;
DROP TABLE IF EXISTS modalidade;
DROP TABLE IF EXISTS membro;
DROP TABLE IF EXISTS usuario;

-- ===================== Pessoas =====================

CREATE TABLE usuario (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    matricula   VARCHAR(20),
    contato     VARCHAR(100),
    login       VARCHAR(50)  NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL CHECK (perfil IN ('ADMINISTRADOR', 'DIRETORIA', 'CONSULTA')),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    -- senha provisória (inicial ou definida por um administrador): troca obrigatória no próximo acesso
    deve_trocar_senha BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE membro (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    matricula   VARCHAR(20)  NOT NULL UNIQUE,
    contato     VARCHAR(100),
    situacao    VARCHAR(20)  NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO'))
);

CREATE TABLE modalidade (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(50)  NOT NULL,
    genero      VARCHAR(20)  NOT NULL CHECK (genero IN ('MASCULINO', 'FEMININO', 'MISTO')),
    UNIQUE (nome, genero)
);

CREATE TABLE atleta (
    id              INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    matricula       VARCHAR(20)  NOT NULL UNIQUE,
    contato         VARCHAR(100),
    data_nascimento DATE,
    situacao        VARCHAR(20)  NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO')),
    observacoes     VARCHAR(500)
);

CREATE TABLE atleta_modalidade (
    atleta_id     INTEGER     NOT NULL,
    modalidade_id INTEGER     NOT NULL,
    situacao      VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
                  CHECK (situacao IN ('ATIVO', 'LESIONADO', 'AFASTADO', 'INATIVO')),
    PRIMARY KEY (atleta_id, modalidade_id),
    FOREIGN KEY (atleta_id) REFERENCES atleta (id) ON DELETE CASCADE,
    FOREIGN KEY (modalidade_id) REFERENCES modalidade (id) ON DELETE RESTRICT
);

-- ===================== Gestão =====================

CREATE TABLE gestao (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL UNIQUE,
    data_inicio DATE         NOT NULL,
    data_fim    DATE         NOT NULL,
    descricao   VARCHAR(500),
    situacao    VARCHAR(20)  NOT NULL CHECK (situacao IN ('PLANEJADA', 'EM_ANDAMENTO', 'ENCERRADA')),
    CHECK (data_fim >= data_inicio)
);

CREATE TABLE cargo (
    id                INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(60) NOT NULL UNIQUE,
    ordem             INTEGER     NOT NULL DEFAULT 1,
    cargo_superior_id INTEGER,
    FOREIGN KEY (cargo_superior_id) REFERENCES cargo (id) ON DELETE SET NULL
);

CREATE TABLE gestao_membro_cargo (
    gestao_id INTEGER NOT NULL,
    membro_id INTEGER NOT NULL,
    cargo_id  INTEGER NOT NULL,
    PRIMARY KEY (gestao_id, membro_id, cargo_id),
    FOREIGN KEY (gestao_id) REFERENCES gestao (id) ON DELETE CASCADE,
    FOREIGN KEY (membro_id) REFERENCES membro (id) ON DELETE RESTRICT,
    FOREIGN KEY (cargo_id) REFERENCES cargo (id) ON DELETE RESTRICT
);

-- ===================== Treinos =====================

CREATE TABLE treino (
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    tipo           VARCHAR(20)  NOT NULL CHECK (tipo IN ('TREINO', 'AMISTOSO')),
    titulo         VARCHAR(100) NOT NULL,
    data           DATE         NOT NULL,
    horario        TIME,
    local          VARCHAR(100),
    modalidade_id  INTEGER      NOT NULL,
    responsavel_id INTEGER,
    adversario     VARCHAR(100),
    resultado      VARCHAR(100),
    observacoes    VARCHAR(500),
    FOREIGN KEY (modalidade_id) REFERENCES modalidade (id) ON DELETE RESTRICT,
    FOREIGN KEY (responsavel_id) REFERENCES membro (id) ON DELETE SET NULL
);

CREATE TABLE presenca_treino (
    treino_id INTEGER NOT NULL,
    atleta_id INTEGER NOT NULL,
    presente  BOOLEAN NOT NULL,
    PRIMARY KEY (treino_id, atleta_id),
    FOREIGN KEY (treino_id) REFERENCES treino (id) ON DELETE CASCADE,
    FOREIGN KEY (atleta_id) REFERENCES atleta (id) ON DELETE RESTRICT
);

-- ===================== Competições =====================

CREATE TABLE competicao (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    titulo      VARCHAR(100) NOT NULL,
    data        DATE         NOT NULL,
    data_fim    DATE,
    horario     TIME,
    local       VARCHAR(100),
    situacao    VARCHAR(20)  NOT NULL CHECK (situacao IN ('PLANEJADA', 'CONFIRMADA', 'ENCERRADA', 'CANCELADA')),
    observacoes VARCHAR(500),
    CHECK (data_fim IS NULL OR data_fim >= data)
);

CREATE TABLE competicao_modalidade (
    competicao_id INTEGER NOT NULL,
    modalidade_id INTEGER NOT NULL,
    PRIMARY KEY (competicao_id, modalidade_id),
    FOREIGN KEY (competicao_id) REFERENCES competicao (id) ON DELETE CASCADE,
    FOREIGN KEY (modalidade_id) REFERENCES modalidade (id) ON DELETE RESTRICT
);

CREATE TABLE competicao_atleta (
    competicao_id INTEGER NOT NULL,
    modalidade_id INTEGER NOT NULL,
    atleta_id     INTEGER NOT NULL,
    PRIMARY KEY (competicao_id, modalidade_id, atleta_id),
    FOREIGN KEY (competicao_id, modalidade_id)
        REFERENCES competicao_modalidade (competicao_id, modalidade_id) ON DELETE CASCADE,
    FOREIGN KEY (atleta_id) REFERENCES atleta (id) ON DELETE RESTRICT
);

CREATE TABLE resultado (
    id            INTEGER AUTO_INCREMENT PRIMARY KEY,
    competicao_id INTEGER NOT NULL,
    modalidade_id INTEGER NOT NULL,
    atleta_id     INTEGER,
    colocacao     INTEGER CHECK (colocacao IS NULL OR colocacao > 0),
    placar        VARCHAR(50),
    observacao    VARCHAR(500),
    FOREIGN KEY (competicao_id, modalidade_id)
        REFERENCES competicao_modalidade (competicao_id, modalidade_id) ON DELETE CASCADE,
    FOREIGN KEY (atleta_id) REFERENCES atleta (id) ON DELETE RESTRICT
);

-- ===================== Eventos e calendário =====================

CREATE TABLE evento (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    titulo      VARCHAR(100) NOT NULL,
    tipo_evento VARCHAR(20)  NOT NULL
                CHECK (tipo_evento IN ('FESTA', 'ACAO_SOCIAL', 'ACAO_AMBIENTAL', 'COMPETICAO', 'OUTRO')),
    data        DATE         NOT NULL,
    horario     TIME,
    local       VARCHAR(100),
    descricao   VARCHAR(500),
    situacao    VARCHAR(20)  NOT NULL CHECK (situacao IN ('PLANEJADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO')),
    observacoes VARCHAR(500)
);

CREATE TABLE evento_responsavel (
    evento_id INTEGER NOT NULL,
    membro_id INTEGER NOT NULL,
    PRIMARY KEY (evento_id, membro_id),
    FOREIGN KEY (evento_id) REFERENCES evento (id) ON DELETE CASCADE,
    FOREIGN KEY (membro_id) REFERENCES membro (id) ON DELETE RESTRICT
);

CREATE TABLE tarefa (
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    evento_id      INTEGER,
    competicao_id  INTEGER,
    descricao      VARCHAR(200) NOT NULL,
    responsavel_id INTEGER,
    prazo          DATE,
    situacao       VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE'
                   CHECK (situacao IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),
    FOREIGN KEY (evento_id) REFERENCES evento (id) ON DELETE CASCADE,
    FOREIGN KEY (competicao_id) REFERENCES competicao (id) ON DELETE CASCADE,
    FOREIGN KEY (responsavel_id) REFERENCES membro (id) ON DELETE RESTRICT
);

CREATE TABLE compromisso (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    titulo      VARCHAR(100) NOT NULL,
    data        DATE         NOT NULL,
    horario     TIME,
    local       VARCHAR(100),
    observacoes VARCHAR(500)
);

-- ===================== Registros (galeria de fotos e vídeos) =====================
-- O arquivo em si fica no disco (pasta de mídias); aqui ficam apenas as informações dele.

CREATE TABLE pasta (
    id        INTEGER AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(60) NOT NULL UNIQUE,
    criada_em DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE arquivo (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    pasta_id    INTEGER      NOT NULL,
    nome        VARCHAR(150) NOT NULL,
    caminho     VARCHAR(300) NOT NULL UNIQUE,
    tipo        VARCHAR(10)  NOT NULL CHECK (tipo IN ('IMAGEM', 'VIDEO')),
    tamanho     BIGINT       NOT NULL CHECK (tamanho >= 0),
    enviado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_por VARCHAR(50),
    descricao   VARCHAR(500),
    FOREIGN KEY (pasta_id) REFERENCES pasta (id) ON DELETE RESTRICT
);

CREATE INDEX idx_treino_data ON treino (data);
CREATE INDEX idx_competicao_data ON competicao (data);
CREATE INDEX idx_evento_data ON evento (data);
CREATE INDEX idx_compromisso_data ON compromisso (data);
