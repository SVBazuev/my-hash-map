package edu.example;

public class Main {
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put(null, 0);
        map.put("three", 3);

        System.out.println(map.get("one"));
        System.out.println(map.get(null));
        System.out.println(map.size());

        map.remove("two");
        System.out.println(map.containsKey("two"));
        System.out.println(map.containsValue(3));

        map.clear();
        System.out.println(map.isEmpty());
    }
}
