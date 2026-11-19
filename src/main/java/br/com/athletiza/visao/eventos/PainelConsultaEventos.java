package br.com.athletiza.visao.eventos;

import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.SituacaoEvento;
import br.com.athletiza.modelo.TipoEvento;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import br.com.athletiza.visao.componentes.PainelTarefas;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComboBox;

public class PainelConsultaEventos extends PainelConsulta<Evento> {
    private enum Periodo {
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

    private final JComboBox<TipoEvento> filtroTipo = Combos.comOpcaoTodos(List.of(TipoEvento.values()), "Todos os tipos");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    public PainelConsultaEventos(Navegador navegador) {
        super("Eventos", new ModeloTabela<Evento>()
                .coluna("Evento", String.class, Evento::getTitulo)
                .coluna("Tipo", String.class, e -> e.getTipoEvento().getDescricao())
                .coluna("Data", LocalDate.class, Evento::getData)
                .coluna("Horário", LocalTime.class, Evento::getHorario)
                .coluna("Local", String.class, Evento::getLocal)
                .coluna("Situação", SituacaoEvento.class, Evento::getSituacao)
                .coluna("Tarefas", String.class, e -> PainelTarefas.resumir(e.getTarefas())),
                EnumSet.allOf(Acao.class), navegador);
        renomearBotao(Acao.GERENCIAR, "Responsáveis e tarefas");
        larguraColuna(0, 190);
        tabela.getColumnModel().getColumn(1).setMinWidth(95);
        larguraColuna(6, 240);
        adicionarFiltro(filtroTipo);
        adicionarFiltro(filtroPeriodo);
        filtroTipo.addActionListener(e -> carregar());
        filtroPeriodo.addActionListener(e -> carregar());
    }

    public TipoEvento getTipoFiltro() {
        return (TipoEvento) filtroTipo.getSelectedItem();
    }

    public boolean isSomenteProximos() {
        return filtroPeriodo.getSelectedItem() == Periodo.PROXIMOS;
    }

    public boolean isSomenteRealizados() {
        return filtroPeriodo.getSelectedItem() == Periodo.REALIZADOS;
    }
}
