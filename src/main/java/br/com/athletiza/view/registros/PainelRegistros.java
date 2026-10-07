package br.com.athletiza.view.registros;

import br.com.athletiza.controller.GaleriaController;
import br.com.athletiza.exception.AthletizaException;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
 */
public class PainelRegistros extends JPanel implements Recarregavel {

    /** Miniaturas já geradas, para não ler as imagens do disco a cada atualização. */
    private static final Map<String, Image> MINIATURAS = new ConcurrentHashMap<>();

    private final GaleriaController controller = new GaleriaController();
    private final DefaultListModel<Pasta> pastas = new DefaultListModel<>();
    private final JList<Pasta> listaPastas = new JList<>(pastas);
    private final JLabel caminho = new JLabel();
    private final JPanel grade = new JPanel();
    private final JScrollPane rolagemGrade = new JScrollPane();
    private final List<CartaoArquivo> cartoes = new ArrayList<>();
    private final boolean podeAlterar = Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
    private CartaoArquivo selecionado;
    private boolean atualizandoPastas;

    public PainelRegistros() {
        super(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(criarCabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);
        corpo.add(criarListaPastas(), BorderLayout.WEST);
        corpo.add(criarAreaArquivos(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("Registros");
        titulo.setFont(Tema.fonteTitulo());
        JButton novaPasta = Botoes.contornoRoxo("+ Nova Pasta", e -> novaPasta());
        JButton adicionar = Botoes.contornoVerde("+ Adicionar Arquivo", e -> escolherArquivos());
        novaPasta.setEnabled(podeAlterar);
        adicionar.setEnabled(podeAlterar);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(novaPasta);
        botoes.add(adicionar);

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
        javax.swing.ListCellRenderer<? super Pasta> padrao = listaPastas.getCellRenderer();
        listaPastas.setCellRenderer((lista, pasta, indice, selecionada, foco) -> {
            @SuppressWarnings("unchecked")
            JLabel rotulo = (JLabel) ((javax.swing.ListCellRenderer<Object>) (javax.swing.ListCellRenderer<?>) padrao)
                    .getListCellRendererComponent(lista, pasta.getNome() + "  (" + pasta.getQuantidadeArquivos() + ")",
                            indice, selecionada, false);
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (selecionada) {
                rotulo.setBackground(Cores.VERDE_SELECAO);
                rotulo.setForeground(Cores.VERDE);
            }
            return rotulo;
        });
        listaPastas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizandoPastas) {
                carregarArquivos();
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

    @Override
    public void carregar() {
        try {
            Pasta atual = listaPastas.getSelectedValue();
            atualizandoPastas = true;
            pastas.clear();
            controller.listarPastas().forEach(pastas::addElement);
            int indice = atual == null ? 0 : Math.max(0, pastas.indexOf(atual));
            if (!pastas.isEmpty()) {
                listaPastas.setSelectedIndex(indice);
            }
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        } finally {
            atualizandoPastas = false;
        }
        carregarArquivos();
    }

    private void carregarArquivos() {
        Pasta pasta = listaPastas.getSelectedValue();
        cartoes.clear();
        selecionado = null;
        caminho.setText(pasta == null ? "Nenhuma pasta" : "Pastas  ›  " + pasta.getNome());
        if (pasta != null) {
            try {
                for (Arquivo arquivo : controller.listarArquivos(pasta)) {
                    cartoes.add(criarCartao(arquivo));
                }
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
        }
        organizarGrade();
        gerarMiniaturas();
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
                    abrir(cartao.getArquivo());
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
            cartoes.forEach(linhas::add);
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
        List<CartaoArquivo> pendentes = cartoes.stream()
                .filter(c -> c.getArquivo().isImagem() && !MINIATURAS.containsKey(c.getArquivo().getCaminho()))
                .toList();
        if (pendentes.isEmpty()) {
            return;
        }
        new SwingWorker<Void, CartaoArquivo>() {
            @Override
            protected Void doInBackground() {
                for (CartaoArquivo cartao : pendentes) {
                    try {
                        BufferedImage original = ImageIO.read(controller.localDoArquivo(cartao.getArquivo()).toFile());
                        if (original != null) {
                            MINIATURAS.put(cartao.getArquivo().getCaminho(), reduzir(original));
                            publish(cartao);
                        }
                    } catch (IOException e) {
                        // Arquivo ausente ou ilegível: o cartão fica sem miniatura.
                    }
                }
                return null;
            }

            @Override
            protected void process(List<CartaoArquivo> prontos) {
                prontos.forEach(c -> c.setMiniatura(MINIATURAS.get(c.getArquivo().getCaminho())));
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

    private void abrir(Arquivo arquivo) {
        Path local = controller.localDoArquivo(arquivo);
        if (!Files.exists(local)) {
            Mensagens.aviso(this, "O arquivo não foi encontrado em:\n" + local
                    + "\n\nEle pode ter sido adicionado em outro computador.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(local.toFile());
            } else {
                Mensagens.aviso(this, "Não foi possível abrir automaticamente. O arquivo está em:\n" + local);
            }
        } catch (IOException e) {
            Mensagens.aviso(this, "Não há um programa para abrir este arquivo. Ele está em:\n" + local);
        }
    }

    private void escolherArquivos() {
        if (listaPastas.getSelectedValue() == null) {
            Mensagens.aviso(this, "Crie ou selecione uma pasta primeiro.");
            return;
        }
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Adicionar arquivos à pasta " + listaPastas.getSelectedValue());
        seletor.setMultiSelectionEnabled(true);
        String[] extensoes = Stream.of(TipoArquivo.values()).flatMap(t -> t.getExtensoes().stream()).toArray(String[]::new);
        seletor.setFileFilter(new FileNameExtensionFilter("Fotos e vídeos", extensoes));
        if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            adicionar(Stream.of(seletor.getSelectedFiles()).map(File::toPath).toList());
        }
    }

    /** Copia os arquivos em segundo plano (vídeos podem ser grandes) e atualiza a tela ao final. */
    private void adicionar(List<Path> arquivos) {
        Pasta pasta = listaPastas.getSelectedValue();
        if (pasta == null || arquivos.isEmpty()) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<List<Arquivo>, Void>() {
            @Override
            protected List<Arquivo> doInBackground() throws Exception {
                return controller.adicionarArquivos(pasta, arquivos);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    int quantidade = get().size();
                    carregar();
                    Mensagens.sucesso(PainelRegistros.this, quantidade + " arquivo(s) adicionado(s) à pasta " + pasta + ".");
                } catch (ExecutionException e) {
                    Mensagens.erro(PainelRegistros.this, e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void novaPasta() {
        String nome = JOptionPane.showInputDialog(this, "Nome da nova pasta:", "Nova pasta", JOptionPane.PLAIN_MESSAGE);
        if (nome != null) {
            try {
                Pasta pasta = new Pasta(nome.trim());
                controller.salvarPasta(pasta);
                carregar();
                listaPastas.setSelectedValue(pasta, true);
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
        }
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
        Pasta pasta = pastas.get(indice);
        JPopupMenu menu = new JPopupMenu();
        menu.add(item("Renomear pasta", () -> renomearPasta(pasta)));
        menu.add(item("Excluir pasta", () -> excluirPasta(pasta)));
        menu.show(listaPastas, e.getX(), e.getY());
    }

    private void renomearPasta(Pasta pasta) {
        String nome = (String) JOptionPane.showInputDialog(this, "Novo nome da pasta:", "Renomear pasta",
                JOptionPane.PLAIN_MESSAGE, null, null, pasta.getNome());
        if (nome != null) {
            String anterior = pasta.getNome();
            pasta.setNome(nome.trim());
            try {
                controller.salvarPasta(pasta);
            } catch (AthletizaException e) {
                pasta.setNome(anterior);
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }

    private void excluirPasta(Pasta pasta) {
        if (Mensagens.confirmar(this, "Excluir a pasta \"" + pasta.getNome() + "\"?")) {
            try {
                controller.excluirPasta(pasta);
                listaPastas.clearSelection();
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }

    private void abrirMenuDoArquivo(CartaoArquivo cartao, MouseEvent e) {
        Arquivo arquivo = cartao.getArquivo();
        JPopupMenu menu = new JPopupMenu();
        menu.add(item("Abrir", () -> abrir(arquivo)));
        if (podeAlterar) {
            menu.add(item("Renomear ou mover...", () -> editarArquivo(arquivo)));
            menu.addSeparator();
            menu.add(item("Excluir", () -> excluirArquivo(arquivo)));
        }
        menu.show(cartao, e.getX(), e.getY());
    }

    private void editarArquivo(Arquivo arquivo) {
        JTextField nome = new JTextField(arquivo.getNome(), 28);
        JComboBox<Pasta> pasta = new JComboBox<>();
        for (int i = 0; i < pastas.size(); i++) {
            pasta.addItem(pastas.get(i));
        }
        pasta.setSelectedItem(arquivo.getPasta());
        JTextField descricao = new JTextField(arquivo.getDescricao());
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
        campos.add(new JLabel("Nome *"));
        campos.add(nome);
        campos.add(new JLabel("Pasta"));
        campos.add(pasta);
        campos.add(new JLabel("Descrição"));
        campos.add(descricao);
        while (JOptionPane.showConfirmDialog(this, campos, "Renomear ou mover", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                arquivo.setNome(nome.getText().trim());
                arquivo.setPasta((Pasta) pasta.getSelectedItem());
                arquivo.setDescricao(descricao.getText().isBlank() ? null : descricao.getText().trim());
                controller.salvarArquivo(arquivo);
                break;
            } catch (AthletizaException ex) {
                Mensagens.erro(this, ex);
            }
        }
        carregar();
    }

    private void excluirArquivo(Arquivo arquivo) {
        if (Mensagens.confirmar(this, "Excluir \"" + arquivo.getNome() + "\"? O arquivo também será apagado do computador.")) {
            try {
                controller.excluirArquivo(arquivo);
                MINIATURAS.remove(arquivo.getCaminho());
            } catch (AthletizaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }

    private static JMenuItem item(String texto, Runnable acao) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> acao.run());
        return item;
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
                adicionar(arquivos.stream().map(File::toPath).toList());
                return true;
            } catch (Exception e) {
                Mensagens.erro(PainelRegistros.this, e);
                return false;
            }
        }
    }
}
