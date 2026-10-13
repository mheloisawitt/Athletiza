package br.com.athletiza.controller;

import br.com.athletiza.dao.ArquivoRepository;
import br.com.athletiza.dao.PastaRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import br.com.athletiza.model.TipoArquivo;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.ArmazenamentoMidia;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.registros.PainelRegistros;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * Controller da galeria de registros: pastas com fotos e vídeos da atlética (CH-42, CH-43).
 */
public class RegistrosController {

    /** Tamanho máximo de cada arquivo enviado. */
    static final long TAMANHO_MAXIMO = 500L * 1024 * 1024;

    private final PainelRegistros view;
    private final PastaRepository pastaRepository;
    private final ArquivoRepository arquivoRepository;

    public RegistrosController(PainelRegistros view, PastaRepository pastaRepository,
            ArquivoRepository arquivoRepository) {
        this.view = view;
        this.pastaRepository = pastaRepository;
        this.arquivoRepository = arquivoRepository;
        if (view != null) {
            view.setPastaDasMidias(ArmazenamentoMidia.getPastaRaiz());
            adicionarAcoesBotoes();
        }
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoSelecionarPasta(e -> carregarArquivos());
        view.adicionarAcaoNovaPasta(e -> novaPasta());
        view.adicionarAcaoRenomearPasta(e -> renomearPasta(view.getPastaClicada()));
        view.adicionarAcaoExcluirPasta(e -> excluirPasta(view.getPastaClicada()));
        view.adicionarAcaoAdicionarArquivos(e -> adicionar(view.getPastaSelecionada(), view.getArquivosEscolhidos()));
        view.adicionarAcaoAbrirArquivo(e -> abrir(view.getArquivoClicado()));
        view.adicionarAcaoEditarArquivo(e -> editarArquivo(view.getArquivoClicado()));
        view.adicionarAcaoExcluirArquivo(e -> excluirArquivo(view.getArquivoClicado()));
    }

    private void carregar() {
        try {
            view.exibirPastas(listarPastas());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregarArquivos();
    }

    private void carregarArquivos() {
        Pasta pasta = view.getPastaSelecionada();
        List<Arquivo> arquivos = new ArrayList<>();
        if (pasta != null) {
            try {
                arquivos = listarArquivos(pasta);
            } catch (PersistenciaException e) {
                view.mostrarErro(e);
            }
        }
        view.exibirArquivos(pasta, arquivos);
    }

    /** Pastas em ordem alfabética, com a quantidade de arquivos. */
    List<Pasta> listarPastas() throws PersistenciaException {
        List<Pasta> pastas = pastaRepository.listarTodos();
        Collections.sort(pastas);
        return pastas;
    }

    /** Arquivos da pasta, os mais recentes primeiro. */
    List<Arquivo> listarArquivos(Pasta pasta) throws PersistenciaException {
        List<Arquivo> arquivos = arquivoRepository.listarPorPasta(pasta);
        Collections.sort(arquivos);
        return arquivos;
    }

    /* Pastas */

    private void novaPasta() {
        String nome = view.pedirNomePasta("Nova pasta", "");
        if (nome == null) {
            return;
        }
        try {
            Pasta pasta = new Pasta(nome.trim());
            salvarPasta(pasta);
            carregar();
            view.selecionarPasta(pasta);
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    private void renomearPasta(Pasta pasta) {
        String nome = view.pedirNomePasta("Renomear pasta", pasta.getNome());
        if (nome == null) {
            return;
        }
        String anterior = pasta.getNome();
        pasta.setNome(nome.trim());
        try {
            salvarPasta(pasta);
        } catch (AthletizaException e) {
            pasta.setNome(anterior);
            view.mostrarErro(e);
        }
        carregar();
    }

    void salvarPasta(Pasta pasta) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        pasta.validar();
        for (Pasta outra : pastaRepository.listarTodos()) {
            if (outra.equals(pasta) && !outra.getId().equals(pasta.getId())) {
                throw new RegraNegocioException("Já existe uma pasta chamada " + pasta.getNome() + ".");
            }
        }
        if (pasta.isNova()) {
            pastaRepository.inserir(pasta);
        } else {
            pastaRepository.atualizar(pasta);
        }
    }

    private void excluirPasta(Pasta pasta) {
        if (!view.confirmar("Excluir a pasta \"" + pasta.getNome() + "\"?")) {
            return;
        }
        try {
            excluirPastaVazia(pasta);
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Só exclui pastas vazias, para nenhuma foto ser apagada por engano. */
    void excluirPastaVazia(Pasta pasta) throws RegraNegocioException, PersistenciaException {
        int arquivos = arquivoRepository.listarPorPasta(pasta).size();
        if (arquivos > 0) {
            throw new RegraNegocioException("A pasta " + pasta.getNome() + " possui " + arquivos
                    + " arquivo(s). Exclua ou mova os arquivos antes de excluir a pasta.");
        }
        pastaRepository.excluir(pasta.getId());
    }

    /* Arquivos */

    /** Copia os arquivos em segundo plano (vídeos podem ser grandes) e atualiza a tela ao final. */
    private void adicionar(Pasta pasta, List<Path> origens) {
        if (pasta == null || origens.isEmpty()) {
            return;
        }
        view.setOcupado(true);
        new SwingWorker<List<Arquivo>, Void>() {
            @Override
            protected List<Arquivo> doInBackground() throws Exception {
                return adicionarArquivos(pasta, origens);
            }

            @Override
            protected void done() {
                view.setOcupado(false);
                try {
                    int quantidade = get().size();
                    carregar();
                    view.mostrarMensagem(quantidade + " arquivo(s) adicionado(s) à pasta " + pasta + ".");
                } catch (ExecutionException e) {
                    view.mostrarErro(e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /**
     * Copia os arquivos para a pasta de mídias e registra cada um no banco.
     * Arquivos de tipo não aceito ou grandes demais são recusados antes de qualquer cópia.
     *
     * @return os arquivos adicionados
     */
    List<Arquivo> adicionarArquivos(Pasta pasta, List<Path> origens) throws ValidacaoException, PersistenciaException {
        Validador validador = new Validador().obrigatorio(pasta, "Pasta");
        for (Path origem : origens) {
            String nome = origem.getFileName().toString();
            if (!Files.isRegularFile(origem)) {
                validador.regra(false, "O arquivo \"" + nome + "\" não foi encontrado.");
                continue;
            }
            validador.regra(TipoArquivo.doArquivo(nome) != null,
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
            arquivo.setTipo(TipoArquivo.doArquivo(origem.getFileName().toString()));
            arquivo.setTamanho(tamanhoDe(origem));
            arquivo.setEnviadoEm(LocalDateTime.now());
            arquivo.setEnviadoPor(Sessao.getUsuarioLogado() == null ? null : Sessao.getUsuarioLogado().getLogin());
            try {
                arquivoRepository.inserir(arquivo);
            } catch (PersistenciaException e) {
                ArmazenamentoMidia.apagar(caminho); // não deixa no disco um arquivo sem registro no banco
                throw e;
            }
            adicionados.add(arquivo);
        }
        return adicionados;
    }

    private void abrir(Arquivo arquivo) {
        Path local = ArmazenamentoMidia.resolver(arquivo.getCaminho());
        if (!Files.exists(local)) {
            view.mostrarAviso("O arquivo não foi encontrado em:\n" + local
                    + "\n\nEle pode ter sido adicionado em outro computador.");
            return;
        }
        view.abrirNoComputador(local);
    }

    /** "Renomear ou mover": reabre enquanto houver erro. */
    private void editarArquivo(Arquivo arquivo) {
        List<Pasta> pastas;
        try {
            pastas = listarPastas();
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
            return;
        }
        while (view.exibirFormularioArquivo(arquivo, pastas)) {
            try {
                arquivo.setNome(view.getNomeArquivo());
                arquivo.setPasta(view.getPastaArquivo());
                arquivo.setDescricao(view.getDescricaoArquivo());
                salvarArquivo(arquivo);
                break;
            } catch (AthletizaException e) {
                view.mostrarErro(e);
            }
        }
        carregar();
    }

    /** Renomeia, move para outra pasta ou altera a descrição do arquivo. */
    void salvarArquivo(Arquivo arquivo) throws ValidacaoException, PersistenciaException {
        arquivo.validar();
        arquivoRepository.atualizar(arquivo);
    }

    private void excluirArquivo(Arquivo arquivo) {
        if (!view.confirmar("Excluir \"" + arquivo.getNome() + "\"? O arquivo também será apagado do computador.")) {
            return;
        }
        try {
            excluirDoBancoEDoDisco(arquivo);
            view.removerMiniatura(arquivo);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Exclui o registro do banco e o arquivo do disco. */
    void excluirDoBancoEDoDisco(Arquivo arquivo) throws PersistenciaException {
        arquivoRepository.excluir(arquivo.getId());
        ArmazenamentoMidia.apagar(arquivo.getCaminho());
    }

    private static long tamanhoDe(Path arquivo) {
        try {
            return Files.size(arquivo);
        } catch (IOException e) {
            return Long.MAX_VALUE; // se não der para ler o tamanho, o arquivo é recusado
        }
    }

    private static String semExtensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto > 0 ? nome.substring(0, ponto) : nome;
    }

    public PainelRegistros getView() {
        return view;
    }
}
