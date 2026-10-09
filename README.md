# Athletiza

Sistema de Gerenciamento de Atlética Acadêmica, desenvolvido em Java (Swing + MVC + DAO + JDBC + MySQL) para a disciplina **Desenvolvimento Orientado a Objetos II (DOO2)**.

O sistema reúne em um só lugar o calendário, as gestões, os atletas, os treinos, as competições e os eventos da atlética.

## Documentação

Em [`docs/requisitos`](docs/requisitos):

- `Requisitos_Sistema_Gerenciamento_Atletica.docx`: requisitos funcionais (RF01-RF24) e não funcionais (RN01-RN20)
- `Chamados_Sistema_Gerenciamento_Atletica.pdf`: plano de construção com 51 chamados (CH-01 a CH-51) em 11 épicos
- `prototipo_telas.webp`: protótipo das telas

Em [`docs/banco/DER.md`](docs/banco/DER.md): diagrama do banco de dados e regras de integridade.

Para a apresentação:

- [`docs/manual.md`](docs/manual.md): manual de uso por módulo
- [`docs/diagramas/classes.md`](docs/diagramas/classes.md): diagrama de classes (modelo, persistência e telas)
- [`docs/diagramas/sequencia.md`](docs/diagramas/sequencia.md): 13 diagramas de sequência (2 ou mais por integrante)
- [`docs/diagramas/imagens`](docs/diagramas/imagens): os mesmos diagramas em PNG, prontos para os slides
- [`docs/matriz-requisitos-tecnicos.md`](docs/matriz-requisitos-tecnicos.md): onde cada requisito técnico está no código
- [`docs/telas-e-distribuicao.md`](docs/telas-e-distribuicao.md): lista das 31 telas e sugestão de divisão entre os integrantes
- [`docs/prototipo/prototipo-telas.html`](docs/prototipo/prototipo-telas.html): protótipo navegável das 31 telas (abrir no navegador), com uma imagem de cada tela em [`docs/prototipo/telas`](docs/prototipo/telas)
- [`docs/roteiro-testes.md`](docs/roteiro-testes.md): roteiro de testes manuais por módulo, para preencher

## Pré-requisitos

- JDK 17 ou superior
- Apache NetBeans (versão recente, já vem com Maven embutido)
- Git
- MySQL Community Server 8.4 LTS ou superior e, de preferência, o MySQL Workbench
  ([passo a passo da instalação no Windows](docs/banco/instalar-mysql.md))

## Configurar o banco de dados

1. Ligue o MySQL (serviço **MySQL84** no aplicativo *Serviços* do Windows) e abra a conexão no MySQL Workbench.
2. Execute, nesta ordem (no Workbench, abra cada script e execute o arquivo inteiro; depois do primeiro,
   coloque `athletiza` no *Default Schema* da conexão):
   - [`00_criar_banco.sql`](src/main/resources/sql/00_criar_banco.sql): cria o banco `athletiza` (em utf8mb4) e o seleciona
   - [`01_estrutura.sql`](src/main/resources/sql/01_estrutura.sql): cria as tabelas (pode ser executado de novo para zerar o banco)
   - [`02_dados_exemplo.sql`](src/main/resources/sql/02_dados_exemplo.sql): dados de exemplo do protótipo
3. Na raiz do projeto, copie `db.properties.example` para `db.properties` e coloque a senha do root do seu MySQL.
   Esse arquivo não vai para o Git (cada integrante tem o seu).

Pelo terminal, com o `mysql` no PATH: `mysql -u root -p < src/main/resources/sql/00_criar_banco.sql`, depois
`mysql -u root -p athletiza < .../01_estrutura.sql` e o mesmo para o `02_dados_exemplo.sql`.

**Log de erros:** detalhes técnicos de erros inesperados ficam em `Documentos/Athletiza-logs/athletiza-0.log`
(até 5 arquivos de 1 MB). Para usar outra pasta: `-Dathletiza.logs=CAMINHO`.

**Fotos e vídeos (Registros):** ficam em `Documentos/Athletiza-midias`, fora do projeto e do Git. Para usar outra
pasta, execute com `-Dathletiza.midias=CAMINHO` (no NetBeans: Properties > Run > VM Options).

**Primeiro acesso:** usuário `admin`, senha `admin123` (criado pelos dados de exemplo). Essa senha é provisória:
o sistema exige a troca logo no primeiro acesso. Depois, troque quando quiser clicando no seu nome, no rodapé do menu. Novos usuários são criados no menu **Usuários** (só para administradores).

## Como abrir no NetBeans

1. `Team > Git > Clone...` e informe `https://github.com/mheloisawitt/athletiza.git`
2. Ao final do clone, aceite abrir o projeto (ou use `File > Open Project` e selecione a pasta clonada)
3. Clique com o botão direito no projeto e escolha **Run** (ou F6)

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
├── Athletiza.java   classe principal
├── model            entidades do domínio (Pessoa, Atleta, Gestao, Atividade, Treino...)
├── view             telas Swing (TelaLogin, TelaPrincipal, Tema)
│   ├── componentes  peças reutilizáveis (PainelConsulta, PainelFormulario, MenuLateral, Botoes...)
│   ├── calendario   tela inicial (PainelCalendario, CelulaDia, ListaAtividades, DialogoCompromisso)
│   ├── atletas      módulo Atletas (consulta e cadastro de atletas e modalidades)
│   ├── gestao       módulo Gestão (gestões, composição, organograma, cargos e membros)
│   ├── competicoes  módulo Competições (consulta, cadastro, inscrições e resultados)
│   ├── treinos      módulo Treinos (consulta, cadastro, presença e frequência)
│   ├── eventos      módulo Eventos (consulta, cadastro, responsáveis e tarefas)
│   ├── usuarios     usuários do sistema e troca de senha
│   └── registros    galeria de fotos e vídeos em pastas
├── controller       ligação entre telas e regras de negócio (LoginController, AtletaController...)
├── dao              acesso ao banco de dados (DAOBase, GenericDAO, AbstractDAO e um DAO por entidade)
├── util             utilitários (ConnectionFactory, Validador, Senha, Sessao, Cores, ArmazenamentoMidia, Log)
└── exception        exceções personalizadas (Validacao, RegraNegocio, Persistencia)
```

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
| CH-06 | GenericDAO, AbstractDAO e DAO de exemplo (`ModalidadeDAO`) | Concluído |
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
| CH-01 | Validar requisitos com a diretoria | Pendente (grupo) |
| CH-15 | Calendário mensal com atividades coloridas por tipo | Concluído |
| CH-16 | Incluir, editar e excluir compromissos pelo calendário | Concluído |
| CH-17 | Painel de próximos eventos (7, 15 ou 30 dias) | Concluído |
| RF09 | Abrir treinos, competições e eventos a partir do calendário | Concluído |
| RF10 | Visão do calendário por semana | Concluído |
| RF24 | Alertas na tela inicial (tarefas atrasadas, presença pendente, competições próximas) | Concluído |
| CH-24 | AtletaDAO (com transação), AtletaController e ModalidadeController | Concluído |
| CH-25 | Consulta de atletas com filtros e ordenação por Comparator | Concluído |
| CH-26 | Cadastro de atleta com várias modalidades e situação em cada uma | Concluído |
| CH-27 | Consulta e cadastro de modalidades (exclusão bloqueada se houver vínculos) | Concluído |
| CH-18 | GestaoDAO, CargoDAO, MembroDAO e controllers (sem períodos sobrepostos) | Concluído |
| CH-19 | Consulta de gestões ordenada por período | Concluído |
| CH-20 | Cadastro de gestão | Concluído |
| CH-21 | Gerenciar gestão: adicionar, trocar e remover membros dos cargos | Concluído |
| CH-22 | Consulta e cadastro de membros (e de cargos) | Concluído |
| CH-23 | Organograma da gestão desenhado com Graphics2D | Concluído |
| CH-28 | CompeticaoDAO (inscrições em transação) e CompeticaoController | Concluído |
| CH-29 | Consulta de competições com filtros de situação e período | Concluído |
| CH-30 | Cadastro de competição (aparece no calendário) | Concluído |
| CH-31 | Gerenciar competição: modalidades e atletas inscritos | Concluído |
| CH-32 | Lançamento de resultados (equipe ou atleta) | Concluído |
| CH-33 | TreinoDAO (treino e amistoso na mesma tabela), presença e frequência | Concluído |
| CH-34 | Consulta de treinos com modalidades à esquerda e filtros de tipo e período | Concluído |
| CH-35 | Cadastro de treino e amistoso com verificação de conflito de horário | Concluído |
| CH-36 | Lista de presença salva em lote | Concluído |
| CH-37 | Frequência por atleta com percentual e histórico de presença do atleta (RF13) | Concluído |
| CH-38 | EventoDAO e EventoController (responsáveis e tarefas) | Concluído |
| CH-39 | Consulta de eventos com filtros de tipo e período e resumo de tarefas | Concluído |
| CH-40 | Cadastro de evento (aparece no calendário) | Concluído |
| CH-41 | Responsáveis e tarefas com alerta de atraso | Concluído |
| RF19 | Tarefas também nas competições (painel de tarefas compartilhado) | Concluído |
| RF01 | Cadastro de usuários e troca da própria senha | Concluído |
| RN19 | Senha provisória com troca obrigatória no primeiro acesso | Concluído |
| CH-47 | Lista de telas e distribuição entre os integrantes | Concluído (nomes a cargo do grupo) |
| CH-48 | Diagramas de sequência (13 fluxos, em Mermaid e PNG) | Concluído |
| CH-49 | Roteiro de testes por módulo | Concluído (execução a cargo do grupo) |
| CH-50 | Documentação: README, manual de uso e scripts SQL | Concluído |
| CH-51 | Matriz de requisitos técnicos para a apresentação | Concluído (slides a cargo do grupo) |
| CH-42 | Registros: pastas e arquivos (PastaDAO, ArquivoDAO, GaleriaController) | Concluído |
| CH-43 | Tela de registros: galeria com miniaturas, envio e arrastar e soltar | Concluído |
| CH-01 | Validar requisitos com a diretoria | Registros definido como galeria de fotos e vídeos |

## Como criar uma tela nova

**Consulta** (lista com JTable): estenda `PainelConsulta<T>`, defina as colunas e implemente `buscarDados()`:

```java
public class PainelConsultaGestao extends PainelConsulta<Gestao> {

    public PainelConsultaGestao() {
        super("Gestões", new ModeloTabela<Gestao>()
                .coluna("Gestão", String.class, Gestao::getNome)
                .coluna("Período", String.class, Gestao::getPeriodo)
                .coluna("Situação", SituacaoGestao.class, Gestao::getSituacao),
                EnumSet.of(Acao.INCLUIR, Acao.GERENCIAR));
    }

    @Override
    protected List<Gestao> buscarDados() throws AthletizaException {
        return controller.listar();
    }
}
```

Busca, ordenação ao clicar no cabeçalho, etiquetas coloridas de situação e datas em dd/mm/aaaa já vêm prontas.

**Cadastro** (formulário): estenda `PainelFormulario`, adicione os campos com `adicionarCampo(...)` e implemente `salvar()`.
Os botões Salvar e Cancelar, as mensagens de erro e a volta para a consulta já vêm prontos.

## Convenção de branches e commits

- `main`: versão estável, sempre compilando
- Branches nomeadas apenas pela numeração sequencial com 4 dígitos, criadas a partir da `main`:
  `0001`, `0002`, `0003`...
- Antes de criar uma branch, verificar a última numeração usada e pegar a próxima
- Toda branch nova sai da `main` atualizada; ao finalizar, ela é levada para a `main` e a próxima começa
- Mensagens de commit começando pelo chamado: `CH-12: valida campos obrigatórios do login`
- Ao concluir o chamado, abrir um Pull Request para a `main` e pedir revisão de outro integrante
- Antes de começar a trabalhar, sempre atualizar: `Team > Remote > Pull`

## Paleta de cores

| Cor | Hex | Uso |
|-----|-----|-----|
| Verde | `#00FF6A` | destaques, botões de ação principal |
| Roxo | `#8A2BE2` | botões secundários, eventos |
| Preto/Grafite | `#1E1E1E` | fundo |
| Cinza escuro | `#3A3A3A` | painéis, bordas |

Disponível em código na classe `br.com.athletiza.util.Cores`.
