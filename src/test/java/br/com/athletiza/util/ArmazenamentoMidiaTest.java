package br.com.athletiza.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArmazenamentoMidiaTest {

    @Test
    void nomeSeguroRemoveAcentosEspacosECaracteresEspeciais() {
        assertEquals("festa-atletica-2025.jpg", ArmazenamentoMidia.nomeSeguro("Festa Atlética 2025.JPG"));
        assertEquals("acao-social-copia-1-.mp4", ArmazenamentoMidia.nomeSeguro("Ação Social (cópia 1).mp4"));
    }
}
