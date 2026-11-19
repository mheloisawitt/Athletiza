package br.com.athletiza.visao.treinos;

import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Presenca;
import br.com.athletiza.modelo.PresencaEmTreino;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;

public class PainelHistoricoAtleta extends PainelConsulta<PresencaEmTreino> {

    public PainelHistoricoAtleta(Atleta atleta, Navegador navegador) {
        super("Treinos de " + atleta.getNome(), new ModeloTabela<PresencaEmTreino>()
                .coluna("Data", LocalDate.class, p -> p.treino().getData())
                .coluna("Horário", LocalTime.class, p -> p.treino().getHorario())
                .coluna("Treino", String.class, p -> p.treino().getTitulo())
                .coluna("Tipo", String.class, p -> p.treino().getTipo().getDescricao())
                .coluna("Local", String.class, p -> p.treino().getLocal())
                .coluna("Presença", Presenca.class, PresencaEmTreino::presenca),
                EnumSet.noneOf(Acao.class), navegador);
        adicionarAcaoDuploClique(() -> { });
        exibirVoltar(navegador::voltar);
        larguraColuna(2, 260);
    }
}
