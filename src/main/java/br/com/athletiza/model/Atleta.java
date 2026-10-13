package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Atleta da atlética (RF03), vinculado a uma ou mais modalidades,
 * cada uma com a sua situação (RF04).
 *
 * Ordem natural: pelo nome. Ordenações alternativas nos Comparators abaixo (CH-11).
 */
public class Atleta extends Pessoa implements Comparable<Atleta> {

    public static final Comparator<Atleta> POR_NOME = (a, b) -> Textos.comparar(a.getNome(), b.getNome());

    public static final Comparator<Atleta> POR_MATRICULA =
            (a, b) -> Textos.compararNulosNoFim(a.getMatricula(), b.getMatricula());

    public static final Comparator<Atleta> POR_SITUACAO = (a, b) -> {
        int resultado = Textos.compararNulosNoFim(a.getSituacao(), b.getSituacao());
        return resultado != 0 ? resultado : POR_NOME.compare(a, b);
    };

    /** Pela primeira modalidade (em ordem alfabética); atletas sem modalidade ficam no final. */
    public static final Comparator<Atleta> POR_MODALIDADE = (a, b) -> {
        int resultado = Textos.compararNulosNoFim(a.getModalidadePrincipal(), b.getModalidadePrincipal());
        return resultado != 0 ? resultado : POR_NOME.compare(a, b);
    };

    private LocalDate dataNascimento;
    private Situacao situacao = Situacao.ATIVO;
    private String observacoes;
    private final Map<Modalidade, SituacaoAtleta> modalidades = new LinkedHashMap<>();

    public Atleta() {
    }

    public Atleta(String nome, String matricula, String contato) {
        super(nome, matricula, contato);
    }

    /** Regras de preenchimento do cadastro (CH-26). Lança ValidacaoException com todos os erros. */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(getNome(), "Nome")
                .tamanhoMaximo(getNome(), 100, "Nome")
                .obrigatorio(getMatricula(), "Matrícula")
                .apenasNumeros(getMatricula(), "Matrícula")
                .tamanhoMaximo(getMatricula(), 20, "Matrícula")
                .contato(getContato(), "Contato")
                .tamanhoMaximo(getContato(), 100, "Contato")
                .obrigatorio(situacao, "Situação")
                .regra(dataNascimento == null || dataNascimento.isBefore(LocalDate.now()),
                        "A \"Data de nascimento\" deve ser anterior a hoje.")
                .regra(!modalidades.isEmpty(), "Selecione ao menos uma modalidade.")
                .tamanhoMaximo(observacoes, 500, "Observações")
                .validar();
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    /** Vincula o atleta à modalidade ou atualiza a situação, se já estiver vinculado. */
    public void vincularModalidade(Modalidade modalidade, SituacaoAtleta situacaoNaModalidade) {
        modalidades.put(modalidade, situacaoNaModalidade);
    }

    public void desvincularModalidade(Modalidade modalidade) {
        modalidades.remove(modalidade);
    }

    /** Remove todos os vínculos (usado antes de gravar as modalidades marcadas na tela). */
    public void desvincularTodas() {
        modalidades.clear();
    }

    public boolean praticaModalidade(Modalidade modalidade) {
        return modalidades.containsKey(modalidade);
    }

    /** Situação do atleta na modalidade, ou null se ele não estiver vinculado a ela. */
    public SituacaoAtleta getSituacaoNaModalidade(Modalidade modalidade) {
        return modalidades.get(modalidade);
    }

    /** Modalidades do atleta, sem repetição e em ordem alfabética. */
    public Set<Modalidade> getModalidades() {
        return Collections.unmodifiableSet(new TreeSet<>(modalidades.keySet()));
    }

    /** Nomes das modalidades separados por vírgula, para as tabelas (ex.: "Futsal (F), Vôlei (F)"). */
    public String getModalidadesTexto() {
        StringBuilder texto = new StringBuilder();
        for (Modalidade modalidade : getModalidades()) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(modalidade);
        }
        return texto.toString();
    }

    public Map<Modalidade, SituacaoAtleta> getVinculos() {
        return Collections.unmodifiableMap(modalidades);
    }

    /** Primeira modalidade em ordem alfabética, ou null se o atleta não tiver nenhuma. */
    public Modalidade getModalidadePrincipal() {
        Set<Modalidade> ordenadas = getModalidades();
        if (ordenadas.isEmpty()) {
            return null;
        }
        return ordenadas.iterator().next();
    }

    @Override
    public int compareTo(Atleta outro) {
        return POR_NOME.compare(this, outro);
    }
}
