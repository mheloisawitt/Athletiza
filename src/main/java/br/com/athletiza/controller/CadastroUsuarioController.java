package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.usuarios.PainelCadastroUsuario;
import java.util.Arrays;

/**
 * Controller do cadastro de usuário (RF01, CH-13). O sistema nunca fica sem um
 * administrador ativo. Senhas definidas por um administrador para outra pessoa
 * são provisórias (troca obrigatória no próximo acesso).
 */
public class CadastroUsuarioController {

    private final PainelCadastroUsuario view;
    private final Usuario model;
    private final UsuarioRepository repository;
    private final Navegador navegador;

    public CadastroUsuarioController(PainelCadastroUsuario view, Usuario model, UsuarioRepository repository,
            Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.navegador = navegador;
        view.preencherCampos(model);
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoCancelar(e -> cancelar());
    }

    private void salvar() {
        char[] senha = view.getSenha();
        char[] confirmacao = view.getConfirmacao();
        try {
            gravar(senha, confirmacao);
            view.mostrarMensagem("Usuário salvo com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        } finally {
            // A senha não fica na memória depois de usada, mesmo se der erro
            Arrays.fill(senha, '\0');
            Arrays.fill(confirmacao, '\0');
        }
    }

    /**
     * Inclui ou altera o usuário. Na inclusão a senha é obrigatória; na alteração,
     * só é trocada se for informada (redefinição feita pelo administrador).
     */
    void gravar(char[] senha, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setLogin(view.getLogin());
        model.setPerfil(view.getPerfil());
        model.setAtivo(view.isAtivo());
        model.validar(senha, confirmacao);

        if (repository.existeLogin(model.getLogin(), model.getId())) {
            throw new RegraNegocioException("O login \"" + model.getLogin() + "\" já está em uso.");
        }
        if (!model.isNova() && !model.isAdministradorAtivo()
                && repository.contarOutrosAdministradoresAtivos(model.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        if (senha != null && senha.length > 0) {
            model.setSenhaHash(Senha.gerarHash(senha));
            // Senha definida para outra pessoa é provisória: ela troca no primeiro acesso
            model.setDeveTrocarSenha(!model.equals(Sessao.getUsuarioLogado()));
        }
        if (model.isNova()) {
            repository.inserir(model);
        } else {
            repository.atualizar(model);
        }
    }

    private void cancelar() {
        if (view.confirmar("Deseja sair sem salvar? As alterações serão perdidas.")) {
            navegador.voltar();
        }
    }

    public PainelCadastroUsuario getView() {
        return view;
    }
}
