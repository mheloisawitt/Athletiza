package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Entidade;
import br.com.athletiza.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base dos DAOs JDBC (CH-06). Implementa as operações comuns (excluir,
 * buscarPorId, listarTodos) e oferece métodos auxiliares para os DAOs concretos.
 *
 * Todo SQLException é convertido em PersistenciaException com mensagem
 * compreensível; o erro técnico vai apenas para o log.
 */
public abstract class AbstractDAO<T extends Entidade> implements GenericDAO<T> {

    private static final Logger LOG = Logger.getLogger(AbstractDAO.class.getName());

    /** Códigos SQLState padrão (PostgreSQL e H2). */
    private static final String VIOLACAO_CHAVE_ESTRANGEIRA = "23503";
    private static final String VIOLACAO_UNICIDADE = "23505";
    private static final String VIOLACAO_CHECK = "23514";

    /** Nome da tabela no banco. */
    protected abstract String getTabela();

    /** Nome da entidade com artigo, usado nas mensagens (ex.: "a modalidade"). */
    protected abstract String getNomeEntidade();

    /** Converte a linha atual do ResultSet na entidade. */
    protected abstract T mapear(ResultSet rs) throws SQLException;

    /** Ordenação usada em listarTodos(). */
    protected String getOrdenacaoPadrao() {
        return "id";
    }

    @Override
    public void excluir(int id) throws PersistenciaException {
        executarAtualizacao("DELETE FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public Optional<T> buscarPorId(int id) throws PersistenciaException {
        return consultarUm("SELECT * FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public List<T> listarTodos() throws PersistenciaException {
        return consultar("SELECT * FROM " + getTabela() + " ORDER BY " + getOrdenacaoPadrao());
    }

    /** Executa um INSERT e devolve o id gerado. */
    protected int executarInsercao(String sql, Object... parametros) throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
            preencherParametros(ps, parametros);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco não retornou o id gerado.");
                }
                return chaves.getInt(1);
            }
        } catch (SQLException e) {
            throw traduzir(e, "incluir");
        }
    }

    /** Executa UPDATE ou DELETE e devolve a quantidade de linhas afetadas. */
    protected int executarAtualizacao(String sql, Object... parametros) throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            preencherParametros(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw traduzir(e, sql.trim().toUpperCase().startsWith("DELETE") ? "excluir" : "salvar");
        }
    }

    protected List<T> consultar(String sql, Object... parametros) throws PersistenciaException {
        return consultar(sql, this::mapear, parametros);
    }

    protected <R> List<R> consultar(String sql, MapeadorLinha<R> mapeador, Object... parametros)
            throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            preencherParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<R> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapeador.mapear(rs));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw traduzir(e, "consultar");
        }
    }

    protected Optional<T> consultarUm(String sql, Object... parametros) throws PersistenciaException {
        List<T> resultado = consultar(sql, parametros);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    /** Verifica se a consulta (um SELECT COUNT) retorna valor maior que zero. */
    protected boolean existe(String sqlContagem, Object... parametros) throws PersistenciaException {
        return consultar(sqlContagem, rs -> rs.getInt(1), parametros).get(0) > 0;
    }

    /** Converte o erro técnico em uma mensagem que o usuário entende. */
    protected PersistenciaException traduzir(SQLException e, String operacao) {
        LOG.log(Level.SEVERE, "Erro ao " + operacao + " " + getNomeEntidade(), e);
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensagem = switch (estado) {
            case VIOLACAO_CHAVE_ESTRANGEIRA -> "excluir".equals(operacao)
                    ? "Não é possível excluir " + getNomeEntidade() + ": existem outros registros vinculados."
                    : "Não é possível salvar " + getNomeEntidade() + ": um dos registros relacionados não existe mais.";
            case VIOLACAO_UNICIDADE -> "Não é possível salvar " + getNomeEntidade() + ": já existe um cadastro com esses dados.";
            case VIOLACAO_CHECK -> "Não é possível salvar " + getNomeEntidade() + ": há dados inválidos.";
            default -> "Erro ao " + operacao + " " + getNomeEntidade() + ". Tente novamente.";
        };
        return new PersistenciaException(mensagem, e);
    }

    private static void preencherParametros(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            int posicao = i + 1;
            if (valor instanceof LocalDate data) {
                ps.setDate(posicao, Date.valueOf(data));
            } else if (valor instanceof LocalTime hora) {
                ps.setTime(posicao, Time.valueOf(hora));
            } else if (valor instanceof LocalDateTime dataHora) {
                ps.setTimestamp(posicao, Timestamp.valueOf(dataHora));
            } else if (valor instanceof Enum<?> constante) {
                ps.setString(posicao, constante.name());
            } else {
                ps.setObject(posicao, valor);
            }
        }
    }

    /* Leitura de colunas que podem ser nulas */

    protected static LocalDate lerData(ResultSet rs, String coluna) throws SQLException {
        Date data = rs.getDate(coluna);
        return data == null ? null : data.toLocalDate();
    }

    protected static LocalTime lerHorario(ResultSet rs, String coluna) throws SQLException {
        Time hora = rs.getTime(coluna);
        return hora == null ? null : hora.toLocalTime();
    }

    protected static LocalDateTime lerDataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp dataHora = rs.getTimestamp(coluna);
        return dataHora == null ? null : dataHora.toLocalDateTime();
    }

    protected static Integer lerInteiro(ResultSet rs, String coluna) throws SQLException {
        int valor = rs.getInt(coluna);
        return rs.wasNull() ? null : valor;
    }

    protected static <E extends Enum<E>> E lerEnum(ResultSet rs, String coluna, Class<E> tipo) throws SQLException {
        String valor = rs.getString(coluna);
        return valor == null ? null : Enum.valueOf(tipo, valor);
    }
}
