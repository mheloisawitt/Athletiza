package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gráfico de rosca (pizza com furo) com a legenda ao lado: nome, quantidade e porcentagem.
 * O total fica no meio. Com mais de 5 fatias, as menores viram "Outros".
 */
public class GraficoPizza extends Grafico {

    private static final int MAXIMO_FATIAS = CORES_SERIES.length;

    private Map<String, Integer> dados = new LinkedHashMap<>();

    public void mostrar(Map<String, Integer> dados) {
        this.dados = agruparExcedentes(dados);
        repaint();
    }

    @Override
    protected boolean temDados() {
        return dados.values().stream().anyMatch(v -> v > 0);
    }

    @Override
    protected void desenhar(Graphics2D g2, int largura, int altura) {
        int total = dados.values().stream().mapToInt(Integer::intValue).sum();
        int diametro = Math.min(altura - 16, largura / 2 - 16);
        int x = 8;
        int y = (altura - diametro) / 2;
        Ellipse2D furo = new Ellipse2D.Double(x + diametro * 0.22, y + diametro * 0.22, diametro * 0.56, diametro * 0.56);

        double inicio = 90;
        int i = 0;
        for (Map.Entry<String, Integer> fatia : dados.entrySet()) {
            double angulo = -360.0 * fatia.getValue() / total;
            Area forma = new Area(new Arc2D.Double(x, y, diametro, diametro, inicio, angulo, Arc2D.PIE));
            forma.subtract(new Area(furo));
            g2.setColor(CORES_SERIES[i % CORES_SERIES.length]);
            g2.fill(forma);
            // Contorno da cor do fundo: 2px de espaço entre as fatias
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(forma);
            areaComDica(forma, fatia.getKey() + ": " + fatia.getValue() + " (" + porcentagem(fatia.getValue(), total) + ")");
            inicio += angulo;
            i++;
        }

        g2.setColor(Cores.TEXTO);
        g2.setFont(Tema.fonte(Font.BOLD, 20f));
        textoCentralizado(g2, String.valueOf(total), x + diametro / 2, y + diametro / 2 - 6);
        g2.setColor(Cores.TEXTO_SECUNDARIO);
        g2.setFont(fontePequena());
        textoCentralizado(g2, "total", x + diametro / 2, y + diametro / 2 + 12);

        // Legenda ao lado
        FontMetrics fm = g2.getFontMetrics();
        int legendaX = x + diametro + 20;
        int linha = 22;
        int legendaY = (altura - dados.size() * linha) / 2;
        i = 0;
        for (Map.Entry<String, Integer> fatia : dados.entrySet()) {
            int topo = legendaY + i * linha;
            g2.setColor(CORES_SERIES[i % CORES_SERIES.length]);
            g2.fillRoundRect(legendaX, topo + 4, 10, 10, 3, 3);
            String valor = fatia.getValue() + " · " + porcentagem(fatia.getValue(), total);
            int larguraValor = fm.stringWidth(valor);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(abreviar(g2, fatia.getKey(), largura - legendaX - 30 - larguraValor), legendaX + 16, topo + 13);
            g2.setColor(Cores.TEXTO);
            g2.drawString(valor, largura - 6 - larguraValor, topo + 13);
            i++;
        }
    }

    private static String porcentagem(int valor, int total) {
        return Math.round(valor * 100f / total) + "%";
    }

    /** Mantém as 4 maiores fatias e soma as demais em "Outros". */
    private static Map<String, Integer> agruparExcedentes(Map<String, Integer> dados) {
        if (dados.size() <= MAXIMO_FATIAS) {
            return dados;
        }
        List<Map.Entry<String, Integer>> ordenadas = new ArrayList<>(dados.entrySet());
        ordenadas.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        Map<String, Integer> agrupado = new LinkedHashMap<>();
        int outros = 0;
        for (int i = 0; i < ordenadas.size(); i++) {
            if (i < MAXIMO_FATIAS - 1) {
                agrupado.put(ordenadas.get(i).getKey(), ordenadas.get(i).getValue());
            } else {
                outros += ordenadas.get(i).getValue();
            }
        }
        agrupado.put("Outros", outros);
        return agrupado;
    }
}
