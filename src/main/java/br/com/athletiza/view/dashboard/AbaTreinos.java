package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Aba Treinos: treinos realizados por mês, treinos por modalidade,
 * presença média por modalidade e a lista dos treinos da semana.
 */
class AbaTreinos extends AbaDashboard {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("EEE dd/MM",
            Locale.forLanguageTag("pt-BR"));

    private final GraficoColunas porMes = new GraficoColunas(new String[]{"Treinos", "Amistosos"},
            new Color[]{Grafico.CORES_SERIES[1], Grafico.CORES_SERIES[2]}, true);
    private final GraficoBarras porModalidade = new GraficoBarras(Grafico.CORES_SERIES[1]);
    private final GraficoBarras frequencia = new GraficoBarras(Grafico.CORES_SERIES[0], "%", 100);
    private final JPanel listaSemana = new JPanel();

    AbaTreinos() {
        listaSemana.setLayout(new BoxLayout(listaSemana, BoxLayout.Y_AXIS));
        listaSemana.setOpaque(false);
        JScrollPane rolagem = new JScrollPane(listaSemana);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);

        adicionarLinha(
                Cartoes.comTitulo("Treinos realizados por mês", "Últimos " + DashboardController.MESES
                        + " meses, até hoje", porMes),
                Cartoes.comTitulo("Próximos treinos da semana", "De hoje até os próximos 6 dias", rolagem));
        adicionarLinha(
                Cartoes.comTitulo("Treinos por modalidade", "Treinos e amistosos dos últimos "
                        + DashboardController.MESES + " meses", porModalidade),
                Cartoes.comTitulo("Presença média por modalidade", "Somente treinos com lista de presença", frequencia));
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        porMes.mostrar(controller.treinosPorMes(hoje));
        porModalidade.mostrar(controller.treinosPorModalidade(hoje));
        frequencia.mostrar(controller.frequenciaPorModalidade());
        mostrarSemana(controller.treinosDaSemana(hoje));
    }

    private void mostrarSemana(List<Treino> treinos) {
        listaSemana.removeAll();
        if (treinos.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum treino marcado para esta semana.");
            vazio.setForeground(Cores.TEXTO_SECUNDARIO);
            listaSemana.add(vazio);
        }
        for (Treino treino : treinos) {
            listaSemana.add(itemDaSemana(treino));
        }
        listaSemana.revalidate();
        listaSemana.repaint();
    }

    /** Linha da lista: dia e horário à esquerda, título e local à direita. */
    private static JPanel itemDaSemana(Treino treino) {
        String dia = FORMATO_DIA.format(treino.getData());
        JLabel quando = new JLabel("<html><b>" + dia.substring(0, 1).toUpperCase() + dia.substring(1) + "</b><br>"
                + (treino.getHorario() == null ? "--:--" : Validador.FORMATO_HORARIO.format(treino.getHorario())) + "</html>");
        quando.setFont(Tema.fonte(Font.PLAIN, 12f));
        String detalhe = (treino instanceof Amistoso ? "Amistoso · " : "") + treino.getModalidade()
                + (treino.getLocal() == null ? "" : " · " + treino.getLocal());
        JLabel oque = new JLabel("<html>" + treino.getTitulo().replace("<", "&lt;") + "<br><font color='"
                + Cores.hex(Cores.TEXTO_SECUNDARIO) + "'>" + detalhe.replace("<", "&lt;") + "</font></html>");
        oque.setFont(Tema.fonte(Font.PLAIN, 12f));

        JPanel item = new JPanel(new BorderLayout(14, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, Color.decode(treino.getCorHex())),
                BorderFactory.createEmptyBorder(4, 10, 4, 0)));
        item.add(quando, BorderLayout.WEST);
        item.add(oque, BorderLayout.CENTER);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, item.getPreferredSize().height + 6));

        JPanel comEspaco = new JPanel(new BorderLayout());
        comEspaco.setOpaque(false);
        comEspaco.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        comEspaco.add(item);
        comEspaco.setAlignmentX(LEFT_ALIGNMENT);
        comEspaco.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, item.getPreferredSize().height + 14));
        return comEspaco;
    }
}
