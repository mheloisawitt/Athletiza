package br.com.athletiza.view.componentes;

import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Painel base das telas de cadastro (CH-03): título, campos organizados em
 * duas colunas e botões Cancelar e Salvar.
 *
 * A subclasse adiciona os campos no construtor e implementa salvar().
 * Erros lançados em salvar() são mostrados ao usuário automaticamente.
 */
public abstract class PainelFormulario extends JPanel {

    private final JPanel campos = new JPanel(new GridBagLayout());
    private final Navegador navegador;
    private int linha;
    private int coluna;

    protected PainelFormulario(String titulo, Navegador navegador) {
        super(new BorderLayout(0, 20));
        this.navegador = navegador;
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel rotuloTitulo = new JLabel(titulo);
        rotuloTitulo.setFont(Tema.fonteTitulo());
        add(rotuloTitulo, BorderLayout.NORTH);

        campos.setOpaque(false);
        JPanel alinhamentoTopo = new JPanel(new BorderLayout());
        alinhamentoTopo.setOpaque(false);
        alinhamentoTopo.add(campos, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(alinhamentoTopo);
        rolagem.setBorder(null);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(Botoes.contorno("Cancelar", e -> cancelar()));
        botoes.add(Botoes.primario("Salvar", e -> confirmarSalvar()));
        add(botoes, BorderLayout.SOUTH);
    }

    /**
     * Valida e grava os dados. Deve lançar ValidacaoException, RegraNegocioException
     * ou PersistenciaException em caso de problema.
     */
    protected abstract void salvar() throws AthletizaException;

    /** Mensagem exibida após salvar com sucesso. */
    protected String getMensagemSucesso() {
        return "Registro salvo com sucesso.";
    }

    /** Adiciona um campo ocupando meia linha (dois campos lado a lado). */
    protected void adicionarCampo(String rotulo, JComponent campo, boolean obrigatorio) {
        posicionar(rotulo, campo, obrigatorio, 1);
    }

    /** Adiciona um campo ocupando a linha inteira. */
    protected void adicionarCampoLinhaInteira(String rotulo, JComponent campo, boolean obrigatorio) {
        if (coluna != 0) {
            linha++;
            coluna = 0;
        }
        posicionar(rotulo, campo, obrigatorio, 2);
    }

    /** Adiciona um componente ocupando a linha inteira, sem rótulo (ex.: texto informativo, barra de botões). */
    protected void adicionarLinha(JComponent componente) {
        if (coluna != 0) {
            linha++;
            coluna = 0;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = linha * 2;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 14, 0);
        campos.add(componente, c);
        linha++;
    }

    /** Área de texto com rolagem, para descrições e observações. */
    protected static JScrollPane areaTexto(JTextArea area) {
        area.setRows(4);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new JScrollPane(area);
    }

    protected void voltar() {
        navegador.voltar();
    }

    private void posicionar(String rotulo, JComponent campo, boolean obrigatorio, int largura) {
        JLabel rotuloCampo = new JLabel(obrigatorio ? rotulo + " *" : rotulo + " (opcional)");
        rotuloCampo.setFont(Tema.fonte(Font.BOLD, 12f));
        rotuloCampo.setForeground(obrigatorio ? Cores.TEXTO : Cores.TEXTO_SECUNDARIO);
        rotuloCampo.setLabelFor(campo);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = coluna;
        c.gridwidth = largura;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, coluna == 0 ? 0 : 16, 4, coluna + largura >= 2 ? 0 : 16);

        c.gridy = linha * 2;
        campos.add(rotuloCampo, c);
        c.gridy = linha * 2 + 1;
        c.insets = new Insets(0, c.insets.left, 18, c.insets.right);
        campos.add(campo, c);

        coluna += largura;
        if (coluna >= 2) {
            coluna = 0;
            linha++;
        }
    }

    private void confirmarSalvar() {
        try {
            salvar();
            Mensagens.sucesso(this, getMensagemSucesso());
            navegador.voltar();
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
    }

    private void cancelar() {
        if (Mensagens.confirmar(this, "Deseja sair sem salvar? As alterações serão perdidas.")) {
            navegador.voltar();
        }
    }
}
