package br.com.athletiza.view.dashboard;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Aba Gestão: membros da gestão atual por cargo e o comparativo de treinos,
 * competições e eventos entre as gestões.
 */
class AbaGestao extends AbaDashboard {

    private final JLabel tituloGestao = new JLabel();
    private final JPanel cargos = new JPanel(new GridLayout(0, 3, 12, 12));
    private final GraficoColunas comparativo = new GraficoColunas(new String[]{"Treinos", "Competições", "Eventos"},
            new Color[]{Grafico.CORES_SERIES[1], Grafico.CORES_SERIES[0], Grafico.CORES_SERIES[4]}, false);
    private final JPanel linhaMembros;

    AbaGestao() {
        tituloGestao.setFont(Tema.fonte(Font.BOLD, 14f));
        cargos.setOpaque(false);
        JPanel cartaoMembros = Cartoes.fundo();
        cartaoMembros.add(tituloGestao, BorderLayout.NORTH);
        cartaoMembros.add(cargos, BorderLayout.CENTER);

        linhaMembros = adicionarLinha(cartaoMembros);
        adicionarLinha(Cartoes.comTitulo("Comparativo entre gestões",
                "Treinos, competições e eventos realizados no período de cada gestão (sem cancelados)", comparativo));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        mostrarGestaoAtual(controller.gestaoAtual(hoje));
        comparativo.mostrar(controller.comparativoGestoes());
    }

    private void mostrarGestaoAtual(Optional<Gestao> gestao) {
        cargos.removeAll();
        if (gestao.isEmpty()) {
            tituloGestao.setText("Membros da gestão atual");
            JLabel vazio = new JLabel("Nenhuma gestão em andamento.");
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            cargos.add(vazio);
        } else {
            tituloGestao.setText("Membros da gestão atual · " + gestao.get().getNome() + " (" + gestao.get().getPeriodo() + ")");
            Map<Cargo, List<Membro>> organograma = gestao.get().getOrganograma();
            if (organograma.isEmpty()) {
                JLabel vazio = new JLabel("A gestão ainda não tem membros.");
                vazio.setForeground(Cores.TEXTO_SECUNDARIO);
                cargos.add(vazio);
            }
            organograma.forEach((cargo, membros) -> cargos.add(cartaoCargo(cargo, membros)));
        }
        // A linha da grade usa a altura preferida: recalcula depois de trocar os cartões
        linhaMembros.setMaximumSize(new Dimension(Integer.MAX_VALUE, linhaMembros.getPreferredSize().height));
        revalidate();
        repaint();
    }

    /** Cartão de um cargo: nome do cargo em cima e os membros embaixo. */
    private static JPanel cartaoCargo(Cargo cargo, List<Membro> membros) {
        JLabel nomeCargo = new JLabel(cargo.getNome());
        nomeCargo.setFont(Tema.fonte(Font.PLAIN, 11f));
        nomeCargo.setForeground(Cores.TEXTO_SECUNDARIO);
        JLabel nomes = new JLabel("<html>" + membros.stream().map(m -> m.getNome().replace("<", "&lt;"))
                .collect(Collectors.joining("<br>")) + "</html>");
        nomes.setFont(Tema.fonte(Font.BOLD, 13f));

        JPanel cartao = new JPanel(new BorderLayout(0, 4));
        cartao.setBackground(new Color(0x1C1C1C));
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, Cores.ROXO),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        cartao.add(nomeCargo, BorderLayout.NORTH);
        cartao.add(nomes, BorderLayout.CENTER);
        return cartao;
    }
}
