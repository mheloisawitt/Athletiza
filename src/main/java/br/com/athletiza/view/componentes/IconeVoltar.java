package br.com.athletiza.view.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Seta para a esquerda do botão Voltar, desenhada com Graphics2D na mesma
 * grade de 24 x 24 e no mesmo traço dos ícones do menu lateral.
 */
final class IconeVoltar implements Icon {

    private static final int TAMANHO = 16;

    private final Color cor;

    IconeVoltar(Color cor) {
        this.cor = cor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        g2.scale(TAMANHO / 24.0, TAMANHO / 24.0);
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D seta = new Path2D.Double();
        seta.moveTo(19, 12);
        seta.lineTo(5, 12);
        seta.moveTo(11, 6);
        seta.lineTo(5, 12);
        seta.lineTo(11, 18);
        g2.draw(seta);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return TAMANHO;
    }

    @Override
    public int getIconHeight() {
        return TAMANHO;
    }
}
