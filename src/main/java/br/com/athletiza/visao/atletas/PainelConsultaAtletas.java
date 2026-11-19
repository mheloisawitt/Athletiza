package br.com.athletiza.visao.atletas;

import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;

public class PainelConsultaAtletas extends PainelConsulta<Atleta> {
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

    private final JComboBox<Modalidade> filtroModalidade = Combos.comOpcaoTodos(List.of(), "Todas as modalidades");
    private final JComboBox<Situacao> filtroSituacao = Combos.comOpcaoTodos(List.of(Situacao.values()), "Todas as situações");
    private final JComboBox<Ordem> ordem = new JComboBox<>(Ordem.values());
    private final JButton btnModalidades = Botoes.contornoVerde("Modalidades", null);
    private boolean atualizandoFiltros;

    public PainelConsultaAtletas(Navegador navegador) {
        super("Atletas", new ModeloTabela<Atleta>()
                .coluna("Nome", String.class, Atleta::getNome)
                .coluna("Matrícula", String.class, Atleta::getMatricula)
                .coluna("Modalidades", String.class, a -> a.getModalidades().stream()
                        .map(Modalidade::toString).collect(Collectors.joining(", ")))
                .coluna("Contato", String.class, Atleta::getContato)
                .coluna("Situação", Situacao.class, Atleta::getSituacao), navegador);
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
        adicionarBotao(btnModalidades);
    }

    public void adicionarAcaoBtnModalidades(ActionListener acao) {
        btnModalidades.addActionListener(acao);
    }

    public void apresentarModalidades(List<Modalidade> modalidades) {
        atualizandoFiltros = true;
        Combos.atualizarItens(filtroModalidade, modalidades);
        atualizandoFiltros = false;
    }

    public Modalidade getModalidadeFiltro() {
        return (Modalidade) filtroModalidade.getSelectedItem();
    }

    public Situacao getSituacaoFiltro() {
        return (Situacao) filtroSituacao.getSelectedItem();
    }

    public Comparator<Atleta> getOrdem() {
        return ((Ordem) ordem.getSelectedItem()).comparador;
    }

    private void recarregarPorFiltro() {
        if (!atualizandoFiltros) {
            carregar();
        }
    }
}
