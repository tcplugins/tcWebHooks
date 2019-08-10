package webhook.teamcity.history;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = -8518874910030309391L;
	private final int maxSize;

    public MaxSizeHashMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}