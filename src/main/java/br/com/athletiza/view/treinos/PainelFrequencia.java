package br.com.athletiza.view.treinos;

import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Histórico de frequência dos atletas nos treinos (CH-37), com filtro de
 * modalidade e período. A busca por texto filtra pelo nome do atleta.
 * As ações ficam no FrequenciaController.
 */
public class PainelFrequencia extends PainelConsulta<Frequencia> {

    /** Períodos do filtro. */
    public enum Periodo {
        TRINTA_DIAS("Últimos 30 dias", 30), NOVENTA_DIAS("Últimos 90 dias", 90),
        ESTE_ANO("Este ano", -1), TUDO("Todo o período", 0);

        private final String texto;
        private final int dias;

        Periodo(String texto, int dias) {
            this.texto = texto;
            this.dias = dias;
        }

        /** Primeiro dia do período, ou null para "Todo o período". */
        public LocalDate inicio(LocalDate hoje) {
            if (dias > 0) {
                return hoje.minusDays(dias);
            }
            if (dias < 0) {
                return hoje.withDayOfYear(1); // este ano
            }
            return null;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final JComboBox<Modalidade> filtroModalidade =
            Combos.comOpcaoTodos(new ArrayList<Modalidade>(), "Todas as modalidades");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());
    private final JButton btnHistorico = Botoes.contornoVerde("Ver treinos do atleta", null);
    private ActionListener acaoFiltrar;
    private boolean atualizandoFiltros;

    public PainelFrequencia() {
        super("Frequência nos treinos", new ModeloTabela<Frequencia>()
                .coluna("Atleta", String.class, f -> f.getAtleta().getNome())
                .coluna("Treinos", Integer.class, f -> f.getTreinos())
                .coluna("Presenças", Integer.class, f -> f.getPresencas())
                .coluna("Faltas", Integer.class, f -> f.getFaltas())
                .coluna("Frequência", Integer.class, f -> f.getPercentual()),
                EnumSet.noneOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
        adicionarBotao(btnHistorico);
        filtroPeriodo.setSelectedItem(Periodo.ESTE_ANO);
        adicionarFiltro(filtroModalidade);
        adicionarFiltro(filtroPeriodo);
        filtroModalidade.addActionListener(e -> filtrar());
        filtroPeriodo.addActionListener(e -> filtrar());
        tabela.getColumnModel().getColumn(4).setCellRenderer(new BarraPercentual());
        larguraColuna(0, 260);
        larguraColuna(4, 260);
    }

    public void adicionarAcaoFiltrar(ActionListener acao) {
        this.acaoFiltrar = acao;
    }

    /** "Ver treinos do atleta" e o clique duplo abrem o histórico do atleta selecionado. */
    public void adicionarAcaoVerHistorico(ActionListener acao) {
        btnHistorico.addActionListener(e -> {
            if (getSelecionado() == null) {
                mostrarAviso("Selecione um atleta na tabela.");
            } else {
                acao.actionPerformed(e);
            }
        });
        adicionarAcaoClicarDuasVezes(acao);
    }

    /** Troca as opções do filtro de modalidade, mantendo a selecionada (sem disparar o filtro). */
    public void setModalidades(List<Modalidade> modalidades) {
        atualizandoFiltros = true;
        try {
            Combos.atualizarItens(filtroModalidade, modalidades);
        } finally {
            atualizandoFiltros = false;
        }
    }

    /** Modalidade escolhida, ou null para todas. */
    public Modalidade getModalidade() {
        return (Modalidade) filtroModalidade.getSelectedItem();
    }

    public Periodo getPeriodo() {
        return (Periodo) filtroPeriodo.getSelectedItem();
    }

    private void filtrar() {
        if (!atualizandoFiltros && acaoFiltrar != null) {
            acaoFiltrar.actionPerformed(null);
        }
    }

    /** Percentual exibido como barra: verde a partir de 75%, amarelo a partir de 50%, vermelho abaixo. */
    private static class BarraPercentual extends JPanel implements TableCellRenderer {

        private final JProgressBar barra = new JProgressBar(0, 100);
        private final JLabel texto = new JLabel();

        BarraPercentual() {
            super(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            barra.setPreferredSize(new Dimension(140, 8));
            JPanel centro = new JPanel(new GridBagLayout());
            centro.setOpaque(false);
            centro.add(barra);
            texto.setPreferredSize(new Dimension(40, 10));
            add(texto, BorderLayout.WEST);
            add(centro, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado, boolean foco,
                int linha, int coluna) {
            int percentual = valor == null ? 0 : (Integer) valor;
            Color cor;
            if (percentual >= 75) {
                cor = Cores.VERDE;
            } else if (percentual >= 50) {
                cor = Cores.AMARELO;
            } else {
                cor = Cores.VERMELHO;
            }
            barra.setValue(percentual);
            barra.putClientProperty(FlatClientProperties.STYLE, "foreground:" + Cores.hex(cor)
                    + "; background:" + Cores.hex(Cores.CINZA_ESCURO) + "; arc:8");
            texto.setText(percentual + "%");
            texto.setForeground(cor);
            setBackground(selecionado ? tabela.getSelectionBackground() : tabela.getBackground());
            return this;
        }
    }
}
