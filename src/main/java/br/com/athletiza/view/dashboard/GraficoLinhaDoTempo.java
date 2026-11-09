package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Atividade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Linha do tempo dos próximos dias: um eixo de hoje até o último dia, com um
 * marcador por atividade nas cores do calendário. Atividades do mesmo dia ficam
 * empilhadas; os títulos alternam acima e abaixo do eixo para não se sobreporem.
 */
class GraficoLinhaDoTempo extends Grafico {

    private static final int ALTURA = 200;
    private static final int MARGEM = 24;
    private static final int RAIO = 7;

    private final int dias;
    private LocalDate hoje = LocalDate.now();
    private List<Atividade> atividades = List.of();
    /** Posição de cada marcador desenhado, para a dica do mouse. */
    private final List<Marcador> marcadores = new ArrayList<>();

    private record Marcador(int x, int y, Atividade atividade) {
    }

    GraficoLinhaDoTempo(int dias) {
        this.dias = dias;
    }

    void mostrar(LocalDate hoje, List<Atividade> proximas) {
        this.hoje = hoje;
        this.atividades = proximas;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, ALTURA);
    }

    private double xDe(LocalDate data) {
        double largura = getWidth() - 2.0 * MARGEM;
        return MARGEM + largura * ChronoUnit.DAYS.between(hoje, data) / dias;
    }

    @Override
    protected void desenhar(Graphics2D g2) {
        marcadores.clear();
        int eixo = getHeight() / 2;
        g2.setFont(Tema.fonte(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(Cores.CINZA_ESCURO);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(MARGEM, eixo, getWidth() - MARGEM, eixo);
        // marcações a cada semana
        for (int dia = 0; dia <= dias; dia += 7) {
            LocalDate data = hoje.plusDays(dia);
            int x = (int) xDe(data);
            g2.setColor(GRADE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(x, 14, x, getHeight() - 18);
            g2.setColor(dia == 0 ? Cores.VERDE : Cores.TEXTO_SECUNDARIO);
            String rotulo = dia == 0 ? "Hoje" : data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            g2.drawString(rotulo, x - fm.stringWidth(rotulo) / 2, getHeight() - 4);
        }

        Map<LocalDate, List<Atividade>> porDia = new TreeMap<>();
        atividades.forEach(a -> porDia.computeIfAbsent(a.getData(), d -> new ArrayList<>()).add(a));
        int indice = 0;
        for (Map.Entry<LocalDate, List<Atividade>> dia : porDia.entrySet()) {
            int x = (int) xDe(dia.getKey());
            boolean acima = indice++ % 2 == 0;
            List<Atividade> doDia = dia.getValue();
            for (int i = 0; i < doDia.size(); i++) {
                Atividade atividade = doDia.get(i);
                Color cor = Color.decode(atividade.getCorHex());
                int y = eixo + (acima ? -1 : 1) * i * (RAIO * 2 + 2);
                g2.setColor(Cores.FUNDO_CARTAO);
                g2.fillOval(x - RAIO - 2, y - RAIO - 2, RAIO * 2 + 4, RAIO * 2 + 4);
                g2.setColor(cor);
                g2.fillOval(x - RAIO, y - RAIO, RAIO * 2, RAIO * 2);
                marcadores.add(new Marcador(x, y, atividade));
            }
            // título da primeira atividade do dia (e quantas mais houver)
            String titulo = doDia.get(0).getTitulo() + (doDia.size() > 1 ? " +" + (doDia.size() - 1) : "");
            titulo = cortar(titulo, fm, 120);
            int pilha = (doDia.size() - 1) * (RAIO * 2 + 2);
            int yTexto = acima ? eixo - RAIO - pilha - 10 : eixo + RAIO + pilha + 18;
            g2.setColor(Cores.TEXTO);
            int xTexto = Math.max(2, Math.min(getWidth() - fm.stringWidth(titulo) - 2, x - fm.stringWidth(titulo) / 2));
            g2.drawString(titulo, xTexto, yTexto);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            String data = dia.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            g2.drawString(data, x - fm.stringWidth(data) / 2, acima ? yTexto - 14 : yTexto + 14);
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
        StringBuilder texto = new StringBuilder();
        for (Marcador marcador : marcadores) {
            if (Math.abs(marcador.x() - evento.getX()) <= RAIO + 2 && Math.abs(marcador.y() - evento.getY()) <= RAIO + 2) {
                Atividade a = marcador.atividade();
                texto.append(texto.length() == 0 ? "<html>" : "<br>")
                        .append("<b>").append(a.getTitulo().replace("<", "&lt;")).append("</b><br>")
                        .append(a.getTipo().getDescricao()).append(" em ").append(Validador.FORMATO_DATA.format(a.getData()))
                        .append(a.getHorario() == null ? "" : " às " + a.getHorario());
            }
        }
        return texto.length() == 0 ? null : texto.append("</html>").toString();
    }
}
