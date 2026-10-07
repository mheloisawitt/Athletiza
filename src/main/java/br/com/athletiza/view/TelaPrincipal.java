package br.com.athletiza.view;

import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.componentes.MenuLateral;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.PainelEmConstrucao;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Janela principal (CH-14): menu lateral à esquerda e a área central, que
 * troca de conteúdo com CardLayout, sem abrir janelas extras.
 */
public class TelaPrincipal extends JFrame implements Navegador {

    private static final String DETALHE = "detalhe";

    /** Módulos do menu, na ordem de exibição. Cada um é criado só quando aberto pela primeira vez. */
    private final Map<String, Supplier<JComponent>> modulos = new LinkedHashMap<>();
    private final Map<String, JComponent> modulosCriados = new HashMap<>();

    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final MenuLateral menu = new MenuLateral(this::mostrarModulo);
    private String moduloAtual;
    private JComponent telaDetalhe;

    public TelaPrincipal(Usuario usuario) {
        super("Athletiza - Sistema de Gerenciamento");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        modulos.put("Início", () -> new PainelEmConstrucao("Calendário", "CH-15 a CH-17"));
        modulos.put("Gestão", () -> new PainelEmConstrucao("Gestões", "CH-18 a CH-23"));
        modulos.put("Atletas", () -> new PainelEmConstrucao("Atletas", "CH-24 a CH-27"));
        modulos.put("Competições", () -> new PainelEmConstrucao("Competições", "CH-28 a CH-32"));
        modulos.put("Treinos", () -> new PainelEmConstrucao("Treinos", "CH-33 a CH-37"));
        modulos.put("Eventos", () -> new PainelEmConstrucao("Eventos", "CH-38 a CH-41"));
        modulos.put("Registros", () -> new PainelEmConstrucao("Registros", "CH-42 a CH-43"));
        modulos.keySet().forEach(menu::adicionarItem);
        menu.definirUsuario(usuario.getNome(), usuario.getPerfil().getDescricao(), this::sair);

        setLayout(new BorderLayout());
        add(menu, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);

        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        menu.selecionar("Início");
    }

    private void mostrarModulo(String nome) {
        fecharDetalhe();
        modulosCriados.computeIfAbsent(nome, chave -> {
            JComponent painel = modulos.get(chave).get();
            conteudo.add(painel, chave);
            return painel;
        });
        moduloAtual = nome;
        cartoes.show(conteudo, nome);
    }

    @Override
    public void abrir(JComponent tela) {
        fecharDetalhe();
        telaDetalhe = tela;
        conteudo.add(tela, DETALHE);
        cartoes.show(conteudo, DETALHE);
    }

    @Override
    public void voltar() {
        fecharDetalhe();
        cartoes.show(conteudo, moduloAtual);
        Component modulo = modulosCriados.get(moduloAtual);
        if (modulo instanceof PainelConsulta<?> consulta) {
            consulta.carregar();
        }
    }

    private void fecharDetalhe() {
        if (telaDetalhe != null) {
            conteudo.remove(telaDetalhe);
            telaDetalhe = null;
        }
    }

    private void sair() {
        if (Mensagens.confirmar(this, "Deseja sair do sistema?")) {
            Sessao.encerrar();
            dispose();
            new TelaLogin().setVisible(true);
        }
    }
}
