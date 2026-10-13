package br.com.athletiza.view.atletas;

import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;

/**
 * Consulta das modalidades esportivas (CH-27), aberta a partir da consulta de atletas.
 * As ações ficam no ConsultaModalidadesController.
 */
public class PainelConsultaModalidades extends PainelConsulta<Modalidade> {

    public PainelConsultaModalidades() {
        super("Modalidades", new ModeloTabela<Modalidade>()
                .coluna("Modalidade", String.class, m -> m.getNome())
                .coluna("Gênero", String.class, m -> m.getGenero().getNomeCompleto()));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
    }
}
