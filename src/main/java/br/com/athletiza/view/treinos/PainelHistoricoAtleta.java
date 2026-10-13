package br.com.athletiza.view.treinos;

import br.com.athletiza.model.Presenca;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;

/**
 * Histórico de um atleta (RF13): em quais treinos esteve presente ou ausente,
 * com os mesmos filtros de modalidade e período da tela de frequência.
 * Apenas consulta; as ações ficam no HistoricoAtletaController.
 */
public class PainelHistoricoAtleta extends PainelConsulta<PresencaEmTreino> {

    public PainelHistoricoAtleta() {
        super("", new ModeloTabela<PresencaEmTreino>()
                .coluna("Data", LocalDate.class, p -> p.getTreino().getData())
                .coluna("Horário", LocalTime.class, p -> p.getTreino().getHorario())
                .coluna("Treino", String.class, p -> p.getTreino().getTitulo())
                .coluna("Tipo", String.class, p -> p.getTreino().getTipo().getDescricao())
                .coluna("Local", String.class, p -> p.getTreino().getLocal())
                .coluna("Presença", Presenca.class, p -> p.getPresenca()),
                EnumSet.noneOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
        larguraColuna(2, 260);
    }

    public void setNomeAtleta(String nome) {
        setTitulo("Treinos de " + nome);
    }
}
