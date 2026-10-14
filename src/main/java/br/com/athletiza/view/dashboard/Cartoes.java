package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Peças de layout do dashboard: o cartão com título que envolve cada gráfico,
 * o cartão de número (ex.: total de atletas) e as linhas da grade.
 */
final class Cartoes {

    /** Altura padrão de um cartão com gráfico. */
    static final int ALTURA_GRAFICO = 290;

    private Cartoes() {
    }

    /** Painel arredondado com o fundo dos cartões. */
    static JPanel fundo() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
        painel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return painel;
    }

    /** Cartão com título (e subtítulo opcional) e o conteúdo embaixo. */
    static JPanel comTitulo(String titulo, String subtitulo, JComponent conteudo) {
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(Tema.fonte(Font.BOLD, 14f));
        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);
        cabecalho.add(rotulo);
        if (subtitulo != null) {
            JLabel detalhe = new JLabel(subtitulo);
            detalhe.setFont(Tema.fonte(Font.PLAIN, 11f));
            detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
            cabecalho.add(detalhe);
        }
        JPanel cartao = fundo();
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(conteudo, BorderLayout.CENTER);
        cartao.setPreferredSize(new Dimension(300, ALTURA_GRAFICO));
        return cartao;
    }

    /** Linha da grade com os itens lado a lado, todos da mesma largura. */
    static JPanel linha(JComponent... itens) {
        JPanel linha = new JPanel(new GridLayout(1, itens.length, 16, 0));
        linha.setOpaque(false);
        for (JComponent item : itens) {
            linha.add(item);
        }
        linha.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        int altura = linha.getPreferredSize().height;
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
        return linha;
    }

    /** Cartão com um número grande (ex.: "Atletas ativos: 7"). */
    static final class Numero extends JPanel {

        private final JLabel valor = new JLabel("-");
        private final JLabel detalhe = new JLabel(" ");

        Numero(String titulo) {
            super(new BorderLayout(0, 2));
            putClientProperty(FlatClientProperties.STYLE, "arc:16; background:" + Cores.hex(Cores.FUNDO_CARTAO));
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            JLabel rotulo = new JLabel(titulo);
            rotulo.setFont(Tema.fonte(Font.PLAIN, 12f));
            rotulo.setForeground(Cores.TEXTO_SECUNDARIO);
            valor.setFont(Tema.fonte(Font.BOLD, 28f));
            detalhe.setFont(Tema.fonte(Font.PLAIN, 11f));
            detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
            add(rotulo, BorderLayout.NORTH);
            add(valor, BorderLayout.CENTER);
            add(detalhe, BorderLayout.SOUTH);
        }

        void mostrar(int numero, String textoDetalhe) {
            valor.setText(String.valueOf(numero));
            detalhe.setText(textoDetalhe == null ? " " : textoDetalhe);
        }
    }
}
