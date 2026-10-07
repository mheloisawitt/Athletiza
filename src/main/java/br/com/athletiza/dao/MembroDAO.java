package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados dos membros da atlética (CH-22).
 */
public class MembroDAO extends AbstractDAO<Membro> {

    @Override
    protected String getTabela() {
        return "membro";
    }

    @Override
    protected String getNomeEntidade() {
        return "o membro";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "nome";
    }

    @Override
    public void inserir(Membro membro) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO membro (nome, matricula, contato, situacao) VALUES (?, ?, ?, ?)",
                membro.getNome(), membro.getMatricula(), membro.getContato(), membro.getSituacao());
        membro.setId(id);
    }

    @Override
    public void atualizar(Membro membro) throws PersistenciaException {
        executarAtualizacao("UPDATE membro SET nome = ?, matricula = ?, contato = ?, situacao = ? WHERE id = ?",
                membro.getNome(), membro.getMatricula(), membro.getContato(), membro.getSituacao(), membro.getId());
    }

    /** Indica se outro membro (diferente de idIgnorado) já usa a matrícula. */
    public boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException {
        return existe("SELECT COUNT(*) FROM membro WHERE matricula = ? AND id <> ?",
                matricula, idIgnorado == null ? -1 : idIgnorado);
    }

    /** Descreve o que impede a exclusão do membro (CH-45). Lista vazia: pode excluir. */
    public List<String> listarVinculos(int id) throws PersistenciaException {
        List<String> vinculos = new ArrayList<>();
        adicionarSeHouver(vinculos, "cargo(s) em gestões",
                "SELECT COUNT(*) FROM gestao_membro_cargo WHERE membro_id = ?", id);
        adicionarSeHouver(vinculos, "evento(s) sob sua responsabilidade",
                "SELECT COUNT(*) FROM evento_responsavel WHERE membro_id = ?", id);
        adicionarSeHouver(vinculos, "tarefa(s) sob sua responsabilidade",
                "SELECT COUNT(*) FROM tarefa WHERE responsavel_id = ?", id);
        return vinculos;
    }

    private void adicionarSeHouver(List<String> vinculos, String descricao, String sql, int id)
            throws PersistenciaException {
        int quantidade = consultar(sql, rs -> rs.getInt(1), id).get(0);
        if (quantidade > 0) {
            vinculos.add(quantidade + " " + descricao);
        }
    }

    @Override
    protected Membro mapear(ResultSet rs) throws SQLException {
        return mapearMembro(rs, "");
    }

    /** Lê um membro de colunas com prefixo (ex.: "membro_nome"), para consultas com JOIN. */
    static Membro mapearMembro(ResultSet rs, String prefixo) throws SQLException {
        Membro membro = new Membro(rs.getString(prefixo + "nome"), rs.getString(prefixo + "matricula"),
                rs.getString(prefixo + "contato"));
        membro.setId(rs.getInt(prefixo + "id"));
        membro.setSituacao(lerEnum(rs, prefixo + "situacao", Situacao.class));
        return membro;
    }
}
