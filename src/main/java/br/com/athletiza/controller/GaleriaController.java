package br.com.athletiza.controller;

import br.com.athletiza.dao.ArquivoDAO;
import br.com.athletiza.dao.PastaDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import br.com.athletiza.model.TipoArquivo;
import br.com.athletiza.util.ArmazenamentoMidia;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Galeria de registros: pastas com fotos e vídeos da atlética (CH-42, CH-43).
 */
public class GaleriaController {

    /** Tamanho máximo de cada arquivo enviado. */
    static final long TAMANHO_MAXIMO = 500L * 1024 * 1024;

    private final PastaDAO pastaDAO = new PastaDAO();
    private final ArquivoDAO arquivoDAO = new ArquivoDAO();

    /** Pastas em ordem alfabética, com a quantidade de arquivos. */
    public List<Pasta> listarPastas() throws PersistenciaException {
        return pastaDAO.listarTodos().stream().sorted().toList();
    }

    public void salvarPasta(Pasta pasta) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(pasta.getNome(), "Nome da pasta")
                .tamanhoMaximo(pasta.getNome(), 60, "Nome da pasta")
                .validar();
        boolean repetida = pastaDAO.listarTodos().stream()
                .anyMatch(p -> p.equals(pasta) && !p.getId().equals(pasta.getId()));
        if (repetida) {
            throw new RegraNegocioException("Já existe uma pasta chamada " + pasta.getNome() + ".");
        }
        if (pasta.isNova()) {
            pastaDAO.inserir(pasta);
        } else {
            pastaDAO.atualizar(pasta);
        }
    }

    /** Só exclui pastas vazias, para nenhuma foto ser apagada por engano. */
    public void excluirPasta(Pasta pasta) throws RegraNegocioException, PersistenciaException {
        int arquivos = arquivoDAO.listarPorPasta(pasta).size();
        if (arquivos > 0) {
            throw new RegraNegocioException("A pasta " + pasta.getNome() + " possui " + arquivos
                    + " arquivo(s). Exclua ou mova os arquivos antes de excluir a pasta.");
        }
        pastaDAO.excluir(pasta.getId());
    }

    /** Arquivos da pasta, os mais recentes primeiro. */
    public List<Arquivo> listarArquivos(Pasta pasta) throws PersistenciaException {
        return arquivoDAO.listarPorPasta(pasta).stream().sorted().toList();
    }

    /**
     * Copia os arquivos para a pasta de mídias e registra cada um no banco.
     * Arquivos de tipo não aceito ou grandes demais são recusados antes de qualquer cópia.
     *
     * @return os arquivos adicionados
     */
    public List<Arquivo> adicionarArquivos(Pasta pasta, List<Path> origens)
            throws ValidacaoException, PersistenciaException {
        Validador validador = new Validador().obrigatorio(pasta, "Pasta");
        for (Path origem : origens) {
            String nome = origem.getFileName().toString();
            if (!Files.isRegularFile(origem)) {
                validador.regra(false, "O arquivo \"" + nome + "\" não foi encontrado.");
                continue;
            }
            validador.regra(TipoArquivo.doArquivo(nome).isPresent(),
                    "\"" + nome + "\" não é uma imagem (jpg, png, gif, bmp) nem um vídeo (mp4, mov, avi, mkv, webm, wmv).");
            validador.regra(tamanhoDe(origem) <= TAMANHO_MAXIMO, "\"" + nome + "\" passa do limite de 500 MB.");
        }
        validador.validar();

        List<Arquivo> adicionados = new ArrayList<>();
        for (Path origem : origens) {
            String caminho = ArmazenamentoMidia.copiar(origem, pasta.getId());
            Arquivo arquivo = new Arquivo();
            arquivo.setPasta(pasta);
            arquivo.setNome(semExtensao(origem.getFileName().toString()));
            arquivo.setCaminho(caminho);
            arquivo.setTipo(TipoArquivo.doArquivo(origem.getFileName().toString()).orElseThrow());
            arquivo.setTamanho(tamanhoDe(origem));
            arquivo.setEnviadoEm(LocalDateTime.now());
            arquivo.setEnviadoPor(Sessao.getUsuarioLogado() == null ? null : Sessao.getUsuarioLogado().getLogin());
            try {
                arquivoDAO.inserir(arquivo);
            } catch (PersistenciaException e) {
                ArmazenamentoMidia.apagar(caminho);
                throw e;
            }
            adicionados.add(arquivo);
        }
        return adicionados;
    }

    /** Renomeia, move para outra pasta ou altera a descrição do arquivo. */
    public void salvarArquivo(Arquivo arquivo) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(arquivo.getNome(), "Nome")
                .tamanhoMaximo(arquivo.getNome(), 150, "Nome")
                .obrigatorio(arquivo.getPasta(), "Pasta")
                .tamanhoMaximo(arquivo.getDescricao(), 500, "Descrição")
                .validar();
        arquivoDAO.atualizar(arquivo);
    }

    /** Exclui o registro do banco e o arquivo do disco. */
    public void excluirArquivo(Arquivo arquivo) throws PersistenciaException {
        arquivoDAO.excluir(arquivo.getId());
        ArmazenamentoMidia.apagar(arquivo.getCaminho());
    }

    /** Local do arquivo no disco, para abrir ou gerar a miniatura. */
    public Path localDoArquivo(Arquivo arquivo) {
        return ArmazenamentoMidia.resolver(arquivo.getCaminho());
    }

    private static long tamanhoDe(Path arquivo) {
        try {
            return Files.size(arquivo);
        } catch (IOException e) {
            return Long.MAX_VALUE;
        }
    }

    private static String semExtensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto > 0 ? nome.substring(0, ponto) : nome;
    }
}
