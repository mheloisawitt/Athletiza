package br.com.athletiza.view.componentes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import javax.swing.JComboBox;
import org.junit.jupiter.api.Test;

class CombosTest {

    @Test
    void criaComboComOpcaoTodosMesmoComListaImutavel() {
        JComboBox<String> combo = Combos.comOpcaoTodos(List.of("A", "B"), "Todos");

        assertEquals(3, combo.getItemCount());
        assertNull(combo.getSelectedItem());
    }

    @Test
    void mantemSelecaoAoAtualizarItensSeElaAindaExistir() {
        JComboBox<String> combo = Combos.comOpcaoTodos(List.of("A", "B"), "Todos");
        combo.setSelectedItem("B");

        Combos.atualizarItens(combo, List.of("B", "C"));
        assertEquals("B", combo.getSelectedItem());

        Combos.atualizarItens(combo, List.of("C"));
        assertNull(combo.getSelectedItem());
    }
}
