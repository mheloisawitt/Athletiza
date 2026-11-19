package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Acesso;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.Perfil;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.repositorio.CompeticaoRepositorio;
import br.com.athletiza.repositorio.DemandaRepositorio;
import br.com.athletiza.repositorio.EventoRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.demandas.PainelCadastroDemanda;
import br.com.athletiza.visao.demandas.PainelConsultaDemandas;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class DemandaControlador {
    static final String SO_SOLICITANTE_CANCELA =
            "Só quem registrou a demanda, a presidência ou o administrador podem cancelá-la ou excluí-la.";

    private PainelConsultaDemandas visao;
    private final DemandaRepositorio repositorio;
    private final EventoRepositorio eventoRepositorio;
    private final CompeticaoRepositorio competicaoRepositorio;
    private final MembroRepositorio membroRepositorio;

    public DemandaControlador(DemandaRepositorio repositorio, EventoRepositorio eventoRepositorio,
            CompeticaoRepositorio competicaoRepositorio, MembroRepositorio membroRepositorio) {
        this.repositorio = repositorio;
        this.eventoRepositorio = eventoRepositorio;
        this.competicaoRepositorio = competicaoRepositorio;
        this.membroRepositorio = membroRepositorio;
    }

    public DemandaControlador(PainelConsultaDemandas visao, DemandaRepositorio repositorio,
            EventoRepositorio eventoRepositorio, CompeticaoRepositorio competicaoRepositorio,
            MembroRepositorio membroRepositorio) {
        this(repositorio, eventoRepositorio, competicaoRepositorio, membroRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.DEMANDAS));
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Demanda()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
        visao.adicionarAcaoBtnMudarSituacao(e -> mudarSituacaoNaTela());
    }

    public void carregar() {
        try {
            Optional<Membro> eu = membroLogado();
            boolean assessor = Sessao.getAcesso().getPerfil() == Perfil.ASSESSOR;
            visao.apresentarFiltros(eu.isPresent() && !assessor, !assessor, membroRepositorio.listarTodos());

            Membro responsavel;
            if (assessor || visao.isMinhasSelecionado() && eu.isPresent()) {
                responsavel = eu.orElse(null);
            } else {
                responsavel = visao.getResponsavelFiltro();
            }
            if (assessor && responsavel == null) {
                visao.apresentarDados(List.of());
            } else {
                visao.apresentarDados(pesquisar(visao.getSituacaoFiltro(), responsavel));
            }
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private Demanda selecionadaQuePodeAlterar() {
        Demanda demanda = visao.getSelecionado();
        if (demanda != null && !Sessao.getAcesso().podeAlterar(demanda)) {
            visao.apresentarAviso("Seu perfil não permite alterar este registro: ele é de outra área.");
            return null;
        }
        return demanda;
    }

    private void editar() {
        Demanda demanda = selecionadaQuePodeAlterar();
        if (demanda != null) {
            abrirCadastro(demanda);
        }
    }

    private void excluirSelecionada() {
        Demanda demanda = selecionadaQuePodeAlterar();
        if (demanda == null || !visao.confirmarExclusao(demanda)) {
            return;
        }
        try {
            excluir(demanda);
            visao.apresentarMensagem("Demanda excluída.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void mudarSituacaoNaTela() {
        Demanda demanda = visao.getSelecionado();
        if (demanda == null) {
            return;
        }
        if (!podeMudarSituacao(demanda)) {
            visao.apresentarAviso("Só os responsáveis pela demanda ou quem pode editá-la mudam a situação.");
            return;
        }
        List<SituacaoDemanda> opcoes = new ArrayList<>();
        for (SituacaoDemanda situacao : SituacaoDemanda.values()) {
            if (situacao != SituacaoDemanda.CANCELADA || podeCancelar(demanda)) {
                opcoes.add(situacao);
            }
        }
        SituacaoDemanda nova = visao.pedirNovaSituacao(demanda, opcoes.toArray(new SituacaoDemanda[0]));
        if (nova == null || nova == demanda.getSituacao()) {
            return;
        }
        try {
            mudarSituacao(demanda, nova);
            visao.apresentarMensagem("Situação alterada para \"" + nova + "\".");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Demanda demanda) {
        PainelCadastroDemanda cadastro = new PainelCadastroDemanda(visao.getNavegador());
        try {
            Acesso acesso = Sessao.getAcesso();
            List<Atividade> relacionaveis = new ArrayList<>();
            for (Atividade atividade : listarRelacionaveis()) {
                if (acesso.podeAlterar(atividade)) {
                    relacionaveis.add(atividade);
                }
            }
            List<Membro> membros = new ArrayList<>(membroRepositorio.listarDaGestaoEmAndamento());
            for (Membro membro : demanda.getResponsaveis()) {
                if (!membros.contains(membro)) {
                    membros.add(membro);
                }
            }
            cadastro.apresentarDemanda(demanda, relacionaveis, acesso.isAcessoTotal(), membros, podeCancelar(demanda));
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, demanda));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroDemanda cadastro, Demanda demanda) {
        try {
            demanda.setTitulo(cadastro.getTitulo());
            demanda.setSituacao(cadastro.getSituacao());
            demanda.setPrioridade(cadastro.getPrioridade());
            demanda.setPrazo(cadastro.getPrazo());
            demanda.setRelacionada(cadastro.getRelacionada());
            demanda.definirResponsaveis(cadastro.getResponsaveis());
            demanda.setDescricao(cadastro.getDescricao());
            if (!Sessao.getAcesso().podeAlterar(demanda)) {
                throw new ValidacaoException("Relacione a demanda a um evento ou competição da sua área.");
            }
            salvar(demanda);
            cadastro.apresentarMensagem("Demanda salva com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    public List<Demanda> pesquisar(SituacaoDemanda situacao, Membro responsavel) throws PersistenciaException {
        return repositorio.listarTodos().stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .filter(d -> responsavel == null || d.getResponsaveis().contains(responsavel))
                .sorted()
                .toList();
    }

    public Optional<Membro> membroLogado() {
        Usuario usuario = Sessao.getUsuarioLogado();
        return Optional.ofNullable(usuario == null ? null : usuario.getMembro());
    }

    public void salvar(Demanda demanda) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Set<Membro> permitidos = new HashSet<>(membroRepositorio.listarDaGestaoEmAndamento());
        Optional<Demanda> anterior = demanda.isNova() ? Optional.empty() : repositorio.buscarPorId(demanda.getId());
        anterior.ifPresent(salva -> permitidos.addAll(salva.getResponsaveis()));
        List<String> foraDaGestao = demanda.getResponsaveis().stream()
                .filter(m -> !permitidos.contains(m)).map(Membro::getNome).toList();
        new Validador()
                .obrigatorio(demanda.getTitulo(), "Demanda")
                .tamanhoMaximo(demanda.getTitulo(), 100, "Demanda")
                .obrigatorio(demanda.getPrioridade(), "Prioridade")
                .obrigatorio(demanda.getSituacao(), "Situação")
                .obrigatorio(demanda.getPrazo(), "Prazo")
                .tamanhoMaximo(demanda.getDescricao(), 500, "Descrição")
                .regra(!demanda.getResponsaveis().isEmpty(), "Selecione ao menos um responsável.")
                .regra(foraDaGestao.isEmpty(), "Os responsáveis devem ser membros da gestão em andamento. Não fazem parte dela: "
                        + String.join(", ", foraDaGestao) + ".")
                .validar();

        if (demanda.isNova()) {
            if (demanda.getSolicitante() == null) {
                demanda.setSolicitante(Sessao.getUsuarioLogado());
            }
            repositorio.inserir(demanda);
            return;
        }

        Demanda salva = anterior.orElse(demanda);
        if (demanda.getSituacao() == SituacaoDemanda.CANCELADA && salva.getSituacao() != SituacaoDemanda.CANCELADA
                && !podeCancelar(salva)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        repositorio.atualizar(demanda);
    }

    public boolean podeCancelar(Demanda demanda) {
        if (Sessao.getAcesso().isAcessoTotal()) {
            return true;
        }
        Usuario usuario = Sessao.getUsuarioLogado();
        return usuario != null && demanda.getSolicitante() != null
                && Objects.equals(demanda.getSolicitante().getId(), usuario.getId());
    }

    public boolean podeMudarSituacao(Demanda demanda) {
        return Sessao.getAcesso().podeAlterar(demanda) || membroLogado().map(demanda::isResponsavel).orElse(false);
    }

    public void mudarSituacao(Demanda demanda, SituacaoDemanda nova) throws RegraNegocioException, PersistenciaException {
        if (nova == null || nova == demanda.getSituacao()) {
            return;
        }
        if (!podeMudarSituacao(demanda)) {
            throw new RegraNegocioException("Só os responsáveis pela demanda ou quem pode editá-la mudam a situação.");
        }
        if (nova == SituacaoDemanda.CANCELADA && !podeCancelar(demanda)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        repositorio.atualizarSituacao(demanda.getId(), nova);
        demanda.setSituacao(nova);
    }

    public void excluir(Demanda demanda) throws RegraNegocioException, PersistenciaException {
        if (!podeCancelar(demanda)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        repositorio.excluir(demanda.getId());
    }

    public List<Atividade> listarRelacionaveis() throws PersistenciaException {
        return Stream.<Atividade>concat(
                new EventoControlador(eventoRepositorio).pesquisar(null, null, null).stream(),
                new CompeticaoControlador(competicaoRepositorio).pesquisar(null, null, null).stream())
                .sorted()
                .toList();
    }
}
