package br.com.athletiza.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidadorTest {

    @Test
    void acumulaTodosOsErros() {
        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> new Validador()
                .obrigatorio("  ", "Nome")
                .obrigatorio((Object) null, "Modalidade")
                .apenasNumeros("12a", "Matrícula")
                .periodo(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 1), "Data de início", "Data de fim")
                .validar());

        assertEquals(4, erro.getErros().size());
        assertEquals("O campo \"Nome\" é obrigatório.", erro.getErros().get(0));
    }

    @Test
    void dadosValidosNaoGeramErro() {
        assertDoesNotThrow(() -> new Validador()
                .obrigatorio("Ana Souza", "Nome")
                .apenasNumeros("202001", "Matrícula")
                .contato("(47) 99999-1001", "Contato")
                .contato("ana@email.com", "Contato")
                .contato("", "Contato")
                .validar());
    }

    @Test
    void contatoInvalido() {
        assertThrows(ValidacaoException.class, () -> new Validador().contato("abc", "Contato").validar());
    }

    @Test
    void converteDataNoFormatoBrasileiro() throws Exception {
        assertEquals(LocalDate.of(2026, 10, 30), Validador.converterData("30/10/2026", "Data"));
        assertNull(Validador.converterData("", "Data"));
    }

    @Test
    void rejeitaDataInexistente() {
        assertThrows(ValidacaoException.class, () -> Validador.converterData("31/02/2026", "Data"));
        assertThrows(ValidacaoException.class, () -> Validador.converterData("2026-10-30", "Data"));
    }
}
