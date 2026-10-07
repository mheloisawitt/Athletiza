package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.model.Treino;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitura de todas as atividades do calendário em um período (CH-15):
 * treinos, amistosos, competições, eventos e compromissos.
 *
 * Apenas consulta; o cadastro de cada tipo fica no DAO do seu módulo.
 */
public class AtividadeDAO extends DAOBase {

    private final CompromissoDAO compromissoDAO = new CompromissoDAO();

    @Override
    protected String getNomeEntidade() {
        return "as atividades do calendário";
    }

    /** Atividades que acontecem, ao menos em parte, entre as duas datas (inclusive). */
    public List<Atividade> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Atividade> atividades = new ArrayList<>();
        atividades.addAll(consultar("SELECT t.*, m.nome AS modalidade_nome, m.genero AS modalidade_genero"
                + " FROM treino t JOIN modalidade m ON m.id = t.modalidade_id"
                + " WHERE t.data BETWEEN ? AND ?", this::mapearTreino, inicio, fim));
        atividades.addAll(consultar("SELECT * FROM competicao WHERE data <= ? AND COALESCE(data_fim, data) >= ?",
                this::mapearCompeticao, fim, inicio));
        atividades.addAll(consultar("SELECT * FROM evento WHERE data BETWEEN ? AND ?",
                this::mapearEvento, inicio, fim));
        atividades.addAll(compromissoDAO.listarPorPeriodo(inicio, fim));
        return atividades;
    }

    private Treino mapearTreino(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade(rs.getString("modalidade_nome"),
                lerEnum(rs, "modalidade_genero", Genero.class));
        modalidade.setId(rs.getInt("modalidade_id"));

        Treino treino;
        if ("AMISTOSO".equals(rs.getString("tipo"))) {
            Amistoso amistoso = new Amistoso();
            amistoso.setAdversario(rs.getString("adversario"));
            amistoso.setResultado(rs.getString("resultado"));
            treino = amistoso;
        } else {
            treino = new Treino();
        }
        treino.setId(rs.getInt("id"));
        treino.setModalidade(modalidade);
        preencherComum(treino, rs);
        return treino;
    }

    private Competicao mapearCompeticao(ResultSet rs) throws SQLException {
        Competicao competicao = new Competicao();
        competicao.setId(rs.getInt("id"));
        competicao.setDataFim(lerData(rs, "data_fim"));
        competicao.setSituacao(lerEnum(rs, "situacao", SituacaoCompeticao.class));
        preencherComum(competicao, rs);
        return competicao;
    }

    private Evento mapearEvento(ResultSet rs) throws SQLException {
        Evento evento = new Evento();
        evento.setId(rs.getInt("id"));
        evento.setTipoEvento(lerEnum(rs, "tipo_evento", TipoEvento.class));
        evento.setSituacao(lerEnum(rs, "situacao", SituacaoEvento.class));
        evento.setDescricao(rs.getString("descricao"));
        preencherComum(evento, rs);
        return evento;
    }

    private static void preencherComum(Atividade atividade, ResultSet rs) throws SQLException {
        atividade.setTitulo(rs.getString("titulo"));
        atividade.setData(lerData(rs, "data"));
        atividade.setHorario(lerHorario(rs, "horario"));
        atividade.setLocal(rs.getString("local"));
        atividade.setObservacoes(rs.getString("observacoes"));
    }
}
