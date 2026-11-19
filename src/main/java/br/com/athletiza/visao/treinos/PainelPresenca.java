package br.com.athletiza.visao.treinos;

import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.SituacaoAtleta;
import br.com.athletiza.modelo.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import br.com.athletiza.visao.componentes.Renderizadores;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class PainelPresenca extends PainelFormulario {
    private final Treino treino;
    private final ModeloChamada modelo = new ModeloChamada();
    private final JLabel contador = new JLabel();

    public PainelPresenca(Treino treino, Navegador navegador) {
        super("Lista de presença", navegador);
        this.treino = treino;

        JLabel descricao = new JLabel(treino.getTitulo() + "  ·  " + Validador.FORMATO_DATA.format(treino.getData())
                + (treino.getHorario() == null ? "" : " às " + Validador.FORMATO_HORARIO.format(treino.getHorario()))
                + (treino.getLocal() == null ? "" : "  ·  " + treino.getLocal()));
        descricao.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(descricao);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botoes.setOpaque(false);
        botoes.add(Botoes.contorno("Marcar todos", e -> modelo.marcarTodos(true)));
        botoes.add(javax.swing.Box.createHorizontalStrut(8));
        botoes.add(Botoes.contorno("Desmarcar todos", e -> modelo.marcarTodos(false)));
        barra.add(botoes, BorderLayout.WEST);
        barra.add(contador, BorderLayout.EAST);
        adicionarLinha(barra);

        JTable tabela = new JTable(modelo);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        Renderizadores.aplicarPadroes(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(90);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        rolagem.setPreferredSize(new Dimension(0, 380));
        adicionarLinha(rolagem);
    }

    public void apresentarChamada(List<Atleta> atletas) {
        modelo.carregar(atletas);
    }

    public List<Atleta> getAtletas() {
        return modelo.atletas;
    }

    public boolean isPresente(int indice) {
        return modelo.presentes.get(indice);
    }

    public long getTotalPresentes() {
        return modelo.totalPresentes();
    }

    private void atualizarContador() {
        contador.setText(modelo.totalPresentes() + " de " + modelo.atletas.size() + " presentes");
    }

    private class ModeloChamada extends AbstractTableModel {
        private final List<Atleta> atletas = new ArrayList<>();
        private final List<Boolean> presentes = new ArrayList<>();

        void carregar(List<Atleta> chamada) {
            for (Atleta atleta : chamada) {
                atletas.add(atleta);
                presentes.add(treino.getPresencas().getOrDefault(atleta, false));
            }
            fireTableDataChanged();
            atualizarContador();
        }

        void marcarTodos(boolean presente) {
            presentes.replaceAll(p -> presente);
            fireTableDataChanged();
            atualizarContador();
        }

        long totalPresentes() {
            return presentes.stream().filter(Boolean::booleanValue).count();
        }

        @Override
        public int getRowCount() {
            return atletas.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int coluna) {
            return new String[]{"Presente", "Atleta", "Matrícula", "Situação na modalidade"}[coluna];
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return coluna == 0 ? Boolean.class : coluna == 3 ? SituacaoAtleta.class : String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna == 0;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            Atleta atleta = atletas.get(linha);
            return switch (coluna) {
                case 0 -> presentes.get(linha);
                case 1 -> atleta.getNome();
                case 2 -> atleta.getMatricula();
                default -> atleta.getSituacaoNaModalidade(treino.getModalidade()).orElse(null);
            };
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            presentes.set(linha, (Boolean) valor);
            fireTableRowsUpdated(linha, linha);
            atualizarContador();
        }
    }
}
