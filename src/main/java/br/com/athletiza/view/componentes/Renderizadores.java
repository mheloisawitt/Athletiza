package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Formas de exibir valores nas células das tabelas.
 */
public final class Renderizadores {

    private Renderizadores() {
    }

    /** Registra os renderizadores padrão (datas, horários e situações) em uma tabela. */
    public static void aplicarPadroes(JTable tabela) {
        tabela.setDefaultRenderer(Object.class, new Texto());
        tabela.setDefaultRenderer(String.class, new Texto());
        tabela.setDefaultRenderer(Integer.class, new Texto());
        tabela.setDefaultRenderer(LocalDate.class, new Texto());
        tabela.setDefaultRenderer(LocalTime.class, new Texto());
        tabela.setDefaultRenderer(Enum.class, new Situacao());
    }

    /** Texto com margem interna; datas em dd/mm/aaaa e horários em hh:mm. */
    static class Texto extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, formatar(valor), selecionado, false, linha, coluna);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            return this;
        }
    }

    /** Texto exibido para um valor: datas em dd/mm/aaaa, horários em hh:mm. */
    static String formatar(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof LocalDate data) {
            return Validador.FORMATO_DATA.format(data);
        }
        if (valor instanceof LocalTime hora) {
            return Validador.FORMATO_HORARIO.format(hora);
        }
        return valor.toString();
    }

    /**
     * Situação exibida como etiqueta colorida, como no protótipo
     * ("Em andamento" em verde, "Planejada" em amarelo, "Encerrada" em cinza).
     */
    static class Situacao extends DefaultTableCellRenderer {

        private Color corEtiqueta;

        Situacao() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                boolean foco, int linha, int coluna) {
            JLabel rotulo = (JLabel) super.getTableCellRendererComponent(tabela, valor, selecionado, false, linha, coluna);
            corEtiqueta = valor instanceof Enum<?> constante ? corDa(constante) : null;
            rotulo.setForeground(corEtiqueta == null ? Cores.TEXTO : corEtiqueta);
            rotulo.setFont(rotulo.getFont().deriveFont(12f));
            return rotulo;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (corEtiqueta != null && getText() != null && !getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int largura = g2.getFontMetrics(getFont()).stringWidth(getText()) + 24;
                int altura = 22;
                int x = (getWidth() - largura) / 2;
                int y = (getHeight() - altura) / 2;
                g2.setColor(new Color(corEtiqueta.getRed(), corEtiqueta.getGreen(), corEtiqueta.getBlue(), 40));
                g2.fillRoundRect(x, y, largura, altura, altura, altura);
                g2.setColor(corEtiqueta);
                g2.drawRoundRect(x, y, largura - 1, altura - 1, altura, altura);
                g2.dispose();
            }
            super.paintComponent(g);
        }

        private static Color corDa(Enum<?> situacao) {
            String nome = situacao.name().toUpperCase(Locale.ROOT);
            if (nome.equals("ATIVO") || nome.contains("ANDAMENTO") || nome.startsWith("CONFIRMAD")) {
                return Cores.VERDE;
            }
            if (nome.startsWith("PLANEJAD") || nome.equals("PENDENTE") || nome.equals("LESIONADO")
                    || nome.equals("AFASTADO")) {
                return Cores.AMARELO;
            }
            if (nome.startsWith("CANCELAD")) {
                return Cores.VERMELHO;
            }
            return Cores.TEXTO_SECUNDARIO;
        }
    }
}
