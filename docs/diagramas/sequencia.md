# Diagramas de sequência (CH-48)

Cada diagrama segue o MVC como visto em aula:

1. O **Controller** registra as ações dos botões da **View** com lambda (`view.adicionarAcaoSalvar(e -> salvar())`).
2. O usuário clica; a View só avisa o Controller.
3. O Controller lê os campos da View (`view.getNome()`), passa para o **Model** e chama `model.validar()`
   (regra de negócio no Model).
4. O Controller grava pelo **Repository** (interface); quem executa é o **DAO** (JDBC), que fala com o banco.
5. Se algo der errado, a exceção (`ValidacaoException`, `RegraNegocioException` ou `PersistenciaException`)
   **sobe** até o Controller (`throws`), que a trata no `catch` e pede à View para mostrar a mensagem.

São 13 fluxos, para que cada integrante escolha pelo menos 2. As mesmas imagens estão em [`imagens`](imagens).
Para exportar de novo: cole o código de um diagrama em <https://mermaid.live> e use **Actions > PNG** (ou SVG).

| # | Fluxo | Telas / classes principais |
|---|---|---|
| 1 | Login | `TelaLogin`, `LoginController`, `UsuarioRepository` (`UsuarioDAO`) |
| 2 | Exibir calendário | `PainelCalendario`, `CalendarioController`, `AtividadeRepository` (`AtividadeDAO`) |
| 3 | Incluir compromisso | `PainelCompromisso`, `CompromissoController`, `CompromissoRepository` |
| 4 | Cadastrar gestão | `PainelCadastroGestao`, `CadastroGestaoController`, `Gestao`, `GestaoRepository` |
| 5 | Adicionar membro a um cargo | `PainelGerenciarGestao`, `GerenciarGestaoController`, `Gestao`, `GestaoRepository` |
| 6 | Incluir atleta | `PainelCadastroAtleta`, `CadastroAtletaController`, `Atleta`, `AtletaRepository` |
| 7 | Excluir atleta com vínculos | `PainelConsultaAtletas`, `ConsultaAtletasController`, `AtletaRepository` |
| 8 | Gerenciar competição (inscrições) | `PainelGerenciarCompeticao`, `GerenciarCompeticaoController`, `Competicao`, `CompeticaoRepository` |
| 9 | Lançar resultado | `PainelResultados`, `ResultadosController`, `Resultado`, `CompeticaoRepository` |
| 10 | Cadastrar treino (conflito de horário) | `PainelCadastroTreino`, `CadastroTreinoController`, `Treino`, `TreinoRepository` |
| 11 | Registrar presença | `PainelPresenca`, `PresencaController`, `Treino`, `TreinoRepository` |
| 12 | Concluir tarefa de evento | `PainelTarefas`, `TarefasController`, `Tarefa`, `TarefaRepository` |
| 13 | Adicionar fotos e vídeos aos registros | `PainelRegistros`, `RegistrosController`, `ArquivoRepository` |

---

## 1. Login

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as TelaLogin (View)
    participant C as LoginController
    participant R as UsuarioRepository (UsuarioDAO)
    participant B as Banco

    Note over C,V: no construtor: view.adicionarAcaoEntrar(e -> entrar())
    U->>V: digita usuário e senha, Enter
    V->>C: entrar() (lambda do botão)
    C->>V: getLogin(), getSenha()
    C->>C: SwingWorker: autenticar() em segundo plano
    C->>C: Validador.obrigatorio(...).validar()
    alt campos vazios
        C-->>C: throw ValidacaoException
        C->>V: mostrarErro(e)
    end
    C->>R: buscarPorLogin(login)
    R->>B: SELECT * FROM usuario WHERE LOWER(login) = LOWER(?)
    B-->>R: linha do usuário (ou nenhuma)
    R-->>C: Usuario (ou null)
    C->>C: Senha.conferir(senha, hash)
    alt usuário null ou senha errada
        C-->>C: throw RegraNegocioException("Usuário ou senha inválidos.")
        C->>V: mostrarErro(e), limparSenha()
    else sucesso
        C->>C: Sessao.iniciar(usuario)
        C->>C: new TelaPrincipal() + new PrincipalController(tela, usuario)
        C->>V: dispose()
    end
```

## 2. Exibir calendário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant T as TelaPrincipal
    participant V as PainelCalendario (View)
    participant C as CalendarioController
    participant R as AtividadeRepository (AtividadeDAO)
    participant B as Banco

    U->>T: clica em "Início"
    T->>V: carregar()
    V->>C: ação "carregar" (lambda registrada pelo controller)
    C->>V: getInicioDoPeriodo(), getFimDoPeriodo()
    C->>R: listarPorPeriodo(1º dia, último dia)
    R->>B: SELECT treinos / competições / eventos / compromissos do período
    B-->>R: linhas
    R-->>C: List<Atividade> (Treino, Amistoso, Competicao, Evento, Compromisso)
    loop cada atividade e cada dia do mês
        C->>C: atividade.ocorreEm(dia) (polimorfismo: Competicao cobre vários dias)
    end
    C->>V: exibir(Map<LocalDate, List<Atividade>>, próximos, alertas)
    V->>V: CelulaDia.atualizar(...) com getCorHex() e getRotuloCurto()
    alt banco fora do ar
        R-->>C: throw PersistenciaException
        C->>V: mostrarErro(e) ("Não foi possível conectar ao banco de dados...")
    end
```

## 3. Incluir compromisso pelo calendário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCalendario (View)
    participant CC as CalendarioController
    participant F as PainelCompromisso (View)
    participant C as CompromissoController
    participant M as Compromisso (Model)
    participant R as CompromissoRepository
    participant B as Banco

    U->>V: clique duplo no dia
    V->>CC: ação "novo compromisso" (getDiaDoNovoCompromisso)
    CC->>C: new CompromissoController(dialogo.getFormulario(), compromisso, repository, dialogo)
    C->>F: preencherCampos(compromisso) e adicionarAcaoSalvar(e -> salvar())
    U->>F: preenche título e horário, Salvar
    F->>C: salvar()
    C->>F: getTituloCompromisso(), getData(), getHorario()
    C->>M: setTitulo(...), setData(Validador.converterData(...))
    C->>M: validar()
    alt título vazio
        M-->>C: throw ValidacaoException
        C->>F: mostrarErro(e) (diálogo continua aberto)
    else válido
        C->>R: inserir(compromisso)
        R->>B: INSERT INTO compromisso ...
        B-->>R: id gerado
        C->>F: mostrarMensagem("Compromisso salvo com sucesso.")
        C-->>CC: isSalvou() = true
        CC->>V: irParaDia(data) e exibir(...) (atualiza o mês)
    end
```

## 4. Cadastrar gestão

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroGestao (View)
    participant C as CadastroGestaoController
    participant M as Gestao (Model)
    participant R as GestaoRepository (GestaoDAO)
    participant B as Banco

    U->>V: preenche nome e período, Salvar
    V->>C: salvar() (lambda do botão)
    C->>V: getNome(), getDataInicio(), getDataFim()
    C->>M: setNome(...), setDataInicio(Validador.converterData(...))
    C->>M: validar() (obrigatórios, fim >= início)
    alt dados inválidos
        M-->>C: throw ValidacaoException
        C->>V: mostrarErro(e)
    end
    C->>R: listarTodos()
    R->>B: SELECT * FROM gestao ORDER BY data_inicio
    B-->>R: gestões
    R-->>C: List<Gestao>
    loop cada outra gestão
        C->>M: sobrepoe(outra)
    end
    alt período sobreposto
        C-->>C: throw RegraNegocioException("...se sobrepõe à Gestão 2025-2026...")
        C->>V: mostrarErro(e)
    else válido
        C->>R: inserir(gestao)
        R->>B: INSERT INTO gestao ...
        C->>V: mostrarMensagem("Gestão salva com sucesso.")
        C->>C: navegador.voltar() (a consulta recarrega)
    end
```

## 5. Adicionar membro a um cargo da gestão

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelGerenciarGestao (View)
    participant C as GerenciarGestaoController
    participant M as Gestao (Model)
    participant R as GestaoRepository (GestaoDAO)
    participant B as Banco

    U->>V: "+ Adicionar membro"
    V->>C: adicionarMembro() (lambda do botão)
    C->>V: exibirFormularioAdicionar(nome, cargos, membros ativos)
    U->>V: escolhe cargo e membro, OK
    C->>V: getCargoEscolhido(), getMembroEscolhido()
    C->>M: adicionarMembro(membro, cargo)
    alt membro já ocupa o cargo
        M-->>C: false (o Set<MembroCargo> não aceita repetição)
        C-->>C: throw RegraNegocioException("... já ocupa o cargo ...")
        C->>V: mostrarErro(e)
    else novo vínculo
        C->>R: adicionarMembro(gestao, membro, cargo)
        R->>B: INSERT INTO gestao_membro_cargo ...
        alt erro no banco
            R-->>C: throw PersistenciaException
            C->>M: removerMembro(membro, cargo) (desfaz e relança: throw e)
        end
        C->>R: carregarComposicao(gestao)
        C->>V: exibir(gestao.getComposicaoOrdenada())
    end
```

## 6. Incluir atleta

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroAtleta (View)
    participant C as CadastroAtletaController
    participant M as Atleta (Model)
    participant R as AtletaRepository (AtletaDAO)
    participant B as Banco

    U->>V: preenche dados e marca modalidades, Salvar
    V->>C: salvar() (lambda do botão)
    C->>V: getNome(), getMatricula(), getModalidadesMarcadas()
    C->>M: setNome(...), vincularModalidade(modalidade, situação)
    C->>M: validar()
    alt nome vazio ou nenhuma modalidade
        M-->>C: throw ValidacaoException (todos os erros de uma vez)
        C->>V: mostrarErro(e)
    end
    C->>R: existeMatricula(matricula, id)
    R->>B: SELECT COUNT(*) FROM atleta WHERE matricula = ?
    alt matrícula repetida
        C-->>C: throw RegraNegocioException
        C->>V: mostrarErro(e)
    else ok
        C->>R: inserir(atleta)
        R->>B: transação: INSERT atleta + INSERT atleta_modalidade (cada uma)
        alt falha no meio
            R->>B: rollback (nada é gravado)
            R-->>C: throw PersistenciaException
            C->>V: mostrarErro(e)
        end
        C->>V: mostrarMensagem("Atleta salvo com sucesso.")
        C->>C: navegador.voltar()
    end
```

## 7. Excluir atleta com vínculos

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelConsultaAtletas (View)
    participant C as ConsultaAtletasController
    participant R as AtletaRepository (AtletaDAO)
    participant B as Banco

    U->>V: seleciona um atleta, Excluir
    V->>V: há linha selecionada? (senão: "Selecione um registro na tabela.")
    V->>C: excluirSelecionado() (lambda do botão)
    C->>V: getSelecionado()
    C->>V: confirmar("Deseja realmente excluir ...?")
    C->>R: listarVinculos(id)
    R->>B: SELECT COUNT(*) presenças / inscrições / resultados
    B-->>R: contagens
    R-->>C: ["1 registro(s) de presença", ...]
    alt possui vínculos
        C-->>C: throw RegraNegocioException("Não é possível excluir..., pois possui: ...")
        C->>V: mostrarErro(e) (sugere alterar a situação para Inativo)
    else sem vínculos
        C->>R: excluir(id)
        R->>B: DELETE FROM atleta WHERE id = ? (modalidades em cascata)
        C->>V: mostrarMensagem("Atleta excluído.")
    end
    C->>C: carregar() (view.exibir(lista atualizada))
```

## 8. Gerenciar competição (inscrições)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelGerenciarCompeticao (View)
    participant C as GerenciarCompeticaoController
    participant M as Competicao (Model)
    participant R as CompeticaoRepository (CompeticaoDAO)
    participant B as Banco

    U->>V: abre "Gerenciar" na consulta
    V->>C: carregar()
    C->>R: carregarInscricoes(competicao)
    R->>B: SELECT competicao_modalidade / competicao_atleta
    C->>V: exibirInscricoes(competicao, modalidades, atletas)
    U->>V: marca um atleta na modalidade
    V->>C: marcarAtleta(modalidade, atleta, true) (lambda)
    C->>M: inscreverAtleta(modalidade, atleta)
    alt atleta não pratica a modalidade
        M-->>C: throw RegraNegocioException
        C->>V: mostrarErro(e)
    else ok
        M->>M: Map<Modalidade, Set<Atleta>>: o Set impede repetição
        C->>V: atualizarInscricoes(modalidade)
    end
    U->>V: "Salvar inscrições"
    V->>C: salvar()
    C->>R: salvarInscricoes(competicao)
    R->>B: transação: DELETE/INSERT modalidades e atletas
    C->>V: mostrarMensagem("Inscrições salvas com sucesso.")
```

## 9. Lançar resultado

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelResultados (View)
    participant C as ResultadosController
    participant M as Resultado / Competicao (Model)
    participant R as CompeticaoRepository (CompeticaoDAO)
    participant B as Banco

    U->>V: "+ Incluir" na aba Resultados
    V->>C: editar(new Resultado()) (lambda)
    C->>R: carregarInscricoes(cópia da competição)
    C->>V: preencherFormulario(resultado, competição salva)
    loop enquanto houver erro e o usuário clicar OK
        V-->>U: exibirFormulario("Lançar resultado")
        C->>V: getModalidade(), getAtleta(), getColocacao()
        C->>C: converterColocacao(texto) (NumberFormatException vira ValidacaoException)
        C->>M: resultado.validar() (colocação ou placar)
        C->>M: competicao.verificarResultado(resultado)
        alt modalidade/atleta não inscritos
            M-->>C: throw RegraNegocioException
            C->>V: mostrarErro(e) e reabre o formulário
        else ok
            C->>R: salvarResultado(competicao, resultado)
            R->>B: INSERT INTO resultado ...
        end
    end
    C->>V: exibir(listarResultados)
```

## 10. Cadastrar treino (conflito de horário)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelCadastroTreino (View)
    participant C as CadastroTreinoController
    participant M as Treino / Amistoso (Model)
    participant R as TreinoRepository (TreinoDAO)
    participant B as Banco

    U->>V: preenche modalidade, data, horário e local, Salvar
    V->>C: salvar() (lambda)
    C->>V: getModalidade(), getData(), getHorario(), getLocal()
    C->>M: setters (no Amistoso, também setAdversario e setResultado)
    C->>M: validar()
    Note over M: Amistoso sobrescreve validarCampos: confere também o adversário (polimorfismo)
    C->>M: gerarTituloSeVazio() (getTituloPadrao() de Treino ou de Amistoso)
    C->>R: listarNoMesmoDiaELocal(treino)
    R->>B: SELECT ... WHERE data = ? AND LOWER(local) = LOWER(?)
    R-->>C: List<Treino>
    alt outro treino a menos de 1 hora
        C-->>C: throw RegraNegocioException("Conflito de horário: ...")
        C->>V: mostrarErro(e)
    else ok
        C->>R: inserir(treino)
        R->>B: INSERT INTO treino (tipo TREINO ou AMISTOSO) ...
        C->>V: mostrarMensagem("Treino salvo com sucesso...")
    end
```

## 11. Registrar presença

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelPresenca (View)
    participant C as PresencaController
    participant M as Treino (Model)
    participant R as TreinoRepository (TreinoDAO)
    participant B as Banco

    Note over C: no construtor: carregarPresencas(treino) e atletasDaChamada()
    C->>V: exibirChamada(treino, atletas)
    U->>V: marca presentes (ou "Marcar todos"), Salvar
    V->>C: salvar() (lambda)
    C->>V: getPresencas()
    loop cada atleta
        C->>M: registrarPresenca(atleta, presente) (Map<Atleta, Boolean>)
    end
    alt treino no futuro
        C-->>C: throw RegraNegocioException("Só é possível registrar presença...")
        C->>V: mostrarErro(e)
    else ok
        C->>R: salvarPresencas(treino)
        R->>B: transação: DELETE presenças do treino + INSERT cada uma
        C->>V: mostrarMensagem("Presença salva: 3 de 4 atleta(s) presente(s).")
    end
```

## 12. Concluir tarefa de evento

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelTarefas (View)
    participant C as TarefasController
    participant M as Tarefa (Model)
    participant R as TarefaRepository (TarefaDAO)
    participant B as Banco
    participant G as GerenciarEventoController

    U->>V: seleciona a tarefa, "Concluir"
    V->>V: há tarefa selecionada? (senão: aviso)
    V->>C: concluirSelecionada() (lambda)
    C->>V: getSelecionado()
    C->>M: setSituacao(CONCLUIDA)
    C->>M: validar()
    C->>R: salvar(EVENTO, idEvento, tarefa)
    R->>B: UPDATE tarefa SET situacao = 'CONCLUIDA' ...
    alt erro no banco
        R-->>C: throw PersistenciaException
        C->>V: mostrarErro(e)
    end
    C->>V: exibir(listar(EVENTO, idEvento))
    C->>G: aoAlterar.run() (lambda)
    G->>G: carregarEvento(): getTarefasAtrasadas(hoje)
    G-->>U: aviso "Atenção: N tarefa(s) atrasada(s)" atualizado
```

## 13. Adicionar fotos e vídeos aos registros

```mermaid
sequenceDiagram
    actor U as Usuário
    participant V as PainelRegistros (View)
    participant C as RegistrosController
    participant A as ArmazenamentoMidia
    participant R as ArquivoRepository (ArquivoDAO)
    participant B as Banco

    U->>V: arrasta arquivos ou "+ Adicionar Arquivo"
    V->>C: ação "adicionar arquivos" (getArquivosEscolhidos)
    C->>C: SwingWorker (cópia em segundo plano)
    C->>C: Validador: tipo aceito? até 500 MB?
    alt arquivo .txt ou grande demais
        C-->>C: throw ValidacaoException (nada é copiado)
        C->>V: mostrarErro(e.getCause())
    else ok
        loop cada arquivo
            C->>A: copiar(origem, idPasta)
            A-->>C: caminho relativo
            C->>R: inserir(arquivo)
            R->>B: INSERT INTO arquivo ...
            alt erro no banco
                R-->>C: throw PersistenciaException
                C->>A: apagar(caminho) (não deixa arquivo sem registro) e relança
            end
        end
        C->>V: exibirPastas / exibirArquivos (miniaturas)
        C->>V: mostrarMensagem("2 arquivo(s) adicionado(s) à pasta Festas.")
    end
```
