package br.com.athletiza.view.eventos;

import br.com.athletiza.controller.EventoController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Responsáveis e tarefas do evento (CH-41). À esquerda, os membros que organizam
 * o evento; à direita, as tarefas com responsável, prazo e situação. Tarefas
 * atrasadas aparecem em vermelho.
 */
public class PainelGerenciarEvento extends JPanel implements Recarregavel {

    private final EventoController controller = new EventoController();
    private final Evento original;
    private final PainelResponsaveis responsaveis = new PainelResponsaveis();
    private final PainelTarefas tarefas = new PainelTarefas();
    private final JLabel alerta = new JLabel();

    public PainelGerenciarEvento(Evento evento, Navegador navegador) {
        super(new BorderLayout(0, 12));
        this.original = evento;
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JButton voltar = new JButton("‹ Voltar");
        voltar.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; margin:2,0,2,8");
        voltar.addActionListener(e -> navegador.voltar());
        JLabel titulo = new JLabel("Responsáveis e tarefas");
        titulo.setFont(Tema.fonteTitulo());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(voltar);
        linha.add(titulo);

        JLabel detalhe = new JLabel(evento.getTitulo() + "  ·  " + evento.getTipoEvento().getDescricao() + "  ·  "
                + Validador.FORMATO_DATA.format(evento.getData())
                + (evento.getLocal() == null ? "" : "  ·  " + evento.getLocal()));
        detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
        detalhe.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        alerta.setForeground(Cores.VERMELHO);
        alerta.setFont(Tema.fonte(Font.BOLD, 13f));
        alerta.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{linha, detalhe, alerta}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            cabecalho.add(c);
        }
        add(cabecalho, BorderLayout.NORTH);

        responsaveis.setPreferredSize(new Dimension(410, 0));
        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);
        corpo.add(responsaveis, BorderLayout.WEST);
        corpo.add(tarefas, BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    @Override
    public void carregar() {
        responsaveis.carregar();
        tarefas.carregar();
    }

    /** Lê o evento novamente do banco, com responsáveis e tarefas. */
    private Evento eventoAtualizado() throws PersistenciaException {
        Evento evento = new Evento(original.getTitulo(), original.getTipoEvento(), original.getData(),
                original.getHorario(), original.getLocal());
        evento.setId(original.getId());
        controller.carregarDetalhes(evento);
        int atrasadas = evento.getTarefasAtrasadas(LocalDate.now()).size();
        alerta.setText(atrasadas == 0 ? " " : "Atenção: " + atrasadas + " tarefa(s) atrasada(s).");
        return evento;
    }

    /** Membros responsáveis pela organização do evento. */
    private class PainelResponsaveis extends PainelConsulta<Membro> {

        private Evento evento;

        PainelResponsaveis() {
            super("Responsáveis", new ModeloTabela<Membro>()
                    .coluna("Membro", String.class, Membro::getNome)
                    .coluna("Contato", String.class, Membro::getContato),
                    EnumSet.of(Acao.INCLUIR, Acao.EXCLUIR));
            renomearBotao(Acao.INCLUIR, "+ Adicionar");
            renomearBotao(Acao.EXCLUIR, "Remover");
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        }

        @Override
        protected List<Membro> buscarDados() throws AthletizaException {
            evento = eventoAtualizado();
            return List.copyOf(evento.getResponsaveis());
        }

        @Override
        protected void incluir() {
            try {
                List<Membro> disponiveis = new MembroController().listarAtivos().stream()
                        .filter(m -> !evento.getResponsaveis().contains(m)).toList();
                if (disponiveis.isEmpty()) {
                    Mensagens.aviso(this, "Todos os membros ativos já são responsáveis por este evento.");
                    return;
                }
                JComboBox<Membro> membro = new JComboBox<>(disponiveis.toArray(Membro[]::new));
                if (JOptionPane.showConfirmDialog(this, membro, "Adicionar responsável", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    controller.adicionarResponsavel(evento, (Membro) membro.getSelectedItem());
                    carregar();
                }
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
        }

        @Override
        protected String getPerguntaExclusao(Membro membro) {
            return "Remover " + membro.getNome() + " dos responsáveis pelo evento?";
        }

        @Override
        protected void excluir(Membro membro) {
            try {
                controller.removerResponsavel(evento, membro);
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }

    /** Tarefas do evento; as atrasadas aparecem em vermelho. */
    private class PainelTarefas extends PainelConsulta<Tarefa> {

        private Evento evento;

        PainelTarefas() {
            super("Tarefas", new ModeloTabela<Tarefa>()
                    .coluna("Tarefa", String.class, Tarefa::getDescricao)
                    .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome())
                    .coluna("Prazo", LocalDate.class, Tarefa::getPrazo)
                    .coluna("Situação", SituacaoTarefa.class, Tarefa::getSituacao),
                    EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            JButton concluir = Botoes.contornoVerde("Concluir", e -> getSelecionado().ifPresentOrElse(this::concluir,
                    () -> Mensagens.aviso(this, "Selecione uma tarefa na tabela.")));
            adicionarBotao(concluir);
            larguraColuna(0, 200);
            tabela.getColumnModel().getColumn(1).setMinWidth(120);
            tabela.getColumnModel().getColumn(2).setMinWidth(185);
            tabela.getColumnModel().getColumn(3).setMinWidth(125);
            tabela.getColumnModel().getColumn(2).setCellRenderer(new RenderizadorPrazo());
        }

        @Override
        protected List<Tarefa> buscarDados() throws AthletizaException {
            evento = eventoAtualizado();
            return evento.getTarefas().stream().sorted().toList();
        }

        @Override
        protected void incluir() {
            editar(new Tarefa());
        }

        /** Diálogo da tarefa; reabre enquanto houver erro. */
        @Override
        protected void editar(Tarefa tarefa) {
            try {
                JTextField descricao = new JTextField(tarefa.getDescricao(), 28);
                JComboBox<Membro> responsavel = Combos.comOpcaoTodos(new MembroController().listarAtivos(), "Sem responsável");
                responsavel.setSelectedItem(tarefa.getResponsavel());
                JTextField prazo = new JTextField(tarefa.getPrazo() == null ? "" : Validador.FORMATO_DATA.format(tarefa.getPrazo()));
                prazo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
                JComboBox<SituacaoTarefa> situacao = new JComboBox<>(SituacaoTarefa.values());
                situacao.setSelectedItem(tarefa.getSituacao());

                JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
                campos.add(new JLabel("Descrição *"));
                campos.add(descricao);
                campos.add(new JLabel("Responsável"));
                campos.add(responsavel);
                campos.add(new JLabel("Prazo"));
                campos.add(prazo);
                campos.add(new JLabel("Situação *"));
                campos.add(situacao);

                String titulo = tarefa.isNova() ? "Nova tarefa" : "Editar tarefa";
                while (JOptionPane.showConfirmDialog(this, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    try {
                        tarefa.setDescricao(descricao.getText().trim());
                        tarefa.setResponsavel((Membro) responsavel.getSelectedItem());
                        tarefa.setPrazo(Validador.converterData(prazo.getText(), "Prazo"));
                        tarefa.setSituacao((SituacaoTarefa) situacao.getSelectedItem());
                        controller.salvarTarefa(evento, tarefa);
                        break;
                    } catch (AthletizaException e) {
                        Mensagens.erro(this, e);
                    }
                }
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }

        private void concluir(Tarefa tarefa) {
            tarefa.setSituacao(SituacaoTarefa.CONCLUIDA);
            try {
                controller.salvarTarefa(evento, tarefa);
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }

        @Override
        protected String getPerguntaExclusao(Tarefa tarefa) {
            return "Excluir a tarefa \"" + tarefa.getDescricao() + "\"?";
        }

        @Override
        protected void excluir(Tarefa tarefa) {
            try {
                controller.excluirTarefa(tarefa);
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }

        /** Prazo em vermelho, com o aviso "atrasada", quando a tarefa passou do prazo sem ser concluída. */
        private class RenderizadorPrazo extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable tab, Object valor, boolean selecionado, boolean foco,
                    int linha, int coluna) {
                Tarefa tarefa = modelo.getLinha(tab.convertRowIndexToModel(linha));
                String texto = tarefa.getPrazo() == null ? "" : Validador.FORMATO_DATA.format(tarefa.getPrazo());
                boolean atrasada = tarefa.isAtrasada(LocalDate.now());
                super.getTableCellRendererComponent(tab, atrasada ? texto + "  · atrasada" : texto, selecionado, false,
                        linha, coluna);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                setForeground(atrasada ? Cores.VERMELHO : Cores.TEXTO);
                setFont(atrasada ? getFont().deriveFont(Font.BOLD) : getFont().deriveFont(Font.PLAIN));
                return this;
            }
        }
    }
}
