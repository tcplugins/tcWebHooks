package webhook.teamcity.history;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor 
public class PagedList<T> {
		
		List<T> items;
		int pageNumber;
		int pageSize;
	
	public static <T> PagedList<T> build(int pageNumber, int pageSize, List<T> items) {
		
		if (pageSize == -1) {
			return new PagedList<>(items, 1, items.size()); 
		}
		
		if (items.size() > ((pageNumber -1) * pageSize) + pageSize) {
			items = items.subList((pageNumber -1) * pageSize, ((pageNumber -1) * pageSize) + pageSize);
			return new PagedList<>(items, pageNumber, pageSize);
		} else {
			items = items.subList((pageNumber -1) * pageSize, items.size());
			return new PagedList<>(items, pageNumber, pageSize);
		}
	}
}

