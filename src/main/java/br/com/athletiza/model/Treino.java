package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

    /** No calendário aparece apenas "Treino" ou "Amistoso"; o detalhe fica na lista do dia. */
    @Override
    public String getRotuloCurto() {
        return getTipo().getDescricao();
    }

    /**
     * Regras de preenchimento do cadastro (CH-35). Amistoso sobrescreve
     * validarCampos para conferir também o adversário (polimorfismo).
     */
    public final void validar() throws ValidacaoException {
        Validador validador = new Validador();
        validarCampos(validador);
        validador.validar();
    }

    protected void validarCampos(Validador validador) {
        validador.obrigatorio(modalidade, "Modalidade")
                .obrigatorio(getData(), "Data")
                .tamanhoMaximo(getLocal(), 100, "Local")
                .tamanhoMaximo(getTitulo(), 100, "Título")
                .tamanhoMaximo(getObservacoes(), 500, "Observações");
    }

    /** Título usado quando o usuário deixa o campo vazio (ex.: "Treino Futsal (F)"). */
    public String getTituloPadrao() {
        return "Treino " + modalidade;
    }

    /** Preenche o título padrão se ele estiver vazio. */
    public void gerarTituloSeVazio() {
        if (getTitulo() == null || getTitulo().isBlank()) {
            setTitulo(getTituloPadrao());
        }
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

    /** Indica se o atleta está marcado como presente (false se não houver registro). */
    public boolean estavaPresente(Atleta atleta) {
        return Boolean.TRUE.equals(presencas.get(atleta));
    }

    public Map<Atleta, Boolean> getPresencas() {
        return Collections.unmodifiableMap(presencas);
    }

    /** Atletas presentes, em ordem alfabética. */
    public List<Atleta> getPresentes() {
        List<Atleta> presentes = new ArrayList<>();
        for (Map.Entry<Atleta, Boolean> presenca : presencas.entrySet()) {
            if (presenca.getValue()) {
                presentes.add(presenca.getKey());
            }
        }
        Collections.sort(presentes);
        return presentes;
    }
}
