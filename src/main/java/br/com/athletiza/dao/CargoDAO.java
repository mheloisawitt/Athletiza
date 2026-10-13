package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Setor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        int id = executarInsercao("INSERT INTO cargo (nome, ordem, setor, cargo_superior_id) VALUES (?, ?, ?, ?)",
                cargo.getNome(), cargo.getOrdem(), cargo.getSetor(), idDoSuperior(cargo));
        cargo.setId(id);
    }

    @Override
    public void atualizar(Cargo cargo) throws PersistenciaException {
        executarAtualizacao("UPDATE cargo SET nome = ?, ordem = ?, setor = ?, cargo_superior_id = ? WHERE id = ?",
                cargo.getNome(), cargo.getOrdem(), cargo.getSetor(), idDoSuperior(cargo), cargo.getId());
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

    /** Cargos que o membro ocupa nas gestões em andamento (com o cargo superior, para saber se é assessoria). */
    public List<Cargo> listarDoMembroNaGestaoEmAndamento(int membroId) throws PersistenciaException {
        Set<Integer> ids = new HashSet<>(consultar("SELECT gmc.cargo_id FROM gestao_membro_cargo gmc"
                + " JOIN gestao g ON g.id = gmc.gestao_id WHERE gmc.membro_id = ? AND g.situacao = 'EM_ANDAMENTO'",
                rs -> rs.getInt(1), membroId));
        return listarTodos().stream().filter(c -> ids.contains(c.getId())).toList();
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
        cargo.setSetor(lerEnum(rs, "setor", Setor.class));
        return cargo;
    }
}
