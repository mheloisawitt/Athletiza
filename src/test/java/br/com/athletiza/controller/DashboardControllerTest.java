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
    void agrupaDemandasAbertasPeloSetorDoResponsavel() throws Exception {
        try (Connection con = ConnectionFactory.getConnection(); Statement st = con.createStatement()) {
            st.execute("INSERT INTO tarefa (evento_id, descricao, situacao)"
                    + " SELECT id, 'Sem responsável', 'PENDENTE' FROM evento WHERE titulo = 'Festa Atlética'");
        }

        List<Indicador> setores = controller.demandasPorSetor(LocalDate.of(2026, 10, 8));

        // "Contratar DJ" (Diretor de Eventos) venceu em 05/10; "Vender ingressos" (Tesoureiro) vence em 11/10
        assertEquals(new Indicador("Diretor de Eventos", 1, 1, "1 em aberto, 1 atrasada(s)"), setores.get(0));
        assertEquals("Sem setor definido", setores.get(1).nome());
        assertEquals("Tesoureiro", setores.get(2).nome());
        assertEquals(0, setores.get(2).parcial());
    }

    @Test
    void dadosExtrasDoDashboardPreenchemOsGraficos() throws Exception {
        BancoDeTeste.executar("03_dados_dashboard.sql");

        DadosDashboard dados = controller.carregar(null, LocalDate.of(2026, 11, 9));

        assertEquals("Futsal (F)", dados.atletasPorModalidade().get(0).nome());
        assertEquals("Futsal (M)", dados.treinosPorModalidade().get(0).nome());
        assertEquals(6, dados.treinosPorModalidade().get(0).valor());
        assertEquals("Diretor de Esportes", dados.demandasPorSetor().get(0).nome());
        assertTrue(dados.menosFrequentes().get(0).getPercentual() < 50);
        assertTrue(dados.presencasPorModalidade().size() >= 4);
    }
}
