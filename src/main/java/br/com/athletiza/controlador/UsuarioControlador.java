package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.repositorio.CargoRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.repositorio.UsuarioRepositorio;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.usuarios.DialogoAlterarSenha;
import br.com.athletiza.visao.usuarios.PainelCadastroUsuario;
import br.com.athletiza.visao.usuarios.PainelConsultaUsuarios;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UsuarioControlador {
    static final int TAMANHO_MINIMO_SENHA = 6;
    private static final Pattern LOGIN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private PainelConsultaUsuarios visao;
    private final UsuarioRepositorio repositorio;
    private CargoRepositorio cargoRepositorio;
    private MembroRepositorio membroRepositorio;

    public UsuarioControlador(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public UsuarioControlador(PainelConsultaUsuarios visao, UsuarioRepositorio repositorio,
            CargoRepositorio cargoRepositorio, MembroRepositorio membroRepositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        this.cargoRepositorio = cargoRepositorio;
        this.membroRepositorio = membroRepositorio;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeGerenciarUsuarios());
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Usuario()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionado());
    }

    public void carregar() {
        try {
            List<Usuario> usuarios = listar();
            AcessoControlador acesso = new AcessoControlador(cargoRepositorio);
            Map<Usuario, String> descricoes = new HashMap<>();
            for (Usuario usuario : usuarios) {
                descricoes.put(usuario, acesso.descrever(usuario));
            }
            visao.apresentarUsuarios(usuarios, descricoes);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editar() {
        Usuario usuario = visao.getSelecionado();
        if (usuario != null) {
            abrirCadastro(usuario);
        }
    }

    private void excluirSelecionado() {
        Usuario usuario = visao.getSelecionado();
        if (usuario == null || !visao.confirmarExclusao(usuario)) {
            return;
        }
        try {
            excluir(usuario);
            visao.apresentarMensagem("Usuário excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Usuario usuario) {
        PainelCadastroUsuario cadastro = new PainelCadastroUsuario(usuario, visao.getNavegador());
        try {
            List<Membro> membros = new ArrayList<>(new MembroControlador(membroRepositorio).listarAtivos());
            if (usuario.getMembro() != null && !membros.contains(usuario.getMembro())) {
                membros.add(usuario.getMembro());
            }
            cadastro.apresentarUsuario(usuario, membros);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        cadastro.adicionarAcaoMudarAcesso(e -> mostrarAcesso(cadastro));
        mostrarAcesso(cadastro);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, usuario));
        cadastro.apresentarTela();
    }

    private void mostrarAcesso(PainelCadastroUsuario cadastro) {
        Usuario previa = new Usuario();
        previa.setAdministrador(cadastro.isAdministrador());
        previa.setMembro(cadastro.getMembro());
        if (!previa.isAdministrador() && previa.getMembro() == null) {
            cadastro.apresentarAcesso("escolha o membro da gestão");
            return;
        }
        try {
            cadastro.apresentarAcesso(new AcessoControlador(cargoRepositorio).descrever(previa));
        } catch (PersistenciaException e) {
            cadastro.apresentarAcesso(" ");
        }
    }

    private void salvarCadastro(PainelCadastroUsuario cadastro, Usuario usuario) {
        char[] senha = cadastro.getSenha();
        char[] confirmacao = cadastro.getConfirmacao();
        try {
            Membro escolhido = cadastro.getMembro();
            usuario.setNome(cadastro.getNome());
            usuario.setLogin(cadastro.getLogin());
            usuario.setMembro(escolhido);
            usuario.setMatricula(escolhido == null ? usuario.getMatricula() : escolhido.getMatricula());
            usuario.setAdministrador(cadastro.isAdministrador());
            usuario.setAtivo(cadastro.isAtivo());
            salvar(usuario, senha, confirmacao);
            cadastro.apresentarMensagem("Usuário salvo com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        } finally {
            Arrays.fill(senha, '\0');
            Arrays.fill(confirmacao, '\0');
        }
    }

    public boolean alterarSenhaNaTela(DialogoAlterarSenha dialogo, Usuario usuario) {
        while (dialogo.apresentarTela()) {
            char[] senhaAtual = dialogo.getSenhaAtual();
            char[] senhaNova = dialogo.getNovaSenha();
            char[] senhaConfirmacao = dialogo.getConfirmacao();
            try {
                alterarSenha(usuario, senhaAtual, senhaNova, senhaConfirmacao);
                dialogo.apresentarMensagem("Senha alterada com sucesso.");
                return true;
            } catch (AthletizaException e) {
                dialogo.apresentarErro(e);
            } finally {
                Arrays.fill(senhaAtual, '\0');
                Arrays.fill(senhaNova, '\0');
                Arrays.fill(senhaConfirmacao, '\0');
            }
        }
        return false;
    }

    public List<Usuario> listar() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    public void salvar(Usuario usuario, char[] senha, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        boolean informouSenha = senha != null && senha.length > 0;
        Validador validador = new Validador()
                .obrigatorio(usuario.getNome(), "Nome")
                .tamanhoMaximo(usuario.getNome(), 100, "Nome")
                .obrigatorio(usuario.getLogin(), "Login")
                .regra(usuario.getLogin() == null || usuario.getLogin().isBlank() || LOGIN.matcher(usuario.getLogin()).matches(),
                        "O \"Login\" deve ter de 3 a 50 caracteres, sem espaços (letras, números, ponto, hífen ou _).")
                .regra(usuario.isAdministrador() || usuario.getMembro() != null,
                        "Escolha o membro da gestão que usará este login (ou marque como administrador).")
                .regra(!usuario.isNova() || informouSenha, "O campo \"Senha\" é obrigatório para novos usuários.");
        if (informouSenha) {
            validarNovaSenha(validador, senha, confirmacao);
        }
        validador.validar();

        if (repositorio.existeLogin(usuario.getLogin(), usuario.getId())) {
            throw new RegraNegocioException("O login \"" + usuario.getLogin() + "\" já está em uso.");
        }
        boolean deixaDeSerAdministradorAtivo = !usuario.isAdministrador() || !usuario.isAtivo();
        if (!usuario.isNova() && deixaDeSerAdministradorAtivo && repositorio.contarOutrosAdministradoresAtivos(usuario.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        if (informouSenha) {
            usuario.setSenhaHash(Senha.gerarHash(senha));

            usuario.setDeveTrocarSenha(!usuario.equals(Sessao.getUsuarioLogado()));
        }
        if (usuario.isNova()) {
            repositorio.inserir(usuario);
        } else {
            repositorio.atualizar(usuario);
        }
    }

    public void alterarSenha(Usuario usuario, char[] senhaAtual, char[] nova, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Validador validador = new Validador()
                .regra(senhaAtual != null && senhaAtual.length > 0, "O campo \"Senha atual\" é obrigatório.");
        validarNovaSenha(validador, nova, confirmacao);
        validador.validar();

        Usuario gravado = repositorio.buscarPorId(usuario.getId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (!Senha.conferir(senhaAtual, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual não confere.");
        }
        if (Senha.conferir(nova, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual.");
        }
        gravado.setSenhaHash(Senha.gerarHash(nova));
        gravado.setDeveTrocarSenha(false);
        repositorio.atualizar(gravado);
        usuario.setSenhaHash(gravado.getSenhaHash());
        usuario.setDeveTrocarSenha(false);
    }

    public void excluir(Usuario usuario) throws RegraNegocioException, PersistenciaException {
        if (usuario.equals(Sessao.getUsuarioLogado())) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário.");
        }
        if (usuario.isAdministrador() && repositorio.contarOutrosAdministradoresAtivos(usuario.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        repositorio.excluir(usuario.getId());
    }

    private static void validarNovaSenha(Validador validador, char[] senha, char[] confirmacao) {
        validador
                .regra(senha != null && senha.length >= TAMANHO_MINIMO_SENHA,
                        "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.")
                .regra(Arrays.equals(senha, confirmacao), "A confirmação não confere com a senha.");
    }
}
