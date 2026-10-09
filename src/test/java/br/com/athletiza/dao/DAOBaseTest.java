package br.com.athletiza.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.athletiza.dao.DAOBase.Violacao;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class DAOBaseTest {

    @Test
    void reconheceOsCodigosDeErroDoMySql() {
        assertEquals(Violacao.UNICIDADE, DAOBase.violacao(new SQLException("Duplicate entry", "23000", 1062)));
        assertEquals(Violacao.CHAVE_ESTRANGEIRA, DAOBase.violacao(new SQLException("Cannot delete", "23000", 1451)));
        assertEquals(Violacao.CHAVE_ESTRANGEIRA, DAOBase.violacao(new SQLException("Cannot add", "23000", 1452)));
        assertEquals(Violacao.CHECK, DAOBase.violacao(new SQLException("Check violated", "HY000", 3819)));
    }

    @Test
    void reconheceOsSqlStatesPadraoDoH2() {
        assertEquals(Violacao.UNICIDADE, DAOBase.violacao(new SQLException("x", "23505", 23505)));
        assertEquals(Violacao.CHAVE_ESTRANGEIRA, DAOBase.violacao(new SQLException("x", "23503", 23503)));
        assertEquals(Violacao.CHECK, DAOBase.violacao(new SQLException("x", "23513", 23513)));
        assertEquals(Violacao.OUTRA, DAOBase.violacao(new SQLException("x", "08001", 0)));
    }
}
