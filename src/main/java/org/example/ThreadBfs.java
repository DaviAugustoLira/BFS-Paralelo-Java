package org.example;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class ThreadBfs implements Runnable{

    private final Set<String> visitados;
    private final Node atual;
    private final Semaphore semaphore;
    private CountDownLatch latch;
    private final List<Node> descobertos;

    public ThreadBfs(Semaphore semaphore, Set<String> visitados, Node atual, CountDownLatch latch, List<Node> descobertos) {
        this.visitados = visitados;
        this.atual = atual;
        this.latch = latch;
        this.descobertos = descobertos;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Edge edge : atual.edges().toList()) {
            Node vizinho = edge.getOpposite(atual);
            if (visitados.add(vizinho.getId())) {
                descobertos.add(vizinho);
            }
        }
        semaphore.release();
        latch.countDown();
    }
}
