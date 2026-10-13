package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.GestaoController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.Recarregavel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Organograma da gestão (CH-23, RF08): cada cargo ocupado é uma caixa com seus
 * membros, ligada ao cargo superior. Atualiza sempre que a tela é aberta.
 */
public class PainelOrganograma extends JPanel implements Recarregavel {

    private final Gestao gestao;
    private final GestaoController controller = new GestaoController();
    private final Desenho desenho = new Desenho();

    public PainelOrganograma(Gestao gestao, Navegador navegador) {
        super(new BorderLayout(0, 20));
        this.gestao = gestao;
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JButton voltar = Botoes.voltar(e -> navegador.voltar());
        JLabel titulo = new JLabel("Organograma · " + gestao.getNome());
        titulo.setFont(Tema.fonteTitulo());
        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cabecalho.setOpaque(false);
        cabecalho.add(voltar);
        cabecalho.add(Box.createHorizontalStrut(12));
        cabecalho.add(titulo);
        add(cabecalho, BorderLayout.NORTH);

        JScrollPane rolagem = new JScrollPane(desenho);
        rolagem.setBorder(BorderFactory.createLineBorder(Cores.CINZA_ESCURO));
        rolagem.getViewport().setBackground(Cores.FUNDO_CARTAO);
        add(rolagem, BorderLayout.CENTER);
    }

    @Override
    public void carregar() {
        Gestao atualizada = new Gestao(gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim());
        atualizada.setId(gestao.getId());
        try {
            controller.carregarComposicao(atualizada);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
        desenho.montar(atualizada.getOrganograma());
    }

    /** Caixa de um cargo no desenho, com os cargos subordinados a ele. */
    private static final class No {

        final Cargo cargo;
        final List<Membro> membros;
        final List<No> filhos = new ArrayList<>();
        int x;
        int y;
        int larguraSubarvore;

        No(Cargo cargo, List<Membro> membros) {
            this.cargo = cargo;
            this.membros = membros;
        }

        int altura() {
            return 38 + membros.size() * 18;
        }
    }

    /** Área de desenho: calcula a posição de cada caixa (de cima para baixo) e desenha. */
    private static final class Desenho extends JComponent {

        private static final int LARGURA_CAIXA = 200;
        private static final int ESPACO_HORIZONTAL = 24;
        private static final int ESPACO_VERTICAL = 56;
        private static final int MARGEM = 32;

        private final List<No> raizes = new ArrayList<>();
        private final List<Integer> alturaDosNiveis = new ArrayList<>();

        void montar(Map<Cargo, List<Membro>> organograma) {
            raizes.clear();
            alturaDosNiveis.clear();
            Map<Cargo, No> nos = new HashMap<>();
            organograma.forEach((cargo, membros) -> nos.put(cargo, new No(cargo, membros)));

            // Cada cargo fica abaixo do superior mais próximo que esteja ocupado nesta gestão
            for (Cargo cargo : organograma.keySet()) {
                Cargo acima = cargo.getCargoSuperior();
                while (acima != null && !nos.containsKey(acima)) {
                    acima = acima.getCargoSuperior();
                }
                (acima == null ? raizes : nos.get(acima).filhos).add(nos.get(cargo));
            }

            int x = MARGEM;
            for (No raiz : raizes) {
                calcularAlturas(raiz, 0);
            }
            for (No raiz : raizes) {
                calcularLargura(raiz);
                posicionar(raiz, x, 0);
                x += raiz.larguraSubarvore + ESPACO_HORIZONTAL;
            }
            int alturaTotal = MARGEM * 2 + alturaDosNiveis.stream().mapToInt(Integer::intValue).sum()
                    + Math.max(0, alturaDosNiveis.size() - 1) * ESPACO_VERTICAL;
            setPreferredSize(new Dimension(Math.max(x - ESPACO_HORIZONTAL + MARGEM, 400), Math.max(alturaTotal, 200)));
            revalidate();
            repaint();
        }

        private void calcularAlturas(No no, int nivel) {
            while (alturaDosNiveis.size() <= nivel) {
                alturaDosNiveis.add(0);
            }
            alturaDosNiveis.set(nivel, Math.max(alturaDosNiveis.get(nivel), no.altura()));
            no.filhos.forEach(filho -> calcularAlturas(filho, nivel + 1));
        }

        private int calcularLargura(No no) {
            int filhos = 0;
            for (No filho : no.filhos) {
                filhos += calcularLargura(filho);
            }
            filhos += Math.max(0, no.filhos.size() - 1) * ESPACO_HORIZONTAL;
            no.larguraSubarvore = Math.max(LARGURA_CAIXA, filhos);
            return no.larguraSubarvore;
        }

        /** Centraliza a caixa sobre a faixa ocupada pelos seus subordinados. */
        private void posicionar(No no, int inicio, int nivel) {
            no.x = inicio + (no.larguraSubarvore - LARGURA_CAIXA) / 2;
            no.y = MARGEM;
            for (int i = 0; i < nivel; i++) {
                no.y += alturaDosNiveis.get(i) + ESPACO_VERTICAL;
            }
            int larguraFilhos = no.filhos.stream().mapToInt(f -> f.larguraSubarvore).sum()
                    + Math.max(0, no.filhos.size() - 1) * ESPACO_HORIZONTAL;
            int x = inicio + (no.larguraSubarvore - larguraFilhos) / 2;
            for (No filho : no.filhos) {
                posicionar(filho, x, nivel + 1);
                x += filho.larguraSubarvore + ESPACO_HORIZONTAL;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Cores.FUNDO_CARTAO);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.translate(Math.max(0, (getWidth() - getPreferredSize().width) / 2), 0);
            if (raizes.isEmpty()) {
                g2.setColor(Cores.TEXTO_SECUNDARIO);
                g2.setFont(Tema.fonte(Font.PLAIN, 14f));
                g2.drawString("Nenhum membro em cargos nesta gestão. Use \"Adicionar membro\" na tela anterior.", MARGEM, MARGEM + 10);
            }
            raizes.forEach(raiz -> desenharLigacoes(g2, raiz));
            raizes.forEach(raiz -> desenharCaixas(g2, raiz, true));
            g2.dispose();
        }

        private void desenharLigacoes(Graphics2D g2, No no) {
            g2.setColor(Cores.CINZA_ESCURO.brighter());
            g2.setStroke(new BasicStroke(2));
            int xPai = no.x + LARGURA_CAIXA / 2;
            int yPai = no.y + no.altura();
            for (No filho : no.filhos) {
                int xFilho = filho.x + LARGURA_CAIXA / 2;
                int yMeio = filho.y - ESPACO_VERTICAL / 2;
                g2.drawLine(xPai, yPai, xPai, yMeio);
                g2.drawLine(xPai, yMeio, xFilho, yMeio);
                g2.drawLine(xFilho, yMeio, xFilho, filho.y);
                desenharLigacoes(g2, filho);
            }
        }

        private void desenharCaixas(Graphics2D g2, No no, boolean topo) {
            g2.setColor(Cores.FUNDO);
            g2.fillRoundRect(no.x, no.y, LARGURA_CAIXA, no.altura(), 14, 14);
            g2.setStroke(new BasicStroke(topo ? 2 : 1));
            g2.setColor(topo ? Cores.VERDE : Cores.ROXO);
            g2.drawRoundRect(no.x, no.y, LARGURA_CAIXA, no.altura(), 14, 14);

            g2.setFont(Tema.fonte(Font.BOLD, 13f));
            FontMetrics metricas = g2.getFontMetrics();
            g2.setColor(topo ? Cores.VERDE : new java.awt.Color(0xB98AF0));
            centralizar(g2, no.cargo.getNome(), no.x, no.y + 22, metricas);

            g2.setFont(Tema.fonte(Font.PLAIN, 12f));
            metricas = g2.getFontMetrics();
            g2.setColor(Cores.TEXTO);
            int y = no.y + 42;
            for (Membro membro : no.membros) {
                centralizar(g2, membro.getNome(), no.x, y, metricas);
                y += 18;
            }
            no.filhos.forEach(filho -> desenharCaixas(g2, filho, false));
        }

        private static void centralizar(Graphics2D g2, String texto, int xCaixa, int y, FontMetrics metricas) {
            g2.drawString(texto, xCaixa + (LARGURA_CAIXA - metricas.stringWidth(texto)) / 2, y);
        }
    }
}
