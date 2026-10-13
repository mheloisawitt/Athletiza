package br.com.athletiza.view.gestao;

import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;

/**
 * Consulta dos membros da atlética (CH-22). A busca filtra por nome ou matrícula.
 * As ações ficam no ConsultaMembrosController.
 */
public class PainelConsultaMembros extends PainelConsulta<Membro> {

    public PainelConsultaMembros() {
        super("Membros", new ModeloTabela<Membro>()
                .coluna("Nome", String.class, m -> m.getNome())
                .coluna("Matrícula", String.class, m -> m.getMatricula())
                .coluna("Contato", String.class, m -> m.getContato())
                .coluna("Situação", Situacao.class, m -> m.getSituacao()));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        exibirVoltar();
    }
}
