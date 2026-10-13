package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Senha;
import br.com.athletiza.view.usuarios.DialogoAlterarSenha;
import java.util.Arrays;

/**
 * Controller da troca da própria senha. Exige a senha atual.
 */
public class AlterarSenhaController {

    private final DialogoAlterarSenha view;
    private final Usuario model;
    private final UsuarioRepository repository;

    public AlterarSenhaController(DialogoAlterarSenha view, Usuario model, UsuarioRepository repository) {
        this.view = view;
        this.model = model;
        this.repository = repository;
    }

    /**
     * Abre o diálogo e o reabre enquanto houver erro.
     *
     * @return true se a senha foi trocada; false se o usuário desistiu
     */
    public boolean executar(String titulo) {
        while (view.exibir(titulo)) {
            char[] atual = view.getSenhaAtual();
            char[] nova = view.getNovaSenha();
            char[] confirmacao = view.getConfirmacao();
            try {
                alterarSenha(atual, nova, confirmacao);
                view.mostrarMensagem("Senha alterada com sucesso.");
                return true;
            } catch (AthletizaException e) {
                view.mostrarErro(e);
            } finally {
                Arrays.fill(atual, '\0');
                Arrays.fill(nova, '\0');
                Arrays.fill(confirmacao, '\0');
            }
        }
        return false;
    }

    void alterarSenha(char[] senhaAtual, char[] nova, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Validador validador = new Validador()
                .regra(senhaAtual != null && senhaAtual.length > 0, "O campo \"Senha atual\" é obrigatório.");
        Usuario.validarNovaSenha(validador, nova, confirmacao);
        validador.validar();

        Usuario gravado = repository.buscarPorId(model.getId());
        if (gravado == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }
        if (!Senha.conferir(senhaAtual, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual não confere.");
        }
        if (Senha.conferir(nova, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual.");
        }
        gravado.setSenhaHash(Senha.gerarHash(nova));
        gravado.setDeveTrocarSenha(false);
        repository.atualizar(gravado);
        model.setSenhaHash(gravado.getSenhaHash());
        model.setDeveTrocarSenha(false);
    }
}
