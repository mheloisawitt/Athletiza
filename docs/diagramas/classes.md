# Diagrama de classes (CH-09)

Dividido em quatro partes para ficar legível: modelo, persistência (DAO), controllers e telas.
O GitHub desenha os diagramas automaticamente; as mesmas imagens estão em [`imagens`](imagens).
Para exportar de novo, cole o código em <https://mermaid.live> e use **Actions > PNG**.

Como os pacotes se relacionam (regras do MVC vistas em aula):

- **model** não conhece nenhum outro pacote (só `exception`): entidades e regras de negócio (`validar()`).
- **view** conhece o **model** (e utilitários como `Cores`), nunca controller ou DAO: monta a tela, devolve o que foi digitado, mostra mensagens.
- **controller** conhece **view**, **model** e as **interfaces** do pacote `dao` (nunca as classes DAO).
- **dao** conhece o **model**: cada `XDAO` implementa a interface `XRepository`.
- Só o `PrincipalController` (e o `main`) cria os DAOs: é ali que se troca o banco, se precisar (DIP).

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
        +validar()
    }
    class Atleta {
        -LocalDate dataNascimento
        -Situacao situacao
        -Map~Modalidade, SituacaoAtleta~ modalidades
        +POR_NOME$ Comparator
        +POR_MATRICULA$ Comparator
        +POR_SITUACAO$ Comparator
        +POR_MODALIDADE$ Comparator
        +validar()
        +vincularModalidade(Modalidade, SituacaoAtleta)
        +getModalidades() Set~Modalidade~
        +compareTo(Atleta) int
    }
    class Usuario {
        -String login
        -String senhaHash
        -Perfil perfil
        -boolean ativo
        +validar(char[], char[])
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
        +validar()
        #validarCampos(Validador)
        +getTituloPadrao() String
        +registrarPresenca(Atleta, boolean)
        +getPresentes() List~Atleta~
    }
    class Amistoso {
        -String adversario
        -String resultado
        #validarCampos(Validador)
        +getTituloPadrao() String
    }
    class Competicao {
        -LocalDate dataFim
        -SituacaoCompeticao situacao
        -Map~Modalidade, Set&lt;Atleta&gt;~ inscricoes
        -List~Resultado~ resultados
        +validar()
        +inscreverAtleta(Modalidade, Atleta) boolean
        +verificarResultado(Resultado)
        +ocorreEm(LocalDate) boolean
    }
    class Evento {
        -TipoEvento tipoEvento
        -SituacaoEvento situacao
        -Set~Membro~ responsaveis
        -List~Tarefa~ tarefas
        +validar()
        +getTarefasAtrasadas(LocalDate) List~Tarefa~
    }
    class Compromisso {
        +validar()
    }

    class Gestao {
        -String nome
        -LocalDate dataInicio
        -LocalDate dataFim
        -SituacaoGestao situacao
        -Set~MembroCargo~ composicao
        +validar()
        +sobrepoe(Gestao) boolean
        +adicionarMembro(Membro, Cargo) boolean
        +getOrganograma() Map~Cargo, List&lt;Membro&gt;~
    }
    class MembroCargo {
        -Membro membro
        -Cargo cargo
        +equals(Object) boolean
        +hashCode() int
    }
    class Cargo {
        -String nome
        -int ordem
        -Cargo cargoSuperior
        +validar()
        +verificarHierarquia()
    }
    class Modalidade {
        -String nome
        -Genero genero
        +validar()
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
    class Pasta {
        -String nome
        -int quantidadeArquivos
        +compareTo(Pasta) int
    }
    class Arquivo {
        -Pasta pasta
        -String nome
        -String caminho
        -TipoArquivo tipo
        -long tamanho
        -LocalDateTime enviadoEm
        +isImagem() boolean
        +compareTo(Arquivo) int
    }
    class Frequencia {
        -Atleta atleta
        -int treinos
        -int presencas
        +getPercentual() int
        +compareTo(Frequencia) int
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
    Entidade <|-- Pasta
    Entidade <|-- Arquivo

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
    Pasta "1" o-- "*" Arquivo : contém
```

Enums do modelo: `Situacao`, `SituacaoAtleta`, `Genero`, `Perfil`, `TipoAtividade`, `TipoEvento`,
`SituacaoGestao`, `SituacaoCompeticao`, `SituacaoEvento`, `SituacaoTarefa`, `TipoArquivo`.

As regras de preenchimento ficam no próprio modelo (como o `isValido()` do exemplo de MVC da aula):
cada entidade tem `validar()`, que usa o `Validador` (também no pacote `model`) e lança
`ValidacaoException` com todos os erros. Em `Treino`/`Amistoso` a validação é polimórfica:
`Amistoso` sobrescreve `validarCampos` para conferir também o adversário.

## 2. Persistência (`br.com.athletiza.dao`) e exceções

Padrão DAO com **Inversão de Dependência (DIP)**: os controllers dependem das interfaces
`XRepository` (abstração); as classes `XDAO` (detalhe, com JDBC e MySQL) implementam essas interfaces.
É o mesmo esquema do exemplo `AlunoRepository` / `AlunoDAO` da aula.

```mermaid
classDiagram
    direction TB

    class GenericDAO~T~ {
        <<interface>>
        +inserir(T)
        +atualizar(T)
        +excluir(int)
        +buscarPorId(int) T
        +listarTodos() List~T~
    }
    class ModalidadeRepository {
        <<interface>>
        +possuiVinculos(int) boolean
    }
    class AtletaRepository {
        <<interface>>
        +existeMatricula(String, Integer) boolean
        +listarVinculos(int) List~String~
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
        +buscarPorId(int) T
        +listarTodos() List~T~
    }
    class ConnectionFactory {
        +getConnection()$ Connection
        +testarConexao()$
    }
    class ModalidadeDAO
    class AtletaDAO

    GenericDAO <|-- ModalidadeRepository
    GenericDAO <|-- AtletaRepository
    GenericDAO <|.. AbstractDAO
    DAOBase <|-- AbstractDAO
    DAOBase ..> MapeadorLinha
    DAOBase ..> ConnectionFactory
    AbstractDAO <|-- ModalidadeDAO
    AbstractDAO <|-- AtletaDAO
    ModalidadeRepository <|.. ModalidadeDAO
    AtletaRepository <|.. AtletaDAO

    class AthletizaException {
        <<abstract>>
    }
    Exception <|-- AthletizaException
    AthletizaException <|-- ValidacaoException
    AthletizaException <|-- RegraNegocioException
    AthletizaException <|-- PersistenciaException
    DAOBase ..> PersistenciaException : lança
```

Todas as entidades seguem o mesmo par interface / implementação:

| Interface (abstração) | Implementação (JDBC) | Métodos além do GenericDAO |
|---|---|---|
| `ModalidadeRepository` | `ModalidadeDAO` | `possuiVinculos` |
| `AtletaRepository` | `AtletaDAO` | `existeMatricula`, `listarVinculos` |
| `MembroRepository` | `MembroDAO` | `listarAtivos`, `existeMatricula`, `listarVinculos` |
| `CargoRepository` | `CargoDAO` | `contarOcupacoes` |
| `GestaoRepository` | `GestaoDAO` | `carregarComposicao`, `adicionarMembro`, `removerMembro`, `trocarMembro` |
| `CompeticaoRepository` | `CompeticaoDAO` | `carregarInscricoes`, `salvarInscricoes`, `listarResultados`, `salvarResultado`, `excluirResultado` |
| `TreinoRepository` | `TreinoDAO` | `listar`, `listarNoMesmoDiaELocal`, `resumirPresencas`, `carregarPresencas`, `salvarPresencas`, `frequencia`, `historicoDoAtleta` |
| `EventoRepository` | `EventoDAO` | `carregarDetalhes`, `adicionarResponsavel`, `removerResponsavel` |
| `CompromissoRepository` | `CompromissoDAO` | `listarPorPeriodo` |
| `UsuarioRepository` | `UsuarioDAO` | `buscarPorLogin`, `existeLogin`, `contarOutrosAdministradoresAtivos` |
| `PastaRepository` | `PastaDAO` | (só os do GenericDAO) |
| `ArquivoRepository` | `ArquivoDAO` | `listarPorPasta` |
| `TarefaRepository` | `TarefaDAO` | `listar`, `salvar`, `excluir` (de um evento ou competição) |
| `AtividadeRepository` | `AtividadeDAO` | `listarPorPeriodo` (todas as atividades do calendário) |
| `AlertaRepository` | `AlertaDAO` | `tarefasAtrasadas`, `treinosSemPresenca`, `competicoesProximas` |

## 3. Controllers (`br.com.athletiza.controller`)

Um controller por tela, como no exemplo `PacienteController` da aula: tem a View e o Model como
atributos, recebe tudo no construtor (inclusive os repositórios, pela interface) e registra as ações
dos botões com lambda em `adicionarAcoesBotoes()`. Cada ação chama um único fluxo de caso de uso.

```mermaid
classDiagram
    direction LR

    class CadastroAtletaController {
        -PainelCadastroAtleta view
        -Atleta model
        -AtletaRepository repository
        -ModalidadeRepository modalidadeRepository
        -Navegador navegador
        +CadastroAtletaController(view, model, repository, modalidadeRepository, navegador)
        +adicionarAcoesBotoes()
        -salvar()
        ~gravar()
        -cancelar()
    }
    class ConsultaAtletasController {
        -PainelConsultaAtletas view
        -AtletaRepository repository
        +adicionarAcoesBotoes()
        -carregar()
        ~pesquisar(texto, modalidade, situacao, ordem) List~Atleta~
        ~excluir(Atleta)
        -abrirCadastro(Atleta)
    }
    class PrincipalController {
        -TelaPrincipal view
        -AtletaRepository atletaRepository
        -ModalidadeRepository modalidadeRepository
        -criarModulos()
    }

    ConsultaAtletasController --> PainelConsultaAtletas : view
    ConsultaAtletasController --> AtletaRepository
    ConsultaAtletasController ..> CadastroAtletaController : cria ao incluir/editar
    CadastroAtletaController --> PainelCadastroAtleta : view
    CadastroAtletaController --> Atleta : model
    CadastroAtletaController --> AtletaRepository
    CadastroAtletaController --> ModalidadeRepository
    PrincipalController ..> AtletaDAO : cria (único lugar)
    PrincipalController ..> ConsultaAtletasController : cria
    AtletaRepository <|.. AtletaDAO
```

| Módulo | Controllers |
|---|---|
| Login e janela principal | `LoginController`, `PrincipalController`, `AlterarSenhaController` |
| Início | `CalendarioController`, `CompromissoController` |
| Gestão | `ConsultaGestoesController`, `CadastroGestaoController`, `GerenciarGestaoController`, `OrganogramaController`, `ConsultaCargosController`, `CadastroCargoController`, `ConsultaMembrosController`, `CadastroMembroController` |
| Atletas | `ConsultaAtletasController`, `CadastroAtletaController`, `ConsultaModalidadesController`, `CadastroModalidadeController` |
| Competições | `ConsultaCompeticoesController`, `CadastroCompeticaoController`, `GerenciarCompeticaoController`, `ResultadosController`, `TarefasController` |
| Treinos | `TreinosController`, `CadastroTreinoController`, `PresencaController`, `FrequenciaController`, `HistoricoAtletaController` |
| Eventos | `ConsultaEventosController`, `CadastroEventoController`, `GerenciarEventoController`, `TarefasController` |
| Registros | `RegistrosController` |
| Usuários | `ConsultaUsuariosController`, `CadastroUsuarioController` |

## 4. Telas (`br.com.athletiza.view`)

As telas não conhecem controllers nem DAOs. As classes base já trazem o que a View faz no MVC:
`adicionarAcaoX(ActionListener)` para o Controller registrar os botões, `mostrarMensagem`,
`mostrarErro`, `confirmar` e, nas consultas, `exibir(lista)` e `getSelecionado()`.

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
        +exibir(List~T~)
        +getSelecionado() T
        +adicionarAcaoCarregar(ActionListener)
        +adicionarAcaoIncluir(ActionListener)
        +adicionarAcaoEditar(ActionListener)
        +adicionarAcaoExcluir(ActionListener)
        +adicionarAcaoGerenciar(ActionListener)
        +mostrarMensagem(String)
        +mostrarErro(Exception)
        +confirmar(String) boolean
        +carregar()
    }
    class PainelFormulario {
        <<abstract>>
        +adicionarAcaoSalvar(ActionListener)
        +adicionarAcaoCancelar(ActionListener)
        +getBtnSalvar() JButton
        +mostrarMensagem(String)
        +mostrarErro(Exception)
        +confirmar(String) boolean
        #adicionarCampo(String, JComponent, boolean)
    }
    class PainelCadastroAtleta {
        -JTextField nome
        -JTextField matricula
        -inicializarComponentes()
        +preencherCampos(Atleta, List~Modalidade~)
        +getNome() String
        +getMatricula() String
        +getModalidadesMarcadas() Map
    }
    class ModeloTabela~T~ {
        +coluna(String, Class, Function) ModeloTabela
        +setLinhas(List~T~)
    }
    class TelaPrincipal {
        -CardLayout cartoes
        -Deque~JComponent~ telasAbertas
        +adicionarModulo(String, JComponent)
    }

    JPanel <|-- PainelConsulta
    JPanel <|-- PainelFormulario
    Recarregavel <|.. PainelConsulta
    Navegador <|.. TelaPrincipal
    JFrame <|-- TelaPrincipal
    JFrame <|-- TelaLogin
    PainelConsulta --> ModeloTabela
    AbstractTableModel <|-- ModeloTabela
    PainelFormulario <|-- PainelCadastroAtleta

    PainelConsulta <|-- PainelConsultaAtletas
    PainelConsulta <|-- PainelConsultaGestoes
    PainelConsulta <|-- PainelConsultaCompeticoes
    PainelConsulta <|-- PainelTreinos
    PainelConsulta <|-- PainelConsultaEventos
    PainelConsulta <|-- PainelConsultaUsuarios
    PainelFormulario <|-- PainelCadastroGestao
    PainelFormulario <|-- PainelCadastroCompeticao
    PainelFormulario <|-- PainelCadastroTreino
    PainelFormulario <|-- PainelPresenca
    PainelFormulario <|-- PainelCadastroEvento
    Recarregavel <|.. PainelCalendario
    Recarregavel <|.. PainelGerenciarCompeticao
    Recarregavel <|.. PainelOrganograma
    Recarregavel <|.. PainelRegistros
    JComponent <|-- CartaoArquivo
    PainelRegistros --> CartaoArquivo
```
