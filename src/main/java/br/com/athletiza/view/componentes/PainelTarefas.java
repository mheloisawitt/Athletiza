package br.com.athletiza.view.componentes;

import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Cores;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
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
 * As atrasadas aparecem em vermelho. As ações ficam no TarefasController.
 */
public class PainelTarefas extends PainelConsulta<Tarefa> {

    private final JButton btnConcluir = Botoes.contornoVerde("Concluir", null);

    /* Campos do formulário de tarefa (diálogo) */
    private final JTextField descricao = new JTextField(28);
    private final JComboBox<Membro> responsavel = Combos.comOpcaoTodos(new ArrayList<Membro>(), "Sem responsável");
    private final JTextField prazo = new JTextField();
    private final JComboBox<SituacaoTarefa> situacao = new JComboBox<>(SituacaoTarefa.values());

    public PainelTarefas() {
        super("Tarefas", new ModeloTabela<Tarefa>()
                .coluna("Tarefa", String.class, t -> t.getDescricao())
                .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome())
                .coluna("Prazo", LocalDate.class, t -> t.getPrazo())
                .coluna("Situação", SituacaoTarefa.class, t -> t.getSituacao()),
                EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        btnConcluir.setEnabled(podeAlterar());
        adicionarBotao(btnConcluir);
        larguraColuna(0, 200);
        tabela.getColumnModel().getColumn(1).setMinWidth(120);
        tabela.getColumnModel().getColumn(2).setMinWidth(185);
        tabela.getColumnModel().getColumn(3).setMinWidth(125);
        tabela.getColumnModel().getColumn(2).setCellRenderer(new RenderizadorPrazo());
        prazo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
    }

    /** "Concluir" só chama o Controller se houver uma tarefa selecionada. */
    public void adicionarAcaoConcluir(ActionListener acao) {
        btnConcluir.addActionListener(e -> {
            if (getSelecionado() == null) {
                mostrarAviso("Selecione uma tarefa na tabela.");
            } else {
                acao.actionPerformed(e);
            }
        });
    }

    /* Formulário de inclusão/edição */

    /** Preenche o formulário com os dados da tarefa e as opções de responsável. */
    public void preencherFormulario(Tarefa tarefa, List<Membro> membros) {
        descricao.setText(tarefa.getDescricao());
        Combos.atualizarItens(responsavel, membros);
        responsavel.setSelectedItem(tarefa.getResponsavel());
        prazo.setText(tarefa.getPrazo() == null ? "" : Validador.FORMATO_DATA.format(tarefa.getPrazo()));
        situacao.setSelectedItem(tarefa.getSituacao());
    }

    /**
     * Mostra o formulário em um diálogo.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibirFormulario(String titulo) {
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
        campos.add(new JLabel("Descrição *"));
        campos.add(descricao);
        campos.add(new JLabel("Responsável"));
        campos.add(responsavel);
        campos.add(new JLabel("Prazo"));
        campos.add(prazo);
        campos.add(new JLabel("Situação *"));
        campos.add(situacao);
        return JOptionPane.showConfirmDialog(this, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public String getDescricao() {
        return descricao.getText().trim();
    }

    public Membro getResponsavel() {
        return (Membro) responsavel.getSelectedItem();
    }

    public String getPrazo() {
        return prazo.getText();
    }

    public SituacaoTarefa getSituacao() {
        return (SituacaoTarefa) situacao.getSelectedItem();
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
