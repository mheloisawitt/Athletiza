package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.DashboardDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.modelo.Comparativo;
import br.com.athletiza.modelo.DadosDashboard;
import br.com.athletiza.modelo.Indicador;
import br.com.athletiza.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardControladorTest {
    private static final LocalDate FIM_DO_ANO = LocalDate.of(2026, 12, 31);

    private final DashboardControlador controlador = new DashboardControlador(new DashboardDAO(), new TreinoDAO(), new AtividadeDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void modalidadeComMaisAtletasVemPrimeiro() throws Exception {
        List<Indicador> atletas = controlador.carregar(null, FIM_DO_ANO).atletasPorModalidade();

        assertEquals("Futsal (F)", atletas.get(0).nome());
        assertEquals(4, atletas.get(0).valor());
        assertEquals("Vôlei (F)", atletas.get(1).nome());
        assertEquals(2, atletas.get(1).valor());
        assertEquals(4, atletas.size());
    }

    @Test
    void contaPresencasETreinosRealizadosNoPeriodo() throws Exception {
        DadosDashboard dados = controlador.carregar(null, FIM_DO_ANO);

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

        assertEquals(2, controlador.carregar(null, LocalDate.of(2026, 10, 5)).treinosPorModalidade().get(0).valor());
        assertTrue(controlador.carregar(LocalDate.of(2026, 10, 2), FIM_DO_ANO).presencasPorModalidade().isEmpty());
    }

    @Test
    void atletaQueMenosParticipaVemPrimeiro() throws Exception {
        var frequencias = controlador.menosFrequentes(null, FIM_DO_ANO);

        assertEquals("Carla Oliveira", frequencias.get(0).atleta().getNome());
        assertEquals(0, frequencias.get(0).getPercentual());
        assertEquals(100, frequencias.get(frequencias.size() - 1).getPercentual());
    }

    @Test
    void agrupaDemandasAbertasPeloSetorDosResponsaveis() throws Exception {
        try (Connection con = ConnectionFactory.getConnection(); Statement st = con.createStatement()) {
            st.execute("INSERT INTO demanda (titulo, situacao) VALUES ('Sem dono', 'ABERTA')");
            st.execute("INSERT INTO demanda (titulo, situacao) VALUES ('Já feita', 'CONCLUIDA')");

            st.execute("INSERT INTO demanda (titulo, situacao, prazo, competicao_id)"
                    + " SELECT 'Inscrições', 'ABERTA', '2026-10-01', id FROM competicao WHERE titulo = 'Torneio Interno'");
        }

        List<Indicador> setores = controlador.demandasPorSetor(LocalDate.of(2026, 10, 22));

        assertEquals(new Indicador("Esportes", 2, 1, "2 em aberto, 1 atrasada(s)"), setores.get(0));

        assertTrue(setores.contains(new Indicador("Eventos", 1, 1, "1 em aberto, 1 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Presidência", 1, 1, "1 em aberto, 1 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Financeiro", 1, 0, "1 em aberto, 0 atrasada(s)")));
        assertTrue(setores.contains(new Indicador("Sem setor definido", 1, 0, "1 em aberto, 0 atrasada(s)")));
        assertEquals(5, setores.size());

        DadosDashboard dados = controlador.carregar(null, LocalDate.of(2026, 10, 22));
        assertEquals(5, dados.resumo().demandasAbertas());
        assertEquals(2, dados.resumo().demandasAtrasadas());
    }

    @Test
    void dadosExtrasDoDashboardPreenchemOsGraficos() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");

        DadosDashboard dados = controlador.carregar(null, LocalDate.of(2026, 11, 9));

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
        DadosDashboard.LinhaDoTempo linha = controlador.linhaDoTempo(LocalDate.of(2026, 9, 1), hoje);

        Comparativo atividades = linha.atividadesPorMes();

        assertEquals(List.of("out/26"), atividades.categorias());

        assertEquals(List.of(4), atividades.serie(DashboardControlador.TREINOS).valores());
        assertEquals(List.of(1), atividades.serie(DashboardControlador.AMISTOSOS).valores());
        assertEquals(List.of(1), atividades.serie(DashboardControlador.COMPETICOES).valores());
        assertEquals(List.of(3), atividades.serie(DashboardControlador.EVENTOS).valores());

        assertEquals(List.of(75), linha.presencaPorMes().serie(DashboardControlador.PRESENCA).valores());
        assertEquals(List.of(3), linha.presencasEFaltasPorMes().serie(DashboardControlador.PRESENCAS).valores());
        assertEquals(List.of(1), linha.presencasEFaltasPorMes().serie(DashboardControlador.FALTAS).valores());

        List<String> proximas = linha.proximas().stream().map(a -> a.getTitulo()).toList();
        assertTrue(proximas.containsAll(List.of("Campeonato Regional", "Workshop")), proximas.toString());
        assertTrue(linha.proximas().stream().noneMatch(a -> a.getData().isBefore(hoje)));
    }

    @Test
    void mesesSemInicioComecamNaPrimeiraAtividadeELimitamA24() {
        assertEquals("jan/26", DashboardControlador.rotulo(java.time.YearMonth.of(2026, 1)));
        assertEquals(24, DashboardControlador.meses(java.time.YearMonth.of(2020, 1), java.time.YearMonth.of(2026, 10)).size());
        assertEquals(3, DashboardControlador.meses(java.time.YearMonth.of(2026, 8), java.time.YearMonth.of(2026, 10)).size());
    }

    @Test
    void distribuicaoContaSituacoesTiposEPodios() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");
        LocalDate hoje = LocalDate.of(2026, 11, 9);

        DadosDashboard.Distribuicao distribuicao = controlador.distribuicao(LocalDate.of(2026, 1, 1), hoje);

        assertTrue(distribuicao.atletasPorSituacao().contains(new Indicador("Lesionado", 1, "1 vínculo")));
        assertEquals("Festa", distribuicao.eventosPorTipo().get(0).nome());
        assertEquals(18, distribuicao.demandasPorSituacao().stream().mapToInt(Indicador::valor).sum());
        assertEquals(17, distribuicao.demandasPorPrioridade().stream().mapToInt(Indicador::valor).sum());

        Comparativo podios = distribuicao.podiosPorModalidade();
        assertEquals("Futsal (F)", podios.categorias().get(0));
        assertEquals(2, podios.serie(DashboardControlador.PRIMEIRO).valor(0));
        assertEquals("Atletismo (F)", podios.categorias().get(1));
        int volei = podios.categorias().indexOf("Vôlei (F)");
        assertEquals(1, podios.serie(DashboardControlador.PRIMEIRO).valor(volei));
        assertEquals(1, podios.serie(DashboardControlador.SEGUNDO).valor(volei));

        assertTrue(controlador.podiosPorModalidade(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)).isVazio());
    }

    @Test
    void mesSemListaDePresencaFicaSemValor() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");

        Comparativo presenca = controlador.linhaDoTempo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 9)).presencaPorMes();

        assertEquals(List.of("jul/26", "ago/26", "set/26", "out/26", "nov/26"), presenca.categorias());
        assertTrue(presenca.serie(DashboardControlador.PRESENCA).valores().stream().allMatch(v -> v != null && v > 0));
    }
}
