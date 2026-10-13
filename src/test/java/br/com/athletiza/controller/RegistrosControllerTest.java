package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.ArquivoDAO;
import br.com.athletiza.dao.PastaDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import br.com.athletiza.model.TipoArquivo;
import br.com.athletiza.util.ArmazenamentoMidia;
import br.com.athletiza.Dados;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Testes da galeria de registros. A pasta de mídias é uma pasta temporária,
 * para os testes não mexerem nas fotos de verdade.
 */
class RegistrosControllerTest {

    @TempDir
    Path temporaria;

    /** Sem tela: os testes chamam direto os casos de uso do controller. */
    private final RegistrosController controller = new RegistrosController(null, new PastaDAO(), new ArquivoDAO());
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
        assertEquals(Arrays.asList("Ações Sociais", "Competições", "Festas", "Outros", "Treinos"),
                Dados.textos(controller.listarPastas(), p -> p.getNome()));
    }

    @Test
    void criaPastaSemNomeRepetido() throws Exception {
        controller.salvarPasta(new Pasta("Viagens"));

        assertEquals(6, controller.listarPastas().size());
        assertThrows(RegraNegocioException.class, () -> controller.salvarPasta(new Pasta("festas")));
        assertThrows(ValidacaoException.class, () -> controller.salvarPasta(new Pasta(" ")));
    }

    @Test
    void adicionaFotosEVideosCopiandoParaAPastaDeMidias() throws Exception {
        Pasta festas = pasta("Festas");
        Path foto = Files.write(origem.resolve("Festa da Atletica 2025.JPG"), new byte[2048]);
        Path video = Files.write(origem.resolve("video-festa.mp4"), new byte[4096]);

        List<Arquivo> adicionados = controller.adicionarArquivos(festas, Arrays.asList(foto, video));

        assertEquals(2, adicionados.size());
        Arquivo arquivoFoto = adicionados.get(0);
        assertEquals("Festa da Atletica 2025", arquivoFoto.getNome());
        assertEquals(TipoArquivo.IMAGEM, arquivoFoto.getTipo());
        assertTrue(arquivoFoto.getCaminho().endsWith("-festa-da-atletica-2025.jpg"), arquivoFoto.getCaminho());
        assertTrue(Files.exists(ArmazenamentoMidia.resolver(arquivoFoto.getCaminho())));
        assertEquals(TipoArquivo.VIDEO, adicionados.get(1).getTipo());
        assertEquals(2, controller.listarArquivos(festas).size());
        assertEquals(2, pasta("Festas").getQuantidadeArquivos());
    }

    @Test
    void recusaTiposNaoAceitosSemCopiarNada() throws Exception {
        Path foto = Files.write(origem.resolve("foto.png"), new byte[10]);
        Path texto = Files.write(origem.resolve("ata.txt"), new byte[10]);

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controller.adicionarArquivos(pasta("Festas"), Arrays.asList(foto, texto)));

        assertTrue(erro.getMessage().contains("ata.txt"), erro.getMessage());
        assertTrue(controller.listarArquivos(pasta("Festas")).isEmpty());
        assertFalse(Files.exists(temporaria.resolve("midias")));
    }

    @Test
    void renomeiaEMoveArquivoDePasta() throws Exception {
        Arquivo arquivo = controller.adicionarArquivos(pasta("Festas"),
                Arrays.asList(Files.write(origem.resolve("acao.jpg"), new byte[10]))).get(0);

        arquivo.setNome("Ação no asilo");
        arquivo.setPasta(pasta("Ações Sociais"));
        controller.salvarArquivo(arquivo);

        assertTrue(controller.listarArquivos(pasta("Festas")).isEmpty());
        assertEquals("Ação no asilo", controller.listarArquivos(pasta("Ações Sociais")).get(0).getNome());
    }

    @Test
    void excluiArquivoDoBancoEDoDiscoESoExcluiPastaVazia() throws Exception {
        Pasta festas = pasta("Festas");
        Arquivo arquivo = controller.adicionarArquivos(festas,
                Arrays.asList(Files.write(origem.resolve("foto.jpg"), new byte[10]))).get(0);
        Path noDisco = ArmazenamentoMidia.resolver(arquivo.getCaminho());

        assertThrows(RegraNegocioException.class, () -> controller.excluirPastaVazia(festas));
        controller.excluirDoBancoEDoDisco(arquivo);
        assertFalse(Files.exists(noDisco));
        controller.excluirPastaVazia(festas);

        assertEquals(4, controller.listarPastas().size());
    }

    private Pasta pasta(String nome) throws Exception {
        for (Pasta pasta : controller.listarPastas()) {
            if (pasta.getNome().equals(nome)) {
                return pasta;
            }
        }
        throw new AssertionError("Pasta não encontrada: " + nome);
    }
}
