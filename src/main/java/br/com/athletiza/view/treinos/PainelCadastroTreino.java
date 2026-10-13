package br.com.athletiza.view.treinos;

import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de treino ou amistoso (CH-35). O amistoso tem os campos
 * extras de adversário e resultado. Ao salvar, aparece no calendário.
 * As ações ficam no CadastroTreinoController.
 */
public class PainelCadastroTreino extends PainelFormulario {

    private final boolean amistoso;
    private final JComboBox<Modalidade> modalidade = new JComboBox<>();
    private final JTextField data = new JTextField();
    private final JTextField horario = new JTextField();
    private final JTextField local = new JTextField();
    private final JComboBox<Membro> responsavel = Combos.comOpcaoTodos(new ArrayList<Membro>(), "Nenhum");
    private final JTextField adversario = new JTextField();
    private final JTextField resultado = new JTextField();
    private final JTextField titulo = new JTextField();
    private final JTextArea observacoes = new JTextArea();

    /** @param amistoso true para mostrar também os campos de adversário e resultado */
    public PainelCadastroTreino(boolean amistoso) {
        this.amistoso = amistoso;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
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
        if (amistoso) {
            adicionarCampo("Adversário", adversario, true);
            adicionarCampo("Resultado", resultado, false);
        }
        adicionarCampo("Local", local, false);
        adicionarCampo("Título", titulo, false);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);
    }

    /**
     * Mostra os dados do treino.
     *
     * @param modalidades opções de modalidade
     * @param membros     opções de responsável (membros ativos)
     */
    public void preencherCampos(Treino treino, List<Modalidade> modalidades, List<Membro> membros) {
        String tipo = amistoso ? "Amistoso" : "Treino";
        setTitulo((treino.isNova() ? "Novo " : "Editar ") + tipo);
        modalidade.removeAllItems();
        for (Modalidade opcao : modalidades) {
            modalidade.addItem(opcao);
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
    }

    /** Preenche os campos exclusivos do amistoso. */
    public void preencherAmistoso(String nomeAdversario, String resultadoDoJogo) {
        adversario.setText(nomeAdversario);
        resultado.setText(resultadoDoJogo);
    }

    public Modalidade getModalidade() {
        return (Modalidade) modalidade.getSelectedItem();
    }

    public Membro getResponsavel() {
        return (Membro) responsavel.getSelectedItem();
    }

    public String getData() {
        return data.getText();
    }

    public String getHorario() {
        return horario.getText();
    }

    public String getLocal() {
        return textoOuNulo(local.getText());
    }

    public String getTituloTreino() {
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
}
