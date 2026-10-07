package br.com.athletiza.view.atletas;

import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Cadastro e edição de modalidade (CH-27).
 */
public class PainelCadastroModalidade extends PainelFormulario {

    private final Modalidade modalidade;
    private final ModalidadeController controller = new ModalidadeController();
    private final JTextField nome = new JTextField();
    private final JComboBox<Genero> genero = new JComboBox<>(Genero.values());

    public PainelCadastroModalidade(Modalidade modalidade, Navegador navegador) {
        super(modalidade.isNova() ? "Nova Modalidade" : "Editar Modalidade", navegador);
        this.modalidade = modalidade;

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Futsal");
        Combos.exibirComo(genero, g -> g == null ? "" : g.getNomeCompleto());
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Gênero", genero, true);

        nome.setText(modalidade.getNome());
        if (modalidade.getGenero() != null) {
            genero.setSelectedItem(modalidade.getGenero());
        }
    }

    @Override
    protected void salvar() throws AthletizaException {
        String nomeAnterior = modalidade.getNome();
        Genero generoAnterior = modalidade.getGenero();
        modalidade.setNome(nome.getText().trim());
        modalidade.setGenero((Genero) genero.getSelectedItem());
        try {
            controller.salvar(modalidade);
        } catch (AthletizaException e) {
            modalidade.setNome(nomeAnterior);
            modalidade.setGenero(generoAnterior);
            throw e;
        }
    }

    @Override
    protected String getMensagemSucesso() {
        return "Modalidade salva com sucesso.";
    }
}
