# Roteiro de testes por módulo (CH-49)

Roteiro para conferir o sistema antes da entrega e da apresentação. Cada integrante executa os casos das telas
que ficou responsável (ver [`telas-e-distribuicao.md`](telas-e-distribuicao.md)) e anota o resultado.
Além deste roteiro manual, o projeto tem testes automatizados (`mvn test`, banco H2 em memória).

## Preparação

1. Recrie o banco: execute `00_criar_banco.sql`, `01_estrutura.sql` e `02_dados_exemplo.sql`
   (pasta `src/main/resources/sql`), nessa ordem.
2. Confira o `db.properties` (copiado do `db.properties.example`) e rode o sistema pelo NetBeans.
3. **Datas dos dados de exemplo:** os treinos, eventos e tarefas são de outubro a dezembro de 2026. Os alertas e a
   lista de próximos eventos dependem da data do computador. Se estiver testando em outra data, use os casos
   marcados com ⏱ como referência e ajuste as datas dos registros para perto de hoje.
4. Usuários para os testes de perfil: crie no módulo Usuários (caso USU-01) um usuário *Diretoria* e um *Consulta*.

**Como preencher:** em *Resultado* escreva **OK** ou **Falhou**; em *Obs.*, o que apareceu de diferente.
Casos marcados com ❌ são negativos: o sistema deve **recusar** a operação com uma mensagem clara.

## 1. Acesso

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| ACE-01 | Banco recém-criado | Entrar com `admin` / `admin123` | Pede a troca obrigatória da senha provisória | | |
| ACE-02 | ACE-01 | Cancelar a troca de senha | ❌ Não entra no sistema; volta ao login | | |
| ACE-03 | ACE-01 | Trocar a senha (nova com 6+ caracteres e confirmação igual) | Entra na tela inicial; no próximo login a troca não é pedida | | |
| ACE-04 | — | Entrar com senha errada | ❌ "Usuário ou senha inválidos." | | |
| ACE-05 | Usuário inativo | Entrar com ele | ❌ "Este usuário está desativado. Procure um administrador." | | |
| ACE-06 | — | Marcar **Lembrar de mim**, entrar, sair e abrir de novo | O login vem preenchido | | |
| ACE-07 | Logado | Clicar no nome no rodapé do menu e trocar a senha informando a atual errada | ❌ "A senha atual não confere." | | |
| ACE-08 | Logado como *Consulta* | Percorrer as telas | Botões de incluir, editar e excluir desabilitados; menu Usuários não aparece | | |
| ACE-09 | Logado como *Diretoria* | Abrir o menu | Menu Usuários não aparece; demais módulos permitem alterar | | |

## 2. Início (calendário)

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| CAL-01 | — | Abrir a tela inicial | Mês atual, dia de hoje destacado, atividades coloridas por tipo | | |
| CAL-02 | — | Usar **<**, **>** e **Hoje** | Troca de mês e volta para o mês atual | | |
| CAL-03 | — | Clicar em **Semana** e navegar | Sete colunas (segunda a domingo), atividades com horário, sem barra de rolagem horizontal | | |
| CAL-04 | — | Clique duplo num dia vazio (visão mensal) | Abre o cadastro de compromisso com a data preenchida | | |
| CAL-05 | CAL-04 | Salvar sem título | ❌ Aponta o campo obrigatório | | |
| CAL-06 | CAL-04 | Salvar com título e horário | Compromisso aparece no dia, em azul | | |
| CAL-07 | Dia com treino | Clicar no dia e em **Abrir** na lista lateral | Abre o cadastro do treino; **Voltar** retorna ao calendário | | |
| CAL-08 | Visão semanal | Clique duplo numa competição e depois num evento | Abre o cadastro da competição / do evento | | |
| CAL-09 | Nenhum dia selecionado | Trocar entre 7, 15 e 30 dias | Lista de próximos eventos muda conforme o período ⏱ | | |
| CAL-10 | Tarefa com prazo vencido | Abrir a tela inicial | Alerta vermelho de tarefa atrasada (ex.: "Contratar DJ") ⏱ | | |
| CAL-11 | Treino passado sem presença | Abrir a tela inicial | Alerta amarelo de presença pendente; some depois de lançar a presença (TRE-07) ⏱ | | |
| CAL-12 | Competição nos próximos 7 dias | Abrir a tela inicial | Alerta verde de competição próxima ⏱ | | |

## 3. Gestão

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| GES-01 | — | Abrir Gestões | Lista ordenada por período; busca filtra enquanto digita | | |
| GES-02 | — | Incluir gestão com período dentro da "Gestão 2025-2026" | ❌ "O período informado se sobrepõe à Gestão 2025-2026…" | | |
| GES-03 | — | Incluir gestão com data final antes da inicial | ❌ Data final não pode ser anterior à inicial | | |
| GES-04 | — | Incluir "Gestão 2027" (01/01/2027 a 31/12/2027) | Gravada e listada | | |
| GES-05 | Gestão 2025-2026 | **Gerenciar** → **Adicionar membro** num cargo | Membro aparece no cargo | | |
| GES-06 | GES-05 | Adicionar o mesmo membro no mesmo cargo | ❌ "… já ocupa o cargo … nesta gestão." | | |
| GES-07 | GES-05 | **Trocar membro** e **Remover** | Ocupante trocado; depois removido (com confirmação) | | |
| GES-08 | — | **Organograma** da Gestão 2025-2026 | Hierarquia desenhada do Presidente para baixo | | |
| GES-09 | — | Cadastrar cargo subordinado a um cargo de nível maior ou igual | ❌ Recusa o cargo superior | | |
| GES-10 | — | Cadastrar membro com matrícula já usada (ex.: 201801) | ❌ "Já existe um membro com a matrícula 201801." | | |
| GES-11 | Membro com cargo | Excluir o membro | ❌ Explica os vínculos e não exclui | | |

## 4. Atletas

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| ATL-01 | — | Filtrar por modalidade *Futsal (F)* e situação *Ativo* | Só os atletas que atendem aos dois filtros | | |
| ATL-02 | — | Trocar a ordenação no combo | Lista reordenada (nome, matrícula, modalidade ou situação) | | |
| ATL-03 | — | Novo atleta com duas modalidades, uma *Lesionado* | Gravado com as duas modalidades e as situações escolhidas | | |
| ATL-04 | — | Novo atleta com matrícula existente (ex.: 202001) | ❌ "Já existe um atleta com a matrícula 202001." | | |
| ATL-05 | — | Data de nascimento no futuro ou `31/02/2004` | ❌ Data inválida / deve ser anterior a hoje | | |
| ATL-06 | Atleta com presença | Excluir "Ana Souza" | ❌ Explica os vínculos e não exclui | | |
| ATL-07 | — | **Modalidades** → incluir "Futsal Feminino" de novo | ❌ "A modalidade … já está cadastrada." | | |
| ATL-08 | — | Excluir modalidade com atletas | ❌ Recusa e explica o motivo | | |

## 5. Competições

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| COM-01 | — | Filtrar por situação e período | Lista filtrada; colunas de melhor resultado e tarefas visíveis | | |
| COM-02 | — | Nova competição com data final antes da inicial | ❌ Data final não pode ser anterior à inicial | | |
| COM-03 | — | Nova competição válida | Aparece na consulta e no calendário (verde) | | |
| COM-04 | JIUDESC 2026 | **Gerenciar** → marcar modalidade, inscrever atletas e **Salvar inscrições** | Inscrições gravadas; ao reabrir continuam marcadas | | |
| COM-05 | COM-04 | Aba **Resultados** → lançar colocação da equipe | Resultado listado e mostrado na consulta | | |
| COM-06 | COM-04 | Lançar resultado de atleta não inscrito na modalidade | ❌ "… não está inscrito(a) em …" | | |
| COM-07 | COM-04 | Aba **Tarefas** → nova tarefa com prazo ontem | Tarefa em vermelho; resumo "… atrasada" na consulta; alerta na tela inicial | | |
| COM-08 | COM-07 | Selecionar a tarefa e **Concluir** | Tarefa concluída, sem destaque de atraso | | |

## 6. Treinos

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| TRE-01 | — | Escolher *Futsal (F)* à esquerda | Treinos e amistoso da modalidade; resumo de presença | | |
| TRE-02 | — | **+ Treino** no mesmo local e dia de um treino, 30 min depois | ❌ "Conflito de horário: já existe …" | | |
| TRE-03 | — | **+ Treino** válido | Gravado e exibido no calendário (roxo) | | |
| TRE-04 | — | **+ Amistoso** sem adversário | ❌ Aponta o campo obrigatório | | |
| TRE-05 | Amistoso realizado | Editar e informar o resultado | Resultado gravado | | |
| TRE-06 | Treino futuro | **Presença** | ❌ "Só é possível registrar presença em treinos que já aconteceram." | | |
| TRE-07 | Treino passado | **Presença** → **Marcar todos**, desmarcar um, salvar | Contador correto; ao reabrir, marcações mantidas | | |
| TRE-08 | TRE-07 | **Frequência** da modalidade | Percentual por atleta com barra colorida | | |
| TRE-09 | TRE-08 | Clique duplo num atleta (ou **Ver treinos do atleta**) | Histórico com cada treino e Presente (verde) / Ausente (vermelho) | | |

## 7. Eventos

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| EVE-01 | — | Filtrar por tipo e período | Lista filtrada com resumo de tarefas | | |
| EVE-02 | — | Novo evento sem nome ou com data inválida | ❌ Aponta os campos com problema | | |
| EVE-03 | — | Novo evento válido | Aparece na consulta e no calendário (rosa) | | |
| EVE-04 | Festa Atlética | **Responsáveis e tarefas** → adicionar responsável já incluído | ❌ "… já é responsável por este evento." | | |
| EVE-05 | Festa Atlética | Nova tarefa, editar o prazo, **Concluir**, excluir | Cada alteração refletida na lista e no resumo da consulta | | |

## 8. Registros (galeria)

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| REG-01 | — | **+ Nova Pasta** com nome já existente (ex.: Festas) | ❌ "Já existe uma pasta chamada Festas." | | |
| REG-02 | — | **+ Adicionar Arquivo** com uma foto e um vídeo | Miniatura da foto e ícone do vídeo na pasta | | |
| REG-03 | — | Arrastar uma imagem do Explorador para a tela | Arquivo adicionado à pasta selecionada | | |
| REG-04 | — | Adicionar arquivo de tipo não aceito (ex.: .pdf) | ❌ Arquivo recusado com mensagem | | |
| REG-05 | REG-02 | Botão direito → renomear, mover para outra pasta | Arquivo renomeado e movido | | |
| REG-06 | Pasta com arquivos | Excluir a pasta | ❌ "A pasta … possui … arquivo(s)…" | | |
| REG-07 | REG-02 | Clique duplo no arquivo | Abre no programa padrão do computador | | |

## 9. Usuários (administrador)

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| USU-01 | Logado como admin | Criar usuário *Diretoria* com senha de 6+ caracteres | Gravado; no primeiro acesso dele a troca de senha é exigida | | |
| USU-02 | — | Senha com menos de 6 caracteres ou confirmação diferente | ❌ Aponta o problema da senha | | |
| USU-03 | — | Login já usado (ex.: admin) | ❌ "O login "admin" já está em uso." | | |
| USU-04 | — | Excluir o próprio usuário | ❌ "Você não pode excluir o seu próprio usuário." | | |
| USU-05 | Só um administrador ativo | Mudar o perfil ou a situação dele | ❌ "O sistema precisa de pelo menos um administrador ativo." | | |
| USU-06 | — | Editar usuário deixando a senha em branco | Senha anterior mantida | | |

## 10. Erros e robustez

| ID | Pré-condição | Passos | Resultado esperado | Resultado | Obs. |
|---|---|---|---|---|---|
| ERR-01 | — | Parar o PostgreSQL e tentar entrar | ❌ Mensagem amigável "Não foi possível conectar ao banco de dados…" | | |
| ERR-02 | Logado | Parar o PostgreSQL e abrir uma consulta | Mensagem amigável, sistema continua aberto | | |
| ERR-03 | ERR-02 | Abrir `Documentos/Athletiza-logs/athletiza-0.log` | Erro registrado com data, hora e detalhes | | |
| ERR-04 | Qualquer cadastro | Preencher e clicar **Cancelar** | Pede confirmação antes de descartar | | |
| ERR-05 | Qualquer consulta | Excluir um registro | Pede confirmação antes de excluir | | |

## Registro da execução

| Data | Integrante | Casos executados | Falhas encontradas | Corrigido em (branch) |
|---|---|---|---|---|
| | | | | |
