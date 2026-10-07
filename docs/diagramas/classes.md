# Diagrama de classes (CH-09)

Dividido em três partes para ficar legível. O GitHub desenha os diagramas automaticamente;
para exportar como imagem, cole o código em <https://mermaid.live> e use **Actions > PNG**.

## 1. Modelo (`br.com.athletiza.model`)

Herança, classes abstratas e as coleções (List, Map, Set) usadas nas associações.

```mermaid
classDiagram
    direction TB

    class Entidade {
        <<abstract>>
        -Integer id
        +isNova() boolean
    }

    class Pessoa {
        <<abstract>>
        -String nome
        -String matricula
        -String contato
        +equals(Object) boolean
        +hashCode() int
    }
    class Membro {
        -Situacao situacao
    }
    class Atleta {
        -LocalDate dataNascimento
        -Situacao situacao
        -Map~Modalidade, SituacaoAtleta~ modalidades
        +POR_NOME$ Comparator
        +POR_MATRICULA$ Comparator
        +POR_SITUACAO$ Comparator
        +POR_MODALIDADE$ Comparator
        +vincularModalidade(Modalidade, SituacaoAtleta)
        +getModalidades() Set~Modalidade~
        +compareTo(Atleta) int
    }
    class Usuario {
        -String login
        -String senhaHash
        -Perfil perfil
        -boolean ativo
    }

    class Atividade {
        <<abstract>>
        -String titulo
        -LocalDate data
        -LocalTime horario
        -String local
        +getTipo()* TipoAtividade
        +getRotuloCurto() String
        +getDescricaoCalendario() String
        +ocorreEm(LocalDate) boolean
        +compareTo(Atividade) int
    }
    class Treino {
        -Modalidade modalidade
        -Membro responsavel
        -Map~Atleta, Boolean~ presencas
        +registrarPresenca(Atleta, boolean)
        +getPresentes() List~Atleta~
    }
    class Amistoso {
        -String adversario
        -String resultado
    }
    class Competicao {
        -LocalDate dataFim
        -SituacaoCompeticao situacao
        -Map~Modalidade, Set&lt;Atleta&gt;~ inscricoes
        -List~Resultado~ resultados
        +inscreverAtleta(Modalidade, Atleta) boolean
        +ocorreEm(LocalDate) boolean
    }
    class Evento {
        -TipoEvento tipoEvento
        -SituacaoEvento situacao
        -Set~Membro~ responsaveis
        -List~Tarefa~ tarefas
        +getTarefasAtrasadas(LocalDate) List~Tarefa~
    }
    class Compromisso

    class Gestao {
        -String nome
        -LocalDate dataInicio
        -LocalDate dataFim
        -SituacaoGestao situacao
        -Set~MembroCargo~ composicao
        +sobrepoe(Gestao) boolean
        +adicionarMembro(Membro, Cargo) boolean
        +getOrganograma() Map~Cargo, List&lt;Membro&gt;~
    }
    class MembroCargo {
        <<record>>
        Membro membro
        Cargo cargo
    }
    class Cargo {
        -String nome
        -int ordem
        -Cargo cargoSuperior
    }
    class Modalidade {
        -String nome
        -Genero genero
    }
    class Resultado {
        -Modalidade modalidade
        -Atleta atleta
        -Integer colocacao
        -String placar
    }
    class Tarefa {
        -String descricao
        -Membro responsavel
        -LocalDate prazo
        -SituacaoTarefa situacao
        +isAtrasada(LocalDate) boolean
    }
    class Frequencia {
        <<record>>
        Atleta atleta
        int treinos
        int presencas
        +getPercentual() int
    }

    Entidade <|-- Pessoa
    Pessoa <|-- Membro
    Pessoa <|-- Atleta
    Pessoa <|-- Usuario
    Entidade <|-- Atividade
    Atividade <|-- Treino
    Treino <|-- Amistoso
    Atividade <|-- Competicao
    Atividade <|-- Evento
    Atividade <|-- Compromisso
    Entidade <|-- Gestao
    Entidade <|-- Cargo
    Entidade <|-- Modalidade
    Entidade <|-- Resultado
    Entidade <|-- Tarefa

    Atleta "*" --> "*" Modalidade : pratica
    Treino --> Modalidade
    Treino --> Membro : responsável
    Competicao *-- Resultado
    Competicao --> Atleta : inscritos
    Evento *-- Tarefa
    Evento --> Membro : responsáveis
    Gestao *-- MembroCargo
    MembroCargo --> Membro
    MembroCargo --> Cargo
    Cargo --> Cargo : superior
    Frequencia --> Atleta
```

Enums do modelo: `Situacao`, `SituacaoAtleta`, `Genero`, `Perfil`, `TipoAtividade`, `TipoEvento`,
`SituacaoGestao`, `SituacaoCompeticao`, `SituacaoEvento`, `SituacaoTarefa`, `AcaoRegistro`.

## 2. Persistência (`br.com.athletiza.dao`) e exceções

```mermaid
classDiagram
    direction TB

    class GenericDAO~T~ {
        <<interface>>
        +inserir(T)
        +atualizar(T)
        +excluir(int)
        +buscarPorId(int) Optional~T~
        +listarTodos() List~T~
    }
    class MapeadorLinha~T~ {
        <<interface>>
        +mapear(ResultSet) T
    }
    class DAOBase {
        <<abstract>>
        #getNomeEntidade()* String
        #executarInsercao(String, Object...) int
        #executarAtualizacao(String, Object...) int
        #consultar(String, MapeadorLinha, Object...) List
        #emTransacao(String, Transacao) R
        #traduzir(SQLException, String) PersistenciaException
    }
    class AbstractDAO~T~ {
        <<abstract>>
        #getTabela()* String
        #mapear(ResultSet)* T
        +excluir(int)
        +buscarPorId(int)
        +listarTodos()
    }
    class ConnectionFactory {
        +getConnection()$ Connection
        +testarConexao()$
    }

    GenericDAO <|.. AbstractDAO
    DAOBase <|-- AbstractDAO
    DAOBase ..> MapeadorLinha
    DAOBase ..> ConnectionFactory
    AbstractDAO <|-- AtletaDAO
    AbstractDAO <|-- ModalidadeDAO
    AbstractDAO <|-- GestaoDAO
    AbstractDAO <|-- CargoDAO
    AbstractDAO <|-- MembroDAO
    AbstractDAO <|-- CompeticaoDAO
    AbstractDAO <|-- TreinoDAO
    AbstractDAO <|-- EventoDAO
    AbstractDAO <|-- CompromissoDAO
    AbstractDAO <|-- UsuarioDAO
    DAOBase <|-- AtividadeDAO

    class AthletizaException {
        <<abstract>>
    }
    Exception <|-- AthletizaException
    AthletizaException <|-- ValidacaoException
    AthletizaException <|-- RegraNegocioException
    AthletizaException <|-- PersistenciaException
    DAOBase ..> PersistenciaException : lança
```

Cada controller (`AtletaController`, `GestaoController`, `CompeticaoController`, `TreinoController`,
`EventoController`, `CalendarioController`, `LoginController`, `UsuarioController`, ...) usa um ou mais DAOs
e o `Validador`, e lança `ValidacaoException` ou `RegraNegocioException`.

## 3. Telas (`br.com.athletiza.view`)

```mermaid
classDiagram
    direction TB

    class Navegador {
        <<interface>>
        +abrir(JComponent)
        +voltar()
    }
    class Recarregavel {
        <<interface>>
        +carregar()
    }
    class PainelConsulta~T~ {
        <<abstract>>
        #ModeloTabela~T~ modelo
        #JTable tabela
        #buscarDados()* List~T~
        #incluir()
        #editar(T)
        #excluir(T)
        #gerenciar(T)
        +carregar()
    }
    class PainelFormulario {
        <<abstract>>
        #salvar()* 
        #adicionarCampo(String, JComponent, boolean)
    }
    class ModeloTabela~T~ {
        +coluna(String, Class, Function) ModeloTabela
        +setLinhas(List~T~)
    }
    class TelaPrincipal {
        -CardLayout cartoes
        -Deque~JComponent~ telasAbertas
    }

    JPanel <|-- PainelConsulta
    JPanel <|-- PainelFormulario
    Recarregavel <|.. PainelConsulta
    Navegador <|.. TelaPrincipal
    JFrame <|-- TelaPrincipal
    JFrame <|-- TelaLogin
    PainelConsulta --> ModeloTabela
    AbstractTableModel <|-- ModeloTabela

    PainelConsulta <|-- PainelConsultaAtletas
    PainelConsulta <|-- PainelConsultaGestoes
    PainelConsulta <|-- PainelConsultaCompeticoes
    PainelConsulta <|-- PainelTreinos
    PainelConsulta <|-- PainelConsultaEventos
    PainelConsulta <|-- PainelConsultaUsuarios
    PainelFormulario <|-- PainelCadastroAtleta
    PainelFormulario <|-- PainelCadastroGestao
    PainelFormulario <|-- PainelCadastroCompeticao
    PainelFormulario <|-- PainelCadastroTreino
    PainelFormulario <|-- PainelPresenca
    PainelFormulario <|-- PainelCadastroEvento
    Recarregavel <|.. PainelCalendario
    Recarregavel <|.. PainelGerenciarCompeticao
    Recarregavel <|.. PainelOrganograma
```
