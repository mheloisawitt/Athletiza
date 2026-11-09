package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import static br.com.athletiza.controller.DashboardController.AMISTOSOS;
import static br.com.athletiza.controller.DashboardController.COMPETICOES;
import static br.com.athletiza.controller.DashboardController.EVENTOS;
import static br.com.athletiza.controller.DashboardController.FALTAS;
import static br.com.athletiza.controller.DashboardController.PRESENCA;
import static br.com.athletiza.controller.DashboardController.PRESENCAS;
import static br.com.athletiza.controller.DashboardController.PRIMEIRO;
import static br.com.athletiza.controller.DashboardController.SEGUNDO;
import static br.com.athletiza.controller.DashboardController.TERCEIRO;
import static br.com.athletiza.controller.DashboardController.TREINOS;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.DadosDashboard.Distribuicao;
import br.com.athletiza.model.DadosDashboard.LinhaDoTempo;
import br.com.athletiza.model.DadosDashboard.Resumo;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Dashboard: números gerais no topo e três abas.
 * <ul>
 * <li>Perguntas da diretoria: um gráfico de barras para cada pergunta (modalidades com
 * mais atletas, presenças e treinos; atletas que menos participam; setores com mais demandas),
 * com a resposta em destaque.</li>
 * <li>Linha do tempo: próximas atividades, atividades e presença mês a mês (linhas) e
 * presenças e faltas por mês (colunas empilhadas).</li>
 * <li>Distribuição: pizzas e roscas (situação dos atletas, eventos por tipo, demandas por
 * situação e prioridade) e pódios por modalidade.</li>
 * </ul>
 */
public class PainelDashboard extends JPanel implements Recarregavel {

    /** Período considerado para treinos, amistosos e presenças. */
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

    private static final Color CINZA = new Color(0x6A6A6A);
    private static final Map<String, Color> CORES_ATIVIDADES = Map.of(
            TREINOS, Color.decode(TipoAtividade.TREINO.getCorHex()),
            AMISTOSOS, Color.decode(TipoAtividade.AMISTOSO.getCorHex()),
            COMPETICOES, Color.decode(TipoAtividade.COMPETICAO.getCorHex()),
            EVENTOS, Color.decode(TipoAtividade.EVENTO.getCorHex()));

    private final GraficoLinhaDoTempo proximas = new GraficoLinhaDoTempo(DashboardController.DIAS_PROXIMAS);
    private final GraficoLinha atividadesPorMes = new GraficoLinha(CORES_ATIVIDADES, false);
    private final GraficoLinha presencaPorMes = new GraficoLinha(Map.of(PRESENCA, Cores.VERDE), true);
    private final GraficoColunas presencasEFaltas = new GraficoColunas(Map.of(PRESENCAS, Cores.VERDE, FALTAS, Cores.VERMELHO));
    private final GraficoPizza atletasPorSituacao = new GraficoPizza(false, Map.of(
            SituacaoAtleta.ATIVO.toString(), Cores.VERDE, SituacaoAtleta.LESIONADO.toString(), Cores.VERMELHO,
            SituacaoAtleta.AFASTADO.toString(), Cores.AMARELO, SituacaoAtleta.INATIVO.toString(), CINZA), "vínculos");
    private final GraficoPizza eventosPorTipo = new GraficoPizza(true, Map.of(
            TipoEvento.FESTA.toString(), new Color(0xFF4FD8), TipoEvento.ACAO_SOCIAL.toString(), new Color(0x3D9BFF),
            TipoEvento.ACAO_AMBIENTAL.toString(), new Color(0x2ED3C6), TipoEvento.COMPETICAO.toString(), Cores.VERDE,
            TipoEvento.OUTRO.toString(), CINZA), "eventos");
    private final GraficoPizza demandasPorSituacao = new GraficoPizza(true, Map.of(
            SituacaoDemanda.ABERTA.toString(), Cores.AMARELO, SituacaoDemanda.EM_ANDAMENTO.toString(), Cores.ROXO,
            SituacaoDemanda.CONCLUIDA.toString(), Cores.VERDE, SituacaoDemanda.CANCELADA.toString(), CINZA), "demandas");
    private final GraficoPizza demandasPorPrioridade = new GraficoPizza(false, Map.of(
            PrioridadeDemanda.ALTA.toString(), Cores.VERMELHO, PrioridadeDemanda.MEDIA.toString(), Cores.AMARELO,
            PrioridadeDemanda.BAIXA.toString(), Cores.VERDE), "em aberto");
    private final GraficoColunas podios = new GraficoColunas(Map.of(
            PRIMEIRO, new Color(0xF5C542), SEGUNDO, new Color(0xC9CED6), TERCEIRO, new Color(0xCD7F32)));

    private final CartaoGrafico cartaoProximas = new CartaoGrafico("Próximos " + DashboardController.DIAS_PROXIMAS
            + " dias", "Treinos, amistosos, competições, eventos e compromissos agendados", proximas,
            "Nenhuma atividade agendada para os próximos dias.");
    private final CartaoGrafico cartaoAtividadesPorMes = new CartaoGrafico("Atividades por mês",
            "Treinos, amistosos, competições e eventos realizados em cada mês do período", atividadesPorMes,
            "Nenhuma atividade realizada no período.");
    private final CartaoGrafico cartaoPresencaPorMes = new CartaoGrafico("Presença nos treinos por mês",
            "Percentual de presentes nas listas de presença", presencaPorMes,
            "Nenhuma lista de presença registrada no período.");
    private final CartaoGrafico cartaoPresencasEFaltas = new CartaoGrafico("Presenças e faltas por mês",
            "Marcações das listas de presença", presencasEFaltas, "Nenhuma lista de presença registrada no período.");
    private final CartaoGrafico cartaoAtletasPorSituacao = new CartaoGrafico("Situação dos atletas nas modalidades",
            "Cada vínculo de atleta com uma modalidade", atletasPorSituacao, "Nenhum atleta vinculado a uma modalidade.");
    private final CartaoGrafico cartaoEventosPorTipo = new CartaoGrafico("Eventos por tipo",
            "Realizados e agendados a partir do início do período (sem os cancelados)", eventosPorTipo,
            "Nenhum evento no período.");
    private final CartaoGrafico cartaoDemandasPorSituacao = new CartaoGrafico("Demandas por situação",
            "Todas as demandas cadastradas", demandasPorSituacao, "Nenhuma demanda cadastrada.");
    private final CartaoGrafico cartaoDemandasPorPrioridade = new CartaoGrafico("Prioridade das demandas em aberto",
            "Abertas e em andamento", demandasPorPrioridade, "Nenhuma demanda em aberto.");
    private final CartaoGrafico cartaoPodios = new CartaoGrafico("Pódios por modalidade",
            "1º, 2º e 3º lugares nas competições do período", podios, "Nenhum resultado de 1º a 3º lugar no período.");

    public PainelDashboard() {
        super(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        filtroPeriodo.setSelectedItem(Periodo.ESTE_ANO);
        filtroPeriodo.addActionListener(e -> carregar());

        JPanel topo = new JPanel(new BorderLayout(0, 16));
        topo.setOpaque(false);
        topo.add(criarCabecalho(), BorderLayout.NORTH);
        topo.add(criarNumeros(), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.putClientProperty(FlatClientProperties.STYLE, "tabHeight:36; underlineColor:" + Cores.hex(Cores.VERDE)
                + "; selectedForeground:" + Cores.hex(Cores.VERDE));
        abas.addTab("Perguntas da diretoria", rolavel(criarPerguntas()));
        abas.addTab("Linha do tempo", rolavel(criarLinhaDoTempo()));
        abas.addTab("Distribuição", rolavel(criarDistribuicao()));
        add(abas, BorderLayout.CENTER);
    }

    private static JScrollPane rolavel(JComponent conteudo) {
        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(null);
        rolagem.getViewport().setOpaque(false);
        rolagem.setOpaque(false);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    @Override
    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            mostrar(controller.carregar(((Periodo) filtroPeriodo.getSelectedItem()).inicio(hoje), hoje), hoje);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    private void mostrar(DadosDashboard dados, LocalDate hoje) {
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

        LinhaDoTempo linha = dados.linhaDoTempo();
        proximas.mostrar(hoje, linha.proximas());
        cartaoProximas.exibir(!linha.proximas().isEmpty());
        atividadesPorMes.mostrar(linha.atividadesPorMes());
        cartaoAtividadesPorMes.exibir(!linha.atividadesPorMes().isVazio());
        presencaPorMes.mostrar(linha.presencaPorMes());
        cartaoPresencaPorMes.exibir(!linha.presencasEFaltasPorMes().isVazio());
        presencasEFaltas.mostrar(linha.presencasEFaltasPorMes());
        cartaoPresencasEFaltas.exibir(!linha.presencasEFaltasPorMes().isVazio());

        Distribuicao distribuicao = dados.distribuicao();
        atletasPorSituacao.mostrar(distribuicao.atletasPorSituacao());
        cartaoAtletasPorSituacao.exibir(!atletasPorSituacao.isVazio());
        eventosPorTipo.mostrar(distribuicao.eventosPorTipo());
        cartaoEventosPorTipo.exibir(!eventosPorTipo.isVazio());
        demandasPorSituacao.mostrar(distribuicao.demandasPorSituacao());
        cartaoDemandasPorSituacao.exibir(!demandasPorSituacao.isVazio());
        demandasPorPrioridade.mostrar(distribuicao.demandasPorPrioridade());
        cartaoDemandasPorPrioridade.exibir(!demandasPorPrioridade.isVazio());
        podios.mostrar(distribuicao.podiosPorModalidade());
        cartaoPodios.exibir(!distribuicao.podiosPorModalidade().isVazio());
    }

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(Tema.fonteTitulo());
        JLabel subtitulo = new JLabel("Passe o mouse nos gráficos para ver os detalhes.");
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

    private JComponent criarNumeros() {
        JPanel numeros = new JPanel(new GridLayout(1, 4, 16, 0));
        numeros.setOpaque(false);
        numeros.add(numeroAtletas);
        numeros.add(numeroTreinos);
        numeros.add(numeroPresenca);
        numeros.add(numeroDemandas);
        return numeros;
    }

    /** Aba com os cartões das perguntas: dois por linha e o de setores na largura toda. */
    private JComponent criarPerguntas() {
        return grade(new JComponent[][]{
            {cartaoAtletas, cartaoPresencas},
            {cartaoTreinos, cartaoFrequencia},
            {cartaoDemandas}});
    }

    private JComponent criarLinhaDoTempo() {
        return grade(new JComponent[][]{
            {cartaoProximas},
            {cartaoAtividadesPorMes},
            {cartaoPresencaPorMes, cartaoPresencasEFaltas}});
    }

    private JComponent criarDistribuicao() {
        return grade(new JComponent[][]{
            {cartaoAtletasPorSituacao, cartaoEventosPorTipo},
            {cartaoDemandasPorSituacao, cartaoDemandasPorPrioridade},
            {cartaoPodios}});
    }

    /** Monta as linhas de cartões; uma linha com um cartão só ocupa a largura toda. */
    private static JComponent grade(JComponent[][] linhas) {
        Corpo corpo = new Corpo();
        corpo.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        for (int linha = 0; linha < linhas.length; linha++) {
            c.gridy = linha;
            if (linhas[linha].length == 1) {
                c.gridx = 0;
                c.gridwidth = 2;
                c.insets = new Insets(0, 0, 16, 0);
                corpo.add(linhas[linha][0], c);
                c.gridwidth = 1;
            } else {
                adicionar(corpo, c, linhas[linha][0], 0);
                adicionar(corpo, c, linhas[linha][1], 1);
            }
        }
        c.gridy = linhas.length;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weighty = 1;
        corpo.add(Box.createGlue(), c);
        return corpo;
    }

    private static void adicionar(JPanel corpo, GridBagConstraints c, JComponent cartao, int coluna) {
        c.gridx = coluna;
        c.insets = new Insets(0, coluna == 0 ? 0 : 8, 16, coluna == 0 ? 8 : 0);
        corpo.add(cartao, c);
    }

    /** Conteúdo rolável que acompanha a largura da tela (só rola na vertical). */
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

    /** Número geral do topo da tela (ex.: "Atletas ativos: 7"). */
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
    }

    /** Cartão com título, explicação e um gráfico; sem dados, mostra uma mensagem no lugar do gráfico. */
    private static class CartaoGrafico extends JPanel {

        private final JComponent grafico;
        private final JLabel vazio;

        CartaoGrafico(String titulo, String explicacao, JComponent grafico, String mensagemVazio) {
            super(new BorderLayout(0, 12));
            this.grafico = grafico;
            this.vazio = new JLabel(mensagemVazio);
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

            JLabel rotuloTitulo = new JLabel(titulo);
            rotuloTitulo.setFont(Tema.fonte(Font.BOLD, 14f));
            JLabel rotuloExplicacao = new JLabel(explicacao);
            rotuloExplicacao.setForeground(Cores.TEXTO_SECUNDARIO);
            JPanel topo = new JPanel();
            topo.setOpaque(false);
            topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
            topo.add(rotuloTitulo);
            topo.add(Box.createVerticalStrut(2));
            topo.add(rotuloExplicacao);
            add(topo, BorderLayout.NORTH);
            add(grafico, BorderLayout.CENTER);
        }

        void exibir(boolean temDados) {
            remove(temDados ? vazio : grafico);
            add(temDados ? grafico : vazio, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    /** Cartão com a pergunta, a resposta em destaque e o gráfico. */
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

        /**
         * Mostra os indicadores. A resposta é o primeiro colocado; havendo empate
         * no primeiro lugar, todos os empatados aparecem.
         */
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

        /** Atletas que menos participam: a resposta é quem tem a menor frequência. */
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

        /** "A", "A e B" ou "A, B e mais 2". */
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
