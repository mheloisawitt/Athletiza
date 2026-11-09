package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.model.Comparativo;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardControllerTest {

    private static final LocalDate FIM_DO_ANO = LocalDate.of(2026, 12, 31);

    private final DashboardController controller = new DashboardController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void modalidadeComMaisAtletasVemPrimeiro() throws Exception {
        List<Indicador> atletas = controller.carregar(null, FIM_DO_ANO).atletasPorModalidade();

        assertEquals("Futsal (F)", atletas.get(0).nome());
        assertEquals(4, atletas.get(0).valor());
        assertEquals("Vôlei (F)", atletas.get(1).nome());
        assertEquals(2, atletas.get(1).valor());
        assertEquals(4, atletas.size());
    }

    @Test
    void contaPresencasETreinosRealizadosNoPeriodo() throws Exception {
        DadosDashboard dados = controller.carregar(null, FIM_DO_ANO);

        Indicador presencas = dados.presencasPorModalidade().get(0);
        assertEquals("Futsal (F)", presencas.nome());
        assertEquals(3, presencas.valor());
        Indicador treinos = dados.treinosPorModalidade().get(0);
        assertEquals(5, treinos.valor());
        assertEquals(1, treinos.parcial());
        assertEquals(4, dados.resumo().treinos());
        assertEquals(1, dados.resumo().amistosos());
        assertEquals(75, dados.resumo().percentualPresenca());
        assertEquals(7, dados.resumo().atletasAtivos());

        // Treinos futuros não contam: até 05/10 só os de 01/10 e 03/10 aconteceram
        assertEquals(2, controller.carregar(null, LocalDate.of(2026, 10, 5)).treinosPorModalidade().get(0).valor());
        assertTrue(controller.carregar(LocalDate.of(2026, 10, 2), FIM_DO_ANO).presencasPorModalidade().isEmpty());
    }

    @Test
    void atletaQueMenosParticipaVemPrimeiro() throws Exception {
        var frequencias = controller.menosFrequentes(null, FIM_DO_ANO);

        assertEquals("Carla Oliveira", frequencias.get(0).atleta().getNome());
        assertEquals(0, frequencias.get(0).getPercentual());
        assertEquals(100, frequencias.get(frequencias.size() - 1).getPercentual());
    }

    @Test
    void agrupaDemandasAbertasPeloSetorDosResponsaveis() throws Exception {
        try (Connection con = ConnectionFactory.getConnection(); Statement st = con.createStatement()) {
            // sem responsável e sem relação: sem setor; concluída: não conta
            st.execute("INSERT INTO demanda (titulo, situacao) VALUES ('Sem dono', 'ABERTA')");
            st.execute("INSERT INTO demanda (titulo, situacao) VALUES ('Já feita', 'CONCLUIDA')");
            // sem responsável, ligada a uma competição: conta para Esportes
            st.execute("INSERT INTO demanda (titulo, situacao, prazo, competicao_id)"
                    + " SELECT 'Inscrições', 'ABERTA', '2026-10-01', id FROM competicao WHERE titulo = 'Torneio Interno'");
        }

        List<Indicador> setores = controller.demandasPorSetor(LocalDate.of(2026, 10, 22));

        // "Reservar ônibus" (Juliana, Esportes, prazo 25/10) + "Inscrições" (pela competição, atrasada)
        assertEquals(new Indicador("Esportes", 2, 1, "2 em aberto, 1 atrasada(s)"), setores.get(0));
        // "Arrecadar alimentos" tem responsáveis de Eventos e da Presidência: conta nos dois (atrasada desde 20/10)
        assertTrue(setores.contains(new Indicador("Eventos", 1, 1, "1 em aberto, 1 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Presidência", 1, 1, "1 em aberto, 1 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Financeiro", 1, 0, "1 em aberto, 0 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Sem setor definido", 1, 0, "1 em aberto, 0 atrasada(s)")));
        assertEquals(5, setores.size());

        // No topo, cada demanda conta uma vez só
        DadosDashboard dados = controller.carregar(null, LocalDate.of(2026, 10, 22));
        assertEquals(5, dados.resumo().demandasAbertas());
        assertEquals(2, dados.resumo().demandasAtrasadas());
    }

    @Test
    void dadosExtrasDoDashboardPreenchemOsGraficos() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");

        DadosDashboard dados = controller.carregar(null, LocalDate.of(2026, 11, 9));

        assertEquals("Futsal (F)", dados.atletasPorModalidade().get(0).nome());
        assertEquals("Futsal (F)", dados.treinosPorModalidade().get(0).nome());
        assertEquals(10, dados.treinosPorModalidade().get(0).valor());
        assertEquals("Esportes", dados.demandasPorSetor().get(0).nome());
        assertTrue(dados.menosFrequentes().get(0).getPercentual() < 50);
        assertTrue(dados.presencasPorModalidade().size() >= 4);
    }

    @Test
    void linhaDoTempoAgrupaAtividadesEPresencaPorMes() throws Exception {
        LocalDate hoje = LocalDate.of(2026, 10, 31);
        DadosDashboard.LinhaDoTempo linha = controller.linhaDoTempo(LocalDate.of(2026, 9, 1), hoje);

        Comparativo atividades = linha.atividadesPorMes();
        // setembro não tem atividade nos dados base: os meses começam em outubro
        assertEquals(List.of("out/26"), atividades.categorias());
        // outubro: 4 treinos e 1 amistoso de Futsal (F), a JIUDESC (30/10) e 3 eventos até o dia 31
        assertEquals(List.of(4), atividades.serie(DashboardController.TREINOS).valores());
        assertEquals(List.of(1), atividades.serie(DashboardController.AMISTOSOS).valores());
        assertEquals(List.of(1), atividades.serie(DashboardController.COMPETICOES).valores());
        assertEquals(List.of(3), atividades.serie(DashboardController.EVENTOS).valores());

        // só o treino de 01/10 tem lista: 3 presentes e 1 falta
        assertEquals(List.of(75), linha.presencaPorMes().serie(DashboardController.PRESENCA).valores());
        assertEquals(List.of(3), linha.presencasEFaltasPorMes().serie(DashboardController.PRESENCAS).valores());
        assertEquals(List.of(1), linha.presencasEFaltasPorMes().serie(DashboardController.FALTAS).valores());

        // próximos 30 dias a partir de 31/10: competições e o Workshop, em ordem de data
        List<String> proximas = linha.proximas().stream().map(a -> a.getTitulo()).toList();
        assertTrue(proximas.containsAll(List.of("Campeonato Regional", "Workshop")), proximas.toString());
        assertTrue(linha.proximas().stream().noneMatch(a -> a.getData().isBefore(hoje)));
    }

    @Test
    void mesesSemInicioComecamNaPrimeiraAtividadeELimitamA24() {
        assertEquals("jan/26", DashboardController.rotulo(java.time.YearMonth.of(2026, 1)));
        assertEquals(24, DashboardController.meses(java.time.YearMonth.of(2020, 1), java.time.YearMonth.of(2026, 10)).size());
        assertEquals(3, DashboardController.meses(java.time.YearMonth.of(2026, 8), java.time.YearMonth.of(2026, 10)).size());
    }

    @Test
    void distribuicaoContaSituacoesTiposEPodios() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        DadosDashboard.Distribuicao distribuicao = controller.distribuicao(LocalDate.of(2026, 1, 1), hoje);

        assertTrue(distribuicao.atletasPorSituacao().contains(new Indicador("Lesionado", 1, "1 vínculo")));
        assertEquals("Festa", distribuicao.eventosPorTipo().get(0).nome());
        assertEquals(18, distribuicao.demandasPorSituacao().stream().mapToInt(Indicador::valor).sum());
        assertEquals(17, distribuicao.demandasPorPrioridade().stream().mapToInt(Indicador::valor).sum());

        // Futsal (F) tem dois 1º lugares e vem primeiro; Vôlei (F) tem um 1º e um 2º
        Comparativo podios = distribuicao.podiosPorModalidade();
        assertEquals("Futsal (F)", podios.categorias().get(0));
        assertEquals(2, podios.serie(DashboardController.PRIMEIRO).valor(0));
        assertEquals("Atletismo (F)", podios.categorias().get(1));
        int volei = podios.categorias().indexOf("Vôlei (F)");
        assertEquals(1, podios.serie(DashboardController.PRIMEIRO).valor(volei));
        assertEquals(1, podios.serie(DashboardController.SEGUNDO).valor(volei));

        // sem resultados antes de julho
        assertTrue(controller.podiosPorModalidade(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)).isVazio());
    }

    @Test
    void mesSemListaDePresencaFicaSemValor() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");

        Comparativo presenca = controller.linhaDoTempo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 9)).presencaPorMes();

        // julho tem a primeira atividade do ano; todos os meses de julho a novembro têm lista de presença
        assertEquals(List.of("jul/26", "ago/26", "set/26", "out/26", "nov/26"), presenca.categorias());
        assertTrue(presenca.serie(DashboardController.PRESENCA).valores().stream().allMatch(v -> v != null && v > 0));
    }
}
