package webhook.teamcity.statistics;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StatisticsEntity {
	@XmlElement
	StatisticsSnapshot statisticsSnapshot;

	@XmlAttribute
	LocalDateTime lastUpdated;
	
	@XmlAttribute
	Boolean reported;
	
	public StatisticsEntity at(LocalDateTime now) {
		this.lastUpdated = now;
		return this;
	}
	
	StatisticsEntity withSnapshot(StatisticsSnapshot statisticsSnapshot) {
		this.statisticsSnapshot = statisticsSnapshot;
		return this;
	}

}
