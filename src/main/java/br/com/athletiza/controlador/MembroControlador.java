package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.gestao.PainelCadastroMembro;
import br.com.athletiza.visao.gestao.PainelConsultaMembros;
import java.util.List;

public class MembroControlador {
    private PainelConsultaMembros visao;
    private final MembroRepositorio repositorio;

    public MembroControlador(MembroRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public MembroControlador(PainelConsultaMembros visao, MembroRepositorio repositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Membro()));
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
        Membro membro = visao.getSelecionado();
        if (membro != null) {
            abrirCadastro(membro);
        }
    }

    private void excluirSelecionado() {
        Membro membro = visao.getSelecionado();
        if (membro == null || !visao.confirmarExclusao(membro)) {
            return;
        }
        try {
            excluir(membro);
            visao.apresentarMensagem("Membro excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Membro membro) {
        PainelCadastroMembro cadastro = new PainelCadastroMembro(visao.getNavegador());
        cadastro.apresentarMembro(membro);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, membro));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroMembro cadastro, Membro membro) {
        try {
            membro.setNome(cadastro.getNome());
            membro.setMatricula(cadastro.getMatricula());
            membro.setContato(cadastro.getContato());
            membro.setSituacao(cadastro.getSituacao());
            salvar(membro);
            cadastro.apresentarMensagem("Membro salvo com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    public List<Membro> listar() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    public List<Membro> listarAtivos() throws PersistenciaException {
        return repositorio.listarTodos().stream().filter(m -> m.getSituacao() == Situacao.ATIVO).toList();
    }

    public List<Membro> listarDaGestaoEmAndamento() throws PersistenciaException {
        return repositorio.listarDaGestaoEmAndamento();
    }

    public void salvar(Membro membro) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(membro.getNome(), "Nome")
                .tamanhoMaximo(membro.getNome(), 100, "Nome")
                .obrigatorio(membro.getMatricula(), "Matrícula")
                .apenasNumeros(membro.getMatricula(), "Matrícula")
                .tamanhoMaximo(membro.getMatricula(), 20, "Matrícula")
                .contato(membro.getContato(), "Contato")
                .tamanhoMaximo(membro.getContato(), 100, "Contato")
                .obrigatorio(membro.getSituacao(), "Situação")
                .validar();

        if (repositorio.existeMatricula(membro.getMatricula(), membro.getId())) {
            throw new RegraNegocioException("Já existe um membro com a matrícula " + membro.getMatricula() + ".");
        }
        if (membro.isNova()) {
            repositorio.inserir(membro);
        } else {
            repositorio.atualizar(membro);
        }
    }

    public void excluir(Membro membro) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = repositorio.listarVinculos(membro.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + membro.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        repositorio.excluir(membro.getId());
    }
}
