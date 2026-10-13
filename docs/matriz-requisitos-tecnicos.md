# Matriz de requisitos técnicos (anexo do CH-51)

Onde cada requisito técnico da disciplina aparece no código, para a parte **Implementação** da apresentação.
Caminhos relativos a `src/main/java/br/com/athletiza/`.

| Requisito | Onde está | O que mostrar |
|---|---|---|
| **MVC** | `model/`, `view/`, `controller/` | Um controller por tela, como o `PacienteController` da aula: `controller/CadastroAtletaController` tem a View (`PainelCadastroAtleta`) e o Model (`Atleta`) como atributos, recebe tudo no construtor e registra os botões em `adicionarAcoesBotoes()` com lambda (`view.adicionarAcaoSalvar(e -> salvar())`). A View só tem componentes, getters, `preencherCampos`, `mostrarMensagem`/`mostrarErro` e `adicionarAcaoX`; não conhece controller nem DAO. O Model tem as regras (`Atleta.validar()`) e não conhece a tela |
| **DAO** | `dao/GenericDAO`, `dao/AbstractDAO`, `dao/DAOBase` e um DAO por entidade | `GenericDAO` define o contrato; `AbstractDAO` implementa `excluir`, `buscarPorId` (retorna `null` se não achar) e `listarTodos`; cada DAO só escreve o SQL específico (`ModalidadeDAO` é o exemplo mais curto) |
| **Inversão de Dependência (DIP)** | `dao/XRepository` (interfaces) e `controller/PrincipalController` | Como `AlunoRepository`/`AlunoDAO` da aula: `ModalidadeDAO implements ModalidadeRepository`; os controllers só conhecem a interface (`private final ModalidadeRepository repository`) e a recebem no construtor. Só o `PrincipalController` faz `new ModalidadeDAO()`. Nos testes, `CadastroModalidadeControllerTest` usa um `ModalidadeRepositoryFake` em memória, sem banco |
| **Pacotes** | `model`, `view` (com subpacotes por módulo), `controller`, `dao`, `util`, `exception` | Estrutura no README e no NetBeans (aba Projects). Regras: model não conhece os outros pacotes; view conhece o model (e utilitários como `Cores`), nunca controller ou dao; controller conhece view, model e as interfaces do dao |
| **Modificadores de escopo e visibilidade** | Atributos `private` com getters/setters em todo o `model` | `protected abstract` em `AbstractDAO` (`getTabela`, `mapear`); classes de pacote `model/Textos` e `view/calendario/CelulaDia`; métodos de pacote nos controllers (`gravar()`, `excluir(...)`) para os testes; construtor `private` em `util/Cores`, `util/Senha`, `util/ConnectionFactory` |
| **Interface** | `dao/GenericDAO`, os `dao/XRepository`, `dao/MapeadorLinha`, `view/componentes/Navegador`, `view/componentes/Recarregavel` | `ModalidadeDAO implements ModalidadeRepository`; `TelaPrincipal implements Navegador`; `PainelCalendario implements Recarregavel` |
| **Classe abstrata** | `model/Entidade`, `model/Pessoa`, `model/Atividade`, `dao/DAOBase`, `dao/AbstractDAO`, `exception/AthletizaException`, `view/componentes/PainelConsulta`, `view/componentes/PainelFormulario` | `Atividade.getTipo()` é abstrato e cada subclasse responde |
| **Herança** | `Pessoa` → `Membro`, `Atleta`, `Usuario`; `Atividade` → `Treino` → `Amistoso`, `Competicao`, `Evento`, `Compromisso` | Diagrama `docs/diagramas/classes.md` |
| **Polimorfismo** | `Atividade.ocorreEm()` sobrescrito em `Competicao` (vários dias); `getTipo()`, `getRotuloCurto()` e `getDescricaoCalendario()`; `Treino.validarCampos()` sobrescrito em `Amistoso` | O calendário (`CalendarioController.atividadesDoPeriodo`) trata tudo como `Atividade`; `CadastroTreinoController` chama `model.validar()` sem saber se é treino ou amistoso; `TreinoDAO.inserir` grava `Treino` ou `Amistoso` conforme o objeto |
| **List** | `Evento.tarefas`, `Competicao.resultados`, `ModeloTabela.linhas`, retornos dos repositórios | `Evento.getTarefasAtrasadas(hoje)` (laço `for` + `Collections.sort`) |
| **Map** | `Atleta.modalidades` (`Map<Modalidade, SituacaoAtleta>`), `Competicao.inscricoes` (`Map<Modalidade, Set<Atleta>>`), `Treino.presencas` (`Map<Atleta, Boolean>`) | `CalendarioController.atividadesDoPeriodo` devolve `Map<LocalDate, List<Atividade>>` (TreeMap); `Gestao.getOrganograma()` devolve `Map<Cargo, List<Membro>>` |
| **Set** | `Gestao.composicao` (`Set<MembroCargo>`), atletas de cada modalidade em `Competicao`, `Evento.responsaveis` | `Gestao.adicionarMembro` retorna `false` quando o membro já ocupa o cargo: o Set impede repetição porque `MembroCargo` sobrescreve `equals` e `hashCode` |
| **Swing** | Todo o pacote `view` | `TelaLogin`, `TelaPrincipal` (CardLayout), `PainelCalendario`, `PainelOrganograma` (desenho com Graphics2D), `PainelRegistros` (miniaturas, `JFileChooser`, arrastar e soltar com `TransferHandler`) |
| **JTable** | `view/componentes/PainelConsulta` + `ModeloTabela` (todas as consultas) | Tabelas editáveis: `PainelCadastroAtleta` (checkbox e combo como editor), `PainelPresenca`, `PainelGerenciarCompeticao`; renderizadores em `Renderizadores` (etiquetas de situação) e `PainelFrequencia` (barra de percentual) |
| **Listeners** | `ActionListener` em todos os botões, registrados pelos controllers (`adicionarAcaoSalvar`, `adicionarAcaoIncluir`...) | `DocumentListener` na busca (`PainelConsulta`), `MouseAdapter` no clique duplo e nos dias do calendário, `ListSelectionListener` em `PainelGerenciarCompeticao` e `PainelTreinos`, `ItemListener` no `MenuLateral`, `ChangeListener` nas abas |
| **Classe anônima** | `PainelConsulta.criarCabecalho()` (`new DocumentListener() {...}`) | `LoginController.entrar()` (`new SwingWorker<>() {...}`), `PainelCalendario.criarMes()` (`new MouseAdapter() {...}`), `Combos.exibirComo()` (`new ListCellRenderer<>() {...}`) |
| **Lambda** | Ações dos botões nos controllers: `view.adicionarAcaoSalvar(e -> salvar())` | Colunas das tabelas: `.coluna("Nome", String.class, a -> a.getNome())`; comparadores `Atleta.POR_NOME = (a, b) -> ...`; transações em `AtletaDAO.inserir` (`emTransacao("incluir", con -> {...})`) |
| **Comparable** | `Atleta`, `Atividade`, `Gestao`, `Cargo`, `Modalidade`, `Tarefa`, `Frequencia`, `Pasta`, `Arquivo`, `MembroCargo` | `Atleta.compareTo` ordena por nome respeitando acentos (`Textos.comparar` usa `Collator` pt-BR) |
| **Comparator** | `Atleta.POR_NOME`, `POR_MATRICULA`, `POR_SITUACAO`, `POR_MODALIDADE` | O combo "Ordenar por" em `PainelConsultaAtletas` escolhe o Comparator; `ConsultaAtletasController.pesquisar` faz `Collections.sort(lista, ordem)` |
| **Exceções e controle de erros** | `exception/`: `AthletizaException` (abstrata, herda de `Exception`: verificada) e as filhas `ValidacaoException`, `RegraNegocioException`, `PersistenciaException`, cada uma com construtor padrão e com mensagem | **throw**: `Atleta.validar()` e os controllers lançam; **throws**: `gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException` propaga até o fluxo do botão; **try/catch**: `salvar()` captura `AthletizaException` e chama `view.mostrarErro(e)`; **finally**: `CadastroUsuarioController.salvar()` apaga a senha da memória mesmo com erro; **try-with-resources**: `DAOBase` fecha `Connection`, `PreparedStatement` e `ResultSet`; **relançar**: `DAOBase.emTransacao()` faz rollback e relança; `GerenciarGestaoController.adicionarMembro` desfaz na memória e relança; **multi-catch**: `PainelRegistros.SoltarArquivos`; **causa**: `traduzir()` guarda a `SQLException` dentro da `PersistenciaException`; **log**: `util/Log` grava o stack trace em arquivo e `Athletiza.main()` registra o tratador global de erros não capturados |
| **Banco de dados (JDBC)** | `util/ConnectionFactory`, `dao/*`, `src/main/resources/sql/` | `PreparedStatement` com parâmetros em `DAOBase`; transação em `AtletaDAO`, `CompeticaoDAO`, `TreinoDAO`; integridade (RESTRICT/CASCADE) em `01_estrutura.sql` e `docs/banco/DER.md` |

O código usa apenas o que foi visto nas aulas: classes, herança, interfaces, coleções (List, Map, Set),
lambda, laços `for` e `switch` clássico. Não são usados `Optional`, `Stream`, `record`, referência de
método (`::`), `var` nem `switch` com `->`.

## Requisitos funcionais por tela

| RF | Tela | Chamados |
|---|---|---|
| RF01 | Usuários (menu, só administradores) | RF01, CH-12, CH-13 |
| RF02 | Gestão > Membros | CH-22 |
| RF03, RF04 | Atletas | CH-24 a CH-26 |
| RF05 | Atletas > Modalidades | CH-27 |
| RF06 | Gestão | CH-18 a CH-20 |
| RF07, RF08 | Gestão > Cargos, Gerenciar, Organograma | CH-21, CH-23 |
| RF09, RF10, RF24 | Início: calendário por mês ou semana, abrir atividades, alertas e próximos eventos | CH-15 a CH-17 |
| RF11 a RF13 | Treinos, Presença, Frequência, Histórico do atleta | CH-33 a CH-37 |
| RF14 a RF16 | Competições, Gerenciar, Resultados | CH-28 a CH-32 |
| RF17 a RF19 | Eventos, Responsáveis e tarefas; Tarefas também em Gerenciar Competição | CH-38 a CH-41 |
| RF20, RF21 | Busca, filtros e ordenação em todas as consultas | CH-44 |
| RF22 | Confirmação e bloqueio de exclusões com vínculos | CH-45 |
| — | Registros: galeria de fotos e vídeos em pastas (definido com a diretoria) | CH-42, CH-43 |
| RF23 | Campos "Observações" em treinos, eventos e competições | CH-30, CH-35, CH-40 |
