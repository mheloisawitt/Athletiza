package br.com.athletiza.view.calendario;

import br.com.athletiza.model.Alerta;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Alertas da tela inicial (RF24): tarefas atrasadas (vermelho), treinos sem lista
 * de presença (amarelo) e competições na próxima semana (verde). Some quando não há alertas.
 */
class PainelAlertas extends JPanel {

    private static final int MAXIMO_VISIVEL = 4;

    PainelAlertas() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    void mostrar(List<Alerta> alertas) {
        removeAll();
        setVisible(!alertas.isEmpty());
        if (alertas.isEmpty()) {
            return;
        }
        JLabel titulo = new JLabel("Alertas (" + alertas.size() + ")");
        titulo.setFont(Tema.fonte(Font.BOLD, 15f));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(8));
        for (Alerta alerta : alertas.subList(0, Math.min(MAXIMO_VISIVEL, alertas.size()))) {
            Color cor = corDo(alerta.tipo());
            JLabel item = new JLabel("<html><div style='width:188px'>" + alerta.texto().replace("<", "&lt;") + "</div></html>");
            item.setFont(Tema.fonte(Font.PLAIN, 12f));
            item.setForeground(cor);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, cor), BorderFactory.createEmptyBorder(2, 8, 2, 0)));
            item.setAlignmentX(LEFT_ALIGNMENT);
            add(item);
            add(Box.createVerticalStrut(6));
        }
        if (alertas.size() > MAXIMO_VISIVEL) {
            JLabel mais = new JLabel("+ " + (alertas.size() - MAXIMO_VISIVEL) + " outro(s)");
            mais.setForeground(Cores.TEXTO_SECUNDARIO);
            mais.setToolTipText("<html>" + alertas.subList(MAXIMO_VISIVEL, alertas.size()).stream()
                    .map(a -> a.texto().replace("<", "&lt;")).collect(Collectors.joining("<br>")) + "</html>");
            mais.setAlignmentX(LEFT_ALIGNMENT);
            add(mais);
        }
        add(Box.createVerticalStrut(10));
        revalidate();
        repaint();
    }

    private static Color corDo(Alerta.Tipo tipo) {
        return switch (tipo) {
            case TAREFA_ATRASADA -> Cores.VERMELHO;
            case PRESENCA_PENDENTE -> Cores.AMARELO;
            case COMPETICAO_PROXIMA -> Cores.VERDE;
        };
    }
}
