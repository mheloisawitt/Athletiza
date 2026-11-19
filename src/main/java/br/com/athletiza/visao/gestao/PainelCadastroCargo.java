package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.modelo.Setor;
import br.com.athletiza.util.Cores;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class PainelCadastroCargo extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JSpinner nivel = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<Cargo> superior = Combos.comOpcaoTodos(List.of(), "Nenhum (topo do organograma)");
    private final JComboBox<Setor> setor = Combos.comOpcaoTodos(List.of(Setor.values()), "Nenhum (sem acesso ao sistema)");

    public PainelCadastroCargo(Navegador navegador) {
        super("Novo Cargo", navegador);
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Diretor de Marketing");
        adicionarCampoLinhaInteira("Nome", nome, true);
        adicionarCampo("Nível (1 = topo)", nivel, true);
        adicionarCampo("Subordinado a", superior, false);
        adicionarCampo("Setor", setor, false);

        JLabel ajuda = new JLabel("<html><body style='width: 640px'>O setor define o acesso ao sistema de quem ocupa o cargo na gestão em andamento:"
                + " Presidência altera tudo; o cargo de um setor que não é subordinado a outro do mesmo setor é o"
                + " diretor (altera só a sua área); os subordinados a ele no mesmo setor são assessores (só consultam)."
                + "</html>");
        ajuda.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(ajuda);
    }

    public void apresentarCargo(Cargo cargo, List<Cargo> outrosCargos) {
        definirTitulo(cargo.isNova() ? "Novo Cargo" : "Editar Cargo");
        Combos.atualizarItens(superior, outrosCargos);
        nome.setText(cargo.getNome());
        nivel.setValue(Math.max(1, cargo.getOrdem()));
        superior.setSelectedItem(cargo.getCargoSuperior());
        setor.setSelectedItem(cargo.getSetor());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public int getNivel() {
        return (Integer) nivel.getValue();
    }

    public Cargo getCargoSuperior() {
        return (Cargo) superior.getSelectedItem();
    }

    public Setor getSetor() {
        return (Setor) setor.getSelectedItem();
    }
}
