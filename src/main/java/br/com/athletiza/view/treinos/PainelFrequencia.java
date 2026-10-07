package br.com.athletiza.view.treinos;

import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.controller.TreinoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Histórico de frequência dos atletas nos treinos (CH-37), com filtro de
 * modalidade e período. A busca por texto filtra pelo nome do atleta.
 */
public class PainelFrequencia extends PainelConsulta<Frequencia> {

    /** Períodos do filtro. */
    private enum Periodo {
        TRINTA_DIAS("Últimos 30 dias", 30), NOVENTA_DIAS("Últimos 90 dias", 90),
        ESTE_ANO("Este ano", -1), TUDO("Todo o período", 0);

        private final String texto;
        private final int dias;

        Periodo(String texto, int dias) {
            this.texto = texto;
            this.dias = dias;
        }

        LocalDate inicio(LocalDate hoje) {
            return dias > 0 ? hoje.minusDays(dias) : dias < 0 ? hoje.withDayOfYear(1) : null;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final TreinoController controller = new TreinoController();
    private final JComboBox<Modalidade> filtroModalidade = Combos.comOpcaoTodos(List.of(), "Todas as modalidades");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());
    private boolean atualizandoFiltros;

    public PainelFrequencia(Navegador navegador) {
        super("Frequência nos treinos", new ModeloTabela<Frequencia>()
                .coluna("Atleta", String.class, f -> f.atleta().getNome())
                .coluna("Treinos", Integer.class, Frequencia::treinos)
                .coluna("Presenças", Integer.class, Frequencia::presencas)
                .coluna("Faltas", Integer.class, Frequencia::getFaltas)
                .coluna("Frequência", Integer.class, Frequencia::getPercentual),
                EnumSet.noneOf(Acao.class));
        exibirVoltar(navegador::voltar);
        filtroPeriodo.setSelectedItem(Periodo.ESTE_ANO);
        adicionarFiltro(filtroModalidade);
        adicionarFiltro(filtroPeriodo);
        filtroModalidade.addActionListener(e -> {
            if (!atualizandoFiltros) {
                carregar();
            }
        });
        filtroPeriodo.addActionListener(e -> carregar());
        tabela.getColumnModel().getColumn(4).setCellRenderer(new BarraPercentual());
        larguraColuna(0, 260);
        larguraColuna(4, 260);
    }

    @Override
    protected List<Frequencia> buscarDados() throws AthletizaException {
        atualizandoFiltros = true;
        try {
            Combos.atualizarItens(filtroModalidade, new ModalidadeController().listar());
        } finally {
            atualizandoFiltros = false;
        }
        Periodo periodo = (Periodo) filtroPeriodo.getSelectedItem();
        return controller.frequencia((Modalidade) filtroModalidade.getSelectedItem(),
                periodo.inicio(LocalDate.now()), null);
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
            Color cor = percentual >= 75 ? Cores.VERDE : percentual >= 50 ? Cores.AMARELO : Cores.VERMELHO;
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
