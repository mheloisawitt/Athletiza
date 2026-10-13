package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.TarefaDAO;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.view.componentes.PainelTarefas;
import br.com.athletiza.view.eventos.PainelCadastroEvento;
import br.com.athletiza.view.eventos.PainelConsultaEventos;
import br.com.athletiza.view.eventos.PainelGerenciarEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

    private final NavegadorFalso navegador = new NavegadorFalso();
    private final EventoDAO dao = new EventoDAO();
    private ConsultaEventosController consulta;

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        consulta = new ConsultaEventosController(new PainelConsultaEventos(), dao, new TarefaDAO(), new MembroDAO(),
                navegador);
    }

    @Test
    void filtraPorTipoEPeriodoEmOrdemDeData() throws Exception {
        assertEquals(Arrays.asList("Festa Atlética", "Ação Social", "Desafio das Atléticas", "Workshop"),
                titulos(consulta.pesquisar(null, null, null)));
        assertEquals(Arrays.asList("Festa Atlética"), titulos(consulta.pesquisar(TipoEvento.FESTA, null, null)));
        assertEquals(Arrays.asList("Ação Social", "Desafio das Atléticas"),
                titulos(consulta.pesquisar(null, LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 31))));
    }

    @Test
    void consultaTrazTarefasParaIndicarAtrasos() throws Exception {
        Evento festa = Dados.evento("Festa Atlética");

        assertEquals(3, festa.getTarefas().size());
        assertEquals(Arrays.asList("Contratar DJ"), Dados.textos(festa.getTarefasAtrasadas(HOJE), t -> t.getDescricao()));
    }

    @Test
    void validaCadastroDeEvento() {
        Evento invalido = new Evento(" ", null, null, null, null);
        invalido.setSituacao(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> invalido.validar());
        assertEquals(4, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiEventoQueApareceNaConsulta() throws Exception {
        Evento plantio = new Evento("Plantio de árvores", TipoEvento.ACAO_AMBIENTAL, LocalDate.of(2026, 11, 22), null, "Parque");
        plantio.setSituacao(SituacaoEvento.CONFIRMADO);

        new CadastroEventoController(new PainelCadastroEvento(), plantio, dao, navegador).gravar();

        assertEquals(Arrays.asList("Plantio de árvores"), titulos(consulta.pesquisar(TipoEvento.ACAO_AMBIENTAL, null, null)));
    }

    @Test
    void gerenciaResponsaveisSemRepeticao() throws Exception {
        GerenciarEventoController gerenciar = gerenciarFesta();
        Membro mariana = Dados.membro("Mariana Rocha");
        assertEquals(Arrays.asList("Paula Ribeiro", "Rafael Teixeira"), responsaveis(gerenciar.getEvento()));

        gerenciar.adicionarResponsavel(mariana);
        assertThrows(RegraNegocioException.class, () -> gerenciar.adicionarResponsavel(mariana));
        assertEquals(3, festaComDetalhes().getResponsaveis().size());

        dao.removerResponsavel(gerenciar.getEvento(), mariana);
        assertEquals(2, festaComDetalhes().getResponsaveis().size());
    }

    @Test
    void incluiConcluiEExcluiTarefa() throws Exception {
        Evento festa = Dados.evento("Festa Atlética");
        TarefasController tarefas = new TarefasController(new PainelTarefas(), new TarefaDAO(), new MembroDAO(),
                TarefaRepository.Dono.EVENTO, festa.getId(), null);
        Tarefa decoracao = new Tarefa("Comprar decoração", Dados.membro("Rafael Teixeira"), LocalDate.of(2026, 10, 10));

        tarefas.salvar(decoracao);
        assertEquals(4, festaComDetalhes().getTarefas().size());

        Tarefa dj = null;
        for (Tarefa tarefa : festaComDetalhes().getTarefas()) {
            if (tarefa.getDescricao().equals("Contratar DJ")) {
                dj = tarefa;
            }
        }
        dj.setSituacao(SituacaoTarefa.CONCLUIDA);
        tarefas.salvar(dj);
        assertTrue(festaComDetalhes().getTarefasAtrasadas(HOJE).isEmpty());

        new TarefaDAO().excluir(decoracao);
        assertEquals(3, festaComDetalhes().getTarefas().size());
        assertThrows(ValidacaoException.class, () -> tarefas.salvar(new Tarefa(" ", null, null)));
    }

    @Test
    void excluiEventoComResponsaveisETarefas() throws Exception {
        dao.excluir(Dados.evento("Festa Atlética").getId());

        assertEquals(3, consulta.pesquisar(null, null, null).size());
    }

    /** Gerenciamento da festa com responsáveis e tarefas já lidos do banco (como ao abrir a tela). */
    private GerenciarEventoController gerenciarFesta() throws Exception {
        GerenciarEventoController gerenciar = new GerenciarEventoController(new PainelGerenciarEvento(),
                Dados.evento("Festa Atlética"), dao, new TarefaDAO(), new MembroDAO(), navegador);
        gerenciar.getView().carregar();
        return gerenciar;
    }

    private Evento festaComDetalhes() throws Exception {
        Evento festa = Dados.evento("Festa Atlética");
        Evento detalhado = new Evento(festa.getTitulo(), festa.getTipoEvento(), festa.getData(), festa.getHorario(),
                festa.getLocal());
        detalhado.setId(festa.getId());
        dao.carregarDetalhes(detalhado);
        return detalhado;
    }

    private static List<String> responsaveis(Evento evento) {
        List<String> nomes = Dados.textos(new ArrayList<>(evento.getResponsaveis()), m -> m.getNome());
        Collections.sort(nomes);
        return nomes;
    }

    private static List<String> titulos(List<Evento> eventos) {
        return Dados.textos(eventos, e -> e.getTitulo());
    }
}
