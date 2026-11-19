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

public final class BancoDeTeste {
    private BancoDeTeste() {
    }

    public static void recriar() throws SQLException {
        Properties config = new Properties();
        config.setProperty("db.url", System.getProperty("teste.db.url",
                "jdbc:h2:mem:athletiza;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"));
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

    public static void executar(String script) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); Statement st = con.createStatement()) {
            for (String comando : lerScript(script).split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando);
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

                    .filter(linha -> !linha.trim().toUpperCase().startsWith("USE "))
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
