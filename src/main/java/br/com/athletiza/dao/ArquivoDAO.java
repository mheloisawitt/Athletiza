package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import br.com.athletiza.model.TipoArquivo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acesso às informações dos arquivos da galeria (o conteúdo fica no disco).
 */
public class ArquivoDAO extends AbstractDAO<Arquivo> {

    @Override
    protected String getTabela() {
        return "arquivo";
    }

    @Override
    protected String getNomeEntidade() {
        return "o arquivo";
    }

    @Override
    public void inserir(Arquivo arquivo) throws PersistenciaException {
        arquivo.setId(executarInsercao("INSERT INTO arquivo (pasta_id, nome, caminho, tipo, tamanho, enviado_em,"
                + " enviado_por, descricao) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                arquivo.getPasta().getId(), arquivo.getNome(), arquivo.getCaminho(), arquivo.getTipo(),
                arquivo.getTamanho(), arquivo.getEnviadoEm(), arquivo.getEnviadoPor(), arquivo.getDescricao()));
    }

    /** Altera nome, pasta e descrição (o arquivo no disco não muda de lugar). */
    @Override
    public void atualizar(Arquivo arquivo) throws PersistenciaException {
        executarAtualizacao("UPDATE arquivo SET pasta_id = ?, nome = ?, descricao = ? WHERE id = ?",
                arquivo.getPasta().getId(), arquivo.getNome(), arquivo.getDescricao(), arquivo.getId());
    }

    public List<Arquivo> listarPorPasta(Pasta pasta) throws PersistenciaException {
        return consultar("SELECT * FROM arquivo WHERE pasta_id = ?", rs -> {
            Arquivo arquivo = mapear(rs);
            arquivo.setPasta(pasta);
            return arquivo;
        }, pasta.getId());
    }

    @Override
    protected Arquivo mapear(ResultSet rs) throws SQLException {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(rs.getInt("id"));
        arquivo.setNome(rs.getString("nome"));
        arquivo.setCaminho(rs.getString("caminho"));
        arquivo.setTipo(lerEnum(rs, "tipo", TipoArquivo.class));
        arquivo.setTamanho(rs.getLong("tamanho"));
        arquivo.setEnviadoEm(lerDataHora(rs, "enviado_em"));
        arquivo.setEnviadoPor(rs.getString("enviado_por"));
        arquivo.setDescricao(rs.getString("descricao"));
        return arquivo;
    }
}
