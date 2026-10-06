package br.com.athletiza.model;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Registro do histórico de operações: quem fez o quê e quando (CH-42).
 * Ordem natural: do mais recente para o mais antigo (CH-43).
 *
 * Premissa a confirmar com o cliente (CH-01): no protótipo, a tela
 * "Registros" aparece como galeria de fotos e vídeos.
 */
public class Registro extends Entidade implements Comparable<Registro> {

    private static final Comparator<Registro> MAIS_RECENTE_PRIMEIRO = Comparator.comparing(
            Registro::getDataHora, Comparator.nullsLast(Comparator.reverseOrder()));

    private String usuarioLogin;
    private AcaoRegistro acao;
    private String modulo;
    private LocalDateTime dataHora;
    private String descricao;

    public Registro() {
    }

    public Registro(String usuarioLogin, AcaoRegistro acao, String modulo, String descricao) {
        this.usuarioLogin = usuarioLogin;
        this.acao = acao;
        this.modulo = modulo;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }

    public void setUsuarioLogin(String usuarioLogin) {
        this.usuarioLogin = usuarioLogin;
    }

    public AcaoRegistro getAcao() {
        return acao;
    }

    public void setAcao(AcaoRegistro acao) {
        this.acao = acao;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public int compareTo(Registro outro) {
        return MAIS_RECENTE_PRIMEIRO.compare(this, outro);
    }
}
