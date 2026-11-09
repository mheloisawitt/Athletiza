package br.com.athletiza.model;

import java.util.List;

/**
 * Tudo o que o dashboard exibe, calculado para um período.
 *
 * @param resumo                números gerais do topo da tela
 * @param atletasPorModalidade  modalidades com mais atletas ativos
 * @param presencasPorModalidade modalidades com mais atletas presentes nos treinos
 * @param treinosPorModalidade  modalidades que mais realizaram treinos e amistosos (parcial = amistosos)
 * @param menosFrequentes       atletas que menos participam dos treinos (menor frequência primeiro)
 * @param demandasPorSetor      setores com mais demandas em aberto (parcial = atrasadas)
 * @param linhaDoTempo          evolução por mês e próximas atividades
 * @param distribuicao          como atletas, eventos e demandas se dividem (pizzas) e pódios
 */
public record DadosDashboard(Resumo resumo, List<Indicador> atletasPorModalidade,
        List<Indicador> presencasPorModalidade, List<Indicador> treinosPorModalidade,
        List<Frequencia> menosFrequentes, List<Indicador> demandasPorSetor,
        LinhaDoTempo linhaDoTempo, Distribuicao distribuicao) {

    /**
     * Aba "Linha do tempo".
     *
     * @param atividadesPorMes      treinos, amistosos, competições e eventos de cada mês
     * @param presencaPorMes        percentual de presença nos treinos de cada mês (nulo = sem lista)
     * @param presencasEFaltasPorMes presenças e faltas registradas em cada mês
     * @param proximas              atividades dos próximos dias, por data
     */
    public record LinhaDoTempo(Comparativo atividadesPorMes, Comparativo presencaPorMes,
            Comparativo presencasEFaltasPorMes, List<Atividade> proximas) {
    }

    /**
     * Aba "Distribuição".
     *
     * @param atletasPorSituacao     vínculos de atletas com modalidades, por situação
     * @param eventosPorTipo         eventos do período (não cancelados), por tipo
     * @param demandasPorSituacao    todas as demandas, por situação
     * @param demandasPorPrioridade  demandas abertas ou em andamento, por prioridade
     * @param podiosPorModalidade    1º, 2º e 3º lugares de cada modalidade nas competições do período
     */
    public record Distribuicao(List<Indicador> atletasPorSituacao, List<Indicador> eventosPorTipo,
            List<Indicador> demandasPorSituacao, List<Indicador> demandasPorPrioridade,
            Comparativo podiosPorModalidade) {
    }

    /** Números gerais do período. */
    public record Resumo(int atletasAtivos, int treinos, int amistosos, int percentualPresenca,
            int demandasAbertas, int demandasAtrasadas) {
    }
}
