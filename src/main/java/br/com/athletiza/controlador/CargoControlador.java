package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.repositorio.CargoRepositorio;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.gestao.PainelCadastroCargo;
import br.com.athletiza.visao.gestao.PainelConsultaCargos;
import java.util.ArrayList;
import java.util.List;

public class CargoControlador {
    private PainelConsultaCargos visao;
    private final CargoRepositorio repositorio;

    public CargoControlador(CargoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public CargoControlador(PainelConsultaCargos visao, CargoRepositorio repositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Cargo()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionado());
        visao.apresentarTela();
    }

    public void carregar() {
        try {
            visao.apresentarDados(listar());
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editar() {
        Cargo cargo = visao.getSelecionado();
        if (cargo != null) {
            abrirCadastro(cargo);
        }
    }

    private void excluirSelecionado() {
        Cargo cargo = visao.getSelecionado();
        if (cargo == null || !visao.confirmarExclusao(cargo)) {
            return;
        }
        try {
            excluir(cargo);
            visao.apresentarMensagem("Cargo excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Cargo cargo) {
        PainelCadastroCargo cadastro = new PainelCadastroCargo(visao.getNavegador());
        try {
            List<Cargo> outros = new ArrayList<>();
            for (Cargo c : listar()) {
                if (!c.equals(cargo)) {
                    outros.add(c);
                }
            }
            cadastro.apresentarCargo(cargo, outros);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, cargo));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroCargo cadastro, Cargo cargo) {
        try {
            cargo.setNome(cadastro.getNome());
            cargo.setOrdem(cadastro.getNivel());
            cargo.setCargoSuperior(cadastro.getCargoSuperior());
            cargo.setSetor(cadastro.getSetor());
            salvar(cargo);
            cadastro.apresentarMensagem("Cargo salvo com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    public List<Cargo> listar() throws PersistenciaException {
        return repositorio.listarTodos().stream().sorted().toList();
    }

    public void salvar(Cargo cargo) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(cargo.getNome(), "Nome")
                .tamanhoMaximo(cargo.getNome(), 60, "Nome")
                .regra(cargo.getOrdem() >= 1, "O \"Nível\" deve ser 1 ou maior (1 = topo do organograma).")
                .validar();

        Cargo superior = cargo.getCargoSuperior();
        if (superior != null) {
            if (superior.getOrdem() >= cargo.getOrdem()) {
                throw new RegraNegocioException("O cargo superior (" + superior + ", nível " + superior.getOrdem()
                        + ") deve ter nível menor que o deste cargo.");
            }
            for (Cargo acima = superior; acima != null; acima = acima.getCargoSuperior()) {
                if (!cargo.isNova() && cargo.getId().equals(acima.getId())) {
                    throw new RegraNegocioException("Um cargo não pode ficar subordinado a si mesmo.");
                }
            }
        }
        boolean repetido = repositorio.listarTodos().stream()
                .anyMatch(c -> c.equals(cargo) && !c.getId().equals(cargo.getId()));
        if (repetido) {
            throw new RegraNegocioException("O cargo " + cargo.getNome() + " já está cadastrado.");
        }
        if (cargo.isNova()) {
            repositorio.inserir(cargo);
        } else {
            repositorio.atualizar(cargo);
        }
    }

    public void excluir(Cargo cargo) throws RegraNegocioException, PersistenciaException {
        int ocupacoes = repositorio.contarOcupacoes(cargo.getId());
        if (ocupacoes > 0) {
            throw new RegraNegocioException("Não é possível excluir o cargo " + cargo.getNome()
                    + ": ele está ocupado " + ocupacoes + " vez(es) em gestões.");
        }
        repositorio.excluir(cargo.getId());
    }
}
