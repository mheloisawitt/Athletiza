package br.com.athletiza.view.usuarios;

import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.PainelConsulta;

/**
 * Consulta dos usuários do sistema (RF01). Disponível apenas para administradores (CH-13).
 * As ações ficam no ConsultaUsuariosController.
 */
public class PainelConsultaUsuarios extends PainelConsulta<Usuario> {

    public PainelConsultaUsuarios() {
        super("Usuários", new ModeloTabela<Usuario>()
                .coluna("Nome", String.class, u -> u.getNome())
                .coluna("Login", String.class, u -> u.getLogin())
                .coluna("Perfil", String.class, u -> u.getPerfil().getDescricao())
                .coluna("Situação", Situacao.class, u -> u.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO));
    }
}
