package webhook.teamcity.diagnostics;

import webhook.teamcity.exception.DslDiagnotisticsException;

public interface MavenFileResolver {

	MavenPom getPom(String groupId, String artifactId, String version, String snapshotVersion) throws DslDiagnotisticsException;

	MavenMetaData getMetaData(String groupId, String artifactId, String version) throws DslDiagnotisticsException;

}
