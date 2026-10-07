package br.com.athletiza.view;

import br.com.athletiza.util.Cores;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;
import javax.swing.UIManager;

/**
 * Aparência padrão do sistema (CH-03): tema escuro do FlatLaf com as cores do protótipo.
 * Deve ser chamado uma vez, antes de abrir a primeira tela.
 */
public final class Tema {

    private Tema() {
    }

    public static void aplicar() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", Cores.hex(Cores.VERDE),
                "@background", Cores.hex(Cores.FUNDO),
                "@foreground", "#FFFFFF"));
        FlatDarkLaf.setup();

        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("Component.borderColor", Cores.CINZA_ESCURO);
        UIManager.put("TextField.margin", new Insets(7, 10, 7, 10));
        UIManager.put("PasswordField.margin", new Insets(7, 10, 7, 10));
        UIManager.put("FormattedTextField.margin", new Insets(7, 10, 7, 10));
        UIManager.put("TextArea.margin", new Insets(7, 10, 7, 10));
        UIManager.put("ComboBox.padding", new Insets(6, 10, 6, 10));
        UIManager.put("TextComponent.selectionBackground", Cores.VERDE_SELECAO);
        UIManager.put("TextComponent.selectionForeground", Cores.TEXTO);
        UIManager.put("Button.default.foreground", Cores.TEXTO_SOBRE_VERDE);

        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.gridColor", Cores.CINZA_ESCURO);
        UIManager.put("Table.background", Cores.FUNDO_CARTAO);
        UIManager.put("Table.selectionBackground", Cores.VERDE_SELECAO);
        UIManager.put("Table.selectionForeground", Cores.TEXTO);
        UIManager.put("Table.selectionInactiveBackground", Cores.VERDE_SELECAO);
        UIManager.put("Table.selectionInactiveForeground", Cores.TEXTO);
        UIManager.put("TableHeader.height", 38);
        UIManager.put("TableHeader.cellMargins", new Insets(2, 14, 2, 14));
        UIManager.put("TableHeader.background", Cores.FUNDO_CARTAO);
        UIManager.put("TableHeader.foreground", Cores.TEXTO_SECUNDARIO);
        UIManager.put("TableHeader.separatorColor", Cores.FUNDO_CARTAO);
        UIManager.put("TableHeader.bottomSeparatorColor", Cores.CINZA_ESCURO);
        UIManager.put("ScrollPane.background", Cores.FUNDO_CARTAO);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "OK");
    }

    public static Font fonte(int estilo, float tamanho) {
        return UIManager.getFont("defaultFont").deriveFont(estilo, tamanho);
    }

    public static Font fonteTitulo() {
        return fonte(Font.BOLD, 20f);
    }
}
