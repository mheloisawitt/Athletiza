package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Cargo;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;

/**
 * Consulta dos cargos da gestão em ordem hierárquica (CH-18, RF07).
 * As ações ficam no ConsultaCargosController.
 */
public class PainelConsultaCargos extends PainelConsulta<Cargo> {

    public PainelConsultaCargos() {
        super("Cargos", new ModeloTabela<Cargo>()
                .coluna("Cargo", String.class, c -> c.getNome())
                .coluna("Nível", Integer.class, c -> c.getOrdem())
                .coluna("Subordinado a", String.class,
                        c -> c.getCargoSuperior() == null ? "—" : c.getCargoSuperior().getNome()));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
    }
}
