package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.CargoController;
import br.com.athletiza.controller.GestaoController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.MembroCargo;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Gerenciamento da gestão (CH-21): quem ocupa cada cargo. Permite adicionar,
 * trocar e remover membros, e abrir o organograma (CH-23).
 */
public class PainelGerenciarGestao extends PainelConsulta<MembroCargo> {

    private static final Comparator<MembroCargo> POR_CARGO = Comparator
            .comparing(MembroCargo::cargo)
            .thenComparing(MembroCargo::membro, Comparator.comparing(Membro::getNome));

    private final GestaoController controller = new GestaoController();
    private final Navegador navegador;
    private Gestao gestao;

    public PainelGerenciarGestao(Gestao gestao, Navegador navegador) {
        super(gestao.getNome(), new ModeloTabela<MembroCargo>()
                .coluna("Cargo", String.class, mc -> mc.cargo().getNome())
                .coluna("Nível", Integer.class, mc -> mc.cargo().getOrdem())
                .coluna("Membro", String.class, mc -> mc.membro().getNome())
                .coluna("Matrícula", String.class, mc -> mc.membro().getMatricula())
                .coluna("Contato", String.class, mc -> mc.membro().getContato()),
                EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
        this.gestao = gestao;
        this.navegador = navegador;
        exibirVoltar(navegador::voltar);
        renomearBotao(Acao.INCLUIR, "+ Adicionar membro");
        renomearBotao(Acao.EDITAR, "Trocar membro");
        renomearBotao(Acao.EXCLUIR, "Remover");
        adicionarBotao(Botoes.contornoVerde("Organograma", e -> navegador.abrir(new PainelOrganograma(gestao, navegador))));
        larguraColuna(1, 60);
    }

    /** Lê a composição de novo do banco, para refletir alterações feitas em outras telas. */
    @Override
    protected List<MembroCargo> buscarDados() throws AthletizaException {
        Gestao atualizada = new Gestao(gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim());
        atualizada.setId(gestao.getId());
        controller.carregarComposicao(atualizada);
        gestao = atualizada;
        return gestao.getComposicao().stream().sorted(POR_CARGO).toList();
    }

    @Override
    protected void incluir() {
        try {
            JComboBox<Cargo> cargo = new JComboBox<>(new CargoController().listar().toArray(Cargo[]::new));
            JComboBox<Membro> membro = new JComboBox<>(new MembroController().listarAtivos().toArray(Membro[]::new));
            JPanel campos = new JPanel(new GridLayout(4, 1, 0, 6));
            campos.add(new JLabel("Cargo"));
            campos.add(cargo);
            campos.add(new JLabel("Membro"));
            campos.add(membro);
            if (JOptionPane.showConfirmDialog(this, campos, "Adicionar membro à " + gestao.getNome(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                controller.adicionarMembro(gestao, (Membro) membro.getSelectedItem(), (Cargo) cargo.getSelectedItem());
                carregar();
            }
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    /** "Trocar membro": escolhe quem passa a ocupar o cargo selecionado. */
    @Override
    protected void editar(MembroCargo ocupacao) {
        try {
            JComboBox<Membro> novo = new JComboBox<>(new MembroController().listarAtivos().toArray(Membro[]::new));
            novo.setSelectedItem(ocupacao.membro());
            JPanel campos = new JPanel(new GridLayout(2, 1, 0, 6));
            campos.add(new JLabel("Novo ocupante do cargo " + ocupacao.cargo().getNome()));
            campos.add(novo);
            if (JOptionPane.showConfirmDialog(this, campos, "Trocar membro", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                controller.trocarMembro(gestao, ocupacao.cargo(), ocupacao.membro(), (Membro) novo.getSelectedItem());
                carregar();
            }
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    @Override
    protected String getPerguntaExclusao(MembroCargo ocupacao) {
        return "Remover " + ocupacao.membro().getNome() + " do cargo " + ocupacao.cargo().getNome() + "?";
    }

    @Override
    protected void excluir(MembroCargo ocupacao) {
        try {
            controller.removerMembro(gestao, ocupacao.membro(), ocupacao.cargo());
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
