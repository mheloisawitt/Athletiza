package br.com.athletiza.model;

import java.util.List;

public record DadosDashboard(Resumo resumo, List<Indicador> atletasPorModalidade,
        List<Indicador> presencasPorModalidade, List<Indicador> treinosPorModalidade,
        List<Frequencia> menosFrequentes, List<Indicador> demandasPorSetor,
        LinhaDoTempo linhaDoTempo, Distribuicao distribuicao) {
    public record LinhaDoTempo(Comparativo atividadesPorMes, Comparativo presencaPorMes,
            Comparativo presencasEFaltasPorMes, List<Atividade> proximas) {
    }

    public record Distribuicao(List<Indicador> atletasPorSituacao, List<Indicador> eventosPorTipo,
            List<Indicador> demandasPorSituacao, List<Indicador> demandasPorPrioridade,
            Comparativo podiosPorModalidade) {
    }

    public record Resumo(int atletasAtivos, int treinos, int amistosos, int percentualPresenca,
            int demandasAbertas, int demandasAtrasadas) {
    }
}
