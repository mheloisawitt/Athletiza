package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.GestaoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de gestão (CH-20). Períodos sobrepostos são recusados.
 */
public class PainelCadastroGestao extends PainelFormulario {

    private final Gestao gestao;
    private final GestaoController controller = new GestaoController();
    private final JTextField nome = new JTextField();
    private final JTextField inicio = new JTextField();
    private final JTextField fim = new JTextField();
    private final JComboBox<SituacaoGestao> situacao = new JComboBox<>(SituacaoGestao.values());
    private final JTextArea descricao = new JTextArea();

    public PainelCadastroGestao(Gestao gestao, Navegador navegador) {
        super(gestao.isNova() ? "Nova Gestão" : "Editar Gestão", navegador);
        this.gestao = gestao;

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Gestão 2025-2026");
        inicio.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        fim.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        descricao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Breve descrição da gestão...");

        adicionarCampoLinhaInteira("Nome da gestão", nome, true);
        adicionarCampo("Data de início", inicio, true);
        adicionarCampo("Data de fim", fim, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampoLinhaInteira("Descrição", areaTexto(descricao), false);

        nome.setText(gestao.getNome());
        inicio.setText(formatar(gestao.getDataInicio()));
        fim.setText(formatar(gestao.getDataFim()));
        situacao.setSelectedItem(gestao.getSituacao());
        descricao.setText(gestao.getDescricao());
    }

    @Override
    protected void salvar() throws AthletizaException {
        gestao.setNome(nome.getText().trim());
        gestao.setDataInicio(Validador.converterData(inicio.getText(), "Data de início"));
        gestao.setDataFim(Validador.converterData(fim.getText(), "Data de fim"));
        gestao.setSituacao((SituacaoGestao) situacao.getSelectedItem());
        gestao.setDescricao(descricao.getText().isBlank() ? null : descricao.getText().trim());
        controller.salvar(gestao);
    }

    /** Avisa quais gestões passam para "Encerrada" ao salvar esta como "Em andamento" (RN08). */
    @Override
    protected boolean confirmarAntesDeSalvar() throws AthletizaException {
        if (situacao.getSelectedItem() != SituacaoGestao.EM_ANDAMENTO) {
            return true;
        }
        List<String> nomes = controller.outrasEmAndamento(gestao).stream().map(Gestao::getNome).toList();
        return nomes.isEmpty() || Mensagens.confirmar(this, "Só uma gestão fica \"Em andamento\". Ao salvar, "
                + String.join(", ", nomes) + (nomes.size() == 1 ? " passa" : " passam") + " para \"Encerrada\". Continuar?");
    }

    @Override
    protected String getMensagemSucesso() {
        return "Gestão salva com sucesso.";
    }

    private static String formatar(LocalDate data) {
        return data == null ? "" : Validador.FORMATO_DATA.format(data);
    }
}
