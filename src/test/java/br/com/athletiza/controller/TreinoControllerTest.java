package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Presenca;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.view.treinos.PainelCadastroTreino;
import br.com.athletiza.view.treinos.PainelFrequencia;
import br.com.athletiza.view.treinos.PainelPresenca;
import br.com.athletiza.view.treinos.PainelTreinos;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TreinoControllerTest {

    private final NavegadorFalso navegador = new NavegadorFalso();
    private final TreinoDAO dao = new TreinoDAO();
    private TreinosController consulta;

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        consulta = new TreinosController(new PainelTreinos(), dao, new ModalidadeDAO(), new MembroDAO(), new AtletaDAO(),
                navegador);
    }

    private CadastroTreinoController cadastro(Treino treino) {
        return new CadastroTreinoController(new PainelCadastroTreino(treino instanceof Amistoso), treino, dao,
                new ModalidadeDAO(), new MembroDAO(), navegador);
    }

    private PresencaController presenca(Treino treino) {
        return new PresencaController(new PainelPresenca(), treino, dao, new AtletaDAO(), navegador);
    }

    private FrequenciaController frequencia() {
        return new FrequenciaController(new PainelFrequencia(), dao, new ModalidadeDAO(), navegador);
    }

    @Test
    void filtraPorModalidadeTipoEPeriodo() throws Exception {
        Modalidade futsalF = Dados.modalidade("Futsal", Genero.FEMININO);

        assertEquals(5, consulta.pesquisar(futsalF, null, null, null).size());
        assertEquals(0, consulta.pesquisar(Dados.modalidade("Vôlei", Genero.FEMININO), null, null, null).size());
        List<Treino> amistosos = consulta.pesquisar(null, null, null, TipoAtividade.AMISTOSO);
        assertEquals(1, amistosos.size());
        assertEquals("Atlética Furiosa", ((Amistoso) amistosos.get(0)).getAdversario());
        assertEquals(2, consulta.pesquisar(null, LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 6), null).size());
    }

    @Test
    void geraTituloEImpedeConflitoDeHorarioNoMesmoLocal() throws Exception {
        Modalidade voleiF = Dados.modalidade("Vôlei", Genero.FEMININO);
        Treino conflito = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(19, 30), "quadra 1");
        Treino outroLocal = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(19, 30), "Quadra 2");
        Treino maisTarde = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(20, 0), "Quadra 1");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> cadastro(conflito).gravar());
        assertTrue(erro.getMessage().contains("06/10/2026 às 19:00"), erro.getMessage());
        cadastro(outroLocal).gravar();
        cadastro(maisTarde).gravar();

        outroLocal.setTitulo(null);
        cadastro(outroLocal).gravar();
        assertEquals("Treino Vôlei (F)", outroLocal.getTitulo());
    }

    @Test
    void amistosoExigeAdversarioAlemDosCamposDoTreino() {
        Amistoso semAdversario = new Amistoso();
        semAdversario.setData(LocalDate.of(2026, 10, 20));

        // Amistoso.validarCampos sobrescreve o de Treino: modalidade (do treino) + adversário (do amistoso)
        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> semAdversario.validar());
        assertEquals(2, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void cadastraAmistosoComTituloGerado() throws Exception {
        Amistoso amistoso = new Amistoso(Dados.modalidade("Vôlei", Genero.FEMININO), "Atlética Fênix",
                LocalDate.of(2026, 10, 21), null, null);
        amistoso.setTitulo(null);

        cadastro(amistoso).gravar();

        assertEquals("Amistoso Vôlei (F) x Atlética Fênix", ((Amistoso) dao.buscarPorId(amistoso.getId())).getTitulo());
    }

    @Test
    void carregaESalvaListaDePresenca() throws Exception {
        Treino dia1 = treinoDoDia(1);
        PresencaController controller = presenca(dia1);
        assertEquals(4, dia1.getPresencas().size());
        assertEquals(Arrays.asList("Ana Souza", "Beatriz Lima", "Daniela Costa"), Dados.textos(dia1.getPresentes(), a -> a.getNome()));

        controller.getView().marcarPresenca(Dados.atleta("Carla Oliveira"), true);
        controller.gravar();

        Treino relido = treinoDoDia(1);
        dao.carregarPresencas(relido);
        assertEquals(4, relido.getPresentes().size());
    }

    @Test
    void listaDeChamadaTemOsAtletasAtivosDaModalidade() throws Exception {
        Treino dia3 = treinoDoDia(3);

        assertEquals(Arrays.asList("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                Dados.textos(presenca(dia3).atletasDaChamada(), a -> a.getNome()));
    }

    @Test
    void naoRegistraPresencaEmTreinoFuturo() throws Exception {
        Treino futuro = new Treino(Dados.modalidade("Futsal", Genero.FEMININO), LocalDate.now().plusDays(5), null, null);
        cadastro(futuro).gravar();

        assertThrows(RegraNegocioException.class, () -> presenca(futuro).gravar());
    }

    @Test
    void calculaFrequenciaPorAtleta() throws Exception {
        PresencaController chamada = presenca(treinoDoDia(3));
        chamada.getView().marcarTodos(true);
        chamada.getView().marcarPresenca(Dados.atleta("Ana Souza"), false);
        chamada.gravar();

        List<Frequencia> lista = frequencia().frequencia(Dados.modalidade("Futsal", Genero.FEMININO), null, null);

        assertEquals(Arrays.asList("Beatriz Lima", "Daniela Costa", "Ana Souza", "Carla Oliveira"),
                Dados.textos(lista, f -> f.getAtleta().getNome()));
        Frequencia ana = lista.get(2);
        assertEquals(2, ana.getTreinos());
        assertEquals(50, ana.getPercentual());
        assertEquals(1, frequencia().frequencia(null, LocalDate.of(2026, 10, 2), null).get(0).getTreinos());
    }

    @Test
    void mostraOHistoricoDePresencaDeUmAtleta() throws Exception {
        PresencaController chamada = presenca(treinoDoDia(3));
        chamada.getView().marcarTodos(true);
        Atleta carla = Dados.atleta("Carla Oliveira");
        chamada.getView().marcarPresenca(carla, false);
        chamada.gravar();

        List<PresencaEmTreino> historico = dao.historicoDoAtleta(carla, null, null);

        assertEquals(2, historico.size());
        assertEquals(LocalDate.of(2026, 10, 3), historico.get(0).getTreino().getData(), "mais recente primeiro");
        assertEquals(Presenca.AUSENTE, historico.get(0).getPresenca());
        assertEquals(Presenca.AUSENTE, historico.get(1).getPresenca());
        assertEquals(1, dao.historicoDoAtleta(carla, null, LocalDate.of(2026, 10, 2)).size());
        assertTrue(dao.historicoDoAtleta(carla, Dados.modalidade("Vôlei", Genero.FEMININO), null).isEmpty());
    }

    @Test
    void excluiTreinoComPresencas() throws Exception {
        dao.excluir(treinoDoDia(1).getId());

        assertEquals(4, consulta.pesquisar(null, null, null, null).size());
        assertTrue(frequencia().frequencia(null, null, null).isEmpty());
    }

    private Treino treinoDoDia(int dia) throws Exception {
        return consulta.pesquisar(null, LocalDate.of(2026, 10, dia), LocalDate.of(2026, 10, dia), null).get(0);
    }
}
