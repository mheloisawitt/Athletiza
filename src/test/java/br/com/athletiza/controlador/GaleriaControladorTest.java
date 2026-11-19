package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.ArquivoDAO;
import br.com.athletiza.dao.PastaDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Arquivo;
import br.com.athletiza.modelo.Pasta;
import br.com.athletiza.modelo.TipoArquivo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GaleriaControladorTest {
    @TempDir
    Path temporaria;

    private final GaleriaControlador controlador = new GaleriaControlador(new PastaDAO(), new ArquivoDAO());
    private Path origem;

    @BeforeEach
    void preparar() throws Exception {
        BancoDeTeste.recriar();
        System.setProperty("athletiza.midias", temporaria.resolve("midias").toString());
        origem = Files.createDirectories(temporaria.resolve("origem"));
    }

    @AfterEach
    void limpar() {
        System.clearProperty("athletiza.midias");
    }

    @Test
    void listaAsPastasDosDadosDeExemploEmOrdemAlfabetica() throws Exception {
        assertEquals(List.of("Ações Sociais", "Competições", "Festas", "Outros", "Treinos"),
                controlador.listarPastas().stream().map(Pasta::getNome).toList());
    }

    @Test
    void criaPastaSemNomeRepetido() throws Exception {
        controlador.salvarPasta(new Pasta("Viagens"));

        assertEquals(6, controlador.listarPastas().size());
        assertThrows(RegraNegocioException.class, () -> controlador.salvarPasta(new Pasta("festas")));
        assertThrows(ValidacaoException.class, () -> controlador.salvarPasta(new Pasta(" ")));
    }

    @Test
    void adicionaFotosEVideosCopiandoParaAPastaDeMidias() throws Exception {
        Pasta festas = pasta("Festas");
        Path foto = Files.write(origem.resolve("Festa da Atletica 2025.JPG"), new byte[2048]);
        Path video = Files.write(origem.resolve("video-festa.mp4"), new byte[4096]);

        List<Arquivo> adicionados = controlador.adicionarArquivos(festas, List.of(foto, video));

        assertEquals(2, adicionados.size());
        Arquivo arquivoFoto = adicionados.get(0);
        assertEquals("Festa da Atletica 2025", arquivoFoto.getNome());
        assertEquals(TipoArquivo.IMAGEM, arquivoFoto.getTipo());
        assertTrue(arquivoFoto.getCaminho().endsWith("-festa-da-atletica-2025.jpg"), arquivoFoto.getCaminho());
        assertTrue(Files.exists(controlador.localDoArquivo(arquivoFoto)));
        assertEquals(TipoArquivo.VIDEO, adicionados.get(1).getTipo());
        assertEquals(2, controlador.listarArquivos(festas).size());
        assertEquals(2, pasta("Festas").getQuantidadeArquivos());
    }

    @Test
    void recusaTiposNaoAceitosSemCopiarNada() throws Exception {
        Path foto = Files.write(origem.resolve("foto.png"), new byte[10]);
        Path texto = Files.write(origem.resolve("ata.txt"), new byte[10]);

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controlador.adicionarArquivos(pasta("Festas"), List.of(foto, texto)));

        assertTrue(erro.getMessage().contains("ata.txt"), erro.getMessage());
        assertTrue(controlador.listarArquivos(pasta("Festas")).isEmpty());
        assertFalse(Files.exists(temporaria.resolve("midias")));
    }

    @Test
    void renomeiaEMoveArquivoDePasta() throws Exception {
        Arquivo arquivo = controlador.adicionarArquivos(pasta("Festas"),
                List.of(Files.write(origem.resolve("acao.jpg"), new byte[10]))).get(0);

        arquivo.setNome("Ação no asilo");
        arquivo.setPasta(pasta("Ações Sociais"));
        controlador.salvarArquivo(arquivo);

        assertTrue(controlador.listarArquivos(pasta("Festas")).isEmpty());
        assertEquals("Ação no asilo", controlador.listarArquivos(pasta("Ações Sociais")).get(0).getNome());
    }

    @Test
    void excluiArquivoDoBancoEDoDiscoESoExcluiPastaVazia() throws Exception {
        Pasta festas = pasta("Festas");
        Arquivo arquivo = controlador.adicionarArquivos(festas,
                List.of(Files.write(origem.resolve("foto.jpg"), new byte[10]))).get(0);
        Path noDisco = controlador.localDoArquivo(arquivo);

        assertThrows(RegraNegocioException.class, () -> controlador.excluirPasta(festas));
        controlador.excluirArquivo(arquivo);
        assertFalse(Files.exists(noDisco));
        controlador.excluirPasta(festas);

        assertEquals(4, controlador.listarPastas().size());
    }

    private Pasta pasta(String nome) throws Exception {
        return controlador.listarPastas().stream().filter(p -> p.getNome().equals(nome)).findFirst().orElseThrow();
    }
}
