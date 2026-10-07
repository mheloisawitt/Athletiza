package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Barra lateral de navegação (CH-14): logo, itens do menu e usuário logado.
 * O item selecionado fica destacado em verde.
 */
public class MenuLateral extends JPanel {

    private static final String ESTILO_ITEM = "background:" + Cores.hex(Cores.FUNDO_MENU)
            + "; selectedBackground:" + Cores.hex(Cores.VERDE_SELECAO)
            + "; selectedForeground:" + Cores.hex(Cores.VERDE)
            + "; hoverBackground:#232323; foreground:#D0D0D0; focusWidth:0; arc:10; margin:9,14,9,14"
            + "; borderColor:" + Cores.hex(Cores.FUNDO_MENU) + "; focusedBorderColor:" + Cores.hex(Cores.FUNDO_MENU);

    /** O item selecionado ganha contorno verde, como no protótipo. */
    private static final String ESTILO_ITEM_SELECIONADO = ESTILO_ITEM
            + "; borderColor:" + Cores.hex(Cores.VERDE) + "; focusedBorderColor:" + Cores.hex(Cores.VERDE)
            + "; hoverBorderColor:" + Cores.hex(Cores.VERDE);

    private final ButtonGroup grupo = new ButtonGroup();
    private final Map<String, JToggleButton> itens = new LinkedHashMap<>();
    private final JPanel painelItens = new JPanel();
    private final Consumer<String> aoSelecionar;

    public MenuLateral(Consumer<String> aoSelecionar) {
        super(new BorderLayout());
        this.aoSelecionar = aoSelecionar;
        setBackground(Cores.FUNDO_MENU);
        setPreferredSize(new Dimension(230, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Cores.CINZA_ESCURO),
                BorderFactory.createEmptyBorder(20, 12, 16, 12)));

        painelItens.setLayout(new BoxLayout(painelItens, BoxLayout.Y_AXIS));
        painelItens.setOpaque(false);

        add(criarLogo(), BorderLayout.NORTH);
        add(painelItens, BorderLayout.CENTER);
    }

    public void adicionarItem(String nome) {
        JToggleButton item = new JToggleButton(nome);
        item.putClientProperty(FlatClientProperties.STYLE, ESTILO_ITEM);
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addActionListener(e -> aoSelecionar.accept(nome));
        item.addItemListener(e -> item.putClientProperty(FlatClientProperties.STYLE,
                item.isSelected() ? ESTILO_ITEM_SELECIONADO : ESTILO_ITEM));
        grupo.add(item);
        itens.put(nome, item);
        painelItens.add(item);
        painelItens.add(Box.createVerticalStrut(4));
    }

    /** Marca o item como selecionado e avisa quem está ouvindo. */
    public void selecionar(String nome) {
        JToggleButton item = itens.get(nome);
        if (item != null) {
            item.setSelected(true);
            aoSelecionar.accept(nome);
        }
    }

    /** Rodapé com o usuário logado e o botão Sair. */
    public void definirUsuario(String nome, String detalhe, Runnable aoAlterarSenha, Runnable aoSair) {
        JLabel rotuloNome = new JLabel(nome);
        rotuloNome.setFont(Tema.fonte(Font.BOLD, 13f));
        JLabel rotuloDetalhe = new JLabel(detalhe);
        rotuloDetalhe.setFont(Tema.fonte(Font.PLAIN, 11f));
        rotuloDetalhe.setForeground(Cores.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(rotuloNome);
        textos.add(rotuloDetalhe);
        textos.setToolTipText("Clique para alterar sua senha");
        textos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        textos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                aoAlterarSenha.run();
            }
        });

        JButton sair = new JButton("Sair");
        sair.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(Cores.TEXTO_SECUNDARIO) + "; font:-1; margin:2,4,2,4");
        sair.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sair.addActionListener(e -> aoSair.run());

        JPanel rodape = new JPanel(new BorderLayout(10, 0));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Cores.CINZA_ESCURO),
                BorderFactory.createEmptyBorder(14, 2, 0, 0)));
        rodape.add(new Avatar(nome), BorderLayout.WEST);
        rodape.add(textos, BorderLayout.CENTER);
        rodape.add(sair, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
    }

    private JComponent criarLogo() {
        java.net.URL imagem = getClass().getResource("/imagens/logo.png");
        JLabel logo;
        if (imagem != null) {
            ImageIcon icone = new ImageIcon(imagem);
            logo = new JLabel(new ImageIcon(icone.getImage().getScaledInstance(-1, 56, java.awt.Image.SCALE_SMOOTH)));
        } else {
            logo = new JLabel("ATLÉTICA");
            logo.setFont(Tema.fonte(Font.BOLD | Font.ITALIC, 24f));
            logo.setForeground(Cores.VERDE);
        }
        logo.setBorder(BorderFactory.createEmptyBorder(0, 6, 24, 0));
        return logo;
    }

    /** Círculo com a inicial do usuário. */
    private static class Avatar extends JComponent {

        private final String inicial;

        Avatar(String nome) {
            this.inicial = nome == null || nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
            setPreferredSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int tamanho = Math.min(getWidth(), getHeight());
            int y = (getHeight() - tamanho) / 2;
            g2.setColor(Cores.ROXO);
            g2.fillOval(0, y, tamanho, tamanho);
            g2.setColor(Color.WHITE);
            g2.setFont(Tema.fonte(Font.BOLD, 14f));
            int larguraTexto = g2.getFontMetrics().stringWidth(inicial);
            g2.drawString(inicial, (tamanho - larguraTexto) / 2,
                    y + (tamanho + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);
            g2.dispose();
        }
    }
}
