package webhook.teamcity.statistics;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StatisticsSnapshot {
	@XmlAttribute
	private LocalDate date;
	
	private int totalCount = 0;
	private int okCount = 0;
	private int errorCount = 0 ;
	private int skippedCount = 0;
	private Map<String, StatisticsItem> urls = new HashMap<>();
	private Map<String, StatisticsItem> templates = new HashMap<>();

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class StatisticsItem {
		String name;
		int invocations;
		Map<Integer, Integer> statuses; 
	}

	public StatisticsSnapshot at(LocalDate now) {
		this.date  = now;
		return this;
	}

	public void addErrorCount(int erroredCount) {
		this.errorCount += erroredCount;
		
	}

	public void addSkippedCount(int skippedCount) {
		this.skippedCount += skippedCount;		
	}

	public void addOkCount(int okCount) {
		this.okCount += okCount;
	}

	public void addTotalCount(int totalCount) {
		this.totalCount += totalCount;
	}
	
	
}
