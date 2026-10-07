package br.com.athletiza.view.componentes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.util.Cores;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JToggleButton;
import org.junit.jupiter.api.Test;

class IconeMenuTest {

    private static final List<String> ITENS = List.of(
            "Início", "Gestão", "Atletas", "Competições", "Treinos", "Eventos", "Registros", "Usuários");

    @Test
    void todoItemDoMenuTemUmIconeDiferente() {
        assertEquals(ITENS.size(), ITENS.stream().map(IconeMenu::doItem).distinct().count());
        ITENS.forEach(item -> assertNotNull(IconeMenu.doItem(item), item));
        assertNull(IconeMenu.doItem("Inexistente"));
    }

    @Test
    void iconeDoItemSelecionadoEhVerde() {
        JToggleButton item = new JToggleButton("Treinos");
        item.setSelected(true);
        assertTrue(contemCor(desenhar(item), Cores.VERDE.getRGB()));
        item.setSelected(false);
        assertFalse(contemCor(desenhar(item), Cores.VERDE.getRGB()));
    }

    private static BufferedImage desenhar(JToggleButton item) {
        BufferedImage imagem = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        IconeMenu.TREINOS.paintIcon(item, g, 0, 0);
        g.dispose();
        return imagem;
    }

    private static boolean contemCor(BufferedImage imagem, int rgb) {
        for (int x = 0; x < imagem.getWidth(); x++) {
            for (int y = 0; y < imagem.getHeight(); y++) {
                if (imagem.getRGB(x, y) == rgb) {
                    return true;
                }
            }
        }
        return false;
    }
}
