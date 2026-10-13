package br.com.athletiza.controller;

import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.view.competicoes.PainelResultados;

/**
 * Controller dos resultados da competição (CH-32). As opções de modalidade e
 * atleta consideram as inscrições já salvas no banco.
 */
public class ResultadosController {

    private final PainelResultados view;
    private final Competicao competicao;
    private final CompeticaoRepository repository;

    public ResultadosController(PainelResultados view, Competicao competicao, CompeticaoRepository repository) {
        this.view = view;
        this.competicao = competicao;
        this.repository = repository;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> editar(new Resultado()));
        view.adicionarAcaoEditar(e -> editar(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
    }

    private void carregar() {
        try {
            view.exibir(repository.listarResultados(competicao));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /** Competição com as inscrições como estão no banco. */
    private Competicao competicaoSalva() throws PersistenciaException {
        Competicao copia = competicao.copiarDados();
        repository.carregarInscricoes(copia);
        return copia;
    }

    /** Formulário de lançamento; reabre enquanto houver erro, para o usuário corrigir. */
    private void editar(Resultado resultado) {
        Competicao salva;
        try {
            salva = competicaoSalva();
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
            return;
        }
        if (salva.getModalidades().isEmpty()) {
            view.mostrarAviso("Inscreva ao menos uma modalidade (e salve) antes de lançar resultados.");
            return;
        }
        view.preencherFormulario(resultado, salva);
        while (view.exibirFormulario(resultado.isNova() ? "Lançar resultado" : "Editar resultado")) {
            try {
                resultado.setModalidade(view.getModalidade());
                resultado.setAtleta(view.getAtleta());
                resultado.setColocacao(converterColocacao(view.getColocacao()));
                resultado.setPlacar(view.getPlacar());
                resultado.setObservacao(view.getObservacao());
                salvar(salva, resultado);
                break;
            } catch (AthletizaException e) {
                view.mostrarErro(e);
            }
        }
        carregar();
    }

    /**
     * Registra ou altera um resultado (CH-32). A modalidade precisa estar inscrita e,
     * se o resultado for individual, o atleta precisa estar inscrito nela.
     */
    void salvar(Competicao comInscricoes, Resultado resultado)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        resultado.validar();
        comInscricoes.verificarResultado(resultado);
        repository.salvarResultado(comInscricoes, resultado);
    }

    private void excluirSelecionado() {
        Resultado resultado = view.getSelecionado();
        String quem = resultado.isResultadoDeEquipe() ? "equipe" : resultado.getAtleta().getNome();
        if (!view.confirmar("Excluir o resultado de " + quem + " em " + resultado.getModalidade() + "?")) {
            return;
        }
        try {
            repository.excluirResultado(resultado);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** "1", "1º" -> 1. Vazio -> null. */
    static Integer converterColocacao(String texto) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim().replace("º", ""));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A \"Colocação\" deve ser um número (ex.: 1 para primeiro lugar).");
        }
    }

    public PainelResultados getView() {
        return view;
    }
}
