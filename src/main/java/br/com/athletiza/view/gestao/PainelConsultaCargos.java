package br.com.athletiza.view.gestao;

import br.com.athletiza.controller.CargoController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Acesso;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.List;

/**
 * Consulta dos cargos da gestão em ordem hierárquica (CH-18, RF07).
 */
public class PainelConsultaCargos extends PainelConsulta<Cargo> {

    private final CargoController controller = new CargoController();
    private final Navegador navegador;

    public PainelConsultaCargos(Navegador navegador) {
        super("Cargos", new ModeloTabela<Cargo>()
                .coluna("Cargo", String.class, Cargo::getNome)
                .coluna("Nível", Integer.class, Cargo::getOrdem)
                .coluna("Subordinado a", String.class,
                        c -> c.getCargoSuperior() == null ? "—" : c.getCargoSuperior().getNome())
                .coluna("Setor", String.class, c -> c.getSetor() == null ? "—" : c.getSetor().getDescricao())
                .coluna("Acesso de quem ocupa", String.class, PainelConsultaCargos::descreverAcesso));
        this.navegador = navegador;
        exibirVoltar(navegador::voltar);
    }

    /** Perfil que o cargo dá a quem o ocupa na gestão em andamento. */
    private static String descreverAcesso(Cargo cargo) {
        return Acesso.pelosCargos(List.of(cargo)).map(Acesso::getDescricao).orElse("Sem acesso ao sistema");
    }

    @Override
    protected List<Cargo> buscarDados() throws AthletizaException {
        return controller.listar();
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroCargo(new Cargo(), navegador));
    }

    @Override
    protected void editar(Cargo cargo) {
        navegador.abrir(new PainelCadastroCargo(cargo, navegador));
    }

    @Override
    protected void excluir(Cargo cargo) {
        try {
            controller.excluir(cargo);
            Mensagens.sucesso(this, "Cargo excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
