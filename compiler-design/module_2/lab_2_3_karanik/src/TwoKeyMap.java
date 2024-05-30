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
}