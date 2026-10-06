package br.com.athletiza.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModalidadeDAOTest {

    private final ModalidadeDAO dao = new ModalidadeDAO();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaModalidadesDosDadosDeExemploEmOrdem() throws Exception {
        List<Modalidade> modalidades = dao.listarTodos();

        assertEquals(10, modalidades.size());
        assertEquals("Atletismo (F)", modalidades.get(0).toString());
    }

    @Test
    void inserePreenchendoIdEBuscaPorId() throws Exception {
        Modalidade xadrez = new Modalidade("Xadrez", Genero.MISTO);

        dao.inserir(xadrez);

        assertNotNull(xadrez.getId());
        Modalidade lida = dao.buscarPorId(xadrez.getId()).orElseThrow();
        assertEquals(xadrez, lida);
        assertEquals(Genero.MISTO, lida.getGenero());
    }

    @Test
    void atualizaModalidade() throws Exception {
        Modalidade xadrez = new Modalidade("Xadrez", Genero.MISTO);
        dao.inserir(xadrez);

        xadrez.setNome("Xadrez Rápido");
        dao.atualizar(xadrez);

        assertEquals("Xadrez Rápido", dao.buscarPorId(xadrez.getId()).orElseThrow().getNome());
    }

    @Test
    void naoPermiteModalidadeDuplicada() {
        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> dao.inserir(new Modalidade("Futsal", Genero.FEMININO)));

        assertTrue(erro.getMessage().contains("já existe"), erro.getMessage());
    }

    @Test
    void naoExcluiModalidadeComVinculosEExplicaOMotivo() throws Exception {
        Modalidade futsalFeminino = buscar("Futsal", Genero.FEMININO);

        assertTrue(dao.possuiVinculos(futsalFeminino.getId()));
        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> dao.excluir(futsalFeminino.getId()));
        assertEquals("Não é possível excluir a modalidade: existem outros registros vinculados.", erro.getMessage());
    }

    @Test
    void excluiModalidadeSemVinculos() throws Exception {
        Modalidade basquete = buscar("Basquete", Genero.MASCULINO);

        assertFalse(dao.possuiVinculos(basquete.getId()));
        dao.excluir(basquete.getId());

        assertTrue(dao.buscarPorId(basquete.getId()).isEmpty());
    }

    private Modalidade buscar(String nome, Genero genero) throws PersistenciaException {
        return dao.listarTodos().stream()
                .filter(m -> m.equals(new Modalidade(nome, genero)))
                .findFirst().orElseThrow();
    }
}
