package br.com.athletiza.util;

import br.com.athletiza.exception.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionFactory {
    private static final Logger LOG = Logger.getLogger(ConnectionFactory.class.getName());
    private static final String ARQUIVO = "db.properties";

    private static Properties configuracao;

    private ConnectionFactory() {
    }

    public static synchronized void configurar(Properties propriedades) {
        configuracao = propriedades;
    }

    public static Connection getConnection() throws PersistenciaException {
        Properties config = getConfiguracao();
        String url = config.getProperty("db.url");
        try {
            return DriverManager.getConnection(url, config.getProperty("db.usuario"), config.getProperty("db.senha"));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao conectar em " + url, e);
            throw new PersistenciaException("Não foi possível conectar ao banco de dados. "
                    + "Verifique se o MySQL está em execução e se os dados em " + ARQUIVO + " estão corretos.", e);
        }
    }

    public static void testarConexao() throws PersistenciaException {
        try (Connection conexao = getConnection()) {
            if (!conexao.isValid(5)) {
                throw new PersistenciaException("O banco de dados não respondeu. Tente novamente.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao verificar a conexão com o banco de dados.", e);
        }
    }

    public static Properties getPropriedades() throws PersistenciaException {
        return getConfiguracao();
    }

    private static synchronized Properties getConfiguracao() throws PersistenciaException {
        if (configuracao == null) {
            configuracao = carregar();
        }
        return configuracao;
    }

    private static Properties carregar() throws PersistenciaException {
        Properties propriedades = new Properties();
        Path arquivo = Path.of(ARQUIVO);
        try {
            if (Files.exists(arquivo)) {
                try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                    propriedades.load(leitor);
                }
                return propriedades;
            }
            try (InputStream entrada = ConnectionFactory.class.getResourceAsStream("/" + ARQUIVO)) {
                if (entrada != null) {
                    propriedades.load(entrada);
                    return propriedades;
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + ARQUIVO + ".", e);
        }
        throw new PersistenciaException("Arquivo " + ARQUIVO + " não encontrado. "
                + "Copie o db.properties.example para db.properties e preencha os dados do seu banco.");
    }
}
