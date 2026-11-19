package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Acesso;
import br.com.athletiza.model.Usuario;
import java.util.Optional;

public class AcessoController {
    private final CargoDAO cargoDAO = new CargoDAO();

    public Optional<Acesso> calcular(Usuario usuario) throws PersistenciaException {
        if (usuario.isAdministrador()) {
            return Optional.of(Acesso.administrador());
        }
        if (usuario.getMembro() == null) {
            return Optional.empty();
        }
        return Acesso.pelosCargos(cargoDAO.listarDoMembroNaGestaoEmAndamento(usuario.getMembro().getId()));
    }

    public String descrever(Usuario usuario) throws PersistenciaException {
        return calcular(usuario).map(Acesso::getDescricao).orElse("Sem cargo na gestão em andamento");
    }
}
