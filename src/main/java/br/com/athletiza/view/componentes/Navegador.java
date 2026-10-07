package br.com.athletiza.view.componentes;

import javax.swing.JComponent;

/**
 * Permite que um painel abra outra tela na área central da janela principal
 * (ex.: a consulta abre o formulário de cadastro) e depois volte.
 */
public interface Navegador {

    void abrir(JComponent tela);

    void voltar();
}
