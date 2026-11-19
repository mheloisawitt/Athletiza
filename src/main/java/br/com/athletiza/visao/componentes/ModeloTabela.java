package br.com.athletiza.visao.componentes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

public class ModeloTabela<T> extends AbstractTableModel {
    private record Coluna<T>(String titulo, Class<?> tipo, Function<T, ?> valor) {
    }

    private final List<Coluna<T>> colunas = new ArrayList<>();
    private List<T> linhas = new ArrayList<>();

    public <V> ModeloTabela<T> coluna(String titulo, Class<V> tipo, Function<T, V> valor) {
        colunas.add(new Coluna<>(titulo, tipo, valor));
        return this;
    }

    public void setLinhas(List<T> novasLinhas) {
        this.linhas = new ArrayList<>(novasLinhas);
        fireTableDataChanged();
    }

    public List<T> getLinhas() {
        return List.copyOf(linhas);
    }

    public T getLinha(int indice) {
        return linhas.get(indice);
    }

    @Override
    public int getRowCount() {
        return linhas.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.size();
    }

    @Override
    public String getColumnName(int coluna) {
        return colunas.get(coluna).titulo();
    }

    @Override
    public Class<?> getColumnClass(int coluna) {
        return colunas.get(coluna).tipo();
    }

    @Override
    public Object getValueAt(int linha, int coluna) {
        return colunas.get(coluna).valor().apply(linhas.get(linha));
    }
}
