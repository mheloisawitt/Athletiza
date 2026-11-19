package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.Resultado;
import br.com.athletiza.modelo.SituacaoCompeticao;
import br.com.athletiza.modelo.Tarefa;
import br.com.athletiza.repositorio.AtletaRepositorio;
import br.com.athletiza.repositorio.CompeticaoRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.repositorio.ModalidadeRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.visao.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.visao.competicoes.PainelGerenciarCompeticao;
import br.com.athletiza.visao.competicoes.PainelResultados;
import java.time.LocalDate;
import java.util.List;

public class CompeticaoControlador {
    private PainelConsultaCompeticoes visao;
    private final CompeticaoRepositorio repositorio;
    private AtletaRepositorio atletaRepositorio;
    private ModalidadeRepositorio modalidadeRepositorio;
    private MembroRepositorio membroRepositorio;

    public CompeticaoControlador(CompeticaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public CompeticaoControlador(PainelConsultaCompeticoes visao, CompeticaoRepositorio repositorio,
            AtletaRepositorio atletaRepositorio, ModalidadeRepositorio modalidadeRepositorio,
            MembroRepositorio membroRepositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        this.atletaRepositorio = atletaRepositorio;
        this.modalidadeRepositorio = modalidadeRepositorio;
        this.membroRepositorio = membroRepositorio;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.COMPETICOES));
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Competicao(), visao.getNavegador()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
        visao.adicionarAcaoBtnGerenciar(e -> gerenciar());
    }

    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            visao.apresentarDados(pesquisar(visao.getSituacaoFiltro(),
                    visao.isSomenteProximas() ? hoje : null,
                    visao.isSomenteAnteriores() ? hoje.minusDays(1) : null));
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editar() {
        Competicao competicao = visao.getSelecionado();
        if (competicao != null) {
            abrirCadastro(competicao, visao.getNavegador());
        }
    }

    private void excluirSelecionada() {
        Competicao competicao = visao.getSelecionado();
        if (competicao == null || !visao.confirmarExclusao(competicao)) {
            return;
        }
        try {
            carregarInscricoes(competicao);
            int inscricoes = competicao.getTotalInscricoes();
            int resultados = competicao.getResultados().size();
            if ((inscricoes > 0 || resultados > 0) && !visao.confirmar(competicao.getTitulo() + " possui "
                    + inscricoes + " inscrição(ões) de atletas e " + resultados + " resultado(s).\n"
                    + "Eles também serão excluídos. Continuar?")) {
                return;
            }
            excluir(competicao);
            visao.apresentarMensagem("Competição excluída.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    public void abrirCadastro(Competicao competicao, Navegador navegador) {
        PainelCadastroCompeticao cadastro = new PainelCadastroCompeticao(navegador);
        cadastro.apresentarCompeticao(competicao);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, competicao));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroCompeticao cadastro, Competicao competicao) {
        try {
            competicao.setTitulo(cadastro.getNome());
            competicao.setLocal(cadastro.getLocal());
            competicao.setData(cadastro.getDataInicio());
            competicao.setDataFim(cadastro.getDataFim());
            competicao.setHorario(cadastro.getHorario());
            competicao.setSituacao(cadastro.getSituacao());
            competicao.setObservacoes(cadastro.getObservacoes());
            salvar(competicao);
            cadastro.apresentarMensagem("Competição salva com sucesso. Ela já aparece no calendário.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    private void gerenciar() {
        Competicao competicao = visao.getSelecionado();
        if (competicao == null) {
            return;
        }
        PainelGerenciarCompeticao gerenciar = new PainelGerenciarCompeticao(competicao, visao.getNavegador());
        PainelResultados resultados = gerenciar.getResultados();
        resultados.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.COMPETICOES));
        gerenciar.getTarefas().definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.COMPETICOES));

        gerenciar.adicionarAcaoCarregar(() -> carregarGerenciar(gerenciar, competicao));
        gerenciar.adicionarAcaoBtnSalvarInscricoes(e -> salvarInscricoesNaTela(gerenciar));

        resultados.adicionarAcaoCarregar(() -> {
            try {
                resultados.apresentarDados(listarResultados(competicaoSalva(competicao)));
            } catch (PersistenciaException e) {
                resultados.apresentarErro(e);
            }
        });
        resultados.adicionarAcaoBtnIncluir(e -> editarResultado(resultados, competicao, new Resultado()));
        resultados.adicionarAcaoBtnEditar(e -> {
            Resultado resultado = resultados.getSelecionado();
            if (resultado != null) {
                editarResultado(resultados, competicao, resultado);
            }
        });
        resultados.adicionarAcaoBtnExcluir(e -> excluirResultadoNaTela(resultados));

        new TarefaControlador(gerenciar.getTarefas(), new TarefaControlador.FonteTarefas() {
            @Override
            public List<Tarefa> listar() throws AthletizaException {
                return listarTarefas(competicao);
            }

            @Override
            public void salvar(Tarefa tarefa) throws AthletizaException {
                salvarTarefa(competicao, tarefa);
            }

            @Override
            public void excluir(Tarefa tarefa) throws AthletizaException {
                excluirTarefa(tarefa);
            }
        }, membroRepositorio);
        gerenciar.apresentarTela();
    }

    private Competicao competicaoSalva(Competicao original) throws PersistenciaException {
        Competicao copia = new Competicao(original.getTitulo(), original.getData(), original.getDataFim(),
                original.getHorario(), original.getLocal());
        copia.setId(original.getId());
        copia.setSituacao(original.getSituacao());
        carregarInscricoes(copia);
        return copia;
    }

    private void carregarGerenciar(PainelGerenciarCompeticao gerenciar, Competicao competicao) {
        try {
            List<Atleta> atletas = new AtletaControlador(atletaRepositorio).listar();
            gerenciar.apresentarCompeticao(competicaoSalva(competicao), modalidadeRepositorio.listarTodos(), atletas);
            gerenciar.getResultados().carregar();
            gerenciar.getTarefas().carregar();
        } catch (PersistenciaException e) {
            gerenciar.apresentarErro(e);
        }
    }

    private void salvarInscricoesNaTela(PainelGerenciarCompeticao gerenciar) {
        try {
            salvarInscricoes(gerenciar.getCompeticao());
            gerenciar.apresentarMensagem("Inscrições salvas com sucesso.");
            gerenciar.carregar();
        } catch (AthletizaException e) {
            gerenciar.apresentarErro(e);
        }
    }

    private void editarResultado(PainelResultados resultados, Competicao original, Resultado resultado) {
        try {
            Competicao competicao = competicaoSalva(original);
            if (competicao.getModalidades().isEmpty()) {
                resultados.apresentarAviso("Inscreva ao menos uma modalidade (e salve) antes de lançar resultados.");
                return;
            }
            boolean continuar = true;
            while (continuar) {
                try {
                    if (resultados.apresentarFormulario(resultado, competicao)) {
                        salvarResultado(competicao, resultado);
                    }
                    continuar = false;
                } catch (AthletizaException e) {
                    resultados.apresentarErro(e);
                }
            }
        } catch (PersistenciaException e) {
            resultados.apresentarErro(e);
        }
        resultados.carregar();
    }

    private void excluirResultadoNaTela(PainelResultados resultados) {
        Resultado resultado = resultados.getSelecionado();
        if (resultado == null || !resultados.confirmarExclusao(resultado)) {
            return;
        }
        try {
            excluirResultado(resultado);
        } catch (AthletizaException e) {
            resultados.apresentarErro(e);
        }
        resultados.carregar();
    }

    public List<Competicao> pesquisar(SituacaoCompeticao situacao, LocalDate inicio, LocalDate fim)
            throws PersistenciaException {
        return repositorio.listarTodos().stream()
                .filter(c -> situacao == null || c.getSituacao() == situacao)
                .filter(c -> inicio == null || !terminoDe(c).isBefore(inicio))
                .filter(c -> fim == null || !c.getData().isAfter(fim))
                .sorted()
                .toList();
    }

    public void salvar(Competicao competicao) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(competicao.getTitulo(), "Nome")
                .tamanhoMaximo(competicao.getTitulo(), 100, "Nome")
                .obrigatorio(competicao.getData(), "Data de início")
                .periodo(competicao.getData(), competicao.getDataFim(), "Data de início", "Data de fim")
                .tamanhoMaximo(competicao.getLocal(), 100, "Local")
                .obrigatorio(competicao.getSituacao(), "Situação")
                .tamanhoMaximo(competicao.getObservacoes(), 500, "Observações")
                .validar();
        if (competicao.isNova()) {
            repositorio.inserir(competicao);
        } else {
            repositorio.atualizar(competicao);
        }
    }

    public void excluir(Competicao competicao) throws PersistenciaException {
        repositorio.excluir(competicao.getId());
    }

    public void carregarInscricoes(Competicao competicao) throws PersistenciaException {
        repositorio.carregarInscricoes(competicao);
    }

    public void salvarInscricoes(Competicao competicao) throws PersistenciaException {
        repositorio.salvarInscricoes(competicao);
    }

    public List<Resultado> listarResultados(Competicao competicao) throws PersistenciaException {
        return repositorio.listarResultados(competicao);
    }

    public void salvarResultado(Competicao competicao, Resultado resultado)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(resultado.getModalidade(), "Modalidade")
                .regra(resultado.getColocacao() == null || resultado.getColocacao() > 0,
                        "A \"Colocação\" deve ser maior que zero.")
                .regra(resultado.getColocacao() != null
                        || (resultado.getPlacar() != null && !resultado.getPlacar().isBlank()),
                        "Informe a colocação ou o placar.")
                .tamanhoMaximo(resultado.getPlacar(), 50, "Placar")
                .tamanhoMaximo(resultado.getObservacao(), 500, "Observação")
                .validar();

        if (!competicao.getModalidades().contains(resultado.getModalidade())) {
            throw new RegraNegocioException("A modalidade " + resultado.getModalidade() + " não está inscrita nesta competição.");
        }
        if (resultado.getAtleta() != null
                && !competicao.getAtletas(resultado.getModalidade()).contains(resultado.getAtleta())) {
            throw new RegraNegocioException(resultado.getAtleta().getNome() + " não está inscrito(a) em "
                    + resultado.getModalidade() + " nesta competição.");
        }
        repositorio.salvarResultado(competicao, resultado);
    }

    public List<Tarefa> listarTarefas(Competicao competicao) throws PersistenciaException {
        return repositorio.listarTarefas(competicao).stream().sorted().toList();
    }

    public void salvarTarefa(Competicao competicao, Tarefa tarefa) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(tarefa.getDescricao(), "Descrição")
                .tamanhoMaximo(tarefa.getDescricao(), 200, "Descrição")
                .obrigatorio(tarefa.getSituacao(), "Situação")
                .validar();
        repositorio.salvarTarefa(competicao, tarefa);
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        repositorio.excluirTarefa(tarefa);
    }

    public void excluirResultado(Resultado resultado) throws PersistenciaException {
        repositorio.excluirResultado(resultado);
    }

    private static LocalDate terminoDe(Competicao competicao) {
        return competicao.getDataFim() == null ? competicao.getData() : competicao.getDataFim();
    }
}
