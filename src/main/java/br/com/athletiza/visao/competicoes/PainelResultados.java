package br.com.athletiza.visao.competicoes;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Resultado;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PainelResultados extends PainelConsulta<Resultado> {

    public PainelResultados(Navegador navegador) {
        super("Resultados", new ModeloTabela<Resultado>()
                .coluna("Modalidade", String.class, r -> r.getModalidade().toString())
                .coluna("Equipe / atleta", String.class, r -> r.isResultadoDeEquipe() ? "Equipe" : r.getAtleta().getNome())
                .coluna("Colocação", Integer.class, Resultado::getColocacao)
                .coluna("Placar", String.class, Resultado::getPlacar)
                .coluna("Observação", String.class, Resultado::getObservacao), navegador);
        setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));
    }

    public boolean apresentarFormulario(Resultado resultado, Competicao competicao) throws ValidacaoException {
        List<Modalidade> modalidades = List.copyOf(competicao.getModalidades());
        JComboBox<Modalidade> modalidade = new JComboBox<>(modalidades.toArray(new Modalidade[0]));
        JComboBox<Atleta> atleta = Combos.comOpcaoTodos(List.of(), "Equipe (resultado coletivo)");
        JTextField colocacao = new JTextField(resultado.getColocacao() == null ? "" : resultado.getColocacao().toString());
        JTextField placar = new JTextField(resultado.getPlacar());
        JTextField observacao = new JTextField(resultado.getObservacao());
        modalidade.addActionListener(e -> Combos.atualizarItens(atleta,
                List.copyOf(competicao.getAtletas((Modalidade) modalidade.getSelectedItem()))));
        if (resultado.getModalidade() != null) {
            modalidade.setSelectedItem(resultado.getModalidade());
        }
        Combos.atualizarItens(atleta, List.copyOf(competicao.getAtletas((Modalidade) modalidade.getSelectedItem())));
        atleta.setSelectedItem(resultado.getAtleta());

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

        String titulo = resultado.isNova() ? "Lançar resultado" : "Editar resultado";
        if (JOptionPane.showConfirmDialog(this, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        resultado.setModalidade((Modalidade) modalidade.getSelectedItem());
        resultado.setAtleta((Atleta) atleta.getSelectedItem());
        resultado.setPlacar(placar.getText().isBlank() ? null : placar.getText().trim());
        resultado.setObservacao(observacao.getText().isBlank() ? null : observacao.getText().trim());
        resultado.setColocacao(converterColocacao(colocacao.getText()));
        return true;
    }

    @Override
    protected String getPerguntaExclusao(Resultado resultado) {
        return "Excluir o resultado de " + (resultado.isResultadoDeEquipe() ? "equipe" : resultado.getAtleta().getNome())
                + " em " + resultado.getModalidade() + "?";
    }

    private static Integer converterColocacao(String texto) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim().replace("º", ""));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A \"Colocação\" deve ser um número (ex.: 1 para primeiro lugar).");
        }
    }
}
