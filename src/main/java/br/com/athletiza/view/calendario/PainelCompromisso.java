package br.com.athletiza.view.calendario;

import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Formulário de compromisso (CH-16), exibido dentro do DialogoCompromisso.
 * As ações ficam no CompromissoController.
 */
public class PainelCompromisso extends PainelFormulario {

    private final JTextField titulo = new JTextField();
    private final JTextField data = new JTextField();
    private final JTextField horario = new JTextField();
    private final JTextField local = new JTextField();
    private final JTextArea observacoes = new JTextArea();

    public PainelCompromisso() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        titulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Reunião da gestão");
        data.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        horario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "hh:mm");
        local.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Sala da atlética");

        adicionarCampoLinhaInteira("Título", titulo, true);
        adicionarCampo("Data", data, true);
        adicionarCampo("Horário", horario, false);
        adicionarCampoLinhaInteira("Local", local, false);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);
    }

    public void preencherCampos(Compromisso compromisso) {
        setTitulo(compromisso.isNova() ? "Novo compromisso" : "Editar compromisso");
        titulo.setText(compromisso.getTitulo());
        data.setText(compromisso.getData() == null ? "" : Validador.FORMATO_DATA.format(compromisso.getData()));
        horario.setText(compromisso.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(compromisso.getHorario()));
        local.setText(compromisso.getLocal());
        observacoes.setText(compromisso.getObservacoes());
    }

    public String getTituloCompromisso() {
        return titulo.getText().trim();
    }

    public String getData() {
        return data.getText();
    }

    public String getHorario() {
        return horario.getText();
    }

    public String getLocal() {
        return local.getText().trim();
    }

    public String getObservacoes() {
        return observacoes.getText().trim();
    }
}
