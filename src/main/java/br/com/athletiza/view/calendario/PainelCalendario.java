package br.com.athletiza.view.calendario;

import br.com.athletiza.controller.CalendarioController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.eventos.PainelCadastroEvento;
import br.com.athletiza.view.treinos.PainelCadastroTreino;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Tela inicial: calendário do mês ou da semana com as atividades de cada dia
 * (CH-15, RF10), compromissos (CH-16), alertas (RF24) e a lista dos próximos eventos (CH-17).
 *
 * Abre sempre no mês atual. Clique em um dia mostra as atividades dele;
 * clique duplo cria um compromisso naquele dia. Treinos, competições e
 * eventos podem ser abertos a partir da lista lateral (RF09).
 */
public class PainelCalendario extends JPanel implements Recarregavel {

    private static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM uuuu", PORTUGUES);
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM", PORTUGUES);
    private static final DateTimeFormatter FORMATO_DIA_MES = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' uuuu", PORTUGUES);
    private static final DateTimeFormatter FORMATO_DIA_MES_CURTO = DateTimeFormatter.ofPattern("d 'de' MMMM", PORTUGUES);
    private static final String[] DIAS_DA_SEMANA = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    /** Opções do período de "Próximos eventos". */
    private enum Periodo {
        SETE(7), QUINZE(15), TRINTA(30);

        private final int dias;

        Periodo(int dias) {
            this.dias = dias;
        }

        @Override
        public String toString() {
            return "Próximos " + dias + " dias";
        }
    }

    /** Visão do calendário (RF10). A lista do dia fica sempre na lateral. */
    private enum Modo {
        MES, SEMANA
    }

    private final CalendarioController controller = new CalendarioController();
    private final Navegador navegador;
    private final CardLayout cartoesVisao = new CardLayout();
    private final JPanel visoes = new JPanel(cartoesVisao);
    private final PainelSemana painelSemana = new PainelSemana();
    private final PainelAlertas alertas = new PainelAlertas();
    private Modo modo = Modo.MES;
    private LocalDate inicioSemana = segundaFeiraDe(LocalDate.now());
    private final CelulaDia[] celulas = new CelulaDia[42];
    private final JLabel rotuloMes = new JLabel();
    private final JLabel tituloLista = new JLabel();
    private final JComboBox<Periodo> comboPeriodo = new JComboBox<>(Periodo.values());
    private final JButton verProximos = Botoes.contorno("Ver próximos", e -> selecionarDia(null));
    private final ListaAtividades lista = new ListaAtividades();

    private YearMonth mesAtual = YearMonth.now();
    private LocalDate diaSelecionado;
    /** Atividades do mês ou da semana exibida, por dia. */
    private Map<LocalDate, List<Atividade>> atividadesDoMes = Map.of();

    public PainelCalendario(Navegador navegador) {
        super(new BorderLayout(0, 20));
        this.navegador = navegador;
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(criarCabecalho(), BorderLayout.NORTH);
        JPanel corpo = new JPanel(new BorderLayout(20, 0));
        corpo.setOpaque(false);
        corpo.add(criarMes(), BorderLayout.CENTER);
        corpo.add(criarLateral(), BorderLayout.EAST);
        add(corpo, BorderLayout.CENTER);
    }

    @Override
    public void carregar() {
        try {
            atividadesDoMes = modo == Modo.MES ? controller.atividadesDoMes(mesAtual)
                    : controller.atividadesDoPeriodo(inicioSemana, inicioSemana.plusDays(6));
            alertas.mostrar(controller.alertas(LocalDate.now()));
        } catch (PersistenciaException e) {
            atividadesDoMes = Map.of();
            Mensagens.erro(this, e);
        }
        atualizarMes();
        atualizarLista();
    }

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("Calendário");
        titulo.setFont(Tema.fonteTitulo());
        JButton novo = Botoes.destaque("+ Compromisso", e -> novoCompromisso(diaSelecionado));
        novo.setEnabled(podeAlterar());

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(novo, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarMes() {
        rotuloMes.setFont(Tema.fonte(Font.BOLD, 16f));
        JButton anterior = Botoes.contorno("<", e -> avancar(-1));
        JButton proximo = Botoes.contorno(">", e -> avancar(1));
        JButton hoje = Botoes.contorno("Hoje", e -> {
            irPara(LocalDate.now());
            selecionarDia(LocalDate.now());
            carregar();
        });
        anterior.setToolTipText("Anterior");
        proximo.setToolTipText("Próximo");

        JToggleButton botaoMes = botaoModo("Mês", Modo.MES);
        JToggleButton botaoSemana = botaoModo("Semana", Modo.SEMANA);
        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(botaoMes);
        grupoModo.add(botaoSemana);
        botaoMes.setSelected(true);

        JPanel navegacao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navegacao.setOpaque(false);
        navegacao.add(botaoMes);
        navegacao.add(botaoSemana);
        navegacao.add(Box.createHorizontalStrut(10));
        navegacao.add(anterior);
        navegacao.add(proximo);
        navegacao.add(hoje);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 12));
        topo.add(rotuloMes, BorderLayout.WEST);
        topo.add(navegacao, BorderLayout.EAST);

        JPanel semana = new JPanel(new GridLayout(1, 7));
        semana.setOpaque(false);
        semana.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        for (String dia : DIAS_DA_SEMANA) {
            JLabel rotulo = new JLabel(dia, SwingConstants.CENTER);
            rotulo.setFont(Tema.fonte(Font.BOLD, 12f));
            rotulo.setForeground(Cores.TEXTO_SECUNDARIO);
            semana.add(rotulo);
        }

        JPanel grade = new JPanel(new GridLayout(6, 7));
        grade.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Cores.CINZA_ESCURO));
        MouseAdapter cliqueNoDia = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LocalDate dia = ((CelulaDia) e.getComponent()).getData();
                if (e.getClickCount() == 2 && podeAlterar()) {
                    novoCompromisso(dia);
                } else {
                    selecionarDia(dia);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((CelulaDia) e.getComponent()).setMouseSobre(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((CelulaDia) e.getComponent()).setMouseSobre(false);
            }
        };
        for (int i = 0; i < celulas.length; i++) {
            celulas[i] = new CelulaDia();
            celulas[i].addMouseListener(cliqueNoDia);
            grade.add(celulas[i]);
        }

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(semana, BorderLayout.NORTH);
        centro.add(grade, BorderLayout.CENTER);

        visoes.setOpaque(false);
        visoes.add(centro, Modo.MES.name());
        visoes.add(painelSemana, Modo.SEMANA.name());

        JPanel cartao = new JPanel(new BorderLayout());
        cartao.putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
        cartao.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(visoes, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel criarLateral() {
        tituloLista.setFont(Tema.fonte(Font.BOLD, 15f));
        comboPeriodo.addActionListener(e -> atualizarLista());

        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.setOpaque(false);
        topo.add(tituloLista, BorderLayout.NORTH);
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controles.setOpaque(false);
        controles.add(comboPeriodo);
        controles.add(verProximos);
        topo.add(controles, BorderLayout.SOUTH);

        JPanel alinhamento = new JPanel(new BorderLayout());
        alinhamento.setOpaque(false);
        alinhamento.add(lista, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(alinhamento);
        rolagem.setBorder(null);
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);

        JPanel acima = new JPanel(new BorderLayout());
        acima.setOpaque(false);
        acima.add(alertas, BorderLayout.NORTH);
        acima.add(topo, BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout(0, 16));
        lateral.putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
        lateral.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 12));
        lateral.setPreferredSize(new Dimension(290, 0));
        lateral.add(acima, BorderLayout.NORTH);
        lateral.add(rolagem, BorderLayout.CENTER);
        return lateral;
    }

    private JToggleButton botaoModo(String texto, Modo novoModo) {
        JToggleButton botao = new JToggleButton(texto);
        botao.putClientProperty(FlatClientProperties.STYLE, "selectedBackground:" + Cores.hex(Cores.VERDE_SELECAO)
                + "; selectedForeground:" + Cores.hex(Cores.VERDE) + "; focusWidth:0; margin:6,14,6,14");
        botao.addActionListener(e -> {
            modo = novoModo;
            irPara(diaSelecionado == null ? LocalDate.now() : diaSelecionado);
            cartoesVisao.show(visoes, modo.name());
            carregar();
        });
        return botao;
    }

    /** Mês ou semana anterior (-1) ou seguinte (+1), conforme a visão atual. */
    private void avancar(int sentido) {
        if (modo == Modo.MES) {
            mesAtual = mesAtual.plusMonths(sentido);
        } else {
            inicioSemana = inicioSemana.plusWeeks(sentido);
        }
        diaSelecionado = null;
        carregar();
    }

    /** Posiciona o mês e a semana exibidos no dia informado. */
    private void irPara(LocalDate dia) {
        mesAtual = YearMonth.from(dia);
        inicioSemana = segundaFeiraDe(dia);
    }

    private static LocalDate segundaFeiraDe(LocalDate dia) {
        return dia.minusDays(dia.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
    }

    private boolean estaVisivel(LocalDate dia) {
        return modo == Modo.MES ? YearMonth.from(dia).equals(mesAtual)
                : !dia.isBefore(inicioSemana) && !dia.isAfter(inicioSemana.plusDays(6));
    }

    /** Seleciona um dia (mostrando suas atividades) ou, com null, volta para os próximos eventos. */
    private void selecionarDia(LocalDate dia) {
        if (dia != null && !estaVisivel(dia)) {
            irPara(dia);
            diaSelecionado = dia;
            carregar();
            return;
        }
        diaSelecionado = dia;
        atualizarMes();
        atualizarLista();
    }

    private void atualizarMes() {
        if (modo == Modo.SEMANA) {
            LocalDate fim = inicioSemana.plusDays(6);
            String texto = inicioSemana.getMonth() == fim.getMonth()
                    ? inicioSemana.getDayOfMonth() + " a " + fim.format(FORMATO_DIA_MES)
                    : inicioSemana.format(FORMATO_DIA_MES_CURTO) + " a " + fim.format(FORMATO_DIA_MES);
            rotuloMes.setText(texto);
            painelSemana.atualizar(inicioSemana, atividadesDoMes, diaSelecionado, this::selecionarDia,
                    atividade -> abrirOuEditar(atividade));
            return;
        }
        String nomeMes = mesAtual.format(FORMATO_MES);
        rotuloMes.setText(Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1));

        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDia = mesAtual.atDay(1);
        LocalDate inicioGrade = primeiroDia.minusDays(primeiroDia.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        for (int i = 0; i < celulas.length; i++) {
            LocalDate dia = inicioGrade.plusDays(i);
            boolean doMes = YearMonth.from(dia).equals(mesAtual);
            List<Atividade> doDia = doMes ? atividadesDoMes.getOrDefault(dia, List.of()) : List.of();
            celulas[i].atualizar(dia, doMes, dia.equals(hoje), dia.equals(diaSelecionado), doDia);
        }
    }

    private void atualizarLista() {
        boolean modoDia = diaSelecionado != null;
        comboPeriodo.setVisible(!modoDia);
        verProximos.setVisible(modoDia);
        if (modoDia) {
            tituloLista.setText("Atividades de " + diaSelecionado.format(FORMATO_DIA));
            List<Atividade> doDia = new ArrayList<>(atividadesDoMes.getOrDefault(diaSelecionado, List.of()));
            boolean alteravel = podeAlterar();
            lista.mostrar(doDia, false, alteravel ? this::editarCompromisso : null,
                    alteravel ? this::excluirCompromisso : null, alteravel ? this::abrir : null);
            return;
        }
        tituloLista.setText("Próximos eventos");
        Periodo periodo = (Periodo) comboPeriodo.getSelectedItem();
        try {
            lista.mostrar(controller.proximasAtividades(LocalDate.now(), periodo.dias), true, null, null,
                    podeAlterar() ? this::abrir : null);
        } catch (PersistenciaException e) {
            lista.mostrar(List.of(), true, null, null, null);
            Mensagens.erro(this, e);
        }
    }

    private void novoCompromisso(LocalDate dia) {
        Compromisso compromisso = new Compromisso();
        compromisso.setData(dia == null ? LocalDate.now() : dia);
        if (DialogoCompromisso.abrir(this, compromisso, controller)) {
            diaSelecionado = compromisso.getData();
            irPara(diaSelecionado);
            carregar();
        }
    }

    private void editarCompromisso(Compromisso compromisso) {
        if (DialogoCompromisso.abrir(this, compromisso, controller)) {
            diaSelecionado = compromisso.getData();
            irPara(diaSelecionado);
        }
        carregar();
    }

    private void excluirCompromisso(Compromisso compromisso) {
        if (Mensagens.confirmar(this, "Deseja realmente excluir o compromisso \"" + compromisso.getTitulo() + "\"?")) {
            try {
                controller.excluirCompromisso(compromisso);
            } catch (PersistenciaException e) {
                Mensagens.erro(this, e);
            }
            carregar();
        }
    }

    /**
     * Abre treino, competição ou evento na tela de edição do seu módulo (RF09).
     * Ao salvar ou cancelar, o "Voltar" retorna ao calendário já atualizado.
     */
    private void abrir(Atividade atividade) {
        try {
            Atividade completa = controller.carregarCompleta(atividade);
            if (completa instanceof Treino treino) {
                navegador.abrir(new PainelCadastroTreino(treino, navegador));
            } else if (completa instanceof Competicao competicao) {
                navegador.abrir(new PainelCadastroCompeticao(competicao, navegador));
            } else if (completa instanceof Evento evento) {
                navegador.abrir(new PainelCadastroEvento(evento, navegador));
            }
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    /** Clique duplo na visão semanal: compromissos abrem o diálogo; o resto, a tela do módulo. */
    private void abrirOuEditar(Atividade atividade) {
        if (!podeAlterar()) {
            return;
        }
        if (atividade instanceof Compromisso compromisso) {
            editarCompromisso(compromisso);
        } else {
            abrir(atividade);
        }
    }

    private static boolean podeAlterar() {
        return Sessao.getUsuarioLogado() == null || Sessao.podeAlterarDados();
    }
}
