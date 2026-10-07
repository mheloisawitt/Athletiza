package br.com.athletiza.view.calendario;

import br.com.athletiza.model.Atividade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComponent;

/**
 * Quadrado de um dia no calendário: número do dia e as atividades,
 * cada uma com a cor do seu tipo (CH-15).
 */
class CelulaDia extends JComponent {

    private static final int MAXIMO_VISIVEL = 3;

    private LocalDate data;
    private boolean doMesAtual;
    private boolean hoje;
    private boolean selecionada;
    private boolean mouseSobre;
    private List<Atividade> atividades = List.of();

    CelulaDia() {
        setPreferredSize(new Dimension(110, 86));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void atualizar(LocalDate data, boolean doMesAtual, boolean hoje, boolean selecionada, List<Atividade> atividades) {
        this.data = data;
        this.doMesAtual = doMesAtual;
        this.hoje = hoje;
        this.selecionada = selecionada;
        this.atividades = atividades;
        setToolTipText(atividades.isEmpty() ? null : "<html>" + atividades.stream()
                .map(a -> a.getDescricaoCalendario().replace("<", "&lt;"))
                .collect(Collectors.joining("<br>")) + "</html>");
        repaint();
    }

    void setMouseSobre(boolean mouseSobre) {
        this.mouseSobre = mouseSobre;
        repaint();
    }

    LocalDate getData() {
        return data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(selecionada ? Cores.VERDE_SELECAO : mouseSobre ? new Color(0x2C2C2C) : Cores.FUNDO_CARTAO);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Cores.CINZA_ESCURO);
        g2.drawLine(w - 1, 0, w - 1, h);
        g2.drawLine(0, h - 1, w, h - 1);
        if (selecionada) {
            g2.setColor(Cores.VERDE);
            g2.drawRect(0, 0, w - 2, h - 2);
        }
        if (data == null) {
            g2.dispose();
            return;
        }

        String numero = String.valueOf(data.getDayOfMonth());
        g2.setFont(Tema.fonte(Font.BOLD, 12f));
        FontMetrics metricas = g2.getFontMetrics();
        if (hoje) {
            g2.setColor(Cores.VERDE);
            g2.fillOval(5, 5, 22, 22);
            g2.setColor(Cores.TEXTO_SOBRE_VERDE);
        } else {
            g2.setColor(doMesAtual ? Cores.TEXTO : new Color(0x5A5A5A));
        }
        g2.drawString(numero, 16 - metricas.stringWidth(numero) / 2, 16 + metricas.getAscent() / 2 - 1);

        g2.setFont(Tema.fonte(Font.PLAIN, 11f));
        metricas = g2.getFontMetrics();
        int y = 34;
        int alturaLinha = metricas.getHeight() + 1;
        int visiveis = atividades.size() > MAXIMO_VISIVEL ? MAXIMO_VISIVEL - 1 : atividades.size();
        for (int i = 0; i < visiveis && y + alturaLinha <= h; i++) {
            Atividade atividade = atividades.get(i);
            Color cor = Color.decode(atividade.getCorHex());
            if (!doMesAtual) {
                cor = cor.darker().darker();
            }
            g2.setColor(cor);
            g2.fillOval(7, y + 4, 6, 6);
            g2.drawString(cortar(atividade.getRotuloCurto(), metricas, w - 24), 17, y + metricas.getAscent());
            y += alturaLinha;
        }
        if (atividades.size() > visiveis) {
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString("+" + (atividades.size() - visiveis) + " mais", 17, y + metricas.getAscent());
        }
        g2.dispose();
    }

    private static String cortar(String texto, FontMetrics metricas, int largura) {
        if (texto == null || metricas.stringWidth(texto) <= largura) {
            return texto == null ? "" : texto;
        }
        String reticencias = "…";
        int fim = texto.length();
        while (fim > 0 && metricas.stringWidth(texto.substring(0, fim) + reticencias) > largura) {
            fim--;
        }
        return texto.substring(0, fim) + reticencias;
    }
}
