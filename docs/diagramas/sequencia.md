# Diagramas de sequência (CH-48)

Cada diagrama mostra **View → Controller → DAO → Banco**, com o fluxo principal e o fluxo de erro
(bloco `alt`), como pede o CH-48. São 12 fluxos, para que cada integrante escolha pelo menos 2.

Para exportar como imagem para os slides: cole o código de um diagrama em <https://mermaid.live>
e use **Actions > PNG** (ou SVG).

| # | Fluxo | Telas / classes principais |
|---|---|---|
| 1 | Login | `TelaLogin`, `LoginController`, `UsuarioDAO` |
| 2 | Exibir calendário | `PainelCalendario`, `CalendarioController`, `AtividadeDAO` |
| 3 | Incluir compromisso | `DialogoCompromisso`, `CalendarioController`, `CompromissoDAO` |
| 4 | Cadastrar gestão | `PainelCadastroGestao`, `GestaoController`, `GestaoDAO` |
| 5 | Adicionar membro a um cargo | `PainelGerenciarGestao`, `GestaoController`, `GestaoDAO` |
| 6 | Incluir atleta | `PainelCadastroAtleta`, `AtletaController`, `AtletaDAO` |
| 7 | Excluir atleta com vínculos | `PainelConsultaAtletas`, `AtletaController`, `AtletaDAO` |
| 8 | Gerenciar competição (inscrições) | `PainelGerenciarCompeticao`, `CompeticaoController`, `CompeticaoDAO` |
| 9 | Lançar resultado | `PainelResultados`, `CompeticaoController`, `CompeticaoDAO` |
| 10 | Cadastrar treino (conflito de horário) | `PainelCadastroTreino`, `TreinoController`, `TreinoDAO` |
| 11 | Registrar presença | `PainelPresenca`, `TreinoController`, `TreinoDAO` |
| 12 | Concluir tarefa de evento | `PainelGerenciarEvento`, `EventoController`, `EventoDAO` |

---

## 1. Login

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as TelaLogin
    participant C as LoginController
    participant D as UsuarioDAO
    participant B as Banco

    U->>V: digita usuário e senha, Enter
    V->>V: SwingWorker (não trava a tela)
    V->>C: autenticar(login, senha)
    C->>C: Validador.obrigatorio(...).validar()
    alt campos vazios
        C-->>V: ValidacaoException
        V-->>U: Mensagens.erro("O campo Usuário é obrigatório")
    end
    C->>D: buscarPorLogin(login)
    D->>B: SELECT * FROM usuario WHERE LOWER(login) = LOWER(?)
    B-->>D: linha do usuário
    D-->>C: Optional<Usuario>
    C->>C: Senha.conferir(senha, hash)
    alt usuário não existe ou senha errada
        C-->>V: RegraNegocioException
        V-->>U: "Usuário ou senha inválidos."
    else sucesso
        C->>C: Sessao.iniciar(usuario)
        C-->>V: Usuario
        V->>V: abre a TelaPrincipal e fecha o login
    end
```

## 2. Exibir calendário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant T as TelaPrincipal
    participant V as PainelCalendario
    participant C as CalendarioController
    participant D as AtividadeDAO
    participant B as Banco

    U->>T: clica em "Início"
    T->>V: carregar()
    V->>C: atividadesDoMes(YearMonth.now())
    C->>D: listarPorPeriodo(1º dia, último dia)
    D->>B: SELECT treinos / competições / eventos / compromissos do período
    B-->>D: linhas
    D-->>C: List<Atividade> (Treino, Amistoso, Competicao, Evento, Compromisso)
    loop cada atividade e cada dia do mês
        C->>C: atividade.ocorreEm(dia) (polimorfismo: Competicao cobre vários dias)
    end
    C-->>V: Map<LocalDate, List<Atividade>> (TreeMap, listas ordenadas)
    V->>V: CelulaDia.atualizar(...) com getCorHex() e getRotuloCurto()
    alt banco fora do ar
        D-->>C: PersistenciaException
        C-->>V: PersistenciaException
        V-->>U: Mensagens.erro("Não foi possível conectar ao banco de dados...")
    end
```

## 3. Incluir compromisso pelo calendário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCalendario
    participant F as DialogoCompromisso
    participant C as CalendarioController
    participant D as CompromissoDAO
    participant B as Banco

    U->>V: clique duplo no dia
    V->>F: DialogoCompromisso.abrir(compromisso com a data do dia)
    U->>F: preenche título e horário, Salvar
    F->>F: Validador.converterData / converterHorario
    F->>C: salvarCompromisso(compromisso)
    C->>C: Validador (título e data obrigatórios)
    alt título vazio
        C-->>F: ValidacaoException
        F-->>U: Mensagens.erro (diálogo continua aberto)
    else válido
        C->>D: inserir(compromisso)
        D->>B: INSERT INTO compromisso ...
        B-->>D: id gerado
        F-->>U: "Compromisso salvo com sucesso."
        F-->>V: true
        V->>V: carregar() (atualiza o mês)
    end
```

## 4. Cadastrar gestão

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroGestao
    participant C as GestaoController
    participant D as GestaoDAO
    participant B as Banco

    U->>V: preenche nome e período, Salvar
    V->>V: Validador.converterData(início / fim)
    V->>C: salvar(gestao)
    C->>C: Validador (obrigatórios, fim >= início)
    C->>D: listarTodos()
    D->>B: SELECT * FROM gestao ORDER BY data_inicio
    B-->>D: gestões
    D-->>C: List<Gestao>
    C->>C: outras.stream().filter(gestao::sobrepoe)
    alt período sobreposto
        C-->>V: RegraNegocioException("...se sobrepõe à Gestão 2025-2026...")
        V-->>U: Mensagens.erro
    else período livre
        C->>D: inserir(gestao)
        D->>B: INSERT INTO gestao ...
        V-->>U: "Gestão salva com sucesso."
    end
```

## 5. Adicionar membro a um cargo da gestão

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelGerenciarGestao
    participant C as GestaoController
    participant M as Gestao (modelo)
    participant D as GestaoDAO
    participant B as Banco

    U->>V: "+ Adicionar membro", escolhe cargo e membro
    V->>C: adicionarMembro(gestao, membro, cargo)
    C->>M: adicionarMembro(membro, cargo)
    M->>M: composicao.add(new MembroCargo(...)) (Set)
    alt já ocupa o cargo (add retorna false)
        M-->>C: false
        C-->>V: RegraNegocioException("...já ocupa o cargo...")
        V-->>U: Mensagens.erro
    else novo vínculo
        M-->>C: true
        C->>D: adicionarMembro(gestao, membro, cargo)
        D->>B: INSERT INTO gestao_membro_cargo ...
        V->>V: carregar() (tabela e organograma atualizados)
    end
```

## 6. Incluir atleta

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroAtleta
    participant C as AtletaController
    participant D as AtletaDAO
    participant B as Banco

    U->>V: preenche dados, marca modalidades, Salvar
    V->>V: atleta.vincularModalidade(m, situação) para cada marcada (Map)
    V->>C: salvar(atleta)
    C->>C: Validador (nome, matrícula numérica, contato, ao menos 1 modalidade)
    C->>D: existeMatricula(matricula, id)
    D->>B: SELECT COUNT(*) FROM atleta WHERE matricula = ?
    alt matrícula já cadastrada
        C-->>V: RegraNegocioException
        V-->>U: "Já existe um atleta com a matrícula 202001."
    else nova
        C->>D: inserir(atleta)
        D->>D: emTransacao(...)
        D->>B: INSERT INTO atleta ...
        D->>B: INSERT INTO atleta_modalidade ... (uma por modalidade)
        alt erro em algum INSERT
            D->>B: ROLLBACK (nada é gravado)
            D-->>V: PersistenciaException
        else
            D->>B: COMMIT
            V-->>U: "Atleta salvo com sucesso."
        end
    end
```

## 7. Excluir atleta com vínculos

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelConsultaAtletas
    participant C as AtletaController
    participant D as AtletaDAO
    participant B as Banco

    U->>V: seleciona atleta, Excluir
    V-->>U: Mensagens.confirmar("Deseja realmente excluir...?")
    U->>V: Sim
    V->>C: excluir(atleta)
    C->>D: listarVinculos(id)
    D->>B: SELECT COUNT(*) em presenca_treino, competicao_atleta, resultado
    D-->>C: ["1 registro(s) de presença...", "1 inscrição(ões)..."]
    alt possui vínculos
        C-->>V: RegraNegocioException(lista dos vínculos)
        V-->>U: "Não é possível excluir Ana Souza, pois possui: ..."
    else sem vínculos
        C->>D: excluir(id)
        D->>B: DELETE FROM atleta WHERE id = ? (modalidades em cascata)
        V-->>U: "Atleta excluído."
    end
    V->>V: carregar()
```

## 8. Gerenciar competição (inscrições)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelGerenciarCompeticao
    participant M as Competicao (modelo)
    participant C as CompeticaoController
    participant D as CompeticaoDAO
    participant B as Banco

    V->>C: carregarInscricoes(competicao)
    C->>D: carregarInscricoes(competicao)
    D->>B: SELECT competicao_modalidade / competicao_atleta
    D-->>V: Map<Modalidade, Set<Atleta>> preenchido
    U->>V: marca modalidade e atletas
    V->>M: adicionarModalidade(m) / inscreverAtleta(m, atleta)
    alt atleta não pratica a modalidade
        M-->>V: RegraNegocioException
        V-->>U: Mensagens.erro
    end
    U->>V: Salvar inscrições
    V->>C: salvarInscricoes(competicao)
    C->>D: salvarInscricoes(competicao)
    D->>D: emTransacao(...)
    D->>B: DELETE modalidades retiradas / INSERT novas
    D->>B: DELETE e INSERT de competicao_atleta
    alt falha
        D->>B: ROLLBACK
        D-->>V: PersistenciaException
    else
        D->>B: COMMIT
        V-->>U: "Inscrições salvas com sucesso."
    end
```

## 9. Lançar resultado de competição

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelResultados
    participant C as CompeticaoController
    participant D as CompeticaoDAO
    participant B as Banco

    U->>V: "+ Incluir", escolhe modalidade, equipe/atleta, colocação
    V->>C: salvarResultado(competicao, resultado)
    C->>C: Validador (modalidade, colocação > 0, colocação ou placar)
    alt modalidade ou atleta não inscrito
        C-->>V: RegraNegocioException
        V-->>U: Mensagens.erro (diálogo reabre para correção)
    else válido
        C->>D: salvarResultado(competicao, resultado)
        D->>B: INSERT INTO resultado ...
        V->>V: carregar()
        Note over V: a consulta passa a mostrar "1º - Futsal (F)"
    end
```

## 10. Cadastrar treino (conflito de horário)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroTreino
    participant C as TreinoController
    participant D as TreinoDAO
    participant B as Banco

    U->>V: preenche modalidade, data, horário, local, Salvar
    V->>C: salvar(treino)  (Treino ou Amistoso: polimorfismo)
    C->>C: Validador (+ adversário, se for Amistoso)
    C->>C: gera o título se estiver vazio
    C->>D: listarNoMesmoDiaELocal(treino)
    D->>B: SELECT ... WHERE data = ? AND LOWER(local) = LOWER(?)
    D-->>C: treinos do dia no local
    alt diferença menor que 1 hora
        C-->>V: RegraNegocioException("Conflito de horário: ...")
        V-->>U: Mensagens.erro
    else sem conflito
        C->>D: inserir(treino)
        D->>B: INSERT INTO treino (tipo = TREINO ou AMISTOSO, ...)
        V-->>U: "Treino salvo com sucesso. Ele já aparece no calendário."
    end
```

## 11. Registrar presença em treino

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelPresenca
    participant M as Treino (modelo)
    participant C as TreinoController
    participant D as TreinoDAO
    participant B as Banco

    V->>C: carregarPresencas(treino) e atletasDaChamada(treino)
    C->>D: carregarPresencas(treino)
    D->>B: SELECT atleta_id, presente FROM presenca_treino
    U->>V: marca presentes, Salvar
    loop cada atleta da lista
        V->>M: registrarPresenca(atleta, presente) (Map<Atleta, Boolean>)
    end
    V->>C: salvarPresencas(treino)
    alt treino ainda não aconteceu
        C-->>V: RegraNegocioException
        V-->>U: "Só é possível registrar presença em treinos que já aconteceram."
    else
        C->>D: salvarPresencas(treino)
        D->>D: emTransacao: DELETE + INSERT de cada presença
        D->>B: COMMIT
        V-->>U: "Presença salva: 3 de 4 atleta(s) presente(s)."
    end
```

## 12. Concluir tarefa de evento

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelGerenciarEvento
    participant C as EventoController
    participant D as EventoDAO
    participant B as Banco

    V->>C: carregarDetalhes(evento)
    C->>D: carregarDetalhes(evento)
    D->>B: SELECT responsáveis e tarefas do evento
    V->>V: evento.getTarefasAtrasadas(hoje) → prazo em vermelho e alerta
    U->>V: seleciona tarefa, Concluir
    V->>C: salvarTarefa(evento, tarefa com situação CONCLUIDA)
    C->>C: Validador (descrição e situação)
    alt dados inválidos
        C-->>V: ValidacaoException
        V-->>U: Mensagens.erro
    else
        C->>D: salvarTarefa(evento, tarefa)
        D->>B: UPDATE tarefa SET situacao = 'CONCLUIDA' ...
        V->>V: carregar() (alerta de atraso some)
    end
```
