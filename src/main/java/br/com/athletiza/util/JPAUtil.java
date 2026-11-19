package br.com.athletiza.util;

import br.com.athletiza.exception.PersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class JPAUtil {

    private static final String UNIDADE = "athletiza";

    private static EntityManagerFactory fabrica;
    private static String urlAtual;

    private JPAUtil() {
    }

    public static synchronized EntityManager getEntityManager() throws PersistenciaException {
        Properties config = ConnectionFactory.getPropriedades();
        String url = config.getProperty("db.url");
        if (fabrica == null || !url.equals(urlAtual)) {
            fechar();
            Map<String, String> propriedades = new HashMap<>();
            propriedades.put("jakarta.persistence.jdbc.url", url);
            propriedades.put("jakarta.persistence.jdbc.user", config.getProperty("db.usuario", ""));
            propriedades.put("jakarta.persistence.jdbc.password", config.getProperty("db.senha", ""));
            try {
                fabrica = Persistence.createEntityManagerFactory(UNIDADE, propriedades);
            } catch (PersistenceException e) {
                throw new PersistenciaException("Não foi possível conectar ao banco de dados pelo JPA. "
                        + "Verifique se o MySQL está em execução e se os dados em db.properties estão corretos.", e);
            }
            urlAtual = url;
        }
        return fabrica.createEntityManager();
    }

    public static synchronized void fechar() {
        if (fabrica != null) {
            fabrica.close();
            fabrica = null;
            urlAtual = null;
        }
    }
}
