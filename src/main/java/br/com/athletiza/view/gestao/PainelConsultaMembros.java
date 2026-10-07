package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.List;

/**
 * Consulta dos membros da atlética (CH-22). A busca filtra por nome ou matrícula.
 */
public class PainelConsultaMembros extends PainelConsulta<Membro> {

    private final MembroController controller = new MembroController();
    private final Navegador navegador;

    public PainelConsultaMembros(Navegador navegador) {
        super("Membros", new ModeloTabela<Membro>()
                .coluna("Nome", String.class, Membro::getNome)
                .coluna("Matrícula", String.class, Membro::getMatricula)
                .coluna("Contato", String.class, Membro::getContato)
                .coluna("Situação", Situacao.class, Membro::getSituacao));
        this.navegador = navegador;
        exibirVoltar(navegador::voltar);
    }

    @Override
    protected List<Membro> buscarDados() throws AthletizaException {
        return controller.listar();
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroMembro(new Membro(), navegador));
    }

    @Override
    protected void editar(Membro membro) {
        navegador.abrir(new PainelCadastroMembro(membro, navegador));
    }

    @Override
    protected void excluir(Membro membro) {
        try {
            controller.excluir(membro);
            Mensagens.sucesso(this, "Membro excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
