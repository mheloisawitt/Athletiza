package br.com.athletiza.view;

import br.com.athletiza.view.componentes.MenuLateral;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.Recarregavel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Janela principal (CH-14): menu lateral à esquerda e a área central, que
 * troca de conteúdo com CardLayout, sem abrir janelas extras.
 *
 * Os módulos (telas do menu) são criados pelo PrincipalController e adicionados
 * com adicionarModulo. Como implementa Navegador, os controllers abrem outras
 * telas na área central (abrir) e voltam para a anterior (voltar).
 */
public class TelaPrincipal extends JFrame implements Navegador {

    private final Map<String, JComponent> modulos = new HashMap<>();
    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final MenuLateral menu = new MenuLateral(e -> mostrarModulo(e.getActionCommand()));
    private String moduloAtual;
    /** Telas abertas sobre o módulo atual (ex.: consulta -> cadastro). "Voltar" retorna à anterior. */
    private final Deque<JComponent> telasAbertas = new ArrayDeque<>();

    public TelaPrincipal() {
        super("Athletiza - Sistema de Gerenciamento");
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(menu, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
    }

    /** Adiciona um item ao menu lateral e a tela que ele abre. */
    public void adicionarModulo(String nome, JComponent tela) {
        modulos.put(nome, tela);
        conteudo.add(tela, nome);
        menu.adicionarItem(nome);
    }

    /** Seleciona um item do menu, como se o usuário clicasse nele. */
    public void selecionarModulo(String nome) {
        menu.selecionar(nome);
    }

    /** Nome e perfil do usuário logado, no rodapé do menu. */
    public void definirUsuario(String nome, String perfil) {
        menu.definirUsuario(nome, perfil);
    }

    public void adicionarAcaoAlterarSenha(ActionListener acao) {
        menu.adicionarAcaoAlterarSenha(acao);
    }

    public void adicionarAcaoSair(ActionListener acao) {
        menu.adicionarAcaoSair(acao);
    }

    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    private void mostrarModulo(String nome) {
        for (JComponent tela : telasAbertas) {
            conteudo.remove(tela);
        }
        telasAbertas.clear();
        moduloAtual = nome;
        cartoes.show(conteudo, nome);
        recarregar(modulos.get(nome));
    }

    @Override
    public void abrir(JComponent tela) {
        telasAbertas.push(tela);
        conteudo.add(tela, nomeCartao(tela));
        cartoes.show(conteudo, nomeCartao(tela));
        recarregar(tela);
    }

    @Override
    public void voltar() {
        if (!telasAbertas.isEmpty()) {
            conteudo.remove(telasAbertas.pop());
        }
        JComponent anterior = telasAbertas.peek();
        if (anterior == null) {
            cartoes.show(conteudo, moduloAtual);
            recarregar(modulos.get(moduloAtual));
        } else {
            cartoes.show(conteudo, nomeCartao(anterior));
            recarregar(anterior);
        }
    }

    private static String nomeCartao(JComponent tela) {
        return "tela-" + System.identityHashCode(tela);
    }

    /** Telas que buscam dados (Recarregavel) são atualizadas sempre que aparecem. */
    private static void recarregar(Component tela) {
        if (tela instanceof Recarregavel) {
            ((Recarregavel) tela).carregar();
        }
    }
}
