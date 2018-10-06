package me.yang.recsys.wikivoyage.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MinHeap<T> {

    private PriorityQueue<Pair<T, Double>> minHeap;
    private int k;

    public MinHeap(int k) {
        this.k = k;
        minHeap = new PriorityQueue<>((o1, o2) -> {
            if (o1.getRight() > o2.getRight())
                return 1;
            else if (o1.getRight() < o2.getRight())
                return -1;
            return 0;
        });
    }

    public void update(T key, double value) {
        Pair<T, Double> pair = Pair.of(key, value);
        try {
            if (minHeap.size() < k) minHeap.add(pair);
            else {
                Pair<T, Double> top = minHeap.peek();
                if (pair.getRight() > top.getRight()) {
                    minHeap.poll();
                    minHeap.add(pair);
                }
            }
        } catch (Exception e) {

        }
    }

    public List<Pair<T, Double>> pollAll() {
        List<Pair<T, Double>> topK = new ArrayList<>();
        while(!minHeap.isEmpty()) topK.add(minHeap.poll());
        return topK;
    }
}
