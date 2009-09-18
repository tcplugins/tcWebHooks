package webhook.teamcity;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTrigger;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.AgentCompatibility;
import jetbrains.buildServer.serverSide.BuildNumbers;
import jetbrains.buildServer.serverSide.BuildTypeRenamingFailedException;
import jetbrains.buildServer.serverSide.DuplicateBuildTypeNameException;
import jetbrains.buildServer.serverSide.InvalidVcsRootScopeException;
import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.dependency.CyclicDependencyFoundException;
import jetbrains.buildServer.serverSide.dependency.Dependency;
import jetbrains.buildServer.serverSide.dependency.Dependent;
import jetbrains.buildServer.serverSide.vcs.VcsLabelingSettings.LabelingType;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.util.Option;
import jetbrains.buildServer.vcs.CheckoutRules;
import jetbrains.buildServer.vcs.FilteredVcsChange;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SVcsRoot;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.VcsRootEntry;
import jetbrains.buildServer.vcs.VcsRootNotFoundException;

public class MockSBuildType implements SBuildType {
	
	private SProject project;
	private String name;
	private ResponsibilityInfo responsibiltyInfo;
	private String artifactPaths;
	private File artifactsDirectory;
	private RunType runType = new MockRunType();
	private String description;
	private String buildTypeId;
	
	public MockSBuildType(String name, String description, String buildTypeId) {
		this.name = name;
		this.description = description;
		this.buildTypeId = buildTypeId;
	}

	public void addBuildParameter(Parameter arg0) {
		// TODO Auto-generated method stub

	}

	public void addBuildTrigger(BuildTrigger arg0) {
		// TODO Auto-generated method stub

	}

	public void addRequirement(Requirement arg0) {
		// TODO Auto-generated method stub

	}

	public void addRunParameter(Parameter arg0) {
		// TODO Auto-generated method stub

	}

	public SQueuedBuild addToQueue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SQueuedBuild addToQueue(BuildAgent arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addVcsRoot(SVcsRoot arg0) throws InvalidVcsRootScopeException,
			VcsRootNotFoundException {
		// TODO Auto-generated method stub

	}

	public void clearRunParameters() {
		// TODO Auto-generated method stub

	}

	public boolean containsVcsRoot(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void forceCheckingForChanges() {
		// TODO Auto-generated method stub

	}

	public List<AgentCompatibility> getAgentCompatibilities() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends SBuildAgent> AgentCompatibility getAgentCompatibility(
			T arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SBuildAgent> getAgentsWhereBuildConfigurationBuilt() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SArtifactDependency> getArtifactDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getArtifactPaths() {
		return this.artifactPaths;
	}

	public File getArtifactsDirectory() {
		return this.artifactsDirectory;
	}

	public List<SBuildType> getArtifactsReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildNumbers getBuildNumbers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getBuildParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Parameter> getBuildParametersCollection() {
		// TODO Auto-generated method stub
		return null;
	}

	public RunType getBuildRunner() {
		return this.runType ;
	}

	public Collection<BuildTrigger> getBuildTriggers() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends BuildAgent> List<T> getCanRunAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends BuildAgent> List<T> getCanRunAndCompatibleAgents(
			boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCheckoutDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	public CheckoutRules getCheckoutRules(VcsRoot arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends BuildAgent> Collection<T> getCompatibleAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getExecutionTimeoutMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<FilteredVcsChange> getFilteredChanges(SVcsModification arg0,
			SBuild arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SFinishedBuild> getHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SFinishedBuild> getHistory(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SFinishedBuild> getHistory(User arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SFinishedBuild> getHistory(User arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SFinishedBuild> getHistoryFull(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Requirement> getImplicitRequirements() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabelPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VcsRoot> getLabelingRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	public LabelingType getLabelingType() {
		// TODO Auto-generated method stub
		return null;
	}

	public SFinishedBuild getLastChangesFinished() {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuild getLastChangesStartedBuild() {
		// TODO Auto-generated method stub
		return null;
	}

	public SFinishedBuild getLastChangesSuccessfullyFinished() {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuild getLastFinished() {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuild getLastStartedBuild() {
		// TODO Auto-generated method stub
		return null;
	}

	public SFinishedBuild getLastSuccessfullyFinished() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaximumNumberOfBuilds() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<SVcsModification> getModificationsSinceLastSuccessful() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfArtifactReferences() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<SVcsModification> getPendingChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	public SProject getProject() {
		return this.project;
	}

	public List<SQueuedBuild> getQueuedBuilds(User arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Requirement> getRequirements() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getRunParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Requirement> getRunTypeRequirements() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SRunningBuild> getRunningBuilds() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SRunningBuild> getRunningBuilds(User arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SBuildType> getTriggeredBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public ValueResolver getValueResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VcsRootEntry> getVcsRootEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SVcsRoot> getVcsRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVcsSettingsHash(List<VcsRootEntry> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAllowExternalStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCleanBuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShouldFailBuildIfTestsFailed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void releaseSources() {
		// TODO Auto-generated method stub

	}

	public void releaseSources(SBuildAgent arg0) {
		// TODO Auto-generated method stub

	}

	public void removeBuildParameter(String arg0) {
		// TODO Auto-generated method stub

	}

	public void removeBuildTrigger(BuildTrigger arg0) {
		// TODO Auto-generated method stub

	}

	public void removeRequirement(String arg0) {
		// TODO Auto-generated method stub

	}

	public void removeVcsRoot(SVcsRoot arg0) {
		// TODO Auto-generated method stub

	}

	public void setArtifactDependencies(List<SArtifactDependency> arg0) {
		// TODO Auto-generated method stub

	}

	public void setArtifactPaths(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setCheckoutDirectory(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setCheckoutRules(VcsRoot arg0, CheckoutRules arg1) {
		// TODO Auto-generated method stub

	}

	public void setCheckoutType(CheckoutType arg0) {
		// TODO Auto-generated method stub

	}

	public void setDescription(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setLabelPattern(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setLabelingRoots(List<VcsRoot> arg0) {
		// TODO Auto-generated method stub

	}

	public void setLabelingType(LabelingType arg0) {
		// TODO Auto-generated method stub

	}

	public void setName(String arg0) throws DuplicateBuildTypeNameException,
			BuildTypeRenamingFailedException {
		this.name = arg0;
	}

	public void setPaused(boolean arg0, User arg1) {
		// TODO Auto-generated method stub

	}

	public void setRunType(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setSleepingChangeBrowserPeriod(int arg0) {
		// TODO Auto-generated method stub

	}

	public Build getBuildByBuildNumber(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBuildParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberQueued() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ResponsibilityInfo getResponsibilityInfo() {
		// TODO Auto-generated method stub
		return this.responsibiltyInfo;
	}

	public String getRunParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInQueue() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeResponsible(boolean arg0, User arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	public void setResponsible(User arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public String getBuildTypeId() {
		return this.buildTypeId;
	}

	public CheckoutType getCheckoutType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		return this.description;
	}

	public String getFullName() {
		return this.project.getName() + " :: " + this.name;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRunType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPersonal() {
		// TODO Auto-generated method stub
		return false;
	}

	public int compareTo(BuildTypeDescriptor o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Option[] getChangedOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getOption(Option<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> void setOption(Option<T> arg0, T arg1) {
		// TODO Auto-generated method stub

	}

	public void addDependency(Dependency arg0)
			throws CyclicDependencyFoundException {
		// TODO Auto-generated method stub

	}

	public List<Dependency> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SBuildType> getDependencyReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfDependencyReferences() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean intersectsWith(Dependent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeDependency(Dependency arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void writeTo(Element arg0) {
		// TODO Auto-generated method stub

	}

	public void setProject(SProject project2) {
		this.project = project2;
	}

}
