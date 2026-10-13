package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Amistoso: um treino contra um adversário, com resultado (CH-35).
 */
public class Amistoso extends Treino {

    private String adversario;
    private String resultado;

    public Amistoso() {
    }

    public Amistoso(Modalidade modalidade, String adversario, LocalDate data, LocalTime horario, String local) {
        super(modalidade, data, horario, local);
        this.adversario = adversario;
        setTitulo(getTituloPadrao());
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.AMISTOSO;
    }

    /** Além dos campos do treino, o adversário é obrigatório. */
    @Override
    protected void validarCampos(Validador validador) {
        super.validarCampos(validador);
        validador.obrigatorio(adversario, "Adversário")
                .tamanhoMaximo(adversario, 100, "Adversário")
                .tamanhoMaximo(resultado, 100, "Resultado");
    }

    @Override
    public String getTituloPadrao() {
        return "Amistoso " + getModalidade() + " x " + adversario;
    }

    public String getAdversario() {
        return adversario;
    }

    public void setAdversario(String adversario) {
        this.adversario = adversario;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
