package br.com.athletiza.visao.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

public abstract class PainelConsulta<T> extends JPanel implements Recarregavel {
    public enum Acao {
        INCLUIR, EDITAR, EXCLUIR, GERENCIAR
    }

    protected final ModeloTabela<T> modelo;
    protected final JTable tabela;

    public static final int LINHAS_POR_PAGINA = 15;

    private final TableRowSorter<ModeloTabela<T>> ordenador;

    private final TableRowSorter<ModeloTabela<T>> ordemCompleta;
    private int pagina;
    private boolean sincronizando;
    private final JLabel rotuloPaginacao = new JLabel();
    private final JButton botaoAnterior = Botoes.contorno("‹", e -> irParaPagina(pagina - 1));
    private final JButton botaoProxima = Botoes.contorno("›", e -> irParaPagina(pagina + 1));
    private final JTextField campoBusca = new JTextField(24);
    private final JPanel barraBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JPanel linhaTitulo = new JPanel(new BorderLayout());
    private final Map<Acao, JButton> botoesPadrao = new EnumMap<>(Acao.class);
    private final JLabel rotuloTitulo = new JLabel();

    private BooleanSupplier permissao = () -> Sessao.getAcesso().isAcessoTotal();
    private final Navegador navegador;
    private Runnable acaoCarregar = () -> { };
    private Runnable acaoDuploClique;

    protected PainelConsulta(String titulo, ModeloTabela<T> modelo, Set<Acao> acoes, Navegador navegador) {
        super(new BorderLayout(0, 16));
        this.navegador = navegador;
        this.modelo = modelo;
        this.tabela = new JTable(modelo);
        this.ordenador = new TableRowSorter<>(modelo);
        this.ordemCompleta = new TableRowSorter<>(modelo);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(criarCabecalho(titulo, acoes), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(criarTabela(), BorderLayout.CENTER);
        centro.add(criarPaginacao(), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        modelo.addTableModelListener(e -> {
            ordemCompleta.allRowsChanged();
            aplicarPagina();
        });
        aplicarPagina();
        atualizarPermissoes();
    }

    protected PainelConsulta(String titulo, ModeloTabela<T> modelo, Navegador navegador) {
        this(titulo, modelo, EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR), navegador);
    }

    public void adicionarAcaoBtnIncluir(ActionListener acao) {
        adicionarAcao(Acao.INCLUIR, acao);
    }

    public void adicionarAcaoBtnEditar(ActionListener acao) {
        adicionarAcao(Acao.EDITAR, acao);
    }

    public void adicionarAcaoBtnExcluir(ActionListener acao) {
        adicionarAcao(Acao.EXCLUIR, acao);
    }

    public void adicionarAcaoBtnGerenciar(ActionListener acao) {
        adicionarAcao(Acao.GERENCIAR, acao);
    }

    public void adicionarAcaoCarregar(Runnable acao) {
        this.acaoCarregar = acao;
    }

    public void adicionarAcaoDuploClique(Runnable acao) {
        this.acaoDuploClique = acao;
    }

    private void adicionarAcao(Acao acao, ActionListener listener) {
        JButton botao = botoesPadrao.get(acao);
        if (botao != null) {
            botao.addActionListener(listener);
        }
    }

    public void definirPermissao(BooleanSupplier permissao) {
        this.permissao = permissao;
        atualizarPermissoes();
    }

    public boolean podeIncluir() {
        return permissao.getAsBoolean();
    }

    @Override
    public void carregar() {
        atualizarPermissoes();
        acaoCarregar.run();
    }

    public void apresentarDados(List<T> dados) {
        modelo.setLinhas(dados);
    }

    public T getSelecionado() {
        int linhaVisivel = tabela.getSelectedRow();
        if (linhaVisivel < 0) {
            Mensagens.aviso(this, "Selecione um registro na tabela.");
            return null;
        }
        return modelo.getLinha(tabela.convertRowIndexToModel(linhaVisivel));
    }

    public boolean confirmarExclusao(T selecionado) {
        return Mensagens.confirmar(this, getPerguntaExclusao(selecionado));
    }

    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    public void apresentarMensagem(String mensagem) {
        Mensagens.sucesso(this, mensagem);
    }

    public void apresentarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    public void apresentarErro(Exception erro) {
        Mensagens.erro(this, erro);
    }

    public Navegador getNavegador() {
        return navegador;
    }

    public void apresentarTela() {
        navegador.abrir(this);
    }

    public void voltar() {
        navegador.voltar();
    }

    protected void adicionarFiltro(javax.swing.JComponent filtro) {
        barraFiltros.add(javax.swing.Box.createHorizontalStrut(8));
        barraFiltros.add(filtro);
    }

    protected void adicionarBotao(JButton botao) {
        barraBotoes.add(botao, 0);
    }

    protected void exibirVoltar(Runnable acao) {
        JButton voltar = Botoes.voltar(e -> acao.run());
        linhaTitulo.remove(rotuloTitulo);
        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titulo.setOpaque(false);
        titulo.add(voltar);
        titulo.add(Box.createHorizontalStrut(12));
        titulo.add(rotuloTitulo);
        linhaTitulo.add(titulo, BorderLayout.WEST);
    }

    protected void adicionarPainelLateral(JComponent lateral) {
        lateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 16), lateral.getBorder()));
        add(lateral, BorderLayout.WEST);
    }

    protected void renomearBotao(Acao acao, String texto) {
        JButton botao = botoesPadrao.get(acao);
        if (botao != null) {
            botao.setText(texto);
        }
    }

    protected String getPerguntaExclusao(T selecionado) {
        return "Deseja realmente excluir \"" + selecionado + "\"?";
    }

    protected void limparOrdenacaoDaTabela() {
        ordenador.setSortKeys(null);
    }

    protected int getPagina() {
        return pagina;
    }

    protected int getTotalPaginas() {
        int total = ordemCompleta.getViewRowCount();
        return Math.max(1, (total + LINHAS_POR_PAGINA - 1) / LINHAS_POR_PAGINA);
    }

    private JPanel criarPaginacao() {
        rotuloPaginacao.setForeground(Cores.TEXTO_SECUNDARIO);
        botaoAnterior.setToolTipText("Página anterior");
        botaoProxima.setToolTipText("Próxima página");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoes.setOpaque(false);
        botoes.add(botaoAnterior);
        botoes.add(botaoProxima);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(rotuloPaginacao, BorderLayout.WEST);
        barra.add(botoes, BorderLayout.EAST);
        return barra;
    }

    private void irParaPagina(int nova) {
        pagina = nova;
        aplicarPagina();
    }

    private void aplicarPagina() {
        int total = ordemCompleta.getViewRowCount();
        int paginas = getTotalPaginas();
        pagina = Math.max(0, Math.min(pagina, paginas - 1));
        int inicio = pagina * LINHAS_POR_PAGINA;
        int fim = inicio + LINHAS_POR_PAGINA;
        ordenador.setRowFilter(new RowFilter<ModeloTabela<T>, Integer>() {
            @Override
            public boolean include(Entry<? extends ModeloTabela<T>, ? extends Integer> linha) {
                int indice = linha.getIdentifier();
                if (indice >= ordemCompleta.getModelRowCount()) {
                    return false;
                }
                int posicao = ordemCompleta.convertRowIndexToView(indice);
                return posicao >= inicio && posicao < fim;
            }
        });
        rotuloPaginacao.setText(total + " registro(s)" + (paginas > 1 ? " · página " + (pagina + 1) + " de " + paginas : ""));
        botaoAnterior.setVisible(paginas > 1);
        botaoProxima.setVisible(paginas > 1);
        botaoAnterior.setEnabled(pagina > 0);
        botaoProxima.setEnabled(pagina < paginas - 1);
    }

    protected void larguraColuna(int coluna, int largura) {
        tabela.getColumnModel().getColumn(coluna).setPreferredWidth(largura);
    }

    private JPanel criarCabecalho(String titulo, Set<Acao> acoes) {
        rotuloTitulo.setText(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo());

        if (acoes.contains(Acao.EXCLUIR)) {
            adicionarBotao(Acao.EXCLUIR, Botoes.contornoVermelho("Excluir", null));
        }
        if (acoes.contains(Acao.EDITAR)) {
            adicionarBotao(Acao.EDITAR, Botoes.contorno("Editar", null));
        }
        if (acoes.contains(Acao.GERENCIAR)) {
            adicionarBotao(Acao.GERENCIAR, Botoes.contornoVerde("Gerenciar", null));
        }
        if (acoes.contains(Acao.INCLUIR)) {
            adicionarBotao(Acao.INCLUIR, Botoes.destaque("+ Incluir", null));
        }

        linhaTitulo.setOpaque(false);
        linhaTitulo.add(rotuloTitulo, BorderLayout.WEST);
        linhaTitulo.add(barraBotoes, BorderLayout.EAST);

        campoBusca.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar...");
        campoBusca.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });
        barraFiltros.setOpaque(false);
        barraFiltros.add(campoBusca);

        JPanel cabecalho = new JPanel(new BorderLayout(0, 16));
        cabecalho.setOpaque(false);
        cabecalho.add(linhaTitulo, BorderLayout.NORTH);
        cabecalho.add(barraFiltros, BorderLayout.SOUTH);
        return cabecalho;
    }

    private JScrollPane criarTabela() {
        TableStringConverter conversor = new TableStringConverter() {
            @Override
            public String toString(TableModel modeloTabela, int linha, int coluna) {
                return Renderizadores.formatar(modeloTabela.getValueAt(linha, coluna));
            }
        };
        ordenador.setStringConverter(conversor);
        ordemCompleta.setStringConverter(conversor);
        tabela.setRowSorter(ordenador);

        ordenador.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED && !sincronizando) {
                sincronizando = true;
                try {
                    ordemCompleta.setSortKeys(ordenador.getSortKeys());
                    pagina = 0;
                    aplicarPagina();
                } finally {
                    sincronizando = false;
                }
            }
        });
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        Renderizadores.aplicarPadroes(tabela);
        garantirLarguraDeDatas();

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0) {
                    clicarDuasVezes();
                }
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        return rolagem;
    }

    private void garantirLarguraDeDatas() {
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            Class<?> tipo = modelo.getColumnClass(i);
            if (tipo == java.time.LocalDate.class) {
                tabela.getColumnModel().getColumn(i).setMinWidth(100);
            } else if (tipo == java.time.LocalTime.class) {
                tabela.getColumnModel().getColumn(i).setMinWidth(70);
            }
        }
    }

    private void adicionarBotao(Acao acao, JButton botao) {
        botoesPadrao.put(acao, botao);
        barraBotoes.add(botao);
    }

    private void atualizarPermissoes() {
        boolean habilitado = podeIncluir();
        botoesPadrao.forEach((acao, botao) -> {
            if (acao != Acao.GERENCIAR) {
                botao.setEnabled(habilitado);
                botao.setToolTipText(habilitado ? null : "Seu perfil não permite esta ação.");
            }
        });
    }

    private void clicarDuasVezes() {
        JButton editar = botoesPadrao.get(Acao.EDITAR);
        if (acaoDuploClique != null) {
            acaoDuploClique.run();
        } else if (editar != null && editar.isEnabled()) {
            editar.doClick();
        }
    }

    private void filtrar() {
        String texto = campoBusca.getText().trim();
        ordemCompleta.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        pagina = 0;
        aplicarPagina();
    }
}
