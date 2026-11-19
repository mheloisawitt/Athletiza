package br.com.athletiza.dao;

import br.com.athletiza.repositorio.PastaRepositorio;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Pasta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PastaDAO extends AbstractDAO<Pasta> implements PastaRepositorio {
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
