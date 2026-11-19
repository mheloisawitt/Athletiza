package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Acesso;
import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.util.List;

public class PainelConsultaCargos extends PainelConsulta<Cargo> {

    public PainelConsultaCargos(Navegador navegador) {
        super("Cargos", new ModeloTabela<Cargo>()
                .coluna("Cargo", String.class, Cargo::getNome)
                .coluna("Nível", Integer.class, Cargo::getOrdem)
                .coluna("Subordinado a", String.class,
                        c -> c.getCargoSuperior() == null ? "—" : c.getCargoSuperior().getNome())
                .coluna("Setor", String.class, c -> c.getSetor() == null ? "—" : c.getSetor().getDescricao())
                .coluna("Acesso de quem ocupa", String.class, PainelConsultaCargos::descreverAcesso), navegador);
        exibirVoltar(navegador::voltar);
    }

    private static String descreverAcesso(Cargo cargo) {
        return Acesso.pelosCargos(List.of(cargo)).map(Acesso::getDescricao).orElse("Sem acesso ao sistema");
    }
}
