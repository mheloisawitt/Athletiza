package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.EnumSet;
import javax.swing.JButton;

/**
 * Consulta das gestões da atlética, ordenadas por período (CH-19).
 * "Gerenciar" abre os cargos e membros da gestão selecionada.
 * As ações ficam no ConsultaGestoesController.
 */
public class PainelConsultaGestoes extends PainelConsulta<Gestao> {

    private final JButton btnCargos = Botoes.contorno("Cargos", null);
    private final JButton btnMembros = Botoes.contorno("Membros", null);

    public PainelConsultaGestoes() {
        super("Gestões", new ModeloTabela<Gestao>()
                .coluna("Gestão", String.class, g -> g.getNome())
                .coluna("Período", String.class, g -> g.getPeriodo())
                .coluna("Início", LocalDate.class, g -> g.getDataInicio())
                .coluna("Fim", LocalDate.class, g -> g.getDataFim())
                .coluna("Situação", SituacaoGestao.class, g -> g.getSituacao()),
                EnumSet.allOf(Acao.class));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        adicionarBotao(btnCargos);
        adicionarBotao(btnMembros);
    }

    public void adicionarAcaoCargos(ActionListener acao) {
        btnCargos.addActionListener(acao);
    }

    public void adicionarAcaoMembros(ActionListener acao) {
        btnMembros.addActionListener(acao);
    }
}
