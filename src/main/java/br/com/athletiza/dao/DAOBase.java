package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Métodos auxiliares de JDBC compartilhados por todos os DAOs: execução de
 * comandos, consultas, leitura de colunas e tradução de erros.
 *
 * Todo SQLException é convertido em PersistenciaException com mensagem
 * compreensível; o erro técnico vai apenas para o log.
 */
public abstract class DAOBase {

    private static final Logger LOG = Logger.getLogger(DAOBase.class.getName());

    /** Tipos de erro do banco que viram mensagens específicas para o usuário. */
    enum Violacao { CHAVE_ESTRANGEIRA, UNICIDADE, CHECK, OUTRA }

    /** Nome do que o DAO grava, com artigo, usado nas mensagens (ex.: "a modalidade"). */
    protected abstract String getNomeEntidade();

    /**
     * Operação com vários comandos que precisam dar certo juntos.
     * Interface funcional: normalmente implementada com lambda.
     */
    @FunctionalInterface
    protected interface Transacao<R> {

        R executar(Connection conexao) throws SQLException;
    }

    /** Executa um INSERT e devolve o id gerado. */
    protected int executarInsercao(String sql, Object... parametros) throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection()) {
            return inserir(con, sql, parametros);
        } catch (SQLException e) {
            throw traduzir(e, "incluir");
        }
    }

    /** Executa UPDATE ou DELETE e devolve a quantidade de linhas afetadas. */
    protected int executarAtualizacao(String sql, Object... parametros) throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection()) {
            return atualizar(con, sql, parametros);
        } catch (SQLException e) {
            throw traduzir(e, sql.trim().toUpperCase().startsWith("DELETE") ? "excluir" : "salvar");
        }
    }

    /**
     * Executa vários comandos em uma única transação: ou todos são gravados,
     * ou nenhum é (ex.: atleta e suas modalidades).
     *
     * @param operacao verbo usado na mensagem de erro (ex.: "salvar")
     */
    protected <R> R emTransacao(String operacao, Transacao<R> transacao) throws PersistenciaException {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                R resultado = transacao.executar(con);
                con.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traduzir(e, operacao);
        }
    }

    /** INSERT usando uma conexão já aberta (dentro de uma transação). Devolve o id gerado. */
    protected static int inserir(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
            preencherParametros(ps, parametros);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco não retornou o id gerado.");
                }
                return chaves.getInt(1);
            }
        }
    }

    /** UPDATE ou DELETE usando uma conexão já aberta (dentro de uma transação). */
    protected static int atualizar(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            preencherParametros(ps, parametros);
            return ps.executeUpdate();
        }
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

    /** Verifica se a consulta (um SELECT COUNT) retorna valor maior que zero. */
    protected boolean existe(String sqlContagem, Object... parametros) throws PersistenciaException {
        return consultar(sqlContagem, rs -> rs.getInt(1), parametros).get(0) > 0;
    }

    /** Converte o erro técnico em uma mensagem que o usuário entende. */
    protected PersistenciaException traduzir(SQLException e, String operacao) {
        LOG.log(Level.SEVERE, "Erro ao " + operacao + " " + getNomeEntidade(), e);
        String mensagem = switch (violacao(e)) {
            case CHAVE_ESTRANGEIRA -> "excluir".equals(operacao)
                    ? "Não é possível excluir " + getNomeEntidade() + ": existem outros registros vinculados."
                    : "Não é possível salvar " + getNomeEntidade() + ": um dos registros relacionados não existe mais.";
            case UNICIDADE -> "Não é possível salvar " + getNomeEntidade() + ": já existe um cadastro com esses dados.";
            case CHECK -> "Não é possível salvar " + getNomeEntidade() + ": há dados inválidos.";
            case OUTRA -> "Erro ao " + operacao + " " + getNomeEntidade() + ". Tente novamente.";
        };
        return new PersistenciaException(mensagem, e);
    }

    /**
     * Identifica o tipo do erro. O MySQL informa pelo código do erro
     * (1062 duplicado, 1451/1452 chave estrangeira, 3819 check);
     * o H2 dos testes, pelo SQLState padrão (23505, 23503, 23513).
     */
    static Violacao violacao(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        return switch (e.getErrorCode()) {
            case 1062 -> Violacao.UNICIDADE;
            case 1451, 1452 -> Violacao.CHAVE_ESTRANGEIRA;
            case 3819 -> Violacao.CHECK;
            default -> switch (estado) {
                case "23505" -> Violacao.UNICIDADE;
                case "23503", "23506" -> Violacao.CHAVE_ESTRANGEIRA;
                case "23513", "23514" -> Violacao.CHECK;
                default -> Violacao.OUTRA;
            };
        };
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
