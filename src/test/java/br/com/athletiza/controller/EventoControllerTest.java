package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Pessoa;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.TipoEvento;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

    private final EventoController controller = new EventoController();
    private final MembroController membros = new MembroController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void filtraPorTipoEPeriodoEmOrdemDeData() throws Exception {
        assertEquals(List.of("Festa Atlética", "Ação Social", "Desafio das Atléticas", "Workshop"),
                titulos(controller.pesquisar(null, null, null)));
        assertEquals(List.of("Festa Atlética"), titulos(controller.pesquisar(TipoEvento.FESTA, null, null)));
        assertEquals(List.of("Ação Social", "Desafio das Atléticas"),
                titulos(controller.pesquisar(null, LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 31))));
    }

    @Test
    void consultaTrazTarefasParaIndicarAtrasos() throws Exception {
        Evento festa = festa();

        assertEquals(3, festa.getTarefas().size());
        assertEquals(List.of("Contratar DJ"), festa.getTarefasAtrasadas(HOJE).stream().map(Tarefa::getDescricao).toList());
    }

    @Test
    void validaCadastroDeEvento() {
        Evento invalido = new Evento(" ", null, null, null, null);
        invalido.setSituacao(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(invalido));
        assertEquals(4, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiEventoQueApareceNaConsulta() throws Exception {
        Evento plantio = new Evento("Plantio de árvores", TipoEvento.ACAO_AMBIENTAL, LocalDate.of(2026, 11, 22), null, "Parque");
        plantio.setSituacao(SituacaoEvento.CONFIRMADO);

        controller.salvar(plantio);

        assertEquals(List.of("Plantio de árvores"), titulos(controller.pesquisar(TipoEvento.ACAO_AMBIENTAL, null, null)));
    }

    @Test
    void gerenciaResponsaveisSemRepeticao() throws Exception {
        Evento festa = festaComDetalhes();
        Membro mariana = membro("Mariana Rocha");
        assertEquals(List.of("Paula Ribeiro", "Rafael Teixeira"), nomes(festa));

        controller.adicionarResponsavel(festa, mariana);
        assertThrows(RegraNegocioException.class, () -> controller.adicionarResponsavel(festa, mariana));
        assertEquals(3, festaComDetalhes().getResponsaveis().size());

        controller.removerResponsavel(festa, mariana);
        assertEquals(2, festaComDetalhes().getResponsaveis().size());
    }

    @Test
    void incluiConcluiEExcluiTarefa() throws Exception {
        Evento festa = festaComDetalhes();
        Tarefa decoracao = new Tarefa("Comprar decoração", membro("Rafael Teixeira"), LocalDate.of(2026, 10, 10));

        controller.salvarTarefa(festa, decoracao);
        assertEquals(4, festaComDetalhes().getTarefas().size());

        Tarefa dj = festaComDetalhes().getTarefas().stream()
                .filter(t -> t.getDescricao().equals("Contratar DJ")).findFirst().orElseThrow();
        dj.setSituacao(SituacaoTarefa.CONCLUIDA);
        controller.salvarTarefa(festa, dj);
        assertTrue(festaComDetalhes().getTarefasAtrasadas(HOJE).isEmpty());

        controller.excluirTarefa(decoracao);
        assertEquals(3, festaComDetalhes().getTarefas().size());
        assertThrows(ValidacaoException.class, () -> controller.salvarTarefa(festa, new Tarefa(" ", null, null)));
    }

    @Test
    void excluiEventoComResponsaveisETarefas() throws Exception {
        controller.excluir(festa());

        assertEquals(3, controller.pesquisar(null, null, null).size());
    }

    private Evento festa() throws Exception {
        return controller.pesquisar(TipoEvento.FESTA, null, null).get(0);
    }

    private Evento festaComDetalhes() throws Exception {
        Evento festa = festa();
        Evento detalhado = new Evento(festa.getTitulo(), festa.getTipoEvento(), festa.getData(), festa.getHorario(), festa.getLocal());
        detalhado.setId(festa.getId());
        controller.carregarDetalhes(detalhado);
        return detalhado;
    }

    private Membro membro(String nome) throws Exception {
        return membros.listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> nomes(Evento evento) {
        return evento.getResponsaveis().stream().map(Pessoa::getNome).sorted().toList();
    }

    private static List<String> titulos(List<Evento> eventos) {
        return eventos.stream().map(Evento::getTitulo).toList();
    }
}
