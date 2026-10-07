package br.com.athletiza.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogTest {

    @TempDir
    Path temporaria;

    @AfterEach
    void encerrar() {
        Log.encerrar();
        System.clearProperty("athletiza.logs");
    }

    @Test
    void gravaErrosComStackTraceNoArquivo() throws Exception {
        System.setProperty("athletiza.logs", temporaria.toString());
        Log.configurar();

        Logger.getLogger("teste").log(Level.SEVERE, "Falha ao salvar o atleta", new IllegalStateException("detalhe técnico"));
        Log.encerrar();

        String conteudo = Files.readString(Log.getArquivoAtual(), StandardCharsets.UTF_8);
        assertTrue(conteudo.contains("Sistema iniciado."), conteudo);
        assertTrue(conteudo.contains("Falha ao salvar o atleta"), conteudo);
        assertTrue(conteudo.contains("IllegalStateException: detalhe técnico"), conteudo);
    }
}
