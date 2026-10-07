package br.com.athletiza.model;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Tipos de arquivo aceitos na galeria de registros, com suas extensões.
 */
public enum TipoArquivo {

    IMAGEM("Imagem", Set.of("jpg", "jpeg", "png", "gif", "bmp")),
    VIDEO("Vídeo", Set.of("mp4", "mov", "avi", "mkv", "webm", "wmv"));

    private final String descricao;
    private final Set<String> extensoes;

    TipoArquivo(String descricao, Set<String> extensoes) {
        this.descricao = descricao;
        this.extensoes = extensoes;
    }

    public String getDescricao() {
        return descricao;
    }

    public Set<String> getExtensoes() {
        return extensoes;
    }

    /** Descobre o tipo pela extensão do nome do arquivo (vazio se não for aceito). */
    public static Optional<TipoArquivo> doArquivo(String nomeArquivo) {
        int ponto = nomeArquivo == null ? -1 : nomeArquivo.lastIndexOf('.');
        if (ponto < 0) {
            return Optional.empty();
        }
        String extensao = nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT);
        for (TipoArquivo tipo : values()) {
            if (tipo.extensoes.contains(extensao)) {
                return Optional.of(tipo);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return descricao;
    }
}
