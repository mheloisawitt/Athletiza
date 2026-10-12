package br.com.athletiza.view.demandas;

import br.com.athletiza.controller.DemandaController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JComboBox;

/**
 * Consulta das demandas da atlética, com filtro por situação e por responsável.
 * As pendentes aparecem primeiro, por prioridade e prazo.
 */
public class PainelConsultaDemandas extends PainelConsulta<Demanda> {

    private final DemandaController controller = new DemandaController();
    private final Navegador navegador;
    private final JComboBox<SituacaoDemanda> filtroSituacao =
            Combos.comOpcaoTodos(List.of(SituacaoDemanda.values()), "Todas as situações");
    private final JComboBox<Membro> filtroResponsavel = Combos.comOpcaoTodos(List.of(), "Todos os responsáveis");

    public PainelConsultaDemandas(Navegador navegador) {
        super("Demandas", new ModeloTabela<Demanda>()
                .coluna("Demanda", String.class, Demanda::getTitulo)
                .coluna("Relacionada a", String.class, Demanda::getDescricaoRelacionada)
                .coluna("Responsáveis", String.class, Demanda::getNomesResponsaveis)
                .coluna("Prazo", LocalDate.class, Demanda::getPrazo)
                .coluna("Prioridade", PrioridadeDemanda.class, Demanda::getPrioridade)
                .coluna("Situação", SituacaoDemanda.class, Demanda::getSituacao));
        this.navegador = navegador;
        larguraColuna(0, 220);
        larguraColuna(1, 230);
        larguraColuna(2, 200);
        tabela.getColumnModel().getColumn(3).setMinWidth(110);
        tabela.getColumnModel().getColumn(4).setMinWidth(95);
        tabela.getColumnModel().getColumn(5).setMinWidth(135);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(filtroResponsavel);
        filtroSituacao.addActionListener(e -> carregar());
        filtroResponsavel.addActionListener(e -> carregar());
    }

    @Override
    protected List<Demanda> buscarDados() throws AthletizaException {
        Combos.atualizarItens(filtroResponsavel, new MembroController().listar());
        return controller.pesquisar((SituacaoDemanda) filtroSituacao.getSelectedItem(),
                (Membro) filtroResponsavel.getSelectedItem());
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroDemanda(new Demanda(), navegador));
    }

    @Override
    protected void editar(Demanda demanda) {
        navegador.abrir(new PainelCadastroDemanda(demanda, navegador));
    }

    @Override
    protected void excluir(Demanda demanda) {
        try {
            controller.excluir(demanda);
            Mensagens.sucesso(this, "Demanda excluída.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
