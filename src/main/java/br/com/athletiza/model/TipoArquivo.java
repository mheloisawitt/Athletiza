package br.com.athletiza.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Tipos de arquivo aceitos na galeria de registros, com suas extensões.
 */
public enum TipoArquivo {

    IMAGEM("Imagem", "jpg", "jpeg", "png", "gif", "bmp"),
    VIDEO("Vídeo", "mp4", "mov", "avi", "mkv", "webm", "wmv");

    private final String descricao;
    private final Set<String> extensoes;

    TipoArquivo(String descricao, String... extensoes) {
        this.descricao = descricao;
        this.extensoes = new HashSet<>(Arrays.asList(extensoes));
    }

    public String getDescricao() {
        return descricao;
    }

    public Set<String> getExtensoes() {
        return extensoes;
    }

    /** Descobre o tipo pela extensão do nome do arquivo (null se não for aceito). */
    public static TipoArquivo doArquivo(String nomeArquivo) {
        int ponto = nomeArquivo == null ? -1 : nomeArquivo.lastIndexOf('.');
        if (ponto < 0) {
            return null;
        }
        String extensao = nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT);
        for (TipoArquivo tipo : values()) {
            if (tipo.extensoes.contains(extensao)) {
                return tipo;
            }
        }
        return null;
    }

    /** Todas as extensões aceitas (fotos e vídeos), para o filtro do seletor de arquivos. */
    public static String[] todasAsExtensoes() {
        List<String> todas = new ArrayList<>();
        for (TipoArquivo tipo : values()) {
            todas.addAll(tipo.extensoes);
        }
        return todas.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
