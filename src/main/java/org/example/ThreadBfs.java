package org.example;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class ThreadBfs implements Runnable{

    private final Set<String> visitados;
    private final Node atual;
    private final CountDownLatch latch;
    private final List<Node> descobertos;

    public ThreadBfs(Set<String> visitados, Node atual, CountDownLatch latch, List<Node> descobertos) {
        this.visitados = visitados;
        this.atual = atual;
        this.latch = latch;
        this.descobertos = descobertos;
    }

    @Override
    public void run() {
        for (Edge edge : atual.edges().toList()) {
            Node vizinho = edge.getOpposite(atual);
            if (visitados.add(vizinho.getId())) {
                descobertos.add(vizinho);
            }
        }
    }
}
