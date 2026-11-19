# Matriz de requisitos técnicos (anexo do CH-51)

Onde cada requisito técnico da disciplina aparece no código, para a parte **Implementação** da apresentação.
Caminhos relativos a `src/main/java/br/com/athletiza/`.

| Requisito | Onde está | O que mostrar |
|---|---|---|
| **MVC** | `model/`, `view/`, `controller/` | Fluxo completo: `view/atletas/PainelCadastroAtleta.salvar()` → `controller/AtletaController.salvar()` → `dao/AtletaDAO.inserir()` → `model/Atleta` |
| **DAO** | `dao/GenericDAO`, `dao/AbstractDAO`, `dao/DAOBase` e um DAO por entidade | `GenericDAO` define o contrato; `AbstractDAO` implementa `excluir`, `buscarPorId` e `listarTodos` para os DAOs JDBC; cada DAO só escreve o SQL específico |
| **Pacotes** | `model`, `view` (com subpacotes por módulo), `controller`, `dao`, `util`, `exception` | Estrutura no README e no NetBeans (aba Projects) |
| **Modificadores de escopo e visibilidade** | Atributos `private` com getters/setters em todo o `model` | `protected abstract` em `AbstractDAO` (`getTabela`, `mapear`); classes de pacote `model/Textos` e `view/calendario/CelulaDia`; construtor `private` em `util/Cores`, `util/Senha`, `util/ConnectionFactory`; construtor de pacote em `LoginController` (para testes) |
| **Interface** | `dao/GenericDAO`, `dao/MapeadorLinha`, `view/componentes/Navegador`, `view/componentes/Recarregavel`, `PainelTarefas.FonteTarefas` | `TelaPrincipal implements Navegador`; `PainelCalendario implements Recarregavel` |
| **Classe abstrata** | `model/Entidade`, `model/Pessoa`, `model/Atividade`, `dao/DAOBase`, `dao/AbstractDAO`, `exception/AthletizaException`, `view/componentes/PainelConsulta`, `view/componentes/PainelFormulario` | `Atividade.getTipo()` é abstrato e cada subclasse responde |
| **Herança** | `Pessoa` → `Membro`, `Atleta`, `Usuario`; `Atividade` → `Treino` → `Amistoso`, `Competicao`, `Evento`, `Compromisso` | Diagrama `docs/diagramas/imagens/classes_1_modelo.png` |
| **Polimorfismo** | `Atividade.ocorreEm()` sobrescrito em `Competicao` (vários dias); `getTipo()`, `getRotuloCurto()` e `getDescricaoCalendario()` | O calendário (`CalendarioController.atividadesDoMes`) trata tudo como `Atividade`; `TreinoDAO.inserir` grava `Treino` ou `Amistoso` conforme o objeto; `Usuario.equals` sobrescreve `Pessoa.equals` |
| **List** | `Evento.tarefas`, `Competicao.resultados`, `ModeloTabela.linhas`, retornos dos controllers | `Evento.getTarefasAtrasadas(hoje)` |
| **Map** | `Atleta.modalidades` (`Map<Modalidade, SituacaoAtleta>`), `Competicao.inscricoes` (`Map<Modalidade, Set<Atleta>>`), `Treino.presencas` (`Map<Atleta, Boolean>`) | `CalendarioController.atividadesDoMes` devolve `Map<LocalDate, List<Atividade>>` (TreeMap); `Gestao.getOrganograma()` devolve `Map<Cargo, List<Membro>>` |
| **Set** | `Gestao.composicao` (`Set<MembroCargo>`), atletas de cada modalidade em `Competicao`, `Evento.responsaveis` | `Gestao.adicionarMembro` retorna `false` quando o membro já ocupa o cargo (o Set impede repetição); `MembroCargo` é um `record`, então `equals`/`hashCode` são automáticos |
| **Swing** | Todo o pacote `view` | `TelaLogin`, `TelaPrincipal` (CardLayout), `PainelCalendario`, `PainelOrganograma` (desenho com Graphics2D), `PainelRegistros` (miniaturas, `JFileChooser`, arrastar e soltar com `TransferHandler`) |
| **JTable** | `view/componentes/PainelConsulta` + `ModeloTabela` (todas as consultas) | Tabelas editáveis: `PainelCadastroAtleta` (checkbox e combo como editor), `PainelPresenca`, `PainelGerenciarCompeticao`; renderizadores em `Renderizadores` (etiquetas de situação) e `PainelFrequencia` (barra de percentual) |
| **Listeners** | `ActionListener` em todos os botões (`view/componentes/Botoes`) | `DocumentListener` na busca (`PainelConsulta`), `MouseAdapter` no clique duplo e nos dias do calendário, `ListSelectionListener` em `PainelGerenciarCompeticao` e `PainelTreinos`, `ItemListener` no `MenuLateral`, `ChangeListener` nas abas |
| **Classe anônima** | `PainelConsulta.criarCabecalho()` (`new DocumentListener() {...}`) | `TelaLogin.entrar()` (`new SwingWorker<>() {...}`), `PainelCalendario.criarMes()` (`new MouseAdapter() {...}`), `Combos.exibirComo()` (`new ListCellRenderer<>() {...}`) |
| **Lambda** | Colunas das tabelas: `.coluna("Nome", String.class, Atleta::getNome)` | Botões: `Botoes.primario("Salvar", e -> salvar())`; filtros com stream em `AtletaController.pesquisar`; transações em `AtletaDAO.inserir` (`emTransacao("incluir", con -> {...})`) |
| **Comparable** | `Atleta`, `Atividade`, `Gestao`, `Cargo`, `Modalidade`, `Tarefa`, `Frequencia`, `Pasta`, `Arquivo` | `Atleta.compareTo` ordena por nome respeitando acentos (`Textos.COMPARADOR` usa `Collator` pt-BR) |
| **Comparator** | `Atleta.POR_NOME`, `POR_MATRICULA`, `POR_SITUACAO`, `POR_MODALIDADE`; `Atividade.POR_DATA`; `Gestao.POR_PERIODO` | O combo "Ordenar por" em `PainelConsultaAtletas` escolhe o Comparator; `PainelGerenciarGestao.POR_CARGO` usa `Comparator.comparing(...).thenComparing(...)` |
| **Exceções e controle de erros** | `exception/` (`ValidacaoException`, `RegraNegocioException`, `PersistenciaException`) | `DAOBase.traduzir()` converte `SQLException` em mensagem amigável; `DAOBase.emTransacao()` faz rollback; `Mensagens.erro()` exibe; `Athletiza.main()` registra o tratador global de erros não capturados; `util/Log` grava os detalhes técnicos em arquivo |
| **Banco de dados (JPA)** | `model/Modalidade` (`@Entity`), `model/Entidade` (`@MappedSuperclass`, `@Id`), `util/JPAUtil`, `dao/ModalidadeDAO`, `META-INF/persistence.xml` | `ModalidadeDAO` usa `EntityManager` (`persist`, `merge`, `remove`, `find` e consulta JPQL) com transação (`begin`, `commit`, `rollback`); Hibernate como implementação |
| **Testes unitários** | `src/test/java` (JUnit 5 com banco H2 em memória) | Regras de negócio testadas nos controllers (`DemandaControllerTest`, `GestaoControllerTest`, `AtletaControllerTest`...) e nos DAOs (`ModalidadeDAOTest` testa o DAO com JPA) |
| **Diagramas UML** | `docs/diagramas/` | Imagens em `imagens/`: classes (`classes_*.png`), sequência (`seq_*.png`) e colaboração (`colab_*.png`) |
| **Banco de dados (JDBC)** | `util/ConnectionFactory`, `dao/*`, `src/main/resources/sql/` | `PreparedStatement` com parâmetros em `DAOBase`; transação em `AtletaDAO`, `CompeticaoDAO`, `TreinoDAO`; integridade (RESTRICT/CASCADE) em `01_estrutura.sql` |

## Requisitos funcionais por tela

Numeração do documento `docs/requisitos/Requisitos_Athletiza.pdf`.

| RF | Tela | Chamados |
|---|---|---|
| RF01, RF02 | Login | CH-12, CH-52 |
| RF03, RF41 | Usuários (só Administrador) e perfis de acesso | CH-13, CH-65 |
| RF04, RF05 | Menu lateral e botão Sair | CH-14, CH-52 |
| RF06 a RF10 | Início: calendário, compromissos e próximos eventos | CH-15 a CH-17, CH-53 |
| RF11 a RF17 | Busca, filtros, ordenação, paginação e exclusão com confirmação em todas as consultas | CH-44, CH-45, CH-54 |
| RF18 a RF22 | Gestão, cargos, membros e organograma | CH-18 a CH-23, CH-55 |
| RF23 a RF25 | Atletas e modalidades | CH-24 a CH-27, CH-56, CH-57 |
| RF26 a RF30 | Competições, gerenciar e resultados | CH-28 a CH-32, CH-58 |
| RF31 a RF34 | Treinos, presença e frequência | CH-33 a CH-37 |
| RF35 a RF37 | Eventos, responsáveis e tarefas | CH-38 a CH-41, CH-59 |
| RF38 a RF40 | Registros: pastas de fotos e vídeos | CH-60 a CH-62 |
| RF42 a RF44 | Dashboard | CH-68 |
| RF45 a RF48 | Demandas | CH-66, CH-67 |
