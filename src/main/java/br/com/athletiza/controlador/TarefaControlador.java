package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.SituacaoTarefa;
import br.com.athletiza.modelo.Tarefa;
import br.com.athletiza.repositorio.MembroRepositorio;
import br.com.athletiza.visao.componentes.PainelTarefas;
import java.util.List;

public class TarefaControlador {

    public interface FonteTarefas {
        List<Tarefa> listar() throws AthletizaException;

        void salvar(Tarefa tarefa) throws AthletizaException;

        void excluir(Tarefa tarefa) throws AthletizaException;
    }

    private final PainelTarefas visao;
    private final FonteTarefas fonte;
    private final MembroRepositorio membroRepositorio;

    public TarefaControlador(PainelTarefas visao, FonteTarefas fonte, MembroRepositorio membroRepositorio) {
        this.visao = visao;
        this.fonte = fonte;
        this.membroRepositorio = membroRepositorio;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoBtnIncluir(e -> editar(new Tarefa()));
        visao.adicionarAcaoBtnEditar(e -> editarSelecionada());
        visao.adicionarAcaoBtnExcluir(e -> excluirSelecionada());
        visao.adicionarAcaoBtnConcluir(e -> concluirSelecionada());
    }

    public void carregar() {
        try {
            visao.apresentarDados(fonte.listar());
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
    }

    private void editarSelecionada() {
        Tarefa tarefa = visao.getSelecionado();
        if (tarefa != null) {
            editar(tarefa);
        }
    }

    private void editar(Tarefa tarefa) {
        try {
            List<Membro> membros = new MembroControlador(membroRepositorio).listarAtivos();
            boolean continuar = true;
            while (continuar) {
                try {
                    if (visao.apresentarFormulario(tarefa, membros)) {
                        fonte.salvar(tarefa);
                    }
                    continuar = false;
                } catch (AthletizaException e) {
                    visao.apresentarErro(e);
                }
            }
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void concluirSelecionada() {
        Tarefa tarefa = visao.getSelecionado();
        if (tarefa == null) {
            return;
        }
        if (!visao.podeIncluir()) {
            visao.apresentarAviso("Seu perfil não permite alterar estas tarefas.");
            return;
        }
        tarefa.setSituacao(SituacaoTarefa.CONCLUIDA);
        try {
            fonte.salvar(tarefa);
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }

    private void excluirSelecionada() {
        Tarefa tarefa = visao.getSelecionado();
        if (tarefa == null || !visao.confirmarExclusao(tarefa)) {
            return;
        }
        try {
            fonte.excluir(tarefa);
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
        }
        carregar();
    }
}
