package webhook.teamcity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.BuildTypeStatusDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.serverSide.dependency.CyclicDependencyFoundException;
import jetbrains.buildServer.serverSide.dependency.Dependency;
import jetbrains.buildServer.serverSide.dependency.Dependent;
import jetbrains.buildServer.serverSide.identifiers.DuplicateExternalIdException;
import jetbrains.buildServer.serverSide.parameters.types.TypedValue;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.util.Option;
import jetbrains.buildServer.vcs.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import webhook.testframework.WebHookMockingFramework;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("rawtypes")
public class MockSBuildType implements SBuildType {

    private SProject project;
    private String name;
    private ResponsibilityInfo responsibiltyInfo;
    private String artifactPaths;
    private File artifactsDirectory;
    private RunType runType = new MockRunType();
    private String description;
    private String buildTypeId;
    private WebHookMockingFramework mockingFramework;


    public MockSBuildType(String name, String description, String buildTypeId) {
        this.name = name;
        this.description = description;
        this.buildTypeId = buildTypeId;
    }

    public void setMockingFrameworkInstance(WebHookMockingFramework mockingFramework) {
        this.mockingFramework = mockingFramework;
    }

    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    public void addBuildParameter(Parameter arg0) {
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

    @Override
    public void forceCheckingForChanges(@NotNull OperationRequestor operationRequestor) {

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
        return this.runType;
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
        return this.mockingFramework.getMockedBuildHistory();
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

    @SuppressWarnings("deprecation")
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

    public void removeRequirement(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRequirement(Requirement requirement) {

    }

    public boolean removeVcsRoot(SVcsRoot arg0) {
        return false;
        // TODO Auto-generated method stub

    }

    public void setArtifactDependencies(List<SArtifactDependency> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addArtifactDependency(@NotNull SArtifactDependency sArtifactDependency) {

    }

    @Override
    public void removeArtifactDependency(@NotNull SArtifactDependency sArtifactDependency) {

    }

    public void setArtifactPaths(String arg0) {
        // TODO Auto-generated method stub

    }

    public void setCheckoutDirectory(String arg0) {
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

    public Collection<VcsRoot> setLabelingRoots(List<VcsRoot> arg0) {
        return null;
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("deprecation")
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
        return this.name;
    }

    public String getProjectId() {
        return this.project.getProjectId();
    }

    public String getProjectName() {
        return this.project.getName();
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

    public Map<String, String> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public String getParameterValue(@NotNull String s) {
        return null;
    }

    public void removeResponsible(boolean arg0, User arg1, String arg2,
                                  User arg3) {
        // TODO Auto-generated method stub

    }

    public void setResponsible(User arg0, String arg1, User arg2) {
        // TODO Auto-generated method stub

    }

    public Collection<String> getRunnerTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addBuildFeature(SBuildFeatureDescriptor arg0) {
        // TODO Auto-generated method stub

    }

    public SBuildFeatureDescriptor addBuildFeature(String arg0,
                                                   Map<String, String> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addBuildRunner(SBuildRunnerDescriptor arg0) {
        // TODO Auto-generated method stub

    }

    public SBuildRunnerDescriptor addBuildRunner(String arg0, String arg1,
                                                 Map<String, String> arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean addBuildTrigger(BuildTriggerDescriptor arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addConfigParameter(Parameter arg0) {
        // TODO Auto-generated method stub

    }

    public void applyRunnersOrder(String[] arg0) {
        // TODO Auto-generated method stub

    }

    public SBuildRunnerDescriptor findBuildRunnerById(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String findRunnerParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<SBuildFeatureDescriptor> getBuildFeatures() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBuildNumberPattern() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<SBuildRunnerDescriptor> getBuildRunners() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<BuildTriggerDescriptor> getBuildTriggersCollection() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> getConfigParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Parameter> getConfigParametersCollection() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Parameter> getRunParametersCollection() {
        // TODO Auto-generated method stub
        return null;
    }

    public BuildTypeTemplate getTemplate() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTemplateId() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getUndefinedParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isTemplateBased() {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeAllBuildRunners() {
        // TODO Auto-generated method stub

    }

    public SBuildFeatureDescriptor removeBuildFeature(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public SBuildRunnerDescriptor removeBuildRunner(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean removeBuildTrigger(BuildTriggerDescriptor arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeConfigParameter(String arg0) {
        // TODO Auto-generated method stub

    }

    public boolean replaceInValues(String arg0, String arg1)
            throws PatternSyntaxException {
        // TODO Auto-generated method stub
        return false;
    }

    public void setBuildNumberPattern(String arg0) {
        // TODO Auto-generated method stub

    }

    public boolean updateBuildFeature(String arg0, String arg1,
                                      Map<String, String> arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean updateBuildRunner(String arg0, String arg1, String arg2,
                                     Map<String, String> arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addParameter(Parameter arg0) {
        // TODO Auto-generated method stub

    }

    public Collection<Parameter> getParametersCollection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public Parameter getParameter(@NotNull String s) {
        return null;
    }

    public void removeParameter(String arg0) {
        // TODO Auto-generated method stub

    }

    public CompatibilityResult getAgentCompatibility(AgentDescription arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<SBuildAgent, CompatibilityResult> getCompatibilityMap() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<SBuildType> getChildDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    public ParametersProvider getParametersProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    public void attachToTemplate(BuildTypeTemplate arg0, boolean arg1)
            throws InvalidVcsRootScopeException {
        // TODO Auto-generated method stub

    }

    public void detachFromTemplate() {
        // TODO Auto-generated method stub

    }

    public SBuildRunnerDescriptor findBuildRunnerByType(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public CustomDataStorage getCustomDataStorage(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getExtendedName() {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getFileContent(String arg0) throws VcsException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Dependency> getOwnDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    public Comment getPauseComment() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<SUser> getPendingChangesCommitters() {
        // TODO Auto-generated method stub
        return null;
    }

    public ResolvedSettings getResolvedSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<VcsRootInstanceEntry> getVcsRootInstanceEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    public VcsRootInstance getVcsRootInstanceForParent(SVcsRoot arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public VcsRootInstanceEntry getVcsRootInstanceEntryForParent(@NotNull SVcsRoot sVcsRoot) {
        return null;
    }

    public List<VcsRootInstance> getVcsRootInstances() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getVcsSettingsHash() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getVcsSettingsHash(List<VcsRootInstanceEntry> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public PathMapping mapVcsPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void moveToProject(SProject arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    public void persist() throws PersistFailedException {
        // TODO Auto-generated method stub

    }

    public void setPaused(boolean arg0, User arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    public boolean addVcsRoot(SVcsRoot arg0)
            throws InvalidVcsRootScopeException, VcsRootNotFoundException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setCheckoutRules(VcsRoot arg0, CheckoutRules arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public Collection<SBuildAgent> getCompatibleAgents() {
        // TODO Auto-generated method stub
        return null;
    }

    // From 7.1

    @Override
    public BuildTriggerDescriptor addBuildTrigger(String arg0,
                                                  Map<String, String> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BuildTriggerDescriptor findTriggerById(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEnabled(String arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    @NotNull
    @Override
    public <T> TypedValue<T> getTypedOption(@NotNull Option<T> option) {
        return null;
    }

    @Override
    public void setEnabled(String arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean updateBuildTrigger(String arg0, String arg1,
                                      Map<String, String> arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    // From 8.0

    @Override
    public File getConfigurationFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExternalId() {
        return this.name.replace(" ", "");
    }

    @Override
    public String getInternalId() {
        return this.buildTypeId;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setExternalId(String arg0) throws InvalidIdentifierException,
            DuplicateExternalIdException {
        // TODO Auto-generated method stub

    }

    @Override
    public SPersistentEntity getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BuildTypeStatusDescriptor getStatusDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProjectExternalId() {
        return TeamCityIdResolver.getExternalProjectIdOrNull(this.project);
    }

    @Override
    public SBuildRunnerDescriptor addBuildRunner(BuildRunnerDescriptor arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getOwnParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public Parameter getOwnParameter(@NotNull String s) {
        return null;
    }

    @Override
    public Collection<Parameter> getOwnParametersCollection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attachToTemplate(BuildTypeTemplate arg0)
            throws CannotAttachToTemplateException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean belongsTo(SProject arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getExtendedFullName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<VcsRootEntry> getOwnVcsRootEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void moveToProject(SProject arg0)
            throws InvalidVcsRootScopeException {
        // TODO Auto-generated method stub
    }

    // From TC 9.1

    @Override
    public void setExternalId(ConfigAction arg0, String arg1)
            throws InvalidIdentifierException, DuplicateExternalIdException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getConfigId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getConfigurationFile(File arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persist(ConfigAction arg0) throws PersistFailedException {
        // TODO Auto-generated method stub

    }

    @Override
    public SBuildFeatureDescriptor findBuildFeatureById(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<SBuildFeatureDescriptor> getBuildFeaturesOfType(
            String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isTemplateAccessible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean replaceInValues(Pattern arg0, String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean textValueMatches(Pattern arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T getOptionDefaultValue(Option<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public <T> T getDeclaredOption(Option<T> option) {
        return null;
    }

    @Override
    public Collection<Option> getOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Option> getOwnOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVcsRootsHash() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void moveToProject(ConfigAction arg0, SProject arg1)
            throws InvalidVcsRootScopeException {
        // TODO Auto-generated method stub

    }


}
