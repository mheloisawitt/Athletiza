# Athletiza

Sistema de Gerenciamento de Atlética Acadêmica, desenvolvido em Java (Swing + MVC + DAO + JDBC + PostgreSQL) para a disciplina **Desenvolvimento Orientado a Objetos II (DOO2)**.

O sistema reúne em um só lugar o calendário, as gestões, os atletas, os treinos, as competições e os eventos da atlética.

## Documentação

Em [`docs/requisitos`](docs/requisitos):

- `Requisitos_Sistema_Gerenciamento_Atletica.docx`: requisitos funcionais (RF01-RF24) e não funcionais (RN01-RN20)
- `Chamados_Sistema_Gerenciamento_Atletica.pdf`: plano de construção com 51 chamados (CH-01 a CH-51) em 11 épicos
- `prototipo_telas.webp`: protótipo das telas

Em [`docs/banco/DER.md`](docs/banco/DER.md): diagrama do banco de dados e regras de integridade.

## Pré-requisitos

- JDK 17 ou superior
- Apache NetBeans (versão recente, já vem com Maven embutido)
- Git
- PostgreSQL 14 ou superior (com pgAdmin, que já vem no instalador)

## Configurar o banco de dados

1. No pgAdmin, abra o **Query Tool** no banco `postgres` e execute
   [`src/main/resources/sql/00_criar_banco.sql`](src/main/resources/sql/00_criar_banco.sql)
2. Abra o **Query Tool** no novo banco `athletiza` e execute, nesta ordem:
   - [`01_estrutura.sql`](src/main/resources/sql/01_estrutura.sql): cria as tabelas (pode ser executado de novo para zerar o banco)
   - [`02_dados_exemplo.sql`](src/main/resources/sql/02_dados_exemplo.sql): dados de exemplo do protótipo
3. Na raiz do projeto, copie `db.properties.example` para `db.properties` e coloque a senha do seu PostgreSQL.
   Esse arquivo não vai para o Git (cada integrante tem o seu).

**Primeiro acesso:** usuário `admin`, senha `admin123` (criado pelos dados de exemplo).

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

Os testes usam um banco H2 em memória, então rodam sem PostgreSQL instalado.
Para testar contra um PostgreSQL real (use um banco separado, pois os testes apagam os dados):

```bash
mvn test -Dteste.db.url=jdbc:postgresql://localhost:5432/athletiza_teste -Dteste.db.usuario=postgres -Dteste.db.senha=SUA_SENHA
```

## Estrutura de pacotes

```
br.com.athletiza
├── Athletiza.java   classe principal
├── model            entidades do domínio (Pessoa, Atleta, Gestao, Atividade, Treino...)
├── view             telas Swing (TelaLogin, TelaPrincipal, Tema)
│   ├── componentes  peças reutilizáveis (PainelConsulta, PainelFormulario, MenuLateral, Botoes...)
│   ├── calendario   tela inicial (PainelCalendario, CelulaDia, ListaAtividades, DialogoCompromisso)
│   └── atletas      módulo Atletas (consulta e cadastro de atletas e modalidades)
├── controller       ligação entre telas e regras de negócio (LoginController, AtletaController...)
├── dao              acesso ao banco de dados (DAOBase, GenericDAO, AbstractDAO e um DAO por entidade)
├── util             utilitários (ConnectionFactory, Validador, Senha, Sessao, Cores)
└── exception        exceções personalizadas (Validacao, RegraNegocio, Persistencia)
```

Principais classes do modelo:

- `Pessoa` (abstrata) → `Membro`, `Atleta`, `Usuario`
- `Atividade` (abstrata, tudo que aparece no calendário) → `Treino` → `Amistoso`; `Competicao`; `Evento`; `Compromisso`
- `Gestao`, `Cargo`, `MembroCargo`, `Modalidade`, `Resultado`, `Tarefa`, `Registro`

## Andamento dos chamados

| Chamado | Descrição | Situação |
|---|---|---|
| CH-02 | Projeto Java, repositório e pacotes | Concluído |
| CH-04 | DER e scripts SQL | Concluído |
| CH-05 | Conexão com o banco (JDBC) | Concluído |
| CH-06 | GenericDAO, AbstractDAO e DAO de exemplo (`ModalidadeDAO`) | Concluído |
| CH-07 | Hierarquia de pessoas | Concluído |
| CH-08 | Hierarquia de atividades do calendário | Concluído |
| CH-09 | Demais entidades com List, Map e Set | Concluído (diagrama de classes pendente) |
| CH-10 | Exceções personalizadas e Validador | Concluído |
| CH-11 | Comparable e Comparators | Concluído |
| CH-03 | Tema escuro, botões, PainelConsulta e PainelFormulario reutilizáveis | Concluído |
| CH-12 | Tela de login com senha em hash (PBKDF2) | Concluído |
| CH-13 | Perfis de acesso | Parcial (botões desabilitados para o perfil Consulta) |
| CH-14 | Tela principal com menu lateral (CardLayout) | Concluído |
| CH-46 | Mensagens padronizadas e tratamento global de erros | Concluído |
| CH-01 | Validar requisitos com a diretoria | Pendente (grupo) |
| CH-15 | Calendário mensal com atividades coloridas por tipo | Concluído |
| CH-16 | Incluir, editar e excluir compromissos pelo calendário | Concluído |
| CH-17 | Painel de próximos eventos (7, 15 ou 30 dias) | Concluído |
| CH-24 | AtletaDAO (com transação), AtletaController e ModalidadeController | Concluído |
| CH-25 | Consulta de atletas com filtros e ordenação por Comparator | Concluído |
| CH-26 | Cadastro de atleta com várias modalidades e situação em cada uma | Concluído |
| CH-27 | Consulta e cadastro de modalidades (exclusão bloqueada se houver vínculos) | Concluído |
| CH-18 | Módulo Gestão (backend) | Próximo |

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
