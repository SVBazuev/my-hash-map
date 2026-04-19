package edu.example;


import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class MyHashMap <K, V> implements Map<K,V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private Node<K, V>[] table;
    private int size;
    private int threshold;
    private final float loadFactor;


    private static class Node<K, V> {
        final int hash;
        final K key;
        V val;
        Node <K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }
    }

    public MyHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initCap) {
        this(initCap, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int initCap, float loadFactor) {

        if (initCap < 0) {
            throw new IllegalArgumentException(
                "Illegal initial capacity: " + initCap
            );
        }

        if (initCap > MAXIMUM_CAPACITY) {
            initCap = MAXIMUM_CAPACITY;
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException(
                "Illegal load factor: " + loadFactor
            );
        }

        this.loadFactor = loadFactor;
        int capacity = tableSizeFor(initCap);
        this.threshold = (int) (capacity * loadFactor);
        this.table = (Node<K, V>[]) new Node[capacity];
    }

    private int tableSizeFor(int initCap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(initCap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    public V put(K key, V val) {
        if (key == null) {
            return putForNullKey(val);
        }
        int hash = hash(key);
        int idx = indexFor(hash, table.length);
        Node<K, V> node = table[idx];

        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                V old = node.val;
                node.val = val;
                return old;
            }
            node = node.next;
        }

        addNode(idx, hash, key, val);
        return null;
    }

    private V putForNullKey(V val) {
        Node<K, V> node = table[0];
        while (node != null) {
            if (node.key == null) {
                V old = node.val;
                node.val = val;
                return old;
            }
            node = node.next;
        }
        addNode(0, 0, null, val);
        return null;
    }

    private void addNode(int bucketIdx, int hash, K key, V value) {
        Node<K, V> node = table[bucketIdx];
        table[bucketIdx] = new Node<>(hash, key, value, node);
        size++;
        if (size >= threshold) {
            resize();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int oldCap = oldTable.length;
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        int newCap = oldCap << 1;
        if (newCap > MAXIMUM_CAPACITY) {
            newCap = MAXIMUM_CAPACITY;
        }
        threshold = (int) (newCap * loadFactor);
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCap];
        // Перехеширование всех существующих записей
        for (Node<K, V> head : oldTable) {
            while (head != null) {
                Node<K, V> next = head.next;
                int idx = indexFor(head.hash, newCap);
                head.next = newTable[idx];
                newTable[idx] = head;
                head = next;
            }
        }
        table = newTable;
    }

    @Override
    public V get(Object key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.val;
    }

    private Node<K, V> getNode(Object key) {
        if (key == null) {
            Node<K, V> node = table[0];
            while (node != null) {
                if (node.key == null) return node;
                node = node.next;
            }
            return null;
        }
        int hash = hash(key);
        int idx = indexFor(hash, table.length);
        Node<K, V> node = table[idx];
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return node;
            }
            node = node.next;
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            return removeNode(0, null, true);
        }
        int hash = hash(key);
        int idx = indexFor(hash, table.length);
        return removeNode(idx, key, false);
    }

    private V removeNode(int bucketIdx, Object key, boolean isNullKey) {
        Node<K, V> prev = null;
        Node<K, V> curr = table[bucketIdx];
        while (curr != null) {
            boolean match = isNullKey ? (curr.key == null) :
                    (curr.hash == hash(key) && Objects.equals(curr.key, key));
            if (match) {
                V oldValue = curr.val;
                if (prev == null) {
                    table[bucketIdx] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return oldValue;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return getNode(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    @Override
    public boolean containsValue(Object val) {
        for (Node<K, V> head : table) {
            Node<K, V> node = head;
            while (node != null) {
                if (Objects.equals(node.val, val)) return true;
                node = node.next;
            }
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Set<K> keySet() {
        // Избыточно для Д/З
        throw new UnsupportedOperationException("keySet not implemented");
    }

    @Override
    public Collection<V> values() {
        // Избыточно для Д/З
        throw new UnsupportedOperationException("values not implemented");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // Избыточно для Д/З
        throw new UnsupportedOperationException("entrySet not implemented");
    }

}
