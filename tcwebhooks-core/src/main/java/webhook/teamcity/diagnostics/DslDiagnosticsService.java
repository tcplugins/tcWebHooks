package webhook.teamcity.diagnostics;

import org.apache.commons.lang3.tuple.Pair;

import webhook.teamcity.diagnostics.MavenPom.Dependency;
import webhook.teamcity.exception.DslDiagnotisticsException;

public interface DslDiagnosticsService {
	
	public static final String SNAPSHOT_VERSION = "1.0-SNAPSHOT";
	public static final String TEAMCITY_GROUP_ID = "org.jetbrains.teamcity";
	public static final String PLUGINS_ARTIFACT_ID = "configs-dsl-kotlin-plugins-latest";
	public static final String TCWEBHOOKS_ARTIFACT_ID = "configs-dsl-kotlin-tcwebhooks-latest";
	
	DslDiagnosticsResult getDiagnostics() throws DslDiagnotisticsException;
	
	Dependency getWebHooksDependency(MavenPom pom);
	Pair<String, MavenPom> getPom(String artifactId) throws DslDiagnotisticsException;
}
