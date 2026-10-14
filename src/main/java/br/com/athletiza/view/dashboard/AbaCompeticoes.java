package br.com.athletiza.view.dashboard;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Desfecho;
import java.awt.Color;
import java.time.LocalDate;
import java.util.Map;

/**
 * Aba Competições: competições por status, atletas inscritos por competição,
 * vitórias/empates/derrotas por modalidade e medalhas conquistadas.
 */
class AbaCompeticoes extends AbaDashboard {

    /** Cores tradicionais das medalhas; a legenda e a dica também trazem o nome. */
    private static final Color[] CORES_MEDALHAS = {new Color(0xC9A227), new Color(0x9EA4AB), new Color(0xB0703A)};

    private final Cartoes.Numero futuras = new Cartoes.Numero("Futuras");
    private final Cartoes.Numero emAndamento = new Cartoes.Numero("Em andamento");
    private final Cartoes.Numero encerradas = new Cartoes.Numero("Encerradas");
    private final GraficoPizza porStatus = new GraficoPizza();
    private final GraficoBarras inscritos = new GraficoBarras(Grafico.CORES_SERIES[3]);
    private final GraficoColunas resultados = new GraficoColunas(nomes(), new Color[]{
        Grafico.CORES_SERIES[0], Grafico.CORES_SERIES[2], Grafico.CORES_SERIES[4]}, true);
    private final GraficoColunas medalhas = new GraficoColunas(new String[]{"Ouro", "Prata", "Bronze"},
            CORES_MEDALHAS, true);

    AbaCompeticoes() {
        adicionarLinha(futuras, emAndamento, encerradas);
        adicionarLinha(
                Cartoes.comTitulo("Competições por status", "Todas as competições, sem as canceladas", porStatus),
                Cartoes.comTitulo("Atletas inscritos por competição", "Competições do ano", inscritos));
        adicionarLinha(
                Cartoes.comTitulo("Resultados por modalidade", "Pelo placar dos resultados (ex.: 3 x 1)", resultados),
                Cartoes.comTitulo("Medalhas por modalidade", "1º, 2º e 3º lugares, da modalidade com mais ouros", medalhas));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        Map<String, Integer> status = controller.competicoesPorStatus(hoje);
        futuras.mostrar(status.get("Futuras"), "ainda vão começar");
        emAndamento.mostrar(status.get("Em andamento"), "acontecendo agora");
        encerradas.mostrar(status.get("Encerradas"), "já terminaram");
        porStatus.mostrar(status);
        inscritos.mostrar(controller.inscritosPorCompeticao(hoje));
        resultados.mostrar(controller.resultadosPorModalidade());
        medalhas.mostrar(controller.medalhasPorModalidade());
    }

    private static String[] nomes() {
        String[] nomes = new String[Desfecho.values().length];
        for (Desfecho desfecho : Desfecho.values()) {
            nomes[desfecho.ordinal()] = desfecho.getDescricao();
        }
        return nomes;
    }
}
