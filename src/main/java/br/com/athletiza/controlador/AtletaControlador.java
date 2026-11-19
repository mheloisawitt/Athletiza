package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.repositorio.AtletaRepositorio;
import br.com.athletiza.repositorio.ModalidadeRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.atletas.PainelCadastroAtleta;
import br.com.athletiza.visao.atletas.PainelConsultaAtletas;
import br.com.athletiza.visao.atletas.PainelConsultaModalidades;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AtletaControlador {
    private PainelConsultaAtletas visao;
    private final AtletaRepositorio repositorio;
    private ModalidadeRepositorio modalidadeRepositorio;

    public AtletaControlador(AtletaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public AtletaControlador(PainelConsultaAtletas visao, AtletaRepositorio repositorio,
            ModalidadeRepositorio modalidadeRepositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        this.modalidadeRepositorio = modalidadeRepositorio;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.ATLETAS));
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Atleta()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionado());
        visao.adicionarAcaoBtnModalidades(e -> abrirModalidades());
    }

    public void carregar() {
        try {
            visao.apresentarModalidades(modalidadeRepositorio.listarTodos());
            visao.apresentarDados(pesquisar(null, visao.getModalidadeFiltro(), visao.getSituacaoFiltro(),
                    visao.getOrdem()));
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void abrirModalidades() {
        PainelConsultaModalidades modalidades = new PainelConsultaModalidades(visao.getNavegador());
        new ModalidadeControlador(modalidades, modalidadeRepositorio);
    }

    private void editar() {
        Atleta atleta = visao.getSelecionado();
        if (atleta != null) {
            abrirCadastro(atleta);
        }
    }

    private void excluirSelecionado() {
        Atleta atleta = visao.getSelecionado();
        if (atleta == null || !visao.confirmarExclusao(atleta)) {
            return;
        }
        try {
            excluir(atleta);
            visao.apresentarMensagem("Atleta excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Atleta atleta) {
        PainelCadastroAtleta cadastro = new PainelCadastroAtleta(visao.getNavegador());
        try {
            cadastro.apresentarAtleta(atleta, modalidadeRepositorio.listarTodos());
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, atleta));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroAtleta cadastro, Atleta atleta) {
        try {
            atleta.setNome(cadastro.getNome());
            atleta.setMatricula(cadastro.getMatricula());
            atleta.setDataNascimento(cadastro.getDataNascimento());
            atleta.setSituacao(cadastro.getSituacao());
            atleta.setContato(cadastro.getContato());
            atleta.setObservacoes(cadastro.getObservacoes());
            new ArrayList<>(atleta.getVinculos().keySet()).forEach(atleta::desvincularModalidade);
            cadastro.getModalidadesMarcadas().forEach(atleta::vincularModalidade);
            salvar(atleta);
            cadastro.apresentarMensagem("Atleta salvo com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    public List<Atleta> listar() throws PersistenciaException {
        return repositorio.listarTodos().stream().sorted().toList();
    }

    public List<Atleta> pesquisar(String texto, Modalidade modalidade, Situacao situacao, Comparator<Atleta> ordem)
            throws PersistenciaException {
        String busca = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return repositorio.listarTodos().stream()
                .filter(a -> busca.isEmpty() || a.getNome().toLowerCase(Locale.ROOT).contains(busca)
                        || a.getMatricula().contains(busca))
                .filter(a -> modalidade == null || a.praticaModalidade(modalidade))
                .filter(a -> situacao == null || a.getSituacao() == situacao)
                .sorted(ordem == null ? Comparator.naturalOrder() : ordem)
                .toList();
    }

    public void salvar(Atleta atleta) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(atleta.getNome(), "Nome")
                .tamanhoMaximo(atleta.getNome(), 100, "Nome")
                .obrigatorio(atleta.getMatricula(), "Matrícula")
                .apenasNumeros(atleta.getMatricula(), "Matrícula")
                .tamanhoMaximo(atleta.getMatricula(), 20, "Matrícula")
                .contato(atleta.getContato(), "Contato")
                .tamanhoMaximo(atleta.getContato(), 100, "Contato")
                .obrigatorio(atleta.getSituacao(), "Situação")
                .regra(atleta.getDataNascimento() == null || atleta.getDataNascimento().isBefore(LocalDate.now()),
                        "A \"Data de nascimento\" deve ser anterior a hoje.")
                .regra(!atleta.getVinculos().isEmpty(), "Selecione ao menos uma modalidade.")
                .tamanhoMaximo(atleta.getObservacoes(), 500, "Observações")
                .validar();

        if (repositorio.existeMatricula(atleta.getMatricula(), atleta.getId())) {
            throw new RegraNegocioException("Já existe um atleta com a matrícula " + atleta.getMatricula() + ".");
        }
        if (atleta.isNova()) {
            repositorio.inserir(atleta);
        } else {
            repositorio.atualizar(atleta);
        }
    }

    public void excluir(Atleta atleta) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = repositorio.listarVinculos(atleta.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + atleta.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        repositorio.excluir(atleta.getId());
    }
}
