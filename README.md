# Athletiza

Sistema de Gerenciamento de Atlética Acadêmica, desenvolvido em Java (Swing + MVC + DAO + JDBC) para a disciplina **Desenvolvimento Orientado a Objetos II (DOO2)**.

O sistema reúne em um só lugar o calendário, as gestões, os atletas, os treinos, as competições e os eventos da atlética.

## Documentação

Em [`docs/requisitos`](docs/requisitos):

- `Requisitos_Sistema_Gerenciamento_Atletica.docx`: requisitos funcionais (RF01-RF24) e não funcionais (RN01-RN20)
- `Chamados_Sistema_Gerenciamento_Atletica.pdf`: plano de construção com 51 chamados (CH-01 a CH-51) em 11 épicos
- `prototipo_telas.webp`: protótipo das telas

## Pré-requisitos

- JDK 17 ou superior
- Apache NetBeans (versão recente, já vem com Maven embutido)
- Git

## Como abrir no NetBeans

1. `Team > Git > Clone...` e informe `https://github.com/mheloisawitt/athletiza.git`
2. Ao final do clone, aceite abrir o projeto (ou use `File > Open Project` e selecione a pasta clonada)
3. Clique com o botão direito no projeto e escolha **Run** (ou F6)

Pela linha de comando:

```bash
mvn package
java -jar target/Athletiza-1.0-SNAPSHOT.jar
```

## Estrutura de pacotes

```
br.com.athletiza
├── Athletiza.java   classe principal
├── model            entidades do domínio (Pessoa, Atleta, Gestao, Treino...)
├── view             telas Swing
├── controller       ligação entre telas e regras de negócio
├── dao              acesso ao banco de dados (padrão DAO)
├── util             utilitários (conexão, cores, validação, mensagens)
└── exception        exceções personalizadas
```

## Convenção de branches e commits

- `main`: versão estável, sempre compilando
- Uma branch por chamado, criada a partir da `main`: `CH-XX-descricao-curta`
  (ex.: `CH-12-tela-login`, `CH-25-consulta-atletas`)
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
