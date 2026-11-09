package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Indicador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Gráfico de barras horizontais desenhado com Graphics2D: uma linha por item,
 * com o nome à esquerda, a barra e o valor à direita. A parte "parcial" do
 * valor (ex.: amistosos, demandas atrasadas) aparece com outra cor.
 * Passar o mouse sobre uma linha mostra o detalhe do item.
 */
class GraficoBarras extends JComponent {

    private static final int ALTURA_LINHA = 30;
    private static final int ALTURA_BARRA = 12;
    private static final int LARGURA_NOME = 150;
    private static final int LARGURA_VALOR = 44;
    private static final int ALTURA_LEGENDA = 26;

    private final Color corValor;
    private final Color corParcial;
    private final String legendaValor;
    private final String legendaParcial;
    private final int maximoItens;
    /** Em modo percentual a escala vai até 100 e a cor da barra indica se a frequência é boa ou ruim. */
    private final boolean percentual;
    private List<Indicador> itens = List.of();

    private GraficoBarras(Color corValor, Color corParcial, String legendaValor, String legendaParcial,
            int maximoItens, boolean percentual) {
        this.corValor = corValor;
        this.corParcial = corParcial;
        this.legendaValor = legendaValor;
        this.legendaParcial = legendaParcial;
        this.maximoItens = maximoItens;
        this.percentual = percentual;
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /** Barras de uma cor só. */
    static GraficoBarras simples(Color cor, int maximoItens) {
        return new GraficoBarras(cor, null, null, null, maximoItens, false);
    }

    /** Barras com duas cores: o valor total e, destacada no fim da barra, a parte parcial. */
    static GraficoBarras composto(Color corValor, String legendaValor, Color corParcial, String legendaParcial,
            int maximoItens) {
        return new GraficoBarras(corValor, corParcial, legendaValor, legendaParcial, maximoItens, false);
    }

    /** Barras de 0 a 100%: verde a partir de 75%, amarelo a partir de 50% e vermelho abaixo. */
    static GraficoBarras percentual(int maximoItens) {
        return new GraficoBarras(Cores.VERDE, null, null, null, maximoItens, true);
    }

    void mostrar(List<Indicador> indicadores) {
        itens = indicadores.subList(0, Math.min(maximoItens, indicadores.size()));
        revalidate();
        repaint();
    }

    private boolean temLegenda() {
        return legendaParcial != null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, (temLegenda() ? ALTURA_LEGENDA : 0) + Math.max(1, itens.size()) * ALTURA_LINHA);
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        int linha = (evento.getY() - (temLegenda() ? ALTURA_LEGENDA : 0)) / ALTURA_LINHA;
        if (evento.getY() < (temLegenda() ? ALTURA_LEGENDA : 0) || linha >= itens.size()) {
            return null;
        }
        Indicador item = itens.get(linha);
        return "<html><b>" + item.nome().replace("<", "&lt;") + "</b><br>" + item.detalhe().replace("<", "&lt;") + "</html>";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int y = 0;
        if (temLegenda()) {
            desenharLegenda(g2);
            y = ALTURA_LEGENDA;
        }
        int maximo = percentual ? 100 : Math.max(1, itens.stream().mapToInt(Indicador::valor).max().orElse(1));
        int inicioBarra = LARGURA_NOME + 10;
        int larguraBarra = Math.max(40, getWidth() - inicioBarra - LARGURA_VALOR - 6);
        for (Indicador item : itens) {
            desenharLinha(g2, item, y, maximo, inicioBarra, larguraBarra);
            y += ALTURA_LINHA;
        }
        g2.dispose();
    }

    private void desenharLinha(Graphics2D g2, Indicador item, int y, int maximo, int x, int largura) {
        g2.setFont(Tema.fonte(Font.PLAIN, 13f));
        FontMetrics fm = g2.getFontMetrics();
        int base = y + (ALTURA_LINHA + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(Cores.TEXTO);
        g2.drawString(cortar(item.nome(), fm, LARGURA_NOME), 0, base);

        int yBarra = y + (ALTURA_LINHA - ALTURA_BARRA) / 2;
        g2.setColor(Cores.CINZA_ESCURO);
        g2.fillRoundRect(x, yBarra, largura, ALTURA_BARRA, ALTURA_BARRA, ALTURA_BARRA);

        int total = item.valor() == 0 ? 0 : Math.max(ALTURA_BARRA, Math.round(largura * (float) item.valor() / maximo));
        Color cor = percentual ? corDoPercentual(item.valor()) : corValor;
        if (total > 0) {
            g2.setColor(cor);
            g2.fillRoundRect(x, yBarra, total, ALTURA_BARRA, ALTURA_BARRA, ALTURA_BARRA);
            if (corParcial != null && item.parcial() > 0) {
                int parcial = Math.max(ALTURA_BARRA, Math.round(total * (float) item.parcial() / item.valor()));
                g2.setColor(corParcial);
                g2.fillRoundRect(x + total - parcial, yBarra, parcial, ALTURA_BARRA, ALTURA_BARRA, ALTURA_BARRA);
                if (parcial < total) {
                    g2.fillRect(x + total - parcial, yBarra, Math.min(ALTURA_BARRA / 2, parcial), ALTURA_BARRA);
                }
            }
        }

        g2.setFont(Tema.fonte(Font.BOLD, 13f));
        g2.setColor(percentual ? cor : Cores.TEXTO);
        String valor = item.valor() + (percentual ? "%" : "");
        g2.drawString(valor, getWidth() - g2.getFontMetrics().stringWidth(valor), base);
    }

    private void desenharLegenda(Graphics2D g2) {
        g2.setFont(Tema.fonte(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int base = (ALTURA_LEGENDA + fm.getAscent() - fm.getDescent()) / 2 - 4;
        int x = 0;
        for (int i = 0; i < 2; i++) {
            g2.setColor(i == 0 ? corValor : corParcial);
            g2.fillRoundRect(x, base - 9, 10, 10, 4, 4);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String texto = i == 0 ? legendaValor : legendaParcial;
            g2.drawString(texto, x + 16, base);
            x += 16 + fm.stringWidth(texto) + 20;
        }
    }

    static Color corDoPercentual(int percentual) {
        return percentual >= 75 ? Cores.VERDE : percentual >= 50 ? Cores.AMARELO : Cores.VERMELHO;
    }

    private static String cortar(String texto, FontMetrics fm, int largura) {
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        String cortado = texto;
        while (!cortado.isEmpty() && fm.stringWidth(cortado + "…") > largura) {
            cortado = cortado.substring(0, cortado.length() - 1);
        }
        return cortado + "…";
    }
}
