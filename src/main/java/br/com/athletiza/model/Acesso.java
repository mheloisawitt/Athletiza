package br.com.athletiza.model;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * O que o usuário logado pode ver e alterar no sistema (RN19).
 *
 * - Administrador: tudo, inclusive usuários.
 * - Presidência (Presidente ou Vice): inclui, edita e exclui em todos os módulos, menos usuários.
 * - Diretor: consulta todos os módulos (o calendário com todas as informações), mas só inclui e
 *   edita o que é da área da sua diretoria: Esportes cuida de atletas, treinos, competições e
 *   eventos de competição; cada diretoria cuida dos eventos do seu tipo (festas, ações sociais...);
 *   demandas, só as relacionadas a um evento ou competição da sua área.
 * - Assessor: somente consulta, e só dos módulos do setor que assessora.
 */
public final class Acesso {

    private static final Acesso TOTAL = new Acesso(Perfil.ADMINISTRADOR, EnumSet.noneOf(Setor.class));

    private final Perfil perfil;
    private final Set<Setor> setores;

    private Acesso(Perfil perfil, Set<Setor> setores) {
        this.perfil = perfil;
        this.setores = Collections.unmodifiableSet(setores);
    }

    public static Acesso administrador() {
        return TOTAL;
    }

    /**
     * Acesso de quem ocupa esses cargos na gestão em andamento. Se tiver mais de um cargo,
     * vale o maior perfil (presidência, depois diretor, depois assessor).
     *
     * @return vazio se nenhum cargo dá acesso ao sistema
     */
    public static Optional<Acesso> pelosCargos(Collection<Cargo> cargos) {
        if (cargos.stream().anyMatch(c -> c.getSetor() == Setor.PRESIDENCIA)) {
            return Optional.of(new Acesso(Perfil.PRESIDENCIA, EnumSet.of(Setor.PRESIDENCIA)));
        }
        Set<Setor> diretorias = setores(cargos, false);
        if (!diretorias.isEmpty()) {
            return Optional.of(new Acesso(Perfil.DIRETOR, diretorias));
        }
        Set<Setor> assessorias = setores(cargos, true);
        return assessorias.isEmpty() ? Optional.empty() : Optional.of(new Acesso(Perfil.ASSESSOR, assessorias));
    }

    private static Set<Setor> setores(Collection<Cargo> cargos, boolean assessoria) {
        return cargos.stream()
                .filter(c -> c.getSetor() != null && c.isAssessoria() == assessoria)
                .map(Cargo::getSetor)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Setor.class)));
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Set<Setor> getSetores() {
        return setores;
    }

    /** Ex.: "Administrador", "Presidência", "Diretor · Esportes", "Assessor · Eventos". */
    public String getDescricao() {
        if (perfil == Perfil.DIRETOR || perfil == Perfil.ASSESSOR) {
            return perfil.getDescricao() + " · " + setores.stream().map(Setor::getDescricao).collect(Collectors.joining(", "));
        }
        return perfil.getDescricao();
    }

    /** Administrador ou presidência: pode alterar tudo (fora usuários, que é só do administrador). */
    public boolean isAcessoTotal() {
        return perfil == Perfil.ADMINISTRADOR || perfil == Perfil.PRESIDENCIA;
    }

    public boolean podeGerenciarUsuarios() {
        return perfil == Perfil.ADMINISTRADOR;
    }

    /** Se o módulo aparece no menu. */
    public boolean veModulo(Modulo modulo) {
        if (modulo == Modulo.USUARIOS) {
            return podeGerenciarUsuarios();
        }
        if (perfil != Perfil.ASSESSOR) {
            return true;
        }
        return modulo == Modulo.INICIO || modulosDosSetores().contains(modulo);
    }

    /**
     * Se pode incluir registros no módulo. Em Eventos e Demandas, cada registro ainda
     * é conferido por podeAlterar(Atividade) e podeAlterar(Demanda).
     */
    public boolean podeAlterar(Modulo modulo) {
        if (modulo == Modulo.USUARIOS) {
            return podeGerenciarUsuarios();
        }
        if (isAcessoTotal()) {
            return true;
        }
        return perfil == Perfil.DIRETOR && modulosDosSetores().contains(modulo);
    }

    /** Se pode editar ou excluir o treino, a competição, o evento ou o compromisso. */
    public boolean podeAlterar(Atividade atividade) {
        if (isAcessoTotal()) {
            return true;
        }
        return perfil == Perfil.DIRETOR && setorDa(atividade).map(setores::contains).orElse(false);
    }

    /** Se pode incluir, editar ou excluir a demanda: diretores, só as ligadas a uma atividade da sua área. */
    public boolean podeAlterar(Demanda demanda) {
        return isAcessoTotal() || demanda.getRelacionada() != null && podeAlterar(demanda.getRelacionada());
    }

    /** Se a atividade aparece nas consultas: o assessor só vê as do seu setor. */
    public boolean ve(Atividade atividade) {
        return perfil != Perfil.ASSESSOR || setorDa(atividade).map(setores::contains).orElse(false);
    }

    /** Tipos de evento que o usuário pode cadastrar. */
    public List<TipoEvento> tiposDeEvento() {
        return EnumSet.allOf(TipoEvento.class).stream()
                .filter(t -> isAcessoTotal() || perfil == Perfil.DIRETOR && setores.contains(t.getSetor()))
                .toList();
    }

    /** Setor responsável pela atividade (vazio para compromissos, que são da presidência). */
    private static Optional<Setor> setorDa(Atividade atividade) {
        if (atividade instanceof Evento evento) {
            return Optional.ofNullable(evento.getTipoEvento()).map(TipoEvento::getSetor);
        }
        if (atividade instanceof Treino || atividade instanceof Competicao) {
            return Optional.of(Setor.ESPORTES);
        }
        return Optional.empty();
    }

    /** Módulos em que os setores do usuário trabalham. */
    private Set<Modulo> modulosDosSetores() {
        Set<Modulo> modulos = EnumSet.noneOf(Modulo.class);
        if (setores.contains(Setor.ESPORTES)) {
            modulos.addAll(List.of(Modulo.ATLETAS, Modulo.TREINOS, Modulo.COMPETICOES));
        }
        if (EnumSet.allOf(TipoEvento.class).stream().anyMatch(t -> setores.contains(t.getSetor()))) {
            modulos.add(Modulo.EVENTOS);
        }
        if (perfil == Perfil.DIRETOR && !modulos.isEmpty()) {
            modulos.add(Modulo.DEMANDAS);
        }
        return modulos;
    }
}
