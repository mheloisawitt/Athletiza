package br.com.athletiza.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.exception.RegraNegocioException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModeloTest {

    private final Modalidade futsalF = new Modalidade("Futsal", Genero.FEMININO);
    private final Modalidade voleiF = new Modalidade("Vôlei", Genero.FEMININO);

    @Test
    void atletasOrdenadosPorNomeRespeitandoAcentos() {
        List<Atleta> atletas = new ArrayList<>(List.of(
                new Atleta("Beatriz", "2", null), new Atleta("álvaro", "3", null), new Atleta("Ana", "1", null)));

        Collections.sort(atletas);

        assertEquals(List.of("álvaro", "Ana", "Beatriz"), atletas.stream().map(Pessoa::getNome).toList());
    }

    @Test
    void atletasOrdenadosPorModalidade() {
        Atleta ana = new Atleta("Ana", "1", null);
        ana.vincularModalidade(voleiF, SituacaoAtleta.ATIVO);
        Atleta bia = new Atleta("Bia", "2", null);
        bia.vincularModalidade(futsalF, SituacaoAtleta.ATIVO);
        Atleta semModalidade = new Atleta("Carla", "3", null);

        List<Atleta> atletas = new ArrayList<>(List.of(semModalidade, ana, bia));
        atletas.sort(Atleta.POR_MODALIDADE);

        assertEquals(List.of(bia, ana, semModalidade), atletas);
    }

    @Test
    void pessoasComMesmaMatriculaSaoIguais() {
        assertEquals(new Atleta("Ana", "202001", null), new Atleta("Ana Souza", "202001", "x"));
        assertFalse(new Atleta("Ana", "202001", null).equals(new Membro("Ana", "202001", null)));
    }

    @Test
    void detectaSobreposicaoDeGestoes() {
        Gestao g2025 = new Gestao("2025-2026", LocalDate.of(2025, 3, 1), LocalDate.of(2026, 2, 28));
        Gestao g2026 = new Gestao("2026-2027", LocalDate.of(2026, 2, 1), LocalDate.of(2027, 2, 28));
        Gestao g2027 = new Gestao("2027-2028", LocalDate.of(2027, 3, 1), LocalDate.of(2028, 2, 28));

        assertTrue(g2025.sobrepoe(g2026));
        assertFalse(g2025.sobrepoe(g2027));
        assertEquals("2025 - 2026", g2025.getPeriodo());
    }

    @Test
    void gestaoNaoAceitaMesmoMembroDuasVezesNoMesmoCargoEMontaOrganograma() {
        Cargo presidente = new Cargo("Presidente", 1, null);
        Cargo tesoureiro = new Cargo("Tesoureiro", 3, presidente);
        Membro mariana = new Membro("Mariana", "1", null);
        Membro paula = new Membro("Paula", "2", null);
        Gestao gestao = new Gestao("Gestão", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertTrue(gestao.adicionarMembro(paula, tesoureiro));
        assertTrue(gestao.adicionarMembro(mariana, presidente));
        assertFalse(gestao.adicionarMembro(new Membro("Mariana", "1", null), presidente));

        Map<Cargo, List<Membro>> organograma = gestao.getOrganograma();
        assertEquals(List.of(presidente, tesoureiro), new ArrayList<>(organograma.keySet()));
        assertEquals(List.of(mariana), organograma.get(presidente));
    }

    @Test
    void competicaoAceitaSoAtletasDaModalidadeESemRepeticao() throws RegraNegocioException {
        Atleta ana = new Atleta("Ana", "1", null);
        ana.vincularModalidade(futsalF, SituacaoAtleta.ATIVO);
        Competicao jiudesc = new Competicao("JIUDESC", LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2), null, "Blumenau");

        assertThrows(RegraNegocioException.class, () -> jiudesc.inscreverAtleta(futsalF, ana));

        jiudesc.adicionarModalidade(futsalF);
        jiudesc.adicionarModalidade(voleiF);
        assertTrue(jiudesc.inscreverAtleta(futsalF, ana));
        assertFalse(jiudesc.inscreverAtleta(futsalF, ana));
        assertThrows(RegraNegocioException.class, () -> jiudesc.inscreverAtleta(voleiF, ana));
        assertEquals(1, jiudesc.getAtletas(futsalF).size());
    }

    @Test
    void atividadesSaoTratadasDeFormaPolimorficaNoCalendario() {
        LocalDate dia31 = LocalDate.of(2026, 10, 31);
        List<Atividade> atividades = new ArrayList<>(List.of(
                new Competicao("JIUDESC", LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2), null, "Blumenau"),
                new Evento("Festa", TipoEvento.FESTA, dia31, LocalTime.of(22, 0), "Ibirama"),
                new Treino(futsalF, dia31, LocalTime.of(19, 0), "Quadra 1"),
                new Compromisso("Reunião", LocalDate.of(2026, 10, 8), LocalTime.of(14, 0), "Sala")));

        List<Atividade> doDia = atividades.stream().filter(a -> a.ocorreEm(dia31)).sorted().toList();

        assertEquals(3, doDia.size());
        assertEquals(TipoAtividade.TREINO, doDia.get(1).getTipo());
        assertEquals("Festa - Festa", doDia.get(2).getDescricaoCalendario());
        assertEquals(TipoAtividade.COMPETICAO.getCorHex(), doDia.get(0).getCorHex());
    }

    @Test
    void amistosoEhUmTreinoComTipoProprio() {
        Treino amistoso = new Amistoso(futsalF, "Atlética Furiosa", LocalDate.of(2026, 10, 8), LocalTime.of(20, 0), "Quadra 1");

        assertEquals(TipoAtividade.AMISTOSO, amistoso.getTipo());
        assertEquals("Amistoso Futsal (F) x Atlética Furiosa", amistoso.getTitulo());
    }

    @Test
    void identificaTarefasAtrasadas() {
        LocalDate hoje = LocalDate.of(2026, 10, 10);
        Evento festa = new Evento("Festa", TipoEvento.FESTA, LocalDate.of(2026, 10, 12), null, "Ibirama");
        Tarefa atrasada = new Tarefa("Contratar DJ", null, LocalDate.of(2026, 10, 5));
        Tarefa concluida = new Tarefa("Reservar espaço", null, LocalDate.of(2026, 9, 30));
        concluida.setSituacao(SituacaoTarefa.CONCLUIDA);
        festa.adicionarTarefa(atrasada);
        festa.adicionarTarefa(concluida);
        festa.adicionarTarefa(new Tarefa("Vender ingressos", null, LocalDate.of(2026, 10, 11)));

        assertEquals(List.of(atrasada), festa.getTarefasAtrasadas(hoje));
    }
}
