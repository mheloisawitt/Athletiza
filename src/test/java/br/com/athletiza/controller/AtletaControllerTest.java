package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Pessoa;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtletaControllerTest {

    private final AtletaController controller = new AtletaController();
    private final ModalidadeController modalidades = new ModalidadeController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaAtletasEmOrdemAlfabeticaComModalidades() throws Exception {
        List<Atleta> atletas = controller.listar();

        assertEquals(7, atletas.size());
        assertEquals("Ana Souza", atletas.get(0).getNome());
        Atleta beatriz = atletas.stream().filter(a -> a.getNome().equals("Beatriz Lima")).findFirst().orElseThrow();
        assertEquals(List.of("Futsal (F)", "Vôlei (F)"), beatriz.getModalidades().stream().map(Modalidade::toString).toList());
    }

    @Test
    void filtraPorModalidadeTextoESituacao() throws Exception {
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);

        assertEquals(List.of("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                nomes(controller.pesquisar(null, futsalF, null, null)));
        assertEquals(List.of("Carla Oliveira", "Carlos Mendes"), nomes(controller.pesquisar("carl", null, null, null)));
        assertEquals(List.of("Ana Souza"), nomes(controller.pesquisar("202001", null, Situacao.ATIVO, null)));
        assertTrue(controller.pesquisar(null, null, Situacao.INATIVO, null).isEmpty());
    }

    @Test
    void ordenaComOComparatorEscolhido() throws Exception {
        List<Atleta> porMatricula = controller.pesquisar(null, null, null, Atleta.POR_MATRICULA.reversed());

        assertEquals("Carla Oliveira", porMatricula.get(0).getNome());
    }

    @Test
    void incluiAtletaComModalidadesEAlteraOsVinculos() throws Exception {
        Modalidade basqueteM = modalidade("Basquete", Genero.MASCULINO);
        Modalidade handebolM = modalidade("Handebol", Genero.MASCULINO);
        Atleta novo = new Atleta("Gabriel Nunes", "202201", "(47) 99999-3000");
        novo.setDataNascimento(LocalDate.of(2004, 6, 1));
        novo.vincularModalidade(basqueteM, SituacaoAtleta.ATIVO);
        novo.vincularModalidade(handebolM, SituacaoAtleta.LESIONADO);

        controller.salvar(novo);
        Atleta lido = buscar("Gabriel Nunes");
        assertEquals(2, lido.getModalidades().size());
        assertEquals(SituacaoAtleta.LESIONADO, lido.getSituacaoNaModalidade(handebolM).orElseThrow());

        lido.desvincularModalidade(handebolM);
        lido.setSituacao(Situacao.INATIVO);
        controller.salvar(lido);
        Atleta alterado = buscar("Gabriel Nunes");
        assertEquals(List.of(basqueteM), List.copyOf(alterado.getModalidades()));
        assertEquals(Situacao.INATIVO, alterado.getSituacao());
    }

    @Test
    void validaCamposObrigatoriosEModalidade() {
        Atleta invalido = new Atleta(" ", "12a", "contato inválido");

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(invalido));

        assertEquals(4, erro.getErros().size(), erro.getMessage());
        assertTrue(erro.getErros().contains("Selecione ao menos uma modalidade."));
    }

    @Test
    void naoPermiteMatriculaRepetida() throws Exception {
        Atleta repetido = new Atleta("Outra Ana", "202001", null);
        repetido.vincularModalidade(modalidade("Futsal", Genero.FEMININO), SituacaoAtleta.ATIVO);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controller.salvar(repetido));
        assertEquals("Já existe um atleta com a matrícula 202001.", erro.getMessage());
    }

    @Test
    void naoExcluiAtletaComPresencasEInscricoesEExplicaOMotivo() throws Exception {
        Atleta ana = buscar("Ana Souza");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controller.excluir(ana));

        assertTrue(erro.getMessage().contains("1 registro(s) de presença em treinos"), erro.getMessage());
        assertTrue(erro.getMessage().contains("inscrição(ões) em competições"), erro.getMessage());
    }

    @Test
    void excluiAtletaSemVinculosJuntoComSuasModalidades() throws Exception {
        Atleta novo = new Atleta("Temporário", "999999", null);
        novo.vincularModalidade(modalidade("Basquete", Genero.FEMININO), SituacaoAtleta.ATIVO);
        controller.salvar(novo);

        controller.excluir(novo);

        assertEquals(7, controller.listar().size());
    }

    @Test
    void modalidadeRepetidaOuComVinculosNaoPodeSerSalvaOuExcluida() throws Exception {
        assertThrows(RegraNegocioException.class, () -> modalidades.salvar(new Modalidade("futsal", Genero.FEMININO)));
        assertThrows(RegraNegocioException.class, () -> modalidades.excluir(modalidade("Futsal", Genero.FEMININO)));

        modalidades.excluir(modalidade("Basquete", Genero.MASCULINO));
        assertEquals(9, modalidades.listar().size());
    }

    private Modalidade modalidade(String nome, Genero genero) throws Exception {
        return modalidades.listar().stream().filter(m -> m.equals(new Modalidade(nome, genero))).findFirst().orElseThrow();
    }

    private Atleta buscar(String nome) throws Exception {
        return controller.listar().stream().filter(a -> a.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> nomes(List<Atleta> atletas) {
        return atletas.stream().map(Pessoa::getNome).toList();
    }
}
