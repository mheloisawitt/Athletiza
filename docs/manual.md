# Manual de uso (CH-50)

## Entrar no sistema

1. Informe **usuário** e **senha** e pressione Enter. No primeiro acesso: `admin` / `admin123`.
   Senhas provisórias (a inicial e as definidas por um administrador) precisam ser trocadas ao entrar.
2. Marque **Lembrar de mim** para o sistema guardar o seu usuário neste computador.
3. Para trocar a sua senha, clique no seu nome, no rodapé do menu.

**Perfis:** *Administrador* faz tudo, inclusive cadastrar usuários. *Diretoria* inclui, edita e exclui
registros. *Consulta* apenas visualiza (os botões de alteração ficam desabilitados).

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

- Crie usuários com login, perfil e senha (mínimo de 6 caracteres).
- Na edição, deixe a senha em branco para mantê-la.
- O sistema sempre mantém pelo menos um administrador ativo.
- A senha que o administrador define para outra pessoa é provisória: ela troca no primeiro acesso.

## Em caso de erro

Se aparecer "Ocorreu um erro inesperado", envie ao responsável pelo sistema o arquivo indicado na mensagem
(`Documentos/Athletiza-logs/athletiza-0.log`), que contém os detalhes técnicos.
