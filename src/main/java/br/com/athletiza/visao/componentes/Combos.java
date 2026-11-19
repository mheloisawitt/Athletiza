package br.com.athletiza.visao.componentes;

import java.awt.Component;
import java.util.List;
import java.util.function.Function;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public final class Combos {
    private Combos() {
    }

    public static <T> JComboBox<T> comOpcaoTodos(List<T> itens, String rotuloTodos) {
        JComboBox<T> combo = new JComboBox<>();
        atualizarItens(combo, itens);
        exibirComo(combo, item -> item == null ? rotuloTodos : item.toString());
        return combo;
    }

    public static <T> void atualizarItens(JComboBox<T> combo, List<T> itens) {
        Object selecionado = combo.getSelectedItem();
        DefaultComboBoxModel<T> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(null);
        itens.forEach(modelo::addElement);
        modelo.setSelectedItem(selecionado != null && itens.contains(selecionado) ? selecionado : null);
        combo.setModel(modelo);
    }

    public static <T> void exibirComo(JComboBox<T> combo, Function<T, String> texto) {
        @SuppressWarnings("unchecked")
        ListCellRenderer<Object> padrao = (ListCellRenderer<Object>) (ListCellRenderer<?>) combo.getRenderer();
        combo.setRenderer(new ListCellRenderer<T>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends T> lista, T valor, int indice,
                    boolean selecionado, boolean foco) {
                @SuppressWarnings("unchecked")
                JList<Object> listaGenerica = (JList<Object>) lista;
                return padrao.getListCellRendererComponent(listaGenerica, texto.apply(valor), indice, selecionado, foco);
            }
        });
    }
}
