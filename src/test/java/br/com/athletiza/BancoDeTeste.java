package br.com.athletiza;

import br.com.athletiza.util.ConnectionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Prepara um banco para os testes executando os scripts de src/main/resources/sql.
 *
 * Por padrão usa H2 em memória (modo PostgreSQL), que não exige instalação.
 * Para testar em um PostgreSQL real, rode com:
 * mvn test -Dteste.db.url=jdbc:postgresql://localhost:5432/athletiza_teste -Dteste.db.usuario=postgres -Dteste.db.senha=...
 */
public final class BancoDeTeste {

    private BancoDeTeste() {
    }

    /** Recria as tabelas e carrega os dados de exemplo. */
    public static void recriar() throws SQLException {
        Properties config = new Properties();
        config.setProperty("db.url", System.getProperty("teste.db.url",
                "jdbc:h2:mem:athletiza;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1"));
        config.setProperty("db.usuario", System.getProperty("teste.db.usuario", "sa"));
        config.setProperty("db.senha", System.getProperty("teste.db.senha", ""));
        ConnectionFactory.configurar(config);

        try (Connection con = ConnectionFactory.getConnection(); Statement st = con.createStatement()) {
            for (String script : new String[]{"01_estrutura.sql", "02_dados_exemplo.sql"}) {
                for (String comando : lerScript(script).split(";")) {
                    if (!comando.isBlank()) {
                        st.execute(comando);
                    }
                }
            }
        } catch (br.com.athletiza.exception.PersistenciaException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private static String lerScript(String nome) {
        try (InputStream entrada = BancoDeTeste.class.getResourceAsStream("/sql/" + nome)) {
            String texto = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            return texto.lines()
                    .filter(linha -> !linha.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
