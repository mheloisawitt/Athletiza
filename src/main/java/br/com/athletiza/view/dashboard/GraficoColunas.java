package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Comparativo;
import br.com.athletiza.model.Comparativo.Serie;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gráfico de colunas empilhadas: uma coluna por categoria, dividida nas séries
 * (ex.: presenças e faltas de cada mês; 1º, 2º e 3º lugares de cada modalidade).
 * O total aparece acima de cada coluna.
 */
class GraficoColunas extends Grafico {

    private static final int ESQUERDA = 36;
    private static final int RODAPE = 26;
    private static final int ALTURA = 240;

    private final Map<String, Color> coresPorSerie;
    private Comparativo dados = new Comparativo(List.of(), List.of());

    GraficoColunas(Map<String, Color> coresPorSerie) {
        this.coresPorSerie = coresPorSerie;
    }

    void mostrar(Comparativo comparativo) {
        dados = comparativo;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, ALTURA);
    }

    private Color cor(int serie) {
        return coresPorSerie.getOrDefault(dados.series().get(serie).nome(), PALETA.get(serie % PALETA.size()));
    }

    private int total(int categoria) {
        return dados.series().stream().mapToInt(s -> s.valor(categoria)).sum();
    }

    private int topo() {
        return ALTURA_LEGENDA + 22;
    }

    private double larguraFaixa() {
        return (getWidth() - ESQUERDA - 4) / (double) Math.max(1, dados.categorias().size());
    }

    @Override
    protected void desenhar(Graphics2D g2) {
        int n = dados.categorias().size();
        if (n == 0) {
            return;
        }
        List<Color> cores = new ArrayList<>();
        for (int i = 0; i < dados.series().size(); i++) {
            cores.add(cor(i));
        }
        desenharLegenda(g2, ESQUERDA, 0, dados.series().stream().map(Serie::nome).toList(), cores);

        int maior = 0;
        for (int i = 0; i < n; i++) {
            maior = Math.max(maior, total(i));
        }
        int maximo = topoDoEixo(maior);
        double altura = getHeight() - topo() - RODAPE;
        int base = getHeight() - RODAPE;

        g2.setFont(Tema.fonte(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();
        int partes = divisoes(maximo);
        for (int i = 0; i <= partes; i++) {
            int valor = maximo * i / partes;
            int y = (int) Math.round(base - altura * valor / maximo);
            g2.setColor(GRADE);
            g2.drawLine(ESQUERDA, y, getWidth() - 4, y);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String texto = String.valueOf(valor);
            g2.drawString(texto, ESQUERDA - 8 - fm.stringWidth(texto), y + 4);
        }

        double faixa = larguraFaixa();
        // rótulos: pula alguns quando não cabem inteiros
        int larguraRotulo = dados.categorias().stream().mapToInt(fm::stringWidth).max().orElse(0) + 8;
        int passo = Math.max(1, (int) Math.ceil(larguraRotulo / faixa));
        int largura = (int) Math.max(8, Math.min(46, faixa * 0.6));
        for (int i = 0; i < n; i++) {
            int x = (int) (ESQUERDA + 2 + faixa * i + (faixa - largura) / 2);
            double y = base;
            for (int s = 0; s < dados.series().size(); s++) {
                int valor = dados.series().get(s).valor(i);
                if (valor == 0) {
                    continue;
                }
                double h = altura * valor / maximo;
                g2.setColor(cor(s));
                g2.fillRect(x, (int) Math.round(y - h), largura, (int) Math.round(h));
                y -= h;
            }
            int total = total(i);
            if (total > 0) {
                g2.setColor(Cores.TEXTO);
                g2.setFont(Tema.fonte(Font.BOLD, 11f));
                String texto = String.valueOf(total);
                g2.drawString(texto, x + (largura - g2.getFontMetrics().stringWidth(texto)) / 2, (int) y - 5);
                g2.setFont(Tema.fonte(Font.PLAIN, 11f));
            }
            if (i % passo != 0) {
                continue;
            }
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String rotulo = cortar(dados.categorias().get(i), fm, (int) (faixa * passo) - 4);
            g2.drawString(rotulo, (int) (ESQUERDA + 2 + faixa * i + (faixa - fm.stringWidth(rotulo)) / 2), getHeight() - 8);
        }
    }

    private static String cortar(String texto, FontMetrics fm, int largura) {
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        String cortado = texto;
        while (cortado.length() > 1 && fm.stringWidth(cortado + "…") > largura) {
            cortado = cortado.substring(0, cortado.length() - 1);
        }
        return cortado + "…";
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        int n = dados.categorias().size();
        int i = (int) ((evento.getX() - ESQUERDA - 2) / larguraFaixa());
        if (n == 0 || evento.getX() < ESQUERDA || i < 0 || i >= n) {
            return null;
        }
        StringBuilder texto = new StringBuilder("<html><b>").append(dados.categorias().get(i).replace("<", "&lt;"))
                .append("</b>");
        for (Serie serie : dados.series()) {
            texto.append("<br>").append(serie.nome()).append(": ").append(serie.valor(i));
        }
        return texto.append("<br>Total: ").append(total(i)).append("</html>").toString();
    }
}
