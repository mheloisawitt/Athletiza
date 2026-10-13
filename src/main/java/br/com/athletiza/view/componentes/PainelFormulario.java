package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Painel base das telas de cadastro (CH-03): título, campos organizados em
 * duas colunas e botões Cancelar e Salvar.
 *
 * Segue as regras da View no MVC: monta os componentes, devolve o que foi
 * digitado, exibe mensagens e recebe do Controller as ações dos botões
 * (adicionarAcaoSalvar, adicionarAcaoCancelar). Não tem regra de negócio.
 */
public abstract class PainelFormulario extends JPanel {

    private final JLabel rotuloTitulo = new JLabel();
    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JButton btnSalvar = Botoes.primario("Salvar", null);
    private final JButton btnCancelar = Botoes.contorno("Cancelar", null);
    private int linha;
    private int coluna;

    protected PainelFormulario() {
        super(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

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
        botoes.add(btnCancelar);
        botoes.add(btnSalvar);
        add(botoes, BorderLayout.SOUTH);
    }

    public void setTitulo(String titulo) {
        rotuloTitulo.setText(titulo);
    }

    /* Ações dos botões: registradas pelo Controller (normalmente com lambda) */

    public void adicionarAcaoSalvar(ActionListener acao) {
        btnSalvar.addActionListener(acao);
    }

    public void adicionarAcaoCancelar(ActionListener acao) {
        btnCancelar.addActionListener(acao);
    }

    public JButton getBtnSalvar() {
        return btnSalvar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    /* Mensagens ao usuário */

    public void mostrarMensagem(String mensagem) {
        Mensagens.sucesso(this, mensagem);
    }

    public void mostrarAviso(String mensagem) {
        Mensagens.aviso(this, mensagem);
    }

    /** Exibe o erro: mensagem amigável para erros do sistema, aviso genérico (e log) para os inesperados. */
    public void mostrarErro(Exception erro) {
        Mensagens.erro(this, erro);
    }

    /** Pergunta Sim/Não. Retorna true se o usuário escolher Sim. */
    public boolean confirmar(String pergunta) {
        return Mensagens.confirmar(this, pergunta);
    }

    /* Montagem dos campos (usado pelas subclasses em inicializarComponentes) */

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

    /** Texto digitado sem espaços nas pontas, ou null se o campo estiver vazio. */
    protected static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /** Texto para exibir em um campo (vazio em vez de "null"). */
    protected static String textoOuVazio(Object valor) {
        return valor == null ? "" : valor.toString();
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
}
