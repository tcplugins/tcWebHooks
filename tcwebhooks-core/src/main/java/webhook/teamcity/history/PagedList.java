package webhook.teamcity.history;

import java.util.List;

import lombok.Getter;

@Getter
public class PagedList<T> {
		
		List<T> items;
		int pageNumber;
		int pageSize;
		int totalItems;
		int totalPages;
		int itemsOnThisPage;
		
	public PagedList(List<T> itemsList, int pageNum, int itemPerPage) {
		this.totalItems = itemsList.size();
		this.pageNumber = pageNum;
		this.pageSize = itemPerPage;
		calculateTotalPages();
		
		if (itemsList.size() > ((pageNum -1) * itemPerPage) + itemPerPage) {
			this.items = itemsList.subList((pageNum -1) * itemPerPage, ((pageNum -1) * itemPerPage) + itemPerPage);
			this.itemsOnThisPage = items.size();
		} else {
			items = itemsList.subList((pageNum -1) * itemPerPage, itemsList.size());
			itemsOnThisPage = items.size();
		}
	}
		
	public static <T> PagedList<T> build(int pageNumber, int pageSize, List<T> items) {
		
		if (pageSize == -1) {
			return new PagedList<>(items, 1, items.size()); 
		}
		
		return new PagedList<>(items, pageNumber, pageSize);
	}
	
	public void calculateTotalPages() {
		if (this.totalItems == 0) {
			this.totalPages = 1;
		} else if (this.totalItems % this.pageSize > 0) { 			// % is modulas, which will be > 0 if we have a remainder.
			this.totalPages = (this.totalItems / this.pageSize) + 1;
		} else {
			this.totalPages = Math.round((float)this.totalItems / this.pageSize);
		}
	}
}

