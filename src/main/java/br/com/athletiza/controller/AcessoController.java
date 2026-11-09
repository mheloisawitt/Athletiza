package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Acesso;
import br.com.athletiza.model.Usuario;
import java.util.Optional;

/**
 * Descobre o acesso de um usuário (RN19): o administrador tem acesso total; os demais
 * recebem o perfil do cargo que o membro ligado a eles ocupa na gestão em andamento.
 */
public class AcessoController {

    private final CargoDAO cargoDAO = new CargoDAO();

    /** @return vazio se o usuário não tem membro ligado ou se o membro não tem cargo que dê acesso */
    public Optional<Acesso> calcular(Usuario usuario) throws PersistenciaException {
        if (usuario.isAdministrador()) {
            return Optional.of(Acesso.administrador());
        }
        if (usuario.getMembro() == null) {
            return Optional.empty();
        }
        return Acesso.pelosCargos(cargoDAO.listarDoMembroNaGestaoEmAndamento(usuario.getMembro().getId()));
    }

    /** Texto do acesso para a consulta de usuários (ex.: "Diretor · Esportes"). */
    public String descrever(Usuario usuario) throws PersistenciaException {
        return calcular(usuario).map(Acesso::getDescricao).orElse("Sem cargo na gestão em andamento");
    }
}
