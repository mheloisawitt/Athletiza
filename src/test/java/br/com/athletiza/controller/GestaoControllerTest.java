package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.GestaoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.view.gestao.PainelCadastroCargo;
import br.com.athletiza.view.gestao.PainelCadastroGestao;
import br.com.athletiza.view.gestao.PainelCadastroMembro;
import br.com.athletiza.view.gestao.PainelConsultaCargos;
import br.com.athletiza.view.gestao.PainelConsultaGestoes;
import br.com.athletiza.view.gestao.PainelConsultaMembros;
import br.com.athletiza.view.gestao.PainelGerenciarGestao;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GestaoControllerTest {

    private final NavegadorFalso navegador = new NavegadorFalso();
    private final GestaoDAO dao = new GestaoDAO();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    private CadastroGestaoController cadastro(Gestao gestao) {
        return new CadastroGestaoController(new PainelCadastroGestao(), gestao, dao, navegador);
    }

    private List<Gestao> gestoes() throws Exception {
        List<Gestao> gestoes = dao.listarTodos();
        Collections.sort(gestoes);
        return gestoes;
    }

    @Test
    void listaGestoesPorPeriodo() throws Exception {
        assertEquals(Arrays.asList("Gestão 2020-2021", "Gestão 2022-2023", "Gestão 2023-2024", "Gestão 2025-2026"),
                Dados.textos(gestoes(), g -> g.getNome()));
    }

    @Test
    void naoPermitePeriodoSobreposto() {
        Gestao nova = gestao("Gestão 2026-2027", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 5, 31));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> cadastro(nova).gravar());

        assertEquals("O período informado se sobrepõe à Gestão 2025-2026 (01/03/2025 a 31/12/2026).", erro.getMessage());
    }

    @Test
    void validaDatasDaGestao() {
        Gestao invalida = gestao(" ", LocalDate.of(2028, 1, 1), LocalDate.of(2027, 1, 1));

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> cadastro(invalida).gravar());

        assertEquals(2, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void incluiEditaGestaoSemSobreposicao() throws Exception {
        Gestao nova = gestao("Gestão 2027-2028", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));
        cadastro(nova).gravar();

        nova.setDataFim(LocalDate.of(2028, 2, 28));
        cadastro(nova).gravar();

        assertEquals(LocalDate.of(2028, 2, 28), gestoes().get(4).getDataFim());
    }

    @Test
    void carregaComposicaoEMontaOrganogramaPorNivel() throws Exception {
        Gestao atual = atual();

        Map<Cargo, List<Membro>> organograma = atual.getOrganograma();

        assertEquals(5, atual.getComposicao().size());
        Cargo primeiro = organograma.keySet().iterator().next();
        assertEquals("Presidente", primeiro.getNome());
        assertEquals("Mariana Rocha", organograma.get(primeiro).get(0).getNome());
        Cargo vice = null;
        for (Cargo cargo : organograma.keySet()) {
            if (cargo.getNome().equals("Vice-presidente")) {
                vice = cargo;
            }
        }
        assertEquals(primeiro, vice.getCargoSuperior());
    }

    @Test
    void adicionaTrocaERemoveMembroGravandoNoBanco() throws Exception {
        GerenciarGestaoController gerenciar = new GerenciarGestaoController(new PainelGerenciarGestao(), gestoes().get(3),
                dao, new CargoDAO(), new MembroDAO(), navegador);
        gerenciar.carregar();
        Cargo secretario = cargo("Secretário");
        Membro paula = Dados.membro("Paula Ribeiro");
        Membro lucas = Dados.membro("Lucas Pereira");

        gerenciar.adicionarMembro(paula, secretario);
        assertThrows(RegraNegocioException.class, () -> gerenciar.adicionarMembro(paula, secretario));
        assertEquals(6, atual().getComposicao().size());

        gerenciar.trocarMembro(secretario, paula, lucas);
        assertTrue(atual().getOrganograma().get(secretario).contains(lucas));

        dao.removerMembro(gerenciar.getModel(), lucas, secretario);
        assertEquals(5, atual().getComposicao().size());
    }

    @Test
    void excluiGestaoJuntoComAComposicao() throws Exception {
        ConsultaGestoesController consulta = new ConsultaGestoesController(new PainelConsultaGestoes(), dao,
                new CargoDAO(), new MembroDAO(), navegador);
        Gestao atual = atual();
        assertEquals(5, consulta.contarMembros(atual));

        dao.excluir(atual.getId());

        assertEquals(3, gestoes().size());
    }

    @Test
    void cargoSuperiorPrecisaTerNivelMenorENaoPodeExcluirCargoOcupado() throws Exception {
        Cargo presidente = cargo("Presidente");
        Cargo novo = new Cargo("Conselheiro", 1, presidente);
        ConsultaCargosController cargos = new ConsultaCargosController(new PainelConsultaCargos(), new CargoDAO(), navegador);

        assertThrows(RegraNegocioException.class, () -> cadastroCargo(novo).gravar());
        novo.setOrdem(2);
        cadastroCargo(novo).gravar();
        assertThrows(RegraNegocioException.class, () -> cargos.excluir(presidente));
        cargos.excluir(novo);
    }

    @Test
    void membroComCargoNaoPodeSerExcluidoEMatriculaEhUnica() throws Exception {
        ConsultaMembrosController membros = new ConsultaMembrosController(new PainelConsultaMembros(), new MembroDAO(),
                navegador);
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> membros.excluir(Dados.membro("Mariana Rocha")));
        assertTrue(erro.getMessage().contains("1 cargo(s) em gestões"), erro.getMessage());

        Membro repetido = new Membro("Outra", "201801", null);
        CadastroMembroController cadastro = new CadastroMembroController(new PainelCadastroMembro(), repetido,
                new MembroDAO(), navegador);
        assertThrows(RegraNegocioException.class, () -> cadastro.gravar());
    }

    private CadastroCargoController cadastroCargo(Cargo cargo) {
        return new CadastroCargoController(new PainelCadastroCargo(), cargo, new CargoDAO(), navegador);
    }

    private Gestao atual() throws Exception {
        Gestao atual = gestoes().get(3);
        dao.carregarComposicao(atual);
        return atual;
    }

    private static Cargo cargo(String nome) throws Exception {
        for (Cargo cargo : new CargoDAO().listarTodos()) {
            if (cargo.getNome().equals(nome)) {
                return cargo;
            }
        }
        throw new AssertionError("Cargo não encontrado: " + nome);
    }

    private static Gestao gestao(String nome, LocalDate inicio, LocalDate fim) {
        Gestao gestao = new Gestao(nome, inicio, fim);
        gestao.setSituacao(SituacaoGestao.PLANEJADA);
        return gestao;
    }
}
