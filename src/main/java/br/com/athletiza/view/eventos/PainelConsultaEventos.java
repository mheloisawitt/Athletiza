package br.com.athletiza.view.eventos;

import br.com.athletiza.controller.EventoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.PainelTarefas;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComboBox;

/**
 * Consulta dos eventos organizados pela atlética (CH-39), com filtro por tipo e período.
 * A coluna "Tarefas" resume o andamento e avisa sobre tarefas atrasadas.
 */
public class PainelConsultaEventos extends PainelConsulta<Evento> {

    /** Filtro de período. */
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

    private final EventoController controller = new EventoController();
    private final Navegador navegador;
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
                EnumSet.allOf(Acao.class));
        this.navegador = navegador;
        renomearBotao(Acao.GERENCIAR, "Responsáveis e tarefas");
        larguraColuna(0, 190);
        tabela.getColumnModel().getColumn(1).setMinWidth(95);
        larguraColuna(6, 240);
        adicionarFiltro(filtroTipo);
        adicionarFiltro(filtroPeriodo);
        filtroTipo.addActionListener(e -> carregar());
        filtroPeriodo.addActionListener(e -> carregar());
    }

    @Override
    protected List<Evento> buscarDados() throws AthletizaException {
        LocalDate hoje = LocalDate.now();
        Periodo periodo = (Periodo) filtroPeriodo.getSelectedItem();
        return controller.pesquisar((TipoEvento) filtroTipo.getSelectedItem(),
                periodo == Periodo.PROXIMOS ? hoje : null,
                periodo == Periodo.REALIZADOS ? hoje.minusDays(1) : null);
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroEvento(new Evento(), navegador));
    }

    @Override
    protected void editar(Evento evento) {
        navegador.abrir(new PainelCadastroEvento(evento, navegador));
    }

    @Override
    protected void gerenciar(Evento evento) {
        navegador.abrir(new PainelGerenciarEvento(evento, navegador));
    }

    @Override
    protected void excluir(Evento evento) {
        if (!evento.getTarefas().isEmpty() && !Mensagens.confirmar(this, evento.getTitulo() + " possui "
                + evento.getTarefas().size() + " tarefa(s), que também serão excluídas, assim como os responsáveis."
                + " Continuar?")) {
            return;
        }
        try {
            controller.excluir(evento);
            Mensagens.sucesso(this, "Evento excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
