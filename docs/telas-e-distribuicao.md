# Telas do sistema e distribuição entre os integrantes (CH-47)

Regra da disciplina: **cada integrante fica com pelo menos 2 telas não triviais e pelo menos 1 tela que usa o banco
de dados**, e nenhuma tela contada pode ter apenas 3 componentes. Cada integrante também apresenta
**pelo menos 2 diagramas de sequência** ([`diagramas/sequencia.md`](diagramas/sequencia.md)).

Caminhos relativos a `src/main/java/br/com/athletiza/view/`.

## 1. Todas as telas

**Complexidade:** *Alta* = várias áreas, tabelas editáveis ou desenho próprio; *Média* = consulta ou cadastro completo;
*Simples* = poucos campos (não usar como uma das 2 telas obrigatórias, apenas como complemento).
Todas as telas abaixo leem ou gravam no banco, exceto onde indicado.

| # | Tela | Classe | Tipo | Complexidade | O que tem |
|---|---|---|---|---|---|
| 1 | Login | `TelaLogin` | Janela | Média | Usuário, senha com botão de mostrar, lembrar de mim, SwingWorker, troca obrigatória de senha provisória |
| 2 | Início (calendário) | `calendario/PainelCalendario` (+ `CelulaDia`, `PainelSemana`, `ListaAtividades`, `PainelAlertas`) | Painel | Alta | Visão por mês e por semana, cores por tipo, alertas, próximos eventos, abrir atividades |
| 3 | Compromisso | `calendario/DialogoCompromisso` | Diálogo | Média | Título, data, horário, local, observações |
| 4 | Consulta de gestões | `gestao/PainelConsultaGestoes` | Consulta | Média | JTable, busca, ordenação por período, exclusão com confirmação da composição |
| 5 | Cadastro de gestão | `gestao/PainelCadastroGestao` | Cadastro | Média | Nome, período (sem sobreposição), situação, descrição |
| 6 | Gerenciar gestão | `gestao/PainelGerenciarGestao` | Consulta | Alta | Adicionar, trocar e remover membros dos cargos (Set impede repetição) |
| 7 | Organograma | `gestao/PainelOrganograma` | Desenho | Alta | Hierarquia de cargos desenhada com Graphics2D |
| 8 | Consulta de cargos | `gestao/PainelConsultaCargos` | Consulta | Média | Nível e cargo superior |
| 9 | Cadastro de cargo | `gestao/PainelCadastroCargo` | Cadastro | Simples | Nome, nível (JSpinner), superior |
| 10 | Consulta de membros | `gestao/PainelConsultaMembros` | Consulta | Média | Exclusão bloqueada com vínculos |
| 11 | Cadastro de membro | `gestao/PainelCadastroMembro` | Cadastro | Simples | Nome, matrícula, contato, situação |
| 12 | Consulta de atletas | `atletas/PainelConsultaAtletas` | Consulta | Alta | Filtros por modalidade e situação, ordenação por Comparator escolhida no combo |
| 13 | Cadastro de atleta | `atletas/PainelCadastroAtleta` | Cadastro | Alta | Tabela editável de modalidades (checkbox + combo de situação), gravação em transação |
| 14 | Consulta de modalidades | `atletas/PainelConsultaModalidades` | Consulta | Média | Exclusão bloqueada com vínculos |
| 15 | Cadastro de modalidade | `atletas/PainelCadastroModalidade` | Cadastro | Simples | Nome e gênero |
| 16 | Consulta de competições | `competicoes/PainelConsultaCompeticoes` | Consulta | Média | Filtros de situação e período, melhor resultado, resumo de tarefas |
| 17 | Cadastro de competição | `competicoes/PainelCadastroCompeticao` | Cadastro | Média | Nome, local, datas, horário, situação, observações |
| 18 | Gerenciar competição | `competicoes/PainelGerenciarCompeticao` | Painel com abas | Alta | Modalidades × atletas inscritos (Map de Set), abas de resultados e tarefas |
| 19 | Resultados | `competicoes/PainelResultados` | Consulta + diálogo | Média | Resultado da equipe ou do atleta, colocação e placar |
| 20 | Treinos | `treinos/PainelTreinos` | Consulta | Alta | Lista de modalidades à esquerda, filtros, resumo de presença |
| 21 | Cadastro de treino/amistoso | `treinos/PainelCadastroTreino` | Cadastro | Média | Campos extras do amistoso (herança), conflito de horário |
| 22 | Lista de presença | `treinos/PainelPresenca` | Cadastro | Alta | Tabela editável, marcar/desmarcar todos, contador |
| 23 | Frequência | `treinos/PainelFrequencia` | Consulta | Média | Percentual com barra colorida (renderizador próprio) |
| 24 | Histórico do atleta | `treinos/PainelHistoricoAtleta` | Consulta | Média | Treinos com presente/ausente |
| 25 | Consulta de eventos | `eventos/PainelConsultaEventos` | Consulta | Média | Filtros de tipo e período, resumo de tarefas |
| 26 | Cadastro de evento | `eventos/PainelCadastroEvento` | Cadastro | Média | Nome, tipo, data, horário, local, situação, descrição |
| 27 | Responsáveis e tarefas | `eventos/PainelGerenciarEvento` (+ `componentes/PainelTarefas`) | Painel | Alta | Responsáveis e tarefas lado a lado, atrasadas em vermelho |
| 28 | Registros (galeria) | `registros/PainelRegistros` (+ `CartaoArquivo`) | Painel | Alta | Pastas, miniaturas, JFileChooser, arrastar e soltar, menu do botão direito |
| 29 | Consulta de usuários | `usuarios/PainelConsultaUsuarios` | Consulta | Média | Só para administradores |
| 30 | Cadastro de usuário | `usuarios/PainelCadastroUsuario` | Cadastro | Média | Perfil, situação, senha e confirmação |
| 31 | Alterar senha | `usuarios/DialogoAlterarSenha` | Diálogo | Simples | Senha atual, nova e confirmação |

Infraestrutura compartilhada (não conta como tela de ninguém, mas qualquer um pode explicar):
`TelaPrincipal` (menu e CardLayout), `componentes/PainelConsulta`, `PainelFormulario`, `ModeloTabela`,
`Renderizadores`, `MenuLateral`, `Botoes`, `Combos`, `Mensagens`, `Tema`.

## 2. Sugestão de distribuição em pacotes

Cada pacote reúne telas de um mesmo módulo, já cumpre a regra (2 ou mais telas não triviais, com banco) e vem com
2 ou mais diagramas de sequência prontos. Se o grupo tiver menos integrantes que pacotes, juntem dois pacotes;
se tiver mais, dividam os pacotes maiores (ex.: o 5).

| Pacote | Telas (#) | Diagramas de sequência | Integrante |
|---|---|---|---|
| **1. Acesso e calendário** | Login (1), Início (2), Compromisso (3) | 1 Login, 2 Exibir calendário, 3 Incluir compromisso | |
| **2. Gestão** | Gestões (4, 5), Gerenciar gestão (6), Organograma (7), Cargos (8, 9), Membros (10, 11) | 4 Cadastrar gestão, 5 Adicionar membro a cargo | |
| **3. Atletas** | Atletas (12, 13), Modalidades (14, 15) | 6 Incluir atleta, 7 Excluir atleta com vínculos | |
| **4. Competições** | Competições (16, 17), Gerenciar competição (18), Resultados (19) | 8 Gerenciar competição, 9 Lançar resultado | |
| **5. Treinos** | Treinos (20), Cadastro de treino (21), Presença (22), Frequência (23), Histórico (24) | 10 Cadastrar treino, 11 Registrar presença | |
| **6. Eventos, registros e usuários** | Eventos (25, 26), Responsáveis e tarefas (27), Registros (28), Usuários (29, 30, 31) | 12 Concluir tarefa, 13 Adicionar arquivos | |

## 3. Conferência antes da apresentação

- [ ] Cada integrante tem 2 ou mais telas de complexidade Média ou Alta
- [ ] Cada integrante tem pelo menos 1 tela que usa o banco de dados
- [ ] Cada integrante tem 2 ou mais diagramas de sequência das suas telas
- [ ] Cada integrante sabe mostrar no código onde suas telas usam os requisitos técnicos
      (ver [`matriz-requisitos-tecnicos.md`](matriz-requisitos-tecnicos.md))
