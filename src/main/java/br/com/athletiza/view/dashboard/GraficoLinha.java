package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Comparativo;
import br.com.athletiza.model.Comparativo.Serie;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gráfico de linhas: uma linha por série, com um ponto por categoria (mês).
 * Valores nulos interrompem a linha. Com uma série só, a área abaixo da linha é preenchida.
 * Passar o mouse sobre um mês mostra os valores de todas as séries naquele mês.
 */
class GraficoLinha extends Grafico {

    private static final int ESQUERDA = 36;
    private static final int RODAPE = 24;
    private static final int ALTURA = 230;

    private final Map<String, Color> coresPorSerie;
    /** Em modo percentual o eixo vai de 0 a 100% e os valores levam "%". */
    private final boolean percentual;
    private Comparativo dados = new Comparativo(List.of(), List.of());

    GraficoLinha(Map<String, Color> coresPorSerie, boolean percentual) {
        this.coresPorSerie = coresPorSerie;
        this.percentual = percentual;
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

    private boolean temLegenda() {
        return dados.series().size() > 1;
    }

    private int topo() {
        return temLegenda() ? ALTURA_LEGENDA + 8 : 8;
    }

    private int maximo() {
        if (percentual) {
            return 100;
        }
        int maior = dados.series().stream().flatMap(s -> s.valores().stream())
                .filter(v -> v != null).mapToInt(Integer::intValue).max().orElse(0);
        return topoDoEixo(maior);
    }

    private double xDe(int categoria) {
        int n = dados.categorias().size();
        double largura = getWidth() - ESQUERDA - 12;
        return n == 1 ? ESQUERDA + largura / 2 : ESQUERDA + 6 + categoria * (largura - 12) / (n - 1);
    }

    private double yDe(int valor) {
        double altura = getHeight() - topo() - RODAPE;
        return topo() + altura - altura * valor / maximo();
    }

    @Override
    protected void desenhar(Graphics2D g2) {
        if (dados.categorias().isEmpty()) {
            return;
        }
        if (temLegenda()) {
            List<Color> cores = new ArrayList<>();
            for (int i = 0; i < dados.series().size(); i++) {
                cores.add(cor(i));
            }
            desenharLegenda(g2, ESQUERDA, 0, dados.series().stream().map(Serie::nome).toList(), cores);
        }
        desenharEixos(g2);
        boolean area = dados.series().size() == 1;
        for (int s = 0; s < dados.series().size(); s++) {
            desenharSerie(g2, dados.series().get(s), cor(s), area);
        }
    }

    private void desenharEixos(Graphics2D g2) {
        int maximo = maximo();
        g2.setFont(Tema.fonte(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();
        int partes = divisoes(maximo);
        for (int i = 0; i <= partes; i++) {
            int valor = maximo * i / partes;
            int y = (int) Math.round(yDe(valor));
            g2.setColor(GRADE);
            g2.drawLine(ESQUERDA, y, getWidth() - 4, y);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String texto = valor + (percentual ? "%" : "");
            g2.drawString(texto, ESQUERDA - 8 - fm.stringWidth(texto), y + 4);
        }
        // rótulos dos meses: pula alguns quando não cabem
        int n = dados.categorias().size();
        int larguraRotulo = fm.stringWidth("mmm/00") + 8;
        int passo = Math.max(1, (int) Math.ceil(n / Math.max(1.0, (getWidth() - ESQUERDA) / (double) larguraRotulo)));
        for (int i = 0; i < n; i += passo) {
            String rotulo = dados.categorias().get(i);
            g2.drawString(rotulo, (int) xDe(i) - fm.stringWidth(rotulo) / 2, getHeight() - 6);
        }
    }

    private void desenharSerie(Graphics2D g2, Serie serie, Color cor, boolean area) {
        Path2D linha = new Path2D.Double();
        boolean aberta = false;
        List<double[]> pontos = new ArrayList<>();
        for (int i = 0; i < serie.valores().size(); i++) {
            Integer valor = serie.valores().get(i);
            if (valor == null) {
                aberta = false;
                continue;
            }
            double x = xDe(i);
            double y = yDe(valor);
            if (aberta) {
                linha.lineTo(x, y);
            } else {
                linha.moveTo(x, y);
                aberta = true;
            }
            pontos.add(new double[]{x, y});
        }
        if (area && pontos.size() > 1 && serie.valores().stream().allMatch(v -> v != null)) {
            Path2D preenchimento = new Path2D.Double(linha);
            preenchimento.lineTo(pontos.get(pontos.size() - 1)[0], yDe(0));
            preenchimento.lineTo(pontos.get(0)[0], yDe(0));
            preenchimento.closePath();
            g2.setPaint(new GradientPaint(0, topo(), new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 90),
                    0, (float) yDe(0), new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 0)));
            g2.fill(preenchimento);
        }
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linha);
        for (double[] ponto : pontos) {
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fillOval((int) ponto[0] - 4, (int) ponto[1] - 4, 9, 9);
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval((int) ponto[0] - 4, (int) ponto[1] - 4, 9, 9);
        }
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        int n = dados.categorias().size();
        if (n == 0 || evento.getX() < ESQUERDA - 10) {
            return null;
        }
        int maisPerto = 0;
        for (int i = 1; i < n; i++) {
            if (Math.abs(xDe(i) - evento.getX()) < Math.abs(xDe(maisPerto) - evento.getX())) {
                maisPerto = i;
            }
        }
        StringBuilder texto = new StringBuilder("<html><b>").append(dados.categorias().get(maisPerto)).append("</b>");
        for (Serie serie : dados.series()) {
            Integer valor = serie.valores().get(maisPerto);
            texto.append("<br>").append(serie.nome()).append(": ")
                    .append(valor == null ? "sem registro" : valor + (percentual ? "%" : ""));
        }
        return texto.append("</html>").toString();
    }
}
