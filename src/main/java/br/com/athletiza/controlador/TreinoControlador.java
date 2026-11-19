package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Amistoso;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Frequencia;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.PresencaEmTreino;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.modelo.TipoAtividade;
import br.com.athletiza.modelo.Treino;
import br.com.athletiza.repositorio.AtletaRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.repositorio.ModalidadeRepositorio;
import br.com.athletiza.repositorio.TreinoRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.treinos.PainelCadastroTreino;
import br.com.athletiza.visao.treinos.PainelFrequencia;
import br.com.athletiza.visao.treinos.PainelHistoricoAtleta;
import br.com.athletiza.visao.treinos.PainelPresenca;
import br.com.athletiza.visao.treinos.PainelTreinos;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreinoControlador {
    static final Duration INTERVALO_MINIMO = Duration.ofHours(1);

    private PainelTreinos visao;
    private final TreinoRepositorio repositorio;
    private final AtletaRepositorio atletaRepositorio;
    private final ModalidadeRepositorio modalidadeRepositorio;
    private final MembroRepositorio membroRepositorio;

    public TreinoControlador(TreinoRepositorio repositorio, AtletaRepositorio atletaRepositorio,
            ModalidadeRepositorio modalidadeRepositorio, MembroRepositorio membroRepositorio) {
        this.repositorio = repositorio;
        this.atletaRepositorio = atletaRepositorio;
        this.modalidadeRepositorio = modalidadeRepositorio;
        this.membroRepositorio = membroRepositorio;
    }

    public TreinoControlador(PainelTreinos visao, TreinoRepositorio repositorio, AtletaRepositorio atletaRepositorio,
            ModalidadeRepositorio modalidadeRepositorio, MembroRepositorio membroRepositorio) {
        this(repositorio, atletaRepositorio, modalidadeRepositorio, membroRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> {
            boolean pode = Sessao.getAcesso().podeAlterar(Modulo.TREINOS);
            visao.habilitarAmistoso(pode);
            return pode;
        });
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> incluir(new Treino()));
        visao.adicionarAcaoBtnAmistoso(e -> incluir(new Amistoso()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionado());
        visao.adicionarAcaoBtnGerenciar(e -> abrirPresenca());
        visao.adicionarAcaoBtnFrequencia(e -> abrirFrequencia());
    }

    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            visao.apresentarModalidades(modalidadeRepositorio.listarTodos());
            visao.apresentarPresencas(resumirPresencas());
            visao.apresentarDados(pesquisar(visao.getModalidadeSelecionada(),
                    visao.isSomenteProximos() ? hoje : null,
                    visao.isSomenteRealizados() ? hoje.minusDays(1) : null,
                    visao.getTipoFiltro()));
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void incluir(Treino novo) {
        novo.setModalidade(visao.getModalidadeSelecionada());
        abrirCadastro(novo, visao.getNavegador());
    }

    private void editar() {
        Treino treino = visao.getSelecionado();
        if (treino != null) {
            abrirCadastro(treino, visao.getNavegador());
        }
    }

    private void excluirSelecionado() {
        Treino treino = visao.getSelecionado();
        if (treino == null || !visao.confirmarExclusao(treino)) {
            return;
        }
        TreinoRepositorio.ResumoPresenca resumo = visao.getPresencas(treino);
        if (resumo != null && !visao.confirmar("Este treino tem " + resumo.registrados()
                + " registro(s) de presença, que também serão excluídos. Continuar?")) {
            return;
        }
        try {
            excluir(treino);
            visao.apresentarMensagem("Treino excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    public void abrirCadastro(Treino treino, Navegador navegador) {
        PainelCadastroTreino cadastro = new PainelCadastroTreino(treino, navegador);
        try {
            cadastro.apresentarTreino(treino, modalidadeRepositorio.listarTodos(),
                    new MembroControlador(membroRepositorio).listarAtivos());
        } catch (PersistenciaException e) {
            cadastro.apresentarErro(e);
        }
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, treino));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroTreino cadastro, Treino treino) {
        try {
            treino.setModalidade(cadastro.getModalidade());
            treino.setResponsavel(cadastro.getResponsavel());
            treino.setData(cadastro.getData());
            treino.setHorario(cadastro.getHorario());
            treino.setLocal(cadastro.getLocal());
            treino.setTitulo(cadastro.getTitulo());
            treino.setObservacoes(cadastro.getObservacoes());
            if (treino instanceof Amistoso amistoso) {
                amistoso.setAdversario(cadastro.getAdversario());
                amistoso.setResultado(cadastro.getResultado());
            }
            salvar(treino);
            cadastro.apresentarMensagem((treino instanceof Amistoso ? "Amistoso" : "Treino")
                    + " salvo com sucesso. Ele já aparece no calendário.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    private void abrirPresenca() {
        Treino treino = visao.getSelecionado();
        if (treino == null) {
            return;
        }
        if (!Sessao.getAcesso().podeAlterar(Modulo.TREINOS)) {
            visao.apresentarAviso("Seu perfil não permite registrar presenças.");
            return;
        }
        PainelPresenca presenca = new PainelPresenca(treino, visao.getNavegador());
        try {
            carregarPresencas(treino);
            presenca.apresentarChamada(atletasDaChamada(treino));
        } catch (PersistenciaException e) {
            presenca.apresentarErro(e);
        }
        presenca.adicionarAcaoBtnSalvar(e -> salvarPresencaNaTela(presenca, treino));
        presenca.apresentarTela();
    }

    private void salvarPresencaNaTela(PainelPresenca presenca, Treino treino) {
        List<Atleta> atletas = presenca.getAtletas();
        for (int i = 0; i < atletas.size(); i++) {
            treino.registrarPresenca(atletas.get(i), presenca.isPresente(i));
        }
        try {
            salvarPresencas(treino);
            presenca.apresentarMensagem("Presença salva: " + presenca.getTotalPresentes() + " de " + atletas.size()
                    + " atleta(s) presente(s).");
            presenca.voltar();
        } catch (AthletizaException e) {
            presenca.apresentarErro(e);
        }
    }

    private void abrirFrequencia() {
        PainelFrequencia frequencia = new PainelFrequencia(visao.getNavegador());
        frequencia.adicionarAcaoCarregar(() -> {
            try {
                frequencia.apresentarModalidades(modalidadeRepositorio.listarTodos());
                frequencia.apresentarDados(frequencia(frequencia.getModalidadeFiltro(),
                        frequencia.getInicioPeriodo(), null));
            } catch (PersistenciaException e) {
                frequencia.apresentarErro(e);
            }
        });
        frequencia.adicionarAcaoBtnVerTreinos(e -> abrirHistorico(frequencia));
        frequencia.adicionarAcaoDuploClique(() -> abrirHistorico(frequencia));
        frequencia.apresentarTela();
    }

    private void abrirHistorico(PainelFrequencia frequencia) {
        Frequencia selecionada = frequencia.getSelecionado();
        if (selecionada == null) {
            return;
        }
        Atleta atleta = selecionada.atleta();
        Modalidade modalidade = frequencia.getModalidadeFiltro();
        LocalDate inicio = frequencia.getInicioPeriodo();
        PainelHistoricoAtleta historico = new PainelHistoricoAtleta(atleta, frequencia.getNavegador());
        historico.adicionarAcaoCarregar(() -> {
            try {
                historico.apresentarDados(historicoDoAtleta(atleta, modalidade, inicio));
            } catch (PersistenciaException e) {
                historico.apresentarErro(e);
            }
        });
        historico.apresentarTela();
    }

    public List<Treino> pesquisar(Modalidade modalidade, LocalDate inicio, LocalDate fim, TipoAtividade tipo)
            throws PersistenciaException {
        return repositorio.listar(modalidade, inicio, fim).stream()
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .sorted()
                .toList();
    }

    public Map<Integer, TreinoRepositorio.ResumoPresenca> resumirPresencas() throws PersistenciaException {
        return repositorio.resumirPresencas();
    }

    public void salvar(Treino treino) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Validador validador = new Validador()
                .obrigatorio(treino.getModalidade(), "Modalidade")
                .obrigatorio(treino.getData(), "Data")
                .tamanhoMaximo(treino.getLocal(), 100, "Local")
                .tamanhoMaximo(treino.getTitulo(), 100, "Título")
                .tamanhoMaximo(treino.getObservacoes(), 500, "Observações");
        if (treino instanceof Amistoso amistoso) {
            validador.obrigatorio(amistoso.getAdversario(), "Adversário")
                    .tamanhoMaximo(amistoso.getAdversario(), 100, "Adversário")
                    .tamanhoMaximo(amistoso.getResultado(), 100, "Resultado");
        }
        validador.validar();

        if (treino.getTitulo() == null || treino.getTitulo().isBlank()) {
            treino.setTitulo(treino instanceof Amistoso amistoso
                    ? "Amistoso " + treino.getModalidade() + " x " + amistoso.getAdversario()
                    : "Treino " + treino.getModalidade());
        }
        verificarConflito(treino);
        if (treino.isNova()) {
            repositorio.inserir(treino);
        } else {
            repositorio.atualizar(treino);
        }
    }

    private void verificarConflito(Treino treino) throws RegraNegocioException, PersistenciaException {
        if (treino.getLocal() == null || treino.getLocal().isBlank() || treino.getHorario() == null) {
            return;
        }
        for (Treino outro : repositorio.listarNoMesmoDiaELocal(treino)) {
            if (outro.getHorario() != null
                    && Duration.between(outro.getHorario(), treino.getHorario()).abs().compareTo(INTERVALO_MINIMO) < 0) {
                throw new RegraNegocioException("Conflito de horário: já existe \"" + outro.getTitulo() + "\" em "
                        + Validador.FORMATO_DATA.format(outro.getData()) + " às "
                        + Validador.FORMATO_HORARIO.format(outro.getHorario()) + " no local " + outro.getLocal() + ".");
            }
        }
    }

    public void excluir(Treino treino) throws PersistenciaException {
        repositorio.excluir(treino.getId());
    }

    public void carregarPresencas(Treino treino) throws PersistenciaException {
        repositorio.carregarPresencas(treino);
    }

    public List<Atleta> atletasDaChamada(Treino treino) throws PersistenciaException {
        Set<Atleta> atletas = new LinkedHashSet<>();
        atletaRepositorio.listarTodos().stream()
                .filter(a -> a.getSituacao() == Situacao.ATIVO && a.praticaModalidade(treino.getModalidade()))
                .sorted()
                .forEach(atletas::add);
        atletas.addAll(treino.getPresencas().keySet());
        return List.copyOf(atletas);
    }

    public void salvarPresencas(Treino treino) throws RegraNegocioException, PersistenciaException {
        if (treino.getData() != null && treino.getData().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Só é possível registrar presença em treinos que já aconteceram.");
        }
        repositorio.salvarPresencas(treino);
    }

    public List<PresencaEmTreino> historicoDoAtleta(Atleta atleta, Modalidade modalidade, LocalDate inicio)
            throws PersistenciaException {
        return repositorio.historicoDoAtleta(atleta, modalidade, inicio);
    }

    public List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        return repositorio.frequencia(modalidade, inicio, fim).stream().sorted().toList();
    }
}
