package org.example.taskmanager.entity;

/**
 * A generic class representing a pair of values.
 * This class is immutable and can hold two values of different types.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class Pair<K, V> {
    private final K key;
    private final V value;

    /**
     * Constructs a new Pair with the specified key and value.
     *
     * @param key   the key of the pair
     * @param value the value of the pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
