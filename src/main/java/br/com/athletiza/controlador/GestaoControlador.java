package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.modelo.Gestao;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.MembroCargo;
import br.com.athletiza.modelo.SituacaoGestao;
import br.com.athletiza.repositorio.CargoRepositorio;
import br.com.athletiza.repositorio.GestaoRepositorio;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.gestao.PainelCadastroGestao;
import br.com.athletiza.visao.gestao.PainelConsultaCargos;
import br.com.athletiza.visao.gestao.PainelConsultaGestoes;
import br.com.athletiza.visao.gestao.PainelConsultaMembros;
import br.com.athletiza.visao.gestao.PainelGerenciarGestao;
import br.com.athletiza.visao.gestao.PainelOrganograma;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GestaoControlador {
    private static final Comparator<MembroCargo> POR_CARGO = Comparator
            .comparing(MembroCargo::cargo)
            .thenComparing(MembroCargo::membro, Comparator.comparing(Membro::getNome));

    private PainelConsultaGestoes visao;
    private final GestaoRepositorio repositorio;
    private CargoRepositorio cargoRepositorio;
    private MembroRepositorio membroRepositorio;
    private Gestao gestaoGerenciada;

    public GestaoControlador(GestaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public GestaoControlador(PainelConsultaGestoes visao, GestaoRepositorio repositorio,
            CargoRepositorio cargoRepositorio, MembroRepositorio membroRepositorio) {
        this.visao = visao;
        this.repositorio = repositorio;
        this.cargoRepositorio = cargoRepositorio;
        this.membroRepositorio = membroRepositorio;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> abrirCadastro(new Gestao()));
        visao.adicionarAcaoBtnEditar(e -> editar());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
        visao.adicionarAcaoBtnGerenciar(e -> gerenciar());
        visao.adicionarAcaoBtnCargos(e -> new CargoControlador(new PainelConsultaCargos(visao.getNavegador()), cargoRepositorio));
        visao.adicionarAcaoBtnMembros(e -> new MembroControlador(new PainelConsultaMembros(visao.getNavegador()), membroRepositorio));
    }

    public void carregar() {
        try {
            visao.apresentarDados(listar());
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editar() {
        Gestao gestao = visao.getSelecionado();
        if (gestao != null) {
            abrirCadastro(gestao);
        }
    }

    private void excluirSelecionada() {
        Gestao gestao = visao.getSelecionado();
        if (gestao == null || !visao.confirmarExclusao(gestao)) {
            return;
        }
        try {
            int membros = contarMembros(gestao);
            if (membros > 0 && !visao.confirmar("A " + gestao.getNome() + " possui " + membros
                    + " membro(s) em cargos.\nEssa composição também será excluída. Continuar?")) {
                return;
            }
            excluir(gestao);
            visao.apresentarMensagem("Gestão excluída.");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void abrirCadastro(Gestao gestao) {
        PainelCadastroGestao cadastro = new PainelCadastroGestao(visao.getNavegador());
        cadastro.apresentarGestao(gestao);
        cadastro.adicionarAcaoBtnSalvar(e -> salvarCadastro(cadastro, gestao));
        cadastro.apresentarTela();
    }

    private void salvarCadastro(PainelCadastroGestao cadastro, Gestao gestao) {
        try {
            if (cadastro.getSituacao() == SituacaoGestao.EM_ANDAMENTO) {
                List<String> nomes = new ArrayList<>();
                for (Gestao outra : outrasEmAndamento(gestao)) {
                    nomes.add(outra.getNome());
                }
                if (!nomes.isEmpty() && !cadastro.confirmar("Só uma gestão fica \"Em andamento\". Ao salvar, "
                        + String.join(", ", nomes) + (nomes.size() == 1 ? " passa" : " passam")
                        + " para \"Encerrada\". Continuar?")) {
                    return;
                }
            }
            gestao.setNome(cadastro.getNome());
            gestao.setDataInicio(cadastro.getDataInicio());
            gestao.setDataFim(cadastro.getDataFim());
            gestao.setSituacao(cadastro.getSituacao());
            gestao.setDescricao(cadastro.getDescricao());
            salvar(gestao);
            cadastro.apresentarMensagem("Gestão salva com sucesso.");
            cadastro.voltar();
        } catch (AthletizaException e) {
            cadastro.apresentarErro(e);
        }
    }

    private void gerenciar() {
        Gestao gestao = visao.getSelecionado();
        if (gestao == null) {
            return;
        }
        PainelGerenciarGestao gerenciar = new PainelGerenciarGestao(gestao, visao.getNavegador());
        gestaoGerenciada = gestao;
        gerenciar.adicionarAcaoCarregar(() -> carregarGerenciar(gerenciar, gestao));
        gerenciar.adicionarAcaoBtnIncluir(e -> adicionarMembroNaTela(gerenciar, gestaoGerenciada));
        gerenciar.adicionarAcaoBtnEditar(e -> trocarMembroNaTela(gerenciar, gestaoGerenciada));
        gerenciar.adicionarAcaoBtnExcluir(e -> removerMembroNaTela(gerenciar, gestaoGerenciada));
        gerenciar.adicionarAcaoBtnOrganograma(e -> abrirOrganograma(gestao));
        gerenciar.apresentarTela();
    }

    private void carregarGerenciar(PainelGerenciarGestao gerenciar, Gestao gestao) {
        Gestao atualizada = new Gestao(gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim());
        atualizada.setId(gestao.getId());
        try {
            carregarComposicao(atualizada);
            gerenciar.apresentarDados(atualizada.getComposicao().stream().sorted(POR_CARGO).toList());
        } catch (PersistenciaException e) {
            gerenciar.apresentarErro(e);
        }
        gestaoGerenciada = atualizada;
    }

    private void adicionarMembroNaTela(PainelGerenciarGestao gerenciar, Gestao gestao) {
        try {
            List<Cargo> cargos = cargoRepositorio.listarTodos().stream().sorted().toList();
            MembroCargo escolha = gerenciar.pedirNovoMembro(gestao.getNome(), cargos, membrosAtivos());
            if (escolha != null) {
                adicionarMembro(gestao, escolha.membro(), escolha.cargo());
                gerenciar.carregar();
            }
        } catch (AthletizaException e) {
            gerenciar.apresentarErro(e);
        }
    }

    private void trocarMembroNaTela(PainelGerenciarGestao gerenciar, Gestao gestao) {
        MembroCargo ocupacao = gerenciar.getSelecionado();
        if (ocupacao == null) {
            return;
        }
        try {
            Membro novo = gerenciar.pedirNovoOcupante(ocupacao, membrosAtivos());
            if (novo != null) {
                trocarMembro(gestao, ocupacao.cargo(), ocupacao.membro(), novo);
                gerenciar.carregar();
            }
        } catch (AthletizaException e) {
            gerenciar.apresentarErro(e);
        }
    }

    private void removerMembroNaTela(PainelGerenciarGestao gerenciar, Gestao gestao) {
        MembroCargo ocupacao = gerenciar.getSelecionado();
        if (ocupacao == null || !gerenciar.confirmarExclusao(ocupacao)) {
            return;
        }
        try {
            removerMembro(gestao, ocupacao.membro(), ocupacao.cargo());
        } catch (AthletizaException e) {
            gerenciar.apresentarErro(e);
        }
        gerenciar.carregar();
    }

    private List<Membro> membrosAtivos() throws PersistenciaException {
        return new MembroControlador(membroRepositorio).listarAtivos();
    }

    private void abrirOrganograma(Gestao gestao) {
        PainelOrganograma organograma = new PainelOrganograma(gestao, visao.getNavegador());
        organograma.adicionarAcaoCarregar(() -> {
            Gestao atualizada = new Gestao(gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim());
            atualizada.setId(gestao.getId());
            try {
                carregarComposicao(atualizada);
            } catch (PersistenciaException e) {
                organograma.apresentarErro(e);
            }
            organograma.apresentarGestao(atualizada);
        });
        organograma.apresentarTela();
    }

    public List<Gestao> listar() throws PersistenciaException {
        return repositorio.listarTodos().stream().sorted().toList();
    }

    public void salvar(Gestao gestao) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(gestao.getNome(), "Nome da gestão")
                .tamanhoMaximo(gestao.getNome(), 100, "Nome da gestão")
                .obrigatorio(gestao.getDataInicio(), "Data de início")
                .obrigatorio(gestao.getDataFim(), "Data de fim")
                .periodo(gestao.getDataInicio(), gestao.getDataFim(), "Data de início", "Data de fim")
                .obrigatorio(gestao.getSituacao(), "Situação")
                .tamanhoMaximo(gestao.getDescricao(), 500, "Descrição")
                .validar();

        List<Gestao> outras = repositorio.listarTodos().stream()
                .filter(g -> !g.getId().equals(gestao.getId()))
                .toList();
        Optional<Gestao> sobreposta = outras.stream().filter(gestao::sobrepoe).findFirst();
        if (sobreposta.isPresent()) {
            Gestao outra = sobreposta.get();
            throw new RegraNegocioException("O período informado se sobrepõe à " + outra.getNome() + " ("
                    + Validador.FORMATO_DATA.format(outra.getDataInicio()) + " a "
                    + Validador.FORMATO_DATA.format(outra.getDataFim()) + ").");
        }
        if (outras.stream().anyMatch(g -> g.getNome().equalsIgnoreCase(gestao.getNome().trim()))) {
            throw new RegraNegocioException("Já existe uma gestão chamada " + gestao.getNome() + ".");
        }

        List<Gestao> emAndamento = gestao.getSituacao() == SituacaoGestao.EM_ANDAMENTO
                ? outras.stream().filter(g -> g.getSituacao() == SituacaoGestao.EM_ANDAMENTO).toList()
                : List.of();
        Optional<Gestao> maisRecente = emAndamento.stream()
                .filter(g -> g.getDataInicio().isAfter(gestao.getDataInicio())).findFirst();
        if (maisRecente.isPresent()) {
            throw new RegraNegocioException("A " + maisRecente.get().getNome() + " é mais recente e está em andamento."
                    + " Só a gestão mais recente pode ficar \"Em andamento\".");
        }
        if (gestao.isNova()) {
            repositorio.inserir(gestao);
        } else {
            repositorio.atualizar(gestao);
        }
        for (Gestao anterior : emAndamento) {
            anterior.setSituacao(SituacaoGestao.ENCERRADA);
            repositorio.atualizar(anterior);
        }
    }

    public List<Gestao> outrasEmAndamento(Gestao gestao) throws PersistenciaException {
        return repositorio.listarTodos().stream()
                .filter(g -> !g.getId().equals(gestao.getId()) && g.getSituacao() == SituacaoGestao.EM_ANDAMENTO)
                .toList();
    }

    public int contarMembros(Gestao gestao) throws PersistenciaException {
        Gestao copia = new Gestao();
        copia.setId(gestao.getId());
        repositorio.carregarComposicao(copia);
        return copia.getComposicao().size();
    }

    public void excluir(Gestao gestao) throws PersistenciaException {
        repositorio.excluir(gestao.getId());
    }

    public void carregarComposicao(Gestao gestao) throws PersistenciaException {
        repositorio.carregarComposicao(gestao);
    }

    public void adicionarMembro(Gestao gestao, Membro membro, Cargo cargo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(cargo, "Cargo").obrigatorio(membro, "Membro").validar();
        if (!gestao.adicionarMembro(membro, cargo)) {
            throw new RegraNegocioException(membro.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        try {
            repositorio.adicionarMembro(gestao, membro, cargo);
        } catch (PersistenciaException e) {
            gestao.removerMembro(membro, cargo);
            throw e;
        }
    }

    public void removerMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException {
        repositorio.removerMembro(gestao, membro, cargo);
        gestao.removerMembro(membro, cargo);
    }

    public void trocarMembro(Gestao gestao, Cargo cargo, Membro atual, Membro novo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(novo, "Novo membro").validar();
        if (atual.equals(novo)) {
            return;
        }
        if (gestao.getComposicao().stream().anyMatch(mc -> mc.cargo().equals(cargo) && mc.membro().equals(novo))) {
            throw new RegraNegocioException(novo.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        repositorio.trocarMembro(gestao, cargo, atual, novo);
        gestao.removerMembro(atual, cargo);
        gestao.adicionarMembro(novo, cargo);
    }
}
