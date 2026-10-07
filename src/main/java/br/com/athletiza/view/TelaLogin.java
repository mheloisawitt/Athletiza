package br.com.athletiza.view;

import br.com.athletiza.controller.LoginController;
import br.com.athletiza.model.Usuario;
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
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
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
import javax.swing.SwingWorker;

/**
 * Tela de login (CH-12). Enter no campo de senha ou o botão Entrar disparam a autenticação.
 */
public class TelaLogin extends JFrame {

    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(TelaLogin.class);
    private static final String ULTIMO_LOGIN = "ultimoLogin";

    private final LoginController controller = new LoginController();
    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoSenha = new JPasswordField();
    private final JCheckBox lembrar = new JCheckBox("Lembrar de mim");
    private final JButton botaoEntrar = Botoes.primario("Entrar", e -> entrar());

    public TelaLogin() {
        super("Athletiza - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        add(new PainelMarca());
        add(criarPainelAcesso());
        getRootPane().setDefaultButton(botaoEntrar);

        String ultimoLogin = PREFERENCIAS.get(ULTIMO_LOGIN, "");
        campoUsuario.setText(ultimoLogin);
        lembrar.setSelected(!ultimoLogin.isEmpty());

        setSize(960, 600);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean visivel) {
        super.setVisible(visivel);
        if (visivel) {
            (campoUsuario.getText().isEmpty() ? campoUsuario : campoSenha).requestFocusInWindow();
        }
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
            ((java.awt.Container) cartao).add(componente);
            if (componente instanceof javax.swing.JComponent jc) {
                jc.setAlignmentX(LEFT_ALIGNMENT);
            }
        }
        cartao.setPreferredSize(new Dimension(370, cartao.getPreferredSize().height));

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Cores.FUNDO);
        painel.add(cartao);
        return painel;
    }

    private void entrar() {
        String login = campoUsuario.getText();
        char[] senha = campoSenha.getPassword();
        botaoEntrar.setEnabled(false);
        botaoEntrar.setText("Entrando...");

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return controller.autenticar(login, senha);
            }

            @Override
            protected void done() {
                Arrays.fill(senha, '\0');
                botaoEntrar.setEnabled(true);
                botaoEntrar.setText("Entrar");
                try {
                    Usuario usuario = get();
                    lembrarLogin(login);
                    new TelaPrincipal(usuario).setVisible(true);
                    dispose();
                } catch (ExecutionException e) {
                    Mensagens.erro(TelaLogin.this, e.getCause());
                    campoSenha.setText("");
                    campoSenha.requestFocusInWindow();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void lembrarLogin(String login) {
        if (lembrar.isSelected()) {
            PREFERENCIAS.put(ULTIMO_LOGIN, login.trim());
        } else {
            PREFERENCIAS.remove(ULTIMO_LOGIN);
        }
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
