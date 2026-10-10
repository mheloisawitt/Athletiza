package br.com.athletiza.view.eventos;

import br.com.athletiza.controller.EventoController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.PainelTarefas;
import br.com.athletiza.view.componentes.Recarregavel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
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

/**
 * Responsáveis e tarefas do evento (CH-41). À esquerda, os membros que organizam
 * o evento; à direita, as tarefas com responsável, prazo e situação. Tarefas
 * atrasadas aparecem em vermelho.
 */
public class PainelGerenciarEvento extends JPanel implements Recarregavel {

    private final EventoController controller = new EventoController();
    private final Evento original;
    private final PainelResponsaveis responsaveis = new PainelResponsaveis();
    private final PainelTarefas tarefas;
    private final JLabel alerta = new JLabel();

    public PainelGerenciarEvento(Evento evento, Navegador navegador) {
        super(new BorderLayout(0, 12));
        this.original = evento;
        this.tarefas = new PainelTarefas(new PainelTarefas.FonteTarefas() {
            @Override
            public List<Tarefa> listar() throws AthletizaException {
                return eventoAtualizado().getTarefas().stream().sorted().toList();
            }

            @Override
            public void salvar(Tarefa tarefa) throws AthletizaException {
                controller.salvarTarefa(original, tarefa);
            }

            @Override
            public void excluir(Tarefa tarefa) throws AthletizaException {
                controller.excluirTarefa(tarefa);
            }
        });
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

    /** Lê o evento novamente do banco, com responsáveis e tarefas. */
    private Evento eventoAtualizado() throws PersistenciaException {
        Evento evento = new Evento(original.getTitulo(), original.getTipoEvento(), original.getData(),
                original.getHorario(), original.getLocal());
        evento.setId(original.getId());
        controller.carregarDetalhes(evento);
        int atrasadas = evento.getTarefasAtrasadas(LocalDate.now()).size();
        alerta.setText(atrasadas == 0 ? " " : "Atenção: " + atrasadas + " tarefa(s) atrasada(s).");
        return evento;
    }

    /** Membros responsáveis pela organização do evento. */
    private class PainelResponsaveis extends PainelConsulta<Membro> {

        private Evento evento;

        PainelResponsaveis() {
            super("Responsáveis", new ModeloTabela<Membro>()
                    .coluna("Membro", String.class, Membro::getNome)
                    .coluna("Contato", String.class, Membro::getContato),
                    EnumSet.of(Acao.INCLUIR, Acao.EXCLUIR));
            renomearBotao(Acao.INCLUIR, "+ Adicionar");
            renomearBotao(Acao.EXCLUIR, "Remover");
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        }

        @Override
        protected List<Membro> buscarDados() throws AthletizaException {
            evento = eventoAtualizado();
            return List.copyOf(evento.getResponsaveis());
        }

        @Override
        protected void incluir() {
            try {
                List<Membro> disponiveis = new MembroController().listarAtivos().stream()
                        .filter(m -> !evento.getResponsaveis().contains(m)).toList();
                if (disponiveis.isEmpty()) {
                    Mensagens.aviso(this, "Todos os membros ativos já são responsáveis por este evento.");
                    return;
                }
                JComboBox<Membro> membro = new JComboBox<>(disponiveis.toArray(Membro[]::new));
                if (JOptionPane.showConfirmDialog(this, membro, "Adicionar responsável", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    controller.adicionarResponsavel(evento, (Membro) membro.getSelectedItem());
                    carregar();
                }
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
        }

        @Override
        protected String getPerguntaExclusao(Membro membro) {
            return "Remover " + membro.getNome() + " dos responsáveis pelo evento?";
        }

        @Override
        protected void excluir(Membro membro) {
            try {
                controller.removerResponsavel(evento, membro);
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }
}
