package br.com.athletiza.view.dashboard;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Situacao;
import java.time.LocalDate;
import java.util.Map;

/**
 * Aba Atletas: totais, atletas por modalidade, por faixa etária e a situação
 * dos atletas nas modalidades. Usa apenas os dados já cadastrados.
 */
class AbaAtletas extends AbaDashboard {

    private final Cartoes.Numero ativos = new Cartoes.Numero("Atletas ativos");
    private final Cartoes.Numero inativos = new Cartoes.Numero("Atletas inativos");
    private final Cartoes.Numero vinculos = new Cartoes.Numero("Vínculos com modalidades");
    private final GraficoBarras porModalidade = new GraficoBarras(Grafico.CORES_SERIES[0]);
    private final GraficoColunas porIdade = new GraficoColunas("Atletas", Grafico.CORES_SERIES[1]);
    private final GraficoPizza situacoes = new GraficoPizza();

    AbaAtletas() {
        adicionarLinha(ativos, inativos, vinculos);
        adicionarLinha(
                Cartoes.comTitulo("Atletas por modalidade", "Atletas ativos; quem pratica mais de uma conta em cada", porModalidade),
                Cartoes.comTitulo("Situação nas modalidades", "Atletas ativos: ativo, lesionado, afastado ou inativo em cada modalidade", situacoes));
        adicionarLinha(
                Cartoes.comTitulo("Distribuição por faixa etária", "Idade dos atletas ativos (N/I = sem data de nascimento)", porIdade));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        Map<Situacao, Integer> porSituacao = controller.atletasPorSituacao();
        Map<String, Integer> situacaoDados = controller.situacaoNasModalidades();
        ativos.mostrar(porSituacao.get(Situacao.ATIVO), "com situação Ativo");
        inativos.mostrar(porSituacao.get(Situacao.INATIVO), "não aparecem nos gráficos");
        vinculos.mostrar(situacaoDados.values().stream().mapToInt(Integer::intValue).sum(), "dos atletas ativos");
        porModalidade.mostrar(controller.atletasPorModalidade());
        porIdade.mostrarSerieUnica(controller.atletasPorFaixaEtaria(hoje));
        situacoes.mostrar(situacaoDados);
    }
}
