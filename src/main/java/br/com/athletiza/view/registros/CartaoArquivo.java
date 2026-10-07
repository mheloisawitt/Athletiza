package br.com.athletiza.view.registros;

import br.com.athletiza.model.Arquivo;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/**
 * Miniatura de um arquivo na galeria: a própria foto (ou um quadro com o
 * símbolo de "play", para vídeos), com o nome e o tamanho abaixo.
 */
class CartaoArquivo extends JComponent {

    static final int LARGURA = 200;
    static final int ALTURA_IMAGEM = 124;

    private final Arquivo arquivo;
    private Image miniatura;
    private boolean selecionado;
    private boolean mouseSobre;

    CartaoArquivo(Arquivo arquivo) {
        this.arquivo = arquivo;
        setPreferredSize(new Dimension(LARGURA, ALTURA_IMAGEM + 50));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("<html><b>" + arquivo.getNome() + "</b><br>" + arquivo.getTipo() + " · " + arquivo.getTamanhoFormatado()
                + (arquivo.getEnviadoEm() == null ? "" : "<br>Enviado em "
                        + Validador.FORMATO_DATA.format(arquivo.getEnviadoEm().toLocalDate())
                        + (arquivo.getEnviadoPor() == null ? "" : " por " + arquivo.getEnviadoPor()))
                + (arquivo.getDescricao() == null ? "" : "<br>" + arquivo.getDescricao()) + "</html>");
    }

    Arquivo getArquivo() {
        return arquivo;
    }

    void setMiniatura(Image miniatura) {
        this.miniatura = miniatura;
        repaint();
    }

    void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
        repaint();
    }

    void setMouseSobre(boolean mouseSobre) {
        this.mouseSobre = mouseSobre;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int largura = getWidth();

        RoundRectangle2D area = new RoundRectangle2D.Float(0, 0, largura - 1, ALTURA_IMAGEM, 12, 12);
        g2.setClip(area);
        if (arquivo.isImagem() && miniatura != null) {
            desenharCobrindo(g2, miniatura, largura, ALTURA_IMAGEM);
        } else if (arquivo.isImagem()) {
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fill(area);
        } else {
            desenharVideo(g2, largura);
        }
        g2.setClip(null);

        g2.setStroke(new BasicStroke(selecionado ? 2.5f : 1f));
        g2.setColor(selecionado ? Cores.VERDE : mouseSobre ? Cores.TEXTO_SECUNDARIO : Cores.CINZA_ESCURO);
        g2.draw(area);

        g2.setFont(Tema.fonte(Font.BOLD, 12f));
        g2.setColor(Cores.TEXTO);
        g2.drawString(cortar(arquivo.getNome(), g2.getFontMetrics(), largura - 4), 2, ALTURA_IMAGEM + 20);
        g2.setFont(Tema.fonte(Font.PLAIN, 11f));
        g2.setColor(Cores.TEXTO_SECUNDARIO);
        g2.drawString(arquivo.getTipo() + " · " + arquivo.getTamanhoFormatado(), 2, ALTURA_IMAGEM + 38);
        g2.dispose();
    }

    /** Desenha a imagem preenchendo a área, cortando as sobras (como "cover" em CSS). */
    private static void desenharCobrindo(Graphics2D g2, Image imagem, int largura, int altura) {
        int w = imagem.getWidth(null);
        int h = imagem.getHeight(null);
        double escala = Math.max((double) largura / w, (double) altura / h);
        int nw = (int) Math.round(w * escala);
        int nh = (int) Math.round(h * escala);
        g2.drawImage(imagem, (largura - nw) / 2, (altura - nh) / 2, nw, nh, null);
    }

    private static void desenharVideo(Graphics2D g2, int largura) {
        g2.setPaint(new GradientPaint(0, 0, new Color(0x2A1840), largura, ALTURA_IMAGEM, new Color(0x10241A)));
        g2.fillRect(0, 0, largura, ALTURA_IMAGEM);
        int raio = 22;
        int cx = largura / 2;
        int cy = ALTURA_IMAGEM / 2;
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillOval(cx - raio, cy - raio, raio * 2, raio * 2);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(cx - raio, cy - raio, raio * 2, raio * 2);
        g2.fillPolygon(new Polygon(new int[]{cx - 6, cx - 6, cx + 10}, new int[]{cy - 10, cy + 10, cy}, 3));
    }

    private static String cortar(String texto, FontMetrics metricas, int largura) {
        if (metricas.stringWidth(texto) <= largura) {
            return texto;
        }
        String fim = "…";
        int tamanho = texto.length();
        while (tamanho > 0 && metricas.stringWidth(texto.substring(0, tamanho) + fim) > largura) {
            tamanho--;
        }
        return texto.substring(0, tamanho) + fim;
    }
}
