package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Arquivo;
import br.com.athletiza.modelo.Pasta;
import br.com.athletiza.modelo.TipoArquivo;
import br.com.athletiza.repositorio.ArquivoRepositorio;
import br.com.athletiza.repositorio.PastaRepositorio;
import br.com.athletiza.util.ArmazenamentoMidia;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.registros.PainelRegistros;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GaleriaControlador {
    static final long TAMANHO_MAXIMO = 500L * 1024 * 1024;

    private PainelRegistros visao;
    private final PastaRepositorio pastaRepositorio;
    private final ArquivoRepositorio arquivoRepositorio;

    public GaleriaControlador(PastaRepositorio pastaRepositorio, ArquivoRepositorio arquivoRepositorio) {
        this.pastaRepositorio = pastaRepositorio;
        this.arquivoRepositorio = arquivoRepositorio;
    }

    public GaleriaControlador(PainelRegistros visao, PastaRepositorio pastaRepositorio,
            ArquivoRepositorio arquivoRepositorio) {
        this(pastaRepositorio, arquivoRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoCarregarArquivos(() -> carregarArquivos());
        visao.adicionarAcaoBtnNovaPasta(e -> novaPasta());
        visao.adicionarAcaoAdicionarArquivos(arquivos -> adicionarNaTela(arquivos));
        visao.adicionarAcaoRenomearPasta(pasta -> renomearPasta(pasta));
        visao.adicionarAcaoExcluirPasta(pasta -> excluirPastaNaTela(pasta));
        visao.adicionarAcaoEditarArquivo(arquivo -> editarArquivo(arquivo));
        visao.adicionarAcaoExcluirArquivo(arquivo -> excluirArquivoNaTela(arquivo));
    }

    public void carregar() {
        try {
            visao.apresentarPastas(listarPastas());
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        carregarArquivos();
    }

    private void carregarArquivos() {
        Pasta pasta = visao.getPastaSelecionada();
        List<Arquivo> arquivos = new ArrayList<>();
        if (pasta != null) {
            try {
                arquivos = listarArquivos(pasta);
            } catch (PersistenciaException e) {
                visao.apresentarErro(e);
            }
        }
        visao.apresentarArquivos(arquivos);
    }

    private void novaPasta() {
        String nome = visao.pedirNomePasta("Nova pasta", "");
        if (nome == null) {
            return;
        }
        try {
            Pasta pasta = new Pasta(nome.trim());
            salvarPasta(pasta);
            carregar();
            visao.selecionarPasta(pasta);
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
    }

    private void renomearPasta(Pasta pasta) {
        String nome = visao.pedirNomePasta("Renomear pasta", pasta.getNome());
        if (nome == null) {
            return;
        }
        String anterior = pasta.getNome();
        pasta.setNome(nome.trim());
        try {
            salvarPasta(pasta);
        } catch (AthletizaException e) {
            pasta.setNome(anterior);
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void excluirPastaNaTela(Pasta pasta) {
        if (!visao.confirmar("Excluir a pasta \"" + pasta.getNome() + "\"?")) {
            return;
        }
        try {
            excluirPasta(pasta);
            visao.limparSelecaoDePasta();
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void adicionarNaTela(List<Path> arquivos) {
        Pasta pasta = visao.getPastaSelecionada();
        try {
            int quantidade = adicionarArquivos(pasta, arquivos).size();
            carregar();
            visao.apresentarMensagem(quantidade + " arquivo(s) adicionado(s) à pasta " + pasta + ".");
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editarArquivo(Arquivo arquivo) {
        boolean continuar = true;
        while (continuar && visao.pedirDadosArquivo(arquivo)) {
            try {
                salvarArquivo(arquivo);
                continuar = false;
            } catch (AthletizaException e) {
                visao.apresentarErro(e);
            }
        }
        carregar();
    }

    private void excluirArquivoNaTela(Arquivo arquivo) {
        if (!visao.confirmar("Excluir \"" + arquivo.getNome() + "\"? O arquivo também será apagado do computador.")) {
            return;
        }
        try {
            excluirArquivo(arquivo);
            visao.removerMiniatura(arquivo);
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    public List<Pasta> listarPastas() throws PersistenciaException {
        return pastaRepositorio.listarTodos().stream().sorted().toList();
    }

    public void salvarPasta(Pasta pasta) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(pasta.getNome(), "Nome da pasta")
                .tamanhoMaximo(pasta.getNome(), 60, "Nome da pasta")
                .validar();
        boolean repetida = pastaRepositorio.listarTodos().stream()
                .anyMatch(p -> p.equals(pasta) && !p.getId().equals(pasta.getId()));
        if (repetida) {
            throw new RegraNegocioException("Já existe uma pasta chamada " + pasta.getNome() + ".");
        }
        if (pasta.isNova()) {
            pastaRepositorio.inserir(pasta);
        } else {
            pastaRepositorio.atualizar(pasta);
        }
    }

    public void excluirPasta(Pasta pasta) throws RegraNegocioException, PersistenciaException {
        int arquivos = arquivoRepositorio.listarPorPasta(pasta).size();
        if (arquivos > 0) {
            throw new RegraNegocioException("A pasta " + pasta.getNome() + " possui " + arquivos
                    + " arquivo(s). Exclua ou mova os arquivos antes de excluir a pasta.");
        }
        pastaRepositorio.excluir(pasta.getId());
    }

    public List<Arquivo> listarArquivos(Pasta pasta) throws PersistenciaException {
        return arquivoRepositorio.listarPorPasta(pasta).stream().sorted().toList();
    }

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
                arquivoRepositorio.inserir(arquivo);
            } catch (PersistenciaException e) {
                ArmazenamentoMidia.apagar(caminho);
                throw e;
            }
            adicionados.add(arquivo);
        }
        return adicionados;
    }

    public void salvarArquivo(Arquivo arquivo) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(arquivo.getNome(), "Nome")
                .tamanhoMaximo(arquivo.getNome(), 150, "Nome")
                .obrigatorio(arquivo.getPasta(), "Pasta")
                .tamanhoMaximo(arquivo.getDescricao(), 500, "Descrição")
                .validar();
        arquivoRepositorio.atualizar(arquivo);
    }

    public void excluirArquivo(Arquivo arquivo) throws PersistenciaException {
        arquivoRepositorio.excluir(arquivo.getId());
        ArmazenamentoMidia.apagar(arquivo.getCaminho());
    }

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
