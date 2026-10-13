package br.com.athletiza.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AcessoTest {

    private final Cargo presidente = cargo("Presidente", Setor.PRESIDENCIA, null);
    private final Cargo vice = cargo("Vice-presidente", Setor.PRESIDENCIA, presidente);
    private final Cargo diretorEsportes = cargo("Diretor de Esportes", Setor.ESPORTES, vice);
    private final Cargo diretorEventos = cargo("Diretor de Eventos", Setor.EVENTOS, vice);
    private final Cargo coordenador = cargo("Coordenador de Modalidade", Setor.ESPORTES, diretorEsportes);
    private final Cargo semSetor = cargo("Conselheiro", null, vice);

    private final Evento festa = evento(TipoEvento.FESTA);
    private final Evento desafio = evento(TipoEvento.COMPETICAO);
    private final Competicao jiudesc = new Competicao("JIUDESC", LocalDate.of(2026, 10, 30), null, null, null);
    private final Compromisso reuniao = new Compromisso("Reunião", LocalDate.of(2026, 10, 8), null, null);

    @Test
    void perfilVemDoSetorEDaHierarquiaDoCargo() {
        assertEquals("Presidência", acesso(presidente).getDescricao());
        assertEquals("Presidência", acesso(vice).getDescricao());
        assertEquals("Diretor · Esportes", acesso(diretorEsportes).getDescricao());
        assertEquals("Assessor · Esportes", acesso(coordenador).getDescricao());
        assertTrue(Acesso.pelosCargos(List.of(semSetor)).isEmpty());
        assertTrue(Acesso.pelosCargos(List.of()).isEmpty());
        // com mais de um cargo, vale o maior perfil
        assertEquals(Perfil.DIRETOR, Acesso.pelosCargos(List.of(coordenador, diretorEventos)).orElseThrow().getPerfil());
    }

    @Test
    void presidenciaAlteraTudoMenosUsuarios() {
        Acesso presidencia = acesso(vice);

        assertTrue(presidencia.podeAlterar(Modulo.GESTAO));
        assertTrue(presidencia.podeAlterar(reuniao));
        assertEquals(List.of(TipoEvento.values()), presidencia.tiposDeEvento());
        assertFalse(presidencia.veModulo(Modulo.USUARIOS));
        assertFalse(presidencia.podeGerenciarUsuarios());
        assertTrue(Acesso.administrador().podeGerenciarUsuarios());
    }

    @Test
    void diretorConsultaTudoMasSoAlteraASuaArea() {
        Acesso esportes = acesso(diretorEsportes);

        assertTrue(esportes.veModulo(Modulo.GESTAO));
        assertTrue(esportes.veModulo(Modulo.REGISTROS));
        assertFalse(esportes.podeAlterar(Modulo.GESTAO));
        assertTrue(esportes.podeAlterar(Modulo.ATLETAS));
        assertTrue(esportes.podeAlterar(Modulo.TREINOS));
        assertTrue(esportes.podeAlterar(jiudesc));
        assertFalse(esportes.podeAlterar(reuniao));
    }

    @Test
    void cadaDiretorIncluiOsEventosDoTipoDaSuaArea() {
        Acesso esportes = acesso(diretorEsportes);
        Acesso eventos = acesso(diretorEventos);

        assertEquals(List.of(TipoEvento.COMPETICAO), esportes.tiposDeEvento());
        assertEquals(List.of(TipoEvento.FESTA, TipoEvento.OUTRO), eventos.tiposDeEvento());
        assertTrue(esportes.podeAlterar(desafio));
        assertFalse(esportes.podeAlterar(festa));
        assertTrue(eventos.podeAlterar(festa));
        assertFalse(eventos.podeAlterar(desafio));
        assertFalse(eventos.podeAlterar(Modulo.ATLETAS));
        assertTrue(eventos.podeAlterar(Modulo.EVENTOS));
    }

    @Test
    void diretorSoAlteraDemandasLigadasASuaArea() {
        Acesso eventos = acesso(diretorEventos);
        Demanda daFesta = new Demanda("Comprar decoração", PrioridadeDemanda.MEDIA, null);
        daFesta.setRelacionada(festa);
        Demanda avulsa = new Demanda("Renovar camisetas", PrioridadeDemanda.MEDIA, null);

        assertTrue(eventos.podeAlterar(Modulo.DEMANDAS));
        assertTrue(eventos.podeAlterar(daFesta));
        assertFalse(eventos.podeAlterar(avulsa));
        assertTrue(acesso(presidente).podeAlterar(avulsa));
    }

    @Test
    void assessorSoConsultaOSeuSetor() {
        Acesso assessor = acesso(coordenador);

        assertTrue(assessor.veModulo(Modulo.INICIO));
        assertTrue(assessor.veModulo(Modulo.ATLETAS));
        assertTrue(assessor.veModulo(Modulo.EVENTOS));
        assertFalse(assessor.veModulo(Modulo.GESTAO));
        assertFalse(assessor.veModulo(Modulo.DEMANDAS));
        assertFalse(assessor.podeAlterar(Modulo.ATLETAS));
        assertFalse(assessor.podeAlterar(jiudesc));
        assertTrue(assessor.tiposDeEvento().isEmpty());
        assertTrue(assessor.ve(desafio));
        assertFalse(assessor.ve(festa));
    }

    private static Acesso acesso(Cargo cargo) {
        return Acesso.pelosCargos(List.of(cargo)).orElseThrow();
    }

    private static Cargo cargo(String nome, Setor setor, Cargo superior) {
        Cargo cargo = new Cargo(nome, 1, superior);
        cargo.setSetor(setor);
        return cargo;
    }

    private static Evento evento(TipoEvento tipo) {
        return new Evento(tipo.getDescricao(), tipo, LocalDate.of(2026, 10, 12), null, null);
    }
}
