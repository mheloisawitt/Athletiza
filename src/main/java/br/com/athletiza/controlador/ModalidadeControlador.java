package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Genero;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.repositorio.ModalidadeRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.atletas.PainelCadastroModalidade;
import br.com.athletiza.visao.atletas.PainelConsultaModalidades;
import java.util.List;

public class ModalidadeControlador {
    private PainelConsultaModalidades visao;
    private final ModalidadeRepositorio repositorio;

    public ModalidadeControlador(ModalidadeRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public ModalidadeControlador(PainelConsultaModalidades visao, ModalidadeRepositorio repositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.ATLETAS));
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Modalidade()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
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
        Modalidade modalidade = visao.getSelecionado();
        if (modalidade != null) {
            abrirCadastro(modalidade);
        }
    }

    private void excluirSelecionada() {
        Modalidade modalidade = visao.getSelecionado();
        if (modalidade == null || !visao.confirmarExclusao(modalidade)) {
            return;
        }
        try {
            excluir(modalidade);
            visao.apresentarMensagem("Modalidade excluída.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Modalidade modalidade) {
        PainelCadastroModalidade cadastro = new PainelCadastroModalidade(visao.getNavegador());
        cadastro.apresentarModalidade(modalidade);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, modalidade));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroModalidade cadastro, Modalidade modalidade) {
        String nomeAnterior = modalidade.getNome();
        Genero generoAnterior = modalidade.getGenero();
        try {
            modalidade.setNome(cadastro.getNome());
            modalidade.setGenero(cadastro.getGenero());
            salvar(modalidade);
            cadastro.apresentarMensagem("Modalidade salva com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            modalidade.setNome(nomeAnterior);
            modalidade.setGenero(generoAnterior);
            cadastro.apresentarErro(e);
        }
    }

    public List<Modalidade> listar() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    public void salvar(Modalidade modalidade) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(modalidade.getNome(), "Nome")
                .tamanhoMaximo(modalidade.getNome(), 50, "Nome")
                .obrigatorio(modalidade.getGenero(), "Gênero")
                .validar();

        boolean repetida = false;
        for (Modalidade m : repositorio.listarTodos()) {
            if (m.equals(modalidade) && !m.getId().equals(modalidade.getId())) {
                repetida = true;
            }
        }
        if (repetida) {
            throw new RegraNegocioException("A modalidade " + modalidade + " já está cadastrada.");
        }
        if (modalidade.isNova()) {
            repositorio.inserir(modalidade);
        } else {
            repositorio.atualizar(modalidade);
        }
    }

    public void excluir(Modalidade modalidade) throws RegraNegocioException, PersistenciaException {
        if (repositorio.possuiVinculos(modalidade.getId())) {
            throw new RegraNegocioException("Não é possível excluir a modalidade " + modalidade
                    + ": existem atletas, treinos ou competições vinculados a ela.");
        }
        repositorio.excluir(modalidade.getId());
    }
}
