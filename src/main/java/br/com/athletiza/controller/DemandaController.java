package br.com.athletiza.controller;

import br.com.athletiza.dao.DemandaDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class DemandaController {
    static final String SO_SOLICITANTE_CANCELA =
            "Só quem registrou a demanda, a presidência ou o administrador podem cancelá-la ou excluí-la.";

    private final DemandaDAO dao = new DemandaDAO();

    public List<Demanda> pesquisar(SituacaoDemanda situacao, Membro responsavel) throws PersistenciaException {
        return dao.listarTodos().stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .filter(d -> responsavel == null || d.getResponsaveis().contains(responsavel))
                .sorted()
                .toList();
    }

    public Optional<Membro> membroLogado() {
        Usuario usuario = Sessao.getUsuarioLogado();
        return Optional.ofNullable(usuario == null ? null : usuario.getMembro());
    }

    public void salvar(Demanda demanda) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Set<Membro> permitidos = new HashSet<>(new MembroController().listarDaGestaoEmAndamento());
        Optional<Demanda> anterior = demanda.isNova() ? Optional.empty() : dao.buscarPorId(demanda.getId());
        anterior.ifPresent(salva -> permitidos.addAll(salva.getResponsaveis()));
        List<String> foraDaGestao = demanda.getResponsaveis().stream()
                .filter(m -> !permitidos.contains(m)).map(Membro::getNome).toList();
        new Validador()
                .obrigatorio(demanda.getTitulo(), "Demanda")
                .tamanhoMaximo(demanda.getTitulo(), 100, "Demanda")
                .obrigatorio(demanda.getPrioridade(), "Prioridade")
                .obrigatorio(demanda.getSituacao(), "Situação")
                .obrigatorio(demanda.getPrazo(), "Prazo")
                .tamanhoMaximo(demanda.getDescricao(), 500, "Descrição")
                .regra(!demanda.getResponsaveis().isEmpty(), "Selecione ao menos um responsável.")
                .regra(foraDaGestao.isEmpty(), "Os responsáveis devem ser membros da gestão em andamento. Não fazem parte dela: "
                        + String.join(", ", foraDaGestao) + ".")
                .validar();

        if (demanda.isNova()) {
            if (demanda.getSolicitante() == null) {
                demanda.setSolicitante(Sessao.getUsuarioLogado());
            }
            dao.inserir(demanda);
            return;
        }

        Demanda salva = anterior.orElse(demanda);
        if (demanda.getSituacao() == SituacaoDemanda.CANCELADA && salva.getSituacao() != SituacaoDemanda.CANCELADA
                && !podeCancelar(salva)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        dao.atualizar(demanda);
    }

    public boolean podeCancelar(Demanda demanda) {
        if (Sessao.getAcesso().isAcessoTotal()) {
            return true;
        }
        Usuario usuario = Sessao.getUsuarioLogado();
        return usuario != null && demanda.getSolicitante() != null
                && Objects.equals(demanda.getSolicitante().getId(), usuario.getId());
    }

    public boolean podeMudarSituacao(Demanda demanda) {
        return Sessao.getAcesso().podeAlterar(demanda) || membroLogado().map(demanda::isResponsavel).orElse(false);
    }

    public void mudarSituacao(Demanda demanda, SituacaoDemanda nova) throws RegraNegocioException, PersistenciaException {
        if (nova == null || nova == demanda.getSituacao()) {
            return;
        }
        if (!podeMudarSituacao(demanda)) {
            throw new RegraNegocioException("Só os responsáveis pela demanda ou quem pode editá-la mudam a situação.");
        }
        if (nova == SituacaoDemanda.CANCELADA && !podeCancelar(demanda)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        dao.atualizarSituacao(demanda.getId(), nova);
        demanda.setSituacao(nova);
    }

    public void excluir(Demanda demanda) throws RegraNegocioException, PersistenciaException {
        if (!podeCancelar(demanda)) {
            throw new RegraNegocioException(SO_SOLICITANTE_CANCELA);
        }
        dao.excluir(demanda.getId());
    }

    public List<Atividade> listarRelacionaveis() throws PersistenciaException {
        return Stream.<Atividade>concat(
                new EventoController().pesquisar(null, null, null).stream(),
                new CompeticaoController().pesquisar(null, null, null).stream())
                .sorted()
                .toList();
    }
}
