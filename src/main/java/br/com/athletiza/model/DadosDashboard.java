package br.com.athletiza.model;

import java.util.List;

public record DadosDashboard(Resumo resumo, List<Indicador> atletasPorModalidade,
        List<Indicador> presencasPorModalidade, List<Indicador> treinosPorModalidade,
        List<Frequencia> menosFrequentes, List<Indicador> demandasPorSetor) {
    public record Resumo(int atletasAtivos, int treinos, int amistosos, int percentualPresenca,
            int demandasAbertas, int demandasAtrasadas) {
    }
}
