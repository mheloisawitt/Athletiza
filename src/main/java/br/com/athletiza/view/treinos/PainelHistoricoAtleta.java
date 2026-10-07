package br.com.athletiza.view.treinos;

import br.com.athletiza.controller.TreinoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Presenca;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Histórico de um atleta (RF13): em quais treinos esteve presente ou ausente,
 * com os mesmos filtros de modalidade e período da tela de frequência.
 */
public class PainelHistoricoAtleta extends PainelConsulta<PresencaEmTreino> {

    private final TreinoController controller = new TreinoController();
    private final Atleta atleta;
    private final Modalidade modalidade;
    private final LocalDate inicio;

    public PainelHistoricoAtleta(Atleta atleta, Modalidade modalidade, LocalDate inicio, Navegador navegador) {
        super("Treinos de " + atleta.getNome(), new ModeloTabela<PresencaEmTreino>()
                .coluna("Data", LocalDate.class, p -> p.treino().getData())
                .coluna("Horário", LocalTime.class, p -> p.treino().getHorario())
                .coluna("Treino", String.class, p -> p.treino().getTitulo())
                .coluna("Tipo", String.class, p -> p.treino().getTipo().getDescricao())
                .coluna("Local", String.class, p -> p.treino().getLocal())
                .coluna("Presença", Presenca.class, PresencaEmTreino::presenca),
                EnumSet.noneOf(Acao.class));
        this.atleta = atleta;
        this.modalidade = modalidade;
        this.inicio = inicio;
        exibirVoltar(navegador::voltar);
        larguraColuna(2, 260);
    }

    @Override
    protected List<PresencaEmTreino> buscarDados() throws AthletizaException {
        return controller.historicoDoAtleta(atleta, modalidade, inicio);
    }

    @Override
    protected void aoClicarDuasVezes(PresencaEmTreino selecionado) {
        // Apenas consulta.
    }
}
