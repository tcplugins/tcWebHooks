package webhook.teamcity.payload.content;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class ExtraParametersMap extends TreeMap<String, String> {

    private static final long serialVersionUID = -2947332186712049416L;

    public ExtraParametersMap(Map<String, String> map) {
        super(map);
    }

    public Set<Entry<String, String>> getEntriesAsSet() {
        return this.entrySet();
    }
}
