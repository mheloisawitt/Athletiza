package br.com.athletiza.view.treinos;

import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.controller.TreinoController;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Consulta de treinos e amistosos (CH-34): modalidades à esquerda e os treinos
 * da modalidade à direita, com filtros de tipo e período.
 */
public class PainelTreinos extends PainelConsulta<Treino> {

    /** Filtro de período. */
    private enum Periodo {
        TODOS("Todas as datas"), PROXIMOS("Próximos"), REALIZADOS("Realizados");

        private final String texto;

        Periodo(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final TreinoController controller = new TreinoController();
    private final Navegador navegador;
    private final DefaultListModel<Modalidade> modalidades = new DefaultListModel<>();
    private final JList<Modalidade> listaModalidades = new JList<>(modalidades);
    private final JComboBox<TipoAtividade> filtroTipo =
            Combos.comOpcaoTodos(List.of(TipoAtividade.TREINO, TipoAtividade.AMISTOSO), "Treinos e amistosos");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());
    /** Resumo de presença de cada treino; compartilhado com a coluna "Presença" da tabela. */
    private final AtomicReference<Map<Integer, TreinoDAO.ResumoPresenca>> presencas;
    private boolean atualizandoLista;

    public PainelTreinos(Navegador navegador) {
        this(navegador, new AtomicReference<>(Map.of()));
    }

    private PainelTreinos(Navegador navegador, AtomicReference<Map<Integer, TreinoDAO.ResumoPresenca>> presencas) {
        super("Treinos", new ModeloTabela<Treino>()
                .coluna("Data", LocalDate.class, Treino::getData)
                .coluna("Horário", LocalTime.class, Treino::getHorario)
                .coluna("Tipo", String.class, t -> t.getTipo().getDescricao())
                .coluna("Título", String.class, Treino::getTitulo)
                .coluna("Local", String.class, Treino::getLocal)
                .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome())
                .coluna("Presença", String.class, t -> {
                    TreinoDAO.ResumoPresenca resumo = presencas.get().get(t.getId());
                    return resumo == null ? "—" : resumo.presentes() + " de " + resumo.registrados();
                }),
                EnumSet.allOf(Acao.class));
        this.navegador = navegador;
        this.presencas = presencas;
        renomearBotao(Acao.INCLUIR, "+ Treino");
        renomearBotao(Acao.GERENCIAR, "Presença");
        JButton amistoso = Botoes.destaque("+ Amistoso", e -> incluir(new Amistoso()));
        amistoso.setEnabled(Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados());
        adicionarBotao(amistoso);
        adicionarBotao(Botoes.contorno("Frequência", e -> navegador.abrir(new PainelFrequencia(navegador))));
        tabela.getColumnModel().getColumn(0).setMinWidth(100);
        tabela.getColumnModel().getColumn(1).setMinWidth(70);
        tabela.getColumnModel().getColumn(2).setMinWidth(85);
        tabela.getColumnModel().getColumn(6).setMinWidth(85);
        larguraColuna(3, 240);

        adicionarFiltro(filtroTipo);
        adicionarFiltro(filtroPeriodo);
        filtroTipo.addActionListener(e -> carregar());
        filtroPeriodo.addActionListener(e -> carregar());
        adicionarPainelLateral(criarListaModalidades());
    }

    private JPanel criarListaModalidades() {
        listaModalidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaModalidades.setFixedCellHeight(34);
        listaModalidades.setBackground(Cores.FUNDO_CARTAO);
        javax.swing.ListCellRenderer<? super Modalidade> padrao = listaModalidades.getCellRenderer();
        listaModalidades.setCellRenderer((lista, modalidade, indice, selecionada, foco) -> {
            @SuppressWarnings("unchecked")
            JLabel rotulo = (JLabel) ((javax.swing.ListCellRenderer<Object>) (javax.swing.ListCellRenderer<?>) padrao)
                    .getListCellRendererComponent(lista, modalidade == null ? "Todas" : modalidade.toString(),
                            indice, selecionada, false);
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (selecionada) {
                rotulo.setBackground(Cores.VERDE_SELECAO);
                rotulo.setForeground(Cores.VERDE);
            }
            return rotulo;
        });
        listaModalidades.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizandoLista) {
                carregar();
            }
        });

        JLabel titulo = new JLabel("Modalidades");
        titulo.setFont(Tema.fonte(Font.BOLD, 13f));
        titulo.setForeground(Cores.TEXTO_SECUNDARIO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        JScrollPane rolagem = new JScrollPane(listaModalidades);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setOpaque(false);
        lateral.setPreferredSize(new Dimension(180, 0));
        lateral.add(titulo, BorderLayout.NORTH);
        lateral.add(rolagem, BorderLayout.CENTER);
        return lateral;
    }

    @Override
    protected List<Treino> buscarDados() throws AthletizaException {
        atualizarModalidades();
        LocalDate hoje = LocalDate.now();
        Periodo periodo = (Periodo) filtroPeriodo.getSelectedItem();
        presencas.set(controller.resumirPresencas());
        return controller.pesquisar(listaModalidades.getSelectedValue(),
                periodo == Periodo.PROXIMOS ? hoje : null,
                periodo == Periodo.REALIZADOS ? hoje.minusDays(1) : null,
                (TipoAtividade) filtroTipo.getSelectedItem());
    }

    /** Recarrega as modalidades da lista lateral, mantendo a selecionada. */
    private void atualizarModalidades() throws AthletizaException {
        Modalidade selecionada = listaModalidades.getSelectedValue();
        int indiceSelecionado = listaModalidades.getSelectedIndex();
        atualizandoLista = true;
        try {
            modalidades.clear();
            modalidades.addElement(null);
            new ModalidadeController().listar().forEach(modalidades::addElement);
            int indice = selecionada == null ? 0 : modalidades.indexOf(selecionada);
            listaModalidades.setSelectedIndex(indiceSelecionado < 0 ? 0 : Math.max(indice, 0));
        } finally {
            atualizandoLista = false;
        }
    }

    private TreinoDAO.ResumoPresenca presencasDoTreino(Treino treino) {
        return presencas.get().get(treino.getId());
    }

    @Override
    protected void incluir() {
        incluir(new Treino());
    }

    private void incluir(Treino novo) {
        novo.setModalidade(listaModalidades.getSelectedValue());
        navegador.abrir(new PainelCadastroTreino(novo, navegador));
    }

    @Override
    protected void editar(Treino treino) {
        navegador.abrir(new PainelCadastroTreino(treino, navegador));
    }

    @Override
    protected void gerenciar(Treino treino) {
        navegador.abrir(new PainelPresenca(treino, navegador));
    }

    @Override
    protected void excluir(Treino treino) {
        TreinoDAO.ResumoPresenca resumo = presencasDoTreino(treino);
        if (resumo != null && !Mensagens.confirmar(this, "Este treino tem " + resumo.registrados()
                + " registro(s) de presença, que também serão excluídos. Continuar?")) {
            return;
        }
        try {
            controller.excluir(treino);
            Mensagens.sucesso(this, "Treino excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
