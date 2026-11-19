package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Gestao;
import br.com.athletiza.modelo.SituacaoGestao;
import br.com.athletiza.visao.componentes.Botoes;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.EnumSet;
import javax.swing.JButton;

public class PainelConsultaGestoes extends PainelConsulta<Gestao> {
    private final JButton btnCargos = Botoes.contorno("Cargos", null);
    private final JButton btnMembros = Botoes.contorno("Membros", null);

    public PainelConsultaGestoes(Navegador navegador) {
        super("Gestões", new ModeloTabela<Gestao>()
                .coluna("Gestão", String.class, Gestao::getNome)
                .coluna("Período", String.class, Gestao::getPeriodo)
                .coluna("Início", LocalDate.class, Gestao::getDataInicio)
                .coluna("Fim", LocalDate.class, Gestao::getDataFim)
                .coluna("Situação", SituacaoGestao.class, Gestao::getSituacao),
                EnumSet.allOf(Acao.class), navegador);
        adicionarBotao(btnCargos);
        adicionarBotao(btnMembros);
    }

    public void adicionarAcaoBtnCargos(ActionListener acao) {
        btnCargos.addActionListener(acao);
    }

    public void adicionarAcaoBtnMembros(ActionListener acao) {
        btnMembros.addActionListener(acao);
    }
}
