package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Aba Atletas: total de ativos, atletas por modalidade, por curso, por fase
 * e novos cadastros por mês.
 */
class AbaAtletas extends AbaDashboard {

    private final Cartoes.Numero ativos = new Cartoes.Numero("Atletas ativos");
    private final Cartoes.Numero novosNoAno = new Cartoes.Numero("Novos cadastros no ano");
    private final Cartoes.Numero cursos = new Cartoes.Numero("Cursos representados");
    private final GraficoBarras porModalidade = new GraficoBarras(Grafico.CORES_SERIES[0]);
    private final GraficoPizza porCurso = new GraficoPizza();
    private final GraficoColunas porFase = new GraficoColunas("Atletas", Grafico.CORES_SERIES[1]);
    private final GraficoLinha cadastros = new GraficoLinha(Grafico.CORES_SERIES[3], "atleta(s)");

    AbaAtletas() {
        adicionarLinha(ativos, novosNoAno, cursos);
        adicionarLinha(
                Cartoes.comTitulo("Atletas por modalidade", "Atletas ativos; quem pratica mais de uma conta em cada", porModalidade),
                Cartoes.comTitulo("Novos cadastros por mês", "Últimos " + DashboardController.MESES + " meses", cadastros));
        adicionarLinha(
                Cartoes.comTitulo("Distribuição por curso", "Atletas ativos", porCurso),
                Cartoes.comTitulo("Distribuição por fase", "Período/fase atual dos atletas ativos (N/I = não informada)", porFase));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        Map<String, Integer> porCursoDados = controller.atletasPorCurso();
        Map<String, Integer> cadastrosDados = controller.novosCadastrosPorMes(hoje);
        ativos.mostrar(controller.totais(hoje).atletasAtivos(), "com situação Ativo");
        novosNoAno.mostrar(controller.novosCadastrosNoAno(hoje), "cadastrados em " + hoje.getYear());
        cursos.mostrar((int) porCursoDados.keySet().stream().filter(c -> !c.equals("Não informado")).count(),
                "com ao menos um atleta ativo");
        porModalidade.mostrar(controller.atletasPorModalidade());
        porCurso.mostrar(porCursoDados);
        porFase.mostrarSerieUnica(controller.atletasPorFase());
        cadastros.mostrar(cadastrosDados);
    }
}
