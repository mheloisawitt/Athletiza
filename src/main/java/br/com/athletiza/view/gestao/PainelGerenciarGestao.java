package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.MembroCargo;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Gerenciamento da gestão (CH-21): quem ocupa cada cargo. Permite adicionar,
 * trocar e remover membros, e abrir o organograma (CH-23).
 * As ações ficam no GerenciarGestaoController.
 */
public class PainelGerenciarGestao extends PainelConsulta<MembroCargo> {

    private final JButton btnOrganograma = Botoes.contornoVerde("Organograma", null);
    private final JComboBox<Cargo> cargo = new JComboBox<>();
    private final JComboBox<Membro> membro = new JComboBox<>();

    public PainelGerenciarGestao() {
        super("", new ModeloTabela<MembroCargo>()
                .coluna("Cargo", String.class, mc -> mc.getCargo().getNome())
                .coluna("Nível", Integer.class, mc -> mc.getCargo().getOrdem())
                .coluna("Membro", String.class, mc -> mc.getMembro().getNome())
                .coluna("Matrícula", String.class, mc -> mc.getMembro().getMatricula())
                .coluna("Contato", String.class, mc -> mc.getMembro().getContato()),
                EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
        renomearBotao(Acao.INCLUIR, "+ Adicionar membro");
        renomearBotao(Acao.EDITAR, "Trocar membro");
        renomearBotao(Acao.EXCLUIR, "Remover");
        adicionarBotao(btnOrganograma);
        larguraColuna(1, 60);
    }

    public void adicionarAcaoOrganograma(ActionListener acao) {
        btnOrganograma.addActionListener(acao);
    }

    /**
     * Diálogo "Adicionar membro": escolha do cargo e do membro.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibirFormularioAdicionar(String nomeGestao, List<Cargo> cargos, List<Membro> membros) {
        cargo.removeAllItems();
        for (Cargo opcao : cargos) {
            cargo.addItem(opcao);
        }
        preencherMembros(membros, null);
        JPanel campos = new JPanel(new GridLayout(4, 1, 0, 6));
        campos.add(new JLabel("Cargo"));
        campos.add(cargo);
        campos.add(new JLabel("Membro"));
        campos.add(membro);
        return JOptionPane.showConfirmDialog(this, campos, "Adicionar membro à " + nomeGestao,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /**
     * Diálogo "Trocar membro": escolha de quem passa a ocupar o cargo.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibirFormularioTroca(MembroCargo ocupacao, List<Membro> membros) {
        preencherMembros(membros, ocupacao.getMembro());
        JPanel campos = new JPanel(new GridLayout(2, 1, 0, 6));
        campos.add(new JLabel("Novo ocupante do cargo " + ocupacao.getCargo().getNome()));
        campos.add(membro);
        return JOptionPane.showConfirmDialog(this, campos, "Trocar membro", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public Cargo getCargoEscolhido() {
        return (Cargo) cargo.getSelectedItem();
    }

    public Membro getMembroEscolhido() {
        return (Membro) membro.getSelectedItem();
    }

    private void preencherMembros(List<Membro> membros, Membro selecionado) {
        membro.removeAllItems();
        for (Membro opcao : membros) {
            membro.addItem(opcao);
        }
        if (selecionado != null) {
            membro.setSelectedItem(selecionado);
        }
    }
}
