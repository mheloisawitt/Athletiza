package br.com.athletiza.view.registros;

import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import br.com.athletiza.model.TipoArquivo;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Recarregavel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Registros (CH-42, CH-43): galeria de fotos e vídeos da atlética organizada em pastas.
 * Pastas à esquerda; miniaturas da pasta selecionada à direita.
 * As ações ficam no RegistrosController.
 */
public class PainelRegistros extends JPanel implements Recarregavel {

    /** Miniaturas já geradas, para não ler as imagens do disco a cada atualização. */
    private static final Map<String, Image> MINIATURAS = new ConcurrentHashMap<>();

    private final DefaultListModel<Pasta> pastas = new DefaultListModel<>();
    private final JList<Pasta> listaPastas = new JList<>(pastas);
    private final JLabel caminho = new JLabel();
    private final JPanel grade = new JPanel();
    private final JScrollPane rolagemGrade = new JScrollPane();
    private final List<CartaoArquivo> cartoes = new ArrayList<>();
    private final boolean podeAlterar = Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
    private final JButton btnNovaPasta = Botoes.contornoRoxo("+ Nova Pasta", null);
    private final JButton btnAdicionar = Botoes.contornoVerde("+ Adicionar Arquivo", null);
    private CartaoArquivo selecionado;
    private boolean atualizandoPastas;
    /** Pasta do computador onde ficam as mídias (informada pelo Controller). */
    private Path pastaDasMidias;

    /* Campos do formulário "Renomear ou mover" (diálogo) */
    private final JTextField nomeArquivo = new JTextField(28);
    private final JComboBox<Pasta> pastaArquivo = new JComboBox<>();
    private final JTextField descricaoArquivo = new JTextField();

    /* Ações registradas pelo Controller e o que o usuário escolheu */
    private ActionListener acaoCarregar;
    private ActionListener acaoSelecionarPasta;
    private ActionListener acaoRenomearPasta;
    private ActionListener acaoExcluirPasta;
    private ActionListener acaoAdicionarArquivos;
    private ActionListener acaoAbrirArquivo;
    private ActionListener acaoEditarArquivo;
    private ActionListener acaoExcluirArquivo;
    private Pasta pastaClicada;
    private Arquivo arquivoClicado;
    private List<Path> arquivosEscolhidos = new ArrayList<>();

    public PainelRegistros() {
        super(new BorderLayout(0, 20));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(criarCabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);
        corpo.add(criarListaPastas(), BorderLayout.WEST);
        corpo.add(criarAreaArquivos(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    /* Ações: registradas pelo Controller */

    public void adicionarAcaoCarregar(ActionListener acao) {
        this.acaoCarregar = acao;
    }

    @Override
    public void carregar() {
        disparar(acaoCarregar, "carregar");
    }

    /** A pasta selecionada na lista mudou (ver getPastaSelecionada). */
    public void adicionarAcaoSelecionarPasta(ActionListener acao) {
        this.acaoSelecionarPasta = acao;
    }

    public void adicionarAcaoNovaPasta(ActionListener acao) {
        btnNovaPasta.addActionListener(acao);
    }

    /** Menu da pasta, botão direito (ver getPastaClicada). */
    public void adicionarAcaoRenomearPasta(ActionListener acao) {
        this.acaoRenomearPasta = acao;
    }

    public void adicionarAcaoExcluirPasta(ActionListener acao) {
        this.acaoExcluirPasta = acao;
    }

    /** Arquivos escolhidos no seletor ou arrastados para a tela (ver getArquivosEscolhidos). */
    public void adicionarAcaoAdicionarArquivos(ActionListener acao) {
        this.acaoAdicionarArquivos = acao;
    }

    /** Clique duplo ou "Abrir" no menu do arquivo (ver getArquivoClicado). */
    public void adicionarAcaoAbrirArquivo(ActionListener acao) {
        this.acaoAbrirArquivo = acao;
    }

    public void adicionarAcaoEditarArquivo(ActionListener acao) {
        this.acaoEditarArquivo = acao;
    }

    public void adicionarAcaoExcluirArquivo(ActionListener acao) {
        this.acaoExcluirArquivo = acao;
    }

    public Pasta getPastaSelecionada() {
        return listaPastas.getSelectedValue();
    }

    public Pasta getPastaClicada() {
        return pastaClicada;
    }

    public Arquivo getArquivoClicado() {
        return arquivoClicado;
    }

    public List<Path> getArquivosEscolhidos() {
        return arquivosEscolhidos;
    }

    /* Dados exibidos */

    public void setPastaDasMidias(Path pasta) {
        this.pastaDasMidias = pasta;
    }

    /** Mostra as pastas, mantendo a selecionada (ou a primeira). Não dispara a seleção. */
    public void exibirPastas(List<Pasta> lista) {
        Pasta atual = listaPastas.getSelectedValue();
        atualizandoPastas = true;
        try {
            pastas.clear();
            for (Pasta pasta : lista) {
                pastas.addElement(pasta);
            }
            int indice = atual == null ? 0 : Math.max(0, pastas.indexOf(atual));
            if (!pastas.isEmpty()) {
                listaPastas.setSelectedIndex(indice);
            }
        } finally {
            atualizandoPastas = false;
        }
    }

    public void selecionarPasta(Pasta pasta) {
        listaPastas.setSelectedValue(pasta, true);
    }

    /** Mostra os arquivos da pasta em miniaturas (as imagens são lidas em segundo plano). */
    public void exibirArquivos(Pasta pasta, List<Arquivo> arquivos) {
        cartoes.clear();
        selecionado = null;
        caminho.setText(pasta == null ? "Nenhuma pasta" : "Pastas  ›  " + pasta.getNome());
        for (Arquivo arquivo : arquivos) {
            cartoes.add(criarCartao(arquivo));
        }
        organizarGrade();
        gerarMiniaturas();
    }

    /** Esquece a miniatura de um arquivo excluído. */
    public void removerMiniatura(Arquivo arquivo) {
        MINIATURAS.remove(arquivo.getCaminho());
    }

    /** Muda o cursor enquanto os arquivos são copiados. */
    public void setOcupado(boolean ocupado) {
        setCursor(ocupado ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    /**
     * Pede o nome de uma pasta.
     *
     * @return o nome digitado, ou null se o usuário cancelou
     */
    public String pedirNomePasta(String titulo, String nomeAtual) {
        return (String) JOptionPane.showInputDialog(this, "Nome da pasta:", titulo, JOptionPane.PLAIN_MESSAGE,
                null, null, nomeAtual);
    }

    /** Diálogo "Renomear ou mover". @return true se o usuário clicou em OK */
    public boolean exibirFormularioArquivo(Arquivo arquivo, List<Pasta> opcoes) {
        nomeArquivo.setText(arquivo.getNome());
        pastaArquivo.removeAllItems();
        for (Pasta pasta : opcoes) {
            pastaArquivo.addItem(pasta);
        }
        pastaArquivo.setSelectedItem(arquivo.getPasta());
        descricaoArquivo.setText(arquivo.getDescricao());
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
        campos.add(new JLabel("Nome *"));
        campos.add(nomeArquivo);
        campos.add(new JLabel("Pasta"));
        campos.add(pastaArquivo);
        campos.add(new JLabel("Descrição"));
        campos.add(descricaoArquivo);
        return JOptionPane.showConfirmDialog(this, campos, "Renomear ou mover", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public String getNomeArquivo() {
        return nomeArquivo.getText().trim();
    }

    public Pasta getPastaArquivo() {
        return (Pasta) pastaArquivo.getSelectedItem();
    }

    public String getDescricaoArquivo() {
        return descricaoArquivo.getText().isBlank() ? null : descricaoArquivo.getText().trim();
    }

    /** Abre o arquivo no programa padrão do computador (visualizador de fotos, player de vídeo). */
    public void abrirNoComputador(Path local) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(local.toFile());
            } else {
                mostrarAviso("Não foi possível abrir automaticamente. O arquivo está em:\n" + local);
            }
        } catch (IOException e) {
            mostrarAviso("Não há um programa para abrir este arquivo. Ele está em:\n" + local);
        }
    }

    /* Mensagens */

    public void mostrarMensagem(String mensagem) {
        Mensagens.sucesso(this, mensagem);
    }

    public void mostrarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    public void mostrarErro(Throwable erro) {
        Mensagens.erro(this, erro);
    }

    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    /* Montagem da tela */

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("Registros");
        titulo.setFont(Tema.fonteTitulo());
        btnAdicionar.addActionListener(e -> escolherArquivos());
        btnNovaPasta.setEnabled(podeAlterar);
        btnAdicionar.setEnabled(podeAlterar);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(btnNovaPasta);
        botoes.add(btnAdicionar);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(botoes, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarListaPastas() {
        listaPastas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPastas.setFixedCellHeight(36);
        listaPastas.setBackground(Cores.FUNDO_CARTAO);
        DefaultListCellRenderer padrao = new DefaultListCellRenderer();
        listaPastas.setCellRenderer((lista, pasta, indice, selecionada, foco) -> {
            JLabel rotulo = (JLabel) padrao.getListCellRendererComponent(lista,
                    pasta.getNome() + "  (" + pasta.getQuantidadeArquivos() + ")", indice, selecionada, false);
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (selecionada) {
                rotulo.setBackground(Cores.VERDE_SELECAO);
                rotulo.setForeground(Cores.VERDE);
            }
            return rotulo;
        });
        listaPastas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizandoPastas) {
                disparar(acaoSelecionarPasta, "selecionarPasta");
            }
        });
        listaPastas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                abrirMenuDaPasta(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                abrirMenuDaPasta(e);
            }
        });

        JLabel titulo = new JLabel("Pastas");
        titulo.setFont(Tema.fonte(Font.BOLD, 13f));
        titulo.setForeground(Cores.TEXTO_SECUNDARIO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        JScrollPane rolagem = new JScrollPane(listaPastas);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setOpaque(false);
        lateral.setPreferredSize(new Dimension(210, 0));
        lateral.add(titulo, BorderLayout.NORTH);
        lateral.add(rolagem, BorderLayout.CENTER);
        return lateral;
    }

    private JPanel criarAreaArquivos() {
        caminho.setFont(Tema.fonte(Font.BOLD, 13f));
        JLabel dica = new JLabel(podeAlterar
                ? "Clique duplo abre o arquivo · botão direito para mais opções · arraste arquivos para cá"
                : "Clique duplo abre o arquivo");
        dica.setFont(Tema.fonte(Font.PLAIN, 11f));
        dica.setForeground(Cores.TEXTO_SECUNDARIO);
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        topo.add(caminho, BorderLayout.WEST);
        topo.add(dica, BorderLayout.EAST);

        grade.setOpaque(false);
        JPanel alinhamento = new JPanel(new BorderLayout());
        alinhamento.setBackground(Cores.FUNDO_CARTAO);
        alinhamento.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        alinhamento.add(grade, BorderLayout.NORTH);
        rolagemGrade.setViewportView(alinhamento);
        rolagemGrade.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        rolagemGrade.getViewport().setBackground(Cores.FUNDO_CARTAO);
        rolagemGrade.getVerticalScrollBar().setUnitIncrement(24);
        rolagemGrade.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                organizarGrade();
            }
        });
        if (podeAlterar) {
            TransferHandler soltarArquivos = new SoltarArquivos();
            rolagemGrade.setTransferHandler(soltarArquivos);
            alinhamento.setTransferHandler(soltarArquivos);
        }

        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);
        area.add(topo, BorderLayout.NORTH);
        area.add(rolagemGrade, BorderLayout.CENTER);
        return area;
    }

    private CartaoArquivo criarCartao(Arquivo arquivo) {
        CartaoArquivo cartao = new CartaoArquivo(arquivo);
        cartao.setMiniatura(MINIATURAS.get(arquivo.getCaminho()));
        cartao.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selecionar(cartao);
                if (e.isPopupTrigger()) {
                    abrirMenuDoArquivo(cartao, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    abrirMenuDoArquivo(cartao, e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    arquivoClicado = cartao.getArquivo();
                    disparar(acaoAbrirArquivo, "abrirArquivo");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cartao.setMouseSobre(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cartao.setMouseSobre(false);
            }
        });
        return cartao;
    }

    /** Distribui os cartões em quantas colunas couberem na largura disponível. */
    private void organizarGrade() {
        grade.removeAll();
        if (cartoes.isEmpty()) {
            grade.setLayout(new GridBagLayout());
            JLabel vazio = new JLabel(podeAlterar
                    ? "Esta pasta está vazia. Use \"+ Adicionar Arquivo\" ou arraste fotos e vídeos para cá."
                    : "Esta pasta está vazia.", SwingConstants.CENTER);
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            vazio.setBorder(BorderFactory.createEmptyBorder(80, 0, 80, 0));
            grade.add(vazio);
        } else {
            int largura = Math.max(rolagemGrade.getViewport().getWidth() - 32, CartaoArquivo.LARGURA);
            int colunas = Math.max(1, (largura + 16) / (CartaoArquivo.LARGURA + 16));
            JPanel linhas = new JPanel(new GridLayout(0, colunas, 16, 18));
            linhas.setOpaque(false);
            for (CartaoArquivo cartao : cartoes) {
                linhas.add(cartao);
            }
            for (int i = cartoes.size(); i % colunas != 0; i++) {
                JPanel vazio = new JPanel();
                vazio.setOpaque(false);
                linhas.add(vazio);
            }
            grade.setLayout(new BorderLayout());
            grade.add(linhas, BorderLayout.WEST);
        }
        grade.revalidate();
        grade.repaint();
    }

    /** Lê as imagens em segundo plano e mostra as miniaturas conforme ficam prontas. */
    private void gerarMiniaturas() {
        List<CartaoArquivo> pendentes = new ArrayList<>();
        for (CartaoArquivo cartao : cartoes) {
            if (cartao.getArquivo().isImagem() && !MINIATURAS.containsKey(cartao.getArquivo().getCaminho())) {
                pendentes.add(cartao);
            }
        }
        if (pendentes.isEmpty() || pastaDasMidias == null) {
            return;
        }
        new SwingWorker<Void, CartaoArquivo>() {
            @Override
            protected Void doInBackground() {
                for (CartaoArquivo cartao : pendentes) {
                    try {
                        File arquivo = pastaDasMidias.resolve(cartao.getArquivo().getCaminho()).toFile();
                        BufferedImage original = ImageIO.read(arquivo);
                        if (original != null) {
                            MINIATURAS.put(cartao.getArquivo().getCaminho(), reduzir(original));
                            publish(cartao);
                        }
                    } catch (IOException e) {
                        // Arquivo ausente ou ilegível: o cartão fica sem miniatura (não é um erro para o usuário).
                    }
                }
                return null;
            }

            @Override
            protected void process(List<CartaoArquivo> prontos) {
                for (CartaoArquivo cartao : prontos) {
                    cartao.setMiniatura(MINIATURAS.get(cartao.getArquivo().getCaminho()));
                }
            }
        }.execute();
    }

    /** Reduz a imagem para o dobro do tamanho do cartão (nítida também em telas de alta resolução). */
    private static Image reduzir(BufferedImage original) {
        double escala = Math.min(1.0, Math.max(CartaoArquivo.LARGURA * 2.0 / original.getWidth(),
                CartaoArquivo.ALTURA_IMAGEM * 2.0 / original.getHeight()));
        int largura = Math.max(1, (int) (original.getWidth() * escala));
        int altura = Math.max(1, (int) (original.getHeight() * escala));
        return original.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
    }

    private void selecionar(CartaoArquivo cartao) {
        if (selecionado != null) {
            selecionado.setSelecionado(false);
        }
        selecionado = cartao;
        cartao.setSelecionado(true);
    }

    private void escolherArquivos() {
        if (listaPastas.getSelectedValue() == null) {
            mostrarAviso("Crie ou selecione uma pasta primeiro.");
            return;
        }
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Adicionar arquivos à pasta " + listaPastas.getSelectedValue());
        seletor.setMultiSelectionEnabled(true);
        seletor.setFileFilter(new FileNameExtensionFilter("Fotos e vídeos", TipoArquivo.todasAsExtensoes()));
        if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            avisarArquivosEscolhidos(seletor.getSelectedFiles());
        }
    }

    private void avisarArquivosEscolhidos(File[] arquivos) {
        arquivosEscolhidos = new ArrayList<>();
        for (File arquivo : arquivos) {
            arquivosEscolhidos.add(arquivo.toPath());
        }
        disparar(acaoAdicionarArquivos, "adicionarArquivos");
    }

    private void abrirMenuDaPasta(MouseEvent e) {
        if (!e.isPopupTrigger() || !podeAlterar) {
            return;
        }
        int indice = listaPastas.locationToIndex(e.getPoint());
        if (indice < 0) {
            return;
        }
        listaPastas.setSelectedIndex(indice);
        pastaClicada = pastas.get(indice);
        JPopupMenu menu = new JPopupMenu();
        menu.add(item("Renomear pasta", acaoRenomearPasta));
        menu.add(item("Excluir pasta", acaoExcluirPasta));
        menu.show(listaPastas, e.getX(), e.getY());
    }

    private void abrirMenuDoArquivo(CartaoArquivo cartao, MouseEvent e) {
        arquivoClicado = cartao.getArquivo();
        JPopupMenu menu = new JPopupMenu();
        menu.add(item("Abrir", acaoAbrirArquivo));
        if (podeAlterar) {
            menu.add(item("Renomear ou mover...", acaoEditarArquivo));
            menu.addSeparator();
            menu.add(item("Excluir", acaoExcluirArquivo));
        }
        menu.show(cartao, e.getX(), e.getY());
    }

    private static JMenuItem item(String texto, ActionListener acao) {
        JMenuItem item = new JMenuItem(texto);
        if (acao != null) {
            item.addActionListener(acao);
        }
        return item;
    }

    private void disparar(ActionListener acao, String comando) {
        if (acao != null) {
            acao.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, comando));
        }
    }

    /** Aceita fotos e vídeos arrastados do Explorador de Arquivos. */
    private class SoltarArquivos extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport suporte) {
            return suporte.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport suporte) {
            try {
                @SuppressWarnings("unchecked")
                List<File> arquivos = (List<File>) suporte.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                avisarArquivosEscolhidos(arquivos.toArray(new File[0]));
                return true;
            } catch (java.awt.datatransfer.UnsupportedFlavorException | IOException e) {
                mostrarErro(e);
                return false;
            }
        }
    }
}
