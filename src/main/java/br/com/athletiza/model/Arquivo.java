package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDateTime;

/**
 * Foto ou vídeo guardado em uma pasta da galeria de registros.
 * O conteúdo fica no disco; "caminho" é relativo à pasta de mídias.
 * Ordem natural: os enviados mais recentemente primeiro.
 */
public class Arquivo extends Entidade implements Comparable<Arquivo> {

    private Pasta pasta;
    private String nome;
    private String caminho;
    private TipoArquivo tipo;
    private long tamanho;
    private LocalDateTime enviadoEm;
    private String enviadoPor;
    private String descricao;

    /** Regras para renomear, mover ou descrever o arquivo. */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(nome, "Nome")
                .tamanhoMaximo(nome, 150, "Nome")
                .obrigatorio(pasta, "Pasta")
                .tamanhoMaximo(descricao, 500, "Descrição")
                .validar();
    }

    public boolean isImagem() {
        return tipo == TipoArquivo.IMAGEM;
    }

    /** Tamanho legível, ex.: "2,4 MB". */
    public String getTamanhoFormatado() {
        if (tamanho < 1024) {
            return tamanho + " B";
        }
        if (tamanho < 1024 * 1024) {
            return String.format("%.0f KB", tamanho / 1024.0);
        }
        return String.format("%.1f MB", tamanho / (1024.0 * 1024.0)).replace('.', ',');
    }

    public Pasta getPasta() {
        return pasta;
    }

    public void setPasta(Pasta pasta) {
        this.pasta = pasta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public TipoArquivo getTipo() {
        return tipo;
    }

    public void setTipo(TipoArquivo tipo) {
        this.tipo = tipo;
    }

    public long getTamanho() {
        return tamanho;
    }

    public void setTamanho(long tamanho) {
        this.tamanho = tamanho;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public void setEnviadoPor(String enviadoPor) {
        this.enviadoPor = enviadoPor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /** Ordem natural: os enviados mais recentemente primeiro e, empatando, pelo nome. */
    @Override
    public int compareTo(Arquivo outro) {
        if (enviadoEm == null && outro.enviadoEm != null) {
            return 1; // sem data fica no final
        }
        if (enviadoEm != null && outro.enviadoEm == null) {
            return -1;
        }
        if (enviadoEm != null && !enviadoEm.equals(outro.enviadoEm)) {
            return outro.enviadoEm.compareTo(enviadoEm); // invertido: o mais recente primeiro
        }
        return Textos.comparar(nome, outro.nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
