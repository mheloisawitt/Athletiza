package br.com.athletiza.visao.eventos;

import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.Tema;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import br.com.athletiza.visao.componentes.PainelTarefas;
import br.com.athletiza.visao.componentes.Recarregavel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PainelGerenciarEvento extends JPanel implements Recarregavel {
    private final Navegador navegador;
    private final PainelResponsaveis responsaveis;
    private final PainelTarefas tarefas;
    private final JLabel alerta = new JLabel();

    public PainelGerenciarEvento(Evento evento, Navegador navegador) {
        super(new BorderLayout(0, 12));
        this.navegador = navegador;
        this.responsaveis = new PainelResponsaveis(navegador);
        this.tarefas = new PainelTarefas(navegador);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JButton voltar = Botoes.voltar(e -> navegador.voltar());
        JLabel titulo = new JLabel("Responsáveis e tarefas");
        titulo.setFont(Tema.fonteTitulo());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(voltar);
        linha.add(Box.createHorizontalStrut(12));
        linha.add(titulo);

        JLabel detalhe = new JLabel(evento.getTitulo() + "  ·  " + evento.getTipoEvento().getDescricao() + "  ·  "
                + Validador.FORMATO_DATA.format(evento.getData())
                + (evento.getLocal() == null ? "" : "  ·  " + evento.getLocal()));
        detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
        detalhe.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        alerta.setForeground(Cores.VERMELHO);
        alerta.setFont(Tema.fonte(Font.BOLD, 13f));
        alerta.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{linha, detalhe, alerta}) {
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

    @Override
    public void carregar() {
        responsaveis.carregar();
        tarefas.carregar();
    }

    public PainelResponsaveis getResponsaveis() {
        return responsaveis;
    }

    public PainelTarefas getTarefas() {
        return tarefas;
    }

    public void apresentarAtrasadas(int atrasadas) {
        alerta.setText(atrasadas == 0 ? " " : "Atenção: " + atrasadas + " tarefa(s) atrasada(s).");
    }

    public void apresentarTela() {
        navegador.abrir(this);
    }

    public static class PainelResponsaveis extends PainelConsulta<Membro> {

        PainelResponsaveis(Navegador navegador) {
            super("Responsáveis", new ModeloTabela<Membro>()
                    .coluna("Membro", String.class, Membro::getNome)
                    .coluna("Contato", String.class, Membro::getContato),
                    EnumSet.of(Acao.INCLUIR, Acao.EXCLUIR), navegador);
            renomearBotao(Acao.INCLUIR, "+ Adicionar");
            renomearBotao(Acao.EXCLUIR, "Remover");
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        }

        public Membro pedirResponsavel(List<Membro> disponiveis) {
            JComboBox<Membro> membro = new JComboBox<>(disponiveis.toArray(new Membro[0]));
            if (JOptionPane.showConfirmDialog(this, membro, "Adicionar responsável", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return null;
            }
            return (Membro) membro.getSelectedItem();
        }

        @Override
        protected String getPerguntaExclusao(Membro membro) {
            return "Remover " + membro.getNome() + " dos responsáveis pelo evento?";
        }
    }
}
