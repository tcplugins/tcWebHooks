package webhook.teamcity.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;

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
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StatisticsItem {
		String name;
		int invocations;
		
		@XmlElementWrapper(name="statuses") @XmlElement(name="entry")
		List<StatisticsItemStatus> statuses;
		
		public int getStatus(BuildStateEnum buildStateEnum, int status) {
			for (StatisticsItemStatus statisticsItemStatus : statuses) {
				if (buildStateEnum != null && buildStateEnum.equals(statisticsItemStatus.state) && status == statisticsItemStatus.statusCode) {
					return statisticsItemStatus.count;
				} else if (buildStateEnum == null || statisticsItemStatus.state == null || statisticsItemStatus.statusCode == null) {
					Loggers.SERVER.debug(
							String.format("StatisticsItem :: Unexpected null item. [buildStateEnum=%s,statisticsItemStatus.state=%s,statisticsItemStatus.statusCode=%s",
									buildStateEnum, statisticsItemStatus.state, statisticsItemStatus.statusCode));
				}
			}
			return 0;
		}
		public void putStatus(BuildStateEnum buildStateEnum, int status, int statusCount) {
			boolean found = false;
			for (StatisticsItemStatus statisticsItemStatus : statuses) {
				if (buildStateEnum != null && buildStateEnum.equals(statisticsItemStatus.state) && status == statisticsItemStatus.statusCode) {
					statisticsItemStatus.count = statusCount;
					found = true;
				} else if (buildStateEnum == null || statisticsItemStatus.state == null || statisticsItemStatus.statusCode == null) {
					Loggers.SERVER.debug(
							String.format("StatisticsItem :: Unexpected null item. [buildStateEnum=%s,statisticsItemStatus.state=%s,statisticsItemStatus.statusCode=%s",
									buildStateEnum, statisticsItemStatus.state, statisticsItemStatus.statusCode));
				}
			}
			if (!found) {
				statuses.add(new StatisticsItemStatus(buildStateEnum, status, statusCount));
			}
		} 
	}
	
	@Data @AllArgsConstructor @NoArgsConstructor
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StatisticsItemStatus {
		@XmlElement(name = "state", defaultValue = "BUILD_FINISHED")
		BuildStateEnum state = BuildStateEnum.BUILD_FINISHED;
		@XmlElement(name = "key")
		Integer statusCode;
		@XmlElement(name = "value")
		Integer count;
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
