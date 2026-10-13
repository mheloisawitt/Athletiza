package br.com.athletiza.view.atletas;

import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Cadastro e edição de modalidade (CH-27). As ações ficam no CadastroModalidadeController.
 */
public class PainelCadastroModalidade extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JComboBox<Genero> genero = new JComboBox<>(Genero.values());

    public PainelCadastroModalidade() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Futsal");
        Combos.exibirComo(genero, g -> g == null ? "" : g.getNomeCompleto());
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Gênero", genero, true);
    }

    /** Mostra os dados da modalidade nos campos. */
    public void preencherCampos(Modalidade modalidade) {
        setTitulo(modalidade.isNova() ? "Nova Modalidade" : "Editar Modalidade");
        nome.setText(modalidade.getNome());
        if (modalidade.getGenero() != null) {
            genero.setSelectedItem(modalidade.getGenero());
        }
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public Genero getGenero() {
        return (Genero) genero.getSelectedItem();
    }

    public void limparTela() {
        nome.setText("");
        genero.setSelectedIndex(0);
    }
}
