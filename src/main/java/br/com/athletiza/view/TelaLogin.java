package br.com.athletiza.view;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Tela de login (CH-12). Enter no campo de senha ou o botão Entrar disparam a
 * autenticação, feita pelo LoginController.
 */
public class TelaLogin extends JFrame {

    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoSenha = new JPasswordField();
    private final JCheckBox lembrar = new JCheckBox("Lembrar de mim");
    private final JButton botaoEntrar = Botoes.primario("Entrar", null);

    public TelaLogin() {
        super("Athletiza - Login");
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        add(new PainelMarca());
        add(criarPainelAcesso());
        getRootPane().setDefaultButton(botaoEntrar);
        setSize(960, 600);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean visivel) {
        super.setVisible(visivel);
        if (visivel) {
            if (campoUsuario.getText().isEmpty()) {
                campoUsuario.requestFocusInWindow();
            } else {
                campoSenha.requestFocusInWindow();
            }
        }
    }

    public void adicionarAcaoEntrar(ActionListener acao) {
        botaoEntrar.addActionListener(acao);
    }

    public String getLogin() {
        return campoUsuario.getText();
    }

    /** Senha digitada. Quem chama deve apagar o array depois de usar (Arrays.fill). */
    public char[] getSenha() {
        return campoSenha.getPassword();
    }

    public boolean isLembrar() {
        return lembrar.isSelected();
    }

    /** Preenche o login lembrado da última vez (e marca "Lembrar de mim"). */
    public void setLoginLembrado(String login) {
        campoUsuario.setText(login);
        lembrar.setSelected(!login.isEmpty());
    }

    /** Desabilita o botão enquanto a autenticação está em andamento. */
    public void setEntrando(boolean entrando) {
        botaoEntrar.setEnabled(!entrando);
        botaoEntrar.setText(entrando ? "Entrando..." : "Entrar");
    }

    public void limparSenha() {
        campoSenha.setText("");
        campoSenha.requestFocusInWindow();
    }

    public void mostrarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    public void mostrarErro(Throwable erro) {
        Mensagens.erro(this, erro);
    }

    private JPanel criarPainelAcesso() {
        JLabel titulo = new JLabel("Acesse sua conta");
        titulo.setFont(Tema.fonte(Font.BOLD, 20f));
        JLabel subtitulo = new JLabel("Entre com suas credenciais para continuar.");
        subtitulo.setForeground(Cores.TEXTO_SECUNDARIO);

        campoUsuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Usuário");
        campoSenha.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Senha");
        campoSenha.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        for (JTextField campo : new JTextField[]{campoUsuario, campoSenha}) {
            campo.putClientProperty(FlatClientProperties.STYLE_CLASS, "large");
            campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            campo.setPreferredSize(new Dimension(300, 40));
        }
        lembrar.setOpaque(false);
        botaoEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel cartao = new JPanel();
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + Cores.hex(Cores.FUNDO_CARTAO));
        cartao.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        for (Component componente : new Component[]{titulo, Box.createVerticalStrut(6), subtitulo,
            Box.createVerticalStrut(28), campoUsuario, Box.createVerticalStrut(12), campoSenha,
            Box.createVerticalStrut(12), lembrar, Box.createVerticalStrut(24), botaoEntrar}) {
            cartao.add(componente);
            if (componente instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) componente).setAlignmentX(LEFT_ALIGNMENT);
            }
        }
        cartao.setPreferredSize(new Dimension(370, cartao.getPreferredSize().height));

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Cores.FUNDO);
        painel.add(cartao);
        return painel;
    }

    /** Lado esquerdo da tela: fundo decorado com marcas de garra, nome e subtítulo. */
    private static class PainelMarca extends JPanel {

        PainelMarca() {
            setLayout(new GridBagLayout());
            JLabel nome = new JLabel("ATLÉTICA");
            nome.setFont(Tema.fonte(Font.BOLD | Font.ITALIC, 52f));
            nome.setForeground(Cores.TEXTO);
            nome.setAlignmentX(CENTER_ALIGNMENT);
            JLabel subtitulo = new JLabel("Sistema de Gerenciamento");
            subtitulo.setFont(Tema.fonte(Font.PLAIN, 15f));
            subtitulo.setForeground(Cores.TEXTO_SECUNDARIO);
            subtitulo.setAlignmentX(CENTER_ALIGNMENT);

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            java.net.URL imagem = getClass().getResource("/imagens/logo.png");
            if (imagem != null) {
                JLabel logo = new JLabel(new javax.swing.ImageIcon(new javax.swing.ImageIcon(imagem).getImage()
                        .getScaledInstance(-1, 180, java.awt.Image.SCALE_SMOOTH)));
                logo.setAlignmentX(CENTER_ALIGNMENT);
                textos.add(logo);
            }
            textos.add(nome);
            textos.add(Box.createVerticalStrut(4));
            textos.add(subtitulo);
            add(textos);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0x1A1025), w, h, Cores.FUNDO_MENU));
            g2.fillRect(0, 0, w, h);

            g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            desenharGarra(g2, -40, h - 20, Cores.VERDE, 90);
            desenharGarra(g2, w - 150, 70, Cores.ROXO, 70);
            g2.setColor(Cores.CINZA_ESCURO);
            g2.fillRect(w - 1, 0, 1, h);
            g2.dispose();
            super.paintComponent(g);
        }

        private static void desenharGarra(Graphics2D g2, int x, int y, Color cor, int alfa) {
            g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), alfa));
            for (int i = 0; i < 3; i++) {
                int deslocamento = i * 26;
                g2.drawLine(x + deslocamento, y, x + 150 + deslocamento, y - 170);
            }
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }
}
