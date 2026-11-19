package br.com.athletiza.util;

import br.com.athletiza.excecao.PersistenciaException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArmazenamentoMidia {
    private static final Logger LOG = Logger.getLogger(ArmazenamentoMidia.class.getName());

    private ArmazenamentoMidia() {
    }

    public static Path getPastaRaiz() {
        String configurada = System.getProperty("athletiza.midias");
        return configurada != null && !configurada.isBlank()
                ? Path.of(configurada)
                : Path.of(System.getProperty("user.home"), "Documents", "Athletiza-midias");
    }

    public static String copiar(Path origem, int pastaId) throws PersistenciaException {
        String relativo = "pasta-" + pastaId + "/" + UUID.randomUUID().toString().substring(0, 8) + "-"
                + nomeSeguro(origem.getFileName().toString());
        Path destino = resolver(relativo);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(origem, destino, StandardCopyOption.COPY_ATTRIBUTES);
            return relativo;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Falha ao copiar " + origem + " para " + destino, e);
            throw new PersistenciaException("Não foi possível copiar o arquivo " + origem.getFileName()
                    + " para a pasta de registros (" + getPastaRaiz() + ").", e);
        }
    }

    public static Path resolver(String caminhoRelativo) {
        return getPastaRaiz().resolve(caminhoRelativo).normalize();
    }

    public static void apagar(String caminhoRelativo) {
        try {
            Files.deleteIfExists(resolver(caminhoRelativo));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Não foi possível apagar " + caminhoRelativo, e);
        }
    }

    static String nomeSeguro(String nome) {
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String seguro = semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-").replaceAll("-{2,}", "-");
        return seguro.length() > 80 ? seguro.substring(seguro.length() - 80) : seguro;
    }
}
