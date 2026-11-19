package br.com.athletiza.visao.gestao;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Gestao;
import br.com.athletiza.modelo.SituacaoGestao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PainelCadastroGestao extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JComboBox<SituacaoGestao> situacao = new JComboBox<>(SituacaoGestao.values());
    private final JTextArea descricao = new JTextArea();

    public PainelCadastroGestao(Navegador navegador) {
        super("Nova Gestão", navegador);
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

    public void apresentarGestao(Gestao gestao) {
        definirTitulo(gestao.isNova() ? "Nova Gestão" : "Editar Gestão");
        nome.setText(gestao.getNome());
        inicio.setText(formatar(gestao.getDataInicio()));
        fim.setText(formatar(gestao.getDataFim()));
        situacao.setSelectedItem(gestao.getSituacao());
        descricao.setText(gestao.getDescricao());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public LocalDate getDataInicio() throws ValidacaoException {
        return Validador.converterData(inicio.getText(), "Data de início");
    }

    public LocalDate getDataFim() throws ValidacaoException {
        return Validador.converterData(fim.getText(), "Data de fim");
    }

    public SituacaoGestao getSituacao() {
        return (SituacaoGestao) situacao.getSelectedItem();
    }

    public String getDescricao() {
        return descricao.getText().isBlank() ? null : descricao.getText().trim();
    }

    private static String formatar(LocalDate data) {
        return data == null ? "" : Validador.FORMATO_DATA.format(data);
    }
}
