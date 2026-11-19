package br.com.athletiza.visao.usuarios;

import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.visao.componentes.ModeloTabela;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelConsulta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PainelConsultaUsuarios extends PainelConsulta<Usuario> {
    private final Map<Usuario, String> acessos;

    public PainelConsultaUsuarios(Navegador navegador) {
        this(navegador, new HashMap<>());
    }

    private PainelConsultaUsuarios(Navegador navegador, Map<Usuario, String> acessos) {
        super("Usuários", new ModeloTabela<Usuario>()
                .coluna("Nome", String.class, Usuario::getNome)
                .coluna("Login", String.class, Usuario::getLogin)
                .coluna("Membro", String.class, u -> u.getMembro() == null ? "" : u.getMembro().getNome())
                .coluna("Acesso", String.class, u -> acessos.getOrDefault(u, ""))
                .coluna("Situação", Situacao.class, u -> u.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO), navegador);
        this.acessos = acessos;
    }

    public void apresentarUsuarios(List<Usuario> usuarios, Map<Usuario, String> descricoes) {
        acessos.clear();
        acessos.putAll(descricoes);
        apresentarDados(usuarios);
    }
}
