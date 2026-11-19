package br.com.athletiza.visao.atletas;

import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;

public class PainelConsultaModalidades extends PainelConsulta<Modalidade> {

    public PainelConsultaModalidades(Navegador navegador) {
        super("Modalidades", new ModeloTabela<Modalidade>()
                .coluna("Modalidade", String.class, Modalidade::getNome)
                .coluna("Gênero", String.class, m -> m.getGenero().getNomeCompleto()), navegador);
        exibirVoltar(navegador::voltar);
    }
}
