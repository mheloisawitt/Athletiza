package br.com.athletiza.visao;

import br.com.athletiza.controlador.AtletaControlador;
import br.com.athletiza.controlador.CalendarioControlador;
import br.com.athletiza.controlador.CompeticaoControlador;
import br.com.athletiza.controlador.DashboardControlador;
import br.com.athletiza.controlador.DemandaControlador;
import br.com.athletiza.controlador.EventoControlador;
import br.com.athletiza.controlador.GaleriaControlador;
import br.com.athletiza.controlador.GestaoControlador;
import br.com.athletiza.controlador.LoginControlador;
import br.com.athletiza.controlador.TreinoControlador;
import br.com.athletiza.controlador.UsuarioControlador;
import br.com.athletiza.dao.AlertaDAO;
import br.com.athletiza.dao.ArquivoDAO;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.dao.DashboardDAO;
import br.com.athletiza.dao.DemandaDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.GestaoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.PastaDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.modelo.Acesso;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.atletas.PainelConsultaAtletas;
import br.com.athletiza.visao.calendario.PainelCalendario;
import br.com.athletiza.visao.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.visao.componentes.Mensagens;
import br.com.athletiza.visao.componentes.MenuLateral;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.Recarregavel;
import br.com.athletiza.visao.dashboard.PainelDashboard;
import br.com.athletiza.visao.demandas.PainelConsultaDemandas;
import br.com.athletiza.visao.eventos.PainelConsultaEventos;
import br.com.athletiza.visao.gestao.PainelConsultaGestoes;
import br.com.athletiza.visao.registros.PainelRegistros;
import br.com.athletiza.visao.treinos.PainelTreinos;
import br.com.athletiza.visao.usuarios.DialogoAlterarSenha;
import br.com.athletiza.visao.usuarios.PainelConsultaUsuarios;
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

public class TelaPrincipal extends JFrame implements Navegador {
    private final Map<String, Supplier<JComponent>> modulos = new LinkedHashMap<>();
    private final Map<String, JComponent> modulosCriados = new HashMap<>();

    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final MenuLateral menu = new MenuLateral(this::mostrarModulo);
    private String moduloAtual;

    private final Deque<JComponent> telasAbertas = new ArrayDeque<>();

    public TelaPrincipal(Usuario usuario) {
        super("Athletiza - Sistema de Gerenciamento");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Acesso acesso = Sessao.getAcesso();
        adicionarModulo(acesso, Modulo.INICIO, () -> acaoInicio());
        adicionarModulo(acesso, Modulo.DASHBOARD, () -> acaoDashboard());
        adicionarModulo(acesso, Modulo.GESTAO, () -> acaoGestao());
        adicionarModulo(acesso, Modulo.ATLETAS, () -> acaoAtletas());
        adicionarModulo(acesso, Modulo.COMPETICOES, () -> acaoCompeticoes());
        adicionarModulo(acesso, Modulo.TREINOS, () -> acaoTreinos());
        adicionarModulo(acesso, Modulo.EVENTOS, () -> acaoEventos());
        adicionarModulo(acesso, Modulo.DEMANDAS, () -> acaoDemandas());
        adicionarModulo(acesso, Modulo.REGISTROS, () -> acaoRegistros());
        adicionarModulo(acesso, Modulo.USUARIOS, () -> acaoUsuarios());
        for (String nome : modulos.keySet()) {
            menu.adicionarItem(nome);
        }
        menu.definirUsuario(usuario.getNome(), acesso.getDescricao(), () -> acaoAlterarSenha(usuario), this::sair);

        setLayout(new BorderLayout());
        add(menu, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);

        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        menu.selecionar(Modulo.INICIO.getNome());
    }

    private JComponent acaoInicio() {
        PainelCalendario visao = new PainelCalendario(this);
        new CalendarioControlador(visao, new AtividadeDAO(), new CompromissoDAO(), new AlertaDAO());
        return visao;
    }

    private JComponent acaoDashboard() {
        PainelDashboard visao = new PainelDashboard(m -> menu.selecionar(m.getNome()));
        new DashboardControlador(visao, new DashboardDAO(), new TreinoDAO(), new AtividadeDAO());
        return visao;
    }

    private JComponent acaoGestao() {
        PainelConsultaGestoes visao = new PainelConsultaGestoes(this);
        new GestaoControlador(visao, new GestaoDAO(), new CargoDAO(), new MembroDAO());
        return visao;
    }

    private JComponent acaoAtletas() {
        PainelConsultaAtletas visao = new PainelConsultaAtletas(this);
        new AtletaControlador(visao, new AtletaDAO(), new ModalidadeDAO());
        return visao;
    }

    private JComponent acaoCompeticoes() {
        PainelConsultaCompeticoes visao = new PainelConsultaCompeticoes(this);
        new CompeticaoControlador(visao, new CompeticaoDAO(), new AtletaDAO(), new ModalidadeDAO(), new MembroDAO());
        return visao;
    }

    private JComponent acaoTreinos() {
        PainelTreinos visao = new PainelTreinos(this);
        new TreinoControlador(visao, new TreinoDAO(), new AtletaDAO(), new ModalidadeDAO(), new MembroDAO());
        return visao;
    }

    private JComponent acaoEventos() {
        PainelConsultaEventos visao = new PainelConsultaEventos(this);
        new EventoControlador(visao, new EventoDAO(), new MembroDAO());
        return visao;
    }

    private JComponent acaoDemandas() {
        PainelConsultaDemandas visao = new PainelConsultaDemandas(this);
        new DemandaControlador(visao, new DemandaDAO(), new EventoDAO(), new CompeticaoDAO(), new MembroDAO());
        return visao;
    }

    private JComponent acaoRegistros() {
        PainelRegistros visao = new PainelRegistros();
        new GaleriaControlador(visao, new PastaDAO(), new ArquivoDAO());
        return visao;
    }

    private JComponent acaoUsuarios() {
        PainelConsultaUsuarios visao = new PainelConsultaUsuarios(this);
        new UsuarioControlador(visao, new UsuarioDAO(), new CargoDAO(), new MembroDAO());
        return visao;
    }

    private void acaoAlterarSenha(Usuario usuario) {
        DialogoAlterarSenha dialogo = new DialogoAlterarSenha(this, false);
        new UsuarioControlador(new UsuarioDAO()).alterarSenhaNaTela(dialogo, usuario);
    }

    private void adicionarModulo(Acesso acesso, Modulo modulo, Supplier<JComponent> criar) {
        if (acesso.veModulo(modulo)) {
            modulos.put(modulo.getNome(), criar);
        }
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
            TelaLogin visao = new TelaLogin();
            new LoginControlador(visao, new UsuarioDAO(), new CargoDAO());
        }
    }
}
