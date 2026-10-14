package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 14);

    private final DashboardController controller = new DashboardController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void totaisDosCartoesDoTopo() throws Exception {
        DashboardController.Totais totais = controller.totais(HOJE);

        assertEquals(7, totais.atletasAtivos());
        assertEquals(10, totais.modalidades());
        assertEquals(4, totais.competicoesNoAno());
        assertEquals(4, totais.eventosNoAno());
    }

    @Test
    void linhaDoTempoDoMesNaoTemCompromissos() throws Exception {
        Map<LocalDate, List<Atividade>> outubro = controller.linhaDoTempo(YearMonth.of(2026, 10));

        assertEquals(1, outubro.get(LocalDate.of(2026, 10, 8)).size(), "a reunião do dia 8 fica de fora");
        assertTrue(outubro.values().stream().flatMap(List::stream).noneMatch(a -> a instanceof Compromisso));
        assertTrue(outubro.containsKey(LocalDate.of(2026, 10, 31)), "JIUDESC continua no dia 31");
    }

    @Test
    void atletasPorModalidadeCursoEFase() throws Exception {
        Map<String, Integer> porModalidade = controller.atletasPorModalidade();
        assertEquals("Futsal (F)", porModalidade.keySet().iterator().next());
        assertEquals(4, porModalidade.get("Futsal (F)"));
        assertEquals(2, porModalidade.get("Vôlei (F)"));

        assertEquals(Map.of("Engenharia Civil", 2, "Administração", 2, "Sistemas de Informação", 2, "Pedagogia", 1),
                controller.atletasPorCurso());
        assertEquals(List.of("1ª", "3ª", "5ª", "7ª"), List.copyOf(controller.atletasPorFase().keySet()));
    }

    @Test
    void novosCadastrosNosUltimos12Meses() throws Exception {
        Map<String, Integer> porMes = controller.novosCadastrosPorMes(HOJE);

        assertEquals(12, porMes.size());
        assertEquals("nov/25", porMes.keySet().iterator().next());
        assertEquals(1, porMes.get("nov/25"));
        assertEquals(2, porMes.get("mar/26"));
        assertEquals(2, porMes.get("ago/26"));
        assertEquals(0, porMes.get("out/26"));
        assertEquals(6, controller.novosCadastrosNoAno(HOJE));
    }

    @Test
    void treinosRealizadosPresencaESemana() throws Exception {
        assertArrayEquals(new int[]{4, 1}, controller.treinosPorMes(HOJE).get("out/26"), "4 treinos e 1 amistoso");
        assertArrayEquals(new int[]{0, 0}, controller.treinosPorMes(HOJE).get("set/26"));
        assertEquals(Map.of("Futsal (F)", 5), controller.treinosPorModalidade(HOJE));
        assertEquals(Map.of("Futsal (F)", 75), controller.frequenciaPorModalidade(), "3 de 4 presentes");
        assertEquals(List.of(LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)),
                controller.treinosDaSemana(LocalDate.of(2026, 10, 7)).stream().map(Atividade::getData).toList());
    }

    @Test
    void competicoesPorStatusEInscritos() throws Exception {
        assertEquals(Map.of("Futuras", 3, "Em andamento", 1, "Encerradas", 0),
                controller.competicoesPorStatus(LocalDate.of(2026, 10, 31)));
        assertEquals(Map.of("Futuras", 0, "Em andamento", 0, "Encerradas", 4),
                controller.competicoesPorStatus(LocalDate.of(2027, 1, 1)));

        Map<String, Integer> inscritos = controller.inscritosPorCompeticao(HOJE);
        assertEquals(List.of("JIUDESC 2026", "Campeonato Regional", "Copa Alto Vale", "Torneio Interno"),
                List.copyOf(inscritos.keySet()));
        assertEquals(7, inscritos.get("JIUDESC 2026"));
        assertEquals(0, inscritos.get("Torneio Interno"));
    }

    @Test
    void eventosPorTipoEProximoEvento() throws Exception {
        assertEquals(Map.of("Festa", 1, "Social", 1, "Competição", 1, "Outro", 1, "Amistoso", 1),
                controller.eventosPorTipo(HOJE));
        assertEquals(1, controller.eventosPorMes(HOJE).get("out/26"), "só a festa do dia 12 já aconteceu");

        assertEquals("Festa Atlética", controller.proximoEvento(LocalDateTime.of(2026, 10, 12, 19, 0)).orElseThrow().getTitulo());
        assertEquals("Ação Social", controller.proximoEvento(LocalDateTime.of(2026, 10, 12, 21, 0)).orElseThrow().getTitulo());
        assertTrue(controller.proximoEvento(LocalDateTime.of(2026, 12, 1, 0, 0)).isEmpty());
    }

    @Test
    void gestaoAtualEComparativo() throws Exception {
        Gestao atual = controller.gestaoAtual(HOJE).orElseThrow();
        assertEquals("Gestão 2025-2026", atual.getNome());
        assertEquals(List.of("Mariana Rocha"), atual.getOrganograma().entrySet().iterator().next().getValue()
                .stream().map(Membro::getNome).toList(), "o primeiro cargo é o de presidente");

        Map<String, int[]> comparativo = controller.comparativoGestoes();
        assertEquals(List.of("Gestão 2020-2021", "Gestão 2022-2023", "Gestão 2023-2024", "Gestão 2025-2026"),
                List.copyOf(comparativo.keySet()));
        assertArrayEquals(new int[]{5, 4, 4}, comparativo.get("Gestão 2025-2026"));
        assertArrayEquals(new int[]{0, 0, 0}, comparativo.get("Gestão 2020-2021"));
    }

    @Test
    void historicoDoDashboardAlimentaResultadosEMedalhas() throws Exception {
        BancoDeTeste.carregarHistoricoDoDashboard();

        assertArrayEquals(new int[]{4, 1, 2}, controller.resultadosPorModalidade().get("Futsal (F)"),
                "4 vitórias, 1 empate e 2 derrotas");
        Map.Entry<String, int[]> primeiro = controller.medalhasPorModalidade().entrySet().iterator().next();
        assertEquals("Futsal (F)", primeiro.getKey());
        assertArrayEquals(new int[]{3, 2, 0}, primeiro.getValue());
        assertArrayEquals(new int[]{1, 1, 1}, controller.medalhasPorModalidade().get("Atletismo (M)"));

        assertEquals(11, controller.totais(HOJE).atletasAtivos());
        assertEquals(4, controller.treinosDaSemana(HOJE).size());
        assertTrue(controller.comparativoGestoes().get("Gestão 2022-2023")[0] > 0);
        assertTrue(controller.frequenciaPorModalidade().size() > 1);
    }
}
