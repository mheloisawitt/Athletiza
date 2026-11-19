package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Genero;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Pessoa;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.modelo.SituacaoAtleta;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtletaControladorTest {
    private final AtletaControlador controlador = new AtletaControlador(new AtletaDAO());
    private final ModalidadeControlador modalidades = new ModalidadeControlador(new ModalidadeDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaAtletasEmOrdemAlfabeticaComModalidades() throws Exception {
        List<Atleta> atletas = controlador.listar();

        assertEquals(7, atletas.size());
        assertEquals("Ana Souza", atletas.get(0).getNome());
        Atleta beatriz = atletas.stream().filter(a -> a.getNome().equals("Beatriz Lima")).findFirst().orElseThrow();
        assertEquals(List.of("Futsal (F)", "Vôlei (F)"), beatriz.getModalidades().stream().map(Modalidade::toString).toList());
    }

    @Test
    void filtraPorModalidadeTextoESituacao() throws Exception {
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);

        assertEquals(List.of("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                nomes(controlador.pesquisar(null, futsalF, null, null)));
        assertEquals(List.of("Carla Oliveira", "Carlos Mendes"), nomes(controlador.pesquisar("carl", null, null, null)));
        assertEquals(List.of("Ana Souza"), nomes(controlador.pesquisar("202001", null, Situacao.ATIVO, null)));
        assertTrue(controlador.pesquisar(null, null, Situacao.INATIVO, null).isEmpty());
    }

    @Test
    void ordenaComOComparatorEscolhido() throws Exception {
        List<Atleta> porMatricula = controlador.pesquisar(null, null, null, Atleta.POR_MATRICULA.reversed());

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

        controlador.salvar(novo);
        Atleta lido = buscar("Gabriel Nunes");
        assertEquals(2, lido.getModalidades().size());
        assertEquals(SituacaoAtleta.LESIONADO, lido.getSituacaoNaModalidade(handebolM).orElseThrow());

        lido.desvincularModalidade(handebolM);
        lido.setSituacao(Situacao.INATIVO);
        controlador.salvar(lido);
        Atleta alterado = buscar("Gabriel Nunes");
        assertEquals(List.of(basqueteM), List.copyOf(alterado.getModalidades()));
        assertEquals(Situacao.INATIVO, alterado.getSituacao());
    }

    @Test
    void validaCamposObrigatoriosEModalidade() {
        Atleta invalido = new Atleta(" ", "12a", "contato inválido");

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controlador.salvar(invalido));

        assertEquals(4, erro.getErros().size(), erro.getMessage());
        assertTrue(erro.getErros().contains("Selecione ao menos uma modalidade."));
    }

    @Test
    void naoPermiteMatriculaRepetida() throws Exception {
        Atleta repetido = new Atleta("Outra Ana", "202001", null);
        repetido.vincularModalidade(modalidade("Futsal", Genero.FEMININO), SituacaoAtleta.ATIVO);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controlador.salvar(repetido));
        assertEquals("Já existe um atleta com a matrícula 202001.", erro.getMessage());
    }

    @Test
    void naoExcluiAtletaComPresencasEInscricoesEExplicaOMotivo() throws Exception {
        Atleta ana = buscar("Ana Souza");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controlador.excluir(ana));

        assertTrue(erro.getMessage().contains("1 registro(s) de presença em treinos"), erro.getMessage());
        assertTrue(erro.getMessage().contains("inscrição(ões) em competições"), erro.getMessage());
    }

    @Test
    void excluiAtletaSemVinculosJuntoComSuasModalidades() throws Exception {
        Atleta novo = new Atleta("Temporário", "999999", null);
        novo.vincularModalidade(modalidade("Basquete", Genero.FEMININO), SituacaoAtleta.ATIVO);
        controlador.salvar(novo);

        controlador.excluir(novo);

        assertEquals(7, controlador.listar().size());
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
        return controlador.listar().stream().filter(a -> a.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> nomes(List<Atleta> atletas) {
        return atletas.stream().map(Pessoa::getNome).toList();
    }
}
