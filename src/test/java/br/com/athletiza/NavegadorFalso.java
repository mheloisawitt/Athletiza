package br.com.athletiza;

import br.com.athletiza.view.componentes.Navegador;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * Navegador usado nos testes: em vez de trocar a tela da janela principal,
 * só guarda o que foi aberto e quantas vezes o controller pediu para voltar.
 */
public class NavegadorFalso implements Navegador {

    public final List<JComponent> abertas = new ArrayList<>();
    public int voltou;

    @Override
    public void abrir(JComponent tela) {
        abertas.add(tela);
    }

    @Override
    public void voltar() {
        voltou++;
    }
}
