package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoDemanda;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DemandaControllerTest {

    private final DemandaController controller = new DemandaController();
    private final MembroController membros = new MembroController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaPorPrioridadeEPrazoComRelacaoEResponsaveis() throws Exception {
        List<Demanda> demandas = controller.pesquisar(null, null);

        assertEquals(List.of("Arrecadar alimentos", "Reservar ônibus para os atletas", "Renovar o estoque de camisetas"),
                titulos(demandas));
        assertEquals("Evento · Ação Social (21/10/2026)", demandas.get(0).getDescricaoRelacionada());
        assertEquals("Mariana Rocha, Rafael Teixeira", demandas.get(0).getNomesResponsaveis());
        assertInstanceOf(Competicao.class, demandas.get(1).getRelacionada());
        assertNull(demandas.get(2).getRelacionada());
    }

    @Test
    void filtraPorSituacaoEPorResponsavel() throws Exception {
        assertEquals(List.of("Arrecadar alimentos"), titulos(controller.pesquisar(SituacaoDemanda.EM_ANDAMENTO, null)));
        assertEquals(List.of("Reservar ônibus para os atletas"),
                titulos(controller.pesquisar(null, membro("Juliana Martins"))));
        assertTrue(controller.pesquisar(SituacaoDemanda.CONCLUIDA, null).isEmpty());
    }

    @Test
    void validaCadastroDeDemanda() {
        Demanda invalida = new Demanda(" ", null, null);
        invalida.setSituacao(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(invalida));
        assertEquals(3, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiDemandaAvulsaEDepoisRelacionaATrocandoResponsaveis() throws Exception {
        Demanda banner = new Demanda("Fazer banner novo", PrioridadeDemanda.BAIXA, null);
        banner.definirResponsaveis(List.of(membro("Lucas Pereira"), membro("Paula Ribeiro")));
        controller.salvar(banner);

        Demanda salva = buscar("Fazer banner novo");
        assertNull(salva.getRelacionada());
        assertEquals("Lucas Pereira, Paula Ribeiro", salva.getNomesResponsaveis());

        Atividade festa = controller.listarRelacionaveis().stream()
                .filter(a -> a.getTitulo().equals("Festa Atlética")).findFirst().orElseThrow();
        salva.setRelacionada(festa);
        salva.definirResponsaveis(List.of(membro("Paula Ribeiro")));
        controller.salvar(salva);

        Demanda alterada = buscar("Fazer banner novo");
        assertInstanceOf(Evento.class, alterada.getRelacionada());
        assertEquals(festa.getId(), alterada.getRelacionada().getId());
        assertEquals("Paula Ribeiro", alterada.getNomesResponsaveis());
    }

    @Test
    void responsavelPrecisaSerMembroDaGestaoEmAndamento() throws Exception {
        Membro semCargo = new Membro("Carlos Souza", "202199", null);
        membros.salvar(semCargo);
        assertFalse(membros.listarDaGestaoEmAndamento().contains(semCargo));
        assertEquals(5, membros.listarDaGestaoEmAndamento().size());

        Demanda demanda = new Demanda("Organizar o almoxarifado", PrioridadeDemanda.BAIXA, null);
        demanda.definirResponsaveis(List.of(membro("Paula Ribeiro"), semCargo));

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(demanda));
        assertTrue(erro.getMessage().contains("Carlos Souza"), erro.getMessage());

        demanda.definirResponsaveis(List.of(membro("Paula Ribeiro")));
        controller.salvar(demanda);
        assertEquals("Paula Ribeiro", buscar("Organizar o almoxarifado").getNomesResponsaveis());
    }

    @Test
    void relacionaveisSaoEventosECompeticoesEmOrdemDeData() throws Exception {
        List<Atividade> relacionaveis = controller.listarRelacionaveis();

        assertEquals(8, relacionaveis.size());
        assertEquals("Festa Atlética", relacionaveis.get(0).getTitulo());
        assertTrue(relacionaveis.stream().allMatch(a -> a instanceof Evento || a instanceof Competicao));
    }

    @Test
    void excluirEventoMantemADemandaSemARelacao() throws Exception {
        EventoController eventos = new EventoController();
        Evento acaoSocial = eventos.pesquisar(null, null, null).stream()
                .filter(e -> e.getTitulo().equals("Ação Social")).findFirst().orElseThrow();

        eventos.excluir(acaoSocial);

        assertNull(buscar("Arrecadar alimentos").getRelacionada());
    }

    @Test
    void excluiDemandaEMembroResponsavelNaoPodeSerExcluido() throws Exception {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> membros.excluir(membro("Juliana Martins")));
        assertTrue(erro.getMessage().contains("1 demanda(s) sob sua responsabilidade"), erro.getMessage());

        controller.excluir(buscar("Reservar ônibus para os atletas"));

        assertEquals(2, controller.pesquisar(null, null).size());
        assertFalse(titulos(controller.pesquisar(null, null)).contains("Reservar ônibus para os atletas"));
    }

    private Demanda buscar(String titulo) throws Exception {
        return controller.pesquisar(null, null).stream().filter(d -> d.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    private Membro membro(String nome) throws Exception {
        return membros.listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> titulos(List<Demanda> demandas) {
        return demandas.stream().map(Demanda::getTitulo).toList();
    }
}
