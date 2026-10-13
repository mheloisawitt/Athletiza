package br.com.athletiza.view.atletas;

import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;

/**
 * Consulta de atletas (CH-25): filtro por modalidade e situação, busca por texto
 * e ordenação escolhida pelo usuário (Comparators de Atleta).
 * As ações ficam no ConsultaAtletasController.
 */
public class PainelConsultaAtletas extends PainelConsulta<Atleta> {

    /** Opções de ordenação, cada uma ligada a um Comparator de Atleta (CH-11). */
    private enum Ordem {
        NOME("Ordenar por nome", Atleta.POR_NOME),
        MATRICULA("Ordenar por matrícula", Atleta.POR_MATRICULA),
        MODALIDADE("Ordenar por modalidade", Atleta.POR_MODALIDADE),
        SITUACAO("Ordenar por situação", Atleta.POR_SITUACAO);

        private final String texto;
        private final Comparator<Atleta> comparador;

        Ordem(String texto, Comparator<Atleta> comparador) {
            this.texto = texto;
            this.comparador = comparador;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final JComboBox<Modalidade> filtroModalidade =
            Combos.comOpcaoTodos(new ArrayList<Modalidade>(), "Todas as modalidades");
    private final JComboBox<Situacao> filtroSituacao =
            Combos.comOpcaoTodos(Arrays.asList(Situacao.values()), "Todas as situações");
    private final JComboBox<Ordem> ordem = new JComboBox<>(Ordem.values());
    private final JButton btnModalidades = Botoes.contornoVerde("Modalidades", null);
    private ActionListener acaoFiltrar;
    private boolean atualizandoFiltros;

    public PainelConsultaAtletas() {
        super("Atletas", new ModeloTabela<Atleta>()
                .coluna("Nome", String.class, a -> a.getNome())
                .coluna("Matrícula", String.class, a -> a.getMatricula())
                .coluna("Modalidades", String.class, a -> a.getModalidadesTexto())
                .coluna("Contato", String.class, a -> a.getContato())
                .coluna("Situação", Situacao.class, a -> a.getSituacao()));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        larguraColuna(0, 220);
        larguraColuna(1, 100);
        larguraColuna(2, 220);
        larguraColuna(4, 110);

        adicionarFiltro(filtroModalidade);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(ordem);
        filtroModalidade.addActionListener(e -> filtrar());
        filtroSituacao.addActionListener(e -> filtrar());
        ordem.addActionListener(e -> {
            limparOrdenacaoDaTabela();
            filtrar();
        });
        adicionarBotao(btnModalidades);
    }

    /** Ação executada quando um filtro ou a ordenação mudam. */
    public void adicionarAcaoFiltrar(ActionListener acao) {
        this.acaoFiltrar = acao;
    }

    public void adicionarAcaoModalidades(ActionListener acao) {
        btnModalidades.addActionListener(acao);
    }

    /** Troca as opções do filtro de modalidade, mantendo a selecionada (sem disparar o filtro). */
    public void setModalidades(List<Modalidade> modalidades) {
        atualizandoFiltros = true;
        try {
            Combos.atualizarItens(filtroModalidade, modalidades);
        } finally {
            atualizandoFiltros = false;
        }
    }

    /** Modalidade escolhida no filtro, ou null para todas. */
    public Modalidade getModalidade() {
        return (Modalidade) filtroModalidade.getSelectedItem();
    }

    /** Situação escolhida no filtro, ou null para todas. */
    public Situacao getSituacao() {
        return (Situacao) filtroSituacao.getSelectedItem();
    }

    /** Critério de ordenação escolhido. */
    public Comparator<Atleta> getOrdem() {
        return ((Ordem) ordem.getSelectedItem()).comparador;
    }

    private void filtrar() {
        if (!atualizandoFiltros && acaoFiltrar != null) {
            acaoFiltrar.actionPerformed(null);
        }
    }
}
