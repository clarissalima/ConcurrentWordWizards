import java.util.*;

public class PalavrasDoJogo {

    private static final String[][] FACIL = {
            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"TCP", "Protocolo de comunicação confiável, orientado à conexão."},
            {"UDP", "Protocolo de comunicação sem conexão e não confiável."},
            {"NETWORK", "Conjunto de computadores interconectados."},
            {"CLIENTE", "Solicita serviços de um servidor e inicia a interação na comunicação em rede."},
            {"SERVIDOR", "Fornece serviços a clientes e opera continuamente."},
            {"PORTA", "Identificador numérico usado para distinguir processos em comunicação de rede."},
            {"WAIT", "Operação que bloqueia um processo até que uma condição seja atendida."},
            {"PROCESSO", "Programa em execução que pode conter múltiplas threads."},

    };

    private static final String[][] MEDIO = {
            {"SOCKET", "Usado para comunicação em rede."},
            {"JOIN", "Método usado para garantir que uma thread espere pela conclusão de outra."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"BUFFER", "Área de memória usada para armazenar temporariamente dados em transmissão."},
            {"SEMAFORO", "Estrutura usada para controle de acesso concorrente em seções críticas."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"STARVATION", "Situação onde um processo nunca ganha acesso à seção crítica devido à prioridade de outros."},
            {"MONITOR", "Estrutura que combina bloqueios e variáveis de condição para sincronização de processos."},

    };

    private static final String[][] DIFICIL = {

            {"EXCLUSÃO MÚTUA", "Mecanismo que impede que múltiplos processos acessem simultaneamente uma seção crítica."},
            {"DEKKER", "Algoritmo que garante exclusão mútua para dois processos."},
            {"PETERSON", "Algoritmo eficiente para exclusão mútua entre dois processos concorrentes."},
            {"DIJKSTRA", "Algoritmo que coordena múltiplos processos garantindo exclusão mútua."},
            {"BAKERY", "Algoritmo inspirado em senhas de padaria para controlar o acesso concorrente."},
            {"BUFFER TX", "Armazena mensagens antes do envio para o destino."},
            {"BUFFER RX", "Recebe mensagens enviadas antes de serem processadas."},
            {"INPUTSTREAM", "Classe Java usada para leitura de dados em um Socket TCP."},
            {"OUTPUTSTREAM", "Classe Java usada para escrita de dados em um Socket TCP."},
            {"MUTEX", "Mecanismo de sincronização que permite apenas um processo por vez em uma seção crítica."},
            {"ATOMICIDADE", "Propriedade onde uma operação ocorre completamente ou não ocorre."},

    };


    public static List<String[]> getPalavrasPorDiculdade(String modo) {
        List<String[]> palavrasSelecionadas = new ArrayList<>();
        Random random = new Random();

        switch (modo) {
            case "fácil":
                palavrasSelecionadas = Arrays.asList(FACIL);
                break;
            case "médio":
                palavrasSelecionadas = Arrays.asList(MEDIO);
                break;
            case "difícil":
                palavrasSelecionadas = Arrays.asList(DIFICIL);
                break;
            default:
                palavrasSelecionadas = Arrays.asList(MEDIO);
        }

        Collections.shuffle(palavrasSelecionadas, random);
        return palavrasSelecionadas;
    }
}
