package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Usuario;

/**
 * Acesso aos usuários do sistema (RF01). Implementada por UsuarioDAO.
 */
public interface UsuarioRepository extends GenericDAO<Usuario> {

    /** Busca pelo login, sem diferenciar maiúsculas de minúsculas. Retorna null se não existir. */
    Usuario buscarPorLogin(String login) throws PersistenciaException;

    /** Indica se outro usuário (diferente de idIgnorado) já usa o login. */
    boolean existeLogin(String login, Integer idIgnorado) throws PersistenciaException;

    /** Quantos administradores ativos existem, sem contar o usuário informado. */
    int contarOutrosAdministradoresAtivos(Integer idIgnorado) throws PersistenciaException;
}
