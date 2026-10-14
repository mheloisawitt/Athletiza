package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Base dos gráficos do dashboard, desenhados com Graphics2D (sem bibliotecas externas).
 *
 * Cada gráfico filho implementa desenhar(...) e, para cada barra, fatia ou ponto,
 * chama areaComDica(...) informando o texto que aparece ao passar o mouse.
 */
public abstract class Grafico extends JComponent {

    /**
     * Cores das séries, sempre usadas nesta ordem. Testadas para o fundo escuro dos
     * cartões e para daltonismo (cores vizinhas continuam diferentes).
     */
    public static final Color[] CORES_SERIES = {
        new Color(0x1FA85A), // verde
        new Color(0x9B6BF2), // roxo
        new Color(0xB88600), // amarelo
        new Color(0x2F86D6), // azul
        new Color(0xD65398)  // rosa
    };

    /** Linhas de grade e eixos: discretas, para não competir com os dados. */
    protected static final Color COR_GRADE = new Color(0x333333);

    /** Região de uma barra, fatia ou ponto e o texto da dica dela. */
    private record AreaComDica(Shape area, String texto) {
    }

    private final List<AreaComDica> dicas = new ArrayList<>();

    protected Grafico() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 230));
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /** Indica se há algo para desenhar; se não houver, aparece "Sem dados". */
    protected abstract boolean temDados();

    protected abstract void desenhar(Graphics2D g2, int largura, int altura);

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        dicas.clear();
        if (temDados()) {
            desenhar(g2, getWidth(), getHeight());
        } else {
            g2.setFont(Tema.fonte(Font.PLAIN, 13f));
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            textoCentralizado(g2, "Sem dados para exibir", getWidth() / 2, getHeight() / 2);
        }
        g2.dispose();
    }

    /** Registra a região (barra, fatia, ponto) que mostra o texto ao passar o mouse. */
    protected void areaComDica(Shape area, String texto) {
        dicas.add(new AreaComDica(area, texto));
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        // Procura do fim para o começo: o último desenhado fica por cima
        for (int i = dicas.size() - 1; i >= 0; i--) {
            if (dicas.get(i).area().contains(evento.getPoint())) {
                return dicas.get(i).texto();
            }
        }
        return null;
    }

    /* ===================== Auxiliares de desenho ===================== */

    protected static Font fontePequena() {
        return Tema.fonte(Font.PLAIN, 11f);
    }

    /** Escreve o texto com o centro no ponto (x, y). */
    protected static void textoCentralizado(Graphics2D g2, String texto, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, x - fm.stringWidth(texto) / 2, y + (fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Corta o texto com "..." para caber na largura. */
    protected static String abreviar(Graphics2D g2, String texto, int larguraMaxima) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(texto) <= larguraMaxima) {
            return texto;
        }
        String cortado = texto;
        while (cortado.length() > 1 && fm.stringWidth(cortado + "...") > larguraMaxima) {
            cortado = cortado.substring(0, cortado.length() - 1);
        }
        return cortado + "...";
    }

    /**
     * Um valor "redondo" e par (2, 4, 6, 8, 10, 20, 40...) maior ou igual ao máximo,
     * para a escala do eixo e a linha do meio terminarem em números fáceis de ler.
     */
    protected static int topoDaEscala(int maximo) {
        if (maximo <= 10) {
            return Math.max(2, maximo + maximo % 2);
        }
        int potencia = 1;
        while (potencia * 10 < maximo) {
            potencia *= 10;
        }
        int passo = 2;
        while (passo * potencia < maximo) {
            passo += 2;
        }
        return passo * potencia;
    }

    /** De quantas em quantas categorias escrever o nome no eixo, para os nomes não se encavalarem. */
    protected static int passoDosRotulos(Graphics2D g2, Iterable<String> rotulos, double larguraCategoria) {
        int maior = 0;
        for (String rotulo : rotulos) {
            maior = Math.max(maior, g2.getFontMetrics().stringWidth(rotulo));
        }
        return Math.max(1, (int) Math.ceil((maior + 6) / larguraCategoria));
    }

    /** Legenda em uma linha: quadradinho colorido + nome de cada série. Devolve a altura usada. */
    protected static int desenharLegenda(Graphics2D g2, String[] nomes, Color[] cores, int x, int y) {
        g2.setFont(fontePequena());
        FontMetrics fm = g2.getFontMetrics();
        int posicao = x;
        for (int i = 0; i < nomes.length; i++) {
            g2.setColor(cores[i]);
            g2.fillRoundRect(posicao, y + 2, 10, 10, 3, 3);
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(nomes[i], posicao + 15, y + 11);
            posicao += 15 + fm.stringWidth(nomes[i]) + 16;
        }
        return 22;
    }
}
