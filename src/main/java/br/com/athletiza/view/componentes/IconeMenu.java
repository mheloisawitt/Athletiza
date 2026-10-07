package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * Ícones de linha do menu lateral, desenhados com Graphics2D (sem arquivos de imagem).
 * Cada desenho usa uma grade de 24 x 24 e é reduzido para o tamanho do ícone.
 * A cor acompanha o texto do item: verde quando selecionado e cinza claro nos demais.
 */
public enum IconeMenu implements Icon {

    /** Casa: página inicial com o calendário. */
    INICIO(() -> List.of(
            caminho(3, 11, 12, 3, 21, 11),
            caminho(5, 10, 5, 20, 10, 20, 10, 14, 14, 14, 14, 20, 19, 20, 19, 10))),

    /** Organograma: a gestão e seus cargos. */
    GESTAO(() -> List.of(
            circulo(12, 6, 3),
            circulo(5, 18, 2.5),
            circulo(19, 18, 2.5),
            new Line2D.Double(12, 9, 12, 12.5),
            caminho(5, 15.5, 5, 12.5, 19, 12.5, 19, 15.5))),

    /** Duas pessoas: os atletas. */
    ATLETAS(() -> {
        Path2D corpo = new Path2D.Double();
        corpo.moveTo(2.5, 20);
        corpo.curveTo(2.5, 16, 5.5, 14, 9, 14);
        corpo.curveTo(12.5, 14, 15.5, 16, 15.5, 20);
        Path2D ombro = new Path2D.Double();
        ombro.moveTo(17.5, 14.2);
        ombro.curveTo(19.8, 15, 21.5, 16.8, 21.5, 20);
        return List.of(circulo(9, 7.5, 3.5), corpo,
                new Arc2D.Double(12.5, 4, 7, 7, -90, 180, Arc2D.OPEN), ombro);
    }),

    /** Troféu: as competições. */
    COMPETICOES(() -> {
        Path2D taca = new Path2D.Double();
        taca.moveTo(7, 3.5);
        taca.lineTo(17, 3.5);
        taca.lineTo(17, 9);
        taca.append(new Arc2D.Double(7, 4, 10, 10, 0, -180, Arc2D.OPEN), true);
        taca.closePath();
        Path2D alcaEsquerda = new Path2D.Double();
        alcaEsquerda.moveTo(7, 5.5);
        alcaEsquerda.lineTo(3.5, 5.5);
        alcaEsquerda.lineTo(3.5, 7.5);
        alcaEsquerda.quadTo(3.5, 11, 7.3, 11);
        Path2D alcaDireita = new Path2D.Double();
        alcaDireita.moveTo(17, 5.5);
        alcaDireita.lineTo(20.5, 5.5);
        alcaDireita.lineTo(20.5, 7.5);
        alcaDireita.quadTo(20.5, 11, 16.7, 11);
        return List.of(taca, alcaEsquerda, alcaDireita,
                new Line2D.Double(12, 14, 12, 18), new Line2D.Double(8, 20.5, 16, 20.5));
    }),

    /** Cronômetro: os treinos e seus horários. */
    TREINOS(() -> List.of(
            circulo(12, 13.5, 7.5),
            new Line2D.Double(12, 13.5, 12, 9.5),
            new Line2D.Double(12, 13.5, 14.5, 15),
            new Line2D.Double(9.5, 2.5, 14.5, 2.5),
            new Line2D.Double(12, 2.5, 12, 6),
            new Line2D.Double(18, 7, 19.8, 5.2))),

    /** Calendário com estrela: os eventos da atlética. */
    EVENTOS(() -> List.of(
            new RoundRectangle2D.Double(3, 4.5, 18, 16.5, 4, 4),
            new Line2D.Double(3, 9.5, 21, 9.5),
            new Line2D.Double(8, 2.5, 8, 6.5),
            new Line2D.Double(16, 2.5, 16, 6.5),
            estrela(12, 15.3, 3.6, 1.6))),

    /** Câmera: a galeria de fotos e vídeos. */
    REGISTROS(() -> List.of(
            new RoundRectangle2D.Double(2.5, 7, 19, 13, 4, 4),
            caminho(8, 7, 9.5, 4.5, 14.5, 4.5, 16, 7),
            circulo(12, 13.5, 3.5),
            circulo(18, 10, 0.4))),

    /** Pessoa com escudo: usuários e permissões de acesso. */
    USUARIOS(() -> {
        Path2D corpo = new Path2D.Double();
        corpo.moveTo(2.5, 21);
        corpo.curveTo(2.5, 17, 5.5, 14, 9.5, 14);
        corpo.curveTo(11, 14, 12.3, 14.4, 13.4, 15);
        Path2D escudo = new Path2D.Double();
        escudo.moveTo(18, 12.5);
        escudo.lineTo(21.5, 14);
        escudo.lineTo(21.5, 16.8);
        escudo.quadTo(21.5, 20, 18, 21.5);
        escudo.quadTo(14.5, 20, 14.5, 16.8);
        escudo.lineTo(14.5, 14);
        escudo.closePath();
        return List.of(circulo(9.5, 7.5, 4), corpo, escudo);
    });

    private static final int TAMANHO = 18;
    private static final Color COR_NORMAL = new Color(0xD0D0D0);

    private final Supplier<List<Shape>> desenho;

    IconeMenu(Supplier<List<Shape>> desenho) {
        this.desenho = desenho;
    }

    /** Ícone do item do menu com esse nome, ou null se o item não tiver ícone. */
    public static IconeMenu doItem(String nome) {
        return switch (nome) {
            case "Início" -> INICIO;
            case "Gestão" -> GESTAO;
            case "Atletas" -> ATLETAS;
            case "Competições" -> COMPETICOES;
            case "Treinos" -> TREINOS;
            case "Eventos" -> EVENTOS;
            case "Registros" -> REGISTROS;
            case "Usuários" -> USUARIOS;
            default -> null;
        };
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        g2.scale(TAMANHO / 24.0, TAMANHO / 24.0);
        g2.setColor(cor(c));
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        desenho.get().forEach(g2::draw);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return TAMANHO;
    }

    @Override
    public int getIconHeight() {
        return TAMANHO;
    }

    private static Color cor(Component c) {
        return c instanceof AbstractButton botao && botao.isSelected() ? Cores.VERDE : COR_NORMAL;
    }

    private static Shape circulo(double cx, double cy, double raio) {
        return new Ellipse2D.Double(cx - raio, cy - raio, raio * 2, raio * 2);
    }

    /** Linha que passa pelos pontos (x1, y1, x2, y2, ...). */
    private static Shape caminho(double... pontos) {
        Path2D linha = new Path2D.Double();
        linha.moveTo(pontos[0], pontos[1]);
        for (int i = 2; i < pontos.length; i += 2) {
            linha.lineTo(pontos[i], pontos[i + 1]);
        }
        return linha;
    }

    private static Shape estrela(double cx, double cy, double raioExterno, double raioInterno) {
        Path2D estrela = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double raio = i % 2 == 0 ? raioExterno : raioInterno;
            double angulo = Math.toRadians(-90 + i * 36);
            double px = cx + raio * Math.cos(angulo);
            double py = cy + raio * Math.sin(angulo);
            if (i == 0) {
                estrela.moveTo(px, py);
            } else {
                estrela.lineTo(px, py);
            }
        }
        estrela.closePath();
        return estrela;
    }
}
