package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Pessoa;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompeticaoControllerTest {

    private final CompeticaoController controller = new CompeticaoController();
    private final AtletaController atletas = new AtletaController();
    private final ModalidadeController modalidades = new ModalidadeController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaPorDataComFiltrosDeSituacaoEPeriodo() throws Exception {
        assertEquals(List.of("JIUDESC 2026", "Campeonato Regional", "Copa Alto Vale", "Torneio Interno"),
                titulos(controller.pesquisar(null, null, null)));
        assertEquals(List.of("Copa Alto Vale", "Torneio Interno"),
                titulos(controller.pesquisar(SituacaoCompeticao.PLANEJADA, null, null)));
        assertEquals(List.of("JIUDESC 2026", "Campeonato Regional"),
                titulos(controller.pesquisar(null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))));
    }

    @Test
    void validaDatasDaCompeticao() {
        Competicao invalida = new Competicao("Copa", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 1), null, "Ibirama");

        assertThrows(ValidacaoException.class, () -> controller.salvar(invalida));
    }

    @Test
    void carregaInscricoesDaJiudesc() throws Exception {
        Competicao jiudesc = jiudesc();

        assertEquals(7, jiudesc.getModalidades().size());
        assertEquals(List.of("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                jiudesc.getAtletas(modalidade("Futsal", Genero.FEMININO)).stream().map(Pessoa::getNome).sorted().toList());
    }

    @Test
    void salvaInscricoesRemovendoEAdicionandoModalidades() throws Exception {
        Competicao jiudesc = jiudesc();
        Modalidade voleiM = modalidade("Vôlei", Genero.MASCULINO);
        Modalidade basqueteM = modalidade("Basquete", Genero.MASCULINO);
        Atleta eduardo = atleta("Eduardo Silva");
        eduardo.vincularModalidade(basqueteM, br.com.athletiza.model.SituacaoAtleta.ATIVO);
        atletas.salvar(eduardo);

        jiudesc.removerModalidade(voleiM);
        jiudesc.adicionarModalidade(basqueteM);
        jiudesc.inscreverAtleta(basqueteM, eduardo);
        controller.salvarInscricoes(jiudesc);

        Competicao relida = jiudesc();
        assertEquals(7, relida.getModalidades().size());
        assertTrue(relida.getModalidades().contains(basqueteM));
        assertEquals(List.of(eduardo), List.copyOf(relida.getAtletas(basqueteM)));
    }

    @Test
    void registraResultadosDeEquipeEIndividuaisEMostraNaConsulta() throws Exception {
        Competicao jiudesc = jiudesc();
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);
        Modalidade atletismoM = modalidade("Atletismo", Genero.MASCULINO);

        controller.salvarResultado(jiudesc, new Resultado(futsalF, null, 2, "3 x 1 na semifinal"));
        controller.salvarResultado(jiudesc, new Resultado(atletismoM, atleta("Carlos Mendes"), 1, null));

        assertEquals(2, controller.listarResultados(jiudesc).size());
        Competicao naConsulta = controller.pesquisar(null, null, null).get(0);
        assertEquals("1º - Atletismo (M)", naConsulta.getMelhorResultado());
    }

    @Test
    void resultadoExigeModalidadeEAtletaInscritosEColocacaoOuPlacar() throws Exception {
        Competicao jiudesc = jiudesc();
        Modalidade basqueteF = modalidade("Basquete", Genero.FEMININO);
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);

        assertThrows(RegraNegocioException.class, () -> controller.salvarResultado(jiudesc, new Resultado(basqueteF, null, 1, null)));
        assertThrows(RegraNegocioException.class,
                () -> controller.salvarResultado(jiudesc, new Resultado(futsalF, atleta("Carlos Mendes"), 1, null)));
        assertThrows(ValidacaoException.class, () -> controller.salvarResultado(jiudesc, new Resultado(futsalF, null, null, " ")));
    }

    @Test
    void competicaoTemTarefasComAtrasoEExibeNaConsulta() throws Exception {
        Competicao jiudesc = jiudesc();
        Tarefa inscricao = new Tarefa("Enviar ficha de inscrição", null, LocalDate.of(2026, 10, 1));
        Tarefa onibus = new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20));

        controller.salvarTarefa(jiudesc, inscricao);
        controller.salvarTarefa(jiudesc, onibus);
        assertThrows(ValidacaoException.class, () -> controller.salvarTarefa(jiudesc, new Tarefa(" ", null, null)));

        assertEquals(List.of("Enviar ficha de inscrição", "Reservar ônibus"),
                controller.listarTarefas(jiudesc).stream().map(Tarefa::getDescricao).toList());
        Competicao naConsulta = controller.pesquisar(null, null, null).get(0);
        assertEquals(List.of("Enviar ficha de inscrição"),
                naConsulta.getTarefasAtrasadas(LocalDate.of(2026, 10, 7)).stream().map(Tarefa::getDescricao).toList());

        inscricao.setSituacao(SituacaoTarefa.CONCLUIDA);
        controller.salvarTarefa(jiudesc, inscricao);
        controller.excluirTarefa(onibus);
        assertEquals(1, controller.listarTarefas(jiudesc).size());
        assertTrue(controller.pesquisar(null, null, null).get(0).getTarefasAtrasadas(LocalDate.of(2026, 10, 7)).isEmpty());
    }

    @Test
    void tarefasDeCompeticaoNaoAparecemNosEventos() throws Exception {
        controller.salvarTarefa(jiudesc(), new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20)));

        int tarefasNosEventos = new EventoController().pesquisar(null, null, null).stream()
                .mapToInt(e -> e.getTarefas().size()).sum();
        assertEquals(3, tarefasNosEventos);
    }

    @Test
    void excluiCompeticaoComInscricoesEResultados() throws Exception {
        Competicao jiudesc = jiudesc();
        controller.salvarResultado(jiudesc, new Resultado(modalidade("Futsal", Genero.FEMININO), null, 1, null));

        controller.excluir(jiudesc);

        assertEquals(3, controller.pesquisar(null, null, null).size());
    }

    private Competicao jiudesc() throws Exception {
        Competicao jiudesc = controller.pesquisar(null, null, null).get(0);
        controller.carregarInscricoes(jiudesc);
        return jiudesc;
    }

    private Modalidade modalidade(String nome, Genero genero) throws Exception {
        return modalidades.listar().stream().filter(m -> m.equals(new Modalidade(nome, genero))).findFirst().orElseThrow();
    }

    private Atleta atleta(String nome) throws Exception {
        return atletas.listar().stream().filter(a -> a.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> titulos(List<Competicao> competicoes) {
        return competicoes.stream().map(Competicao::getTitulo).toList();
    }
}
