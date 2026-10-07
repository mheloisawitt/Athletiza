package br.com.athletiza.view.eventos;

import br.com.athletiza.controller.EventoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de evento (CH-40). Ao salvar, aparece no calendário.
 */
public class PainelCadastroEvento extends PainelFormulario {

    private final Evento evento;
    private final EventoController controller = new EventoController();
    private final JTextField nome = new JTextField();
    private final JComboBox<TipoEvento> tipo = new JComboBox<>(TipoEvento.values());
    private final JTextField data = new JTextField();
    private final JTextField horario = new JTextField();
    private final JTextField local = new JTextField();
    private final JComboBox<SituacaoEvento> situacao = new JComboBox<>(SituacaoEvento.values());
    private final JTextArea descricao = new JTextArea();
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroEvento(Evento evento, Navegador navegador) {
        super(evento.isNova() ? "Novo Evento" : "Editar Evento", navegador);
        this.evento = evento;

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Festa Atlética");
        data.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        horario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "hh:mm");
        local.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Ibirama");
        descricao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "O que vai acontecer no evento...");

        adicionarCampo("Nome", nome, true);
        adicionarCampo("Tipo", tipo, true);
        adicionarCampo("Data", data, true);
        adicionarCampo("Horário", horario, false);
        adicionarCampo("Local", local, false);
        adicionarCampo("Situação", situacao, true);
        adicionarCampoLinhaInteira("Descrição", areaTexto(descricao), false);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);

        nome.setText(evento.getTitulo());
        if (evento.getTipoEvento() != null) {
            tipo.setSelectedItem(evento.getTipoEvento());
        }
        data.setText(evento.getData() == null ? "" : Validador.FORMATO_DATA.format(evento.getData()));
        horario.setText(evento.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(evento.getHorario()));
        local.setText(evento.getLocal());
        situacao.setSelectedItem(evento.getSituacao());
        descricao.setText(evento.getDescricao());
        observacoes.setText(evento.getObservacoes());
    }

    @Override
    protected void salvar() throws AthletizaException {
        evento.setTitulo(nome.getText().trim());
        evento.setTipoEvento((TipoEvento) tipo.getSelectedItem());
        evento.setData(Validador.converterData(data.getText(), "Data"));
        evento.setHorario(Validador.converterHorario(horario.getText(), "Horário"));
        evento.setLocal(textoOuNulo(local.getText()));
        evento.setSituacao((SituacaoEvento) situacao.getSelectedItem());
        evento.setDescricao(textoOuNulo(descricao.getText()));
        evento.setObservacoes(textoOuNulo(observacoes.getText()));
        controller.salvar(evento);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Evento salvo com sucesso. Ele já aparece no calendário.";
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
