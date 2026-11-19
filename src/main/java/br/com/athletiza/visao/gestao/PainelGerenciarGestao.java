package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.modelo.Gestao;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.MembroCargo;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PainelGerenciarGestao extends PainelConsulta<MembroCargo> {
    private final JButton btnOrganograma = Botoes.contornoVerde("Organograma", null);

    public PainelGerenciarGestao(Gestao gestao, Navegador navegador) {
        super(gestao.getNome(), new ModeloTabela<MembroCargo>()
                .coluna("Cargo", String.class, mc -> mc.cargo().getNome())
                .coluna("Nível", Integer.class, mc -> mc.cargo().getOrdem())
                .coluna("Membro", String.class, mc -> mc.membro().getNome())
                .coluna("Matrícula", String.class, mc -> mc.membro().getMatricula())
                .coluna("Contato", String.class, mc -> mc.membro().getContato()),
                EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR), navegador);
        exibirVoltar(navegador::voltar);
        renomearBotao(Acao.INCLUIR, "+ Adicionar membro");
        renomearBotao(Acao.EDITAR, "Trocar membro");
        renomearBotao(Acao.EXCLUIR, "Remover");
        adicionarBotao(btnOrganograma);
        larguraColuna(1, 60);
    }

    public void adicionarAcaoBtnOrganograma(ActionListener acao) {
        btnOrganograma.addActionListener(acao);
    }

    public MembroCargo pedirNovoMembro(String nomeGestao, List<Cargo> cargos, List<Membro> membros) {
        JComboBox<Cargo> cargo = new JComboBox<>(cargos.toArray(new Cargo[0]));
        JComboBox<Membro> membro = new JComboBox<>(membros.toArray(new Membro[0]));
        JPanel campos = new JPanel(new GridLayout(4, 1, 0, 6));
        campos.add(new JLabel("Cargo"));
        campos.add(cargo);
        campos.add(new JLabel("Membro"));
        campos.add(membro);
        if (JOptionPane.showConfirmDialog(this, campos, "Adicionar membro à " + nomeGestao,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        return new MembroCargo((Membro) membro.getSelectedItem(), (Cargo) cargo.getSelectedItem());
    }

    public Membro pedirNovoOcupante(MembroCargo ocupacao, List<Membro> membros) {
        JComboBox<Membro> novo = new JComboBox<>(membros.toArray(new Membro[0]));
        novo.setSelectedItem(ocupacao.membro());
        JPanel campos = new JPanel(new GridLayout(2, 1, 0, 6));
        campos.add(new JLabel("Novo ocupante do cargo " + ocupacao.cargo().getNome()));
        campos.add(novo);
        if (JOptionPane.showConfirmDialog(this, campos, "Trocar membro", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        return (Membro) novo.getSelectedItem();
    }

    @Override
    protected String getPerguntaExclusao(MembroCargo ocupacao) {
        return "Remover " + ocupacao.membro().getNome() + " do cargo " + ocupacao.cargo().getNome() + "?";
    }
}
