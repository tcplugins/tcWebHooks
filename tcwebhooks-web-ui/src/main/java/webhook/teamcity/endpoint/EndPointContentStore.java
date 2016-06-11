package webhook.teamcity.endpoint;

import java.util.List;
import java.util.ArrayList;

import lombok.Synchronized;


public class EndPointContentStore {
	
	private static final int MAX_SIZE = 100;
	
	List<EndPointPayload> store = new ArrayList<EndPointPayload>(50);
	
	@Synchronized
	public void put(EndPointPayload payload){
		store.add(payload);
		if (store.size() > MAX_SIZE){
			store.remove(0);
		}
	}
	
	@Synchronized
	public List<EndPointPayload> getAll(){
		return store;
	}
	

}
