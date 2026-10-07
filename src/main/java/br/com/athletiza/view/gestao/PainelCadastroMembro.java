package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Cadastro e edição de membro (CH-22).
 */
public class PainelCadastroMembro extends PainelFormulario {

    private final Membro membro;
    private final MembroController controller = new MembroController();
    private final JTextField nome = new JTextField();
    private final JTextField matricula = new JTextField();
    private final JTextField contato = new JTextField();
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());

    public PainelCadastroMembro(Membro membro, Navegador navegador) {
        super(membro.isNova() ? "Novo Membro" : "Editar Membro", navegador);
        this.membro = membro;

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        matricula.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: 201801");
        contato.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Telefone ou e-mail");
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Matrícula", matricula, true);
        adicionarCampo("Contato", contato, false);
        adicionarCampo("Situação", situacao, true);

        nome.setText(membro.getNome());
        matricula.setText(membro.getMatricula());
        contato.setText(membro.getContato());
        situacao.setSelectedItem(membro.getSituacao());
    }

    @Override
    protected void salvar() throws AthletizaException {
        membro.setNome(nome.getText().trim());
        membro.setMatricula(matricula.getText().trim());
        membro.setContato(contato.getText().isBlank() ? null : contato.getText().trim());
        membro.setSituacao((Situacao) situacao.getSelectedItem());
        controller.salvar(membro);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Membro salvo com sucesso.";
    }
}
