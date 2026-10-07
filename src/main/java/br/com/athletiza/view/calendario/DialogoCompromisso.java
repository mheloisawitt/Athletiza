package br.com.athletiza.view.calendario;

import br.com.athletiza.controller.CalendarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Diálogo de inclusão e edição de compromissos a partir do calendário (CH-16).
 * Reaproveita o PainelFormulario; "voltar" fecha o diálogo.
 */
class DialogoCompromisso extends JDialog implements Navegador {

    private boolean salvou;

    private DialogoCompromisso(Component pai, Compromisso compromisso, CalendarioController controller) {
        super(SwingUtilities.getWindowAncestor(pai), compromisso.isNova() ? "Novo compromisso" : "Editar compromisso",
                ModalityType.APPLICATION_MODAL);
        setContentPane(new Formulario(compromisso, controller, this));
        setSize(new Dimension(620, 520));
        setLocationRelativeTo(pai);
    }

    /**
     * Abre o diálogo e espera ele ser fechado.
     *
     * @return true se o compromisso foi salvo
     */
    static boolean abrir(Component pai, Compromisso compromisso, CalendarioController controller) {
        DialogoCompromisso dialogo = new DialogoCompromisso(pai, compromisso, controller);
        dialogo.setVisible(true);
        return dialogo.salvou;
    }

    @Override
    public void abrir(JComponent tela) {
        setContentPane(tela);
        revalidate();
    }

    @Override
    public void voltar() {
        dispose();
    }

    private class Formulario extends PainelFormulario {

        private final Compromisso compromisso;
        private final CalendarioController controller;
        private final JTextField titulo = new JTextField();
        private final JTextField data = new JTextField();
        private final JTextField horario = new JTextField();
        private final JTextField local = new JTextField();
        private final JTextArea observacoes = new JTextArea();

        Formulario(Compromisso compromisso, CalendarioController controller, Navegador navegador) {
            super(compromisso.isNova() ? "Novo compromisso" : "Editar compromisso", navegador);
            this.compromisso = compromisso;
            this.controller = controller;

            titulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Reunião da gestão");
            data.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
            horario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "hh:mm");
            local.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Sala da atlética");

            adicionarCampoLinhaInteira("Título", titulo, true);
            adicionarCampo("Data", data, true);
            adicionarCampo("Horário", horario, false);
            adicionarCampoLinhaInteira("Local", local, false);
            adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);

            titulo.setText(compromisso.getTitulo());
            LocalDate dataAtual = compromisso.getData();
            data.setText(dataAtual == null ? "" : Validador.FORMATO_DATA.format(dataAtual));
            horario.setText(compromisso.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(compromisso.getHorario()));
            local.setText(compromisso.getLocal());
            observacoes.setText(compromisso.getObservacoes());
        }

        @Override
        protected void salvar() throws AthletizaException {
            compromisso.setTitulo(titulo.getText().trim());
            compromisso.setData(Validador.converterData(data.getText(), "Data"));
            compromisso.setHorario(Validador.converterHorario(horario.getText(), "Horário"));
            compromisso.setLocal(local.getText().trim());
            compromisso.setObservacoes(observacoes.getText().trim());
            controller.salvarCompromisso(compromisso);
            salvou = true;
        }

        @Override
        protected String getMensagemSucesso() {
            return "Compromisso salvo com sucesso.";
        }
    }
}
