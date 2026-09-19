package webhook.teamcity.diagnostics;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/*
 
 <metadata modelVersion="1.1.0">
  <groupId>org.jetbrains.teamcity</groupId>
  <artifactId>configs-dsl-kotlin-tcwebhooks-latest</artifactId>
  <version>1.0-SNAPSHOT</version>
  <versioning>
    <snapshot>
      <timestamp>20260811.232606</timestamp>
      <buildNumber>1</buildNumber>
    </snapshot>
    <lastUpdated>20260811232606</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>jar</extension>
        <value>1.0-20260811.232606-1</value>
        <updated>20260811232606</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>pom</extension>
        <value>1.0-20260811.232606-1</value>
        <updated>20260811232606</updated>
      </snapshotVersion>
    </snapshotVersions>
  </versioning>
</metadata>
 
 */

@ToString
@XmlRootElement(name = "metadata")
public class MavenMetaData {
	
	@XmlElement
	@Getter
	public String groupId;
	
	@XmlElement
	@Getter
	public String artifactId;
	
	@XmlElement
	@Getter
	public String version;
	
	@XmlElement(name = "versioning") @Getter
	Versioning versioning;
	
	@XmlRootElement(name = "versioning")
	@XmlType(name = "versioning", propOrder = { "snapshot", "lastUpdated", "snapshotVersions"})
	@Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class Versioning {
		@XmlElement
		@Getter
		public Snapshot snapshot;
		
		@XmlElement
		@Getter
		public Snapshot lastUpdated;
		
		@XmlElement(name = "snapshotVersion") @XmlElementWrapper(name = "snapshotVersions") @Getter
		List<SnapshotVersion> snapshotVersions;
	}
	
	/*
	<snapshot>
      <timestamp>20260811.232606</timestamp>
      <buildNumber>1</buildNumber>
    </snapshot>
	 */
	
	@XmlRootElement(name = "snapshot")
	@XmlType(name = "snapshot")
	@Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class Snapshot {
		@XmlElement
		@Getter
		public String timestamp;
		
		@XmlElement
		@Getter
		public String buildNumber;
	}
	
	/*
	    <snapshotVersions>
	      <snapshotVersion>
	        <extension>jar</extension>
	        <value>1.0-20260811.232606-1</value>
	        <updated>20260811232606</updated>
	      </snapshotVersion>
	      <snapshotVersion>
	        <extension>pom</extension>
	        <value>1.0-20260811.232606-1</value>
	        <updated>20260811232606</updated>
	      </snapshotVersion>
	    </snapshotVersions>
	 */
	
	@XmlRootElement(name = "snapshotVersion")
	@XmlType(name = "snapshotVersion")
	@Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class SnapshotVersion {
		@XmlElement
		@Getter
		public String extension;
		
		@XmlElement
		@Getter
		public String value;
		
		@XmlElement
		@Getter
		public String updated;
	}
}
