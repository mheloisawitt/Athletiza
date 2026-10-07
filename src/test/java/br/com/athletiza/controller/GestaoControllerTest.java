package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoGestao;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GestaoControllerTest {

    private final GestaoController controller = new GestaoController();
    private final CargoController cargos = new CargoController();
    private final MembroController membros = new MembroController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void listaGestoesPorPeriodo() throws Exception {
        assertEquals(List.of("Gestão 2020-2021", "Gestão 2022-2023", "Gestão 2023-2024", "Gestão 2025-2026"),
                controller.listar().stream().map(Gestao::getNome).toList());
    }

    @Test
    void naoPermitePeriodoSobreposto() {
        Gestao nova = gestao("Gestão 2026-2027", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 5, 31));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> controller.salvar(nova));

        assertEquals("O período informado se sobrepõe à Gestão 2025-2026 (01/03/2025 a 31/12/2026).", erro.getMessage());
    }

    @Test
    void validaDatasDaGestao() {
        Gestao invalida = gestao(" ", LocalDate.of(2028, 1, 1), LocalDate.of(2027, 1, 1));

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> controller.salvar(invalida));

        assertEquals(2, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiEditaGestaoSemSobreposicao() throws Exception {
        Gestao nova = gestao("Gestão 2027-2028", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));
        controller.salvar(nova);

        nova.setDataFim(LocalDate.of(2028, 2, 28));
        controller.salvar(nova);

        assertEquals(LocalDate.of(2028, 2, 28), controller.listar().get(4).getDataFim());
    }

    @Test
    void carregaComposicaoEMontaOrganogramaPorNivel() throws Exception {
        Gestao atual = atual();

        Map<Cargo, List<Membro>> organograma = atual.getOrganograma();

        assertEquals(5, atual.getComposicao().size());
        Cargo primeiro = organograma.keySet().iterator().next();
        assertEquals("Presidente", primeiro.getNome());
        assertEquals("Mariana Rocha", organograma.get(primeiro).get(0).getNome());
        Cargo vice = organograma.keySet().stream().filter(c -> c.getNome().equals("Vice-presidente")).findFirst().orElseThrow();
        assertEquals(primeiro, vice.getCargoSuperior());
    }

    @Test
    void adicionaTrocaERemoveMembroGravandoNoBanco() throws Exception {
        Gestao atual = atual();
        Cargo secretario = cargo("Secretário");
        Membro paula = membro("Paula Ribeiro");
        Membro lucas = membro("Lucas Pereira");

        controller.adicionarMembro(atual, paula, secretario);
        assertThrows(RegraNegocioException.class, () -> controller.adicionarMembro(atual, paula, secretario));
        assertEquals(6, atual().getComposicao().size());

        controller.trocarMembro(atual, secretario, paula, lucas);
        assertTrue(atual().getOrganograma().get(secretario).contains(lucas));

        controller.removerMembro(atual, lucas, secretario);
        assertEquals(5, atual().getComposicao().size());
    }

    @Test
    void excluiGestaoJuntoComAComposicao() throws Exception {
        Gestao atual = atual();
        assertEquals(5, controller.contarMembros(atual));

        controller.excluir(atual);

        assertEquals(3, controller.listar().size());
    }

    @Test
    void cargoSuperiorPrecisaTerNivelMenorENaoPodeExcluirCargoOcupado() throws Exception {
        Cargo presidente = cargo("Presidente");
        Cargo novo = new Cargo("Conselheiro", 1, presidente);

        assertThrows(RegraNegocioException.class, () -> cargos.salvar(novo));
        novo.setOrdem(2);
        cargos.salvar(novo);
        assertThrows(RegraNegocioException.class, () -> cargos.excluir(presidente));
        cargos.excluir(novo);
    }

    @Test
    void membroComCargoNaoPodeSerExcluidoEMatriculaEhUnica() throws Exception {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> membros.excluir(membro("Mariana Rocha")));
        assertTrue(erro.getMessage().contains("1 cargo(s) em gestões"), erro.getMessage());

        Membro repetido = new Membro("Outra", "201801", null);
        assertThrows(RegraNegocioException.class, () -> membros.salvar(repetido));
    }

    private Gestao atual() throws Exception {
        Gestao atual = controller.listar().get(3);
        controller.carregarComposicao(atual);
        return atual;
    }

    private Cargo cargo(String nome) throws Exception {
        return cargos.listar().stream().filter(c -> c.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private Membro membro(String nome) throws Exception {
        return membros.listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static Gestao gestao(String nome, LocalDate inicio, LocalDate fim) {
        Gestao gestao = new Gestao(nome, inicio, fim);
        gestao.setSituacao(SituacaoGestao.PLANEJADA);
        return gestao;
    }
}
