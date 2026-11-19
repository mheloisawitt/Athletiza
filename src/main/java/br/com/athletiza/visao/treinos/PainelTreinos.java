package br.com.athletiza.visao.treinos;

import br.com.athletiza.repositorio.TreinoRepositorio;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.TipoAtividade;
import br.com.athletiza.modelo.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.visao.Tema;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class PainelTreinos extends PainelConsulta<Treino> {
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

    private final DefaultListModel<Modalidade> modalidades = new DefaultListModel<>();
    private final JList<Modalidade> listaModalidades = new JList<>(modalidades);
    private final JComboBox<TipoAtividade> filtroTipo =
            Combos.comOpcaoTodos(List.of(TipoAtividade.TREINO, TipoAtividade.AMISTOSO), "Treinos e amistosos");
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    private final JButton btnAmistoso = Botoes.destaque("+ Amistoso", null);
    private final JButton btnFrequencia = Botoes.contorno("Frequência", null);
    private final Map<Integer, TreinoRepositorio.ResumoPresenca> presencas;
    private boolean atualizandoLista;

    public PainelTreinos(Navegador navegador) {
        this(navegador, new HashMap<>());
    }

    private PainelTreinos(Navegador navegador, Map<Integer, TreinoRepositorio.ResumoPresenca> presencas) {
        super("Treinos", new ModeloTabela<Treino>()
                .coluna("Data", LocalDate.class, Treino::getData)
                .coluna("Horário", LocalTime.class, Treino::getHorario)
                .coluna("Tipo", String.class, t -> t.getTipo().getDescricao())
                .coluna("Título", String.class, Treino::getTitulo)
                .coluna("Local", String.class, Treino::getLocal)
                .coluna("Responsável", String.class, t -> t.getResponsavel() == null ? "" : t.getResponsavel().getNome())
                .coluna("Presença", String.class, t -> {
                    TreinoRepositorio.ResumoPresenca resumo = presencas.get(t.getId());
                    return resumo == null ? "—" : resumo.presentes() + " de " + resumo.registrados();
                }),
                EnumSet.allOf(Acao.class), navegador);
        this.presencas = presencas;
        renomearBotao(Acao.INCLUIR, "+ Treino");
        renomearBotao(Acao.GERENCIAR, "Presença");
        adicionarBotao(btnAmistoso);
        adicionarBotao(btnFrequencia);
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

    public void adicionarAcaoBtnAmistoso(ActionListener acao) {
        btnAmistoso.addActionListener(acao);
    }

    public void adicionarAcaoBtnFrequencia(ActionListener acao) {
        btnFrequencia.addActionListener(acao);
    }

    public void habilitarAmistoso(boolean habilitado) {
        btnAmistoso.setEnabled(habilitado);
    }

    public void apresentarModalidades(List<Modalidade> lista) {
        Modalidade selecionada = listaModalidades.getSelectedValue();
        int indiceSelecionado = listaModalidades.getSelectedIndex();
        atualizandoLista = true;
        modalidades.clear();
        modalidades.addElement(null);
        for (Modalidade modalidade : lista) {
            modalidades.addElement(modalidade);
        }
        int indice = selecionada == null ? 0 : modalidades.indexOf(selecionada);
        listaModalidades.setSelectedIndex(indiceSelecionado < 0 ? 0 : Math.max(indice, 0));
        atualizandoLista = false;
    }

    public void apresentarPresencas(Map<Integer, TreinoRepositorio.ResumoPresenca> novas) {
        presencas.clear();
        presencas.putAll(novas);
    }

    public TreinoRepositorio.ResumoPresenca getPresencas(Treino treino) {
        return presencas.get(treino.getId());
    }

    public Modalidade getModalidadeSelecionada() {
        return listaModalidades.getSelectedValue();
    }

    public TipoAtividade getTipoFiltro() {
        return (TipoAtividade) filtroTipo.getSelectedItem();
    }

    public boolean isSomenteProximos() {
        return filtroPeriodo.getSelectedItem() == Periodo.PROXIMOS;
    }

    public boolean isSomenteRealizados() {
        return filtroPeriodo.getSelectedItem() == Periodo.REALIZADOS;
    }
}
