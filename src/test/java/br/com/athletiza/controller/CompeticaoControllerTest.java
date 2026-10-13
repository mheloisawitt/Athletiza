package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.TarefaDAO;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.view.atletas.PainelCadastroAtleta;
import br.com.athletiza.view.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.view.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.view.competicoes.PainelGerenciarCompeticao;
import br.com.athletiza.view.competicoes.PainelResultados;
import br.com.athletiza.view.componentes.PainelTarefas;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompeticaoControllerTest {

    private final NavegadorFalso navegador = new NavegadorFalso();
    private final CompeticaoDAO dao = new CompeticaoDAO();
    private ConsultaCompeticoesController consulta;

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        consulta = new ConsultaCompeticoesController(new PainelConsultaCompeticoes(), dao, new AtletaDAO(),
                new ModalidadeDAO(), new TarefaDAO(), new MembroDAO(), navegador);
    }

    @Test
    void listaPorDataComFiltrosDeSituacaoEPeriodo() throws Exception {
        assertEquals(Arrays.asList("JIUDESC 2026", "Campeonato Regional", "Copa Alto Vale", "Torneio Interno"),
                titulos(consulta.pesquisar(null, null, null)));
        assertEquals(Arrays.asList("Copa Alto Vale", "Torneio Interno"),
                titulos(consulta.pesquisar(SituacaoCompeticao.PLANEJADA, null, null)));
        assertEquals(Arrays.asList("JIUDESC 2026", "Campeonato Regional"),
                titulos(consulta.pesquisar(null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))));
    }

    @Test
    void validaDatasDaCompeticao() {
        Competicao invalida = new Competicao("Copa", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 1), null, "Ibirama");
        CadastroCompeticaoController cadastro = new CadastroCompeticaoController(new PainelCadastroCompeticao(),
                invalida, dao, navegador);

        assertThrows(ValidacaoException.class, () -> cadastro.gravar());
    }

    @Test
    void carregaInscricoesDaJiudesc() throws Exception {
        Competicao jiudesc = jiudesc();

        assertEquals(7, jiudesc.getModalidades().size());
        List<String> futsalF = Dados.textos(new ArrayList<>(jiudesc.getAtletas(Dados.modalidade("Futsal", Genero.FEMININO))),
                a -> a.getNome());
        Collections.sort(futsalF);
        assertEquals(Arrays.asList("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"), futsalF);
    }

    @Test
    void gerenciarInscricoesPelaTelaESalvar() throws Exception {
        Modalidade voleiM = Dados.modalidade("Vôlei", Genero.MASCULINO);
        Modalidade basqueteM = Dados.modalidade("Basquete", Genero.MASCULINO);
        Atleta eduardo = Dados.atleta("Eduardo Silva");
        eduardo.vincularModalidade(basqueteM, SituacaoAtleta.ATIVO);
        new CadastroAtletaController(new PainelCadastroAtleta(), eduardo, new AtletaDAO(), new ModalidadeDAO(),
                navegador).gravar();

        ViewGerenciarFalsa view = new ViewGerenciarFalsa();
        GerenciarCompeticaoController gerenciar = new GerenciarCompeticaoController(view,
                Dados.competicao("JIUDESC 2026"), dao, new AtletaDAO(), new ModalidadeDAO(), new TarefaDAO(),
                new MembroDAO(), navegador);
        gerenciar.carregar();

        // Desmarca Vôlei (M) e marca o Eduardo no Basquete (M): a modalidade entra junto
        gerenciar.marcarModalidade(voleiM, false);
        gerenciar.marcarAtleta(basqueteM, eduardo, true);
        view.getBtnSalvar().doClick();

        assertEquals(Arrays.asList("Inscrições salvas com sucesso."), view.mensagens);
        Competicao relida = jiudesc();
        assertEquals(7, relida.getModalidades().size());
        assertTrue(relida.getModalidades().contains(basqueteM));
        assertEquals(Arrays.asList(eduardo), new ArrayList<>(relida.getAtletas(basqueteM)));
    }

    @Test
    void atletaSemAModalidadeNaoPodeSerInscrito() throws Exception {
        Competicao jiudesc = jiudesc();

        assertThrows(RegraNegocioException.class,
                () -> jiudesc.inscreverAtleta(Dados.modalidade("Futsal", Genero.FEMININO), Dados.atleta("Carlos Mendes")));
    }

    @Test
    void registraResultadosDeEquipeEIndividuaisEMostraNaConsulta() throws Exception {
        Competicao jiudesc = jiudesc();
        ResultadosController resultados = resultados(jiudesc);
        Modalidade futsalF = Dados.modalidade("Futsal", Genero.FEMININO);
        Modalidade atletismoM = Dados.modalidade("Atletismo", Genero.MASCULINO);

        resultados.salvar(jiudesc, new Resultado(futsalF, null, 2, "3 x 1 na semifinal"));
        resultados.salvar(jiudesc, new Resultado(atletismoM, Dados.atleta("Carlos Mendes"), 1, null));

        assertEquals(2, dao.listarResultados(jiudesc).size());
        Competicao naConsulta = consulta.pesquisar(null, null, null).get(0);
        assertEquals("1º - Atletismo (M)", naConsulta.getMelhorResultado());
    }

    @Test
    void resultadoExigeModalidadeEAtletaInscritosEColocacaoOuPlacar() throws Exception {
        Competicao jiudesc = jiudesc();
        ResultadosController resultados = resultados(jiudesc);
        Modalidade basqueteF = Dados.modalidade("Basquete", Genero.FEMININO);
        Modalidade futsalF = Dados.modalidade("Futsal", Genero.FEMININO);
        Atleta carlos = Dados.atleta("Carlos Mendes");

        assertThrows(RegraNegocioException.class, () -> resultados.salvar(jiudesc, new Resultado(basqueteF, null, 1, null)));
        assertThrows(RegraNegocioException.class, () -> resultados.salvar(jiudesc, new Resultado(futsalF, carlos, 1, null)));
        assertThrows(ValidacaoException.class, () -> resultados.salvar(jiudesc, new Resultado(futsalF, null, null, " ")));
        assertThrows(ValidacaoException.class, () -> ResultadosController.converterColocacao("primeiro"));
        assertEquals(Integer.valueOf(1), ResultadosController.converterColocacao("1º"));
    }

    @Test
    void competicaoTemTarefasComAtrasoEExibeNaConsulta() throws Exception {
        Competicao jiudesc = jiudesc();
        TarefasController tarefas = new TarefasController(new PainelTarefas(), new TarefaDAO(), new MembroDAO(),
                TarefaRepository.Dono.COMPETICAO, jiudesc.getId(), null);
        Tarefa inscricao = new Tarefa("Enviar ficha de inscrição", null, LocalDate.of(2026, 10, 1));
        Tarefa onibus = new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20));

        tarefas.salvar(inscricao);
        tarefas.salvar(onibus);
        assertThrows(ValidacaoException.class, () -> tarefas.salvar(new Tarefa(" ", null, null)));

        assertEquals(Arrays.asList("Enviar ficha de inscrição", "Reservar ônibus"),
                Dados.textos(new TarefaDAO().listar(TarefaRepository.Dono.COMPETICAO, jiudesc.getId()), t -> t.getDescricao()));
        Competicao naConsulta = consulta.pesquisar(null, null, null).get(0);
        assertEquals(Arrays.asList("Enviar ficha de inscrição"),
                Dados.textos(naConsulta.getTarefasAtrasadas(LocalDate.of(2026, 10, 7)), t -> t.getDescricao()));

        inscricao.setSituacao(SituacaoTarefa.CONCLUIDA);
        tarefas.salvar(inscricao);
        new TarefaDAO().excluir(onibus);
        assertEquals(1, new TarefaDAO().listar(TarefaRepository.Dono.COMPETICAO, jiudesc.getId()).size());
        assertTrue(consulta.pesquisar(null, null, null).get(0).getTarefasAtrasadas(LocalDate.of(2026, 10, 7)).isEmpty());
    }

    @Test
    void tarefasDeCompeticaoNaoAparecemNosEventos() throws Exception {
        new TarefaDAO().salvar(TarefaRepository.Dono.COMPETICAO, jiudesc().getId(),
                new Tarefa("Reservar ônibus", null, LocalDate.of(2026, 10, 20)));

        int tarefasNosEventos = 0;
        for (Evento evento : new EventoDAO().listarTodos()) {
            tarefasNosEventos += evento.getTarefas().size();
        }
        assertEquals(3, tarefasNosEventos);
    }

    @Test
    void excluiCompeticaoComInscricoesEResultados() throws Exception {
        Competicao jiudesc = jiudesc();
        resultados(jiudesc).salvar(jiudesc, new Resultado(Dados.modalidade("Futsal", Genero.FEMININO), null, 1, null));

        dao.excluir(jiudesc.getId());

        assertEquals(3, consulta.pesquisar(null, null, null).size());
    }

    /** Tela de gerenciamento que responde "Sim" às confirmações e guarda as mensagens. */
    private static class ViewGerenciarFalsa extends PainelGerenciarCompeticao {

        final List<String> mensagens = new ArrayList<>();

        @Override
        public boolean confirmar(String pergunta) {
            return true;
        }

        @Override
        public void mostrarMensagem(String mensagem) {
            mensagens.add(mensagem);
        }
    }

    private Competicao jiudesc() throws Exception {
        Competicao jiudesc = Dados.competicao("JIUDESC 2026");
        dao.carregarInscricoes(jiudesc);
        return jiudesc;
    }

    private ResultadosController resultados(Competicao competicao) {
        return new ResultadosController(new PainelResultados(), competicao, dao);
    }

    private static List<String> titulos(List<Competicao> competicoes) {
        return Dados.textos(competicoes, c -> c.getTitulo());
    }
}
