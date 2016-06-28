package webhook.teamcity;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import jetbrains.buildServer.AgentRestrictor;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.StatusDescriptor;
import jetbrains.buildServer.issueTracker.Issue;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.DownloadedArtifacts;
import jetbrains.buildServer.serverSide.RepositoryVersion;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.serverSide.impl.RunningBuildState;
import jetbrains.buildServer.serverSide.userChanges.CanceledInfo;
import jetbrains.buildServer.serverSide.userChanges.PersonalChangeDescriptor;
import jetbrains.buildServer.serverSide.vcs.VcsLabel;
import jetbrains.buildServer.tests.TestInfo;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserSet;
import jetbrains.buildServer.vcs.CheckoutRules;
import jetbrains.buildServer.vcs.FilteredVcsChange;
import jetbrains.buildServer.vcs.RelationType;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SVcsRoot;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsFileModification;
import jetbrains.buildServer.vcs.VcsModification;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.vcs.VcsRootInstanceEntry;
import jetbrains.buildServer.vcs.VcsRootNotFoundException;
import jetbrains.vcs.api.VcsService;

public class MockSRunningBuild implements SRunningBuild {

	private SBuildType sBuildType;
	private MockSBuildAgent sBuildAgent;
	private String buildNumber;
	private MockTriggeredBy triggeredBy;
	private Status status;
	private String statusText;
	private long buildId = 123456;
	private Map<String,String> buildParameters = new TreeMap<>();
	private ParametersProvider parameterProvider;
	private List<SVcsModification> modifications;

	public MockSRunningBuild(SBuildType buildType, String triggeredBy, Status status, String statusText, String buildNumber) {
		this.sBuildType = buildType;
		this.sBuildAgent = new MockSBuildAgent("Test Agent", 
									"agent.hostname.domain.name", 
									"192.168.0.1",
									1, 
									"Linux, version 2.6.27.21" );
		sBuildAgent.setRunningBuild(this);
		this.triggeredBy = new MockTriggeredBy(triggeredBy);
		this.status = status;
		this.statusText = statusText;
		this.buildNumber = buildNumber;
		this.buildParameters.put("test.thing1","thing1");
		this.buildParameters.put("test.thing2","thing2");
		this.buildParameters.put("test.thing3","thing3");
		this.buildParameters.put("webhook.test.thing","This is a webhook build property, so should get to the webhook");
		this.parameterProvider = new ParametersProvider() {
			
			@Override
			public int size() {
				return buildParameters.size();
			}
			
			@Override
			public Map<String, String> getAll() {
				return buildParameters;
			}
			
			@Override
			public String get(String key) {
				return buildParameters.get(key);
			}
		};
		
		this.modifications = new ArrayList<SVcsModification>();
		this.modifications.add(new SVcsModification() {
			
			@Override
			public Date getVcsDate() {
				return new Date();
			}
			
			@Override
			public String getUserName() {
				return "some_user_name";
			}
			
			@Override
			public String getDescription() {
				return "This is a mock description for a Mock change";
			}
			
			@Override
			public int compareTo(VcsModification o) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean isPersonal() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String getVersionControlName() {
				// TODO Auto-generated method stub
				return "A VersionControlName";
			}
			
			@Override
			public String getVersion() {
				return "12345";
			}
			
			@Override
			public long getId() {
				return 0;
			}
			
			@Override
			public String getDisplayVersion() {
				return "12345";
			}
			
			@Override
			public List<VcsFileModification> getChanges() {
				List<VcsFileModification> mods = new ArrayList<VcsFileModification>();
				mods.add(new VcsFileModification() {
					
					@Override
					public Type getType() {
						return Type.CHANGED ;
					}
					
					@Override
					public String getRelativeFileName() {
						return "Some/File.txt";
					}
					
					@Override
					public String getFileName() {
						return "/Some/File.txt";
					}
					
					@Override
					public String getChangeTypeName() {
						return Type.CHANGED.name() ;
					}
					
					@Override
					public String getBeforeChangeRevisionNumber() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getAfterChangeRevisionNumber() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public byte[] getContentBefore() throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public byte[] getContentAfter() throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
				});
				return mods;
			}
			
			@Override
			public int getChangeCount() {
				return 1;
			}
			
			@Override
			public VcsFileModification findChangeByPath(String fileName) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean isHasRelatedIssues() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isCommitter(User user) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasRelation(SBuildType buildType, RelationType relation) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public VcsRootInstance getVcsRoot() throws VcsRootNotFoundException {
				// TODO Auto-generated method stub
				return new VcsRootInstance() {
					
					@Override
					public boolean isCurrentVersionExpensive() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public String getVersionDisplayName(String version) throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public <T extends VcsService> T getService(Class<T> serviceClass,
							CheckoutRules checkoutRules) throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public <T extends VcsService> T getService(Class<T> serviceClass)
							throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public long getParentId() {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public long getCheckoutPropertiesHash(boolean serverSideCheckout) {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public <T extends VcsService> T findService(Class<T> serviceClass,
							CheckoutRules checkoutRules) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getVcsDisplayName() {
						// TODO Auto-generated method stub
						return "Test VCS";
					}
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String describe(boolean verbose) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getVcsName() {
						// TODO Auto-generated method stub
						return "Test VCS";
					}
					
					@Override
					public String getProperty(String propertyName, String defaultValue) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getProperty(String propertyName) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Map<String, String> getProperties() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getName() {
						return "Test VCS";
					}
					
					@Override
					public long getId() {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public boolean isDagBased() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public Map<SBuildType, CheckoutRules> getUsages() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public SVcsRoot getParent() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public RepositoryVersion getLastUsedRevision() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public RepositoryVersion getCurrentRevision() throws VcsException {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
			
			@Override
			public Collection<SProject> getRelatedProjects() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<Issue> getRelatedIssues() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<SBuildType> getRelatedConfigurations() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Date getRegistrationDate() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public PersonalChangeDescriptor getPersonalChangeInfo() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<String> getParentRevisions() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<SVcsModification> getParentModifications() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getNumberOfRelatedConfigurations() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Map<SBuildType, SBuild> getFirstBuilds() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<FilteredVcsChange> getFilteredChanges(
					BuildPromotion buildPromotion) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<FilteredVcsChange> getFilteredChanges(SBuild build) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<FilteredVcsChange> getFilteredChanges(SBuildType buildType) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<SVcsModification> getDuplicates() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<SUser> getCommitters() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<Long> getCommitterIds() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<SBuildType, RelationType> getBuildTypeRelations() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String, String> getAttributes() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	public void addBuildMessage(BuildMessage1 arg0) {
		// TODO Auto-generated method stub

	}

	public void addBuildMessages(List<BuildMessage1> arg0) {
		// TODO Auto-generated method stub

	}

	public SBuildAgent getAgent() {
		return sBuildAgent;
	}

	public String getAgentAccessCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCompletedPercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCurrentPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getLastBuildActivityTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getQueuedAgentId() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSignature() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTimeSpentSinceLastBuildActivity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isProbablyHanging() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setBuildNumber(String arg0) {
		this.buildNumber = arg0;
	}

	public void setBuildStatus(Status arg0) {
		this.status = arg0;
	}

	public void setInterrupted(RunningBuildState arg0, User arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	public void setSignature(int arg0) {
		// TODO Auto-generated method stub

	}

	public void stop(User arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public Date convertToAgentTime(Date arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date convertToServerTime(Date arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public File getArtifactsDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	public Comment getBuildComment() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBuildDescription() {
		return this.sBuildType.getDescription();
	}

	public BuildLog getBuildLog() {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildStatistics getBuildStatistics(BuildStatisticsOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuildType getBuildType() {
		return this.sBuildType;
	}

	public List<SVcsModification> getChanges(SelectPrevBuildPolicy arg0,
			boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getClientStartDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public UserSet<SUser> getCommitters(SelectPrevBuildPolicy arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SVcsModification> getContainingChanges() {
		return modifications;
	}

	public DownloadedArtifacts getDownloadedArtifacts() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getFileContent(String arg0) throws VcsException {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildStatistics getFullStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VcsLabel> getLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	public SUser getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	public DownloadedArtifacts getProvidedArtifacts() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getQueuedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestor() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<BuildRevision> getRevisions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getServerStartDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public ShortStatistics getShortStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getTags() {
		List<String> tags = new ArrayList<String>();
		tags.add("someTag");
		return tags;
	}

	public TriggeredBy getTriggeredBy() {
		// TODO Auto-generated method stub
		return this.triggeredBy;
	}

	public ValueResolver getValueResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isArtifactsExists() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOutOfChangesSequence() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPinned() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isResponsibleNeeded() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUsedByOtherBuilds() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setBuildComment(User arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void setTags(List<String> arg0) {
		// TODO Auto-generated method stub

	}

	public String getAgentName() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getBuildId() {
		return this.buildId ;
	}

	public String getBuildNumber() {
		return this.buildNumber;
	}

	public Status getBuildStatus() {
		return this.status;
	}

	public String getBuildTypeId() {
		return this.sBuildType.getBuildTypeId();
	}

	public String getBuildTypeName() {
		return this.sBuildType.getName();
	}

	public CanceledInfo getCanceledInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getFinishDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getLogMessages(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProjectId() {
		// TODO Auto-generated method stub
		return this.getBuildType().getProjectId();
	}

	public Date getStartDate() {
		return new Date();
	}

	public StatusDescriptor getStatusDescriptor() {
		return new StatusDescriptor(this.status, this.statusText);
	}

	public List<TestInfo> getTestMessages(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPersonal() {
		// TODO Auto-generated method stub
		return false;
	}

	public BuildPromotion getBuildPromotion() {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuild getSequenceBuild() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getDurationEstimate() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getDurationOvertime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getElapsedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getEstimationForTimeLeft() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<SArtifactDependency> getArtifactDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildArtifacts getArtifacts(BuildArtifactsViewMode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getBuildOwnParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFirstInternalError() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFirstInternalErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRawBuildNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Issue> getRelatedIssues() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInternalError() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStartedOnAgent() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setTags(User arg0, List<String> arg1) {
		// TODO Auto-generated method stub
		
	}

	public List<String> getCompilationErrorMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TestInfo> getTestMessages(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ParametersProvider getParametersProvider() {
		return this.parameterProvider;
	}

	public boolean isOutdated() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<VcsRootInstanceEntry> getVcsRootEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeZone getClientTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHasInternalArtifactsOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHasRelatedIssues() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AgentRestrictor getQueuedAgentRestrictor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBuildProblem(BuildProblemData arg0) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public BuildProblemData addUserBuildProblem(User arg0, String arg1) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Branch getBranch() {
		// TODO Auto-generated method stub
		return new MockBranch();
	}

	@Override
	public List<BuildProblemData> getFailureReasons() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SFinishedBuild getPreviousFinished() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasBuildProblemOfType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public void muteBuildProblems(User arg0, boolean arg1, String arg2) {
//		// TODO Auto-generated method stub
//		
//	}

	// From 8.0
	
	@Override
	public BigDecimal getStatisticValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, BigDecimal> getStatisticValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBuildTypeExternalId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProjectExternalId() {
		// TODO Auto-generated method stub
		return null;
	}
	// From TC 9.1

	@Override
	public BuildProblemData addUserBuildProblem(SUser arg0, String arg1) {
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
	public void muteBuildProblems(SUser arg0, boolean arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinishedBuild getRecentlyFinishedBuild() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
