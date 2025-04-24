package org.example;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Main {
    public static void main(String[] args) {

        Graph grafo = new SingleGraph("Grafo");

        for (int i = 0; i < 1000000; i++) {
            String id = String.valueOf(i);
            grafo.addNode(id);
        }

        // Gerar arestas aleatórias
        Random rand = new Random();
        for (int i = 0; i < 5000000; i++) {
            int source = rand.nextInt(1000000);
            int target = rand.nextInt(1000000);
            if (source != target) {
                String edgeId = source + "-" + target;
                if (grafo.getEdge(edgeId) == null && grafo.getEdge(target + "-" + source) == null) {
                    grafo.addEdge(edgeId, String.valueOf(source), String.valueOf(target));
                }
            }
        }

        bfs(grafo, "0", "999999");
    }

    public static void bfs(Graph grafo, String idNoInicial, String noBuscado) {
        long tempoInicial = System.currentTimeMillis();

        final int NUM_THREADS = 4;

        Set<String> visitados = ConcurrentHashMap.newKeySet();
        Queue<Node> fila = new ConcurrentLinkedQueue<>();
        Node noInicial = grafo.getNode(idNoInicial);
        AtomicBoolean encontrado = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        fila.add(noInicial);
        visitados.add(idNoInicial);

        while (!fila.isEmpty() && !encontrado.get()) {
            List<Node> nivelAtual = new ArrayList<>();
            while (!fila.isEmpty()) {
                Node n = fila.poll();
                if (n != null) nivelAtual.add(n);
            }

//            Queue<Node> descobertos = new ConcurrentLinkedQueue<>();
            CountDownLatch latch = new CountDownLatch(nivelAtual.size());

            for (Node atual : nivelAtual) {
                if(encontrado.get()){
                    executor.shutdownNow();
                    return;
                }

                if (noBuscado.equals(atual.getId())) {
                    System.out.println("Nó encontrado: " + atual.getId());
                    System.out.println("Tempo de execução: " + (System.currentTimeMillis() - tempoInicial) + "ms");
                    encontrado.set(true);
                    return;
                }

                executor.submit( () -> {
                    atual.edges().forEach( element -> {
                            Node vizinho = element.getOpposite(atual);
                            if(visitados.add(vizinho.getId())){
                            fila.add(vizinho);
                            }
                        });
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

//            fila.addAll(descobertos);
        }

        System.out.println("Nó não encontrado.");
        executor.shutdownNow();
    }
}