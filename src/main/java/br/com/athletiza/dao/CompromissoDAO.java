package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Compromisso;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos dados dos compromissos e ações do calendário (CH-16).
 */
public class CompromissoDAO extends AbstractDAO<Compromisso> {

    @Override
    protected String getTabela() {
        return "compromisso";
    }

    @Override
    protected String getNomeEntidade() {
        return "o compromisso";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "data, horario";
    }

    @Override
    public void inserir(Compromisso compromisso) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO compromisso (titulo, data, horario, local, observacoes) VALUES (?, ?, ?, ?, ?)",
                compromisso.getTitulo(), compromisso.getData(), compromisso.getHorario(), compromisso.getLocal(),
                compromisso.getObservacoes());
        compromisso.setId(id);
    }

    @Override
    public void atualizar(Compromisso compromisso) throws PersistenciaException {
        executarAtualizacao("UPDATE compromisso SET titulo = ?, data = ?, horario = ?, local = ?, observacoes = ? WHERE id = ?",
                compromisso.getTitulo(), compromisso.getData(), compromisso.getHorario(), compromisso.getLocal(),
                compromisso.getObservacoes(), compromisso.getId());
    }

    public List<Compromisso> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        return consultar("SELECT * FROM compromisso WHERE data BETWEEN ? AND ? ORDER BY data, horario", inicio, fim);
    }

    @Override
    protected Compromisso mapear(ResultSet rs) throws SQLException {
        Compromisso compromisso = new Compromisso(rs.getString("titulo"), lerData(rs, "data"),
                lerHorario(rs, "horario"), rs.getString("local"));
        compromisso.setId(rs.getInt("id"));
        compromisso.setObservacoes(rs.getString("observacoes"));
        return compromisso;
    }
}
