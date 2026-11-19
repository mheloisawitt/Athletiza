# Matriz de requisitos técnicos (anexo do CH-51)

Onde cada requisito técnico da disciplina aparece no código, para a parte **Implementação** da apresentação.
Caminhos relativos a `src/main/java/br/com/athletiza/`.

| Requisito | Onde está | O que mostrar |
|---|---|---|
| **MVC** | `modelo/`, `visao/`, `controlador/` | O menu (`visao/TelaPrincipal`) cria a visão e o DAO e entrega ao controlador; o controlador registra as ações (`visao.adicionarAcaoBtnSalvar(e -> ...)`), lê os campos pelos getters da visão (`PainelCadastroAtleta.getNome()`), valida, grava pelo repositório (`AtletaRepositorio.inserir()`, implementado por `dao/AtletaDAO`) e devolve o resultado com `apresentarMensagem` |
| **Repositório e DAO** | `repositorio/Repositorio` e uma interface por entidade (`AtletaRepositorio`...); `dao/AbstractDAO`, `dao/DAOBase` e um DAO por entidade | O controlador conhece só a interface do repositório; o DAO a implementa (`AtletaDAO implements AtletaRepositorio`); `AbstractDAO` implementa `excluir`, `buscarPorId` e `listarTodos` para os DAOs JDBC |
| **Pacotes** | `app`, `modelo`, `visao` (com subpacotes por módulo), `controlador`, `repositorio`, `dao`, `util`, `excecao` | Estrutura no README e no NetBeans (aba Projects) |
| **Modificadores de escopo e visibilidade** | Atributos `private` com getters/setters em todo o `modelo` | `protected abstract` em `AbstractDAO` (`getTabela`, `mapear`); classes de pacote `modelo/Textos` e `visao/calendario/CelulaDia`; construtor `private` em `util/Cores`, `util/Senha`, `util/ConnectionFactory` |
| **Interface** | `repositorio/*Repositorio`, `dao/MapeadorLinha`, `visao/componentes/Navegador`, `visao/componentes/Recarregavel`, `TarefaControlador.FonteTarefas` | `TelaPrincipal implements Navegador`; `PainelCalendario implements Recarregavel` |
| **Classe abstrata** | `modelo/Entidade`, `modelo/Pessoa`, `modelo/Atividade`, `dao/DAOBase`, `dao/AbstractDAO`, `excecao/AthletizaException`, `visao/componentes/PainelConsulta`, `visao/componentes/PainelFormulario` | `Atividade.getTipo()` é abstrato e cada subclasse responde |
| **Herança** | `Pessoa` → `Membro`, `Atleta`, `Usuario`; `Atividade` → `Treino` → `Amistoso`, `Competicao`, `Evento`, `Compromisso` | Diagrama `docs/diagramas/imagens/classes_1_modelo.png` |
| **Polimorfismo** | `Atividade.ocorreEm()` sobrescrito em `Competicao` (vários dias); `getTipo()`, `getRotuloCurto()` e `getDescricaoCalendario()` | O calendário (`CalendarioControlador.atividadesDoMes`) trata tudo como `Atividade`; `TreinoDAO.inserir` grava `Treino` ou `Amistoso` conforme o objeto; `Usuario.equals` sobrescreve `Pessoa.equals` |
| **List** | `Evento.tarefas`, `Competicao.resultados`, `ModeloTabela.linhas`, retornos dos controladores | `Evento.getTarefasAtrasadas(hoje)` |
| **Map** | `Atleta.modalidades` (`Map<Modalidade, SituacaoAtleta>`), `Competicao.inscricoes` (`Map<Modalidade, Set<Atleta>>`), `Treino.presencas` (`Map<Atleta, Boolean>`) | `CalendarioControlador.atividadesDoMes` devolve `Map<LocalDate, List<Atividade>>` (TreeMap); `Gestao.getOrganograma()` devolve `Map<Cargo, List<Membro>>` |
| **Set** | `Gestao.composicao` (`Set<MembroCargo>`), atletas de cada modalidade em `Competicao`, `Evento.responsaveis` | `Gestao.adicionarMembro` retorna `false` quando o membro já ocupa o cargo (o Set impede repetição); `MembroCargo` é um `record`, então `equals`/`hashCode` são automáticos |
| **Swing** | Todo o pacote `visao` | `TelaLogin`, `TelaPrincipal` (CardLayout), `PainelCalendario`, `PainelOrganograma` (desenho com Graphics2D), `PainelRegistros` (miniaturas, `JFileChooser`, arrastar e soltar com `TransferHandler`) |
| **JTable** | `visao/componentes/PainelConsulta` + `ModeloTabela` (todas as consultas) | Tabelas editáveis: `PainelCadastroAtleta` (checkbox e combo como editor), `PainelPresenca`, `PainelGerenciarCompeticao`; renderizadores em `Renderizadores` (etiquetas de situação) e `PainelFrequencia` (barra de percentual) |
| **Listeners** | `ActionListener` em todos os botões, registrado pelo controlador (`visao.adicionarAcaoBtnSalvar(e -> ...)`) | `DocumentListener` na busca (`PainelConsulta`), `MouseAdapter` no clique duplo e nos dias do calendário, `ListSelectionListener` em `PainelGerenciarCompeticao` e `PainelTreinos`, `ItemListener` no `MenuLateral`, `ChangeListener` nas abas |
| **Classe anônima** | `PainelConsulta.criarCabecalho()` (`new DocumentListener() {...}`) | `EventoControlador.gerenciar()` (`new TarefaControlador.FonteTarefas() {...}`), `PainelCalendario.criarMes()` (`new MouseAdapter() {...}`), `Combos.exibirComo()` (`new ListCellRenderer<>() {...}`) |
| **Lambda** | Colunas das tabelas: `.coluna("Nome", String.class, Atleta::getNome)` | Ações: `visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada())`; filtros com stream em `AtletaControlador.pesquisar`; transações em `AtletaDAO.inserir` (`emTransacao("incluir", con -> {...})`) |
| **Comparable** | `Atleta`, `Atividade`, `Gestao`, `Cargo`, `Modalidade`, `Tarefa`, `Frequencia`, `Pasta`, `Arquivo` | `Atleta.compareTo` ordena por nome respeitando acentos (`Textos.COMPARADOR` usa `Collator` pt-BR) |
| **Comparator** | `Atleta.POR_NOME`, `POR_MATRICULA`, `POR_SITUACAO`, `POR_MODALIDADE`; `Atividade.POR_DATA`; `Gestao.POR_PERIODO` | O combo "Ordenar por" em `PainelConsultaAtletas` escolhe o Comparator; `PainelGerenciarGestao.POR_CARGO` usa `Comparator.comparing(...).thenComparing(...)` |
| **Exceções e controle de erros** | `excecao/` (`ValidacaoException`, `RegraNegocioException`, `PersistenciaException`) | `DAOBase.traduzir()` converte `SQLException` em mensagem amigável; `DAOBase.emTransacao()` faz rollback; `Mensagens.erro()` exibe; `Athletiza.main()` registra o tratador global de erros não capturados; `util/Log` grava os detalhes técnicos em arquivo |
| **Banco de dados (JPA)** | `modelo/Modalidade` (`@Entity`), `modelo/Entidade` (`@MappedSuperclass`, `@Id`), `util/JPAUtil`, `dao/ModalidadeDAO`, `META-INF/persistence.xml` | `ModalidadeDAO` usa `EntityManager` (`persist`, `merge`, `remove`, `find` e consulta JPQL) com transação (`begin`, `commit`, `rollback`); Hibernate como implementação |
| **Testes unitários** | `src/test/java` (JUnit 5 com banco H2 em memória) | Regras de negócio testadas nos controladores (`DemandaControladorTest`, `GestaoControladorTest`, `AtletaControladorTest`...) e nos DAOs (`ModalidadeDAOTest` testa o DAO com JPA) |
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
