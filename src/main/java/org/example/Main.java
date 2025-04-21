package org.example;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;
import java.util.concurrent.*;


public class Main {
    public static void main(String[] args) {
        long tempoInicial = System.currentTimeMillis();

        Graph grafo = new SingleGraph("Grafo");

        for (int i = 0; i < 10000; i++) {
            String id = String.valueOf(i);
            Node node = grafo.addNode(id);
        }

        // Gerar arestas aleatórias
        Random rand = new Random();
        for (int i = 0; i < 50000; i++) {
            int source = rand.nextInt(10000);
            int target = rand.nextInt(10000);
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
        final int NUM_THREADS = 2;

        Set<String> visitados = ConcurrentHashMap.newKeySet();
        Queue<Node> fila = new ConcurrentLinkedQueue<>();
        Node noInicial = grafo.getNode(idNoInicial);

        fila.add(noInicial);
        visitados.add(idNoInicial);

        while (!fila.isEmpty()) {
            List<Node> nivelAtual = new ArrayList<>();
            while (!fila.isEmpty()) {
                Node n = fila.poll();
                if (n != null) nivelAtual.add(n);
            }

            List<Node> descobertos = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(nivelAtual.size());
            Semaphore semaphore = new Semaphore(NUM_THREADS);

            for (Node atual : nivelAtual) {
                if (noBuscado.equals(atual.getId())) {
                    System.out.println("Nó encontrado: " + atual.getId());
                    System.out.println("Tempo de execução: " + (System.currentTimeMillis() - tempoInicial) + "ms");
                    return;
                }
                Runnable thread = new ThreadBfs(semaphore, visitados, atual, latch, descobertos);
                new Thread(thread).start();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            fila.addAll(descobertos);
        }

        System.out.println("Nó não encontrado.");
    }
}