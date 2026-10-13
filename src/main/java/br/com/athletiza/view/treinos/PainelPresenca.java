package br.com.athletiza.view.treinos;

import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.Treino;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.PainelFormulario;
import br.com.athletiza.view.componentes.Renderizadores;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Lista de presença do treino (CH-36): cada atleta da modalidade marcado como
 * presente ou ausente, salvo de uma vez. Pode ser editada depois.
 * As ações ficam no PresencaController.
 */
public class PainelPresenca extends PainelFormulario {

    private final ModeloChamada modelo = new ModeloChamada();
    private final JLabel descricao = new JLabel();
    private final JLabel contador = new JLabel();
    private Treino treino;

    public PainelPresenca() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitulo("Lista de presença");
        descricao.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(descricao);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botoes.setOpaque(false);
        botoes.add(Botoes.contorno("Marcar todos", e -> modelo.marcarTodos(true)));
        botoes.add(Box.createHorizontalStrut(8));
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

    /** Mostra a chamada: os atletas, marcando quem já estava presente no treino. */
    public void exibirChamada(Treino treinoExibido, List<Atleta> atletas) {
        this.treino = treinoExibido;
        descricao.setText(treino.getTitulo() + "  ·  " + Validador.FORMATO_DATA.format(treino.getData())
                + (treino.getHorario() == null ? "" : " às " + Validador.FORMATO_HORARIO.format(treino.getHorario()))
                + (treino.getLocal() == null ? "" : "  ·  " + treino.getLocal()));
        modelo.carregar(atletas);
    }

    /** Presença marcada de cada atleta (true = presente). */
    public Map<Atleta, Boolean> getPresencas() {
        Map<Atleta, Boolean> presencas = new LinkedHashMap<>();
        for (int i = 0; i < modelo.atletas.size(); i++) {
            presencas.put(modelo.atletas.get(i), modelo.presentes.get(i));
        }
        return presencas;
    }

    /** Marca ou desmarca a presença de um atleta (o mesmo que clicar na caixa; usado nos testes). */
    public void marcarPresenca(Atleta atleta, boolean presente) {
        int linha = modelo.atletas.indexOf(atleta);
        if (linha >= 0) {
            modelo.setValueAt(presente, linha, 0);
        }
    }

    /** Marca ou desmarca a presença de todos (botões da tela; usado também nos testes). */
    public void marcarTodos(boolean presente) {
        modelo.marcarTodos(presente);
    }

    private void atualizarContador() {
        contador.setText(modelo.totalPresentes() + " de " + modelo.atletas.size() + " presentes");
    }

    /** [presente] [atleta] [matrícula] [situação na modalidade]. */
    private class ModeloChamada extends AbstractTableModel {

        private final List<Atleta> atletas = new ArrayList<>();
        private final List<Boolean> presentes = new ArrayList<>();

        void carregar(List<Atleta> chamada) {
            atletas.clear();
            presentes.clear();
            for (Atleta atleta : chamada) {
                atletas.add(atleta);
                presentes.add(treino.estavaPresente(atleta));
            }
            fireTableDataChanged();
            atualizarContador();
        }

        void marcarTodos(boolean presente) {
            for (int i = 0; i < presentes.size(); i++) {
                presentes.set(i, presente);
            }
            fireTableDataChanged();
            atualizarContador();
        }

        int totalPresentes() {
            int total = 0;
            for (Boolean presente : presentes) {
                if (presente) {
                    total++;
                }
            }
            return total;
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
            if (coluna == 0) {
                return Boolean.class;
            }
            return coluna == 3 ? SituacaoAtleta.class : String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna == 0;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            Atleta atleta = atletas.get(linha);
            if (coluna == 0) {
                return presentes.get(linha);
            }
            if (coluna == 1) {
                return atleta.getNome();
            }
            if (coluna == 2) {
                return atleta.getMatricula();
            }
            return atleta.getSituacaoNaModalidade(treino.getModalidade());
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            presentes.set(linha, (Boolean) valor);
            fireTableRowsUpdated(linha, linha);
            atualizarContador();
        }
    }
}
