package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.atletas.PainelCadastroModalidade;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Exemplo de teste do Controller sem banco de dados e sem abrir janelas, como
 * nos slides de DAO/DIP e de MVC:
 * - o repositório é uma implementação falsa em memória (o controller só conhece a interface);
 * - a View é uma subclasse que guarda as mensagens em vez de mostrá-las.
 */
class CadastroModalidadeControllerTest {

    /** Repositório falso: guarda as modalidades em uma lista. */
    private static class ModalidadeRepositoryFake implements ModalidadeRepository {

        private final List<Modalidade> dados = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public void inserir(Modalidade modalidade) {
            modalidade.setId(proximoId++);
            dados.add(modalidade);
        }

        @Override
        public void atualizar(Modalidade modalidade) {
            // A lista guarda o próprio objeto: não há nada a fazer
        }

        @Override
        public void excluir(int id) {
            dados.remove(buscarPorId(id));
        }

        @Override
        public Modalidade buscarPorId(int id) {
            for (Modalidade modalidade : dados) {
                if (modalidade.getId() == id) {
                    return modalidade;
                }
            }
            return null;
        }

        @Override
        public List<Modalidade> listarTodos() {
            return new ArrayList<>(dados);
        }

        @Override
        public boolean possuiVinculos(int id) {
            return false;
        }
    }

    /** View falsa: guarda as mensagens e os erros em vez de abrir janelas. */
    private static class ViewFalsa extends PainelCadastroModalidade {

        final List<String> mensagens = new ArrayList<>();
        Exception erro;

        @Override
        public void mostrarMensagem(String mensagem) {
            mensagens.add(mensagem);
        }

        @Override
        public void mostrarErro(Exception e) {
            erro = e;
        }
    }

    private final ModalidadeRepositoryFake repository = new ModalidadeRepositoryFake();
    private final NavegadorFalso navegador = new NavegadorFalso();

    @Test
    void botaoSalvarGravaMostraMensagemEVoltaParaAConsulta() {
        ViewFalsa view = new ViewFalsa();
        new CadastroModalidadeController(view, new Modalidade("Xadrez", Genero.MISTO), repository, navegador);

        view.getBtnSalvar().doClick();

        assertEquals(1, repository.listarTodos().size());
        assertEquals("Xadrez (Misto)", repository.listarTodos().get(0).toString());
        assertEquals("Modalidade salva com sucesso.", view.mensagens.get(0));
        assertEquals(1, navegador.voltou);
    }

    @Test
    void erroDeValidacaoApareceNaTelaENaoVolta() {
        ViewFalsa view = new ViewFalsa();
        new CadastroModalidadeController(view, new Modalidade("   ", Genero.MISTO), repository, navegador);

        view.getBtnSalvar().doClick();

        assertInstanceOf(ValidacaoException.class, view.erro);
        assertTrue(view.erro.getMessage().contains("\"Nome\" é obrigatório"), view.erro.getMessage());
        assertTrue(repository.listarTodos().isEmpty());
        assertEquals(0, navegador.voltou);
    }

    @Test
    void modalidadeRepetidaERecusadaEOObjetoVoltaAoQueEra() {
        repository.inserir(new Modalidade("Xadrez", Genero.MISTO));
        Modalidade outra = new Modalidade("Damas", Genero.MISTO);
        repository.inserir(outra);
        ViewFalsa view = new ViewFalsa();
        CadastroModalidadeController controller = new CadastroModalidadeController(view, outra, repository, navegador);

        // O usuário troca o nome para um que já existe
        view.preencherCampos(new Modalidade("xadrez", Genero.MISTO));
        view.getBtnSalvar().doClick();

        assertInstanceOf(RegraNegocioException.class, view.erro);
        assertEquals("Damas", outra.getNome());
        assertEquals(controller.getView(), view);
    }
}
