// ==============================================================================
// INICIO DO PADRAO: COMMAND
// Papel desta classe: Interface do comando
// ==============================================================================
package sorveteria.command;

/** COMMAND: a acao vira um objeto que sabe executar e desfazer. */
public interface Comando {
    void executar();
    void desfazer();
    String getNome();
}

// ------------------------------------------------------------------------------
// FIM DO PADRAO: COMMAND  (arquivo Comando.java)
