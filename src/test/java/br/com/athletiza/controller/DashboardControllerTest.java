package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
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
        assertEquals("Futsal (M)", dados.treinosPorModalidade().get(0).nome());
        assertEquals(6, dados.treinosPorModalidade().get(0).valor());
        assertEquals("Esportes", dados.demandasPorSetor().get(0).nome());
        assertTrue(dados.menosFrequentes().get(0).getPercentual() < 50);
        assertTrue(dados.presencasPorModalidade().size() >= 4);
    }
}
