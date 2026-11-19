package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.AlertaDAO;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Alerta;
import br.com.athletiza.modelo.Amistoso;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Compromisso;
import br.com.athletiza.modelo.Treino;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarioControladorTest {
    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private final CalendarioControlador controlador = new CalendarioControlador(new AtividadeDAO(), new CompromissoDAO(), new AlertaDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    @Test
    void agrupaAtividadesDoMesPorDiaEmOrdemDeHorario() throws Exception {
        Map<LocalDate, List<Atividade>> mes = controlador.atividadesDoMes(OUTUBRO);

        List<Atividade> dia8 = mes.get(LocalDate.of(2026, 10, 8));
        assertEquals(2, dia8.size());
        assertInstanceOf(Compromisso.class, dia8.get(0), "Reunião às 14h vem antes do amistoso às 20h");
        assertInstanceOf(Amistoso.class, dia8.get(1));
        assertEquals("Amistoso", dia8.get(1).getRotuloCurto());
        assertFalse(mes.containsKey(LocalDate.of(2026, 10, 2)));
    }

    @Test
    void competicaoDeVariosDiasApareceEmTodosOsDiasDoMes() throws Exception {
        Map<LocalDate, List<Atividade>> outubro = controlador.atividadesDoMes(OUTUBRO);
        Map<LocalDate, List<Atividade>> novembro = controlador.atividadesDoMes(YearMonth.of(2026, 11));

        assertTrue(outubro.get(LocalDate.of(2026, 10, 31)).stream().anyMatch(a -> a instanceof Competicao));
        assertTrue(novembro.get(LocalDate.of(2026, 11, 2)).stream().anyMatch(a -> a.getTitulo().equals("JIUDESC 2026")));
        assertFalse(novembro.containsKey(LocalDate.of(2026, 11, 3)));
    }

    @Test
    void listaProximasAtividadesOrdenadas() throws Exception {
        List<Atividade> proximas = controlador.proximasAtividades(LocalDate.of(2026, 10, 7), 7);

        assertEquals(List.of("Reunião Gestão", "Amistoso Futsal (F) x Atlética Furiosa", "Treino Futsal (F)",
                "Festa Atlética"), proximas.stream().map(Atividade::getTitulo).toList());
    }

    @Test
    void incluiEditaEExcluiCompromisso() throws Exception {
        Compromisso reuniao = new Compromisso("Reunião com patrocinador", LocalDate.of(2026, 10, 20),
                LocalTime.of(18, 30), "Sala da atlética");

        controlador.salvarCompromisso(reuniao);
        reuniao.setTitulo("Reunião com patrocinadores");
        controlador.salvarCompromisso(reuniao);

        List<Atividade> dia20 = controlador.atividadesDoMes(OUTUBRO).get(LocalDate.of(2026, 10, 20));
        assertEquals(List.of("Reunião com patrocinadores"), dia20.stream().map(Atividade::getTitulo).toList());

        controlador.excluirCompromisso(reuniao);
        assertFalse(controlador.atividadesDoMes(OUTUBRO).containsKey(LocalDate.of(2026, 10, 20)));
    }

    @Test
    void agrupaAtividadesDeUmaSemana() throws Exception {
        Map<LocalDate, List<Atividade>> semana = controlador.atividadesDoPeriodo(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 11));

        assertEquals(List.of(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)),
                List.copyOf(semana.keySet()));
    }

    @Test
    void alertasMostramTarefasAtrasadasTreinosSemPresencaECompeticoesProximas() throws Exception {
        List<Alerta> alertas = controlador.alertas(LocalDate.of(2026, 10, 7));

        assertEquals(List.of(Alerta.Tipo.TAREFA_ATRASADA, Alerta.Tipo.PRESENCA_PENDENTE, Alerta.Tipo.PRESENCA_PENDENTE),
                alertas.stream().map(Alerta::tipo).toList());
        assertTrue(alertas.get(0).texto().contains("Contratar DJ (Festa Atlética, venceu em 05/10/2026)"),
                alertas.get(0).texto());
        assertTrue(alertas.get(1).texto().contains("de 03/10/2026"), alertas.get(1).texto());

        List<Alerta> semanaDaJiudesc = controlador.alertas(LocalDate.of(2026, 10, 25));
        assertTrue(semanaDaJiudesc.stream().anyMatch(a -> a.texto().equals("JIUDESC 2026 começa em 5 dias (30/10/2026)")),
                semanaDaJiudesc.toString());
    }

    @Test
    void carregaAtividadeCompletaParaEdicao() throws Exception {
        Atividade resumo = controlador.atividadesDoMes(OUTUBRO).get(LocalDate.of(2026, 10, 6)).get(0);

        Atividade completa = controlador.carregarCompleta(resumo);

        assertInstanceOf(Treino.class, completa);
        assertEquals("Juliana Martins", ((Treino) completa).getResponsavel().getNome());
    }

    @Test
    void compromissoExigeTituloEData() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controlador.salvarCompromisso(new Compromisso(" ", null, null, null)));

        assertEquals(2, erro.getErros().size());
    }
}
