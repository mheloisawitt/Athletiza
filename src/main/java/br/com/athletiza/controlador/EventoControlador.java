package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Modulo;
import br.com.athletiza.modelo.Tarefa;
import br.com.athletiza.modelo.TipoEvento;
import br.com.athletiza.repositorio.EventoRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.eventos.PainelCadastroEvento;
import br.com.athletiza.visao.eventos.PainelConsultaEventos;
import br.com.athletiza.visao.eventos.PainelGerenciarEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventoControlador {
    private PainelConsultaEventos visao;
    private final EventoRepositorio repositorio;
    private MembroRepositorio membroRepositorio;
    private Evento eventoGerenciado;

    public EventoControlador(EventoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public EventoControlador(PainelConsultaEventos visao, EventoRepositorio repositorio,
            MembroRepositorio membroRepositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        this.membroRepositorio = membroRepositorio;
        initTela();
    }

    public void initTela() {
        visao.definirPermissao(() -> Sessao.getAcesso().podeAlterar(Modulo.EVENTOS));
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Evento(), visao.getNavegador()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionado());
        visao.adicionarAcaoBtnGerenciar(e -> gerenciar());
    }

    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            List<Evento> eventos = pesquisar(visao.getTipoFiltro(),
                    visao.isSomenteProximos() ? hoje : null,
                    visao.isSomenteRealizados() ? hoje.minusDays(1) : null);
            List<Evento> visiveis = new ArrayList<>();
            for (Evento evento : eventos) {
                if (Sessao.getAcesso().ve(evento)) {
                    visiveis.add(evento);
                }
            }
            visao.apresentarDados(visiveis);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private Evento selecionadoQuePodeAlterar() {
        Evento evento = visao.getSelecionado();
        if (evento != null && !Sessao.getAcesso().podeAlterar(evento)) {
            visao.apresentarAviso("Seu perfil não permite alterar este registro: ele é de outra área.");
            return null;
        }
        return evento;
    }

    private void editar() {
        Evento evento = selecionadoQuePodeAlterar();
        if (evento != null) {
            abrirCadastro(evento, visao.getNavegador());
        }
    }

    private void excluirSelecionado() {
        Evento evento = selecionadoQuePodeAlterar();
        if (evento == null || !visao.confirmarExclusao(evento)) {
            return;
        }
        if (!evento.getTarefas().isEmpty() && !visao.confirmar(evento.getTitulo() + " possui "
                + evento.getTarefas().size() + " tarefa(s), que também serão excluídas, assim como os responsáveis."
                + " Continuar?")) {
            return;
        }
        try {
            excluir(evento);
            visao.apresentarMensagem("Evento excluído.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    public void abrirCadastro(Evento evento, Navegador navegador) {
        PainelCadastroEvento cadastro = new PainelCadastroEvento(navegador);
        cadastro.apresentarEvento(evento);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, evento));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroEvento cadastro, Evento evento) {
        try {
            evento.setTitulo(cadastro.getNome());
            evento.setTipoEvento(cadastro.getTipo());
            evento.setData(cadastro.getData());
            evento.setHorario(cadastro.getHorario());
            evento.setLocal(cadastro.getLocal());
            evento.setSituacao(cadastro.getSituacao());
            evento.setDescricao(cadastro.getDescricao());
            evento.setObservacoes(cadastro.getObservacoes());
            salvar(evento);
            cadastro.apresentarMensagem("Evento salvo com sucesso. Ele já aparece no calendário.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    private void gerenciar() {
        Evento evento = visao.getSelecionado();
        if (evento == null) {
            return;
        }
        PainelGerenciarEvento gerenciar = new PainelGerenciarEvento(evento, visao.getNavegador());
        PainelGerenciarEvento.PainelResponsaveis responsaveis = gerenciar.getResponsaveis();
        responsaveis.definirPermissao(() -> Sessao.getAcesso().podeAlterar(evento));
        gerenciar.getTarefas().definirPermissao(() -> Sessao.getAcesso().podeAlterar(evento));

        responsaveis.adicionarAcaoCarregar(() -> {
            try {
                eventoGerenciado = eventoAtualizado(evento, gerenciar);
                responsaveis.apresentarDados(List.copyOf(eventoGerenciado.getResponsaveis()));
            } catch (PersistenciaException e) {
                responsaveis.apresentarErro(e);
            }
        });
        responsaveis.adicionarAcaoBtnIncluir(e -> adicionarResponsavelNaTela(responsaveis));
        responsaveis.adicionarAcaoBtnExcluir(e -> removerResponsavelNaTela(responsaveis));

        new TarefaControlador(gerenciar.getTarefas(), new TarefaControlador.FonteTarefas() {
            @Override
            public List<Tarefa> listar() throws AthletizaException {
                return eventoAtualizado(evento, gerenciar).getTarefas().stream().sorted().toList();
            }

            @Override
            public void salvar(Tarefa tarefa) throws AthletizaException {
                salvarTarefa(evento, tarefa);
            }

            @Override
            public void excluir(Tarefa tarefa) throws AthletizaException {
                excluirTarefa(tarefa);
            }
        }, membroRepositorio);
        gerenciar.apresentarTela();
    }

    private Evento eventoAtualizado(Evento original, PainelGerenciarEvento gerenciar) throws PersistenciaException {
        Evento evento = new Evento(original.getTitulo(), original.getTipoEvento(), original.getData(),
                original.getHorario(), original.getLocal());
        evento.setId(original.getId());
        carregarDetalhes(evento);
        gerenciar.apresentarAtrasadas(evento.getTarefasAtrasadas(LocalDate.now()).size());
        return evento;
    }

    private void adicionarResponsavelNaTela(PainelGerenciarEvento.PainelResponsaveis responsaveis) {
        try {
            List<Membro> disponiveis = new ArrayList<>();
            for (Membro membro : new MembroControlador(membroRepositorio).listarAtivos()) {
                if (!eventoGerenciado.getResponsaveis().contains(membro)) {
                    disponiveis.add(membro);
                }
            }
            if (disponiveis.isEmpty()) {
                responsaveis.apresentarAviso("Todos os membros ativos já são responsáveis por este evento.");
                return;
            }
            Membro membro = responsaveis.pedirResponsavel(disponiveis);
            if (membro != null) {
                adicionarResponsavel(eventoGerenciado, membro);
                responsaveis.carregar();
            }
        } catch (AthletizaException e) {
            responsaveis.apresentarErro(e);
        }
    }

    private void removerResponsavelNaTela(PainelGerenciarEvento.PainelResponsaveis responsaveis) {
        Membro membro = responsaveis.getSelecionado();
        if (membro == null || !responsaveis.confirmarExclusao(membro)) {
            return;
        }
        try {
            removerResponsavel(eventoGerenciado, membro);
        } catch (AthletizaException e) {
            responsaveis.apresentarErro(e);
        }
        responsaveis.carregar();
    }

    public List<Evento> pesquisar(TipoEvento tipo, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        return repositorio.listarTodos().stream()
                .filter(e -> tipo == null || e.getTipoEvento() == tipo)
                .filter(e -> inicio == null || !e.getData().isBefore(inicio))
                .filter(e -> fim == null || !e.getData().isAfter(fim))
                .sorted()
                .toList();
    }

    public void salvar(Evento evento) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(evento.getTitulo(), "Nome")
                .tamanhoMaximo(evento.getTitulo(), 100, "Nome")
                .obrigatorio(evento.getTipoEvento(), "Tipo")
                .obrigatorio(evento.getData(), "Data")
                .tamanhoMaximo(evento.getLocal(), 100, "Local")
                .obrigatorio(evento.getSituacao(), "Situação")
                .tamanhoMaximo(evento.getDescricao(), 500, "Descrição")
                .tamanhoMaximo(evento.getObservacoes(), 500, "Observações")
                .validar();
        if (evento.isNova()) {
            repositorio.inserir(evento);
        } else {
            repositorio.atualizar(evento);
        }
    }

    public void excluir(Evento evento) throws PersistenciaException {
        repositorio.excluir(evento.getId());
    }

    public void carregarDetalhes(Evento evento) throws PersistenciaException {
        repositorio.carregarDetalhes(evento);
    }

    public void adicionarResponsavel(Evento evento, Membro membro)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(membro, "Membro").validar();
        if (!evento.adicionarResponsavel(membro)) {
            throw new RegraNegocioException(membro.getNome() + " já é responsável por este evento.");
        }
        try {
            repositorio.adicionarResponsavel(evento, membro);
        } catch (PersistenciaException e) {
            evento.removerResponsavel(membro);
            throw e;
        }
    }

    public void removerResponsavel(Evento evento, Membro membro) throws PersistenciaException {
        repositorio.removerResponsavel(evento, membro);
        evento.removerResponsavel(membro);
    }

    public void salvarTarefa(Evento evento, Tarefa tarefa) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(tarefa.getDescricao(), "Descrição")
                .tamanhoMaximo(tarefa.getDescricao(), 200, "Descrição")
                .obrigatorio(tarefa.getSituacao(), "Situação")
                .validar();
        repositorio.salvarTarefa(evento, tarefa);
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        repositorio.excluirTarefa(tarefa);
    }
}
