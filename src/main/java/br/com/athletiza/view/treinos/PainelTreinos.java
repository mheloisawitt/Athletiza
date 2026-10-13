package br.com.athletiza.view.treinos;

import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.ResumoPresenca;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
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
 * As ações ficam no TreinosController.
 */
public class PainelTreinos extends PainelConsulta<Treino> {

    /** Filtro de período. */
    public enum Periodo {
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

    private final DefaultListModel<Modalidade> modalidades = new DefaultListModel<>();
    private final JList<Modalidade> listaModalidades = new JList<>(modalidades);
    private final JComboBox<TipoAtividade> filtroTipo = Combos.comOpcaoTodos(
            Arrays.asList(TipoAtividade.TREINO, TipoAtividade.AMISTOSO), "Treinos e amistosos");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());
    private final JButton btnAmistoso = Botoes.destaque("+ Amistoso", null);
    private final JButton btnFrequencia = Botoes.contorno("Frequência", null);
    /** Resumo de presença de cada treino (id do treino -> resumo), usado na coluna "Presença". */
    private Map<Integer, ResumoPresenca> presencas = new HashMap<>();
    private ActionListener acaoFiltrar;
    private boolean atualizandoLista;

    public PainelTreinos() {
        super("Treinos", new ModeloTabela<Treino>()
                .coluna("Data", LocalDate.class, t -> t.getData())
                .coluna("Horário", LocalTime.class, t -> t.getHorario())
                .coluna("Tipo", String.class, t -> t.getTipo().getDescricao())
                .coluna("Título", String.class, t -> t.getTitulo())
                .coluna("Local", String.class, t -> t.getLocal())
                .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome()),
                EnumSet.allOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // A coluna "Presença" usa o resumo guardado neste painel, por isso é criada depois do super(...)
        modelo.coluna("Presença", String.class, t -> textoPresenca(t));
        modelo.fireTableStructureChanged();

        renomearBotao(Acao.INCLUIR, "+ Treino");
        renomearBotao(Acao.GERENCIAR, "Presença");
        btnAmistoso.setEnabled(podeAlterar());
        adicionarBotao(btnAmistoso);
        adicionarBotao(btnFrequencia);
        tabela.getColumnModel().getColumn(0).setMinWidth(100);
        tabela.getColumnModel().getColumn(1).setMinWidth(70);
        tabela.getColumnModel().getColumn(2).setMinWidth(85);
        tabela.getColumnModel().getColumn(6).setMinWidth(85);
        larguraColuna(3, 240);

        adicionarFiltro(filtroTipo);
        adicionarFiltro(filtroPeriodo);
        filtroTipo.addActionListener(e -> filtrar());
        filtroPeriodo.addActionListener(e -> filtrar());
        adicionarPainelLateral(criarListaModalidades());
    }

    private JPanel criarListaModalidades() {
        listaModalidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaModalidades.setFixedCellHeight(34);
        listaModalidades.setBackground(Cores.FUNDO_CARTAO);
        DefaultListCellRenderer padrao = new DefaultListCellRenderer();
        listaModalidades.setCellRenderer((lista, modalidade, indice, selecionada, foco) -> {
            JLabel rotulo = (JLabel) padrao.getListCellRendererComponent(lista, modalidade, indice, selecionada, false);
            rotulo.setText(modalidade == null ? "Todas" : modalidade.toString());
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (selecionada) {
                rotulo.setBackground(Cores.VERDE_SELECAO);
                rotulo.setForeground(Cores.VERDE);
            }
            return rotulo;
        });
        listaModalidades.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                filtrar();
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

    /* Ações: registradas pelo Controller */

    /** Ação executada quando a modalidade da lista ou um filtro mudam. */
    public void adicionarAcaoFiltrar(ActionListener acao) {
        this.acaoFiltrar = acao;
    }

    public void adicionarAcaoAmistoso(ActionListener acao) {
        btnAmistoso.addActionListener(acao);
    }

    public void adicionarAcaoFrequencia(ActionListener acao) {
        btnFrequencia.addActionListener(acao);
    }

    /* Dados exibidos */

    /** Mostra os treinos e o resumo de presença de cada um. */
    public void exibir(List<Treino> treinos, Map<Integer, ResumoPresenca> resumoPresencas) {
        this.presencas = resumoPresencas;
        exibir(treinos);
    }

    /** Recarrega as modalidades da lista lateral ("Todas" primeiro), mantendo a selecionada. */
    public void setModalidades(List<Modalidade> todas) {
        Modalidade selecionada = listaModalidades.getSelectedValue();
        int indiceSelecionado = listaModalidades.getSelectedIndex();
        atualizandoLista = true;
        try {
            modalidades.clear();
            modalidades.addElement(null);
            for (Modalidade modalidade : todas) {
                modalidades.addElement(modalidade);
            }
            int indice = selecionada == null ? 0 : modalidades.indexOf(selecionada);
            listaModalidades.setSelectedIndex(indiceSelecionado < 0 ? 0 : Math.max(indice, 0));
        } finally {
            atualizandoLista = false;
        }
    }

    /** Modalidade selecionada na lista, ou null para "Todas". */
    public Modalidade getModalidade() {
        return listaModalidades.getSelectedValue();
    }

    /** TipoAtividade.TREINO, TipoAtividade.AMISTOSO ou null para ambos. */
    public TipoAtividade getTipo() {
        return (TipoAtividade) filtroTipo.getSelectedItem();
    }

    public Periodo getPeriodo() {
        return (Periodo) filtroPeriodo.getSelectedItem();
    }

    /** Resumo de presença do treino, ou null se ainda não houver lista. */
    public ResumoPresenca getResumoPresenca(Treino treino) {
        return presencas.get(treino.getId());
    }

    private String textoPresenca(Treino treino) {
        ResumoPresenca resumo = presencas.get(treino.getId());
        return resumo == null ? "—" : resumo.toString();
    }

    private void filtrar() {
        if (!atualizandoLista && acaoFiltrar != null) {
            acaoFiltrar.actionPerformed(null);
        }
    }
}
