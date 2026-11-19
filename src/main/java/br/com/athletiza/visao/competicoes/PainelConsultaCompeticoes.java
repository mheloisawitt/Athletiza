package br.com.athletiza.visao.competicoes;

import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.SituacaoCompeticao;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import br.com.athletiza.visao.componentes.PainelTarefas;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComboBox;

public class PainelConsultaCompeticoes extends PainelConsulta<Competicao> {
    private enum Periodo {
        TODAS("Todas as datas"), PROXIMAS("Próximas e em andamento"), ANTERIORES("Já iniciadas");

        private final String texto;

        Periodo(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final JComboBox<SituacaoCompeticao> filtroSituacao =
            Combos.comOpcaoTodos(List.of(SituacaoCompeticao.values()), "Todas as situações");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    public PainelConsultaCompeticoes(Navegador navegador) {
        super("Competições", new ModeloTabela<Competicao>()
                .coluna("Competição", String.class, Competicao::getTitulo)
                .coluna("Início", LocalDate.class, Competicao::getData)
                .coluna("Fim", LocalDate.class, Competicao::getDataFim)
                .coluna("Local", String.class, Competicao::getLocal)
                .coluna("Situação", SituacaoCompeticao.class, Competicao::getSituacao)
                .coluna("Melhor resultado", String.class, Competicao::getMelhorResultado)
                .coluna("Tarefas", String.class, c -> PainelTarefas.resumir(c.getTarefas())),
                EnumSet.allOf(Acao.class), navegador);
        larguraColuna(0, 170);
        tabela.getColumnModel().getColumn(5).setMinWidth(140);
        tabela.getColumnModel().getColumn(6).setMinWidth(215);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(filtroPeriodo);
        filtroSituacao.addActionListener(e -> carregar());
        filtroPeriodo.addActionListener(e -> carregar());
    }

    public SituacaoCompeticao getSituacaoFiltro() {
        return (SituacaoCompeticao) filtroSituacao.getSelectedItem();
    }

    public boolean isSomenteProximas() {
        return filtroPeriodo.getSelectedItem() == Periodo.PROXIMAS;
    }

    public boolean isSomenteAnteriores() {
        return filtroPeriodo.getSelectedItem() == Periodo.ANTERIORES;
    }
}
