package com.Lilith.Curtain.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvictingQueue<T> extends LinkedHashMap<T, Integer> {

    private static final long serialVersionUID = 1L;

    public void put(T key) {
        super.put(key, 1);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<T, Integer> eldest) {
        return this.size() > 10;
    }
}
