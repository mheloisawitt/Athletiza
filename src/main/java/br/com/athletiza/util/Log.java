package br.com.athletiza.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Grava os erros técnicos em arquivo (CH-46): o usuário vê uma mensagem amigável
 * e os detalhes (stack trace) ficam no log, para quem for dar suporte.
 *
 * Pasta padrão: Documentos/Athletiza-logs, na pasta do usuário. Pode ser trocada
 * com a propriedade de sistema "athletiza.logs". São mantidos até 5 arquivos de 1 MB.
 */
public final class Log {

    private static final String ARQUIVO = "athletiza-%g.log";
    private static FileHandler arquivo;

    private Log() {
    }

    public static Path getPasta() {
        String configurada = System.getProperty("athletiza.logs");
        return configurada != null && !configurada.isBlank()
                ? Path.of(configurada)
                : Path.of(System.getProperty("user.home"), "Documents", "Athletiza-logs");
    }

    /** Arquivo de log atual (o mais recente). */
    public static Path getArquivoAtual() {
        return getPasta().resolve(ARQUIVO.replace("%g", "0"));
    }

    /** Liga a gravação em arquivo. Chamado uma vez, ao abrir o sistema. */
    public static synchronized void configurar() {
        if (arquivo != null) {
            return;
        }
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT  %4$-7s  %2$s%n  %5$s%6$s%n");
        try {
            Files.createDirectories(getPasta());
            arquivo = new FileHandler(getPasta().resolve(ARQUIVO).toString(), 1024 * 1024, 5, true);
            arquivo.setEncoding("UTF-8");
            arquivo.setFormatter(new SimpleFormatter());
            arquivo.setLevel(Level.INFO);
            Logger raiz = Logger.getLogger("");
            raiz.addHandler(arquivo);
            Logger.getLogger(Log.class.getName()).info("Sistema iniciado.");
        } catch (IOException e) {
            Logger.getLogger(Log.class.getName()).log(Level.WARNING,
                    "Não foi possível criar o arquivo de log em " + getPasta(), e);
        }
    }

    /** Desliga a gravação em arquivo (usado nos testes). */
    static synchronized void encerrar() {
        if (arquivo != null) {
            Logger.getLogger("").removeHandler(arquivo);
            arquivo.close();
            arquivo = null;
        }
    }
}
