package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Base dos gráficos do dashboard: fundo transparente, desenho suavizado,
 * dica ao passar o mouse e legenda com quadradinhos coloridos.
 */
abstract class Grafico extends JComponent {

    /** Paleta para séries sem cor própria, na ordem de uso. */
    static final List<Color> PALETA = List.of(Cores.VERDE, Cores.ROXO, new Color(0xFF4FD8), new Color(0x3D9BFF),
            Cores.AMARELO, new Color(0xFF8A3D), new Color(0x2ED3C6), Cores.TEXTO_SECUNDARIO);

    static final Color GRADE = new Color(0x333333);
    static final int ALTURA_LEGENDA = 24;

    Grafico() {
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    protected final void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        desenhar(g2);
        g2.dispose();
    }

    protected abstract void desenhar(Graphics2D g2);

    /** Legenda em uma linha a partir de (x, y); devolve a altura usada. */
    static int desenharLegenda(Graphics2D g2, int x, int y, List<String> nomes, List<Color> cores) {
        g2.setFont(Tema.fonte(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int base = y + (ALTURA_LEGENDA + fm.getAscent() - fm.getDescent()) / 2 - 2;
        for (int i = 0; i < nomes.size(); i++) {
            g2.setColor(cores.get(i));
            g2.fillRoundRect(x, base - 9, 10, 10, 4, 4);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(nomes.get(i), x + 16, base);
            x += 16 + fm.stringWidth(nomes.get(i)) + 18;
        }
        return ALTURA_LEGENDA;
    }

    static String html(String titulo, String detalhe) {
        return "<html><b>" + titulo.replace("<", "&lt;") + "</b><br>" + detalhe.replace("<", "&lt;") + "</html>";
    }

    /** Em quantas partes dividir o eixo para as marcações saírem redondas (ex.: 10 -> 5 partes de 2). */
    static int divisoes(int maximo) {
        if (maximo <= 5) {
            return Math.max(1, maximo);
        }
        for (int partes : new int[]{4, 5, 2}) {
            if (maximo % partes == 0) {
                return partes;
            }
        }
        return 1;
    }

    /** Valor "bonito" para o topo do eixo (1, 2, 5, 10, 20, 25, 50...), pelo menos igual a {@code maximo}. */
    static int topoDoEixo(int maximo) {
        if (maximo <= 4) {
            return Math.max(1, maximo);
        }
        int potencia = 1;
        while (potencia * 10 < maximo) {
            potencia *= 10;
        }
        for (int passo : new int[]{1, 2, 4, 5, 10}) {
            if (passo * potencia >= maximo) {
                return passo * potencia;
            }
        }
        return 10 * potencia;
    }
}
