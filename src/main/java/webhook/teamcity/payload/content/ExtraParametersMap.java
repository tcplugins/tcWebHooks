package webhook.teamcity.payload.content;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class ExtraParametersMap extends TreeMap<String, String> {

	private static final long serialVersionUID = -2947332186712049416L;

	public ExtraParametersMap(SortedMap<String, String> map){
		super(map);
	}
		
	public Set<Entry<String, String>> getEntriesAsSet(){
		return this.entrySet();
	}
}
