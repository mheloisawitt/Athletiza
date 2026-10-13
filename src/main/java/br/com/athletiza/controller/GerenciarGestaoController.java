package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoRepository;
import br.com.athletiza.dao.GestaoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.MembroCargo;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelGerenciarGestao;
import br.com.athletiza.view.gestao.PainelOrganograma;

/**
 * Controller do gerenciamento da gestão (CH-21): adicionar, trocar e remover
 * membros dos cargos, e abrir o organograma (CH-23).
 */
public class GerenciarGestaoController {

    private final PainelGerenciarGestao view;
    private final GestaoRepository repository;
    private final CargoRepository cargoRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;
    /** Gestão com a composição como está no banco; lida de novo a cada carregar(). */
    private Gestao model;

    public GerenciarGestaoController(PainelGerenciarGestao view, Gestao model, GestaoRepository repository,
            CargoRepository cargoRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        view.setTitulo(model.getNome());
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> adicionarMembro());
        view.adicionarAcaoEditar(e -> trocarMembro());
        view.adicionarAcaoExcluir(e -> removerMembro());
        view.adicionarAcaoOrganograma(e -> abrirOrganograma());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    /** Lê a composição de novo do banco, para refletir alterações feitas em outras telas. */
    void carregar() {
        Gestao atualizada = new Gestao(model.getNome(), model.getDataInicio(), model.getDataFim());
        atualizada.setId(model.getId());
        try {
            repository.carregarComposicao(atualizada);
            model = atualizada;
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        view.exibir(model.getComposicaoOrdenada());
    }

    private void adicionarMembro() {
        try {
            if (view.exibirFormularioAdicionar(model.getNome(), cargoRepository.listarTodos(),
                    membroRepository.listarAtivos())) {
                adicionarMembro(view.getMembroEscolhido(), view.getCargoEscolhido());
                carregar();
            }
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    /** Coloca um membro em um cargo (CH-21). O mesmo membro não pode ocupar o mesmo cargo duas vezes. */
    void adicionarMembro(Membro membro, Cargo cargo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(cargo, "Cargo").obrigatorio(membro, "Membro").validar();
        if (!model.adicionarMembro(membro, cargo)) {
            throw new RegraNegocioException(membro.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        try {
            repository.adicionarMembro(model, membro, cargo);
        } catch (PersistenciaException e) {
            model.removerMembro(membro, cargo); // desfaz na memória e relança para a tela mostrar
            throw e;
        }
    }

    /** "Trocar membro": escolhe quem passa a ocupar o cargo selecionado. */
    private void trocarMembro() {
        MembroCargo ocupacao = view.getSelecionado();
        try {
            if (view.exibirFormularioTroca(ocupacao, membroRepository.listarAtivos())) {
                trocarMembro(ocupacao.getCargo(), ocupacao.getMembro(), view.getMembroEscolhido());
                carregar();
            }
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void trocarMembro(Cargo cargo, Membro atual, Membro novo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(novo, "Novo membro").validar();
        if (atual.equals(novo)) {
            return;
        }
        if (model.ocupa(novo, cargo)) {
            throw new RegraNegocioException(novo.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        repository.trocarMembro(model, cargo, atual, novo);
        model.removerMembro(atual, cargo);
        model.adicionarMembro(novo, cargo);
    }

    private void removerMembro() {
        MembroCargo ocupacao = view.getSelecionado();
        if (!view.confirmar("Remover " + ocupacao.getMembro().getNome() + " do cargo "
                + ocupacao.getCargo().getNome() + "?")) {
            return;
        }
        try {
            repository.removerMembro(model, ocupacao.getMembro(), ocupacao.getCargo());
            model.removerMembro(ocupacao.getMembro(), ocupacao.getCargo());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    private void abrirOrganograma() {
        PainelOrganograma tela = new PainelOrganograma();
        new OrganogramaController(tela, model, repository, navegador);
        navegador.abrir(tela);
    }

    /** Gestão como está no controller (usado nos testes). */
    Gestao getModel() {
        return model;
    }

    public PainelGerenciarGestao getView() {
        return view;
    }
}
