import java.util.HashMap;
import java.util.Map;

public class TwoKeyMap<K1, K2, V> {
    private final Map<K1, Map<K2, V>> map = new HashMap<>();

    // Метод для добавления значения в карту
    public void put(K1 key1, K2 key2, V value) {
        map.computeIfAbsent(key1, k -> new HashMap<>()).put(key2, value);
    }

    // Метод для получения значения из карты
    public V get(K1 key1, K2 key2) {
        Map<K2, V> innerMap = map.get(key1);
        if (innerMap == null) {
            return null;
        }
        return innerMap.get(key2);
    }

    // Метод для проверки наличия пары ключей в карте
    public boolean containsKeys(K1 key1, K2 key2) {
        return map.containsKey(key1) && map.get(key1).containsKey(key2);
    }

    // Method to print the map in a table format
    public void print() {
        if (map.isEmpty()) {
            System.out.println("The table is empty.");
            return;
        }

        // Collect all keys for both dimensions
        Map<K2, Boolean> allSecondKeys = new HashMap<>();
        map.values().forEach(innerMap -> innerMap.keySet().forEach(key -> {
            if (key != null) {
                allSecondKeys.put(key, true);
            }
        }));

        // Determine the maximum width for each column
        int maxKey1Length = map.keySet().stream().map(key -> key != null ? key.toString().length() : 0).max(Integer::compare).orElse(0);
        Map<K2, Integer> maxKey2Lengths = new HashMap<>();
        for (K2 key2 : allSecondKeys.keySet()) {
            int maxLength = key2 != null ? key2.toString().length() : 0;
            for (Map<K2, V> innerMap : map.values()) {
                V value = innerMap.get(key2);
                if (value != null) {
                    maxLength = Math.max(maxLength, value.toString().length());
                }
            }
            maxKey2Lengths.put(key2, maxLength);
        }

        // Print header
        System.out.print(String.format("%-" + (maxKey1Length + 2) + "s", ""));
        for (K2 secondKey : allSecondKeys.keySet()) {
            System.out.print(String.format("%-" + (maxKey2Lengths.get(secondKey) + 2) + "s", secondKey));
        }
        System.out.println();

        // Print each row
        for (Map.Entry<K1, Map<K2, V>> entry : map.entrySet()) {
            K1 firstKey = entry.getKey();
            System.out.print(String.format("%-" + (maxKey1Length + 2) + "s", firstKey));
            Map<K2, V> innerMap = entry.getValue();
            for (K2 secondKey : allSecondKeys.keySet()) {
                V value = innerMap.get(secondKey);
                System.out.print(String.format("%-" + (maxKey2Lengths.get(secondKey) + 2) + "s", value != null ? value : "null"));
            }
            System.out.println();
        }
    }

}