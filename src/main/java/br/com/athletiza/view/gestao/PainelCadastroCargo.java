package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Cargo;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Cadastro e edição de cargo: nome, nível no organograma e cargo superior (CH-18).
 * As ações ficam no CadastroCargoController.
 */
public class PainelCadastroCargo extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JSpinner nivel = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<Cargo> superior = Combos.comOpcaoTodos(new ArrayList<Cargo>(), "Nenhum (topo do organograma)");

    public PainelCadastroCargo() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Diretor de Marketing");
        adicionarCampoLinhaInteira("Nome", nome, true);
        adicionarCampo("Nível (1 = topo)", nivel, true);
        adicionarCampo("Subordinado a", superior, false);
    }

    /**
     * Mostra os dados do cargo.
     *
     * @param outrosCargos opções de "Subordinado a" (sem o próprio cargo)
     */
    public void preencherCampos(Cargo cargo, List<Cargo> outrosCargos) {
        setTitulo(cargo.isNova() ? "Novo Cargo" : "Editar Cargo");
        Combos.atualizarItens(superior, outrosCargos);
        nome.setText(cargo.getNome());
        nivel.setValue(Math.max(1, cargo.getOrdem()));
        superior.setSelectedItem(cargo.getCargoSuperior());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public int getNivel() {
        return (Integer) nivel.getValue();
    }

    /** Cargo superior escolhido, ou null se for o topo do organograma. */
    public Cargo getSuperior() {
        return (Cargo) superior.getSelectedItem();
    }
}
