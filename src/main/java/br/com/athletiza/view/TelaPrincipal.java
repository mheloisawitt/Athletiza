package br.com.athletiza.view;

import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.componentes.MenuLateral;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelEmConstrucao;
import br.com.athletiza.view.componentes.Recarregavel;
import br.com.athletiza.view.atletas.PainelConsultaAtletas;
import br.com.athletiza.view.calendario.PainelCalendario;
import br.com.athletiza.view.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.view.gestao.PainelConsultaGestoes;
import br.com.athletiza.view.treinos.PainelTreinos;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayDeque;
import java.util.Deque;
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

    /** Módulos do menu, na ordem de exibição. Cada um é criado só quando aberto pela primeira vez. */
    private final Map<String, Supplier<JComponent>> modulos = new LinkedHashMap<>();
    private final Map<String, JComponent> modulosCriados = new HashMap<>();

    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final MenuLateral menu = new MenuLateral(this::mostrarModulo);
    private String moduloAtual;
    /** Telas abertas sobre o módulo atual (ex.: consulta -> cadastro). "Voltar" retorna à anterior. */
    private final Deque<JComponent> telasAbertas = new ArrayDeque<>();

    public TelaPrincipal(Usuario usuario) {
        super("Athletiza - Sistema de Gerenciamento");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        modulos.put("Início", PainelCalendario::new);
        modulos.put("Gestão", () -> new PainelConsultaGestoes(this));
        modulos.put("Atletas", () -> new PainelConsultaAtletas(this));
        modulos.put("Competições", () -> new PainelConsultaCompeticoes(this));
        modulos.put("Treinos", () -> new PainelTreinos(this));
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
        telasAbertas.forEach(conteudo::remove);
        telasAbertas.clear();
        modulosCriados.computeIfAbsent(nome, chave -> {
            JComponent painel = modulos.get(chave).get();
            conteudo.add(painel, chave);
            return painel;
        });
        moduloAtual = nome;
        cartoes.show(conteudo, nome);
        recarregarModuloAtual();
    }

    @Override
    public void abrir(JComponent tela) {
        telasAbertas.push(tela);
        conteudo.add(tela, nomeCartao(tela));
        cartoes.show(conteudo, nomeCartao(tela));
        if (tela instanceof Recarregavel recarregavel) {
            recarregavel.carregar();
        }
    }

    @Override
    public void voltar() {
        if (!telasAbertas.isEmpty()) {
            conteudo.remove(telasAbertas.pop());
        }
        JComponent anterior = telasAbertas.peek();
        if (anterior == null) {
            cartoes.show(conteudo, moduloAtual);
            recarregarModuloAtual();
        } else {
            cartoes.show(conteudo, nomeCartao(anterior));
            if (anterior instanceof Recarregavel recarregavel) {
                recarregavel.carregar();
            }
        }
    }

    private static String nomeCartao(JComponent tela) {
        return "tela-" + System.identityHashCode(tela);
    }

    private void recarregarModuloAtual() {
        Component modulo = modulosCriados.get(moduloAtual);
        if (modulo instanceof Recarregavel recarregavel) {
            recarregavel.carregar();
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
