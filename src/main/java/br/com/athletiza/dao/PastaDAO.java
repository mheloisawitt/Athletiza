package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Pasta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acesso aos dados das pastas da galeria de registros.
 */
public class PastaDAO extends AbstractDAO<Pasta> {

    @Override
    protected String getTabela() {
        return "pasta";
    }

    @Override
    protected String getNomeEntidade() {
        return "a pasta";
    }

    @Override
    public void inserir(Pasta pasta) throws PersistenciaException {
        pasta.setId(executarInsercao("INSERT INTO pasta (nome) VALUES (?)", pasta.getNome()));
    }

    @Override
    public void atualizar(Pasta pasta) throws PersistenciaException {
        executarAtualizacao("UPDATE pasta SET nome = ? WHERE id = ?", pasta.getNome(), pasta.getId());
    }

    /** Pastas com a quantidade de arquivos de cada uma. */
    @Override
    public List<Pasta> listarTodos() throws PersistenciaException {
        return consultar("SELECT p.*, (SELECT COUNT(*) FROM arquivo a WHERE a.pasta_id = p.id) AS quantidade"
                + " FROM pasta p ORDER BY p.nome");
    }

    @Override
    protected Pasta mapear(ResultSet rs) throws SQLException {
        Pasta pasta = new Pasta(rs.getString("nome"));
        pasta.setId(rs.getInt("id"));
        if (temColuna(rs, "quantidade")) {
            pasta.setQuantidadeArquivos(rs.getInt("quantidade"));
        }
        return pasta;
    }

    private static boolean temColuna(ResultSet rs, String coluna) throws SQLException {
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            if (coluna.equalsIgnoreCase(rs.getMetaData().getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
