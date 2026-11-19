package br.com.athletiza.visao.gestao;

import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;

public class PainelConsultaMembros extends PainelConsulta<Membro> {

    public PainelConsultaMembros(Navegador navegador) {
        super("Membros", new ModeloTabela<Membro>()
                .coluna("Nome", String.class, Membro::getNome)
                .coluna("Matrícula", String.class, Membro::getMatricula)
                .coluna("Contato", String.class, Membro::getContato)
                .coluna("Situação", Situacao.class, Membro::getSituacao), navegador);
        exibirVoltar(navegador::voltar);
    }
}
