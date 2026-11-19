package br.com.athletiza.visao.atletas;

import br.com.athletiza.modelo.Genero;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PainelCadastroModalidade extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JComboBox<Genero> genero = new JComboBox<>(Genero.values());

    public PainelCadastroModalidade(Navegador navegador) {
        super("Nova Modalidade", navegador);
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Futsal");
        Combos.exibirComo(genero, g -> g == null ? "" : g.getNomeCompleto());
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Gênero", genero, true);
    }

    public void apresentarModalidade(Modalidade modalidade) {
        definirTitulo(modalidade.isNova() ? "Nova Modalidade" : "Editar Modalidade");
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
}
