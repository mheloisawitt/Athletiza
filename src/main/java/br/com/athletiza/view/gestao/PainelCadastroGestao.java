package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de gestão (CH-20). As ações ficam no CadastroGestaoController.
 */
public class PainelCadastroGestao extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JComboBox<SituacaoGestao> situacao = new JComboBox<>(SituacaoGestao.values());
    private final JTextArea descricao = new JTextArea();

    public PainelCadastroGestao() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Gestão 2025-2026");
        inicio.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        fim.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        descricao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Breve descrição da gestão...");

        adicionarCampoLinhaInteira("Nome da gestão", nome, true);
        adicionarCampo("Data de início", inicio, true);
        adicionarCampo("Data de fim", fim, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampoLinhaInteira("Descrição", areaTexto(descricao), false);
    }

    public void preencherCampos(Gestao gestao) {
        setTitulo(gestao.isNova() ? "Nova Gestão" : "Editar Gestão");
        nome.setText(gestao.getNome());
        inicio.setText(formatar(gestao.getDataInicio()));
        fim.setText(formatar(gestao.getDataFim()));
        situacao.setSelectedItem(gestao.getSituacao());
        descricao.setText(gestao.getDescricao());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getDataInicio() {
        return inicio.getText();
    }

    public String getDataFim() {
        return fim.getText();
    }

    public SituacaoGestao getSituacao() {
        return (SituacaoGestao) situacao.getSelectedItem();
    }

    public String getDescricao() {
        return textoOuNulo(descricao.getText());
    }

    private static String formatar(LocalDate data) {
        return data == null ? "" : Validador.FORMATO_DATA.format(data);
    }
}
