package br.com.athletiza.visao.treinos;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Amistoso;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Treino;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PainelCadastroTreino extends PainelFormulario {
    private final JComboBox<Modalidade> modalidade = new JComboBox<>();
    private final JTextField data = new JTextField();
    private final JTextField horario = new JTextField();
    private final JTextField local = new JTextField();
    private final JComboBox<Membro> responsavel = Combos.comOpcaoTodos(List.of(), "Nenhum");
    private final JTextField adversario = new JTextField();
    private final JTextField resultado = new JTextField();
    private final JTextField titulo = new JTextField();
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroTreino(Treino treino, Navegador navegador) {
        super(tituloDaTela(treino), navegador);

        data.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        horario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "hh:mm");
        local.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Quadra 1");
        adversario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Atlética Furiosa");
        resultado.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: 3 x 2 (preencher após o jogo)");
        titulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Gerado automaticamente se ficar vazio");

        adicionarCampo("Modalidade", modalidade, true);
        adicionarCampo("Responsável", responsavel, false);
        adicionarCampo("Data", data, true);
        adicionarCampo("Horário", horario, false);
        if (treino instanceof Amistoso) {
            adicionarCampo("Adversário", adversario, true);
            adicionarCampo("Resultado", resultado, false);
        }
        adicionarCampo("Local", local, false);
        adicionarCampo("Título", titulo, false);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);
    }

    private static String tituloDaTela(Treino treino) {
        String tipo = treino instanceof Amistoso ? "Amistoso" : "Treino";
        return (treino.isNova() ? "Novo " : "Editar ") + tipo;
    }

    public void apresentarTreino(Treino treino, List<Modalidade> modalidades, List<Membro> membros) {
        for (Modalidade m : modalidades) {
            modalidade.addItem(m);
        }
        Combos.atualizarItens(responsavel, membros);
        if (treino.getModalidade() != null) {
            modalidade.setSelectedItem(treino.getModalidade());
        }
        responsavel.setSelectedItem(treino.getResponsavel());
        data.setText(treino.getData() == null ? "" : Validador.FORMATO_DATA.format(treino.getData()));
        horario.setText(treino.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(treino.getHorario()));
        local.setText(treino.getLocal());
        titulo.setText(treino.getTitulo());
        observacoes.setText(treino.getObservacoes());
        if (treino instanceof Amistoso amistoso) {
            adversario.setText(amistoso.getAdversario());
            resultado.setText(amistoso.getResultado());
        }
    }

    public Modalidade getModalidade() {
        return (Modalidade) modalidade.getSelectedItem();
    }

    public Membro getResponsavel() {
        return (Membro) responsavel.getSelectedItem();
    }

    public LocalDate getData() throws ValidacaoException {
        return Validador.converterData(data.getText(), "Data");
    }

    public LocalTime getHorario() throws ValidacaoException {
        return Validador.converterHorario(horario.getText(), "Horário");
    }

    public String getLocal() {
        return textoOuNulo(local.getText());
    }

    public String getTitulo() {
        return textoOuNulo(titulo.getText());
    }

    public String getObservacoes() {
        return textoOuNulo(observacoes.getText());
    }

    public String getAdversario() {
        return textoOuNulo(adversario.getText());
    }

    public String getResultado() {
        return textoOuNulo(resultado.getText());
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
