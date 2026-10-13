package br.com.athletiza.controller;

import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.treinos.PainelCadastroTreino;
import java.time.Duration;
import java.util.ArrayList;

/**
 * Controller do cadastro de treino ou amistoso (CH-35).
 */
public class CadastroTreinoController {

    /** Dois treinos no mesmo local com menos que este intervalo entre os horários são considerados em conflito. */
    static final Duration INTERVALO_MINIMO = Duration.ofHours(1);

    private final PainelCadastroTreino view;
    private final Treino model;
    private final TreinoRepository repository;
    private final ModalidadeRepository modalidadeRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;

    public CadastroTreinoController(PainelCadastroTreino view, Treino model, TreinoRepository repository,
            ModalidadeRepository modalidadeRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.modalidadeRepository = modalidadeRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        preencherTela();
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoCancelar(e -> cancelar());
    }

    private void preencherTela() {
        try {
            view.preencherCampos(model, modalidadeRepository.listarTodos(), membroRepository.listarAtivos());
        } catch (PersistenciaException e) {
            view.preencherCampos(model, new ArrayList<Modalidade>(), new ArrayList<Membro>());
            view.mostrarErro(e);
        }
        if (model instanceof Amistoso) {
            Amistoso amistoso = (Amistoso) model;
            view.preencherAmistoso(amistoso.getAdversario(), amistoso.getResultado());
        }
    }

    private void salvar() {
        try {
            gravar();
            view.mostrarMensagem(model.getTipo().getDescricao() + " salvo com sucesso. Ele já aparece no calendário.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setModalidade(view.getModalidade());
        model.setResponsavel(view.getResponsavel());
        model.setData(Validador.converterData(view.getData(), "Data"));
        model.setHorario(Validador.converterHorario(view.getHorario(), "Horário"));
        model.setLocal(view.getLocal());
        model.setTitulo(view.getTituloTreino());
        model.setObservacoes(view.getObservacoes());
        if (model instanceof Amistoso) {
            Amistoso amistoso = (Amistoso) model;
            amistoso.setAdversario(view.getAdversario());
            amistoso.setResultado(view.getResultado());
        }
        model.validar(); // Amistoso confere também o adversário (polimorfismo)
        model.gerarTituloSeVazio();
        verificarConflito();
        if (model.isNova()) {
            repository.inserir(model);
        } else {
            repository.atualizar(model);
        }
    }

    /** Impede dois treinos no mesmo local com menos de 1 hora de diferença (CH-35). */
    private void verificarConflito() throws RegraNegocioException, PersistenciaException {
        if (model.getLocal() == null || model.getLocal().isBlank() || model.getHorario() == null) {
            return;
        }
        for (Treino outro : repository.listarNoMesmoDiaELocal(model)) {
            if (outro.getHorario() != null
                    && Duration.between(outro.getHorario(), model.getHorario()).abs().compareTo(INTERVALO_MINIMO) < 0) {
                throw new RegraNegocioException("Conflito de horário: já existe \"" + outro.getTitulo() + "\" em "
                        + Validador.FORMATO_DATA.format(outro.getData()) + " às "
                        + Validador.FORMATO_HORARIO.format(outro.getHorario()) + " no local " + outro.getLocal() + ".");
            }
        }
    }

    private void cancelar() {
        if (view.confirmar("Deseja sair sem salvar? As alterações serão perdidas.")) {
            navegador.voltar();
        }
    }

    public PainelCadastroTreino getView() {
        return view;
    }
}
