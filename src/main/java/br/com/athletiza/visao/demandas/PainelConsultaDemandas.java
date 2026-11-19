package br.com.athletiza.visao.demandas;

import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.PrioridadeDemanda;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PainelConsultaDemandas extends PainelConsulta<Demanda> {
    private static final int COLUNA_PRAZO = 4;

    private final JComboBox<SituacaoDemanda> filtroSituacao =
            Combos.comOpcaoTodos(List.of(SituacaoDemanda.values()), "Todas as situações");
    private final JComboBox<Membro> filtroResponsavel = Combos.comOpcaoTodos(List.of(), "Todos os responsáveis");
    private final JCheckBox minhas = new JCheckBox("Minhas demandas");
    private final JButton btnMudarSituacao = Botoes.contornoVerde("Mudar situação", null);

    public PainelConsultaDemandas(Navegador navegador) {
        super("Demandas", new ModeloTabela<Demanda>()
                .coluna("Demanda", String.class, Demanda::getTitulo)
                .coluna("Relacionada a", String.class, Demanda::getDescricaoRelacionada)
                .coluna("Responsáveis", String.class, Demanda::getNomesResponsaveis)
                .coluna("Solicitante", String.class, Demanda::getNomeSolicitante)
                .coluna("Prazo", LocalDate.class, Demanda::getPrazo)
                .coluna("Prioridade", PrioridadeDemanda.class, Demanda::getPrioridade)
                .coluna("Situação", SituacaoDemanda.class, Demanda::getSituacao), navegador);
        larguraColuna(0, 210);
        larguraColuna(1, 210);
        larguraColuna(2, 180);
        larguraColuna(3, 130);
        tabela.getColumnModel().getColumn(COLUNA_PRAZO).setMinWidth(110);
        tabela.getColumnModel().getColumn(5).setMinWidth(95);
        tabela.getColumnModel().getColumn(6).setMinWidth(135);
        tabela.getColumnModel().getColumn(COLUNA_PRAZO).setCellRenderer(new PrazoRenderer());

        minhas.setOpaque(false);
        minhas.setToolTipText("Só as demandas em que você é um dos responsáveis.");
        adicionarFiltro(minhas);
        adicionarFiltro(filtroSituacao);
        adicionarFiltro(filtroResponsavel);
        minhas.addActionListener(e -> carregar());
        filtroSituacao.addActionListener(e -> carregar());
        filtroResponsavel.addActionListener(e -> carregar());

        adicionarBotao(btnMudarSituacao);
    }

    public void adicionarAcaoBtnMudarSituacao(ActionListener acao) {
        btnMudarSituacao.addActionListener(acao);
    }

    public void apresentarFiltros(boolean mostrarMinhas, boolean mostrarResponsavel, List<Membro> membros) {
        minhas.setVisible(mostrarMinhas);
        filtroResponsavel.setVisible(mostrarResponsavel);
        Combos.atualizarItens(filtroResponsavel, membros);
    }

    public boolean isMinhasSelecionado() {
        return minhas.isSelected();
    }

    public Membro getResponsavelFiltro() {
        return (Membro) filtroResponsavel.getSelectedItem();
    }

    public SituacaoDemanda getSituacaoFiltro() {
        return (SituacaoDemanda) filtroSituacao.getSelectedItem();
    }

    public SituacaoDemanda pedirNovaSituacao(Demanda demanda, SituacaoDemanda[] opcoes) {
        return (SituacaoDemanda) JOptionPane.showInputDialog(this,
                "Nova situação de \"" + demanda.getTitulo() + "\":", "Mudar situação",
                JOptionPane.PLAIN_MESSAGE, null, opcoes, demanda.getSituacao());
    }

    private class PrazoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tab, Object valor, boolean selecionado,
                boolean foco, int linha, int coluna) {
            Demanda demanda = modelo.getLinha(tab.convertRowIndexToModel(linha));
            boolean atrasada = demanda.isAtrasada(LocalDate.now());
            String texto = valor instanceof LocalDate data ? Validador.FORMATO_DATA.format(data) : "";
            super.getTableCellRendererComponent(tab, atrasada ? texto + " · atrasada" : texto,
                    selecionado, false, linha, coluna);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            if (atrasada) {
                setForeground(Cores.VERMELHO);
            } else if (!selecionado) {
                setForeground(tab.getForeground());
            }
            setToolTipText(atrasada ? "Prazo vencido e a demanda ainda não foi concluída." : null);
            return this;
        }
    }
}
