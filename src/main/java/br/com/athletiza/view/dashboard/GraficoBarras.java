package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Barras horizontais de uma série, uma por categoria (ex.: atletas por modalidade).
 * O nome fica à esquerda e o valor no fim da barra, na ordem do Map recebido.
 */
public class GraficoBarras extends Grafico {

    private final Color cor;
    /** Texto depois do valor (ex.: "%"). */
    private final String sufixo;
    /** Valor que preenche a barra inteira (ex.: 100 para porcentagem); 0 = o maior valor. */
    private final int maximoFixo;
    private Map<String, Integer> dados = new LinkedHashMap<>();

    public GraficoBarras(Color cor) {
        this(cor, "", 0);
    }

    public GraficoBarras(Color cor, String sufixo, int maximoFixo) {
        this.cor = cor;
        this.sufixo = sufixo;
        this.maximoFixo = maximoFixo;
    }

    public void mostrar(Map<String, Integer> dados) {
        this.dados = dados;
        repaint();
    }

    @Override
    protected boolean temDados() {
        return dados.values().stream().anyMatch(v -> v > 0);
    }

    @Override
    protected void desenhar(Graphics2D g2, int largura, int altura) {
        g2.setFont(fontePequena());
        FontMetrics fm = g2.getFontMetrics();
        int maximo = maximoFixo > 0 ? maximoFixo : dados.values().stream().max(Integer::compare).orElse(1);
        int larguraNomes = Math.min(150, largura / 3);
        int larguraValores = fm.stringWidth(maximo + sufixo) + 8;
        int inicioBarras = larguraNomes + 10;
        int larguraUtil = Math.max(10, largura - inicioBarras - larguraValores);

        int linhas = dados.size();
        int alturaLinha = Math.min(34, (altura - 8) / linhas);
        int espessura = Math.max(6, Math.min(18, alturaLinha - 8));
        int y = 4;
        for (Map.Entry<String, Integer> item : dados.entrySet()) {
            int centro = y + alturaLinha / 2;
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String nome = abreviar(g2, item.getKey(), larguraNomes);
            g2.drawString(nome, larguraNomes - fm.stringWidth(nome), centro + fm.getAscent() / 2 - 1);

            int comprimento = Math.max(item.getValue() > 0 ? 4 : 0, item.getValue() * larguraUtil / Math.max(1, maximo));
            // Trilho: mostra quanto falta para o máximo (útil na porcentagem)
            if (maximoFixo > 0) {
                g2.setColor(COR_GRADE);
                g2.fill(new RoundRectangle2D.Double(inicioBarras, centro - espessura / 2.0, larguraUtil, espessura, 4, 4));
            }
            g2.setColor(cor);
            g2.fill(new RoundRectangle2D.Double(inicioBarras, centro - espessura / 2.0, comprimento, espessura, 4, 4));
            g2.setColor(Cores.TEXTO);
            g2.drawString(item.getValue() + sufixo, inicioBarras + comprimento + 6, centro + fm.getAscent() / 2 - 1);

            areaComDica(new Rectangle(0, y, largura, alturaLinha), item.getKey() + ": " + item.getValue() + sufixo);
            y += alturaLinha;
        }
    }
}
