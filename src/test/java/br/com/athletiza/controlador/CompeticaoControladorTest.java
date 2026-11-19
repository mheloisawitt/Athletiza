package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Genero;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Pessoa;
import br.com.athletiza.modelo.Resultado;
import br.com.athletiza.modelo.SituacaoCompeticao;
import br.com.athletiza.modelo.SituacaoTarefa;
import br.com.athletiza.modelo.Tarefa;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompeticaoControladorTest {
    private final CompeticaoControlador controlador = new CompeticaoControlador(new CompeticaoDAO());
    private final AtletaControlador atletas = new AtletaControlador(new AtletaDAO());
    private final ModalidadeControlador modalidades = new ModalidadeControlador(new ModalidadeDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaPorDataComFiltrosDeSituacaoEPeriodo() throws Exception {
        assertEquals(List.of("JIUDESC 2026", "Campeonato Regional", "Copa Alto Vale", "Torneio Interno"),
                titulos(controlador.pesquisar(null, null, null)));
        assertEquals(List.of("Copa Alto Vale", "Torneio Interno"),
                titulos(controlador.pesquisar(SituacaoCompeticao.PLANEJADA, null, null)));
        assertEquals(List.of("JIUDESC 2026", "Campeonato Regional"),
                titulos(controlador.pesquisar(null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))));
    }

    @Test
    void validaDatasDaCompeticao() {
        Competicao invalida = new Competicao("Copa", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 1), null, "Ibirama");

        assertThrows(ValidacaoException.class, () -> controlador.salvar(invalida));
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
        eduardo.vincularModalidade(basqueteM, br.com.athletiza.modelo.SituacaoAtleta.ATIVO);
        atletas.salvar(eduardo);

        jiudesc.removerModalidade(voleiM);
        jiudesc.adicionarModalidade(basqueteM);
        jiudesc.inscreverAtleta(basqueteM, eduardo);
        controlador.salvarInscricoes(jiudesc);

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

        controlador.salvarResultado(jiudesc, new Resultado(futsalF, null, 2, "3 x 1 na semifinal"));
        controlador.salvarResultado(jiudesc, new Resultado(atletismoM, atleta("Carlos Mendes"), 1, null));

        assertEquals(2, controlador.listarResultados(jiudesc).size());
        Competicao naConsulta = controlador.pesquisar(null, null, null).get(0);
        assertEquals("1º - Atletismo (M)", naConsulta.getMelhorResultado());
    }

    @Test
    void resultadoExigeModalidadeEAtletaInscritosEColocacaoOuPlacar() throws Exception {
        Competicao jiudesc = jiudesc();
        Modalidade basqueteF = modalidade("Basquete", Genero.FEMININO);
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);

        assertThrows(RegraNegocioException.class, () -> controlador.salvarResultado(jiudesc, new Resultado(basqueteF, null, 1, null)));
        assertThrows(RegraNegocioException.class,
                () -> controlador.salvarResultado(jiudesc, new Resultado(futsalF, atleta("Carlos Mendes"), 1, null)));
        assertThrows(ValidacaoException.class, () -> controlador.salvarResultado(jiudesc, new Resultado(futsalF, null, null, " ")));
    }

    @Test
    void competicaoTemTarefasComAtrasoEExibeNaConsulta() throws Exception {
        Competicao jiudesc = jiudesc();
        Tarefa inscricao = new Tarefa("Enviar ficha de inscrição", null, LocalDate.of(2026, 10, 1));
        Tarefa onibus = new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20));

        controlador.salvarTarefa(jiudesc, inscricao);
        controlador.salvarTarefa(jiudesc, onibus);
        assertThrows(ValidacaoException.class, () -> controlador.salvarTarefa(jiudesc, new Tarefa(" ", null, null)));

        assertEquals(List.of("Enviar ficha de inscrição", "Reservar ônibus"),
                controlador.listarTarefas(jiudesc).stream().map(Tarefa::getDescricao).toList());
        Competicao naConsulta = controlador.pesquisar(null, null, null).get(0);
        assertEquals(List.of("Enviar ficha de inscrição"),
                naConsulta.getTarefasAtrasadas(LocalDate.of(2026, 10, 7)).stream().map(Tarefa::getDescricao).toList());

        inscricao.setSituacao(SituacaoTarefa.CONCLUIDA);
        controlador.salvarTarefa(jiudesc, inscricao);
        controlador.excluirTarefa(onibus);
        assertEquals(1, controlador.listarTarefas(jiudesc).size());
        assertTrue(controlador.pesquisar(null, null, null).get(0).getTarefasAtrasadas(LocalDate.of(2026, 10, 7)).isEmpty());
    }

    @Test
    void tarefasDeCompeticaoNaoAparecemNosEventos() throws Exception {
        controlador.salvarTarefa(jiudesc(), new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20)));

        int tarefasNosEventos = new EventoControlador(new EventoDAO()).pesquisar(null, null, null).stream()
                .mapToInt(e -> e.getTarefas().size()).sum();
        assertEquals(3, tarefasNosEventos);
    }

    @Test
    void excluiCompeticaoComInscricoesEResultados() throws Exception {
        Competicao jiudesc = jiudesc();
        controlador.salvarResultado(jiudesc, new Resultado(modalidade("Futsal", Genero.FEMININO), null, 1, null));

        controlador.excluir(jiudesc);

        assertEquals(3, controlador.pesquisar(null, null, null).size());
    }

    private Competicao jiudesc() throws Exception {
        Competicao jiudesc = controlador.pesquisar(null, null, null).get(0);
        controlador.carregarInscricoes(jiudesc);
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
