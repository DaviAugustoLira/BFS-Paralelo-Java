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
        long tempoInicial = System.currentTimeMillis();

        Graph grafo = new SingleGraph("Grafo");

        for (int i = 0; i < 100000; i++) {
            String id = String.valueOf(i);
            Node node = grafo.addNode(id);
        }

        // Gerar arestas aleatórias
        Random rand = new Random();
        for (int i = 0; i < 500000; i++) {
            int source = rand.nextInt(100000);
            int target = rand.nextInt(100000);
            if (source != target) {
                String edgeId = source + "-" + target;
                if (grafo.getEdge(edgeId) == null && grafo.getEdge(target + "-" + source) == null) {
                    grafo.addEdge(edgeId, String.valueOf(source), String.valueOf(target));
                }
            }
        }

        bfs(grafo, "0", "9999", tempoInicial);
    }

    public static void bfs(Graph grafo, String idNoInicial, String noBuscado, long tempoInicial) {
        final int NUM_THREADS = 8;

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

            Queue<Node> descobertos = new ConcurrentLinkedQueue<>();
            CountDownLatch latch = new CountDownLatch(nivelAtual.size());

            for (Node atual : nivelAtual) {
                executor.submit( () -> {
                    if(encontrado.get()){
                        return;
                    }
                    if (noBuscado.equals(atual.getId())) {
                        System.out.println("Nó encontrado: " + atual.getId());
                        System.out.println("Tempo de execução: " + (System.currentTimeMillis() - tempoInicial) + "ms");
                        encontrado.set(true);
                        return;
                    }
                    for (Edge edge : atual.edges().toList()) {
                        Node vizinho = edge.getOpposite(atual);
                        if (visitados.add(vizinho.getId())) {
                            descobertos.add(vizinho);
                        }
                    }
                    latch.countDown();
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            fila.addAll(descobertos);
        }

        System.out.println("Nó não encontrado.");
        executor.shutdown();
    }
}