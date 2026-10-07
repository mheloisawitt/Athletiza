package br.com.athletiza.view.competicoes;

import br.com.athletiza.controller.AtletaController;
import br.com.athletiza.controller.CompeticaoController;
import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.Recarregavel;
import br.com.athletiza.view.componentes.Renderizadores;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Gerenciar competição (CH-31): à esquerda, as modalidades inscritas; à direita,
 * os atletas da modalidade selecionada. Na outra aba, os resultados (CH-32).
 *
 * As inscrições são montadas em memória (Map modalidade -> Set de atletas, em
 * Competicao) e gravadas de uma vez em "Salvar inscrições".
 */
public class PainelGerenciarCompeticao extends JPanel implements Recarregavel {

    private final CompeticaoController controller = new CompeticaoController();
    private final Navegador navegador;
    private final Competicao original;
    private Competicao competicao;
    private List<Atleta> todosAtletas = List.of();
    private boolean alterado;

    private final ModeloModalidades modeloModalidades = new ModeloModalidades();
    private final ModeloAtletas modeloAtletas = new ModeloAtletas();
    private final JTable tabelaModalidades = new JTable(modeloModalidades);
    private final JTable tabelaAtletas = new JTable(modeloAtletas);
    private final JLabel tituloAtletas = new JLabel();
    private final PainelResultados resultados;

    public PainelGerenciarCompeticao(Competicao competicao, Navegador navegador) {
        super(new BorderLayout(0, 16));
        this.original = competicao;
        this.competicao = competicao;
        this.navegador = navegador;
        this.resultados = new PainelResultados(this::competicaoSalva);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(criarCabecalho(), BorderLayout.NORTH);
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Modalidades e atletas", criarAbaInscricoes());
        abas.addTab("Resultados", resultados);
        abas.addChangeListener(e -> {
            if (abas.getSelectedComponent() == resultados) {
                if (alterado) {
                    Mensagens.aviso(this, "Há inscrições não salvas. Os resultados consideram apenas as inscrições salvas.");
                }
                resultados.carregar();
            }
        });
        add(abas, BorderLayout.CENTER);
    }

    @Override
    public void carregar() {
        try {
            competicao = competicaoSalva();
            todosAtletas = new AtletaController().listar();
            modeloModalidades.carregar(new ModalidadeController().listar());
            alterado = false;
            if (modeloModalidades.getRowCount() > 0 && tabelaModalidades.getSelectedRow() < 0) {
                int primeiraInscrita = modeloModalidades.primeiraInscrita();
                tabelaModalidades.setRowSelectionInterval(primeiraInscrita, primeiraInscrita);
            }
            atualizarAtletas();
            resultados.carregar();
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    /** Cópia da competição com as inscrições como estão no banco. */
    private Competicao competicaoSalva() throws PersistenciaException {
        Competicao copia = new Competicao(original.getTitulo(), original.getData(), original.getDataFim(),
                original.getHorario(), original.getLocal());
        copia.setId(original.getId());
        copia.setSituacao(original.getSituacao());
        controller.carregarInscricoes(copia);
        return copia;
    }

    private JPanel criarCabecalho() {
        JButton voltar = new JButton("‹ Voltar");
        voltar.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; margin:2,0,2,8");
        voltar.addActionListener(e -> {
            if (!alterado || Mensagens.confirmar(this, "Há inscrições não salvas. Deseja sair mesmo assim?")) {
                navegador.voltar();
            }
        });
        JLabel titulo = new JLabel("Gerenciar Competição");
        titulo.setFont(Tema.fonteTitulo());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(voltar);
        linha.add(titulo);

        String periodo = Validador.FORMATO_DATA.format(original.getData())
                + (original.getDataFim() == null ? "" : " a " + Validador.FORMATO_DATA.format(original.getDataFim()));
        JLabel detalhe = new JLabel(original.getTitulo() + "  ·  " + periodo
                + (original.getLocal() == null ? "" : "  ·  " + original.getLocal()));
        detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
        detalhe.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        linha.setAlignmentX(LEFT_ALIGNMENT);
        detalhe.setAlignmentX(LEFT_ALIGNMENT);
        cabecalho.add(linha);
        cabecalho.add(detalhe);
        return cabecalho;
    }

    private JPanel criarAbaInscricoes() {
        configurarTabela(tabelaModalidades);
        tabelaModalidades.getColumnModel().getColumn(2).setMinWidth(100);
        tabelaModalidades.getColumnModel().getColumn(2).setMaxWidth(110);
        tabelaModalidades.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarAtletas();
            }
        });
        configurarTabela(tabelaAtletas);

        JLabel tituloModalidades = new JLabel("Modalidades da competição");
        tituloModalidades.setFont(Tema.fonte(Font.BOLD, 14f));
        tituloAtletas.setFont(Tema.fonte(Font.BOLD, 14f));

        JPanel colunas = new JPanel(new GridLayout(1, 2, 16, 0));
        colunas.setOpaque(false);
        colunas.add(cartao(tituloModalidades, tabelaModalidades));
        colunas.add(cartao(tituloAtletas, tabelaAtletas));

        JButton salvar = Botoes.primario("Salvar inscrições", e -> salvar());
        salvar.setEnabled(Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        rodape.add(salvar);

        JPanel aba = new JPanel(new BorderLayout(0, 16));
        aba.setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));
        aba.add(colunas, BorderLayout.CENTER);
        aba.add(rodape, BorderLayout.SOUTH);
        return aba;
    }

    private static void configurarTabela(JTable tabela) {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        Renderizadores.aplicarPadroes(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(46);
        tabela.setEnabled(Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados());
    }

    private static JPanel cartao(JLabel titulo, JTable tabela) {
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        JPanel cartao = new JPanel(new BorderLayout(0, 10));
        cartao.setOpaque(false);
        cartao.add(titulo, BorderLayout.NORTH);
        cartao.add(rolagem, BorderLayout.CENTER);
        return cartao;
    }

    private Modalidade modalidadeSelecionada() {
        int linha = tabelaModalidades.getSelectedRow();
        return linha < 0 ? null : modeloModalidades.modalidades.get(linha);
    }

    private void atualizarAtletas() {
        Modalidade modalidade = modalidadeSelecionada();
        tituloAtletas.setText(modalidade == null ? "Atletas" : "Atletas da modalidade - " + modalidade);
        modeloAtletas.carregar(modalidade);
    }

    private void salvar() {
        try {
            controller.salvarInscricoes(competicao);
            alterado = false;
            Mensagens.sucesso(this, "Inscrições salvas com sucesso.");
            carregar();
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    /** [inscrita] [modalidade] [nº de atletas inscritos]. */
    private class ModeloModalidades extends AbstractTableModel {

        private final List<Modalidade> modalidades = new ArrayList<>();

        void carregar(List<Modalidade> todas) {
            modalidades.clear();
            modalidades.addAll(todas);
            fireTableDataChanged();
        }

        int primeiraInscrita() {
            for (int i = 0; i < modalidades.size(); i++) {
                if (competicao.getModalidades().contains(modalidades.get(i))) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public int getRowCount() {
            return modalidades.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int coluna) {
            return new String[]{"", "Modalidade", "Inscritos"}[coluna];
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return coluna == 0 ? Boolean.class : coluna == 2 ? Integer.class : String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna == 0;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            Modalidade modalidade = modalidades.get(linha);
            boolean inscrita = competicao.getModalidades().contains(modalidade);
            return switch (coluna) {
                case 0 -> inscrita;
                case 1 -> modalidade.toString();
                default -> inscrita ? competicao.getAtletas(modalidade).size() : null;
            };
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            Modalidade modalidade = modalidades.get(linha);
            if (Boolean.TRUE.equals(valor)) {
                competicao.adicionarModalidade(modalidade);
            } else {
                int atletas = competicao.getAtletas(modalidade).size();
                if (atletas > 0 && !Mensagens.confirmar(PainelGerenciarCompeticao.this, "Retirar " + modalidade
                        + " remove " + atletas + " atleta(s) inscrito(s) e, ao salvar, os resultados dessa modalidade."
                        + " Continuar?")) {
                    return;
                }
                competicao.removerModalidade(modalidade);
            }
            alterado = true;
            fireTableRowsUpdated(linha, linha);
            tabelaModalidades.setRowSelectionInterval(linha, linha);
            atualizarAtletas();
        }
    }

    /** [inscrito] [nome] [matrícula] [situação na modalidade] dos atletas da modalidade selecionada. */
    private class ModeloAtletas extends AbstractTableModel {

        private final List<Atleta> atletas = new ArrayList<>();
        private Modalidade modalidade;

        void carregar(Modalidade novaModalidade) {
            modalidade = novaModalidade;
            atletas.clear();
            if (modalidade != null) {
                Set<Atleta> visiveis = new LinkedHashSet<>();
                todosAtletas.stream().filter(a -> a.praticaModalidade(modalidade)).forEach(visiveis::add);
                visiveis.addAll(competicao.getAtletas(modalidade));
                atletas.addAll(visiveis);
            }
            fireTableDataChanged();
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
            return new String[]{"", "Atleta", "Matrícula", "Situação"}[coluna];
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
                case 0 -> competicao.getAtletas(modalidade).contains(atleta);
                case 1 -> atleta.getNome();
                case 2 -> atleta.getMatricula();
                default -> atleta.getSituacaoNaModalidade(modalidade).orElse(null);
            };
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            Atleta atleta = atletas.get(linha);
            if (Boolean.TRUE.equals(valor)) {
                try {
                    competicao.adicionarModalidade(modalidade);
                    competicao.inscreverAtleta(modalidade, atleta);
                } catch (RegraNegocioException e) {
                    Mensagens.erro(PainelGerenciarCompeticao.this, e);
                    return;
                }
            } else {
                competicao.removerAtleta(modalidade, atleta);
            }
            alterado = true;
            fireTableRowsUpdated(linha, linha);
            int linhaModalidade = modeloModalidades.modalidades.indexOf(modalidade);
            modeloModalidades.fireTableRowsUpdated(linhaModalidade, linhaModalidade);
        }
    }
}
