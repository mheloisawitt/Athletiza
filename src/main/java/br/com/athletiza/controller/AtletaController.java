package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Regras do cadastro de atletas (CH-24 a CH-26).
 */
public class AtletaController {

    private final AtletaDAO dao = new AtletaDAO();

    /** Todos os atletas em ordem alfabética (ordem natural de Atleta). */
    public List<Atleta> listar() throws PersistenciaException {
        return dao.listarTodos().stream().sorted().toList();
    }

    /**
     * Filtra os atletas (CH-24). Filtros nulos ou vazios são ignorados.
     *
     * @param texto      trecho do nome ou da matrícula
     * @param modalidade apenas atletas vinculados a ela
     * @param situacao   apenas atletas nessa situação
     * @param ordem      critério de ordenação (ex.: Atleta.POR_SITUACAO); null = por nome
     */
    public List<Atleta> pesquisar(String texto, Modalidade modalidade, Situacao situacao, Comparator<Atleta> ordem)
            throws PersistenciaException {
        String busca = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return dao.listarTodos().stream()
                .filter(a -> busca.isEmpty() || a.getNome().toLowerCase(Locale.ROOT).contains(busca)
                        || a.getMatricula().contains(busca))
                .filter(a -> modalidade == null || a.praticaModalidade(modalidade))
                .filter(a -> situacao == null || a.getSituacao() == situacao)
                .sorted(ordem == null ? Comparator.naturalOrder() : ordem)
                .toList();
    }

    public void salvar(Atleta atleta) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(atleta.getNome(), "Nome")
                .tamanhoMaximo(atleta.getNome(), 100, "Nome")
                .obrigatorio(atleta.getMatricula(), "Matrícula")
                .apenasNumeros(atleta.getMatricula(), "Matrícula")
                .tamanhoMaximo(atleta.getMatricula(), 20, "Matrícula")
                .contato(atleta.getContato(), "Contato")
                .tamanhoMaximo(atleta.getContato(), 100, "Contato")
                .obrigatorio(atleta.getSituacao(), "Situação")
                .regra(atleta.getDataNascimento() == null || atleta.getDataNascimento().isBefore(LocalDate.now()),
                        "A \"Data de nascimento\" deve ser anterior a hoje.")
                .regra(!atleta.getVinculos().isEmpty(), "Selecione ao menos uma modalidade.")
                .tamanhoMaximo(atleta.getObservacoes(), 500, "Observações")
                .validar();

        if (dao.existeMatricula(atleta.getMatricula(), atleta.getId())) {
            throw new RegraNegocioException("Já existe um atleta com a matrícula " + atleta.getMatricula() + ".");
        }
        if (atleta.isNova()) {
            dao.inserir(atleta);
        } else {
            dao.atualizar(atleta);
        }
    }

    /** Exclui o atleta, a menos que tenha presenças, inscrições ou resultados (CH-45). */
    public void excluir(Atleta atleta) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = dao.listarVinculos(atleta.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + atleta.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        dao.excluir(atleta.getId());
    }
}
