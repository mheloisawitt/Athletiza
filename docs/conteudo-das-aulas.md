# Conteúdo das aulas no código

Onde cada assunto dos slides da disciplina aparece no Athletiza. Serve para conferir o nível do código e
para preparar a apresentação. Caminhos relativos a `src/main/java/br/com/athletiza/`.

Slides de referência: *Pacotes, DAO e SOLID*, *MVC* (duas aulas), *Exceções em Java* e *Tratamento de Exceções*.

## 1. Pacotes

| Slide | No código |
|---|---|
| Pacotes organizam as classes ("pastas lógicas") | `model`, `view`, `controller`, `dao`, `exception`, `util` |
| `model` → classes de domínio, `dao` → acesso a dados | `model/Atleta`, `dao/AtletaDAO` |
| MVC em pacotes separados (`model`, `view`, `controller`) | `view` ainda tem um subpacote por módulo (`view/atletas`, `view/gestao`...) |

## 2. DAO e Inversão de Dependência (DIP)

| Slide | No código |
|---|---|
| DAO separa o acesso a dados da lógica de negócio | Todo SQL fica em `dao/`; controllers e telas não têm SQL |
| `AlunoDAO` com `salvar`, `listar`, `buscarPorId` | `GenericDAO`: `inserir`, `atualizar`, `excluir`, `buscarPorId` (retorna `null` se não achar), `listarTodos` |
| Abstração: `interface AlunoRepository` | `dao/ModalidadeRepository`, `dao/AtletaRepository`... (uma por entidade) |
| `AlunoDAO implements AlunoRepository` | `ModalidadeDAO extends AbstractDAO<Modalidade> implements ModalidadeRepository` |
| Problema sem DIP: `private AlunoDAO dao = new AlunoDAO();` | Nenhum controller faz `new ...DAO()`; só o `controller/PrincipalController` cria os DAOs |
| `Sistema(AlunoRepository repository)`: recebe a abstração no construtor | `CadastroModalidadeController(view, model, ModalidadeRepository repository, navegador)` |
| Teste com `AlunoRepositoryFake` | `src/test/.../CadastroModalidadeControllerTest`: `ModalidadeRepositoryFake` guarda tudo em uma lista, sem banco |

## 3. MVC

Exemplo da aula: `Paciente` (Model), `PacienteView` (View) e `PacienteController`. Equivalente no sistema:
`Modalidade`, `PainelCadastroModalidade` e `CadastroModalidadeController`.

| Regra do slide | No código |
|---|---|
| **Model**: dados e regras de negócio (`isValido()`) | `Modalidade.validar()`, `Atleta.validar()`, `Gestao.sobrepoe(...)`, `Cargo.verificarHierarquia()`, `Competicao.inscreverAtleta(...)` |
| **Model** não conhece a interface gráfica | Nenhuma classe de `model` importa `view`, `controller` ou `dao` |
| Listagens usam `List<T>` | `repository.listarTodos()` devolve `List<Modalidade>`; a tabela recebe `view.exibir(lista)` |
| **View**: inicializa componentes | Construtor chama `inicializarComponentes()` |
| **View**: captura os dados | `getNome()`, `getGenero()`, `getMatricula()`... |
| **View**: exibe mensagens | `mostrarMensagem(...)`, `mostrarErro(...)`, `confirmar(...)` (em `PainelFormulario` e `PainelConsulta`) |
| **View**: limpa campos | `PainelCadastroModalidade.limparTela()` |
| **View**: adiciona ações aos botões (lambda) | `adicionarAcaoSalvar(ActionListener)`, `adicionarAcaoIncluir(...)`, `getBtnSalvar()` |
| **View** não contém regra de negócio | Telas só montam componentes e repassam o que foi digitado |
| **Controller**: possui View e Model como atributos, recebidos no construtor | `private final PainelCadastroModalidade view; private final Modalidade model;` |
| **Controller**: adiciona as ações dos botões | `adicionarAcoesBotoes() { view.adicionarAcaoSalvar(e -> salvar()); ... }` |
| **Controller**: cada ação chama um único fluxo | `salvar()`, `cancelar()`, `excluirSelecionada()`... |
| **Controller** não armazena dados | O que é gravado fica no Model e no banco; o controller só orquestra |
| Controller testado com "mock" da View | `CadastroModalidadeControllerTest.ViewFalsa` guarda as mensagens em vez de abrir janelas e clica em `getBtnSalvar().doClick()` |

Fluxo de um botão (igual ao "Fluxo MVC na prática" do slide):

1. Usuário clica em **Salvar** na View.
2. A View dispara o evento (lambda registrada pelo Controller).
3. O Controller lê os campos (`view.getNome()`) e passa para o Model.
4. O Model valida (`model.validar()`).
5. O Controller grava pelo repositório e manda a View mostrar o resultado.

## 4. Exceções

| Slide | No código |
|---|---|
| Exceção personalizada **checked** (`extends Exception`) | `exception/AthletizaException` (abstrata) e as filhas `ValidacaoException`, `RegraNegocioException`, `PersistenciaException` |
| Dois construtores (padrão e com mensagem) | `new RegraNegocioException()` e `new RegraNegocioException("mensagem")`; `PersistenciaException` também guarda a causa |
| `throw` | `Modalidade.validar()` → `Validador.validar()` lança `ValidacaoException`; controllers lançam `RegraNegocioException` |
| `throws` (propagar) | `void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException` |
| `try` / `catch` | `salvar() { try { gravar(); ... } catch (AthletizaException e) { view.mostrarErro(e); } }` |
| Ordem dos `catch` (mais específica antes) | `LoginController.entrar()`: `catch (ExecutionException e)` e depois `catch (InterruptedException e)` |
| `finally` | `CadastroUsuarioController.salvar()` e `AlterarSenhaController.executar()` apagam a senha da memória mesmo se der erro |
| try-with-resources | `DAOBase`: `try (Connection con = ...; PreparedStatement ps = ...)` |
| Multi-catch | `PainelRegistros.SoltarArquivos`: `catch (UnsupportedFlavorException \| IOException e)` |
| Relançar a exceção | `DAOBase.emTransacao()`: `rollback()` e `throw e`; `GerenciarGestaoController.adicionarMembro()` desfaz na memória e relança |
| Propagar até um tratador de alto nível | Erros inesperados chegam ao tratador global registrado em `Athletiza.main()` |
| Não ignorar exceções; registrar em log | `DAOBase.traduzir()` grava o erro técnico no log e devolve mensagem amigável; `util/Log` guarda o stack trace em arquivo |
| Exceção não verificada (`RuntimeException`) | `Senha.calcular()` lança `IllegalStateException` (erro de programação/ambiente, não do usuário) |
| `NumberFormatException` vira erro de validação | `ResultadosController.converterColocacao()` |

## 5. O que foi simplificado

Para o código ficar no nível dos slides, estes recursos foram trocados pela forma vista em aula:

| Antes | Agora |
|---|---|
| `Optional<T>` em `buscarPorId` | retorna o objeto ou `null` |
| `stream().filter(...).toList()` | laço `for` com `if` |
| `record` | classe com atributos `private`, getters, `equals` e `hashCode` |
| Referência de método (`Atleta::getNome`) | lambda (`a -> a.getNome()`) |
| `switch` com `->` | `switch` com `case ...:` e `return`/`break` |
| `instanceof Atleta a` | `instanceof Atleta` seguido de cast `(Atleta) objeto` |
| `List.of(...)`, `Set.of(...)` | `new ArrayList<>()`, `Arrays.asList(...)` |
| Telas criando os controllers e chamando métodos deles | Controllers criam/recebem a tela e registram os botões |
