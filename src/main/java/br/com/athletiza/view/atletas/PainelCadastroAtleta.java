package br.com.athletiza.view.atletas;

import br.com.athletiza.controller.AtletaController;
import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
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
 */
public class PainelCadastroAtleta extends PainelFormulario {

    private final Atleta atleta;
    private final AtletaController controller = new AtletaController();
    private final JTextField nome = new JTextField();
    private final JTextField matricula = new JTextField();
    private final JTextField nascimento = new JTextField();
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JTextField contato = new JTextField();
    private final JTextArea observacoes = new JTextArea();
    private final ModeloModalidades modeloModalidades = new ModeloModalidades();
    private final JTable tabelaModalidades = new JTable(modeloModalidades);

    public PainelCadastroAtleta(Atleta atleta, Navegador navegador) {
        super(atleta.isNova() ? "Novo Atleta" : "Editar Atleta", navegador);
        this.atleta = atleta;

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

        preencherCampos();
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

    private void preencherCampos() {
        nome.setText(atleta.getNome());
        matricula.setText(atleta.getMatricula());
        nascimento.setText(atleta.getDataNascimento() == null ? "" : Validador.FORMATO_DATA.format(atleta.getDataNascimento()));
        situacao.setSelectedItem(atleta.getSituacao());
        contato.setText(atleta.getContato());
        observacoes.setText(atleta.getObservacoes());
        try {
            modeloModalidades.carregar(new ModalidadeController().listar(), atleta);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    @Override
    protected void salvar() throws AthletizaException {
        if (tabelaModalidades.isEditing()) {
            tabelaModalidades.getCellEditor().stopCellEditing();
        }
        atleta.setNome(nome.getText().trim());
        atleta.setMatricula(matricula.getText().trim());
        atleta.setDataNascimento(Validador.converterData(nascimento.getText(), "Data de nascimento"));
        atleta.setSituacao((Situacao) situacao.getSelectedItem());
        atleta.setContato(textoOuNulo(contato.getText()));
        atleta.setObservacoes(textoOuNulo(observacoes.getText()));

        new ArrayList<>(atleta.getVinculos().keySet()).forEach(atleta::desvincularModalidade);
        modeloModalidades.getMarcadas().forEach(atleta::vincularModalidade);

        controller.salvar(atleta);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Atleta salvo com sucesso.";
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /** Tabela de modalidades: [marcada] [modalidade] [situação do atleta nela]. */
    private static class ModeloModalidades extends AbstractTableModel {

        private static final String[] COLUNAS = {"", "Modalidade", "Situação na modalidade"};

        private final List<Modalidade> modalidades = new ArrayList<>();
        private final List<Boolean> marcadas = new ArrayList<>();
        private final List<SituacaoAtleta> situacoes = new ArrayList<>();

        void carregar(List<Modalidade> todas, Atleta atleta) {
            for (Modalidade modalidade : todas) {
                modalidades.add(modalidade);
                marcadas.add(atleta.praticaModalidade(modalidade));
                situacoes.add(atleta.getSituacaoNaModalidade(modalidade).orElse(SituacaoAtleta.ATIVO));
            }
            fireTableDataChanged();
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
            return switch (coluna) {
                case 0 -> Boolean.class;
                case 2 -> SituacaoAtleta.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna != 1;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            return switch (coluna) {
                case 0 -> marcadas.get(linha);
                case 1 -> modalidades.get(linha).toString();
                default -> marcadas.get(linha) ? situacoes.get(linha) : null;
            };
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
