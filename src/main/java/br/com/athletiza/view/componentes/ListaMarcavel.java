package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import java.awt.Dimension;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Lista com uma caixa de marcação por item, para escolher vários de uma vez
 * (ex.: os responsáveis por uma demanda).
 *
 * @param <T> tipo dos itens
 */
public class ListaMarcavel<T> extends JScrollPane {

    private final Map<T, JCheckBox> caixas = new LinkedHashMap<>();
    private final JPanel painel = new JPanel();
    private final String textoVazio;

    public ListaMarcavel(String textoVazio) {
        this.textoVazio = textoVazio;
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Cores.FUNDO_CARTAO);
        painel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setViewportView(painel);
        setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        getVerticalScrollBar().setUnitIncrement(16);
        setPreferredSize(new Dimension(0, 150));
    }

    /** Troca os itens exibidos, mantendo marcados os que já estavam. */
    public void setItens(List<T> itens, Function<T, String> texto) {
        Collection<T> marcados = getMarcados();
        caixas.clear();
        painel.removeAll();
        for (T item : itens) {
            JCheckBox caixa = new JCheckBox(texto.apply(item), marcados.contains(item));
            caixa.setOpaque(false);
            caixas.put(item, caixa);
            painel.add(caixa);
        }
        if (itens.isEmpty()) {
            JLabel vazio = new JLabel(textoVazio);
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            painel.add(vazio);
        }
        painel.revalidate();
        painel.repaint();
    }

    public void setMarcados(Collection<T> marcados) {
        caixas.forEach((item, caixa) -> caixa.setSelected(marcados.contains(item)));
    }

    /** Itens marcados, na ordem em que aparecem na lista. */
    public List<T> getMarcados() {
        return caixas.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).toList();
    }
}
