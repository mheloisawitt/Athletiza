package br.com.athletiza.controlador;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Acesso;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.repositorio.CargoRepositorio;
import java.util.Optional;

public class AcessoControlador {
    private final CargoRepositorio cargoRepositorio;

    public AcessoControlador(CargoRepositorio cargoRepositorio) {
        this.cargoRepositorio = cargoRepositorio;
    }

    public Optional<Acesso> calcular(Usuario usuario) throws PersistenciaException {
        if (usuario.isAdministrador()) {
            return Optional.of(Acesso.administrador());
        }
        if (usuario.getMembro() == null) {
            return Optional.empty();
        }
        return Acesso.pelosCargos(cargoRepositorio.listarDoMembroNaGestaoEmAndamento(usuario.getMembro().getId()));
    }

    public String descrever(Usuario usuario) throws PersistenciaException {
        return calcular(usuario).map(Acesso::getDescricao).orElse("Sem cargo na gestão em andamento");
    }
}
