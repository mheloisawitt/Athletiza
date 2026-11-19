package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Usuario;
import java.util.Optional;

public interface UsuarioRepositorio extends Repositorio<Usuario> {
    Optional<Usuario> buscarPorLogin(String login) throws PersistenciaException;

    boolean existeLogin(String login, Integer idIgnorado) throws PersistenciaException;

    int contarOutrosAdministradoresAtivos(Integer idIgnorado) throws PersistenciaException;
}
