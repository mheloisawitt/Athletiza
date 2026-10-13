package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

/**
 * Painel base das telas de consulta (CH-03, CH-44): título, botões de ação,
 * campo de busca e JTable com ordenação ao clicar no cabeçalho.
 *
 * Segue as regras da View no MVC: exibe a lista recebida (exibir), informa a
 * linha selecionada (getSelecionado) e recebe do Controller as ações dos
 * botões (adicionarAcaoIncluir, adicionarAcaoEditar...). Não acessa o banco.
 *
 * @param <T> tipo dos objetos listados
 */
public abstract class PainelConsulta<T> extends JPanel implements Recarregavel {

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
    private final JPanel linhaTitulo = new JPanel(new BorderLayout());
    private final Map<Acao, JButton> botoesPadrao = new EnumMap<>(Acao.class);
    private final JLabel rotuloTitulo = new JLabel();
    private final JButton btnVoltar = new JButton("‹ Voltar");
    private ActionListener acaoCarregar;
    private ActionListener acaoClicarDuasVezes;

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

    /* Dados exibidos */

    /** Mostra os registros na tabela (chamado pelo Controller). */
    public void exibir(List<T> registros) {
        modelo.setLinhas(registros);
    }

    /** Objeto da linha selecionada, considerando ordenação e filtro; null se nenhuma estiver selecionada. */
    public T getSelecionado() {
        int linhaVisivel = tabela.getSelectedRow();
        if (linhaVisivel < 0) {
            return null;
        }
        return modelo.getLinha(tabela.convertRowIndexToModel(linhaVisivel));
    }

    /** Seleciona a primeira linha (usado nos testes e após incluir um registro). */
    public void selecionarLinha(int linha) {
        if (linha >= 0 && linha < tabela.getRowCount()) {
            tabela.setRowSelectionInterval(linha, linha);
        }
    }

    public void setTitulo(String titulo) {
        rotuloTitulo.setText(titulo);
    }

    /* Ações: registradas pelo Controller */

    /**
     * Ação executada sempre que a tela precisa buscar os dados de novo
     * (ao abrir, ao voltar de outra tela, ao mudar um filtro).
     */
    public void adicionarAcaoCarregar(ActionListener acao) {
        this.acaoCarregar = acao;
    }

    /** Recarrega os dados da tabela: dispara a ação registrada pelo Controller. */
    @Override
    public void carregar() {
        if (acaoCarregar != null) {
            acaoCarregar.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "carregar"));
        }
    }

    public void adicionarAcaoIncluir(ActionListener acao) {
        adicionarAcao(Acao.INCLUIR, acao);
    }

    /** Editar também é disparado pelo clique duplo na linha (se o perfil permitir). */
    public void adicionarAcaoEditar(ActionListener acao) {
        adicionarAcao(Acao.EDITAR, acao);
        if (acaoClicarDuasVezes == null && podeAlterar()) {
            acaoClicarDuasVezes = acao;
        }
    }

    public void adicionarAcaoExcluir(ActionListener acao) {
        adicionarAcao(Acao.EXCLUIR, acao);
    }

    public void adicionarAcaoGerenciar(ActionListener acao) {
        adicionarAcao(Acao.GERENCIAR, acao);
    }

    /** Troca o que acontece no clique duplo em uma linha (por padrão, editar). */
    public void adicionarAcaoClicarDuasVezes(ActionListener acao) {
        this.acaoClicarDuasVezes = acao;
    }

    public void adicionarAcaoVoltar(ActionListener acao) {
        btnVoltar.addActionListener(acao);
    }

    public JButton getBotao(Acao acao) {
        return botoesPadrao.get(acao);
    }

    /* Mensagens ao usuário */

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

    /* Montagem da tela (usado pelas subclasses) */

    /** Adiciona um filtro extra (ex.: combo de modalidade) ao lado do campo de busca. */
    protected void adicionarFiltro(JComponent filtro) {
        barraFiltros.add(Box.createHorizontalStrut(8));
        barraFiltros.add(filtro);
    }

    /** Adiciona um botão extra ao cabeçalho, à esquerda dos botões padrão. */
    protected void adicionarBotao(JButton botao) {
        barraBotoes.add(botao, 0);
    }

    /** Mostra "‹ Voltar" antes do título (para consultas abertas a partir de outra tela). */
    protected void exibirVoltar() {
        btnVoltar.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; margin:2,0,2,8");
        linhaTitulo.remove(rotuloTitulo);
        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titulo.setOpaque(false);
        titulo.add(btnVoltar);
        titulo.add(rotuloTitulo);
        linhaTitulo.add(titulo, BorderLayout.WEST);
    }

    /** Coloca um painel à esquerda da tabela (ex.: lista de modalidades na tela de treinos). */
    protected void adicionarPainelLateral(JComponent lateral) {
        lateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 16), lateral.getBorder()));
        add(lateral, BorderLayout.WEST);
    }

    /** Troca o texto de um botão padrão (ex.: "Excluir" vira "Remover"). */
    protected void renomearBotao(Acao acao, String texto) {
        JButton botao = botoesPadrao.get(acao);
        if (botao != null) {
            botao.setText(texto);
        }
    }

    /** Desfaz a ordenação feita pelo clique no cabeçalho, mantendo a ordem em que os dados vieram. */
    protected void limparOrdenacaoDaTabela() {
        ordenador.setSortKeys(null);
    }

    /** Ajusta a largura preferida de uma coluna. */
    protected void larguraColuna(int coluna, int largura) {
        tabela.getColumnModel().getColumn(coluna).setPreferredWidth(largura);
    }

    /** Indica se o usuário logado pode incluir, editar e excluir (sem login, nos testes, pode). */
    protected static boolean podeAlterar() {
        return Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
    }

    /**
     * Botões que dependem de uma linha selecionada só disparam a ação do
     * Controller se houver seleção; senão, avisam o usuário.
     */
    private void adicionarAcao(Acao acao, ActionListener listener) {
        JButton botao = botoesPadrao.get(acao);
        if (botao == null) {
            return;
        }
        if (acao == Acao.INCLUIR) {
            botao.addActionListener(listener);
            return;
        }
        botao.addActionListener(e -> {
            if (getSelecionado() == null) {
                mostrarAviso("Selecione um registro na tabela.");
            } else {
                listener.actionPerformed(e);
            }
        });
    }

    private JPanel criarCabecalho(String titulo, Set<Acao> acoes) {
        rotuloTitulo.setText(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo());

        boolean podeAlterar = podeAlterar();
        if (acoes.contains(Acao.EXCLUIR)) {
            adicionarBotaoPadrao(Acao.EXCLUIR, Botoes.contornoVermelho("Excluir", null), podeAlterar);
        }
        if (acoes.contains(Acao.EDITAR)) {
            adicionarBotaoPadrao(Acao.EDITAR, Botoes.contorno("Editar", null), podeAlterar);
        }
        if (acoes.contains(Acao.GERENCIAR)) {
            adicionarBotaoPadrao(Acao.GERENCIAR, Botoes.contornoVerde("Gerenciar", null), true);
        }
        if (acoes.contains(Acao.INCLUIR)) {
            adicionarBotaoPadrao(Acao.INCLUIR, Botoes.destaque("+ Incluir", null), podeAlterar);
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
        Renderizadores.aplicarPadroes(tabela);
        garantirLarguraDeDatas();

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSelecionado() != null && acaoClicarDuasVezes != null) {
                    acaoClicarDuasVezes.actionPerformed(new ActionEvent(tabela, ActionEvent.ACTION_PERFORMED, "abrir"));
                }
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        return rolagem;
    }

    /** Datas e horários nunca aparecem cortados, qualquer que seja o tamanho da janela. */
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

    private void adicionarBotaoPadrao(Acao acao, JButton botao, boolean habilitado) {
        botoesPadrao.put(acao, botao);
        botao.setEnabled(habilitado);
        if (!habilitado) {
            botao.setToolTipText("Seu perfil não permite esta ação.");
        }
        barraBotoes.add(botao);
    }

    private void filtrar() {
        String texto = campoBusca.getText().trim();
        ordenador.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }
}
