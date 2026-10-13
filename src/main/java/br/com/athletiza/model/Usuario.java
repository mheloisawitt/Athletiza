package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Usuário que acessa o sistema (RF01). A senha nunca é guardada em texto
 * puro, apenas o hash.
 */
public class Usuario extends Pessoa {

    public static final int TAMANHO_MINIMO_SENHA = 6;
    private static final Pattern LOGIN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private String login;
    private String senhaHash;
    private Perfil perfil = Perfil.CONSULTA;
    private boolean ativo = true;
    private boolean deveTrocarSenha;

    public Usuario() {
    }

    public Usuario(String nome, String login, Perfil perfil) {
        setNome(nome);
        this.login = login;
        this.perfil = perfil;
    }

    /**
     * Regras do cadastro (RF01). Na inclusão a senha é obrigatória; na alteração,
     * só é conferida se for informada (redefinição feita pelo administrador).
     */
    public void validar(char[] senha, char[] confirmacao) throws ValidacaoException {
        boolean informouSenha = senha != null && senha.length > 0;
        Validador validador = new Validador()
                .obrigatorio(getNome(), "Nome")
                .tamanhoMaximo(getNome(), 100, "Nome")
                .obrigatorio(login, "Login")
                .regra(login == null || login.isBlank() || LOGIN.matcher(login).matches(),
                        "O \"Login\" deve ter de 3 a 50 caracteres, sem espaços (letras, números, ponto, hífen ou _).")
                .obrigatorio(perfil, "Perfil")
                .regra(!isNova() || informouSenha, "O campo \"Senha\" é obrigatório para novos usuários.");
        if (informouSenha) {
            validarNovaSenha(validador, senha, confirmacao);
        }
        validador.validar();
    }

    /** Regras de uma nova senha: tamanho mínimo e confirmação igual. */
    public static void validarNovaSenha(Validador validador, char[] senha, char[] confirmacao) {
        validador
                .regra(senha != null && senha.length >= TAMANHO_MINIMO_SENHA,
                        "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.")
                .regra(Arrays.equals(senha, confirmacao), "A confirmação não confere com a senha.");
    }

    /** Indica se o usuário é administrador e está ativo. */
    public boolean isAdministradorAtivo() {
        return perfil == Perfil.ADMINISTRADOR && ativo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /** Senha provisória: o usuário precisa trocá-la no próximo acesso. */
    public boolean isDeveTrocarSenha() {
        return deveTrocarSenha;
    }

    public void setDeveTrocarSenha(boolean deveTrocarSenha) {
        this.deveTrocarSenha = deveTrocarSenha;
    }

    /** Usuários são identificados pelo login, não pela matrícula. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario) || login == null) {
            return false;
        }
        Usuario outro = (Usuario) o;
        return login.equalsIgnoreCase(outro.login);
    }

    @Override
    public int hashCode() {
        return login == null ? System.identityHashCode(this) : Objects.hash(login.toLowerCase());
    }
}
