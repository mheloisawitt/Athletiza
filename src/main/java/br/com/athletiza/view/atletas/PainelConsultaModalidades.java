package br.com.athletiza.view.atletas;

import br.com.athletiza.controller.ModalidadeController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.List;

/**
 * Consulta das modalidades esportivas (CH-27), aberta a partir da consulta de atletas.
 */
public class PainelConsultaModalidades extends PainelConsulta<Modalidade> {

    private final ModalidadeController controller = new ModalidadeController();
    private final Navegador navegador;

    public PainelConsultaModalidades(Navegador navegador) {
        super("Modalidades", new ModeloTabela<Modalidade>()
                .coluna("Modalidade", String.class, Modalidade::getNome)
                .coluna("Gênero", String.class, m -> m.getGenero().getNomeCompleto()));
        this.navegador = navegador;
        exibirVoltar(navegador::voltar);
    }

    @Override
    protected List<Modalidade> buscarDados() throws AthletizaException {
        return controller.listar();
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroModalidade(new Modalidade(), navegador));
    }

    @Override
    protected void editar(Modalidade modalidade) {
        navegador.abrir(new PainelCadastroModalidade(modalidade, navegador));
    }

    @Override
    protected void excluir(Modalidade modalidade) {
        try {
            controller.excluir(modalidade);
            Mensagens.sucesso(this, "Modalidade excluída.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
