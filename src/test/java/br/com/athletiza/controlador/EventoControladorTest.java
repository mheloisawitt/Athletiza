package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Pessoa;
import br.com.athletiza.modelo.SituacaoEvento;
import br.com.athletiza.modelo.SituacaoTarefa;
import br.com.athletiza.modelo.Tarefa;
import br.com.athletiza.modelo.TipoEvento;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoControladorTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

    private final EventoControlador controlador = new EventoControlador(new EventoDAO());
    private final MembroControlador membros = new MembroControlador(new MembroDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void filtraPorTipoEPeriodoEmOrdemDeData() throws Exception {
        assertEquals(List.of("Festa Atlética", "Ação Social", "Desafio das Atléticas", "Workshop"),
                titulos(controlador.pesquisar(null, null, null)));
        assertEquals(List.of("Festa Atlética"), titulos(controlador.pesquisar(TipoEvento.FESTA, null, null)));
        assertEquals(List.of("Ação Social", "Desafio das Atléticas"),
                titulos(controlador.pesquisar(null, LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 31))));
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

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controlador.salvar(invalido));
        assertEquals(4, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiEventoQueApareceNaConsulta() throws Exception {
        Evento plantio = new Evento("Plantio de árvores", TipoEvento.ACAO_AMBIENTAL, LocalDate.of(2026, 11, 22), null, "Parque");
        plantio.setSituacao(SituacaoEvento.CONFIRMADO);

        controlador.salvar(plantio);

        assertEquals(List.of("Plantio de árvores"), titulos(controlador.pesquisar(TipoEvento.ACAO_AMBIENTAL, null, null)));
    }

    @Test
    void gerenciaResponsaveisSemRepeticao() throws Exception {
        Evento festa = festaComDetalhes();
        Membro mariana = membro("Mariana Rocha");
        assertEquals(List.of("Paula Ribeiro", "Rafael Teixeira"), nomes(festa));

        controlador.adicionarResponsavel(festa, mariana);
        assertThrows(RegraNegocioException.class, () -> controlador.adicionarResponsavel(festa, mariana));
        assertEquals(3, festaComDetalhes().getResponsaveis().size());

        controlador.removerResponsavel(festa, mariana);
        assertEquals(2, festaComDetalhes().getResponsaveis().size());
    }

    @Test
    void incluiConcluiEExcluiTarefa() throws Exception {
        Evento festa = festaComDetalhes();
        Tarefa decoracao = new Tarefa("Comprar decoração", membro("Rafael Teixeira"), LocalDate.of(2026, 10, 10));

        controlador.salvarTarefa(festa, decoracao);
        assertEquals(4, festaComDetalhes().getTarefas().size());

        Tarefa dj = festaComDetalhes().getTarefas().stream()
                .filter(t -> t.getDescricao().equals("Contratar DJ")).findFirst().orElseThrow();
        dj.setSituacao(SituacaoTarefa.CONCLUIDA);
        controlador.salvarTarefa(festa, dj);
        assertTrue(festaComDetalhes().getTarefasAtrasadas(HOJE).isEmpty());

        controlador.excluirTarefa(decoracao);
        assertEquals(3, festaComDetalhes().getTarefas().size());
        assertThrows(ValidacaoException.class, () -> controlador.salvarTarefa(festa, new Tarefa(" ", null, null)));
    }

    @Test
    void excluiEventoComResponsaveisETarefas() throws Exception {
        controlador.excluir(festa());

        assertEquals(3, controlador.pesquisar(null, null, null).size());
    }

    private Evento festa() throws Exception {
        return controlador.pesquisar(TipoEvento.FESTA, null, null).get(0);
    }

    private Evento festaComDetalhes() throws Exception {
        Evento festa = festa();
        Evento detalhado = new Evento(festa.getTitulo(), festa.getTipoEvento(), festa.getData(), festa.getHorario(), festa.getLocal());
        detalhado.setId(festa.getId());
        controlador.carregarDetalhes(detalhado);
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
