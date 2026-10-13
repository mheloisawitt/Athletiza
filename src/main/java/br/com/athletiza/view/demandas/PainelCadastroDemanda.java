package br.com.athletiza.view.demandas;

import br.com.athletiza.controller.DemandaController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.util.Validador;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.ListaMarcavel;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cadastro e edição de demanda. A relação com um evento ou competição é
 * opcional, e a demanda pode ter um ou mais membros responsáveis.
 */
public class PainelCadastroDemanda extends PainelFormulario {

    private final Demanda demanda;
    private final DemandaController controller = new DemandaController();
    private final JTextField titulo = new JTextField();
    private final JComboBox<SituacaoDemanda> situacao = new JComboBox<>(SituacaoDemanda.values());
    private final JComboBox<PrioridadeDemanda> prioridade = new JComboBox<>(PrioridadeDemanda.values());
    private final JTextField prazo = new JTextField();
    private final JComboBox<Atividade> relacionada = new JComboBox<>();
    private final ListaMarcavel<Membro> responsaveis = new ListaMarcavel<>(
            "Nenhum membro na gestão em andamento. Cadastre a composição da gestão no módulo Gestão.");
    private final JTextArea descricao = new JTextArea();

    public PainelCadastroDemanda(Demanda demanda, Navegador navegador) {
        super(demanda.isNova() ? "Nova Demanda" : "Editar Demanda", navegador);
        this.demanda = demanda;

        titulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Reservar ônibus para os atletas");
        prazo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        descricao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Detalhes do que precisa ser feito...");
        Combos.exibirComo(relacionada, item -> item == null
                ? "Nenhum (demanda avulsa)" : Demanda.descreverRelacionada(item));

        adicionarCampo("Demanda", titulo, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampo("Prioridade", prioridade, true);
        adicionarCampo("Prazo", prazo, false);
        adicionarCampoLinhaInteira("Relacionada a evento ou competição", relacionada, false);
        adicionarCampoLinhaInteira("Responsáveis (membros da gestão em andamento)", responsaveis, false);
        adicionarCampoLinhaInteira("Descrição", areaTexto(descricao), false);

        carregarOpcoes();
        preencher();
    }

    /** Eventos e competições para relacionar e membros da gestão em andamento (mais os já responsáveis). */
    private void carregarOpcoes() {
        try {
            Combos.atualizarItens(relacionada, controller.listarRelacionaveis());
            List<Membro> membros = new ArrayList<>(new MembroController().listarDaGestaoEmAndamento());
            demanda.getResponsaveis().stream().filter(m -> !membros.contains(m)).forEach(membros::add);
            responsaveis.setItens(membros, Membro::getNome);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    private void preencher() {
        titulo.setText(demanda.getTitulo());
        situacao.setSelectedItem(demanda.getSituacao());
        prioridade.setSelectedItem(demanda.getPrioridade());
        prazo.setText(demanda.getPrazo() == null ? "" : Validador.FORMATO_DATA.format(demanda.getPrazo()));
        relacionada.setSelectedItem(opcaoRelacionada(demanda.getRelacionada()));
        responsaveis.setMarcados(demanda.getResponsaveis());
        descricao.setText(demanda.getDescricao());
    }

    /** A opção do combo que corresponde ao evento ou competição da demanda (mesmo tipo e mesmo id). */
    private Atividade opcaoRelacionada(Atividade atual) {
        if (atual == null) {
            return null;
        }
        for (int i = 0; i < relacionada.getItemCount(); i++) {
            Atividade opcao = relacionada.getItemAt(i);
            if (opcao != null && opcao.getClass() == atual.getClass() && Objects.equals(opcao.getId(), atual.getId())) {
                return opcao;
            }
        }
        return null;
    }

    @Override
    protected void salvar() throws AthletizaException {
        demanda.setTitulo(titulo.getText().trim());
        demanda.setSituacao((SituacaoDemanda) situacao.getSelectedItem());
        demanda.setPrioridade((PrioridadeDemanda) prioridade.getSelectedItem());
        demanda.setPrazo(Validador.converterData(prazo.getText(), "Prazo"));
        demanda.setRelacionada((Atividade) relacionada.getSelectedItem());
        demanda.definirResponsaveis(responsaveis.getMarcados());
        demanda.setDescricao(descricao.getText() == null || descricao.getText().isBlank() ? null : descricao.getText().trim());
        controller.salvar(demanda);
    }

    @Override
    protected String getMensagemSucesso() {
        return "Demanda salva com sucesso.";
    }
}
