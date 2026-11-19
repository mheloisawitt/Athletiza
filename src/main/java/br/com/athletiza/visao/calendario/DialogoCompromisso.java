package br.com.athletiza.visao.calendario;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Compromisso;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class DialogoCompromisso extends JDialog implements Navegador {
    private final Formulario formulario;

    public DialogoCompromisso(Component pai, Compromisso compromisso) {
        super(SwingUtilities.getWindowAncestor(pai), compromisso.isNova() ? "Novo compromisso" : "Editar compromisso",
                ModalityType.APPLICATION_MODAL);
        formulario = new Formulario(compromisso, this);
        setContentPane(formulario);
        setSize(new Dimension(620, 520));
        setLocationRelativeTo(pai);
    }

    public void adicionarAcaoBtnSalvar(ActionListener acao) {
        formulario.adicionarAcaoBtnSalvar(acao);
    }

    public void apresentarTela() {
        setVisible(true);
    }

    public String getTitulo() {
        return formulario.titulo.getText().trim();
    }

    public LocalDate getData() throws ValidacaoException {
        return Validador.converterData(formulario.data.getText(), "Data");
    }

    public LocalTime getHorario() throws ValidacaoException {
        return Validador.converterHorario(formulario.horario.getText(), "Horário");
    }

    public String getLocal() {
        return formulario.local.getText().trim();
    }

    public String getObservacoes() {
        return formulario.observacoes.getText().trim();
    }

    public void apresentarMensagem(String mensagem) {
        formulario.apresentarMensagem(mensagem);
    }

    public void apresentarErro(Exception erro) {
        formulario.apresentarErro(erro);
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

    private static class Formulario extends PainelFormulario {
        private final JTextField titulo = new JTextField();
        private final JTextField data = new JTextField();
        private final JTextField horario = new JTextField();
        private final JTextField local = new JTextField();
        private final JTextArea observacoes = new JTextArea();

        Formulario(Compromisso compromisso, Navegador navegador) {
            super(compromisso.isNova() ? "Novo compromisso" : "Editar compromisso", navegador);
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
    }
}
