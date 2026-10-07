package br.com.athletiza.util;

import java.awt.Color;

/**
 * Paleta de cores padrão do sistema, conforme o protótipo das telas.
 */
public final class Cores {

    public static final Color VERDE = new Color(0x00FF6A);
    public static final Color ROXO = new Color(0x8A2BE2);
    public static final Color FUNDO = new Color(0x1E1E1E);
    public static final Color CINZA_ESCURO = new Color(0x3A3A3A);
    public static final Color TEXTO = Color.WHITE;

    /* Tons auxiliares derivados da paleta */
    public static final Color FUNDO_MENU = new Color(0x161616);
    public static final Color FUNDO_CARTAO = new Color(0x252525);
    public static final Color TEXTO_SECUNDARIO = new Color(0xA0A0A0);
    public static final Color VERDE_SELECAO = new Color(0x1F3A2A);
    public static final Color AMARELO = new Color(0xE6C229);
    public static final Color VERMELHO = new Color(0xFF5A5A);
    public static final Color TEXTO_SOBRE_VERDE = new Color(0x0B0B0B);

    private Cores() {
    }

    /** Cor no formato #RRGGBB, usada nos estilos do FlatLaf. */
    public static String hex(Color cor) {
        return String.format("#%06X", cor.getRGB() & 0xFFFFFF);
    }
}
