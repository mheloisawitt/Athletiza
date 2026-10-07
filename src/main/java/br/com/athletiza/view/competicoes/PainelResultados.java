package br.com.athletiza.view.competicoes;

import br.com.athletiza.controller.CompeticaoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Resultados da competição por modalidade, da equipe ou de um atleta (CH-32).
 * As opções de modalidade e atleta consideram as inscrições já salvas.
 */
class PainelResultados extends PainelConsulta<Resultado> {

    /** Fornece a competição com as inscrições salvas no banco. */
    @FunctionalInterface
    interface FonteCompeticao {

        Competicao obter() throws PersistenciaException;
    }

    private final CompeticaoController controller = new CompeticaoController();
    private final FonteCompeticao fonte;

    PainelResultados(FonteCompeticao fonte) {
        super("Resultados", new ModeloTabela<Resultado>()
                .coluna("Modalidade", String.class, r -> r.getModalidade().toString())
                .coluna("Equipe / atleta", String.class, r -> r.isResultadoDeEquipe() ? "Equipe" : r.getAtleta().getNome())
                .coluna("Colocação", Integer.class, Resultado::getColocacao)
                .coluna("Placar", String.class, Resultado::getPlacar)
                .coluna("Observação", String.class, Resultado::getObservacao));
        this.fonte = fonte;
        setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));
    }

    @Override
    protected List<Resultado> buscarDados() throws AthletizaException {
        return controller.listarResultados(fonte.obter());
    }

    @Override
    protected void incluir() {
        editar(new Resultado());
    }

    /** Diálogo de lançamento; reabre enquanto houver erro, para o usuário corrigir. */
    @Override
    protected void editar(Resultado resultado) {
        try {
            Competicao competicao = fonte.obter();
            List<Modalidade> modalidades = List.copyOf(competicao.getModalidades());
            if (modalidades.isEmpty()) {
                Mensagens.aviso(this, "Inscreva ao menos uma modalidade (e salve) antes de lançar resultados.");
                return;
            }
            JComboBox<Modalidade> modalidade = new JComboBox<>(modalidades.toArray(Modalidade[]::new));
            JComboBox<Atleta> atleta = Combos.comOpcaoTodos(List.of(), "Equipe (resultado coletivo)");
            JTextField colocacao = new JTextField(resultado.getColocacao() == null ? "" : resultado.getColocacao().toString());
            JTextField placar = new JTextField(resultado.getPlacar());
            JTextField observacao = new JTextField(resultado.getObservacao());
            modalidade.addActionListener(e -> Combos.atualizarItens(atleta,
                    List.copyOf(competicao.getAtletas((Modalidade) modalidade.getSelectedItem()))));
            if (resultado.getModalidade() != null) {
                modalidade.setSelectedItem(resultado.getModalidade());
            }
            Combos.atualizarItens(atleta, List.copyOf(competicao.getAtletas((Modalidade) modalidade.getSelectedItem())));
            atleta.setSelectedItem(resultado.getAtleta());

            JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
            campos.add(new JLabel("Modalidade *"));
            campos.add(modalidade);
            campos.add(new JLabel("Equipe ou atleta"));
            campos.add(atleta);
            campos.add(new JLabel("Colocação (1, 2, 3...)"));
            campos.add(colocacao);
            campos.add(new JLabel("Placar"));
            campos.add(placar);
            campos.add(new JLabel("Observação"));
            campos.add(observacao);

            String titulo = resultado.isNova() ? "Lançar resultado" : "Editar resultado";
            while (JOptionPane.showConfirmDialog(this, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    resultado.setModalidade((Modalidade) modalidade.getSelectedItem());
                    resultado.setAtleta((Atleta) atleta.getSelectedItem());
                    resultado.setColocacao(converterColocacao(colocacao.getText()));
                    resultado.setPlacar(placar.getText().isBlank() ? null : placar.getText().trim());
                    resultado.setObservacao(observacao.getText().isBlank() ? null : observacao.getText().trim());
                    controller.salvarResultado(competicao, resultado);
                    carregar();
                    return;
                } catch (AthletizaException e) {
                    Mensagens.erro(this, e);
                }
            }
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }

    @Override
    protected String getPerguntaExclusao(Resultado resultado) {
        return "Excluir o resultado de " + (resultado.isResultadoDeEquipe() ? "equipe" : resultado.getAtleta().getNome())
                + " em " + resultado.getModalidade() + "?";
    }

    @Override
    protected void excluir(Resultado resultado) {
        try {
            controller.excluirResultado(resultado);
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }

    private static Integer converterColocacao(String texto) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim().replace("º", ""));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A \"Colocação\" deve ser um número (ex.: 1 para primeiro lugar).");
        }
    }
}
