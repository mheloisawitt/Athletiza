package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.CargoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Setor;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Cadastro e edição de cargo: nome, nível no organograma, cargo superior (CH-18)
 * e setor, que define o acesso ao sistema de quem o ocupa (RN19).
 */
public class PainelCadastroCargo extends PainelFormulario {

    private final Cargo cargo;
    private final CargoController controller = new CargoController();
    private final JTextField nome = new JTextField();
    private final JSpinner nivel = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<Cargo> superior = Combos.comOpcaoTodos(List.of(), "Nenhum (topo do organograma)");
    private final JComboBox<Setor> setor = Combos.comOpcaoTodos(List.of(Setor.values()), "Nenhum (sem acesso ao sistema)");

    public PainelCadastroCargo(Cargo cargo, Navegador navegador) {
        super(cargo.isNova() ? "Novo Cargo" : "Editar Cargo", navegador);
        this.cargo = cargo;

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

        try {
            List<Cargo> outros = controller.listar().stream().filter(c -> !c.equals(cargo)).toList();
            Combos.atualizarItens(superior, outros);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
        nome.setText(cargo.getNome());
        nivel.setValue(Math.max(1, cargo.getOrdem()));
        superior.setSelectedItem(cargo.getCargoSuperior());
        setor.setSelectedItem(cargo.getSetor());
    }

    @Override
    protected void salvar() throws AthletizaException {
        cargo.setNome(nome.getText().trim());
        cargo.setOrdem((Integer) nivel.getValue());
        cargo.setCargoSuperior((Cargo) superior.getSelectedItem());
        cargo.setSetor((Setor) setor.getSelectedItem());
        controller.salvar(cargo);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Cargo salvo com sucesso.";
    }
}
