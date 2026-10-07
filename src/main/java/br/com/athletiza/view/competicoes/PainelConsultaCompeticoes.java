package br.com.athletiza.view.competicoes;

import br.com.athletiza.controller.CompeticaoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import br.com.athletiza.view.componentes.PainelTarefas;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JComboBox;

/**
 * Consulta das competições, ordenadas por data (CH-29). "Gerenciar" abre as
 * inscrições e os resultados da competição selecionada.
 */
public class PainelConsultaCompeticoes extends PainelConsulta<Competicao> {

    /** Filtro de período da consulta. */
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

    private final CompeticaoController controller = new CompeticaoController();
    private final Navegador navegador;
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
                EnumSet.allOf(Acao.class));
        this.navegador = navegador;
        larguraColuna(0, 170);
        tabela.getColumnModel().getColumn(5).setMinWidth(140);
        tabela.getColumnModel().getColumn(6).setMinWidth(215);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(filtroPeriodo);
        filtroSituacao.addActionListener(e -> carregar());
        filtroPeriodo.addActionListener(e -> carregar());
    }

    @Override
    protected List<Competicao> buscarDados() throws AthletizaException {
        LocalDate hoje = LocalDate.now();
        Periodo periodo = (Periodo) filtroPeriodo.getSelectedItem();
        return controller.pesquisar((SituacaoCompeticao) filtroSituacao.getSelectedItem(),
                periodo == Periodo.PROXIMAS ? hoje : null,
                periodo == Periodo.ANTERIORES ? hoje.minusDays(1) : null);
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroCompeticao(new Competicao(), navegador));
    }

    @Override
    protected void editar(Competicao competicao) {
        navegador.abrir(new PainelCadastroCompeticao(competicao, navegador));
    }

    @Override
    protected void gerenciar(Competicao competicao) {
        navegador.abrir(new PainelGerenciarCompeticao(competicao, navegador));
    }

    /** Avisa que inscrições e resultados também serão apagados (CH-29). */
    @Override
    protected void excluir(Competicao competicao) {
        try {
            controller.carregarInscricoes(competicao);
            int inscricoes = competicao.getTotalInscricoes();
            int resultados = competicao.getResultados().size();
            if ((inscricoes > 0 || resultados > 0) && !Mensagens.confirmar(this, competicao.getTitulo() + " possui "
                    + inscricoes + " inscrição(ões) de atletas e " + resultados + " resultado(s).\n"
                    + "Eles também serão excluídos. Continuar?")) {
                return;
            }
            controller.excluir(competicao);
            Mensagens.sucesso(this, "Competição excluída.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
