package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acesso aos dados dos usuários do sistema (RF01).
 */
public class UsuarioDAO extends AbstractDAO<Usuario> {

    @Override
    protected String getTabela() {
        return "usuario";
    }

    @Override
    protected String getNomeEntidade() {
        return "o usuário";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "nome";
    }

    @Override
    public void inserir(Usuario usuario) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO usuario (nome, matricula, contato, login, senha_hash, perfil, ativo,"
                + " deve_trocar_senha) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                usuario.getNome(), usuario.getMatricula(), usuario.getContato(), usuario.getLogin(),
                usuario.getSenhaHash(), usuario.getPerfil(), usuario.isAtivo(), usuario.isDeveTrocarSenha());
        usuario.setId(id);
    }

    @Override
    public void atualizar(Usuario usuario) throws PersistenciaException {
        executarAtualizacao("UPDATE usuario SET nome = ?, matricula = ?, contato = ?, login = ?, senha_hash = ?,"
                + " perfil = ?, ativo = ?, deve_trocar_senha = ? WHERE id = ?",
                usuario.getNome(), usuario.getMatricula(), usuario.getContato(), usuario.getLogin(),
                usuario.getSenhaHash(), usuario.getPerfil(), usuario.isAtivo(), usuario.isDeveTrocarSenha(),
                usuario.getId());
    }

    /** Busca pelo login, sem diferenciar maiúsculas de minúsculas. */
    public Optional<Usuario> buscarPorLogin(String login) throws PersistenciaException {
        return consultarUm("SELECT * FROM usuario WHERE LOWER(login) = LOWER(?)", login);
    }

    /** Indica se outro usuário (diferente de idIgnorado) já usa o login. */
    public boolean existeLogin(String login, Integer idIgnorado) throws PersistenciaException {
        return existe("SELECT COUNT(*) FROM usuario WHERE LOWER(login) = LOWER(?) AND id <> ?",
                login, idIgnorado == null ? -1 : idIgnorado);
    }

    /** Quantos administradores ativos existem, sem contar o usuário informado. */
    public int contarOutrosAdministradoresAtivos(Integer idIgnorado) throws PersistenciaException {
        return consultar("SELECT COUNT(*) FROM usuario WHERE perfil = 'ADMINISTRADOR' AND ativo AND id <> ?",
                rs -> rs.getInt(1), idIgnorado == null ? -1 : idIgnorado).get(0);
    }

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getString("nome"), rs.getString("login"),
                lerEnum(rs, "perfil", Perfil.class));
        usuario.setId(rs.getInt("id"));
        usuario.setMatricula(rs.getString("matricula"));
        usuario.setContato(rs.getString("contato"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setDeveTrocarSenha(rs.getBoolean("deve_trocar_senha"));
        return usuario;
    }
}
