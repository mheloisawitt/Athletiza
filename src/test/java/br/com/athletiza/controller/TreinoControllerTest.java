package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Pessoa;
import br.com.athletiza.model.Presenca;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TreinoControllerTest {

    private final TreinoController controller = new TreinoController();
    private final ModalidadeController modalidades = new ModalidadeController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void filtraPorModalidadeTipoEPeriodo() throws Exception {
        Modalidade futsalF = modalidade("Futsal", Genero.FEMININO);

        assertEquals(5, controller.pesquisar(futsalF, null, null, null).size());
        assertEquals(0, controller.pesquisar(modalidade("Vôlei", Genero.FEMININO), null, null, null).size());
        List<Treino> amistosos = controller.pesquisar(null, null, null, TipoAtividade.AMISTOSO);
        assertEquals(1, amistosos.size());
        assertEquals("Atlética Furiosa", ((Amistoso) amistosos.get(0)).getAdversario());
        assertEquals(2, controller.pesquisar(null, LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 6), null).size());
    }

    @Test
    void geraTituloEImpedeConflitoDeHorarioNoMesmoLocal() throws Exception {
        Modalidade voleiF = modalidade("Vôlei", Genero.FEMININO);
        Treino conflito = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(19, 30), "quadra 1");
        Treino outroLocal = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(19, 30), "Quadra 2");
        Treino maisTarde = new Treino(voleiF, LocalDate.of(2026, 10, 6), LocalTime.of(20, 0), "Quadra 1");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controller.salvar(conflito));
        assertTrue(erro.getMessage().contains("06/10/2026 às 19:00"), erro.getMessage());
        controller.salvar(outroLocal);
        controller.salvar(maisTarde);

        outroLocal.setTitulo(null);
        controller.salvar(outroLocal);
        assertEquals("Treino Vôlei (F)", outroLocal.getTitulo());
    }

    @Test
    void amistosoExigeAdversario() {
        Amistoso semAdversario = new Amistoso();
        semAdversario.setData(LocalDate.of(2026, 10, 20));

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(semAdversario));
        assertEquals(2, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void carregaESalvaListaDePresenca() throws Exception {
        Treino dia1 = treinoDoDia(1);
        controller.carregarPresencas(dia1);
        assertEquals(4, dia1.getPresencas().size());
        assertEquals(List.of("Ana Souza", "Beatriz Lima", "Daniela Costa"),
                dia1.getPresentes().stream().map(Pessoa::getNome).toList());

        Atleta carla = dia1.getPresencas().keySet().stream()
                .filter(a -> a.getNome().equals("Carla Oliveira")).findFirst().orElseThrow();
        dia1.registrarPresenca(carla, true);
        controller.salvarPresencas(dia1);

        Treino relido = treinoDoDia(1);
        controller.carregarPresencas(relido);
        assertEquals(4, relido.getPresentes().size());
    }

    @Test
    void listaDeChamadaTemOsAtletasAtivosDaModalidade() throws Exception {
        Treino dia3 = treinoDoDia(3);

        assertEquals(List.of("Ana Souza", "Beatriz Lima", "Carla Oliveira", "Daniela Costa"),
                controller.atletasDaChamada(dia3).stream().map(Pessoa::getNome).toList());
    }

    @Test
    void naoRegistraPresencaEmTreinoFuturo() throws Exception {
        Treino futuro = new Treino(modalidade("Futsal", Genero.FEMININO), LocalDate.now().plusDays(5), null, null);
        controller.salvar(futuro);

        assertThrows(RegraNegocioException.class, () -> controller.salvarPresencas(futuro));
    }

    @Test
    void calculaFrequenciaPorAtleta() throws Exception {
        Treino dia3 = treinoDoDia(3);
        for (Atleta atleta : controller.atletasDaChamada(dia3)) {
            dia3.registrarPresenca(atleta, !atleta.getNome().equals("Ana Souza"));
        }
        controller.salvarPresencas(dia3);

        List<Frequencia> frequencia = controller.frequencia(modalidade("Futsal", Genero.FEMININO), null, null);

        assertEquals(List.of("Beatriz Lima", "Daniela Costa", "Ana Souza", "Carla Oliveira"),
                frequencia.stream().map(f -> f.atleta().getNome()).toList());
        Frequencia ana = frequencia.get(2);
        assertEquals(2, ana.treinos());
        assertEquals(50, ana.getPercentual());
        assertEquals(1, controller.frequencia(null, LocalDate.of(2026, 10, 2), null).get(0).treinos());
    }

    @Test
    void mostraOHistoricoDePresencaDeUmAtleta() throws Exception {
        Treino dia3 = treinoDoDia(3);
        for (Atleta atleta : controller.atletasDaChamada(dia3)) {
            dia3.registrarPresenca(atleta, !atleta.getNome().equals("Carla Oliveira"));
        }
        controller.salvarPresencas(dia3);
        Atleta carla = controller.atletasDaChamada(dia3).stream()
                .filter(a -> a.getNome().equals("Carla Oliveira")).findFirst().orElseThrow();

        List<PresencaEmTreino> historico = controller.historicoDoAtleta(carla, null, null);

        assertEquals(2, historico.size());
        assertEquals(LocalDate.of(2026, 10, 3), historico.get(0).treino().getData(), "mais recente primeiro");
        assertEquals(List.of(Presenca.AUSENTE, Presenca.AUSENTE), historico.stream().map(PresencaEmTreino::presenca).toList());
        assertEquals(1, controller.historicoDoAtleta(carla, null, LocalDate.of(2026, 10, 2)).size());
        assertTrue(controller.historicoDoAtleta(carla, modalidade("Vôlei", Genero.FEMININO), null).isEmpty());
    }

    @Test
    void excluiTreinoComPresencas() throws Exception {
        controller.excluir(treinoDoDia(1));

        assertEquals(4, controller.pesquisar(null, null, null, null).size());
        assertTrue(controller.frequencia(null, null, null).isEmpty());
    }

    private Treino treinoDoDia(int dia) throws Exception {
        return controller.pesquisar(null, LocalDate.of(2026, 10, dia), LocalDate.of(2026, 10, dia), null).get(0);
    }

    private Modalidade modalidade(String nome, Genero genero) throws Exception {
        return modalidades.listar().stream().filter(m -> m.equals(new Modalidade(nome, genero))).findFirst().orElseThrow();
    }
}
