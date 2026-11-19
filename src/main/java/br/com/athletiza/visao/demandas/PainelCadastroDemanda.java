package br.com.athletiza.visao.demandas;

import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.PrioridadeDemanda;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.ListaMarcavel;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PainelCadastroDemanda extends PainelFormulario {
    private final JTextField titulo = new JTextField();
    private final JComboBox<SituacaoDemanda> situacao = new JComboBox<>(SituacaoDemanda.values());
    private final JComboBox<PrioridadeDemanda> prioridade = new JComboBox<>(PrioridadeDemanda.values());
    private final JTextField prazo = new JTextField();
    private final JComboBox<Atividade> relacionada = new JComboBox<>();
    private final ListaMarcavel<Membro> responsaveis = new ListaMarcavel<>(
            "Nenhum membro na gestão em andamento. Cadastre a composição da gestão no módulo Gestão.");
    private final JTextArea descricao = new JTextArea();

    public PainelCadastroDemanda(Navegador navegador) {
        super("Nova Demanda", navegador);
        titulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: Reservar ônibus para os atletas");
        prazo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/mm/aaaa");
        descricao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Detalhes do que precisa ser feito...");
        Combos.exibirComo(relacionada, item -> item == null
                ? "Nenhum (demanda avulsa)" : Demanda.descreverRelacionada(item));

        adicionarCampo("Demanda", titulo, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampo("Prioridade", prioridade, true);
        adicionarCampo("Prazo", prazo, true);
        adicionarCampoLinhaInteira("Relacionada a evento ou competição", relacionada, false);
        adicionarCampoLinhaInteira("Responsáveis (membros da gestão em andamento)", responsaveis, true);
        adicionarCampoLinhaInteira("Descrição", areaTexto(descricao), false);
    }

    public void apresentarDemanda(Demanda demanda, List<Atividade> relacionaveis, boolean permiteAvulsa,
            List<Membro> membros, boolean podeCancelar) {
        definirTitulo(demanda.isNova() ? "Nova Demanda" : "Editar Demanda");
        if (!demanda.isNova()) {
            JLabel rotuloSolicitante = new JLabel(demanda.getNomeSolicitante().isEmpty()
                    ? "Não registrado" : demanda.getNomeSolicitante());
            adicionarCampo("Solicitante", rotuloSolicitante, false);
        }
        if (!podeCancelar && demanda.getSituacao() != SituacaoDemanda.CANCELADA) {
            situacao.removeItem(SituacaoDemanda.CANCELADA);
        }
        Combos.atualizarItens(relacionada, relacionaveis);
        if (!permiteAvulsa) {
            ((DefaultComboBoxModel<Atividade>) relacionada.getModel()).removeElementAt(0);
        }
        responsaveis.setItens(membros, Membro::getNome);

        titulo.setText(demanda.getTitulo());
        situacao.setSelectedItem(demanda.getSituacao());
        prioridade.setSelectedItem(demanda.getPrioridade());
        prazo.setText(demanda.getPrazo() == null ? "" : Validador.FORMATO_DATA.format(demanda.getPrazo()));
        relacionada.setSelectedItem(opcaoRelacionada(demanda.getRelacionada()));
        if (relacionada.getSelectedItem() == null && relacionada.getItemCount() > 0 && !permiteAvulsa) {
            relacionada.setSelectedIndex(0);
        }
        responsaveis.setMarcados(demanda.getResponsaveis());
        descricao.setText(demanda.getDescricao());
    }

    public String getTitulo() {
        return titulo.getText().trim();
    }

    public SituacaoDemanda getSituacao() {
        return (SituacaoDemanda) situacao.getSelectedItem();
    }

    public PrioridadeDemanda getPrioridade() {
        return (PrioridadeDemanda) prioridade.getSelectedItem();
    }

    public LocalDate getPrazo() throws ValidacaoException {
        return Validador.converterData(prazo.getText(), "Prazo");
    }

    public Atividade getRelacionada() {
        return (Atividade) relacionada.getSelectedItem();
    }

    public List<Membro> getResponsaveis() {
        return responsaveis.getMarcados();
    }

    public String getDescricao() {
        return descricao.getText() == null || descricao.getText().isBlank() ? null : descricao.getText().trim();
    }

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
}
