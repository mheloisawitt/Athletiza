package br.com.athletiza.view.competicoes;

import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.PainelTarefas;
import br.com.athletiza.view.componentes.Recarregavel;
import br.com.athletiza.view.componentes.Renderizadores;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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
 * os atletas da modalidade selecionada. Nas outras abas, os resultados (CH-32)
 * e as tarefas da organização (RF19).
 *
 * Marcar ou desmarcar uma caixa avisa o GerenciarCompeticaoController, que altera
 * as inscrições em memória; "Salvar inscrições" grava tudo de uma vez.
 */
public class PainelGerenciarCompeticao extends JPanel implements Recarregavel {

    private final ModeloModalidades modeloModalidades = new ModeloModalidades();
    private final ModeloAtletas modeloAtletas = new ModeloAtletas();
    private final JTable tabelaModalidades = new JTable(modeloModalidades);
    private final JTable tabelaAtletas = new JTable(modeloAtletas);
    private final JLabel tituloAtletas = new JLabel();
    private final JLabel detalhe = new JLabel();
    private final JButton btnVoltar = new JButton("‹ Voltar");
    private final JButton btnSalvar = Botoes.primario("Salvar inscrições", null);
    private final JTabbedPane abas = new JTabbedPane();
    private final PainelResultados resultados = new PainelResultados();
    private final PainelTarefas tarefas = new PainelTarefas();

    /** Dados exibidos: a competição (com as inscrições em edição), todas as modalidades e todos os atletas. */
    private Competicao competicao;
    private List<Atleta> todosAtletas = new ArrayList<>();

    /* Última caixa marcada/desmarcada, lida pelo Controller */
    private Modalidade modalidadeClicada;
    private Atleta atletaClicado;
    private boolean marcacaoClicada;

    private ActionListener acaoCarregar;
    private ActionListener acaoMarcarModalidade;
    private ActionListener acaoMarcarAtleta;
    private ActionListener acaoAbrirResultados;

    public PainelGerenciarCompeticao() {
        super(new BorderLayout(0, 16));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(criarCabecalho(), BorderLayout.NORTH);
        abas.addTab("Modalidades e atletas", criarAbaInscricoes());
        abas.addTab("Resultados", resultados);
        abas.addTab("Tarefas", tarefas);
        abas.addChangeListener(e -> {
            if (abas.getSelectedComponent() == resultados && acaoAbrirResultados != null) {
                acaoAbrirResultados.actionPerformed(new ActionEvent(abas, ActionEvent.ACTION_PERFORMED, "resultados"));
            } else if (abas.getSelectedComponent() == tarefas) {
                tarefas.carregar();
            }
        });
        add(abas, BorderLayout.CENTER);
    }

    /* Ações: registradas pelo Controller */

    public void adicionarAcaoCarregar(ActionListener acao) {
        this.acaoCarregar = acao;
    }

    @Override
    public void carregar() {
        if (acaoCarregar != null) {
            acaoCarregar.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "carregar"));
        }
    }

    public void adicionarAcaoVoltar(ActionListener acao) {
        btnVoltar.addActionListener(acao);
    }

    public void adicionarAcaoSalvar(ActionListener acao) {
        btnSalvar.addActionListener(acao);
    }

    /** Uma modalidade foi marcada ou desmarcada (ver getModalidadeClicada e isMarcacaoClicada). */
    public void adicionarAcaoMarcarModalidade(ActionListener acao) {
        this.acaoMarcarModalidade = acao;
    }

    /** Um atleta foi marcado ou desmarcado (ver getAtletaClicado e isMarcacaoClicada). */
    public void adicionarAcaoMarcarAtleta(ActionListener acao) {
        this.acaoMarcarAtleta = acao;
    }

    /** A aba "Resultados" foi aberta. */
    public void adicionarAcaoAbrirResultados(ActionListener acao) {
        this.acaoAbrirResultados = acao;
    }

    public JButton getBtnSalvar() {
        return btnSalvar;
    }

    public Modalidade getModalidadeClicada() {
        return modalidadeClicada;
    }

    public Atleta getAtletaClicado() {
        return atletaClicado;
    }

    public boolean isMarcacaoClicada() {
        return marcacaoClicada;
    }

    public PainelResultados getPainelResultados() {
        return resultados;
    }

    public PainelTarefas getPainelTarefas() {
        return tarefas;
    }

    /* Dados exibidos */

    public void exibirCabecalho(Competicao dados) {
        String periodo = Validador.FORMATO_DATA.format(dados.getData())
                + (dados.getDataFim() == null ? "" : " a " + Validador.FORMATO_DATA.format(dados.getDataFim()));
        detalhe.setText(dados.getTitulo() + "  ·  " + periodo + (dados.getLocal() == null ? "" : "  ·  " + dados.getLocal()));
    }

    /** Mostra as inscrições: todas as modalidades (marcando as inscritas) e os atletas da selecionada. */
    public void exibirInscricoes(Competicao competicao, List<Modalidade> todasModalidades, List<Atleta> todosAtletas) {
        this.competicao = competicao;
        this.todosAtletas = todosAtletas;
        modeloModalidades.carregar(todasModalidades);
        if (modeloModalidades.getRowCount() > 0 && tabelaModalidades.getSelectedRow() < 0) {
            int primeiraInscrita = modeloModalidades.primeiraInscrita();
            tabelaModalidades.setRowSelectionInterval(primeiraInscrita, primeiraInscrita);
        }
        atualizarAtletas();
    }

    /** Atualiza as tabelas depois de o Controller alterar as inscrições da modalidade. */
    public void atualizarInscricoes(Modalidade modalidade) {
        int linha = modeloModalidades.modalidades.indexOf(modalidade);
        if (linha >= 0) {
            modeloModalidades.fireTableRowsUpdated(linha, linha);
            if (tabelaModalidades.getSelectedRow() != linha) {
                tabelaModalidades.setRowSelectionInterval(linha, linha);
            }
        }
        atualizarAtletas();
    }

    /** Modalidade selecionada na tabela da esquerda (null se nenhuma). */
    public Modalidade getModalidadeSelecionada() {
        int linha = tabelaModalidades.getSelectedRow();
        return linha < 0 ? null : modeloModalidades.modalidades.get(linha);
    }

    /** Vai para a primeira aba (modalidades e atletas inscritos). */
    public void mostrarAbaInscricoes() {
        abas.setSelectedIndex(0);
    }

    /* Mensagens */

    public void mostrarMensagem(String mensagem) {
        Mensagens.sucesso(this, mensagem);
    }

    public void mostrarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    public void mostrarErro(Exception erro) {
        Mensagens.erro(this, erro);
    }

    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    /* Montagem da tela */

    private JPanel criarCabecalho() {
        btnVoltar.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; margin:2,0,2,8");
        JLabel titulo = new JLabel("Gerenciar Competição");
        titulo.setFont(Tema.fonteTitulo());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(btnVoltar);
        linha.add(titulo);

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

        btnSalvar.setEnabled(podeAlterar());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        rodape.add(btnSalvar);

        JPanel aba = new JPanel(new BorderLayout(0, 16));
        aba.setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));
        aba.add(colunas, BorderLayout.CENTER);
        aba.add(rodape, BorderLayout.SOUTH);
        return aba;
    }

    private static boolean podeAlterar() {
        return Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
    }

    private static void configurarTabela(JTable tabela) {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        Renderizadores.aplicarPadroes(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(46);
        tabela.setEnabled(podeAlterar());
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

    private void atualizarAtletas() {
        Modalidade modalidade = getModalidadeSelecionada();
        tituloAtletas.setText(modalidade == null ? "Atletas" : "Atletas da modalidade - " + modalidade);
        modeloAtletas.carregar(modalidade);
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
            if (coluna == 0) {
                return Boolean.class;
            }
            return coluna == 2 ? Integer.class : String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return coluna == 0;
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            Modalidade modalidade = modalidades.get(linha);
            boolean inscrita = competicao.getModalidades().contains(modalidade);
            if (coluna == 0) {
                return inscrita;
            }
            if (coluna == 1) {
                return modalidade.toString();
            }
            return inscrita ? competicao.getAtletas(modalidade).size() : null;
        }

        /** Clique na caixa: avisa o Controller, que decide o que fazer. */
        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            modalidadeClicada = modalidades.get(linha);
            marcacaoClicada = Boolean.TRUE.equals(valor);
            if (acaoMarcarModalidade != null) {
                acaoMarcarModalidade.actionPerformed(new ActionEvent(tabelaModalidades,
                        ActionEvent.ACTION_PERFORMED, "modalidade"));
            }
        }
    }

    /** [inscrito] [nome] [matrícula] [situação na modalidade] dos atletas da modalidade selecionada. */
    private class ModeloAtletas extends AbstractTableModel {

        private final List<Atleta> atletas = new ArrayList<>();
        private Modalidade modalidade;

        void carregar(Modalidade novaModalidade) {
            modalidade = novaModalidade;
            atletas.clear();
            if (modalidade != null && competicao != null) {
                atletas.addAll(competicao.getAtletasDisponiveis(modalidade, todosAtletas));
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
                return competicao.getAtletas(modalidade).contains(atleta);
            }
            if (coluna == 1) {
                return atleta.getNome();
            }
            if (coluna == 2) {
                return atleta.getMatricula();
            }
            return atleta.getSituacaoNaModalidade(modalidade);
        }

        @Override
        public void setValueAt(Object valor, int linha, int coluna) {
            modalidadeClicada = modalidade;
            atletaClicado = atletas.get(linha);
            marcacaoClicada = Boolean.TRUE.equals(valor);
            if (acaoMarcarAtleta != null) {
                acaoMarcarAtleta.actionPerformed(new ActionEvent(tabelaAtletas, ActionEvent.ACTION_PERFORMED, "atleta"));
            }
        }
    }
}
