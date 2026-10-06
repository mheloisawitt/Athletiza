package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acesso aos dados das modalidades. Também serve de exemplo de DAO concreto
 * para os demais módulos (CH-06).
 */
public class ModalidadeDAO extends AbstractDAO<Modalidade> {

    @Override
    protected String getTabela() {
        return "modalidade";
    }

    @Override
    protected String getNomeEntidade() {
        return "a modalidade";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "nome, genero";
    }

    @Override
    public void inserir(Modalidade modalidade) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO modalidade (nome, genero) VALUES (?, ?)",
                modalidade.getNome(), modalidade.getGenero());
        modalidade.setId(id);
    }

    @Override
    public void atualizar(Modalidade modalidade) throws PersistenciaException {
        executarAtualizacao("UPDATE modalidade SET nome = ?, genero = ? WHERE id = ?",
                modalidade.getNome(), modalidade.getGenero(), modalidade.getId());
    }

    /** Indica se a modalidade possui atletas, treinos ou competições vinculados (CH-27). */
    public boolean possuiVinculos(int id) throws PersistenciaException {
        return existe("SELECT (SELECT COUNT(*) FROM atleta_modalidade WHERE modalidade_id = ?)"
                + " + (SELECT COUNT(*) FROM treino WHERE modalidade_id = ?)"
                + " + (SELECT COUNT(*) FROM competicao_modalidade WHERE modalidade_id = ?)", id, id, id);
    }

    @Override
    protected Modalidade mapear(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade(rs.getString("nome"), lerEnum(rs, "genero", Genero.class));
        modalidade.setId(rs.getInt("id"));
        return modalidade;
    }
}
