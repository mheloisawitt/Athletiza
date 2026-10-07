package br.com.athletiza.view.componentes;

import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

/**
 * Painel base das telas de consulta (CH-03, CH-44): título, botões de ação,
 * campo de busca e JTable com ordenação ao clicar no cabeçalho.
 *
 * Cada tela de consulta estende esta classe e implementa apenas o que é
 * específico: de onde vêm os dados e o que fazer em cada botão.
 *
 * @param <T> tipo dos objetos listados
 */
public abstract class PainelConsulta<T> extends JPanel {

    /** Botões que a tela de consulta exibe. */
    public enum Acao {
        INCLUIR, EDITAR, EXCLUIR, GERENCIAR
    }

    protected final ModeloTabela<T> modelo;
    protected final JTable tabela;
    private final TableRowSorter<ModeloTabela<T>> ordenador;
    private final JTextField campoBusca = new JTextField(24);
    private final JPanel barraBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    protected PainelConsulta(String titulo, ModeloTabela<T> modelo, Set<Acao> acoes) {
        super(new BorderLayout(0, 16));
        this.modelo = modelo;
        this.tabela = new JTable(modelo);
        this.ordenador = new TableRowSorter<>(modelo);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(criarCabecalho(titulo, acoes), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);
    }

    protected PainelConsulta(String titulo, ModeloTabela<T> modelo) {
        this(titulo, modelo, EnumSet.of(Acao.INCLUIR, Acao.EDITAR, Acao.EXCLUIR));
    }

    /** Busca os registros a exibir (normalmente pelo controller). */
    protected abstract List<T> buscarDados() throws AthletizaException;

    protected void incluir() {
    }

    protected void editar(T selecionado) {
    }

    protected void excluir(T selecionado) {
    }

    protected void gerenciar(T selecionado) {
    }

    /** Recarrega os dados da tabela. Chamado ao abrir a tela e após cada alteração. */
    public void carregar() {
        try {
            modelo.setLinhas(buscarDados());
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        carregar();
    }

    /** Objeto da linha selecionada, considerando ordenação e filtro. */
    protected Optional<T> getSelecionado() {
        int linhaVisivel = tabela.getSelectedRow();
        if (linhaVisivel < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getLinha(tabela.convertRowIndexToModel(linhaVisivel)));
    }

    /** Adiciona um filtro extra (ex.: combo de modalidade) ao lado do campo de busca. */
    protected void adicionarFiltro(javax.swing.JComponent filtro) {
        barraFiltros.add(javax.swing.Box.createHorizontalStrut(8));
        barraFiltros.add(filtro);
    }

    /** Ajusta a largura preferida de uma coluna. */
    protected void larguraColuna(int coluna, int largura) {
        tabela.getColumnModel().getColumn(coluna).setPreferredWidth(largura);
    }

    private JPanel criarCabecalho(String titulo, Set<Acao> acoes) {
        JLabel rotuloTitulo = new JLabel(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo());

        boolean podeAlterar = Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
        if (acoes.contains(Acao.EXCLUIR)) {
            adicionarBotao(Botoes.contornoVermelho("Excluir", e -> comSelecionado(this::confirmarExclusao)), podeAlterar);
        }
        if (acoes.contains(Acao.EDITAR)) {
            adicionarBotao(Botoes.contorno("Editar", e -> comSelecionado(this::editar)), podeAlterar);
        }
        if (acoes.contains(Acao.GERENCIAR)) {
            adicionarBotao(Botoes.contornoVerde("Gerenciar", e -> comSelecionado(this::gerenciar)), true);
        }
        if (acoes.contains(Acao.INCLUIR)) {
            adicionarBotao(Botoes.destaque("+ Incluir", e -> incluir()), podeAlterar);
        }

        JPanel linhaTitulo = new JPanel(new BorderLayout());
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
        ordenador.setStringConverter(new TableStringConverter() {
            @Override
            public String toString(TableModel modeloTabela, int linha, int coluna) {
                return Renderizadores.formatar(modeloTabela.getValueAt(linha, coluna));
            }
        });
        tabela.setRowSorter(ordenador);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        alinharCabecalho();
        Renderizadores.aplicarPadroes(tabela);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0
                        && (Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados())) {
                    getSelecionado().ifPresent(PainelConsulta.this::editar);
                }
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        return rolagem;
    }

    /** Títulos alinhados como o conteúdo: à esquerda, e centralizados nas colunas de situação. */
    private void alinharCabecalho() {
        javax.swing.table.TableCellRenderer padrao = tabela.getTableHeader().getDefaultRenderer();
        tabela.getTableHeader().setDefaultRenderer((tab, valor, selecionado, foco, linha, coluna) -> {
            java.awt.Component componente = padrao.getTableCellRendererComponent(tab, valor, selecionado, foco, linha, coluna);
            if (componente instanceof JLabel rotulo) {
                boolean situacao = Enum.class.isAssignableFrom(tab.getColumnClass(coluna));
                rotulo.setHorizontalAlignment(situacao ? JLabel.CENTER : JLabel.LEFT);
            }
            return componente;
        });
    }

    private void adicionarBotao(JButton botao, boolean habilitado) {
        botao.setEnabled(habilitado);
        if (!habilitado) {
            botao.setToolTipText("Seu perfil não permite esta ação.");
        }
        barraBotoes.add(botao);
    }

    private void comSelecionado(java.util.function.Consumer<T> acao) {
        getSelecionado().ifPresentOrElse(acao, () -> Mensagens.aviso(this, "Selecione um registro na tabela."));
    }

    private void confirmarExclusao(T selecionado) {
        if (Mensagens.confirmar(this, "Deseja realmente excluir \"" + selecionado + "\"?")) {
            excluir(selecionado);
        }
    }

    private void filtrar() {
        String texto = campoBusca.getText().trim();
        ordenador.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }
}
