package br.com.athletiza.view.treinos;

import br.com.athletiza.controller.MembroController;
import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.controller.TreinoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de treino ou amistoso (CH-35). O amistoso tem os campos
 * extras de adversário e resultado. Ao salvar, aparece no calendário.
 */
public class PainelCadastroTreino extends PainelFormulario {

    private final Treino treino;
    private final TreinoController controller = new TreinoController();
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
        this.treino = treino;

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

        carregarOpcoes();
        preencher();
    }

    private static String tituloDaTela(Treino treino) {
        String tipo = treino instanceof Amistoso ? "Amistoso" : "Treino";
        return (treino.isNova() ? "Novo " : "Editar ") + tipo;
    }

    private void carregarOpcoes() {
        try {
            new ModalidadeController().listar().forEach(modalidade::addItem);
            Combos.atualizarItens(responsavel, new MembroController().listarAtivos());
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    private void preencher() {
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

    @Override
    protected void salvar() throws AthletizaException {
        treino.setModalidade((Modalidade) modalidade.getSelectedItem());
        treino.setResponsavel((Membro) responsavel.getSelectedItem());
        treino.setData(Validador.converterData(data.getText(), "Data"));
        treino.setHorario(Validador.converterHorario(horario.getText(), "Horário"));
        treino.setLocal(textoOuNulo(local.getText()));
        treino.setTitulo(textoOuNulo(titulo.getText()));
        treino.setObservacoes(textoOuNulo(observacoes.getText()));
        if (treino instanceof Amistoso amistoso) {
            amistoso.setAdversario(textoOuNulo(adversario.getText()));
            amistoso.setResultado(textoOuNulo(resultado.getText()));
        }
        controller.salvar(treino);
    }

    @Override
    protected String getMensagemSucesso() {
        return (treino instanceof Amistoso ? "Amistoso" : "Treino") + " salvo com sucesso. Ele já aparece no calendário.";
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
