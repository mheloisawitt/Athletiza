package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * Fábrica dos botões padronizados do sistema (CH-03).
 */
public final class Botoes {

    private Botoes() {
    }

    /** Ação principal da tela (Salvar, Entrar): fundo verde. */
    public static JButton primario(String texto, ActionListener acao) {
        return criar(texto, acao, "background:" + Cores.hex(Cores.VERDE)
                + "; foreground:" + Cores.hex(Cores.TEXTO_SOBRE_VERDE)
                + "; hoverBackground:#33FF88; pressedBackground:#00CC55; borderWidth:0; focusWidth:0; font:bold");
    }

    /** Ação de destaque nas consultas (Incluir): fundo roxo. */
    public static JButton destaque(String texto, ActionListener acao) {
        return criar(texto, acao, "background:" + Cores.hex(Cores.ROXO)
                + "; foreground:#FFFFFF; hoverBackground:#9D4EE8; pressedBackground:#7323C0; borderWidth:0; focusWidth:0");
    }

    /** Ação secundária (Editar, Cancelar): apenas contorno cinza. */
    public static JButton contorno(String texto, ActionListener acao) {
        return contorno(texto, Cores.TEXTO, Cores.CINZA_ESCURO, acao);
    }

    /** Ação secundária com destaque (Gerenciar): contorno verde. */
    public static JButton contornoVerde(String texto, ActionListener acao) {
        return contorno(texto, Cores.VERDE, Cores.VERDE, acao);
    }

    /** Ação secundária com destaque roxo (ex.: "+ Nova Pasta"). */
    public static JButton contornoRoxo(String texto, ActionListener acao) {
        return contorno(texto, new Color(0xB98AF0), Cores.ROXO, acao);
    }

    /** Ação perigosa (Excluir): contorno vermelho. */
    public static JButton contornoVermelho(String texto, ActionListener acao) {
        return contorno(texto, Cores.VERMELHO, Cores.VERMELHO, acao);
    }

    /**
     * Botão Voltar das telas abertas sobre outra: redondo, só com a seta,
     * em cinza claro sobre cinza escuro para não competir com as ações principais.
     */
    public static JButton voltar(ActionListener acao) {
        JButton botao = new JButton(new IconeVoltar(new Color(0xC8C8C8)));
        botao.putClientProperty(FlatClientProperties.STYLE, "arc:999; background:" + Cores.hex(Cores.CINZA_ESCURO)
                + "; hoverBackground:#4A4A4A; pressedBackground:#555555; borderWidth:0; focusWidth:0"
                + "; margin:0,0,0,0");
        botao.setPreferredSize(new Dimension(34, 34));
        botao.setToolTipText("Voltar");
        botao.getAccessibleContext().setAccessibleName("Voltar");
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (acao != null) {
            botao.addActionListener(acao);
        }
        return botao;
    }

    private static JButton contorno(String texto, Color corTexto, Color borda, ActionListener acao) {
        return criar(texto, acao, "background:" + Cores.hex(Cores.FUNDO)
                + "; foreground:" + Cores.hex(corTexto) + "; borderColor:" + Cores.hex(borda)
                + "; hoverBorderColor:" + Cores.hex(borda) + "; focusWidth:0");
    }

    private static JButton criar(String texto, ActionListener acao, String estilo) {
        JButton botao = new JButton(texto);
        botao.putClientProperty(FlatClientProperties.STYLE, estilo + "; margin:6,16,6,16");
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (acao != null) {
            botao.addActionListener(acao);
        }
        return botao;
    }
}
