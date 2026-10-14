package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Recarregavel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Base das abas do dashboard: as linhas de cartões ficam uma embaixo da outra,
 * com rolagem. Cada aba monta seus cartões no construtor e preenche os gráficos em atualizar().
 */
abstract class AbaDashboard extends JPanel implements Recarregavel {

    protected final DashboardController controller = new DashboardController();
    private final JPanel linhas = new JPanel();

    protected AbaDashboard() {
        super(new BorderLayout());
        setOpaque(false);
        linhas.setLayout(new BoxLayout(linhas, BoxLayout.Y_AXIS));
        linhas.setOpaque(false);
        linhas.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 4));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(linhas, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(topo);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        rolagem.getViewport().setBackground(Cores.FUNDO);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);
    }

    /** Adiciona uma linha com os cartões lado a lado e a devolve. */
    protected JPanel adicionarLinha(JComponent... cartoes) {
        if (linhas.getComponentCount() > 0) {
            linhas.add(Box.createVerticalStrut(16));
        }
        JPanel linha = Cartoes.linha(cartoes);
        linhas.add(linha);
        return linha;
    }

    /** Busca os dados e preenche os gráficos desta aba. */
    protected abstract void atualizar(LocalDate hoje) throws PersistenciaException;

    @Override
    public void carregar() {
        try {
            atualizar(LocalDate.now());
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }
}
