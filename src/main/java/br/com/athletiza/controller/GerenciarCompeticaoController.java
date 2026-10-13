package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.competicoes.PainelGerenciarCompeticao;
import br.com.athletiza.view.componentes.Navegador;
import java.util.Collections;
import java.util.List;

/**
 * Controller do gerenciamento da competição (CH-31): inscrição de modalidades e
 * atletas. As inscrições são montadas em memória (Map modalidade -> Set de atletas,
 * em Competicao) e gravadas de uma vez em "Salvar inscrições".
 *
 * As abas de resultados e de tarefas têm seus próprios controllers.
 */
public class GerenciarCompeticaoController {

    private final PainelGerenciarCompeticao view;
    private final Competicao original;
    private final CompeticaoRepository repository;
    private final AtletaRepository atletaRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final Navegador navegador;
    /** Cópia da competição com as inscrições em edição. */
    private Competicao competicao;
    private boolean alterado;

    public GerenciarCompeticaoController(PainelGerenciarCompeticao view, Competicao original,
            CompeticaoRepository repository, AtletaRepository atletaRepository,
            ModalidadeRepository modalidadeRepository, TarefaRepository tarefaRepository,
            MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.original = original;
        this.repository = repository;
        this.atletaRepository = atletaRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.navegador = navegador;
        this.competicao = original.copiarDados();
        new ResultadosController(view.getPainelResultados(), original, repository);
        new TarefasController(view.getPainelTarefas(), tarefaRepository, membroRepository,
                TarefaRepository.Dono.COMPETICAO, original.getId(), null);
        view.exibirCabecalho(original);
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoVoltar(e -> voltar());
        view.adicionarAcaoMarcarModalidade(e -> marcarModalidade(view.getModalidadeClicada(), view.isMarcacaoClicada()));
        view.adicionarAcaoMarcarAtleta(e -> marcarAtleta(view.getModalidadeClicada(), view.getAtletaClicado(),
                view.isMarcacaoClicada()));
        view.adicionarAcaoAbrirResultados(e -> abrirResultados());
    }

    /** Lê de novo do banco as inscrições, as modalidades e os atletas. */
    void carregar() {
        try {
            competicao = original.copiarDados();
            repository.carregarInscricoes(competicao);
            List<Atleta> atletas = atletaRepository.listarTodos();
            Collections.sort(atletas);
            alterado = false;
            view.exibirInscricoes(competicao, modalidadeRepository.listarTodos(), atletas);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        view.getPainelResultados().carregar();
        view.getPainelTarefas().carregar();
    }

    /** Marcou ou desmarcou uma modalidade da competição. */
    void marcarModalidade(Modalidade modalidade, boolean marcada) {
        if (marcada) {
            competicao.adicionarModalidade(modalidade);
        } else {
            int atletas = competicao.getAtletas(modalidade).size();
            if (atletas > 0 && !view.confirmar("Retirar " + modalidade + " remove " + atletas
                    + " atleta(s) inscrito(s) e, ao salvar, os resultados dessa modalidade. Continuar?")) {
                return;
            }
            competicao.removerModalidade(modalidade);
        }
        alterado = true;
        view.atualizarInscricoes(modalidade);
    }

    /** Marcou ou desmarcou um atleta na modalidade. A regra de inscrição fica em Competicao. */
    void marcarAtleta(Modalidade modalidade, Atleta atleta, boolean marcado) {
        if (marcado) {
            try {
                competicao.adicionarModalidade(modalidade);
                competicao.inscreverAtleta(modalidade, atleta);
            } catch (RegraNegocioException e) {
                view.mostrarErro(e);
                return;
            }
        } else {
            competicao.removerAtleta(modalidade, atleta);
        }
        alterado = true;
        view.atualizarInscricoes(modalidade);
    }

    private void salvar() {
        try {
            repository.salvarInscricoes(competicao);
            alterado = false;
            view.mostrarMensagem("Inscrições salvas com sucesso.");
            carregar();
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirResultados() {
        if (alterado) {
            view.mostrarAviso("Há inscrições não salvas. Os resultados consideram apenas as inscrições salvas.");
        }
        view.getPainelResultados().carregar();
    }

    private void voltar() {
        if (!alterado || view.confirmar("Há inscrições não salvas. Deseja sair mesmo assim?")) {
            navegador.voltar();
        }
    }

    /** Competição com as inscrições em edição (usado nos testes). */
    Competicao getCompeticao() {
        return competicao;
    }

    public PainelGerenciarCompeticao getView() {
        return view;
    }
}
