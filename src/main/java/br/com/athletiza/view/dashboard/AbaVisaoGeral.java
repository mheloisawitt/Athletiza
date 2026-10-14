package br.com.athletiza.view.dashboard;

import br.com.athletiza.controller.DashboardController;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Botoes;
import br.com.athletiza.view.componentes.Mensagens;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Primeira aba do dashboard: cartões com os totais e a linha do tempo do mês,
 * com treinos, competições e eventos juntos (mesmas cores do calendário).
 */
class AbaVisaoGeral extends AbaDashboard {

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM 'de' uuuu",
            Locale.forLanguageTag("pt-BR"));

    private final Cartoes.Numero atletas = new Cartoes.Numero("Atletas ativos");
    private final Cartoes.Numero modalidades = new Cartoes.Numero("Modalidades");
    private final Cartoes.Numero competicoes = new Cartoes.Numero("Competições no ano");
    private final Cartoes.Numero eventos = new Cartoes.Numero("Eventos no ano");
    private final LinhaDoTempo linhaDoTempo = new LinhaDoTempo();
    private final JLabel rotuloMes = new JLabel();
    private YearMonth mes = YearMonth.now();

    AbaVisaoGeral() {
        adicionarLinha(atletas, modalidades, competicoes, eventos);
        adicionarLinha(criarCartaoLinhaDoTempo());
    }

    private JPanel criarCartaoLinhaDoTempo() {
        JLabel titulo = new JLabel("Linha do tempo do mês");
        titulo.setFont(Tema.fonte(Font.BOLD, 14f));
        rotuloMes.setForeground(Cores.TEXTO_SECUNDARIO);
        JButton anterior = Botoes.contorno("<", e -> trocarMes(-1));
        JButton proximo = Botoes.contorno(">", e -> trocarMes(1));
        JButton atual = Botoes.contorno("Mês atual", e -> {
            mes = YearMonth.now();
            mostrarLinhaDoTempo();
        });

        JPanel navegacao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navegacao.setOpaque(false);
        navegacao.add(anterior);
        navegacao.add(proximo);
        navegacao.add(atual);

        JPanel textos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(rotuloMes);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(textos, BorderLayout.WEST);
        cabecalho.add(navegacao, BorderLayout.EAST);

        // Legenda com as cores do calendário
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legenda.setOpaque(false);
        for (TipoAtividade tipo : new TipoAtividade[]{TipoAtividade.TREINO, TipoAtividade.AMISTOSO,
            TipoAtividade.COMPETICAO, TipoAtividade.EVENTO}) {
            JLabel item = new JLabel("● " + tipo.getDescricao());
            item.setForeground(Color.decode(tipo.getCorHex()));
            item.setFont(Tema.fonte(Font.PLAIN, 11f));
            legenda.add(item);
        }
        JLabel dica = new JLabel("Passe o mouse sobre um ponto para ver as atividades do dia");
        dica.setForeground(Cores.TEXTO_SECUNDARIO);
        dica.setFont(Tema.fonte(Font.PLAIN, 11f));
        legenda.add(dica);

        JPanel cartao = Cartoes.fundo();
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(linhaDoTempo, BorderLayout.CENTER);
        cartao.add(legenda, BorderLayout.SOUTH);
        cartao.setPreferredSize(new Dimension(600, 270));
        return cartao;
    }

    @Override
    protected void atualizar(LocalDate hoje) throws PersistenciaException {
        DashboardController.Totais totais = controller.totais(hoje);
        atletas.mostrar(totais.atletasAtivos(), "com situação Ativo");
        modalidades.mostrar(totais.modalidades(), "cadastradas");
        competicoes.mostrar(totais.competicoesNoAno(), "em " + hoje.getYear() + ", sem as canceladas");
        eventos.mostrar(totais.eventosNoAno(), "em " + hoje.getYear() + ", sem os cancelados");
        mostrarLinhaDoTempo();
    }

    private void trocarMes(int quantidade) {
        mes = mes.plusMonths(quantidade);
        mostrarLinhaDoTempo();
    }

    private void mostrarLinhaDoTempo() {
        String nome = FORMATO_MES.format(mes);
        rotuloMes.setText(nome.substring(0, 1).toUpperCase() + nome.substring(1));
        try {
            linhaDoTempo.mostrar(mes, LocalDate.now(), controller.linhaDoTempo(mes));
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }
}
