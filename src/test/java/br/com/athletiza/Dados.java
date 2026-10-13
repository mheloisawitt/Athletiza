package br.com.athletiza;

import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Entidade;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Modalidade;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Busca nos dados de exemplo (02_dados_exemplo.sql) os registros usados nos testes.
 */
public final class Dados {

    private Dados() {
    }

    public static Modalidade modalidade(String nome, Genero genero) throws Exception {
        Modalidade procurada = new Modalidade(nome, genero);
        for (Modalidade modalidade : new ModalidadeDAO().listarTodos()) {
            if (modalidade.equals(procurada)) {
                return modalidade;
            }
        }
        throw new AssertionError("Modalidade não encontrada: " + procurada);
    }

    public static Atleta atleta(String nome) throws Exception {
        for (Atleta atleta : new AtletaDAO().listarTodos()) {
            if (atleta.getNome().equals(nome)) {
                return atleta;
            }
        }
        throw new AssertionError("Atleta não encontrado: " + nome);
    }

    public static Membro membro(String nome) throws Exception {
        for (Membro membro : new MembroDAO().listarTodos()) {
            if (membro.getNome().equals(nome)) {
                return membro;
            }
        }
        throw new AssertionError("Membro não encontrado: " + nome);
    }

    public static Competicao competicao(String titulo) throws Exception {
        for (Competicao competicao : new CompeticaoDAO().listarTodos()) {
            if (competicao.getTitulo().equals(titulo)) {
                return competicao;
            }
        }
        throw new AssertionError("Competição não encontrada: " + titulo);
    }

    public static Evento evento(String titulo) throws Exception {
        for (Evento evento : new EventoDAO().listarTodos()) {
            if (evento.getTitulo().equals(titulo)) {
                return evento;
            }
        }
        throw new AssertionError("Evento não encontrado: " + titulo);
    }

    /** Lista com um texto de cada objeto (ex.: os nomes), para comparar nos asserts. */
    public static <T> List<String> textos(List<T> objetos, Function<T, String> texto) {
        List<String> textos = new ArrayList<>();
        for (T objeto : objetos) {
            textos.add(texto.apply(objeto));
        }
        return textos;
    }

    /** Procura pelo id na lista (null se não estiver). */
    public static <T extends Entidade> T porId(List<T> lista, int id) {
        for (T item : lista) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
