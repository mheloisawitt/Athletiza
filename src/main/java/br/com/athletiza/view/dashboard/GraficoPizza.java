package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Indicador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.util.List;
import java.util.Map;

/**
 * Gráfico de pizza (ou de rosca, com o total no meio) desenhado com Graphics2D.
 * A legenda à direita mostra cada fatia com o valor e o percentual.
 */
class GraficoPizza extends Grafico {

    private static final int DIAMETRO = 150;
    private static final int ALTURA_ITEM = 22;

    private final boolean rosca;
    /** Cor de cada nome (ex.: "Ativo" verde); nomes sem cor usam a paleta. */
    private final Map<String, Color> coresPorNome;
    private final String unidade;
    private List<Indicador> fatias = List.of();

    GraficoPizza(boolean rosca, Map<String, Color> coresPorNome, String unidade) {
        this.rosca = rosca;
        this.coresPorNome = coresPorNome;
        this.unidade = unidade;
    }

    void mostrar(List<Indicador> indicadores) {
        fatias = indicadores.stream().filter(i -> i.valor() > 0).toList();
        revalidate();
        repaint();
    }

    boolean isVazio() {
        return fatias.isEmpty();
    }

    private int total() {
        return fatias.stream().mapToInt(Indicador::valor).sum();
    }

    Color cor(int indice) {
        return coresPorNome.getOrDefault(fatias.get(indice).nome(), PALETA.get(indice % PALETA.size()));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, Math.max(DIAMETRO + 8, fatias.size() * ALTURA_ITEM));
    }

    @Override
    protected void desenhar(Graphics2D g2) {
        int total = total();
        int y0 = (getHeight() - DIAMETRO) / 2;
        double angulo = 90;
        for (int i = 0; i < fatias.size(); i++) {
            double extensao = -360.0 * fatias.get(i).valor() / total;
            g2.setColor(cor(i));
            g2.fill(new Arc2D.Double(4, y0, DIAMETRO, DIAMETRO, angulo, extensao, Arc2D.PIE));
            angulo += extensao;
        }
        // separa as fatias com o fundo do cartão
        g2.setColor(Cores.FUNDO_CARTAO);
        g2.setStroke(new BasicStroke(2f));
        angulo = 90;
        for (Indicador fatia : fatias) {
            if (fatias.size() > 1) {
                g2.draw(new Arc2D.Double(4, y0, DIAMETRO, DIAMETRO, angulo, 0, Arc2D.PIE));
            }
            angulo -= 360.0 * fatia.valor() / total;
        }
        if (rosca) {
            int furo = DIAMETRO * 58 / 100;
            g2.fillOval(4 + (DIAMETRO - furo) / 2, y0 + (DIAMETRO - furo) / 2, furo, furo);
            g2.setFont(Tema.fonte(Font.BOLD, 24f));
            FontMetrics fm = g2.getFontMetrics();
            String texto = String.valueOf(total);
            g2.setColor(Cores.TEXTO);
            g2.drawString(texto, 4 + (DIAMETRO - fm.stringWidth(texto)) / 2, y0 + DIAMETRO / 2 + 4);
            g2.setFont(Tema.fonte(Font.PLAIN, 11f));
            fm = g2.getFontMetrics();
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(unidade, 4 + (DIAMETRO - fm.stringWidth(unidade)) / 2, y0 + DIAMETRO / 2 + 20);
        }
        desenharLegendaLateral(g2, total);
    }

    private void desenharLegendaLateral(Graphics2D g2, int total) {
        int x = DIAMETRO + 28;
        int y = (getHeight() - fatias.size() * ALTURA_ITEM) / 2;
        g2.setFont(Tema.fonte(Font.PLAIN, 13f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < fatias.size(); i++) {
            Indicador fatia = fatias.get(i);
            int base = y + (ALTURA_ITEM + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(cor(i));
            g2.fillRoundRect(x, base - 10, 11, 11, 4, 4);
            g2.setColor(Cores.TEXTO);
            g2.drawString(fatia.nome(), x + 18, base);
            String valor = fatia.valor() + "  (" + Math.round(fatia.valor() * 100f / total) + "%)";
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(valor, getWidth() - fm.stringWidth(valor), base);
            y += ALTURA_ITEM;
        }
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        if (fatias.isEmpty()) {
            return null;
        }
        int total = total();
        int indice = -1;
        int cx = 4 + DIAMETRO / 2;
        int cy = getHeight() / 2;
        double dx = evento.getX() - cx;
        double dy = evento.getY() - cy;
        if (Math.hypot(dx, dy) <= DIAMETRO / 2.0 && (!rosca || Math.hypot(dx, dy) >= DIAMETRO * 0.29)) {
            // ângulo a partir do topo, no sentido horário (como as fatias são desenhadas)
            double graus = (Math.toDegrees(Math.atan2(dx, -dy)) + 360) % 360;
            double acumulado = 0;
            for (int i = 0; i < fatias.size(); i++) {
                acumulado += 360.0 * fatias.get(i).valor() / total;
                if (graus <= acumulado) {
                    indice = i;
                    break;
                }
            }
        } else if (evento.getX() > DIAMETRO + 20) {
            int linha = (evento.getY() - (getHeight() - fatias.size() * ALTURA_ITEM) / 2) / ALTURA_ITEM;
            indice = linha >= 0 && linha < fatias.size() ? linha : -1;
        }
        if (indice < 0) {
            return null;
        }
        Indicador fatia = fatias.get(indice);
        return html(fatia.nome(), fatia.detalhe() + " (" + Math.round(fatia.valor() * 100f / total) + "% do total)");
    }
}
