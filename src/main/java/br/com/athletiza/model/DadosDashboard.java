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
 */
public record DadosDashboard(Resumo resumo, List<Indicador> atletasPorModalidade,
        List<Indicador> presencasPorModalidade, List<Indicador> treinosPorModalidade,
        List<Frequencia> menosFrequentes, List<Indicador> demandasPorSetor) {

    /** Números gerais do período. */
    public record Resumo(int atletasAtivos, int treinos, int amistosos, int percentualPresenca,
            int demandasAbertas, int demandasAtrasadas) {
    }
}
