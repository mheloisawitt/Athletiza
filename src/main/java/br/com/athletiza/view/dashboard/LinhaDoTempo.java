package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Linha do tempo do mês: uma faixa para treinos, uma para competições e uma para
 * eventos, com os dias do mês no eixo. Usa as mesmas cores do calendário da tela inicial.
 * Competições de vários dias aparecem como uma barra do início ao fim.
 */
public class LinhaDoTempo extends Grafico {

    private static final String[] FAIXAS = {"Treinos", "Competições", "Eventos"};

    private YearMonth mes = YearMonth.now();
    private LocalDate hoje = LocalDate.now();
    private Map<LocalDate, List<Atividade>> atividades = new TreeMap<>();

    public LinhaDoTempo() {
        setPreferredSize(new java.awt.Dimension(600, 190));
    }

    public void mostrar(YearMonth mes, LocalDate hoje, Map<LocalDate, List<Atividade>> atividades) {
        this.mes = mes;
        this.hoje = hoje;
        this.atividades = atividades;
        repaint();
    }

    /** Sempre desenha: um mês sem atividades ainda mostra os dias e as faixas vazias. */
    @Override
    protected boolean temDados() {
        return true;
    }

    @Override
    protected void desenhar(Graphics2D g2, int largura, int altura) {
        g2.setFont(fontePequena());
        FontMetrics fm = g2.getFontMetrics();
        int esquerda = 90;
        int direita = largura - 8;
        int dias = mes.lengthOfMonth();
        double larguraDia = (direita - esquerda) / (double) dias;
        int alturaFaixa = Math.max(24, (altura - 34) / FAIXAS.length);
        int base = 6 + alturaFaixa * FAIXAS.length;

        // Fins de semana sombreados e números dos dias
        int passo = larguraDia < fm.stringWidth("00") + 4 ? 2 : 1;
        for (int dia = 1; dia <= dias; dia++) {
            LocalDate data = mes.atDay(dia);
            int x = xDoDia(esquerda, larguraDia, dia);
            if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                g2.setColor(new Color(0x2C2C2C));
                g2.fillRect((int) (esquerda + larguraDia * (dia - 1)), 6, (int) Math.ceil(larguraDia), base - 6);
            }
            if (dia == 1 || dia % passo == 1 || passo == 1) {
                g2.setColor(data.equals(hoje) ? Cores.VERDE : Cores.TEXTO_SECUNDARIO);
                textoCentralizado(g2, String.valueOf(dia), x, base + 12);
            }
        }

        // Nomes e linhas das faixas
        for (int f = 0; f < FAIXAS.length; f++) {
            int centro = 6 + alturaFaixa * f + alturaFaixa / 2;
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString(FAIXAS[f], 4, centro + fm.getAscent() / 2 - 1);
            g2.setColor(COR_GRADE);
            g2.drawLine(esquerda, centro, direita, centro);
        }

        desenharCompeticoes(g2, esquerda, larguraDia, 6 + alturaFaixa + alturaFaixa / 2);
        desenharPontos(g2, esquerda, larguraDia, 6 + alturaFaixa / 2, Treino.class);
        desenharPontos(g2, esquerda, larguraDia, 6 + alturaFaixa * 2 + alturaFaixa / 2, Evento.class);

        // Marcação de hoje
        if (YearMonth.from(hoje).equals(mes)) {
            int x = xDoDia(esquerda, larguraDia, hoje.getDayOfMonth());
            g2.setColor(Cores.VERDE);
            g2.drawLine(x, 2, x, base);
        }
    }

    /** Um ponto por dia com atividades do tipo; o número aparece quando há mais de uma no dia. */
    private void desenharPontos(Graphics2D g2, int esquerda, double larguraDia, int centro, Class<? extends Atividade> tipo) {
        atividades.forEach((dia, lista) -> {
            List<Atividade> doTipo = lista.stream().filter(tipo::isInstance).toList();
            if (doTipo.isEmpty()) {
                return;
            }
            int x = xDoDia(esquerda, larguraDia, dia.getDayOfMonth());
            int raio = doTipo.size() > 1 ? 8 : 5;
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fill(new Ellipse2D.Double(x - raio - 2, centro - raio - 2, raio * 2 + 4, raio * 2 + 4));
            g2.setColor(Color.decode(doTipo.get(0).getCorHex()));
            Ellipse2D ponto = new Ellipse2D.Double(x - raio, centro - raio, raio * 2, raio * 2);
            g2.fill(ponto);
            if (doTipo.size() > 1) {
                g2.setColor(Cores.TEXTO);
                textoCentralizado(g2, String.valueOf(doTipo.size()), x, centro);
            }
            areaComDica(new Rectangle(x - 9, centro - 11, 18, 22), dica(dia, doTipo));
        });
    }

    /** Barra do primeiro ao último dia de cada competição dentro do mês. */
    private void desenharCompeticoes(Graphics2D g2, int esquerda, double larguraDia, int centro) {
        List<Competicao> competicoes = new ArrayList<>();
        atividades.values().forEach(lista -> lista.stream()
                .filter(a -> a instanceof Competicao && !competicoes.contains(a))
                .forEach(a -> competicoes.add((Competicao) a)));
        for (Competicao competicao : competicoes) {
            LocalDate inicio = competicao.getData().isBefore(mes.atDay(1)) ? mes.atDay(1) : competicao.getData();
            LocalDate fimReal = competicao.getDataFim() == null ? competicao.getData() : competicao.getDataFim();
            LocalDate fim = fimReal.isAfter(mes.atEndOfMonth()) ? mes.atEndOfMonth() : fimReal;
            int x1 = xDoDia(esquerda, larguraDia, inicio.getDayOfMonth()) - 5;
            int x2 = xDoDia(esquerda, larguraDia, fim.getDayOfMonth()) + 5;
            RoundRectangle2D barra = new RoundRectangle2D.Double(x1, centro - 5, x2 - x1, 10, 10, 10);
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fill(new RoundRectangle2D.Double(x1 - 2, centro - 7, x2 - x1 + 4, 14, 14, 14));
            g2.setColor(Color.decode(competicao.getCorHex()));
            g2.fill(barra);
            String periodo = competicao.getDataFim() == null ? Validador.FORMATO_DATA.format(competicao.getData())
                    : Validador.FORMATO_DATA.format(competicao.getData()) + " a " + Validador.FORMATO_DATA.format(fimReal);
            areaComDica(barra.getBounds(), competicao.getTitulo() + " (" + periodo + ")");
        }
    }

    private static int xDoDia(int esquerda, double larguraDia, int dia) {
        return (int) (esquerda + larguraDia * (dia - 1) + larguraDia / 2);
    }

    private static String dica(LocalDate dia, List<Atividade> lista) {
        return "<html><b>" + Validador.FORMATO_DATA.format(dia) + "</b><br>" + lista.stream()
                .map(a -> (a.getHorario() == null ? "" : Validador.FORMATO_HORARIO.format(a.getHorario()) + " · ")
                        + a.getTitulo().replace("<", "&lt;"))
                .collect(Collectors.joining("<br>")) + "</html>";
    }
}
