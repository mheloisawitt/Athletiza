package br.com.athletiza.visao.eventos;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.SituacaoEvento;
import br.com.athletiza.modelo.TipoEvento;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PainelCadastroEvento extends PainelFormulario {
    private final JTextField nome = new JTextField();

    private final JComboBox<TipoEvento> tipo = new JComboBox<>(
            Sessao.getAcesso().tiposDeEvento().toArray(TipoEvento[]::new));
    private final JTextField data = new JTextField();
    private final JTextField horario = new JTextField();
    private final JTextField local = new JTextField();
    private final JComboBox<SituacaoEvento> situacao = new JComboBox<>(SituacaoEvento.values());
    private final JTextArea descricao = new JTextArea();
    private final JTextArea observacoes = new JTextArea();

    public PainelCadastroEvento(Navegador navegador) {
        super("Novo Evento", navegador);

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
    }

    public void apresentarEvento(Evento evento) {
        definirTitulo(evento.isNova() ? "Novo Evento" : "Editar Evento");
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

    public String getNome() {
        return nome.getText().trim();
    }

    public TipoEvento getTipo() {
        return (TipoEvento) tipo.getSelectedItem();
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

    public SituacaoEvento getSituacao() {
        return (SituacaoEvento) situacao.getSelectedItem();
    }

    public String getDescricao() {
        return textoOuNulo(descricao.getText());
    }

    public String getObservacoes() {
        return textoOuNulo(observacoes.getText());
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
