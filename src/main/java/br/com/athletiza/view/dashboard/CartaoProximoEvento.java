package br.com.athletiza.view.dashboard;

import br.com.athletiza.model.Evento;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Destaque do próximo evento com contagem regressiva (dias, horas, minutos e segundos),
 * atualizada a cada segundo por um javax.swing.Timer.
 */
class CartaoProximoEvento extends JPanel {

    private final JLabel titulo = new JLabel("Nenhum evento agendado");
    private final JLabel detalhes = new JLabel(" ");
    private final JLabel[] numeros = new JLabel[4];
    private final Timer relogio = new Timer(1000, e -> atualizarContagem());
    private LocalDateTime momento;

    CartaoProximoEvento() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        titulo.setFont(Tema.fonte(Font.BOLD, 20f));
        detalhes.setForeground(Cores.TEXTO_SECUNDARIO);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(detalhes);

        JPanel contagem = new JPanel(new GridLayout(1, 4, 10, 0));
        contagem.setOpaque(false);
        String[] unidades = {"dias", "horas", "min", "seg"};
        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = new JLabel("--", SwingConstants.CENTER);
            numeros[i].setFont(Tema.fonte(Font.BOLD, 30f));
            numeros[i].setForeground(Cores.VERDE);
            JLabel unidade = new JLabel(unidades[i], SwingConstants.CENTER);
            unidade.setForeground(Cores.TEXTO_SECUNDARIO);
            JPanel bloco = new JPanel(new BorderLayout());
            bloco.setBackground(new Color(0x1C1C1C));
            bloco.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
            bloco.add(numeros[i], BorderLayout.CENTER);
            bloco.add(unidade, BorderLayout.SOUTH);
            contagem.add(bloco);
        }

        add(textos, BorderLayout.NORTH);
        add(contagem, BorderLayout.CENTER);
        setPreferredSize(new Dimension(300, 170));
    }

    /** Mostra o evento (ou a mensagem de que não há nenhum) e liga o relógio. */
    void mostrar(Evento evento) {
        if (evento == null) {
            momento = null;
            titulo.setText("Nenhum evento agendado");
            detalhes.setText("Cadastre um evento planejado ou confirmado para vê-lo aqui.");
            relogio.stop();
        } else {
            LocalTime horario = evento.getHorario() == null ? LocalTime.MIDNIGHT : evento.getHorario();
            momento = evento.getData().atTime(horario);
            titulo.setText(evento.getTitulo());
            detalhes.setText(evento.getTipoEvento() + " · " + Validador.FORMATO_DATA.format(evento.getData())
                    + (evento.getHorario() == null ? "" : " às " + Validador.FORMATO_HORARIO.format(evento.getHorario()))
                    + (evento.getLocal() == null ? "" : " · " + evento.getLocal()));
            relogio.start();
        }
        atualizarContagem();
    }

    private void atualizarContagem() {
        long segundos = momento == null ? 0 : Math.max(0, Duration.between(LocalDateTime.now(), momento).getSeconds());
        long[] partes = {segundos / 86400, segundos % 86400 / 3600, segundos % 3600 / 60, segundos % 60};
        for (int i = 0; i < numeros.length; i++) {
            numeros[i].setText(momento == null ? "--" : String.format("%02d", partes[i]));
        }
        if (momento != null && segundos == 0) {
            relogio.stop();
            detalhes.setText(detalhes.getText().startsWith("É hoje!") ? detalhes.getText() : "É hoje! " + detalhes.getText());
        }
    }

    /** Para o relógio quando o cartão sai da tela, para não ficar rodando à toa. */
    @Override
    public void removeNotify() {
        relogio.stop();
        super.removeNotify();
    }
}
