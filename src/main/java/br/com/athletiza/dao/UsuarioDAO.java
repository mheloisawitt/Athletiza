package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Acesso aos dados dos usuários do sistema (RF01).
 */
public class UsuarioDAO extends AbstractDAO<Usuario> {

    /** Usuário com o membro ligado a ele (se houver). */
    private static final String SELECT = "SELECT u.*, m.id AS mb_id, m.nome AS mb_nome, m.matricula AS mb_matricula,"
            + " m.contato AS mb_contato, m.situacao AS mb_situacao"
            + " FROM usuario u LEFT JOIN membro m ON m.id = u.membro_id";

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
        int id = executarInsercao("INSERT INTO usuario (nome, matricula, contato, login, senha_hash, administrador,"
                + " membro_id, ativo, deve_trocar_senha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                usuario.getNome(), usuario.getMatricula(), usuario.getContato(), usuario.getLogin(),
                usuario.getSenhaHash(), usuario.isAdministrador(), idDoMembro(usuario), usuario.isAtivo(),
                usuario.isDeveTrocarSenha());
        usuario.setId(id);
    }

    @Override
    public void atualizar(Usuario usuario) throws PersistenciaException {
        executarAtualizacao("UPDATE usuario SET nome = ?, matricula = ?, contato = ?, login = ?, senha_hash = ?,"
                + " administrador = ?, membro_id = ?, ativo = ?, deve_trocar_senha = ? WHERE id = ?",
                usuario.getNome(), usuario.getMatricula(), usuario.getContato(), usuario.getLogin(),
                usuario.getSenhaHash(), usuario.isAdministrador(), idDoMembro(usuario), usuario.isAtivo(),
                usuario.isDeveTrocarSenha(), usuario.getId());
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) throws PersistenciaException {
        return consultarUm(SELECT + " WHERE u.id = ?", id);
    }

    @Override
    public List<Usuario> listarTodos() throws PersistenciaException {
        return consultar(SELECT + " ORDER BY u.nome");
    }

    /** Busca pelo login, sem diferenciar maiúsculas de minúsculas. */
    public Optional<Usuario> buscarPorLogin(String login) throws PersistenciaException {
        return consultarUm(SELECT + " WHERE LOWER(u.login) = LOWER(?)", login);
    }

    /** Indica se outro usuário (diferente de idIgnorado) já usa o login. */
    public boolean existeLogin(String login, Integer idIgnorado) throws PersistenciaException {
        return existe("SELECT COUNT(*) FROM usuario WHERE LOWER(login) = LOWER(?) AND id <> ?",
                login, idIgnorado == null ? -1 : idIgnorado);
    }

    /** Quantos administradores ativos existem, sem contar o usuário informado. */
    public int contarOutrosAdministradoresAtivos(Integer idIgnorado) throws PersistenciaException {
        return consultar("SELECT COUNT(*) FROM usuario WHERE administrador AND ativo AND id <> ?",
                rs -> rs.getInt(1), idIgnorado == null ? -1 : idIgnorado).get(0);
    }

    private static Integer idDoMembro(Usuario usuario) {
        return usuario.getMembro() == null ? null : usuario.getMembro().getId();
    }

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(rs.getString("nome"), rs.getString("login"));
        usuario.setId(rs.getInt("id"));
        usuario.setAdministrador(rs.getBoolean("administrador"));
        usuario.setMembro(lerInteiro(rs, "mb_id") == null ? null : MembroDAO.mapearMembro(rs, "mb_"));
        usuario.setMatricula(rs.getString("matricula"));
        usuario.setContato(rs.getString("contato"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setDeveTrocarSenha(rs.getBoolean("deve_trocar_senha"));
        return usuario;
    }
}
