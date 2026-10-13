package br.com.athletiza.view.competicoes;

import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de competição (CH-30). Ao salvar, ela passa a aparecer no calendário.
 * As ações ficam no CadastroCompeticaoController.
 */
public class PainelCadastroCompeticao extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JTextField local = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JTextField horario = new JTextField();
    private final JComboBox<SituacaoCompeticao> situacao = new JComboBox<>(SituacaoCompeticao.values());
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroCompeticao() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: JIUDESC 2026");
        local.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cidade ou ginásio");
        inicio.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        fim.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa (se durar mais de um dia)");
        horario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "hh:mm");

        adicionarCampo("Nome", nome, true);
        adicionarCampo("Local", local, false);
        adicionarCampo("Data de início", inicio, true);
        adicionarCampo("Data de fim", fim, false);
        adicionarCampo("Horário", horario, false);
        adicionarCampo("Situação", situacao, true);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);
    }

    public void preencherCampos(Competicao competicao) {
        setTitulo(competicao.isNova() ? "Nova Competição" : "Editar Competição");
        nome.setText(competicao.getTitulo());
        local.setText(competicao.getLocal());
        inicio.setText(competicao.getData() == null ? "" : Validador.FORMATO_DATA.format(competicao.getData()));
        fim.setText(competicao.getDataFim() == null ? "" : Validador.FORMATO_DATA.format(competicao.getDataFim()));
        horario.setText(competicao.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(competicao.getHorario()));
        situacao.setSelectedItem(competicao.getSituacao());
        observacoes.setText(competicao.getObservacoes());
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getLocal() {
        return textoOuNulo(local.getText());
    }

    public String getDataInicio() {
        return inicio.getText();
    }

    public String getDataFim() {
        return fim.getText();
    }

    public String getHorario() {
        return horario.getText();
    }

    public SituacaoCompeticao getSituacao() {
        return (SituacaoCompeticao) situacao.getSelectedItem();
    }

    public String getObservacoes() {
        return textoOuNulo(observacoes.getText());
    }
}
