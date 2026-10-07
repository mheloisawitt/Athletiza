# Matriz de requisitos técnicos (anexo do CH-51)

Onde cada requisito técnico da disciplina aparece no código, para a parte **Implementação** da apresentação.
Caminhos relativos a `src/main/java/br/com/athletiza/`.

| Requisito | Onde está | O que mostrar |
|---|---|---|
| **MVC** | `model/`, `view/`, `controller/` | Fluxo completo: `view/atletas/PainelCadastroAtleta.salvar()` → `controller/AtletaController.salvar()` → `dao/AtletaDAO.inserir()` → `model/Atleta` |
| **DAO** | `dao/GenericDAO`, `dao/AbstractDAO`, `dao/DAOBase` e um DAO por entidade | `GenericDAO` define o contrato; `AbstractDAO` implementa `excluir`, `buscarPorId` e `listarTodos` para todos; cada DAO só escreve o SQL específico (`ModalidadeDAO` é o exemplo mais curto) |
| **Pacotes** | `model`, `view` (com subpacotes por módulo), `controller`, `dao`, `util`, `exception` | Estrutura no README e no NetBeans (aba Projects) |
| **Modificadores de escopo e visibilidade** | Atributos `private` com getters/setters em todo o `model` | `protected abstract` em `AbstractDAO` (`getTabela`, `mapear`); classes de pacote `model/Textos` e `view/calendario/CelulaDia`; construtor `private` em `util/Cores`, `util/Senha`, `util/ConnectionFactory`; construtor de pacote em `LoginController` (para testes) |
| **Interface** | `dao/GenericDAO`, `dao/MapeadorLinha`, `view/componentes/Navegador`, `view/componentes/Recarregavel` | `TelaPrincipal implements Navegador`; `PainelCalendario implements Recarregavel` |
| **Classe abstrata** | `model/Entidade`, `model/Pessoa`, `model/Atividade`, `dao/DAOBase`, `dao/AbstractDAO`, `exception/AthletizaException`, `view/componentes/PainelConsulta`, `view/componentes/PainelFormulario` | `Atividade.getTipo()` é abstrato e cada subclasse responde |
| **Herança** | `Pessoa` → `Membro`, `Atleta`, `Usuario`; `Atividade` → `Treino` → `Amistoso`, `Competicao`, `Evento`, `Compromisso` | Diagrama `docs/diagramas/classes.md` |
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
| **Banco de dados (JDBC)** | `util/ConnectionFactory`, `dao/*`, `src/main/resources/sql/` | `PreparedStatement` com parâmetros em `DAOBase`; transação em `AtletaDAO`, `CompeticaoDAO`, `TreinoDAO`; integridade (RESTRICT/CASCADE) em `01_estrutura.sql` e `docs/banco/DER.md` |

## Requisitos funcionais por tela

| RF | Tela | Chamados |
|---|---|---|
| RF01 | Usuários (menu, só administradores) | RF01, CH-12, CH-13 |
| RF02 | Gestão > Membros | CH-22 |
| RF03, RF04 | Atletas | CH-24 a CH-26 |
| RF05 | Atletas > Modalidades | CH-27 |
| RF06 | Gestão | CH-18 a CH-20 |
| RF07, RF08 | Gestão > Cargos, Gerenciar, Organograma | CH-21, CH-23 |
| RF09, RF10, RF24 | Início (calendário e próximos eventos) | CH-15 a CH-17 |
| RF11 a RF13 | Treinos, Presença, Frequência | CH-33 a CH-37 |
| RF14 a RF16 | Competições, Gerenciar, Resultados | CH-28 a CH-32 |
| RF17 a RF19 | Eventos, Responsáveis e tarefas | CH-38 a CH-41 |
| RF20, RF21 | Busca, filtros e ordenação em todas as consultas | CH-44 |
| RF22 | Confirmação e bloqueio de exclusões com vínculos | CH-45 |
| — | Registros: galeria de fotos e vídeos em pastas (definido com a diretoria) | CH-42, CH-43 |
| RF23 | Campos "Observações" em treinos, eventos e competições | CH-30, CH-35, CH-40 |
