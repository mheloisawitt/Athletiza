package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Colunas verticais (ex.: treinos por mês). Aceita várias séries, lado a lado
 * (agrupadas) ou uma sobre a outra (empilhadas, ex.: vitórias, empates e derrotas).
 *
 * Os dados são um Map categoria -> valores, com um valor para cada série, na
 * mesma ordem dos nomes das séries.
 */
public class GraficoColunas extends Grafico {

    private final String[] series;
    private final Color[] cores;
    private final boolean empilhado;
    private Map<String, int[]> dados = new LinkedHashMap<>();

    /** Gráfico de uma série só, com a cor informada. */
    public GraficoColunas(String serie, Color cor) {
        this(new String[]{serie}, new Color[]{cor}, false);
    }

    public GraficoColunas(String[] series, Color[] cores, boolean empilhado) {
        this.series = series;
        this.cores = cores;
        this.empilhado = empilhado;
    }

    public void mostrar(Map<String, int[]> dados) {
        this.dados = dados;
        repaint();
    }

    /** Atalho para gráficos de uma série só. */
    public void mostrarSerieUnica(Map<String, Integer> valores) {
        Map<String, int[]> convertidos = new LinkedHashMap<>();
        valores.forEach((categoria, valor) -> convertidos.put(categoria, new int[]{valor}));
        mostrar(convertidos);
    }

    @Override
    protected boolean temDados() {
        return dados.values().stream().anyMatch(valores -> soma(valores) > 0);
    }

    @Override
    protected void desenhar(Graphics2D g2, int largura, int altura) {
        int topo = 4;
        if (series.length > 1) {
            topo += desenharLegenda(g2, series, cores, 34, 0);
        }
        g2.setFont(fontePequena());
        FontMetrics fm = g2.getFontMetrics();
        int esquerda = 34;
        int base = altura - fm.getHeight() - 6;
        int alturaUtil = Math.max(10, base - topo - fm.getHeight());
        int maximo = topoDaEscala(dados.values().stream()
                .mapToInt(v -> empilhado ? soma(v) : maior(v)).max().orElse(1));

        // Grade: zero, metade e topo da escala
        for (int valor : new int[]{0, maximo / 2, maximo}) {
            int y = base - valor * alturaUtil / maximo;
            g2.setColor(COR_GRADE);
            g2.drawLine(esquerda, y, largura - 4, y);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String texto = String.valueOf(valor);
            g2.drawString(texto, esquerda - 6 - fm.stringWidth(texto), y + fm.getAscent() / 2 - 1);
        }

        int quantidade = dados.size();
        double faixa = (largura - esquerda - 4) / (double) quantidade;
        int larguraGrupo = (int) Math.max(4, Math.min(48, faixa * 0.6));
        // Nomes longos (ex.: modalidades) são abreviados; nomes curtos (ex.: meses) pulam de 2 em 2 se não couberem
        int passoRotulo = Math.min(2, passoDosRotulos(g2, dados.keySet(), faixa));
        int i = 0;
        for (Map.Entry<String, int[]> item : dados.entrySet()) {
            int centro = (int) (esquerda + faixa * i + faixa / 2);
            int[] valores = item.getValue();
            if (empilhado) {
                desenharPilha(g2, item.getKey(), valores, centro - larguraGrupo / 2, larguraGrupo, base, alturaUtil, maximo);
            } else {
                int larguraColuna = Math.max(3, (larguraGrupo - 2 * (valores.length - 1)) / valores.length);
                int x = centro - (larguraColuna * valores.length + 2 * (valores.length - 1)) / 2;
                for (int s = 0; s < valores.length; s++) {
                    int alturaColuna = valores[s] * alturaUtil / maximo;
                    g2.setColor(cores[s]);
                    g2.fill(colunaArredondada(x, base - alturaColuna, larguraColuna, alturaColuna, true));
                    areaComDica(new Rectangle(x - 1, topo, larguraColuna + 2, base - topo),
                            item.getKey() + " · " + series[s] + ": " + valores[s]);
                    x += larguraColuna + 2;
                }
                // Número em cima de cada coluna, quando há espaço para ele
                x = centro - (larguraColuna * valores.length + 2 * (valores.length - 1)) / 2;
                for (int valor : valores) {
                    if (valor > 0 && larguraColuna + 2 >= fm.stringWidth(String.valueOf(valor))) {
                        g2.setColor(Cores.TEXTO);
                        textoCentralizado(g2, String.valueOf(valor), x + larguraColuna / 2, base - valor * alturaUtil / maximo - 8);
                    }
                    x += larguraColuna + 2;
                }
            }
            if (i % passoRotulo == 0) {
                g2.setColor(Cores.TEXTO_SECUNDARIO);
                int espacoRotulo = (int) (faixa * passoRotulo) - 4;
                textoCentralizado(g2, abreviar(g2, item.getKey(), espacoRotulo), centro, base + fm.getHeight() / 2 + 4);
            }
            i++;
        }
    }

    /** Segmentos de baixo para cima, com 2px de espaço entre eles; o total vai em cima. */
    private void desenharPilha(Graphics2D g2, String categoria, int[] valores, int x, int larguraColuna,
            int base, int alturaUtil, int maximo) {
        int y = base;
        int ultimoComValor = -1;
        for (int s = 0; s < valores.length; s++) {
            if (valores[s] > 0) {
                ultimoComValor = s;
            }
        }
        StringBuilder dica = new StringBuilder("<html><b>" + categoria + "</b>");
        for (int s = 0; s < valores.length; s++) {
            dica.append("<br>").append(series[s]).append(": ").append(valores[s]);
            if (valores[s] == 0) {
                continue;
            }
            int alturaSegmento = Math.max(2, valores[s] * alturaUtil / maximo);
            int espaco = s == ultimoComValor ? 0 : 2;
            g2.setColor(cores[s]);
            g2.fill(colunaArredondada(x, y - alturaSegmento, larguraColuna, alturaSegmento - espaco, s == ultimoComValor));
            y -= alturaSegmento;
        }
        if (soma(valores) > 0) {
            g2.setColor(Cores.TEXTO);
            textoCentralizado(g2, String.valueOf(soma(valores)), x + larguraColuna / 2, y - 8);
        }
        areaComDica(new Rectangle(x - 4, 0, larguraColuna + 8, base), dica.append("</html>").toString());
    }

    /** Retângulo com os cantos de cima arredondados (a base fica reta, apoiada no eixo). */
    private static Path2D colunaArredondada(int x, int y, int larguraColuna, int alturaColuna, boolean arredondar) {
        double raio = arredondar ? Math.min(4, Math.min(larguraColuna / 2.0, alturaColuna)) : 0;
        Path2D forma = new Path2D.Double();
        forma.moveTo(x, y + alturaColuna);
        forma.lineTo(x, y + raio);
        forma.quadTo(x, y, x + raio, y);
        forma.lineTo(x + larguraColuna - raio, y);
        forma.quadTo(x + larguraColuna, y, x + larguraColuna, y + raio);
        forma.lineTo(x + larguraColuna, y + alturaColuna);
        forma.closePath();
        return forma;
    }

    private static int soma(int[] valores) {
        int total = 0;
        for (int valor : valores) {
            total += valor;
        }
        return total;
    }

    private static int maior(int[] valores) {
        int maior = 0;
        for (int valor : valores) {
            maior = Math.max(maior, valor);
        }
        return maior;
    }
}
