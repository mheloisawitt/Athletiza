package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.GestaoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

/**
 * Consulta das gestões da atlética, ordenadas por período (CH-19).
 * "Gerenciar" abre os cargos e membros da gestão selecionada.
 */
public class PainelConsultaGestoes extends PainelConsulta<Gestao> {

    private final GestaoController controller = new GestaoController();
    private final Navegador navegador;

    public PainelConsultaGestoes(Navegador navegador) {
        super("Gestões", new ModeloTabela<Gestao>()
                .coluna("Gestão", String.class, Gestao::getNome)
                .coluna("Período", String.class, Gestao::getPeriodo)
                .coluna("Início", LocalDate.class, Gestao::getDataInicio)
                .coluna("Fim", LocalDate.class, Gestao::getDataFim)
                .coluna("Situação", SituacaoGestao.class, Gestao::getSituacao),
                EnumSet.allOf(Acao.class));
        this.navegador = navegador;
        adicionarBotao(Botoes.contorno("Cargos", e -> navegador.abrir(new PainelConsultaCargos(navegador))));
        adicionarBotao(Botoes.contorno("Membros", e -> navegador.abrir(new PainelConsultaMembros(navegador))));
    }

    @Override
    protected List<Gestao> buscarDados() throws AthletizaException {
        return controller.listar();
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroGestao(new Gestao(), navegador));
    }

    @Override
    protected void editar(Gestao gestao) {
        navegador.abrir(new PainelCadastroGestao(gestao, navegador));
    }

    @Override
    protected void gerenciar(Gestao gestao) {
        navegador.abrir(new PainelGerenciarGestao(gestao, navegador));
    }

    /** Se a gestão tem membros em cargos, avisa que eles também serão removidos (CH-18). */
    @Override
    protected void excluir(Gestao gestao) {
        try {
            int membros = controller.contarMembros(gestao);
            if (membros > 0 && !Mensagens.confirmar(this, "A " + gestao.getNome() + " possui " + membros
                    + " membro(s) em cargos.\nEssa composição também será excluída. Continuar?")) {
                return;
            }
            controller.excluir(gestao);
            Mensagens.sucesso(this, "Gestão excluída.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
