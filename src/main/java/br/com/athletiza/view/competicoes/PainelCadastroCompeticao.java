package br.com.athletiza.view.competicoes;

import br.com.athletiza.controller.CompeticaoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de competição (CH-30). Ao salvar, ela passa a aparecer no calendário.
 */
public class PainelCadastroCompeticao extends PainelFormulario {

    private final Competicao competicao;
    private final CompeticaoController controller = new CompeticaoController();
    private final JTextField nome = new JTextField();
    private final JTextField local = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JTextField horario = new JTextField();
    private final JComboBox<SituacaoCompeticao> situacao = new JComboBox<>(SituacaoCompeticao.values());
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroCompeticao(Competicao competicao, Navegador navegador) {
        super(competicao.isNova() ? "Nova Competição" : "Editar Competição", navegador);
        this.competicao = competicao;

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

        nome.setText(competicao.getTitulo());
        local.setText(competicao.getLocal());
        inicio.setText(competicao.getData() == null ? "" : Validador.FORMATO_DATA.format(competicao.getData()));
        fim.setText(competicao.getDataFim() == null ? "" : Validador.FORMATO_DATA.format(competicao.getDataFim()));
        horario.setText(competicao.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(competicao.getHorario()));
        situacao.setSelectedItem(competicao.getSituacao());
        observacoes.setText(competicao.getObservacoes());
    }

    @Override
    protected void salvar() throws AthletizaException {
        competicao.setTitulo(nome.getText().trim());
        competicao.setLocal(local.getText().isBlank() ? null : local.getText().trim());
        competicao.setData(Validador.converterData(inicio.getText(), "Data de início"));
        competicao.setDataFim(Validador.converterData(fim.getText(), "Data de fim"));
        competicao.setHorario(Validador.converterHorario(horario.getText(), "Horário"));
        competicao.setSituacao((SituacaoCompeticao) situacao.getSelectedItem());
        competicao.setObservacoes(observacoes.getText().isBlank() ? null : observacoes.getText().trim());
        controller.salvar(competicao);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Competição salva com sucesso. Ela já aparece no calendário.";
    }
}
