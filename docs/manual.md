# Manual de uso (CH-50)

## Entrar no sistema

1. Informe **usuário** e **senha** e pressione Enter. No primeiro acesso: `admin` / `admin123`.
   Senhas provisórias (a inicial e as definidas por um administrador) precisam ser trocadas ao entrar.
2. Marque **Lembrar de mim** para o sistema guardar o seu usuário neste computador.
3. Para trocar a sua senha, clique no seu nome, no rodapé do menu.

**Perfis.** Fora o administrador, o perfil vem do cargo que o membro ligado ao seu usuário ocupa na
gestão em andamento (o setor de cada cargo é definido em **Gestão → Cargos**):

| Perfil | Quem | O que pode fazer |
|---|---|---|
| Administrador | Usuário marcado como administrador | Tudo, inclusive cadastrar usuários e liberar acessos |
| Presidência | Presidente ou Vice-presidente | Inclui, edita e exclui em todos os módulos, menos usuários |
| Diretor | Cargo que dirige um setor (ex.: Diretor de Esportes) | Consulta tudo, inclusive o calendário completo; inclui e edita só a área da sua diretoria |
| Assessor | Cargo subordinado a um diretor do mesmo setor (ex.: Coordenador de Modalidade) | Só consulta, e só os módulos do setor que assessora |

Área de cada diretoria: **Esportes** cuida de Atletas, Treinos, Competições e dos eventos do tipo
Competição; **Eventos**, das festas e eventos "Outro"; **Ações sociais** e **Ações ambientais**, dos
eventos desses tipos. Em **Demandas**, o diretor só registra as ligadas a um evento ou competição da sua
área. Gestão, compromissos do calendário e Registros ficam com a presidência. Os botões que o perfil
não permite ficam desabilitados.

Usuários de exemplo (senha `athletiza`): `mariana` (Presidência), `juliana` (Diretora de Esportes),
`rafael` (Diretor de Eventos) e `bruno` (Assessor de Esportes).

## Padrão das telas

- **Consultas:** campo **Buscar** (filtra enquanto você digita), filtros ao lado, clique no título
  de uma coluna para ordenar, clique duplo numa linha para editar.
- **Cadastros:** campos com `*` são obrigatórios. **Salvar** grava; **Cancelar** pede confirmação.
  Datas no formato `dd/mm/aaaa` e horários em `hh:mm`.
- **Excluir** sempre pede confirmação. Se o registro tiver vínculos (ex.: atleta com presenças), o sistema
  explica o motivo e não exclui. Nesses casos, prefira mudar a situação para *Inativo*.

## Início (calendário)

- Mostra o mês atual, com as atividades de cada dia em cores: treino (roxo), amistoso (lilás),
  competição (verde), evento (rosa) e compromisso (azul).
- **Mês** e **Semana** alternam a visão. Na semana aparecem todas as atividades de cada dia, com horário.
- **<** e **>** trocam de mês (ou de semana); **Hoje** volta para a data atual.
- Clique num dia para ver as atividades dele na lateral; **clique duplo** cria um compromisso naquele dia.
- Sem dia selecionado, a lateral mostra os **próximos eventos** (7, 15 ou 30 dias).
- **Abrir** (na lista lateral) ou clique duplo na visão semanal leva à tela de edição do treino, competição ou evento.
- **Alertas** (topo da lateral): tarefas atrasadas (vermelho), treinos dos últimos 30 dias sem lista de presença
  (amarelo) e competições que começam nos próximos 7 dias (verde).

## Dashboard

No topo ficam os números gerais: atletas ativos, treinos e amistosos realizados, percentual de presença e
demandas em aberto. O filtro **Treinos e presenças** (últimos 30 dias, 90 dias, este ano ou todo o período)
vale para treinos, amistosos, presenças, eventos e pódios; atletas e demandas mostram sempre a situação atual.
Passe o mouse sobre qualquer gráfico para ver os detalhes. O Dashboard aparece para administrador,
presidência e diretores; assessores não o veem, porque ele mostra os dados de todos os setores.

**Aba Perguntas da diretoria** (barras horizontais, com a resposta em destaque e as 6 primeiras posições;
havendo empate no primeiro lugar, todos os empatados aparecem):

- **Qual modalidade tem mais atletas?** Atletas ativos em cada modalidade (vínculos inativos não contam).
- **Qual modalidade tem mais atletas presentes nos treinos?** Total de presenças nas listas de presença.
  O detalhe mostra a média de presentes por treino e o percentual de presença.
- **Qual modalidade realizou mais treinos e amistosos?** Treinos (verde) e amistosos (roxo) que já aconteceram.
- **Quais atletas menos participam dos treinos?** Frequência de cada atleta, da menor para a maior
  (vermelho abaixo de 50%, amarelo até 75%, verde acima).
- **Quais setores estão com o maior número de demandas?** Demandas abertas ou em andamento (módulo
  **Demandas**), agrupadas pelo setor dos cargos dos responsáveis na gestão em andamento (Esportes, Eventos,
  Financeiro...). A parte vermelha da barra são as atrasadas. Uma demanda com responsáveis de setores
  diferentes conta em cada um deles (no número do topo, conta uma vez só). Sem responsável com setor, vale o
  setor do evento ou competição relacionado; sem nenhum dos dois, aparece em *Sem setor definido*.

**Aba Linha do tempo:**

- **Próximos 30 dias:** linha do tempo com treinos, amistosos, competições, eventos e compromissos agendados,
  nas cores do calendário. Atividades do mesmo dia ficam empilhadas.
- **Atividades por mês:** gráfico de linhas com treinos, amistosos, competições e eventos realizados em cada mês
  (a partir do primeiro mês com atividade no período; no máximo 24 meses).
- **Presença nos treinos por mês:** linha com o percentual de presentes (meses sem lista de presença ficam em branco).
- **Presenças e faltas por mês:** colunas empilhadas com as marcações das listas de presença.

**Aba Distribuição:**

- **Situação dos atletas nas modalidades** (pizza): ativos, lesionados, afastados e inativos.
- **Eventos por tipo** (rosca, com o total no meio): festas, ações sociais e ambientais, competições e outros,
  realizados e agendados a partir do início do período (os cancelados não entram).
- **Demandas por situação** (rosca) e **Prioridade das demandas em aberto** (pizza).
- **Pódios por modalidade** (colunas empilhadas): 1º, 2º e 3º lugares nas competições do período, da modalidade
  com mais pódios para a com menos.

Para ver o dashboard com mais dados, execute o script opcional `03_dados_dashboard.sql` (veja o README).

## Gestão

- **Gestões:** períodos não podem se sobrepor. **Gerenciar** mostra quem ocupa cada cargo, com
  **Adicionar membro**, **Trocar membro** e **Remover**. **Organograma** desenha a hierarquia.
- **Cargos:** nível 1 é o topo; cada cargo pode ser subordinado a um cargo de nível menor.
- **Membros:** pessoas que ocupam cargos, organizam eventos e cuidam de tarefas.

## Atletas

- Filtre por modalidade e situação e escolha a ordenação.
- No cadastro, marque as modalidades do atleta e a situação em cada uma (Ativo, Lesionado, Afastado, Inativo).
- **Modalidades** abre o cadastro de modalidades (ex.: Futsal Feminino).

## Treinos

- Escolha a modalidade na lista à esquerda. **+ Treino** e **+ Amistoso** abrem o cadastro
  (o amistoso pede o adversário e, depois do jogo, o resultado).
- O sistema não aceita dois treinos no mesmo local com menos de 1 hora de diferença.
- **Presença:** marque quem compareceu e salve. Só vale para treinos que já aconteceram.
- **Frequência:** percentual de presença de cada atleta, por modalidade e período. Clique duplo num atleta
  (ou **Ver treinos do atleta**) mostra em quais treinos ele esteve presente ou ausente.

## Competições

- **Gerenciar:** à esquerda, marque as modalidades da competição; à direita, os atletas inscritos na
  modalidade selecionada. Clique em **Salvar inscrições** ao terminar.
- Aba **Resultados:** lance colocação e/ou placar da equipe ou de um atleta inscrito.
- Aba **Tarefas:** tarefas da organização (inscrição, transporte, uniformes...), com responsável, prazo e
  situação. As atrasadas aparecem em vermelho.
- A consulta mostra o melhor resultado e o resumo das tarefas de cada competição.

## Eventos

- Tipos: festa, ação social, ação ambiental, competição ou outro.
- **Responsáveis e tarefas:** adicione os membros que organizam o evento e as tarefas, com responsável,
  prazo e situação. Tarefas atrasadas aparecem em vermelho; **Concluir** encerra a tarefa selecionada.

## Demandas

- Registre o que precisa ser feito pela atlética, com prioridade (alta, média ou baixa), prazo e situação
  (aberta, em andamento, concluída ou cancelada).
- **Relacionada a:** opcionalmente, ligue a demanda a um evento (festa, ação social, ação ambiental...) ou
  a uma competição. Deixe "Nenhum" para uma demanda avulsa.
- **Responsáveis:** marque um ou mais membros da gestão em andamento (quem tem cargo nela, no módulo Gestão).
  Filtre a consulta por situação ou por responsável.
- As demandas pendentes aparecem primeiro, das de maior prioridade e prazo mais próximo.

## Registros (fotos e vídeos)

- As fotos e vídeos ficam organizados em **pastas** (Festas, Ações Sociais, Competições, Treinos, Outros...).
- **+ Nova Pasta** cria uma pasta; botão direito numa pasta permite renomear ou excluir (só pastas vazias).
- **+ Adicionar Arquivo** envia fotos (jpg, png, gif, bmp) e vídeos (mp4, mov, avi, mkv, webm, wmv) para a
  pasta selecionada. Também é possível **arrastar** arquivos do Explorador de Arquivos para a tela.
- **Clique duplo** abre o arquivo no programa padrão do computador; **botão direito** permite renomear,
  mover para outra pasta ou excluir.
- Os arquivos são copiados para `Documentos/Athletiza-midias`. Cada computador tem a sua cópia: uma foto
  adicionada em outro computador aparece na lista, mas só abre onde o arquivo existir.

## Usuários (administradores)

- Crie usuários com login e senha (mínimo de 6 caracteres) e escolha o **membro da gestão** que vai usá-lo:
  o acesso dele aparece na hora, calculado pelo cargo na gestão em andamento. Marque **Administrador**
  apenas para quem deve ter acesso total.
- Quem não tem cargo com setor na gestão em andamento não consegue entrar. Ao trocar de gestão, o
  acesso muda sozinho conforme os novos cargos.
- Na edição, deixe a senha em branco para mantê-la.
- O sistema sempre mantém pelo menos um administrador ativo.
- A senha que o administrador define para outra pessoa é provisória: ela troca no primeiro acesso.

## Em caso de erro

Se aparecer "Ocorreu um erro inesperado", envie ao responsável pelo sistema o arquivo indicado na mensagem
(`Documentos/Athletiza-logs/athletiza-0.log`), que contém os detalhes técnicos.
