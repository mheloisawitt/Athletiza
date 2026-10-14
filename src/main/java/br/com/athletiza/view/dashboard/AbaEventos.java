package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aba Eventos: próximo evento com contagem regressiva, eventos realizados por mês
 * e eventos do ano por tipo (com os amistosos).
 */
class AbaEventos extends AbaDashboard {

    private final CartaoProximoEvento proximo = new CartaoProximoEvento();
    private final GraficoColunas porMes = new GraficoColunas("Eventos", Grafico.CORES_SERIES[4]);
    private final GraficoPizza porTipo = new GraficoPizza();

    AbaEventos() {
        javax.swing.JPanel cartaoProximo = Cartoes.comTitulo("Próximo evento", "Contagem regressiva até o início", proximo);
        cartaoProximo.setPreferredSize(new java.awt.Dimension(300, 230));
        adicionarLinha(cartaoProximo);
        adicionarLinha(
                Cartoes.comTitulo("Eventos realizados por mês", "Últimos " + DashboardController.MESES
                        + " meses, sem os cancelados", porMes),
                Cartoes.comTitulo("Eventos por tipo", "Eventos do ano e amistosos", porTipo));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        proximo.mostrar(controller.proximoEvento(LocalDateTime.now()).orElse(null));
        porMes.mostrarSerieUnica(controller.eventosPorMes(hoje));
        porTipo.mostrar(controller.eventosPorTipo(hoje));
    }
}
