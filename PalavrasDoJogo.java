import java.util.*;

public class PalavrasDoJogo {
    private static final String[][] WORDS = {
            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"SOCKET", "Usado para comunicação em rede."},
            {"TCP", "Protocolo de comunicação confiável, orientado à conexão."},
            {"UDP", "Protocolo de comunicação sem conexão e não confiável."},
            {"JOIN", "Método usado para garantir que uma thread espere pela conclusão de outra."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"NETWORK", "Conjunto de computadores interconectados."},
            {"CLIENTE", "Solicita serviços de um servidor e inicia a interação na comunicação em rede."},
            {"SERVIDOR", "Fornece serviços a clientes e opera continuamente."},
            {"PORTA", "Identificador numérico usado para distinguir processos em comunicação de rede."},
            {"BUFFER", "Área de memória usada para armazenar temporariamente dados em transmissão."},
            {"EXCLUSÃO MÚTUA", "Mecanismo que impede que múltiplos processos acessem simultaneamente uma seção crítica."},
            {"DEKKER", "Algoritmo que garante exclusão mútua para dois processos."},
            {"PETERSON", "Algoritmo eficiente para exclusão mútua entre dois processos concorrentes."},
            {"DIJKSTRA", "Algoritmo que coordena múltiplos processos garantindo exclusão mútua."},
            {"BAKERY", "Algoritmo inspirado em senhas de padaria para controlar o acesso concorrente."},
            {"BUFFER TX", "Armazena mensagens antes do envio para o destino."},
            {"BUFFER RX", "Recebe mensagens enviadas antes de serem processadas."},
            {"INPUTSTREAM", "Classe Java usada para leitura de dados em um Socket TCP."},
            {"OUTPUTSTREAM", "Classe Java usada para escrita de dados em um Socket TCP."},
            {"STARVATION", "Situação onde um processo nunca ganha acesso à seção crítica devido à prioridade de outros."},
            {"MUTEX", "Mecanismo de sincronização que permite apenas um processo por vez em uma seção crítica."},
            {"SEMAFORO", "Estrutura usada para controle de acesso concorrente em seções críticas."},
            {"WAIT", "Operação que bloqueia um processo até que uma condição seja atendida."},
            {"PROCESSO", "Programa em execução que pode conter múltiplas threads."},
            {"MONITOR", "Estrutura que combina bloqueios e variáveis de condição para sincronização de processos."},
            {"ATOMICIDADE", "Propriedade onde uma operação ocorre completamente ou não ocorre."},
    };

    public static List<String[]> getPalavrasEmbaralhadas() {
        List<String[]> shuffledWords = new ArrayList<>(Arrays.asList(WORDS));
        Collections.shuffle(shuffledWords, new Random());
        return shuffledWords;
    }
}
