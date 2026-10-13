package br.com.athletiza.view.atletas;

import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.PainelFormulario;
import br.com.athletiza.view.componentes.Renderizadores;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

/**
 * Cadastro e edição de atleta (CH-26). As modalidades são escolhidas em uma
 * tabela com marcação e a situação do atleta em cada uma (RF04).
 * As ações ficam no CadastroAtletaController.
 */
public class PainelCadastroAtleta extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JTextField matricula = new JTextField();
    private final JTextField nascimento = new JTextField();
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JTextField contato = new JTextField();
    private final JTextArea observacoes = new JTextArea();
    private final ModeloModalidades modeloModalidades = new ModeloModalidades();
    private final JTable tabelaModalidades = new JTable(modeloModalidades);

    public PainelCadastroAtleta() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        matricula.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: 202001");
        nascimento.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        contato.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Telefone ou e-mail");
        observacoes.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Informações adicionais...");

        adicionarCampo("Nome", nome, true);
        adicionarCampo("Matrícula", matricula, true);
        adicionarCampo("Data de nascimento", nascimento, false);
        adicionarCampo("Situação", situacao, true);
        adicionarCampoLinhaInteira("Contato", contato, false);
        adicionarCampoLinhaInteira("Modalidades (marque e informe a situação em cada uma)", criarTabelaModalidades(), true);
        adicionarCampoLinhaInteira("Observações", areaTexto(observacoes), false);
    }

    private JScrollPane criarTabelaModalidades() {
        tabelaModalidades.setRowHeight(32);
        tabelaModalidades.getTableHeader().setReorderingAllowed(false);
        Renderizadores.aplicarPadroes(tabelaModalidades);
        tabelaModalidades.getColumnModel().getColumn(0).setMaxWidth(50);
        tabelaModalidades.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(SituacaoAtleta.values())));
        tabelaModalidades.putClientProperty("terminateEditOnFocusLost", true);

        JScrollPane rolagem = new JScrollPane(tabelaModalidades);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        rolagem.setPreferredSize(new Dimension(0, 220));
        return rolagem;
    }

    /**
     * Mostra os dados do atleta. A tabela lista todas as modalidades,
     * marcando as que o atleta pratica.
     */
    public void preencherCampos(Atleta atleta, List<Modalidade> todasModalidades) {
        setTitulo(atleta.isNova() ? "Novo Atleta" : "Editar Atleta");
        nome.setText(atleta.getNome());
        matricula.setText(atleta.getMatricula());
        nascimento.setText(atleta.getDataNascimento() == null ? "" : Validador.FORMATO_DATA.format(atleta.getDataNascimento()));
        situacao.setSelectedItem(atleta.getSituacao());
        contato.setText(atleta.getContato());
        observacoes.setText(atleta.getObservacoes());
        modeloModalidades.carregar(todasModalidades, atleta);
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getMatricula() {
        return matricula.getText().trim();
    }

    /** Data digitada (dd/mm/aaaa); a conversão é feita pelo Controller. */
    public String getDataNascimento() {
        return nascimento.getText();
    }

    public Situacao getSituacao() {
        return (Situacao) situacao.getSelectedItem();
    }

    public String getContato() {
        return textoOuNulo(contato.getText());
    }

    public String getObservacoes() {
        return textoOuNulo(observacoes.getText());
    }

    /** Modalidades marcadas na tabela, com a situação do atleta em cada uma. */
    public Map<Modalidade, SituacaoAtleta> getModalidadesMarcadas() {
        if (tabelaModalidades.isEditing()) {
            tabelaModalidades.getCellEditor().stopCellEditing();
        }
        return modeloModalidades.getMarcadas();
    }

    /** Marca ou desmarca uma modalidade da tabela (usado também nos testes). */
    public void marcarModalidade(Modalidade modalidade, SituacaoAtleta situacaoNaModalidade) {
        modeloModalidades.marcar(modalidade, situacaoNaModalidade);
    }

    /** Tabela de modalidades: [marcada] [modalidade] [situação do atleta nela]. */
    private static class ModeloModalidades extends AbstractTableModel {

        private static final String[] COLUNAS = {"", "Modalidade", "Situação na modalidade"};

        private final List<Modalidade> modalidades = new ArrayList<>();
        private final List<Boolean> marcadas = new ArrayList<>();
        private final List<SituacaoAtleta> situacoes = new ArrayList<>();

        void carregar(List<Modalidade> todas, Atleta atleta) {
            modalidades.clear();
            marcadas.clear();
            situacoes.clear();
            for (Modalidade modalidade : todas) {
                SituacaoAtleta situacaoNela = atleta.getSituacaoNaModalidade(modalidade);
                modalidades.add(modalidade);
                marcadas.add(situacaoNela != null);
                situacoes.add(situacaoNela == null ? SituacaoAtleta.ATIVO : situacaoNela);
            }
            fireTableDataChanged();
        }

        void marcar(Modalidade modalidade, SituacaoAtleta situacaoNaModalidade) {
            int linha = modalidades.indexOf(modalidade);
            if (linha >= 0) {
                marcadas.set(linha, situacaoNaModalidade != null);
                if (situacaoNaModalidade != null) {
                    situacoes.set(linha, situacaoNaModalidade);
                }
                fireTableRowsUpdated(linha, linha);
            }
        }

        Map<Modalidade, SituacaoAtleta> getMarcadas() {
            Map<Modalidade, SituacaoAtleta> resultado = new LinkedHashMap<>();
            for (int i = 0; i < modalidades.size(); i++) {
                if (marcadas.get(i)) {
                    resultado.put(modalidades.get(i), situacoes.get(i));
                }
            }
            return resultado;
        }

        @Override
        public int getRowCount() {
            return modalidades.size();
        }

        @Override
        public int getColumnCount() {
            return COLUNAS.length;
        }

        @Override
        public String getColumnName(int coluna) {
            return COLUNAS[coluna];
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            if (coluna == 0) {
                return Boolean.class;
            }
            if (coluna == 2) {
                return SituacaoAtleta.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna != 1;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            if (coluna == 0) {
                return marcadas.get(linha);
            }
            if (coluna == 1) {
                return modalidades.get(linha).toString();
            }
            return marcadas.get(linha) ? situacoes.get(linha) : null;
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            if (coluna == 0) {
                marcadas.set(linha, (Boolean) valor);
            } else if (coluna == 2 && valor != null) {
                situacoes.set(linha, (SituacaoAtleta) valor);
                marcadas.set(linha, true);
            }
            fireTableRowsUpdated(linha, linha);
        }
    }
}
