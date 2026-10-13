package br.com.athletiza.view.eventos;

import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.PainelTarefas;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Responsáveis e tarefas do evento (CH-41). À esquerda, os membros que organizam
 * o evento; à direita, as tarefas com responsável, prazo e situação. Tarefas
 * atrasadas aparecem em vermelho. As ações ficam no GerenciarEventoController.
 */
public class PainelGerenciarEvento extends JPanel implements Recarregavel {

    private final PainelResponsaveis responsaveis = new PainelResponsaveis();
    private final PainelTarefas tarefas = new PainelTarefas();
    private final JLabel detalhe = new JLabel();
    private final JLabel alerta = new JLabel();
    private final JButton btnVoltar = new JButton("‹ Voltar");
    private final JComboBox<Membro> membro = new JComboBox<>();
    private ActionListener acaoCarregar;

    public PainelGerenciarEvento() {
        super(new BorderLayout(0, 12));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        btnVoltar.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; margin:2,0,2,8");
        JLabel titulo = new JLabel("Responsáveis e tarefas");
        titulo.setFont(Tema.fonteTitulo());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(btnVoltar);
        linha.add(titulo);

        detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
        detalhe.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        alerta.setForeground(Cores.VERMELHO);
        alerta.setFont(Tema.fonte(Font.BOLD, 13f));
        alerta.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{linha, detalhe, alerta}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            cabecalho.add(c);
        }
        add(cabecalho, BorderLayout.NORTH);

        responsaveis.setPreferredSize(new Dimension(410, 0));
        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);
        corpo.add(responsaveis, BorderLayout.WEST);
        corpo.add(tarefas, BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    /* Ações: registradas pelo Controller */

    public void adicionarAcaoCarregar(ActionListener acao) {
        this.acaoCarregar = acao;
    }

    @Override
    public void carregar() {
        if (acaoCarregar != null) {
            acaoCarregar.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "carregar"));
        }
    }

    public void adicionarAcaoVoltar(ActionListener acao) {
        btnVoltar.addActionListener(acao);
    }

    public void adicionarAcaoAdicionarResponsavel(ActionListener acao) {
        responsaveis.adicionarAcaoIncluir(acao);
    }

    public void adicionarAcaoRemoverResponsavel(ActionListener acao) {
        responsaveis.adicionarAcaoExcluir(acao);
    }

    /* Dados exibidos */

    public void exibirCabecalho(Evento evento) {
        detalhe.setText(evento.getTitulo() + "  ·  " + evento.getTipoEvento().getDescricao() + "  ·  "
                + Validador.FORMATO_DATA.format(evento.getData())
                + (evento.getLocal() == null ? "" : "  ·  " + evento.getLocal()));
    }

    public void exibirResponsaveis(List<Membro> membros) {
        responsaveis.exibir(membros);
    }

    /** Mostra o aviso de tarefas atrasadas (some quando não há nenhuma). */
    public void exibirTarefasAtrasadas(int quantidade) {
        alerta.setText(quantidade == 0 ? " " : "Atenção: " + quantidade + " tarefa(s) atrasada(s).");
    }

    public Membro getResponsavelSelecionado() {
        return responsaveis.getSelecionado();
    }

    public PainelTarefas getPainelTarefas() {
        return tarefas;
    }

    /**
     * Diálogo para escolher um membro para a organização do evento.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibirFormularioResponsavel(List<Membro> disponiveis) {
        membro.removeAllItems();
        for (Membro opcao : disponiveis) {
            membro.addItem(opcao);
        }
        return JOptionPane.showConfirmDialog(this, membro, "Adicionar responsável", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public Membro getMembroEscolhido() {
        return (Membro) membro.getSelectedItem();
    }

    /* Mensagens */

    public void mostrarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    public void mostrarErro(Exception erro) {
        Mensagens.erro(this, erro);
    }

    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    /** Membros responsáveis pela organização do evento. */
    private static class PainelResponsaveis extends PainelConsulta<Membro> {

        PainelResponsaveis() {
            super("Responsáveis", new ModeloTabela<Membro>()
                    .coluna("Membro", String.class, m -> m.getNome())
                    .coluna("Contato", String.class, m -> m.getContato()),
                    EnumSet.of(Acao.INCLUIR, Acao.EXCLUIR));
            renomearBotao(Acao.INCLUIR, "+ Adicionar");
            renomearBotao(Acao.EXCLUIR, "Remover");
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        }
    }
}
