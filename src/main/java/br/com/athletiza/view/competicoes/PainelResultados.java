package br.com.athletiza.view.competicoes;

import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Resultados da competição por modalidade, da equipe ou de um atleta (CH-32).
 * Fica na aba "Resultados" do gerenciamento; as ações ficam no ResultadosController.
 */
public class PainelResultados extends PainelConsulta<Resultado> {

    /* Campos do formulário de resultado (diálogo) */
    private final JComboBox<Modalidade> modalidade = new JComboBox<>();
    private final JComboBox<Atleta> atleta = Combos.comOpcaoTodos(new ArrayList<Atleta>(), "Equipe (resultado coletivo)");
    private final JTextField colocacao = new JTextField();
    private final JTextField placar = new JTextField();
    private final JTextField observacao = new JTextField();
    /** Competição com as inscrições salvas: define os atletas de cada modalidade no formulário. */
    private Competicao competicao;

    public PainelResultados() {
        super("Resultados", new ModeloTabela<Resultado>()
                .coluna("Modalidade", String.class, r -> r.getModalidade().toString())
                .coluna("Equipe / atleta", String.class, r -> r.isResultadoDeEquipe() ? "Equipe" : r.getAtleta().getNome())
                .coluna("Colocação", Integer.class, r -> r.getColocacao())
                .coluna("Placar", String.class, r -> r.getPlacar())
                .coluna("Observação", String.class, r -> r.getObservacao()));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));
        // Ao trocar a modalidade, as opções de atleta passam a ser os inscritos nela
        modalidade.addActionListener(e -> atualizarAtletas());
    }

    /** Preenche o formulário com o resultado e as modalidades inscritas na competição. */
    public void preencherFormulario(Resultado resultado, Competicao competicao) {
        this.competicao = competicao;
        modalidade.removeAllItems();
        for (Modalidade inscrita : competicao.getModalidades()) {
            modalidade.addItem(inscrita);
        }
        if (resultado.getModalidade() != null) {
            modalidade.setSelectedItem(resultado.getModalidade());
        }
        atualizarAtletas();
        atleta.setSelectedItem(resultado.getAtleta());
        colocacao.setText(resultado.getColocacao() == null ? "" : resultado.getColocacao().toString());
        placar.setText(resultado.getPlacar());
        observacao.setText(resultado.getObservacao());
    }

    /**
     * Mostra o formulário em um diálogo.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibirFormulario(String titulo) {
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
        campos.add(new JLabel("Modalidade *"));
        campos.add(modalidade);
        campos.add(new JLabel("Equipe ou atleta"));
        campos.add(atleta);
        campos.add(new JLabel("Colocação (1, 2, 3...)"));
        campos.add(colocacao);
        campos.add(new JLabel("Placar"));
        campos.add(placar);
        campos.add(new JLabel("Observação"));
        campos.add(observacao);
        return JOptionPane.showConfirmDialog(this, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public Modalidade getModalidade() {
        return (Modalidade) modalidade.getSelectedItem();
    }

    /** Atleta escolhido, ou null para resultado da equipe. */
    public Atleta getAtleta() {
        return (Atleta) atleta.getSelectedItem();
    }

    /** Colocação digitada (texto); a conversão para número é feita pelo Controller. */
    public String getColocacao() {
        return colocacao.getText();
    }

    public String getPlacar() {
        return placar.getText().isBlank() ? null : placar.getText().trim();
    }

    public String getObservacao() {
        return observacao.getText().isBlank() ? null : observacao.getText().trim();
    }

    private void atualizarAtletas() {
        List<Atleta> inscritos = new ArrayList<>();
        Modalidade escolhida = getModalidade();
        if (competicao != null && escolhida != null) {
            inscritos.addAll(competicao.getAtletas(escolhida));
        }
        Combos.atualizarItens(atleta, inscritos);
    }
}
