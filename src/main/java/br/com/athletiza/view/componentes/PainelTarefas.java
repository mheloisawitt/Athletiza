package br.com.athletiza.view.componentes;

import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Tarefas com responsável, prazo e situação (RF19), usadas em eventos e em competições.
 * As atrasadas aparecem em vermelho. De onde vêm e para onde vão as tarefas é
 * definido pela FonteTarefas recebida.
 */
public class PainelTarefas extends PainelConsulta<Tarefa> {

    /** Como listar, salvar e excluir as tarefas do evento ou da competição. */
    public interface FonteTarefas {

        List<Tarefa> listar() throws AthletizaException;

        void salvar(Tarefa tarefa) throws AthletizaException;

        void excluir(Tarefa tarefa) throws AthletizaException;
    }

    private final FonteTarefas fonte;

    /** Resumo para as consultas, ex.: "1 de 3 concluídas · 1 atrasada(s)". */
    public static String resumir(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            return "—";
        }
        long concluidas = tarefas.stream().filter(t -> t.getSituacao() == SituacaoTarefa.CONCLUIDA).count();
        long atrasadas = tarefas.stream().filter(t -> t.isAtrasada(LocalDate.now())).count();
        return concluidas + " de " + tarefas.size() + " concluídas" + (atrasadas > 0 ? " · " + atrasadas + " atrasada(s)" : "");
    }

    public PainelTarefas(FonteTarefas fonte) {
        super("Tarefas", new ModeloTabela<Tarefa>()
                .coluna("Tarefa", String.class, Tarefa::getDescricao)
                .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome())
                .coluna("Prazo", LocalDate.class, Tarefa::getPrazo)
                .coluna("Situação", SituacaoTarefa.class, Tarefa::getSituacao),
                EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
        this.fonte = fonte;
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
        return fonte.listar();
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
                    fonte.salvar(tarefa);
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
            fonte.salvar(tarefa);
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
            fonte.excluir(tarefa);
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
