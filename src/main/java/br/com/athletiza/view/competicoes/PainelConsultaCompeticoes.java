package br.com.athletiza.view.competicoes;

import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import javax.swing.JComboBox;

/**
 * Consulta das competições, ordenadas por data (CH-29). "Gerenciar" abre as
 * inscrições e os resultados da competição selecionada.
 * As ações ficam no ConsultaCompeticoesController.
 */
public class PainelConsultaCompeticoes extends PainelConsulta<Competicao> {

    /** Filtro de período da consulta. */
    public enum Periodo {
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
            Combos.comOpcaoTodos(Arrays.asList(SituacaoCompeticao.values()), "Todas as situações");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    public PainelConsultaCompeticoes() {
        super("Competições", new ModeloTabela<Competicao>()
                .coluna("Competição", String.class, c -> c.getTitulo())
                .coluna("Início", LocalDate.class, c -> c.getData())
                .coluna("Fim", LocalDate.class, c -> c.getDataFim())
                .coluna("Local", String.class, c -> c.getLocal())
                .coluna("Situação", SituacaoCompeticao.class, c -> c.getSituacao())
                .coluna("Melhor resultado", String.class, c -> c.getMelhorResultado())
                .coluna("Tarefas", String.class, c -> Tarefa.resumir(c.getTarefas(), LocalDate.now())),
                EnumSet.allOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        larguraColuna(0, 170);
        tabela.getColumnModel().getColumn(5).setMinWidth(140);
        tabela.getColumnModel().getColumn(6).setMinWidth(215);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(filtroPeriodo);
    }

    /** Ação executada quando um filtro muda. */
    public void adicionarAcaoFiltrar(ActionListener acao) {
        filtroSituacao.addActionListener(acao);
        filtroPeriodo.addActionListener(acao);
    }

    /** Situação escolhida no filtro, ou null para todas. */
    public SituacaoCompeticao getSituacao() {
        return (SituacaoCompeticao) filtroSituacao.getSelectedItem();
    }

    public Periodo getPeriodo() {
        return (Periodo) filtroPeriodo.getSelectedItem();
    }
}
