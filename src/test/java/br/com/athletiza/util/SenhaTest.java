package br.com.athletiza.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhaTest {

    @Test
    void confereSenhaCorretaERejeitaErrada() {
        String hash = Senha.gerarHash("segredo".toCharArray());

        assertTrue(Senha.conferir("segredo".toCharArray(), hash));
        assertFalse(Senha.conferir("Segredo".toCharArray(), hash));
    }

    @Test
    void hashNaoContemASenhaEMudaACadaGeracao() {
        String hash1 = Senha.gerarHash("segredo".toCharArray());
        String hash2 = Senha.gerarHash("segredo".toCharArray());

        assertFalse(hash1.contains("segredo"));
        assertNotEquals(hash1, hash2);
    }

    @Test
    void rejeitaHashInvalido() {
        assertFalse(Senha.conferir("x".toCharArray(), "texto-puro"));
        assertFalse(Senha.conferir("x".toCharArray(), null));
    }
}
