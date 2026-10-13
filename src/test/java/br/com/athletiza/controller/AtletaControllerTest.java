package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.view.atletas.PainelCadastroAtleta;
import br.com.athletiza.view.atletas.PainelCadastroModalidade;
import br.com.athletiza.view.atletas.PainelConsultaAtletas;
import br.com.athletiza.view.atletas.PainelConsultaModalidades;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes do módulo de atletas: consulta, cadastro e modalidades.
 * Os controllers recebem telas de verdade (sem exibi-las) e os DAOs ligados ao banco de teste.
 */
class AtletaControllerTest {

    private final NavegadorFalso navegador = new NavegadorFalso();
    private ConsultaAtletasController consulta;

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        consulta = new ConsultaAtletasController(new PainelConsultaAtletas(), new AtletaDAO(), new ModalidadeDAO(),
                navegador);
    }

    /** Cadastro com a tela preenchida a partir do atleta (como se o usuário tivesse digitado esses dados). */
    private CadastroAtletaController cadastro(Atleta atleta) {
        return new CadastroAtletaController(new PainelCadastroAtleta(), atleta, new AtletaDAO(), new ModalidadeDAO(),
                navegador);
    }

    @Test
    void listaAtletasEmOrdemAlfabeticaComModalidades() throws Exception {
        List<Atleta> atletas = consulta.pesquisar(null, null, null, null);

        assertEquals(7, atletas.size());
        assertEquals("Ana Souza", atletas.get(0).getNome());
        assertEquals("Futsal (F), Vôlei (F)", Dados.atleta("Beatriz Lima").getModalidadesTexto());
    }

    @Test
    void filtraPorModalidadeTextoESituacao() throws Exception {
        Modalidade futsalF = Dados.modalidade("Futsal", Genero.FEMININO);

        assertEquals(Arrays.asList("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                nomes(consulta.pesquisar(null, futsalF, null, null)));
        assertEquals(Arrays.asList("Carla Oliveira", "Carlos Mendes"), nomes(consulta.pesquisar("carl", null, null, null)));
        assertEquals(Arrays.asList("Ana Souza"), nomes(consulta.pesquisar("202001", null, Situacao.ATIVO, null)));
        assertTrue(consulta.pesquisar(null, null, Situacao.INATIVO, null).isEmpty());
    }

    @Test
    void ordenaComOComparatorEscolhido() throws Exception {
        List<Atleta> porMatricula = consulta.pesquisar(null, null, null, Collections.reverseOrder(Atleta.POR_MATRICULA));

        assertEquals("Carla Oliveira", porMatricula.get(0).getNome());
    }

    @Test
    void incluiAtletaComModalidadesEAlteraOsVinculos() throws Exception {
        Modalidade basqueteM = Dados.modalidade("Basquete", Genero.MASCULINO);
        Modalidade handebolM = Dados.modalidade("Handebol", Genero.MASCULINO);
        Atleta novo = new Atleta("Gabriel Nunes", "202201", "(47) 99999-3000");
        novo.setDataNascimento(LocalDate.of(2004, 6, 1));
        novo.vincularModalidade(basqueteM, SituacaoAtleta.ATIVO);
        novo.vincularModalidade(handebolM, SituacaoAtleta.LESIONADO);

        cadastro(novo).gravar();
        Atleta lido = Dados.atleta("Gabriel Nunes");
        assertEquals(2, lido.getModalidades().size());
        assertEquals(SituacaoAtleta.LESIONADO, lido.getSituacaoNaModalidade(handebolM));

        lido.desvincularModalidade(handebolM);
        lido.setSituacao(Situacao.INATIVO);
        cadastro(lido).gravar();
        Atleta alterado = Dados.atleta("Gabriel Nunes");
        assertEquals(Arrays.asList(basqueteM), new ArrayList<>(alterado.getModalidades()));
        assertEquals(Situacao.INATIVO, alterado.getSituacao());
    }

    @Test
    void marcarModalidadeNaTelaVinculaOAtleta() throws Exception {
        Modalidade voleiF = Dados.modalidade("Vôlei", Genero.FEMININO);
        Atleta novo = new Atleta("Helena Prado", "202301", null);
        CadastroAtletaController controller = cadastro(novo);

        controller.getView().marcarModalidade(voleiF, SituacaoAtleta.AFASTADO);
        controller.gravar();

        assertEquals(SituacaoAtleta.AFASTADO, Dados.atleta("Helena Prado").getSituacaoNaModalidade(voleiF));
    }

    @Test
    void validaCamposObrigatoriosEModalidade() {
        Atleta invalido = new Atleta(" ", "12a", "contato inválido");

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> cadastro(invalido).gravar());

        assertEquals(4, erro.getErros().size(), erro.getMessage());
        assertTrue(erro.getErros().contains("Selecione ao menos uma modalidade."));
    }

    @Test
    void naoPermiteMatriculaRepetida() throws Exception {
        Atleta repetido = new Atleta("Outra Ana", "202001", null);
        repetido.vincularModalidade(Dados.modalidade("Futsal", Genero.FEMININO), SituacaoAtleta.ATIVO);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> cadastro(repetido).gravar());
        assertEquals("Já existe um atleta com a matrícula 202001.", erro.getMessage());
    }

    @Test
    void naoExcluiAtletaComPresencasEInscricoesEExplicaOMotivo() throws Exception {
        Atleta ana = Dados.atleta("Ana Souza");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> consulta.excluir(ana));

        assertTrue(erro.getMessage().contains("1 registro(s) de presença em treinos"), erro.getMessage());
        assertTrue(erro.getMessage().contains("inscrição(ões) em competições"), erro.getMessage());
    }

    @Test
    void excluiAtletaSemVinculosJuntoComSuasModalidades() throws Exception {
        Atleta novo = new Atleta("Temporário", "999999", null);
        novo.vincularModalidade(Dados.modalidade("Basquete", Genero.FEMININO), SituacaoAtleta.ATIVO);
        cadastro(novo).gravar();

        consulta.excluir(Dados.atleta("Temporário"));

        assertEquals(7, consulta.pesquisar(null, null, null, null).size());
    }

    @Test
    void modalidadeRepetidaOuComVinculosNaoPodeSerSalvaOuExcluida() throws Exception {
        CadastroModalidadeController cadastroRepetido = new CadastroModalidadeController(new PainelCadastroModalidade(),
                new Modalidade("futsal", Genero.FEMININO), new ModalidadeDAO(), navegador);
        ConsultaModalidadesController modalidades = new ConsultaModalidadesController(new PainelConsultaModalidades(),
                new ModalidadeDAO(), navegador);

        assertThrows(RegraNegocioException.class, () -> cadastroRepetido.gravar());
        assertThrows(RegraNegocioException.class,
                () -> modalidades.excluir(Dados.modalidade("Futsal", Genero.FEMININO)));

        modalidades.excluir(Dados.modalidade("Basquete", Genero.MASCULINO));
        assertEquals(9, new ModalidadeDAO().listarTodos().size());
    }

    private static List<String> nomes(List<Atleta> atletas) {
        return Dados.textos(atletas, a -> a.getNome());
    }
}
