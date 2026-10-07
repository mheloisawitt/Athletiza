package br.com.athletiza.view.calendario;

import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Lista de atividades em cartões, usada em "Próximos eventos" e nas
 * atividades do dia selecionado (CH-17).
 */
class ListaAtividades extends JPanel {

    ListaAtividades() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    /**
     * @param mostrarData exibe a data em cada cartão (desligado quando todos são do mesmo dia)
     * @param editar      ação de edição dos compromissos (null para não exibir os botões)
     * @param excluir     ação de exclusão dos compromissos (null para não exibir os botões)
     * @param abrir       abre treinos, competições e eventos na tela de edição (null para não exibir)
     */
    void mostrar(List<Atividade> atividades, boolean mostrarData, Consumer<Compromisso> editar,
            Consumer<Compromisso> excluir, Consumer<Atividade> abrir) {
        removeAll();
        if (atividades.isEmpty()) {
            JLabel vazio = new JLabel("Nenhuma atividade.");
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            vazio.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));
            vazio.setAlignmentX(LEFT_ALIGNMENT);
            add(vazio);
        }
        for (Atividade atividade : atividades) {
            Component cartao = criarCartao(atividade, mostrarData, editar, excluir, abrir);
            add(cartao);
            add(Box.createVerticalStrut(10));
        }
        revalidate();
        repaint();
    }

    private Component criarCartao(Atividade atividade, boolean mostrarData, Consumer<Compromisso> editar,
            Consumer<Compromisso> excluir, Consumer<Atividade> abrir) {
        Color cor = Color.decode(atividade.getCorHex());

        StringBuilder quando = new StringBuilder();
        if (mostrarData && atividade.getData() != null) {
            quando.append(atividade.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
        }
        if (atividade.getHorario() != null) {
            quando.append(quando.length() > 0 ? " - " : "").append(Validador.FORMATO_HORARIO.format(atividade.getHorario()));
        }
        JLabel rotuloQuando = new JLabel(quando.length() > 0 ? quando.toString() : "Dia todo");
        rotuloQuando.setFont(Tema.fonte(Font.PLAIN, 11f));
        rotuloQuando.setForeground(Cores.TEXTO_SECUNDARIO);

        JLabel titulo = new JLabel(atividade.getTitulo());
        titulo.setFont(Tema.fonte(Font.BOLD, 13f));

        String detalhe = atividade.getTipo().getDescricao()
                + (atividade.getLocal() == null || atividade.getLocal().isBlank() ? "" : " · " + atividade.getLocal());
        JLabel rotuloDetalhe = new JLabel(detalhe);
        rotuloDetalhe.setFont(Tema.fonte(Font.PLAIN, 11f));
        rotuloDetalhe.setForeground(cor);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(rotuloQuando);
        textos.add(Box.createVerticalStrut(2));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(rotuloDetalhe);

        JPanel cartao = new JPanel(new BorderLayout(8, 0));
        cartao.setOpaque(false);
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, cor),
                BorderFactory.createEmptyBorder(2, 10, 2, 0)));
        cartao.add(textos, BorderLayout.CENTER);
        cartao.setToolTipText(atividade.getObservacoes());

        if (atividade instanceof Compromisso compromisso && editar != null && excluir != null) {
            JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            acoes.setOpaque(false);
            acoes.add(link("Editar", Cores.TEXTO_SECUNDARIO, () -> editar.accept(compromisso)));
            acoes.add(link("Excluir", Cores.VERMELHO, () -> excluir.accept(compromisso)));
            cartao.add(acoes, BorderLayout.SOUTH);
        } else if (!(atividade instanceof Compromisso) && abrir != null) {
            JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            acoes.setOpaque(false);
            acoes.add(link("Abrir", Cores.TEXTO_SECUNDARIO, () -> abrir.accept(atividade)));
            cartao.add(acoes, BorderLayout.SOUTH);
        }
        cartao.setAlignmentX(LEFT_ALIGNMENT);
        cartao.setMaximumSize(new Dimension(Integer.MAX_VALUE, cartao.getPreferredSize().height));
        return cartao;
    }

    private static JButton link(String texto, Color cor, Runnable acao) {
        JButton botao = new JButton(texto);
        botao.putClientProperty(FlatClientProperties.STYLE,
                "buttonType:borderless; foreground:" + Cores.hex(cor) + "; font:-2; margin:2,6,2,6");
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.addActionListener(e -> acao.run());
        return botao;
    }
}
