package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PainelCadastroMembro extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JTextField matricula = new JTextField();
    private final JTextField contato = new JTextField();
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());

    public PainelCadastroMembro(Navegador navegador) {
        super("Novo Membro", navegador);
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        matricula.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: 201801");
        contato.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Telefone ou e-mail");
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Matrícula", matricula, true);
        adicionarCampo("Contato", contato, false);
        adicionarCampo("Situação", situacao, true);
    }

    public void apresentarMembro(Membro membro) {
        definirTitulo(membro.isNova() ? "Novo Membro" : "Editar Membro");
        nome.setText(membro.getNome());
        matricula.setText(membro.getMatricula());
        contato.setText(membro.getContato());
        situacao.setSelectedItem(membro.getSituacao());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getMatricula() {
        return matricula.getText().trim();
    }

    public String getContato() {
        return contato.getText().isBlank() ? null : contato.getText().trim();
    }

    public Situacao getSituacao() {
        return (Situacao) situacao.getSelectedItem();
    }
}
