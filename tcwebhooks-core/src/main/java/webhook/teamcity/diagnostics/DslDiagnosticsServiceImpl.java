package webhook.teamcity.diagnostics;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.tuple.Pair;

import lombok.AllArgsConstructor;
import webhook.teamcity.Loggers;
import webhook.teamcity.diagnostics.MavenMetaData.SnapshotVersion;
import webhook.teamcity.diagnostics.MavenPom.Dependency;
import webhook.teamcity.exception.DslDiagnotisticsException;

@AllArgsConstructor
public class DslDiagnosticsServiceImpl implements DslDiagnosticsService {
	private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withZone(ZoneId.of("UTC"));;
	
	private MavenFileResolver mavenFileResolver;

	@Override
	public DslDiagnosticsResult getDiagnostics() throws DslDiagnotisticsException {
		DslDiagnosticsResult result = new DslDiagnosticsResult();
		Pair<String, MavenPom> teamcityDslPom = getPom(PLUGINS_ARTIFACT_ID);
		if (teamcityDslPom != null) {
			Loggers.SERVER.debug("Fetched POM for " + PLUGINS_ARTIFACT_ID);
			Dependency tcWebHookDsl = getWebHooksDependency(teamcityDslPom.getRight());
			result.setLastTeamCityDslUpdate(teamcityDslPom.getLeft());
			if (tcWebHookDsl != null) {
				result.setTcWebHooksDslFound(true);
				Pair<String, MavenPom> tcWebHooksPom = getPom(TCWEBHOOKS_ARTIFACT_ID);
				if (tcWebHooksPom != null) {
					result.setLastTcWebHooksDslUpdate(tcWebHooksPom.getLeft());
					result.setTcWebHooksDslVersion(tcWebHooksPom.getRight().getVersion());
					result.setLastTcWebHooksUpdateFriendly(calculateTimeAgoWithPeriodAndDuration(LocalDateTime.parse(tcWebHooksPom.getLeft(), DATE_TIME_FORMATTER)));
				}
			}
		}
		return result;
	}
	
	@Override
	public Dependency getWebHooksDependency(MavenPom pom) {
		return pom.getDependencies()
				.stream()
				.filter(d -> d.getArtifactId().equals(TCWEBHOOKS_ARTIFACT_ID))
				.findFirst().orElse(null);
	}
	
	@Override
	public Pair<String, MavenPom> getPom(String artifactId) throws DslDiagnotisticsException {
		MavenMetaData metaData = mavenFileResolver.getMetaData(
				TEAMCITY_GROUP_ID, 
				artifactId, 
				SNAPSHOT_VERSION);
		SnapshotVersion pomVersion = getSnapshotVersion("pom", metaData);
		if (pomVersion != null) {
			Loggers.SERVER.debug("Fetched metadata for " + TEAMCITY_GROUP_ID);
			return Pair.of(pomVersion.updated,mavenFileResolver.getPom(
					TEAMCITY_GROUP_ID, 
					artifactId, 
					SNAPSHOT_VERSION,
					pomVersion.getValue()
				));
		} else {
			return null;
		}
	}
	
	private SnapshotVersion getSnapshotVersion(String ext, MavenMetaData metaData) {
		return metaData.getVersioning().getSnapshotVersions()
				.stream()
				.filter(m -> m.getExtension().equals(ext))
				.findFirst().orElse(null);
	}
	
	static String calculateTimeAgoWithPeriodAndDuration(LocalDateTime pastTime) {
		Period period = Period.between(pastTime.toLocalDate(), LocalDate.now());
		Duration duration = Duration.between(pastTime, LocalDateTime.now());
		if (period.getYears() != 0) {
			return pluraliseAgo(period.getYears(), "year");
		} else if (period.getMonths() != 0) {
			return pluraliseAgo(period.getMonths(), "month");
		} else if (period.getDays() != 0) {
			return  pluraliseAgo(period.getDays(),"day");
		} else if (duration.toHours() != 0) {
			return pluraliseAgo(duration.toHours(), "hour");
		} else if (duration.toMinutes() != 0) {
			return pluraliseAgo(duration.toMinutes(), "minute");
		} else if (duration.getSeconds() != 0) {
			return pluraliseAgo(duration.getSeconds(), "second");
		} else {
			return "moments ago";
		}
	}
	
	static String pluraliseAgo(int period, String unit) {
		return String.format("%s %s%s ago", 
				period,
				unit,
				period == 1 ? "" : "s");
	}
	static String pluraliseAgo(long period, String unit) {
		return String.format("%s %s%s ago", 
				period,
				unit,
				period == 1 ? "" : "s");
	}
}
