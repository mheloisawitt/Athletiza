package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso aos dados dos cargos da gestão (CH-18).
 */
public class CargoDAO extends AbstractDAO<Cargo> {

    @Override
    protected String getTabela() {
        return "cargo";
    }

    @Override
    protected String getNomeEntidade() {
        return "o cargo";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "ordem, nome";
    }

    @Override
    public void inserir(Cargo cargo) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO cargo (nome, ordem, cargo_superior_id) VALUES (?, ?, ?)",
                cargo.getNome(), cargo.getOrdem(), idDoSuperior(cargo));
        cargo.setId(id);
    }

    @Override
    public void atualizar(Cargo cargo) throws PersistenciaException {
        executarAtualizacao("UPDATE cargo SET nome = ?, ordem = ?, cargo_superior_id = ? WHERE id = ?",
                cargo.getNome(), cargo.getOrdem(), idDoSuperior(cargo), cargo.getId());
    }

    /** Todos os cargos, cada um ligado ao objeto do seu cargo superior. */
    @Override
    public List<Cargo> listarTodos() throws PersistenciaException {
        Map<Integer, Integer> superiores = new HashMap<>();
        List<Cargo> cargos = consultar("SELECT * FROM cargo ORDER BY " + getOrdenacaoPadrao(), rs -> {
            Cargo cargo = mapear(rs);
            Integer superior = lerInteiro(rs, "cargo_superior_id");
            if (superior != null) {
                superiores.put(cargo.getId(), superior);
            }
            return cargo;
        });
        Map<Integer, Cargo> porId = new HashMap<>();
        cargos.forEach(c -> porId.put(c.getId(), c));
        cargos.forEach(c -> c.setCargoSuperior(porId.get(superiores.get(c.getId()))));
        return cargos;
    }

    @Override
    public Optional<Cargo> buscarPorId(int id) throws PersistenciaException {
        return listarTodos().stream().filter(c -> c.getId() == id).findFirst();
    }

    /** Quantas vezes o cargo está ocupado, somando todas as gestões. */
    public int contarOcupacoes(int id) throws PersistenciaException {
        return consultar("SELECT COUNT(*) FROM gestao_membro_cargo WHERE cargo_id = ?", rs -> rs.getInt(1), id).get(0);
    }

    private static Integer idDoSuperior(Cargo cargo) {
        return cargo.getCargoSuperior() == null ? null : cargo.getCargoSuperior().getId();
    }

    @Override
    protected Cargo mapear(ResultSet rs) throws SQLException {
        Cargo cargo = new Cargo(rs.getString("nome"), rs.getInt("ordem"), null);
        cargo.setId(rs.getInt("id"));
        return cargo;
    }
}
