package br.com.athletiza.view.demandas;

import br.com.athletiza.controller.DemandaController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Modulo;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.util.Cores;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PainelConsultaDemandas extends PainelConsulta<Demanda> {
    private static final int COLUNA_PRAZO = 4;

    private final DemandaController controller = new DemandaController();
    private final Navegador navegador;
    private final JComboBox<SituacaoDemanda> filtroSituacao =
            Combos.comOpcaoTodos(List.of(SituacaoDemanda.values()), "Todas as situações");
    private final JComboBox<Membro> filtroResponsavel = Combos.comOpcaoTodos(List.of(), "Todos os responsáveis");
    private final JCheckBox minhas = new JCheckBox("Minhas demandas");

    public PainelConsultaDemandas(Navegador navegador) {
        super("Demandas", new ModeloTabela<Demanda>()
                .coluna("Demanda", String.class, Demanda::getTitulo)
                .coluna("Relacionada a", String.class, Demanda::getDescricaoRelacionada)
                .coluna("Responsáveis", String.class, Demanda::getNomesResponsaveis)
                .coluna("Solicitante", String.class, Demanda::getNomeSolicitante)
                .coluna("Prazo", LocalDate.class, Demanda::getPrazo)
                .coluna("Prioridade", PrioridadeDemanda.class, Demanda::getPrioridade)
                .coluna("Situação", SituacaoDemanda.class, Demanda::getSituacao));
        this.navegador = navegador;
        definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.DEMANDAS));
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

        adicionarBotao(Botoes.contornoVerde("Mudar situação", e -> mudarSituacao()));
    }

    @Override
    protected List<Demanda> buscarDados() throws AthletizaException {
        Optional<Membro> eu = controller.membroLogado();
        boolean assessor = Sessao.getAcesso().getPerfil() == Perfil.ASSESSOR;

        minhas.setVisible(eu.isPresent() && !assessor);
        filtroResponsavel.setVisible(!assessor);
        Combos.atualizarItens(filtroResponsavel, new MembroController().listar());

        Membro responsavel = assessor || minhas.isSelected() && eu.isPresent()
                ? eu.orElse(null) : (Membro) filtroResponsavel.getSelectedItem();
        if (assessor && responsavel == null) {
            return List.of();
        }
        return controller.pesquisar((SituacaoDemanda) filtroSituacao.getSelectedItem(), responsavel);
    }

    @Override
    protected boolean podeAlterar(Demanda demanda) {
        return Sessao.getAcesso().podeAlterar(demanda);
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroDemanda(new Demanda(), navegador));
    }

    @Override
    protected void editar(Demanda demanda) {
        navegador.abrir(new PainelCadastroDemanda(demanda, navegador));
    }

    @Override
    protected void excluir(Demanda demanda) {
        try {
            controller.excluir(demanda);
            Mensagens.sucesso(this, "Demanda excluída.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }

    private void mudarSituacao() {
        Optional<Demanda> selecionada = getSelecionado();
        if (selecionada.isEmpty()) {
            Mensagens.aviso(this, "Selecione uma demanda na tabela.");
            return;
        }
        Demanda demanda = selecionada.get();
        if (!controller.podeMudarSituacao(demanda)) {
            Mensagens.aviso(this, "Só os responsáveis pela demanda ou quem pode editá-la mudam a situação.");
            return;
        }
        SituacaoDemanda[] opcoes = Arrays.stream(SituacaoDemanda.values())
                .filter(s -> s != SituacaoDemanda.CANCELADA || controller.podeCancelar(demanda))
                .toArray(SituacaoDemanda[]::new);
        SituacaoDemanda nova = (SituacaoDemanda) JOptionPane.showInputDialog(this,
                "Nova situação de \"" + demanda.getTitulo() + "\":", "Mudar situação",
                JOptionPane.PLAIN_MESSAGE, null, opcoes, demanda.getSituacao());
        if (nova == null || nova == demanda.getSituacao()) {
            return;
        }
        try {
            controller.mudarSituacao(demanda, nova);
            Mensagens.sucesso(this, "Situação alterada para \"" + nova + "\".");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
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
