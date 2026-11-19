package br.com.athletiza.visao.competicoes;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.SituacaoCompeticao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PainelCadastroCompeticao extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JTextField local = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JTextField horario = new JTextField();
    private final JComboBox<SituacaoCompeticao> situacao = new JComboBox<>(SituacaoCompeticao.values());
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroCompeticao(Navegador navegador) {
        super("Nova Competição", navegador);

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

    public void apresentarCompeticao(Competicao competicao) {
        definirTitulo(competicao.isNova() ? "Nova Competição" : "Editar Competição");
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
        return local.getText().isBlank() ? null : local.getText().trim();
    }

    public LocalDate getDataInicio() throws ValidacaoException {
        return Validador.converterData(inicio.getText(), "Data de início");
    }

    public LocalDate getDataFim() throws ValidacaoException {
        return Validador.converterData(fim.getText(), "Data de fim");
    }

    public LocalTime getHorario() throws ValidacaoException {
        return Validador.converterHorario(horario.getText(), "Horário");
    }

    public SituacaoCompeticao getSituacao() {
        return (SituacaoCompeticao) situacao.getSelectedItem();
    }

    public String getObservacoes() {
        return observacoes.getText().isBlank() ? null : observacoes.getText().trim();
    }
}
