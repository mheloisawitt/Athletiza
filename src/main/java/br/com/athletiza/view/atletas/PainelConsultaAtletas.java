package br.com.athletiza.view.atletas;

import br.com.athletiza.controller.AtletaController;
import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComboBox;

/**
 * Consulta de atletas (CH-25): filtro por modalidade e situação, busca por texto
 * e ordenação escolhida pelo usuário (Comparators de Atleta).
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

    private final AtletaController controller = new AtletaController();
    private final ModalidadeController modalidadeController = new ModalidadeController();
    private final Navegador navegador;
    private final JComboBox<Modalidade> filtroModalidade = Combos.comOpcaoTodos(List.of(), "Todas as modalidades");
    private final JComboBox<Situacao> filtroSituacao = Combos.comOpcaoTodos(List.of(Situacao.values()), "Todas as situações");
    private final JComboBox<Ordem> ordem = new JComboBox<>(Ordem.values());
    private boolean atualizandoFiltros;

    public PainelConsultaAtletas(Navegador navegador) {
        super("Atletas", new ModeloTabela<Atleta>()
                .coluna("Nome", String.class, Atleta::getNome)
                .coluna("Matrícula", String.class, Atleta::getMatricula)
                .coluna("Modalidades", String.class, a -> a.getModalidades().stream()
                        .map(Modalidade::toString).collect(Collectors.joining(", ")))
                .coluna("Contato", String.class, Atleta::getContato)
                .coluna("Situação", Situacao.class, Atleta::getSituacao));
        this.navegador = navegador;
        larguraColuna(0, 220);
        larguraColuna(1, 100);
        larguraColuna(2, 220);
        larguraColuna(4, 110);

        adicionarFiltro(filtroModalidade);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(ordem);
        filtroModalidade.addActionListener(e -> recarregarPorFiltro());
        filtroSituacao.addActionListener(e -> recarregarPorFiltro());
        ordem.addActionListener(e -> {
            limparOrdenacaoDaTabela();
            recarregarPorFiltro();
        });

        adicionarBotao(Botoes.contornoVerde("Modalidades", e -> navegador.abrir(new PainelConsultaModalidades(navegador))));
    }

    @Override
    protected List<Atleta> buscarDados() throws AthletizaException {
        atualizarOpcoesDeModalidade();
        Ordem ordemEscolhida = (Ordem) ordem.getSelectedItem();
        return controller.pesquisar(null, (Modalidade) filtroModalidade.getSelectedItem(),
                (Situacao) filtroSituacao.getSelectedItem(), ordemEscolhida.comparador);
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroAtleta(new Atleta(), navegador));
    }

    @Override
    protected void editar(Atleta atleta) {
        navegador.abrir(new PainelCadastroAtleta(atleta, navegador));
    }

    @Override
    protected void excluir(Atleta atleta) {
        try {
            controller.excluir(atleta);
            Mensagens.sucesso(this, "Atleta excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }

    /** As modalidades podem ter mudado no cadastro de modalidades; mantém a seleção atual. */
    private void atualizarOpcoesDeModalidade() throws AthletizaException {
        atualizandoFiltros = true;
        try {
            Combos.atualizarItens(filtroModalidade, modalidadeController.listar());
        } finally {
            atualizandoFiltros = false;
        }
    }

    private void recarregarPorFiltro() {
        if (!atualizandoFiltros) {
            carregar();
        }
    }
}
