package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Treino de uma modalidade, com responsável e lista de presença (RF11, RF12).
 */
public class Treino extends Atividade {

    private Modalidade modalidade;
    private Membro responsavel;
    /** Atleta -> presente (true) ou ausente (false). */
    private final Map<Atleta, Boolean> presencas = new LinkedHashMap<>();

    public Treino() {
    }

    public Treino(Modalidade modalidade, LocalDate data, LocalTime horario, String local) {
        super("Treino " + modalidade, data, horario, local);
        this.modalidade = modalidade;
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.TREINO;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public Membro getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Membro responsavel) {
        this.responsavel = responsavel;
    }

    public void registrarPresenca(Atleta atleta, boolean presente) {
        presencas.put(atleta, presente);
    }

    public Map<Atleta, Boolean> getPresencas() {
        return Collections.unmodifiableMap(presencas);
    }

    public List<Atleta> getPresentes() {
        return presencas.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }
}
