package br.com.athletiza.view.eventos;

import br.com.athletiza.model.Evento;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import javax.swing.JComboBox;

/**
 * Consulta dos eventos organizados pela atlética (CH-39), com filtro por tipo e período.
 * A coluna "Tarefas" resume o andamento e avisa sobre tarefas atrasadas.
 * As ações ficam no ConsultaEventosController.
 */
public class PainelConsultaEventos extends PainelConsulta<Evento> {

    /** Filtro de período. */
    public enum Periodo {
        TODOS("Todas as datas"), PROXIMOS("Próximos"), REALIZADOS("Já realizados");

        private final String texto;

        Periodo(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final JComboBox<TipoEvento> filtroTipo =
            Combos.comOpcaoTodos(Arrays.asList(TipoEvento.values()), "Todos os tipos");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    public PainelConsultaEventos() {
        super("Eventos", new ModeloTabela<Evento>()
                .coluna("Evento", String.class, e -> e.getTitulo())
                .coluna("Tipo", String.class, e -> e.getTipoEvento().getDescricao())
                .coluna("Data", LocalDate.class, e -> e.getData())
                .coluna("Horário", LocalTime.class, e -> e.getHorario())
                .coluna("Local", String.class, e -> e.getLocal())
                .coluna("Situação", SituacaoEvento.class, e -> e.getSituacao())
                .coluna("Tarefas", String.class, e -> Tarefa.resumir(e.getTarefas(), LocalDate.now())),
                EnumSet.allOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        renomearBotao(Acao.GERENCIAR, "Responsáveis e tarefas");
        larguraColuna(0, 190);
        tabela.getColumnModel().getColumn(1).setMinWidth(95);
        larguraColuna(6, 240);
        adicionarFiltro(filtroTipo);
        adicionarFiltro(filtroPeriodo);
    }

    public void adicionarAcaoFiltrar(ActionListener acao) {
        filtroTipo.addActionListener(acao);
        filtroPeriodo.addActionListener(acao);
    }

    /** Tipo escolhido no filtro, ou null para todos. */
    public TipoEvento getTipo() {
        return (TipoEvento) filtroTipo.getSelectedItem();
    }

    public Periodo getPeriodo() {
        return (Periodo) filtroPeriodo.getSelectedItem();
    }
}
