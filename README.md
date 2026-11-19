# Athletiza

Sistema de Gerenciamento de Atlética Acadêmica, desenvolvido em Java (Swing + MVC + DAO + JDBC + JPA + MySQL) para a disciplina **Desenvolvimento Orientado a Objetos II (DOO2)**.

O sistema reúne em um só lugar o calendário, o dashboard com os indicadores, as gestões, os atletas, os treinos, as competições e os eventos da atlética.

## Documentação

Em [`docs/requisitos`](docs/requisitos):

- `Requisitos_Athletiza.pdf`: regras de negócio (RN01-RN42), requisitos funcionais (RF01-RF48) e não funcionais (RNF01-RNF10)
- `Chamados_Athletiza.pdf`: plano de construção com 68 chamados, em dois blocos: plano original (CH-01 a CH-50) e
  pedidos da cliente na 3ª reunião (CH-51 a CH-68)
- `Prototipos_Athletiza.pdf`: protótipo das 30 telas
- `Atas_Reunioes_Athletiza.pdf`: atas das quatro reuniões com a cliente

Documentação técnica:

- [`docs/manual.md`](docs/manual.md): manual de uso por módulo
- [`docs/diagramas/imagens`](docs/diagramas/imagens): diagramas de classes (modelo, persistência e telas), 13 de sequência e 4 de colaboração
- [`docs/matriz-requisitos-tecnicos.md`](docs/matriz-requisitos-tecnicos.md): onde cada requisito técnico está no código
- [`docs/telas-e-distribuicao.md`](docs/telas-e-distribuicao.md): lista das 31 telas e da complexidade de cada uma
- [`docs/prototipo/prototipo-telas.html`](docs/prototipo/prototipo-telas.html): protótipo navegável das 31 telas (abrir no navegador), com uma imagem de cada tela em [`docs/prototipo/telas`](docs/prototipo/telas)
- [`docs/roteiro-testes.md`](docs/roteiro-testes.md): roteiro de testes manuais por módulo, para preencher

## Pré-requisitos

- JDK 17 ou superior
- Apache NetBeans (versão recente, já vem com Maven embutido)
- Git
- MySQL Community Server 8.4 LTS ou superior e, de preferência, o MySQL Workbench

## Configurar o banco de dados

1. Ligue o MySQL (serviço **MySQL84** no aplicativo *Serviços* do Windows) e abra a conexão no MySQL Workbench.
2. Execute, nesta ordem (no Workbench, abra cada script e execute o arquivo inteiro; depois do primeiro,
   coloque `athletiza` no *Default Schema* da conexão):
   - [`00_criar_banco.sql`](src/main/resources/sql/00_criar_banco.sql): cria o banco `athletiza` (em utf8mb4) e o seleciona
   - [`01_estrutura.sql`](src/main/resources/sql/01_estrutura.sql): cria as tabelas (pode ser executado de novo para zerar o banco)
   - [`02_dados_exemplo.sql`](src/main/resources/sql/02_dados_exemplo.sql): dados de exemplo do protótipo
   - [`03_dados_dashboard.sql`](src/main/resources/sql/03_dados_dashboard.sql) (opcional, uma vez só): mais atletas,
     treinos (desde julho), presenças, eventos, resultados de competições e demandas, para os gráficos
     do **Dashboard** terem o que mostrar
   - [`04_atualizacao_requisitos.sql`](src/main/resources/sql/04_atualizacao_requisitos.sql): **só para quem já tinha
     o banco criado** antes desta versão (acrescenta quem registrou cada demanda). Quem executa o `01_estrutura.sql`
     de novo não precisa dele
3. Na raiz do projeto, copie `db.properties.example` para `db.properties` e coloque a senha do root do seu MySQL.
   Esse arquivo não vai para o Git (cada computador tem o seu).

Pelo terminal, com o `mysql` no PATH: `mysql -u root -p < src/main/resources/sql/00_criar_banco.sql`, depois
`mysql -u root -p athletiza < .../01_estrutura.sql` e o mesmo para o `02_dados_exemplo.sql` (e o `03`, se quiser).

**Log de erros:** detalhes técnicos de erros inesperados ficam em `Documentos/Athletiza-logs/athletiza-0.log`
(até 5 arquivos de 1 MB). Para usar outra pasta: `-Dathletiza.logs=CAMINHO`.

**Fotos e vídeos (Registros):** ficam em `Documentos/Athletiza-midias`, fora do projeto e do Git. Para usar outra
pasta, execute com `-Dathletiza.midias=CAMINHO` (no NetBeans: Properties > Run > VM Options).

**Primeiro acesso:** usuário `admin`, senha `admin123` (criado pelos dados de exemplo). Essa senha é provisória:
o sistema exige a troca logo no primeiro acesso. Depois, troque quando quiser clicando no seu nome, no rodapé do menu. Novos usuários são criados no menu **Usuários** (só para administradores).

## Como abrir no NetBeans

1. `Team > Git > Clone...` e informe `https://github.com/mheloisawitt/athletiza.git`
2. Ao final do clone, aceite abrir o projeto (ou use `File > Open Project` e selecione a pasta clonada)
3. Clique com o botão direito no projeto e escolha **Run** (ou F6). O arquivo `nbactions.xml` já indica a classe
   principal (`br.com.athletiza.app.Athletiza`); se aparecer `Could not find or load main class ${packageClassName}`,
   atualize o projeto (Pull) ou escolha a classe em *Properties > Run > Main Class*

Pela linha de comando:

```bash
mvn package
java -jar target/Athletiza-1.0-SNAPSHOT.jar
```

## Testes

No NetBeans: botão direito no projeto > **Test** (ou Alt+F6). Pela linha de comando: `mvn test`.

Os testes usam um banco H2 em memória (modo MySQL), então rodam sem MySQL instalado.
Para testar contra um MySQL real, crie antes um banco separado (`CREATE DATABASE athletiza_teste;`), pois os testes apagam os dados:

```bash
mvn test -Dteste.db.url=jdbc:mysql://localhost:3306/athletiza_teste -Dteste.db.usuario=root -Dteste.db.senha=SUA_SENHA
```

## Estrutura de pacotes

```
br.com.athletiza
├── app              classe principal (Athletiza)
├── modelo           entidades do domínio (Pessoa, Atleta, Gestao, Atividade, Treino...)
├── visao            telas Swing (TelaLogin, TelaPrincipal, Tema)
│   ├── componentes  peças reutilizáveis (PainelConsulta, PainelFormulario, MenuLateral, Botoes...)
│   ├── calendario   tela inicial (PainelCalendario, CelulaDia, ListaAtividades, DialogoCompromisso)
│   ├── dashboard    indicadores em três abas: barras, linhas, colunas, pizza/rosca e linha do tempo
│   ├── atletas      módulo Atletas (consulta e cadastro de atletas e modalidades)
│   ├── gestao       módulo Gestão (gestões, composição, organograma, cargos e membros)
│   ├── competicoes  módulo Competições (consulta, cadastro, inscrições e resultados)
│   ├── treinos      módulo Treinos (consulta, cadastro, presença e frequência)
│   ├── eventos      módulo Eventos (consulta, cadastro, responsáveis e tarefas)
│   ├── usuarios     usuários do sistema e troca de senha
│   └── registros    galeria de fotos e vídeos em pastas
├── controlador      recebem a visão e o repositório, registram as ações dos botões e aplicam as regras
├── repositorio      interfaces de acesso aos dados (Repositorio e uma por entidade)
├── dao              implementações dos repositórios com JDBC e JPA (DAOBase, AbstractDAO e um DAO por entidade)
├── util             utilitários (ConnectionFactory, Validador, Senha, Sessao, Cores, ArmazenamentoMidia, Log)
└── excecao          exceções personalizadas (Validacao, RegraNegocio, Persistencia)
```

Fluxo de cada tela: a `TelaPrincipal` (menu) cria a visão e o DAO e entrega os dois ao controlador
(`new AtletaControlador(visao, new AtletaDAO(), new ModalidadeDAO())`). O controlador, no `initTela()`,
registra as ações dos botões (`visao.adicionarAcaoBtnIncluir(e -> ...)`), busca os dados pelo repositório e
chama `visao.apresentarDados(...)`, `visao.apresentarMensagem(...)` e `visao.apresentarTela()`. A visão só
mostra os dados e devolve o que foi digitado pelos getters (`getNome()`, `getDataNascimento()`...).

Principais classes do modelo:

- `Pessoa` (abstrata) → `Membro`, `Atleta`, `Usuario`
- `Atividade` (abstrata, tudo que aparece no calendário) → `Treino` → `Amistoso`; `Competicao`; `Evento`; `Compromisso`
- `Gestao`, `Cargo`, `MembroCargo`, `Modalidade`, `Resultado`, `Tarefa`, `Pasta`, `Arquivo`

## Andamento dos chamados

| Chamado | Descrição | Situação |
|---|---|---|
| CH-02 | Projeto Java, repositório e pacotes | Concluído |
| CH-04 | DER e scripts SQL | Concluído |
| CH-05 | Conexão com o banco (JDBC) | Concluído |
| CH-05 | Troca do PostgreSQL pelo MySQL 8.4 (scripts, driver, mensagens de erro e testes) | Concluído |
| CH-06 | Repositorio, AbstractDAO (JDBC) e `ModalidadeDAO` com JPA | Concluído |
| CH-07 | Hierarquia de pessoas | Concluído |
| CH-08 | Hierarquia de atividades do calendário | Concluído |
| CH-09 | Demais entidades com List, Map e Set e diagrama de classes | Concluído |
| CH-10 | Exceções personalizadas e Validador | Concluído |
| CH-11 | Comparable e Comparators | Concluído |
| CH-03 | Tema escuro, botões, PainelConsulta e PainelFormulario reutilizáveis | Concluído |
| CH-12 | Tela de login com senha em hash (PBKDF2) | Concluído |
| CH-13 | Perfis de acesso (Usuários só para administradores; Consulta só visualiza) | Concluído |
| CH-14 | Tela principal com menu lateral (CardLayout) e ícone em cada item | Concluído |
| CH-46 | Mensagens padronizadas, tratamento global de erros e log em arquivo | Concluído |
| CH-01 | Validar requisitos com a diretoria | Concluído (atas das reuniões) |
| CH-15 | Calendário mensal com atividades coloridas por tipo | Concluído |
| CH-16 | Incluir, editar e excluir compromissos pelo calendário | Concluído |
| CH-17 | Painel de próximos eventos (7, 15 ou 30 dias) | Concluído |
| RF08 | Abrir treinos, competições e eventos a partir do calendário | Concluído |
| Extra | Visão do calendário por semana | Concluído |
| Extra | Alertas na tela inicial (tarefas atrasadas, presença pendente, competições próximas) | Concluído |
| CH-24 | AtletaDAO (com transação), AtletaControlador e ModalidadeControlador | Concluído |
| CH-25 | Consulta de atletas com filtros e ordenação por Comparator | Concluído |
| CH-26 | Cadastro de atleta com várias modalidades e situação em cada uma | Concluído |
| CH-27 | Consulta e cadastro de modalidades (exclusão bloqueada se houver vínculos) | Concluído |
| CH-18 | GestaoDAO, CargoDAO, MembroDAO e controladores (sem períodos sobrepostos) | Concluído |
| CH-19 | Consulta de gestões ordenada por período | Concluído |
| CH-20 | Cadastro de gestão | Concluído |
| CH-21 | Gerenciar gestão: adicionar, trocar e remover membros dos cargos | Concluído |
| CH-22 | Consulta e cadastro de membros (e de cargos) | Concluído |
| CH-23 | Organograma da gestão desenhado com Graphics2D | Concluído |
| CH-28 | CompeticaoDAO (inscrições em transação) e CompeticaoControlador | Concluído |
| CH-29 | Consulta de competições com filtros de situação e período | Concluído |
| CH-30 | Cadastro de competição (aparece no calendário) | Concluído |
| CH-31 | Gerenciar competição: modalidades e atletas inscritos | Concluído |
| CH-32 | Lançamento de resultados (equipe ou atleta) | Concluído |
| CH-33 | TreinoDAO (treino e amistoso na mesma tabela), presença e frequência | Concluído |
| CH-34 | Consulta de treinos com modalidades à esquerda e filtros de tipo e período | Concluído |
| CH-35 | Cadastro de treino e amistoso com verificação de conflito de horário | Concluído |
| CH-36 | Lista de presença salva em lote | Concluído |
| CH-37 | Frequência por atleta com percentual e histórico de presença do atleta (RF34) | Concluído |
| CH-38 | EventoDAO e EventoControlador (responsáveis e tarefas) | Concluído |
| CH-39 | Consulta de eventos com filtros de tipo e período e resumo de tarefas | Concluído |
| CH-40 | Cadastro de evento (aparece no calendário) | Concluído |
| CH-41 | Responsáveis e tarefas com alerta de atraso | Concluído |
| Extra | Tarefas também nas competições (painel de tarefas compartilhado) | Concluído |
| RF41 | Cadastro de usuários e troca da própria senha | Concluído |
| Extra | Senha provisória com troca obrigatória no primeiro acesso | Concluído |
| CH-47 | Lista de telas exigidas pela disciplina | Concluído |
| CH-48 | Diagramas de sequência (13 fluxos, em Mermaid e PNG) | Concluído |
| CH-49 | Roteiro de testes por módulo | Concluído |
| CH-50 | Documentação: README, manual de uso e scripts SQL | Concluído |
| Extra | Matriz de requisitos técnicos | Concluído |
| CH-60 | Registros: pastas e arquivos (PastaDAO, ArquivoDAO, GaleriaControlador) | Concluído |
| CH-61 | Tela de registros: galeria com miniaturas, envio e arrastar e soltar | Concluído |
| Dashboard | Indicadores: modalidades com mais atletas, presenças e treinos; atletas que menos participam; setores com mais demandas | Concluído |
| Dashboard | Abas Linha do tempo (próximos 30 dias, linhas e colunas por mês) e Distribuição (pizzas, roscas e pódios) | Concluído |
| CH-51 a CH-68 | Pedidos da cliente na 3ª reunião (identidade visual, perfis, Demandas, Dashboard, paginação e demais ajustes) | Concluído |

## Como criar uma tela nova

**Consulta** (lista com JTable): a visão estende `PainelConsulta<T>` e só define as colunas:

```java
public class PainelConsultaModalidades extends PainelConsulta<Modalidade> {

    public PainelConsultaModalidades(Navegador navegador) {
        super("Modalidades", new ModeloTabela<Modalidade>()
                .coluna("Modalidade", String.class, Modalidade::getNome)
                .coluna("Gênero", String.class, m -> m.getGenero().getNomeCompleto()), navegador);
    }
}
```

O controlador liga as ações e carrega os dados:

```java
public ModalidadeControlador(PainelConsultaModalidades visao, ModalidadeRepositorio repositorio) {
    this.visao = visao;
    this.repositorio = repositorio;
    initTela();
}

public void initTela() {
    visao.adicionarAcaoCarregar(() -> carregar());
    visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Modalidade()));
    visao.adicionarAcaoBtnEditar(e -> editar());
    visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
    visao.apresentarTela();
}
```

Busca, ordenação ao clicar no cabeçalho, etiquetas coloridas de situação e datas em dd/mm/aaaa já vêm prontas.

**Cadastro** (formulário): a visão estende `PainelFormulario`, adiciona os campos com `adicionarCampo(...)` e tem
getters para cada campo. O controlador registra `cadastro.adicionarAcaoBtnSalvar(e -> ...)`, monta o objeto com os
getters, valida, salva pelo repositório e chama `cadastro.apresentarMensagem(...)` e `cadastro.voltar()`.

## Convenção de branches e commits

- `main`: versão estável, sempre compilando
- Branches nomeadas apenas pela numeração sequencial com 4 dígitos, criadas a partir da `main`:
  `0001`, `0002`, `0003`...
- Antes de criar uma branch, verificar a última numeração usada e pegar a próxima
- Toda branch nova sai da `main` atualizada; ao finalizar, ela é levada para a `main` e a próxima começa
- Mensagens de commit começando pelo chamado: `CH-12: valida campos obrigatórios do login`
- Ao concluir o chamado, abrir um Pull Request para a `main`
- Antes de começar a trabalhar, sempre atualizar: `Team > Remote > Pull`

## Paleta de cores

| Cor | Hex | Uso |
|-----|-----|-----|
| Verde | `#00FF6A` | destaques, botões de ação principal |
| Roxo | `#8A2BE2` | botões secundários, eventos |
| Preto/Grafite | `#1E1E1E` | fundo |
| Cinza escuro | `#3A3A3A` | painéis, bordas |

Disponível em código na classe `br.com.athletiza.util.Cores`.
