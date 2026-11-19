package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.DemandaDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.PrioridadeDemanda;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.util.Sessao;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemandaControladorTest {
    private static final LocalDate PRAZO = LocalDate.of(2026, 12, 10);

    private final DemandaControlador controlador = new DemandaControlador(new DemandaDAO(), new EventoDAO(), new CompeticaoDAO(), new MembroDAO());
    private final MembroControlador membros = new MembroControlador(new MembroDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @AfterEach
    void sair() {
        Sessao.encerrar();
    }

    @Test
    void registraQuemSolicitouADemanda() throws Exception {
        entrarComo("juliana");
        Demanda uniformes = new Demanda("Buscar uniformes", PrioridadeDemanda.MEDIA, PRAZO);
        uniformes.definirResponsaveis(List.of(membro("Juliana Martins")));
        controlador.salvar(uniformes);

        assertEquals("Juliana Martins", buscar("Buscar uniformes").getNomeSolicitante());
        assertEquals("Mariana Rocha", buscar("Reservar ônibus para os atletas").getNomeSolicitante());
    }

    @Test
    void soQuemRegistrouAPresidenciaOuOAdministradorCancelam() throws Exception {
        entrarComo("juliana");
        Demanda onibus = buscar("Reservar ônibus para os atletas");

        assertFalse(controlador.podeCancelar(onibus));
        assertThrows(RegraNegocioException.class, () -> controlador.excluir(onibus));
        assertThrows(RegraNegocioException.class, () -> controlador.mudarSituacao(onibus, SituacaoDemanda.CANCELADA));
        onibus.setSituacao(SituacaoDemanda.CANCELADA);
        assertThrows(RegraNegocioException.class, () -> controlador.salvar(onibus));

        controlador.mudarSituacao(buscar("Reservar ônibus para os atletas"), SituacaoDemanda.CONCLUIDA);
        assertEquals(SituacaoDemanda.CONCLUIDA, buscar("Reservar ônibus para os atletas").getSituacao());

        entrarComo("mariana");
        controlador.mudarSituacao(buscar("Reservar ônibus para os atletas"), SituacaoDemanda.CANCELADA);
        assertEquals(SituacaoDemanda.CANCELADA, buscar("Reservar ônibus para os atletas").getSituacao());
    }

    @Test
    void assessorResponsavelSoMudaASituacao() throws Exception {
        entrarComo("mariana");
        Demanda bolas = new Demanda("Comprar bolas", PrioridadeDemanda.BAIXA, PRAZO);
        bolas.definirResponsaveis(List.of(membro("Bruno Costa")));
        controlador.salvar(bolas);

        entrarComo("bruno");
        Demanda salva = buscar("Comprar bolas");
        assertFalse(Sessao.getAcesso().podeAlterar(salva));
        assertTrue(controlador.podeMudarSituacao(salva));
        controlador.mudarSituacao(salva, SituacaoDemanda.EM_ANDAMENTO);
        assertEquals(SituacaoDemanda.EM_ANDAMENTO, buscar("Comprar bolas").getSituacao());
        assertThrows(RegraNegocioException.class, () -> controlador.mudarSituacao(salva, SituacaoDemanda.CANCELADA));
        assertEquals(List.of("Comprar bolas"), titulos(controlador.pesquisar(null, controlador.membroLogado().orElseThrow())));

        entrarComo("rafael");
        assertFalse(controlador.podeMudarSituacao(salva));
        assertThrows(RegraNegocioException.class, () -> controlador.mudarSituacao(salva, SituacaoDemanda.CONCLUIDA));
    }

    @Test
    void listaPorPrioridadeEPrazoComRelacaoEResponsaveis() throws Exception {
        List<Demanda> demandas = controlador.pesquisar(null, null);

        assertEquals(List.of("Arrecadar alimentos", "Reservar ônibus para os atletas", "Renovar o estoque de camisetas"),
                titulos(demandas));
        assertEquals("Evento · Ação Social (21/10/2026)", demandas.get(0).getDescricaoRelacionada());
        assertEquals("Mariana Rocha, Rafael Teixeira", demandas.get(0).getNomesResponsaveis());
        assertInstanceOf(Competicao.class, demandas.get(1).getRelacionada());
        assertNull(demandas.get(2).getRelacionada());
    }

    @Test
    void filtraPorSituacaoEPorResponsavel() throws Exception {
        assertEquals(List.of("Arrecadar alimentos"), titulos(controlador.pesquisar(SituacaoDemanda.EM_ANDAMENTO, null)));
        assertEquals(List.of("Reservar ônibus para os atletas"),
                titulos(controlador.pesquisar(null, membro("Juliana Martins"))));
        assertTrue(controlador.pesquisar(SituacaoDemanda.CONCLUIDA, null).isEmpty());
    }

    @Test
    void validaCadastroDeDemanda() {
        Demanda invalida = new Demanda(" ", null, null);
        invalida.setSituacao(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controlador.salvar(invalida));
        assertEquals(5, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiDemandaAvulsaEDepoisRelacionaATrocandoResponsaveis() throws Exception {
        Demanda banner = new Demanda("Fazer banner novo", PrioridadeDemanda.BAIXA, PRAZO);
        banner.definirResponsaveis(List.of(membro("Lucas Pereira"), membro("Paula Ribeiro")));
        controlador.salvar(banner);

        Demanda salva = buscar("Fazer banner novo");
        assertNull(salva.getRelacionada());
        assertEquals("Lucas Pereira, Paula Ribeiro", salva.getNomesResponsaveis());

        Atividade festa = controlador.listarRelacionaveis().stream()
                .filter(a -> a.getTitulo().equals("Festa Atlética")).findFirst().orElseThrow();
        salva.setRelacionada(festa);
        salva.definirResponsaveis(List.of(membro("Paula Ribeiro")));
        controlador.salvar(salva);

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
        assertEquals(6, membros.listarDaGestaoEmAndamento().size());

        Demanda demanda = new Demanda("Organizar o almoxarifado", PrioridadeDemanda.BAIXA, PRAZO);
        demanda.definirResponsaveis(List.of(membro("Paula Ribeiro"), semCargo));

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controlador.salvar(demanda));
        assertTrue(erro.getMessage().contains("Carlos Souza"), erro.getMessage());

        demanda.definirResponsaveis(List.of(membro("Paula Ribeiro")));
        controlador.salvar(demanda);
        assertEquals("Paula Ribeiro", buscar("Organizar o almoxarifado").getNomesResponsaveis());
    }

    @Test
    void relacionaveisSaoEventosECompeticoesEmOrdemDeData() throws Exception {
        List<Atividade> relacionaveis = controlador.listarRelacionaveis();

        assertEquals(8, relacionaveis.size());
        assertEquals("Festa Atlética", relacionaveis.get(0).getTitulo());
        assertTrue(relacionaveis.stream().allMatch(a -> a instanceof Evento || a instanceof Competicao));
    }

    @Test
    void excluirEventoMantemADemandaSemARelacao() throws Exception {
        EventoControlador eventos = new EventoControlador(new EventoDAO());
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

        controlador.excluir(buscar("Reservar ônibus para os atletas"));

        assertEquals(2, controlador.pesquisar(null, null).size());
        assertFalse(titulos(controlador.pesquisar(null, null)).contains("Reservar ônibus para os atletas"));
    }

    private Demanda buscar(String titulo) throws Exception {
        return controlador.pesquisar(null, null).stream().filter(d -> d.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    private static void entrarComo(String login) throws Exception {
        Usuario usuario = new UsuarioDAO().buscarPorLogin(login).orElseThrow();
        Sessao.iniciar(usuario, new AcessoControlador(new CargoDAO()).calcular(usuario).orElseThrow());
    }

    private Membro membro(String nome) throws Exception {
        return membros.listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static List<String> titulos(List<Demanda> demandas) {
        return demandas.stream().map(Demanda::getTitulo).toList();
    }
}
