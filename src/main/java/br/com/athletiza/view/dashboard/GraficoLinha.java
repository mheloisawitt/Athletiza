package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Linha de uma série ao longo do tempo (ex.: novos cadastros por mês).
 * Cada ponto mostra o valor ao passar o mouse; o maior valor aparece escrito.
 */
public class GraficoLinha extends Grafico {

    private final Color cor;
    private final String unidade;
    private Map<String, Integer> dados = new LinkedHashMap<>();

    /**
     * @param unidade usado na dica do ponto (ex.: "atleta(s)")
     */
    public GraficoLinha(Color cor, String unidade) {
        this.cor = cor;
        this.unidade = unidade;
    }

    public void mostrar(Map<String, Integer> dados) {
        this.dados = dados;
        repaint();
    }

    @Override
    protected boolean temDados() {
        return dados.size() > 1;
    }

    @Override
    protected void desenhar(Graphics2D g2, int largura, int altura) {
        g2.setFont(fontePequena());
        FontMetrics fm = g2.getFontMetrics();
        int esquerda = 34;
        int topo = 18;
        int base = altura - fm.getHeight() - 6;
        int alturaUtil = Math.max(10, base - topo);
        int maiorValor = dados.values().stream().max(Integer::compare).orElse(0);
        int maximo = topoDaEscala(maiorValor);

        for (int valor : new int[]{0, maximo / 2, maximo}) {
            int y = base - valor * alturaUtil / maximo;
            g2.setColor(COR_GRADE);
            g2.drawLine(esquerda, y, largura - 4, y);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String texto = String.valueOf(valor);
            g2.drawString(texto, esquerda - 6 - fm.stringWidth(texto), y + fm.getAscent() / 2 - 1);
        }

        double faixa = (largura - esquerda - 4) / (double) dados.size();
        List<int[]> pontos = new ArrayList<>();
        List<String> rotulos = new ArrayList<>(dados.keySet());
        List<Integer> valores = new ArrayList<>(dados.values());
        for (int i = 0; i < valores.size(); i++) {
            pontos.add(new int[]{(int) (esquerda + faixa * i + faixa / 2), base - valores.get(i) * alturaUtil / maximo});
        }

        // Área sombreada embaixo da linha
        Path2D area = new Path2D.Double();
        area.moveTo(pontos.get(0)[0], base);
        pontos.forEach(p -> area.lineTo(p[0], p[1]));
        area.lineTo(pontos.get(pontos.size() - 1)[0], base);
        area.closePath();
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 40));
        g2.fill(area);

        Path2D linha = new Path2D.Double();
        linha.moveTo(pontos.get(0)[0], pontos.get(0)[1]);
        pontos.forEach(p -> linha.lineTo(p[0], p[1]));
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linha);

        boolean maiorJaEscrito = false;
        // A cada quantos meses escrever o nome no eixo, para não encavalar
        int passoRotulo = passoDosRotulos(g2, dados.keySet(), faixa);
        for (int i = 0; i < pontos.size(); i++) {
            int[] p = pontos.get(i);
            // Anel da cor do fundo em volta do ponto, para separá-lo da linha
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fill(new Ellipse2D.Double(p[0] - 6, p[1] - 6, 12, 12));
            g2.setColor(cor);
            g2.fill(new Ellipse2D.Double(p[0] - 4, p[1] - 4, 8, 8));
            if (!maiorJaEscrito && valores.get(i) == maiorValor && maiorValor > 0) {
                g2.setColor(Cores.TEXTO);
                textoCentralizado(g2, String.valueOf(maiorValor), p[0], p[1] - 12);
                maiorJaEscrito = true;
            }
            if (i % passoRotulo == 0) {
                g2.setColor(Cores.TEXTO_SECUNDARIO);
                textoCentralizado(g2, rotulos.get(i), p[0], base + fm.getHeight() / 2 + 4);
            }
            areaComDica(new Rectangle((int) (p[0] - faixa / 2), 0, (int) Math.ceil(faixa), altura),
                    rotulos.get(i) + ": " + valores.get(i) + " " + unidade);
        }
    }
}
