package br.com.athletiza.view.calendario;

import br.com.athletiza.model.Atividade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Visão da semana no calendário (RF10): sete colunas, de segunda a domingo,
 * com todas as atividades de cada dia e seus horários.
 */
class PainelSemana extends JPanel {

    private static final DateTimeFormatter FORMATO_CABECALHO =
            DateTimeFormatter.ofPattern("EEE dd/MM", Locale.forLanguageTag("pt-BR"));

    /** As sete colunas dividem a largura disponível; só há rolagem vertical. */
    private final JPanel colunas = new ColunasLarguraFixa();

    PainelSemana() {
        super(new BorderLayout());
        setOpaque(false);
        colunas.setOpaque(false);
        colunas.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Cores.CINZA_ESCURO));
        JScrollPane rolagem = new JScrollPane(colunas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setBorder(null);
        rolagem.getViewport().setOpaque(false);
        rolagem.setOpaque(false);
        add(rolagem, BorderLayout.CENTER);
    }

    /**
     * @param aoSelecionarDia clique em um dia
     * @param aoAbrir         clique duplo em uma atividade
     */
    void atualizar(LocalDate inicio, Map<LocalDate, List<Atividade>> atividades, LocalDate selecionado,
            Consumer<LocalDate> aoSelecionarDia, Consumer<Atividade> aoAbrir) {
        colunas.removeAll();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicio.plusDays(i);
            colunas.add(criarColuna(dia, atividades.getOrDefault(dia, List.of()), dia.equals(hoje),
                    dia.equals(selecionado), aoSelecionarDia, aoAbrir));
        }
        colunas.revalidate();
        colunas.repaint();
    }

    private static JPanel criarColuna(LocalDate dia, List<Atividade> atividades, boolean hoje, boolean selecionado,
            Consumer<LocalDate> aoSelecionarDia, Consumer<Atividade> aoAbrir) {
        String texto = dia.format(FORMATO_CABECALHO);
        JLabel cabecalho = new JLabel(Character.toUpperCase(texto.charAt(0)) + texto.substring(1), SwingConstants.CENTER);
        cabecalho.setFont(Tema.fonte(Font.BOLD, 12f));
        cabecalho.setOpaque(hoje);
        cabecalho.setBackground(Cores.VERDE);
        cabecalho.setForeground(hoje ? Cores.TEXTO_SOBRE_VERDE : Cores.TEXTO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JPanel itens = new JPanel();
        itens.setOpaque(false);
        itens.setLayout(new BoxLayout(itens, BoxLayout.Y_AXIS));
        itens.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        for (Atividade atividade : atividades) {
            JLabel item = criarItem(atividade);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    aoSelecionarDia.accept(dia);
                    if (e.getClickCount() == 2) {
                        aoAbrir.accept(atividade);
                    }
                }
            });
            itens.add(item);
            itens.add(Box.createVerticalStrut(6));
        }
        if (atividades.isEmpty()) {
            JLabel vazio = new JLabel("—");
            vazio.setForeground(new Color(0x5A5A5A));
            itens.add(vazio);
        }

        JPanel coluna = new JPanel(new BorderLayout());
        coluna.setBackground(selecionado ? Cores.VERDE_SELECAO : Cores.FUNDO_CARTAO);
        coluna.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Cores.CINZA_ESCURO));
        coluna.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        coluna.add(cabecalho, BorderLayout.NORTH);
        coluna.add(itens, BorderLayout.CENTER);
        coluna.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                aoSelecionarDia.accept(dia);
            }
        });
        return coluna;
    }

    /** Atividade com horário, título e uma faixa na cor do seu tipo; o texto quebra em várias linhas. */
    private static JLabel criarItem(Atividade atividade) {
        Color cor = Color.decode(atividade.getCorHex());
        String horario = atividade.getHorario() == null ? "" : "<b>" + Validador.FORMATO_HORARIO.format(atividade.getHorario())
                + "</b><br>";
        JLabel item = new JLabel("<html><div style='width:62px'>" + horario
                + atividade.getTitulo().replace("<", "&lt;") + "</div></html>");
        item.setFont(Tema.fonte(Font.PLAIN, 11f));
        item.setForeground(cor);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, cor), BorderFactory.createEmptyBorder(2, 6, 2, 2)));
        item.setToolTipText(atividade.getDescricaoCalendario()
                + (atividade.getLocal() == null ? "" : " · " + atividade.getLocal()));
        item.setAlignmentX(LEFT_ALIGNMENT);
        return item;
    }

    /** Painel que acompanha a largura da área visível, para as colunas nunca saírem da tela. */
    private static class ColunasLarguraFixa extends JPanel implements Scrollable {

        ColunasLarguraFixa() {
            super(new GridLayout(1, 7));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visivel, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visivel, int orientacao, int direcao) {
            return visivel.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getPreferredSize().height < getParent().getHeight();
        }
    }
}
