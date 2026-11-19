package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Indicador;
import br.com.athletiza.modelo.Setor;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DashboardRepositorio {
    record DemandaAberta(int id, LocalDate prazo, Setor setorDaAtividade) {
    }

    record RegistroPresenca(LocalDate data, boolean presente) {
    }

    record Podio(String modalidade, int colocacao) {
    }

    List<Indicador> atletasPorModalidade() throws PersistenciaException;

    List<Indicador> presencasPorModalidade(LocalDate inicio, LocalDate fim) throws PersistenciaException;

    List<Indicador> treinosPorModalidade(LocalDate inicio, LocalDate fim) throws PersistenciaException;

    int percentualPresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException;

    int contarAtletasAtivos() throws PersistenciaException;

    List<DemandaAberta> demandasAbertas() throws PersistenciaException;

    Map<Integer, List<Integer>> responsaveisDasDemandas() throws PersistenciaException;

    Map<Integer, Set<Setor>> setoresDosMembros() throws PersistenciaException;

    List<RegistroPresenca> registrosDePresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException;

    List<Indicador> atletasPorSituacao() throws PersistenciaException;

    List<Indicador> eventosPorTipo(LocalDate inicio) throws PersistenciaException;

    List<Indicador> demandasPorSituacao() throws PersistenciaException;

    List<Indicador> demandasAbertasPorPrioridade() throws PersistenciaException;

    List<Podio> podios(LocalDate inicio, LocalDate fim) throws PersistenciaException;
}
