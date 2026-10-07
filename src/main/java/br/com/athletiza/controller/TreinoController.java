package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Validador;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de treinos, amistosos, presença e frequência (CH-33 a CH-37).
 */
public class TreinoController {

    /** Dois treinos no mesmo local com menos que este intervalo entre os horários são considerados em conflito. */
    static final Duration INTERVALO_MINIMO = Duration.ofHours(1);

    private final TreinoDAO dao = new TreinoDAO();
    private final AtletaDAO atletaDAO = new AtletaDAO();

    /**
     * Treinos e amistosos em ordem de data (ordem natural de Atividade).
     *
     * @param tipo TipoAtividade.TREINO, TipoAtividade.AMISTOSO ou null para ambos
     */
    public List<Treino> pesquisar(Modalidade modalidade, LocalDate inicio, LocalDate fim, TipoAtividade tipo)
            throws PersistenciaException {
        return dao.listar(modalidade, inicio, fim).stream()
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .sorted()
                .toList();
    }

    public Map<Integer, TreinoDAO.ResumoPresenca> resumirPresencas() throws PersistenciaException {
        return dao.resumirPresencas();
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
            dao.inserir(treino);
        } else {
            dao.atualizar(treino);
        }
    }

    /** Impede dois treinos no mesmo local com menos de 1 hora de diferença (CH-35). */
    private void verificarConflito(Treino treino) throws RegraNegocioException, PersistenciaException {
        if (treino.getLocal() == null || treino.getLocal().isBlank() || treino.getHorario() == null) {
            return;
        }
        for (Treino outro : dao.listarNoMesmoDiaELocal(treino)) {
            if (outro.getHorario() != null
                    && Duration.between(outro.getHorario(), treino.getHorario()).abs().compareTo(INTERVALO_MINIMO) < 0) {
                throw new RegraNegocioException("Conflito de horário: já existe \"" + outro.getTitulo() + "\" em "
                        + Validador.FORMATO_DATA.format(outro.getData()) + " às "
                        + Validador.FORMATO_HORARIO.format(outro.getHorario()) + " no local " + outro.getLocal() + ".");
            }
        }
    }

    /** Exclui o treino junto com sua lista de presença. */
    public void excluir(Treino treino) throws PersistenciaException {
        dao.excluir(treino.getId());
    }

    public void carregarPresencas(Treino treino) throws PersistenciaException {
        dao.carregarPresencas(treino);
    }

    /**
     * Atletas que aparecem na lista de presença: os ativos da modalidade
     * e os que já tinham presença registrada no treino.
     */
    public List<Atleta> atletasDaChamada(Treino treino) throws PersistenciaException {
        Set<Atleta> atletas = new LinkedHashSet<>();
        atletaDAO.listarTodos().stream()
                .filter(a -> a.getSituacao() == Situacao.ATIVO && a.praticaModalidade(treino.getModalidade()))
                .sorted()
                .forEach(atletas::add);
        atletas.addAll(treino.getPresencas().keySet());
        return List.copyOf(atletas);
    }

    /** Grava a lista de presença montada com Treino.registrarPresenca (CH-36). */
    public void salvarPresencas(Treino treino) throws RegraNegocioException, PersistenciaException {
        if (treino.getData() != null && treino.getData().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Só é possível registrar presença em treinos que já aconteceram.");
        }
        dao.salvarPresencas(treino);
    }

    /** Frequência por atleta no período, maior percentual primeiro (CH-37). */
    public List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        return dao.frequencia(modalidade, inicio, fim).stream().sorted().toList();
    }
}
