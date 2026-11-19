package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.CalendarioController;
import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.DadosDashboard.Resumo;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.model.Modulo;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.calendario.ListaAtividades;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class PainelDashboard extends JPanel implements Recarregavel {
    enum Periodo {
        TRINTA_DIAS("Últimos 30 dias", 30), NOVENTA_DIAS("Últimos 90 dias", 90),
        ESTE_ANO("Este ano", -1), TUDO("Todo o período", 0);

        private final String texto;
        private final int dias;

        Periodo(String texto, int dias) {
            this.texto = texto;
            this.dias = dias;
        }

        LocalDate inicio(LocalDate hoje) {
            return dias > 0 ? hoje.minusDays(dias) : dias < 0 ? hoje.withDayOfYear(1) : null;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private static final int ITENS_POR_GRAFICO = 6;

    private final DashboardController controller = new DashboardController();
    private final JComboBox<Periodo> filtroPeriodo = new JComboBox<>(Periodo.values());

    private final Numero numeroAtletas = new Numero("Atletas ativos", Cores.VERDE);
    private final Numero numeroTreinos = new Numero("Treinos e amistosos", Cores.ROXO);
    private final Numero numeroPresenca = new Numero("Presença nos treinos", Cores.VERDE);
    private final Numero numeroDemandas = new Numero("Demandas em aberto", Cores.AMARELO);

    private final Cartao cartaoAtletas = new Cartao("Qual modalidade tem mais atletas?",
            GraficoBarras.simples(Cores.VERDE, ITENS_POR_GRAFICO));
    private final Cartao cartaoPresencas = new Cartao("Qual modalidade tem mais atletas presentes nos treinos?",
            GraficoBarras.simples(Cores.VERDE, ITENS_POR_GRAFICO));
    private final Cartao cartaoTreinos = new Cartao("Qual modalidade realizou mais treinos e amistosos?",
            GraficoBarras.composto(Cores.VERDE, "Treinos", Cores.ROXO, "Amistosos", ITENS_POR_GRAFICO));
    private final Cartao cartaoFrequencia = new Cartao("Quais atletas menos participam dos treinos?",
            GraficoBarras.percentual(ITENS_POR_GRAFICO));
    private final Cartao cartaoDemandas = new Cartao("Quais setores estão com o maior número de demandas?",
            GraficoBarras.composto(Cores.AMARELO, "Em aberto", Cores.VERMELHO, "Atrasadas", ITENS_POR_GRAFICO));

    private static final int DIAS_PROXIMAS = 14;
    private static final int MAXIMO_PROXIMAS = 6;

    private final CalendarioController calendario = new CalendarioController();
    private final ListaAtividades listaProximas = new ListaAtividades();

    public PainelDashboard(Consumer<Modulo> irPara) {
        super(new BorderLayout(0, 20));
        numeroAtletas.aoClicar(Modulo.ATLETAS, irPara);
        numeroTreinos.aoClicar(Modulo.TREINOS, irPara);
        numeroPresenca.aoClicar(Modulo.TREINOS, irPara);
        numeroDemandas.aoClicar(Modulo.DEMANDAS, irPara);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        filtroPeriodo.setSelectedItem(Periodo.ESTE_ANO);
        filtroPeriodo.addActionListener(e -> carregar());

        add(criarCabecalho(), BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(criarCorpo());
        rolagem.setBorder(null);
        rolagem.getViewport().setOpaque(false);
        rolagem.setOpaque(false);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);
    }

    @Override
    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            mostrar(controller.carregar(((Periodo) filtroPeriodo.getSelectedItem()).inicio(hoje), hoje));
            listaProximas.mostrar(calendario.proximasAtividades(hoje, DIAS_PROXIMAS).stream()
                    .filter(Sessao.getAcesso()::ve)
                    .limit(MAXIMO_PROXIMAS)
                    .toList(), true, null, null, null);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    private void mostrar(DadosDashboard dados) {
        Resumo resumo = dados.resumo();
        numeroAtletas.mostrar(String.valueOf(resumo.atletasAtivos()), "cadastrados e ativos");
        numeroTreinos.mostrar(String.valueOf(resumo.treinos() + resumo.amistosos()),
                resumo.treinos() + " treino(s) e " + resumo.amistosos() + " amistoso(s)");
        numeroPresenca.mostrar(resumo.percentualPresenca() + "%", "das presenças registradas");
        numeroPresenca.cor(GraficoBarras.corDoPercentual(resumo.percentualPresenca()));
        numeroDemandas.mostrar(String.valueOf(resumo.demandasAbertas()),
                resumo.demandasAtrasadas() + " atrasada(s)");

        cartaoAtletas.mostrar(dados.atletasPorModalidade(), i -> i.valor() + (i.valor() == 1 ? " atleta" : " atletas"),
                "Nenhum atleta ativo vinculado a uma modalidade.");
        cartaoPresencas.mostrar(dados.presencasPorModalidade(),
                i -> i.valor() + (i.valor() == 1 ? " presença" : " presenças"),
                "Nenhuma lista de presença registrada no período.");
        cartaoTreinos.mostrar(dados.treinosPorModalidade(), Indicador::detalhe,
                "Nenhum treino ou amistoso realizado no período.");
        cartaoDemandas.mostrar(dados.demandasPorSetor(), Indicador::detalhe,
                "Nenhuma demanda em aberto. Tudo em dia!");
        cartaoFrequencia.mostrarAtletas(dados.menosFrequentes());
    }

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(Tema.fonteTitulo());
        JLabel subtitulo = new JLabel("Passe o mouse nas barras para ver os detalhes.");
        subtitulo.setToolTipText("O período vale para treinos, amistosos e presenças. "
                + "Atletas e demandas mostram a situação atual.");
        subtitulo.setForeground(Cores.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);

        JPanel cabecalho = new JPanel(new BorderLayout(16, 0));
        cabecalho.setOpaque(false);
        cabecalho.add(textos, BorderLayout.CENTER);
        JPanel filtro = new JPanel(new GridBagLayout());
        filtro.setOpaque(false);
        filtro.add(filtroPeriodo);
        JLabel rotuloPeriodo = new JLabel("Treinos e presenças:");
        rotuloPeriodo.setForeground(Cores.TEXTO_SECUNDARIO);
        rotuloPeriodo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        filtro.add(rotuloPeriodo, 0);
        cabecalho.add(filtro, BorderLayout.EAST);
        return cabecalho;
    }

    private JComponent criarCorpo() {
        JPanel numeros = new JPanel(new GridLayout(1, 4, 16, 0));
        numeros.setOpaque(false);
        numeros.add(numeroAtletas);
        numeros.add(numeroTreinos);
        numeros.add(numeroPresenca);
        numeros.add(numeroDemandas);

        Corpo corpo = new Corpo();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 16, 0);
        corpo.add(numeros, c);

        c.gridwidth = 1;
        c.gridy = 1;
        adicionar(corpo, c, cartaoAtletas, 0);
        adicionar(corpo, c, cartaoPresencas, 1);
        c.gridy = 2;
        adicionar(corpo, c, cartaoTreinos, 0);
        adicionar(corpo, c, cartaoFrequencia, 1);
        c.gridy = 3;
        adicionar(corpo, c, cartaoDemandas, 0);
        adicionar(corpo, c, criarCartaoProximas(), 1);

        c.gridy = 4;
        c.weighty = 1;
        corpo.add(Box.createGlue(), c);
        return corpo;
    }

    private JComponent criarCartaoProximas() {
        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        cartao.putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JLabel pergunta = new JLabel("Quais são as próximas atividades?");
        pergunta.setFont(Tema.fonte(Font.BOLD, 14f));
        JLabel periodo = new JLabel("Treinos, competições, eventos e compromissos dos próximos " + DIAS_PROXIMAS + " dias");
        periodo.setForeground(Cores.TEXTO_SECUNDARIO);
        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.add(pergunta);
        topo.add(Box.createVerticalStrut(4));
        topo.add(periodo);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(listaProximas, BorderLayout.CENTER);
        return cartao;
    }

    private static void adicionar(JPanel corpo, GridBagConstraints c, JComponent cartao, int coluna) {
        c.gridx = coluna;
        c.insets = new Insets(0, coluna == 0 ? 0 : 8, 16, coluna == 0 ? 8 : 0);
        corpo.add(cartao, c);
    }

    private static class Corpo extends JPanel implements Scrollable {
        Corpo() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        @Override
        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visivel, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visivel, int orientacao, int direcao) {
            return visivel.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class Numero extends JPanel {
        private final JLabel valor = new JLabel("-");
        private final JLabel detalhe = new JLabel(" ");

        Numero(String titulo, Color cor) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
            setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            JLabel rotulo = new JLabel(titulo);
            rotulo.setForeground(Cores.TEXTO_SECUNDARIO);
            valor.setFont(Tema.fonte(Font.BOLD, 28f));
            valor.setForeground(cor);
            detalhe.setFont(Tema.fonte(Font.PLAIN, 12f));
            detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
            add(rotulo);
            add(Box.createVerticalStrut(4));
            add(valor);
            add(detalhe);
        }

        void mostrar(String numero, String texto) {
            valor.setText(numero);
            detalhe.setText(texto);
        }

        void cor(Color cor) {
            valor.setForeground(cor);
        }

        void aoClicar(Modulo modulo, Consumer<Modulo> irPara) {
            if (irPara == null || !Sessao.getAcesso().veModulo(modulo)) {
                return;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Abrir " + modulo.getNome());
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    irPara.accept(modulo);
                }
            });
        }
    }

    private static class Cartao extends JPanel {
        private final JLabel resposta = new JLabel(" ");
        private final JLabel complemento = new JLabel(" ");
        private final GraficoBarras grafico;
        private final JLabel vazio = new JLabel("", SwingConstants.LEFT);

        Cartao(String pergunta, GraficoBarras grafico) {
            super(new BorderLayout(0, 12));
            this.grafico = grafico;
            putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

            JLabel rotuloPergunta = new JLabel(pergunta);
            rotuloPergunta.setFont(Tema.fonte(Font.BOLD, 14f));
            resposta.setFont(Tema.fonte(Font.BOLD, 18f));
            resposta.setForeground(Cores.VERDE);
            complemento.setForeground(Cores.TEXTO_SECUNDARIO);
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);

            JPanel topo = new JPanel();
            topo.setOpaque(false);
            topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
            topo.add(rotuloPergunta);
            topo.add(Box.createVerticalStrut(8));
            topo.add(resposta);
            topo.add(complemento);
            add(topo, BorderLayout.NORTH);
            add(grafico, BorderLayout.CENTER);
        }

        void mostrar(List<Indicador> itens, Function<Indicador, String> descrever, String mensagemVazio) {
            if (itens.isEmpty() || itens.get(0).valor() == 0) {
                semDados(mensagemVazio);
                return;
            }
            Indicador primeiro = itens.get(0);
            resposta.setForeground(Cores.VERDE);
            List<Indicador> empatados = itens.stream()
                    .filter(i -> i.valor() == primeiro.valor() && i.parcial() == primeiro.parcial()).toList();
            resposta.setText(nomes(empatados.stream().map(Indicador::nome).toList()));
            complemento.setText((empatados.size() > 1 ? "Empatados com " : "") + descrever.apply(primeiro));
            exibirGrafico(itens);
        }

        void mostrarAtletas(List<Frequencia> frequencias) {
            if (frequencias.isEmpty()) {
                semDados("Nenhuma lista de presença registrada no período.");
                return;
            }
            Frequencia menor = frequencias.get(0);
            List<String> empatados = frequencias.stream()
                    .filter(f -> f.getPercentual() == menor.getPercentual() && f.getFaltas() == menor.getFaltas())
                    .map(f -> f.atleta().getNome()).toList();
            resposta.setText(nomes(empatados));
            resposta.setForeground(GraficoBarras.corDoPercentual(menor.getPercentual()));
            complemento.setText(menor.getPercentual() + "% de presença (" + menor.getFaltas() + " falta(s) em "
                    + menor.treinos() + " treino(s))");
            exibirGrafico(frequencias.stream().map(f -> new Indicador(f.atleta().getNome(), f.getPercentual(),
                    f.presencas() + " presença(s) e " + f.getFaltas() + " falta(s) em " + f.treinos() + " treino(s)"))
                    .toList());
        }

        private void exibirGrafico(List<Indicador> itens) {
            grafico.mostrar(itens);
            remove(vazio);
            add(grafico, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        private void semDados(String mensagem) {
            resposta.setText("Sem dados");
            resposta.setForeground(Cores.TEXTO_SECUNDARIO);
            complemento.setText(" ");
            vazio.setText(mensagem);
            remove(grafico);
            add(vazio, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        private static String nomes(List<String> nomes) {
            if (nomes.size() == 1) {
                return nomes.get(0);
            }
            if (nomes.size() <= 3) {
                return String.join(", ", nomes.subList(0, nomes.size() - 1)) + " e " + nomes.get(nomes.size() - 1);
            }
            return nomes.subList(0, 2).stream().collect(Collectors.joining(", ")) + " e mais " + (nomes.size() - 2);
        }
    }
}
