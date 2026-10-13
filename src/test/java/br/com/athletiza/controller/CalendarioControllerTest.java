package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.Dados;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.AlertaDAO;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Alerta;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Treino;
import br.com.athletiza.view.calendario.PainelCalendario;
import br.com.athletiza.view.calendario.PainelCompromisso;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarioControllerTest {

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private final NavegadorFalso navegador = new NavegadorFalso();
    private CalendarioController controller;

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        controller = new CalendarioController(new PainelCalendario(), new AtividadeDAO(), new AlertaDAO(),
                new CompromissoDAO(), new TreinoDAO(), new CompeticaoDAO(), new EventoDAO(), new ModalidadeDAO(),
                new MembroDAO(), navegador);
    }

    private static CompromissoController compromisso(Compromisso compromisso) {
        return new CompromissoController(new PainelCompromisso(), compromisso, new CompromissoDAO(), new NavegadorFalso());
    }

    @Test
    void agrupaAtividadesDoMesPorDiaEmOrdemDeHorario() throws Exception {
        Map<LocalDate, List<Atividade>> mes = controller.atividadesDoMes(OUTUBRO);

        List<Atividade> dia8 = mes.get(LocalDate.of(2026, 10, 8));
        assertEquals(2, dia8.size());
        assertInstanceOf(Compromisso.class, dia8.get(0), "Reunião às 14h vem antes do amistoso às 20h");
        assertInstanceOf(Amistoso.class, dia8.get(1));
        assertEquals("Amistoso", dia8.get(1).getRotuloCurto());
        assertFalse(mes.containsKey(LocalDate.of(2026, 10, 2)));
    }

    @Test
    void competicaoDeVariosDiasApareceEmTodosOsDiasDoMes() throws Exception {
        Map<LocalDate, List<Atividade>> outubro = controller.atividadesDoMes(OUTUBRO);
        Map<LocalDate, List<Atividade>> novembro = controller.atividadesDoMes(YearMonth.of(2026, 11));

        assertTrue(titulos(outubro.get(LocalDate.of(2026, 10, 31))).contains("JIUDESC 2026"));
        assertInstanceOf(Competicao.class, novembro.get(LocalDate.of(2026, 11, 2)).get(0));
        assertFalse(novembro.containsKey(LocalDate.of(2026, 11, 3)));
    }

    @Test
    void listaProximasAtividadesOrdenadas() throws Exception {
        List<Atividade> proximas = controller.proximasAtividades(LocalDate.of(2026, 10, 7), 7);

        assertEquals(Arrays.asList("Reunião Gestão", "Amistoso Futsal (F) x Atlética Furiosa", "Treino Futsal (F)",
                "Festa Atlética"), titulos(proximas));
    }

    @Test
    void incluiEditaEExcluiCompromisso() throws Exception {
        Compromisso reuniao = new Compromisso("Reunião com patrocinador", LocalDate.of(2026, 10, 20),
                LocalTime.of(18, 30), "Sala da atlética");

        compromisso(reuniao).gravar();
        reuniao.setTitulo("Reunião com patrocinadores");
        compromisso(reuniao).gravar();

        List<Atividade> dia20 = controller.atividadesDoMes(OUTUBRO).get(LocalDate.of(2026, 10, 20));
        assertEquals(Arrays.asList("Reunião com patrocinadores"), titulos(dia20));

        new CompromissoDAO().excluir(reuniao.getId());
        assertFalse(controller.atividadesDoMes(OUTUBRO).containsKey(LocalDate.of(2026, 10, 20)));
    }

    @Test
    void agrupaAtividadesDeUmaSemana() throws Exception {
        Map<LocalDate, List<Atividade>> semana = controller.atividadesDoPeriodo(LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 11));

        assertEquals(Arrays.asList(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)),
                new ArrayList<>(semana.keySet()));
    }

    @Test
    void alertasMostramTarefasAtrasadasTreinosSemPresencaECompeticoesProximas() throws Exception {
        List<Alerta> alertas = controller.alertas(LocalDate.of(2026, 10, 7));

        assertEquals(Arrays.asList(Alerta.Tipo.TAREFA_ATRASADA, Alerta.Tipo.PRESENCA_PENDENTE,
                Alerta.Tipo.PRESENCA_PENDENTE), Arrays.asList(alertas.get(0).getTipo(), alertas.get(1).getTipo(),
                alertas.get(2).getTipo()));
        assertEquals(3, alertas.size());
        assertTrue(alertas.get(0).getTexto().contains("Contratar DJ (Festa Atlética, venceu em 05/10/2026)"),
                alertas.get(0).getTexto());
        assertTrue(alertas.get(1).getTexto().contains("de 03/10/2026"), alertas.get(1).getTexto());

        List<String> semanaDaJiudesc = Dados.textos(controller.alertas(LocalDate.of(2026, 10, 25)), a -> a.getTexto());
        assertTrue(semanaDaJiudesc.contains("JIUDESC 2026 começa em 5 dias (30/10/2026)"), semanaDaJiudesc.toString());
    }

    @Test
    void carregaAtividadeCompletaParaEdicao() throws Exception {
        Atividade resumo = controller.atividadesDoMes(OUTUBRO).get(LocalDate.of(2026, 10, 6)).get(0);

        Atividade completa = controller.carregarCompleta(resumo);

        assertInstanceOf(Treino.class, completa);
        assertEquals("Juliana Martins", ((Treino) completa).getResponsavel().getNome());
    }

    @Test
    void compromissoExigeTituloEData() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> compromisso(new Compromisso(" ", null, null, null)).gravar());

        assertEquals(2, erro.getErros().size());
    }

    private static List<String> titulos(List<Atividade> atividades) {
        return Dados.textos(atividades, a -> a.getTitulo());
    }
}
