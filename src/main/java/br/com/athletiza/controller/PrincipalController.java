package br.com.athletiza.controller;

import br.com.athletiza.dao.AlertaDAO;
import br.com.athletiza.dao.AlertaRepository;
import br.com.athletiza.dao.ArquivoDAO;
import br.com.athletiza.dao.ArquivoRepository;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.AtividadeRepository;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.CargoRepository;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.dao.CompromissoRepository;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.EventoRepository;
import br.com.athletiza.dao.GestaoDAO;
import br.com.athletiza.dao.GestaoRepository;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.PastaDAO;
import br.com.athletiza.dao.PastaRepository;
import br.com.athletiza.dao.TarefaDAO;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.dao.UsuarioRepository;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.TelaLogin;
import br.com.athletiza.view.TelaPrincipal;
import br.com.athletiza.view.atletas.PainelConsultaAtletas;
import br.com.athletiza.view.calendario.PainelCalendario;
import br.com.athletiza.view.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.view.eventos.PainelConsultaEventos;
import br.com.athletiza.view.gestao.PainelConsultaGestoes;
import br.com.athletiza.view.registros.PainelRegistros;
import br.com.athletiza.view.treinos.PainelTreinos;
import br.com.athletiza.view.usuarios.DialogoAlterarSenha;
import br.com.athletiza.view.usuarios.PainelConsultaUsuarios;

/**
 * Controller da janela principal (CH-14). Monta os módulos do menu: para cada
 * um cria a View e o Controller.
 *
 * É o único lugar que conhece as classes DAO (a implementação com MySQL).
 * Os demais controllers recebem as interfaces Repository no construtor,
 * seguindo a Inversão de Dependência (DIP): trocar o banco de dados só
 * mudaria as linhas abaixo.
 */
public class PrincipalController {

    private final TelaPrincipal view;
    private final Usuario usuario;

    /* Implementações dos repositórios: criadas uma vez e compartilhadas */
    private final AlertaRepository alertaRepository = new AlertaDAO();
    private final ArquivoRepository arquivoRepository = new ArquivoDAO();
    private final AtividadeRepository atividadeRepository = new AtividadeDAO();
    private final AtletaRepository atletaRepository = new AtletaDAO();
    private final CargoRepository cargoRepository = new CargoDAO();
    private final CompeticaoRepository competicaoRepository = new CompeticaoDAO();
    private final CompromissoRepository compromissoRepository = new CompromissoDAO();
    private final EventoRepository eventoRepository = new EventoDAO();
    private final GestaoRepository gestaoRepository = new GestaoDAO();
    private final MembroRepository membroRepository = new MembroDAO();
    private final ModalidadeRepository modalidadeRepository = new ModalidadeDAO();
    private final PastaRepository pastaRepository = new PastaDAO();
    private final TarefaRepository tarefaRepository = new TarefaDAO();
    private final TreinoRepository treinoRepository = new TreinoDAO();
    private final UsuarioRepository usuarioRepository = new UsuarioDAO();

    public PrincipalController(TelaPrincipal view, Usuario usuario) {
        this.view = view;
        this.usuario = usuario;
        criarModulos();
        view.definirUsuario(usuario.getNome(), usuario.getPerfil().getDescricao());
        adicionarAcoesBotoes();
        view.selecionarModulo("Início");
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoAlterarSenha(e -> alterarSenha());
        view.adicionarAcaoSair(e -> sair());
    }

    /** Cada módulo do menu: a View e o Controller dela (que registra as ações dos botões). */
    private void criarModulos() {
        PainelCalendario calendario = new PainelCalendario();
        new CalendarioController(calendario, atividadeRepository, alertaRepository, compromissoRepository,
                treinoRepository, competicaoRepository, eventoRepository, modalidadeRepository, membroRepository, view);
        view.adicionarModulo("Início", calendario);

        PainelConsultaGestoes gestoes = new PainelConsultaGestoes();
        new ConsultaGestoesController(gestoes, gestaoRepository, cargoRepository, membroRepository, view);
        view.adicionarModulo("Gestão", gestoes);

        PainelConsultaAtletas atletas = new PainelConsultaAtletas();
        new ConsultaAtletasController(atletas, atletaRepository, modalidadeRepository, view);
        view.adicionarModulo("Atletas", atletas);

        PainelConsultaCompeticoes competicoes = new PainelConsultaCompeticoes();
        new ConsultaCompeticoesController(competicoes, competicaoRepository, atletaRepository, modalidadeRepository,
                tarefaRepository, membroRepository, view);
        view.adicionarModulo("Competições", competicoes);

        PainelTreinos treinos = new PainelTreinos();
        new TreinosController(treinos, treinoRepository, modalidadeRepository, membroRepository, atletaRepository, view);
        view.adicionarModulo("Treinos", treinos);

        PainelConsultaEventos eventos = new PainelConsultaEventos();
        new ConsultaEventosController(eventos, eventoRepository, tarefaRepository, membroRepository, view);
        view.adicionarModulo("Eventos", eventos);

        PainelRegistros registros = new PainelRegistros();
        new RegistrosController(registros, pastaRepository, arquivoRepository);
        view.adicionarModulo("Registros", registros);

        if (Sessao.podeGerenciarUsuarios()) {
            PainelConsultaUsuarios usuarios = new PainelConsultaUsuarios();
            new ConsultaUsuariosController(usuarios, usuarioRepository, view);
            view.adicionarModulo("Usuários", usuarios);
        }
    }

    private void alterarSenha() {
        DialogoAlterarSenha dialogo = new DialogoAlterarSenha(view, null);
        new AlterarSenhaController(dialogo, usuario, usuarioRepository).executar("Alterar minha senha");
    }

    private void sair() {
        if (view.confirmar("Deseja sair do sistema?")) {
            Sessao.encerrar();
            view.dispose();
            TelaLogin login = new TelaLogin();
            new LoginController(login, usuarioRepository);
            login.setVisible(true);
        }
    }
}
