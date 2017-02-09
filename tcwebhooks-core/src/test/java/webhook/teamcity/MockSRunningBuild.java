package webhook.teamcity;

import jetbrains.buildServer.AgentRestrictor;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.StatusDescriptor;
import jetbrains.buildServer.groups.UserGroup;
import jetbrains.buildServer.issueTracker.Issue;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.notification.DuplicateNotificationRuleException;
import jetbrains.buildServer.notification.NotificationRule;
import jetbrains.buildServer.notification.NotificationRulesHolder;
import jetbrains.buildServer.notification.WatchedBuilds;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.auth.*;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.serverSide.impl.RunningBuildState;
import jetbrains.buildServer.serverSide.userChanges.CanceledInfo;
import jetbrains.buildServer.serverSide.userChanges.PersonalChangeDescriptor;
import jetbrains.buildServer.serverSide.vcs.VcsLabel;
import jetbrains.buildServer.tests.TestInfo;
import jetbrains.buildServer.users.*;
import jetbrains.buildServer.vcs.*;
import jetbrains.vcs.api.VcsService;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

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

					@NotNull
					@Override
					public VcsRootStatus getStatus() {
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
					public int getEffectiveModificationCheckInterval() {
						return 0;
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
				return new Collection<SUser>() {
					@Override
					public int size() {
						return 0;
					}

					@Override
					public boolean isEmpty() {
						return false;
					}

					@Override
					public boolean contains(Object o) {
						return false;
					}

					@NotNull
					@Override
					public Iterator<SUser> iterator() {
						return new Iterator<SUser>() {
							@Override
							public boolean hasNext() {
								return false;
							}
							@Override
							public void remove() {}

							@Override
							public SUser next() {
								return new SUser() {
									@NotNull
									@Override
									public List<SVcsModification> getVcsModifications(int i) {
										return null;
									}

									@NotNull
									@Override
									public List<SVcsModification> getAllModifications() {
										return null;
									}

									@Override
									public void updateUserAccount(@NotNull String s, String s1, String s2) throws UserNotFoundException, DuplicateUserAccountException, EmptyUsernameException {

									}

									@Override
									public void setUserProperties(@NotNull Map<? extends PropertyKey, String> map) throws UserNotFoundException {

									}

									@Override
									public void setUserProperty(@NotNull PropertyKey propertyKey, String s) throws UserNotFoundException {

									}

									@Override
									public void deleteUserProperty(@NotNull PropertyKey propertyKey) throws UserNotFoundException {

									}

									@Override
									public void setPassword(String s) throws UserNotFoundException {

									}

									@NotNull
									@Override
									public List<String> getProjectsOrder() throws UserNotFoundException {
										return null;
									}

									@Override
									public void setProjectsOrder(@NotNull List<String> list) throws UserNotFoundException {

									}

									@Override
									public void setVisibleProjects(@NotNull Collection<String> collection) throws UserNotFoundException {

									}

									@Override
									public void hideProject(@NotNull String s) throws UserNotFoundException {

									}

									@Override
									public void setLastLoginTimestamp(@NotNull Date date) throws UserNotFoundException {

									}

									@Override
									public void setBlockState(String s, String s1) {

									}

									@Nullable
									@Override
									public String getBlockState(String s) {
										return null;
									}

									@NotNull
									@Override
									public List<UserGroup> getUserGroups() {
										return null;
									}

									@NotNull
									@Override
									public List<UserGroup> getAllUserGroups() {
										return null;
									}

									@NotNull
									@Override
									public List<VcsUsernamePropertyKey> getVcsUsernameProperties() {
										return null;
									}

									@NotNull
									@Override
									public List<SBuildType> getOrderedBuildTypes(@Nullable SProject sProject) {
										return null;
									}

									@NotNull
									@Override
									public Collection<SBuildType> getBuildTypesOrder(@NotNull SProject sProject) {
										return null;
									}

									@Override
									public void setBuildTypesOrder(@NotNull SProject sProject, @NotNull List<SBuildType> list, @NotNull List<SBuildType> list1) {

									}

									@Override
									public boolean isHighlightRelatedDataInUI() {
										return false;
									}

									@NotNull
									@Override
									public List<NotificationRule> getNotificationRules(@NotNull String s) {
										return null;
									}

									@Override
									public void setNotificationRules(@NotNull String s, @NotNull List<NotificationRule> list) {

									}

									@Override
									public void removeRule(long l) {

									}

									@Override
									public void applyOrder(@NotNull String s, @NotNull long[] longs) {

									}

									@Override
									public long addNewRule(@NotNull String s, @NotNull NotificationRule notificationRule) throws DuplicateNotificationRuleException {
										return 0;
									}

									@Nullable
									@Override
									public Collection<Long> findConflictingRules(@NotNull String s, @NotNull WatchedBuilds watchedBuilds) {
										return null;
									}

									@Nullable
									@Override
									public NotificationRule findRuleById(long l) {
										return null;
									}

									@NotNull
									@Override
									public List<NotificationRulesHolder> getParentRulesHolders() {
										return null;
									}

									@NotNull
									@Override
									public List<NotificationRulesHolder> getAllParentRulesHolders() {
										return null;
									}

									@NotNull
									@Override
									public Collection<Role> getRolesWithScope(@NotNull RoleScope roleScope) {
										return null;
									}

									@Override
									public boolean isSystemAdministratorRoleGrantedDirectly() {
										return false;
									}

									@NotNull
									@Override
									public Collection<RolesHolder> getParentHolders() {
										return null;
									}

									@NotNull
									@Override
									public Collection<RolesHolder> getAllParentHolders() {
										return null;
									}

									@Override
									public long getId() {
										return 0;
									}

									@Override
									public String getRealm() {
										return null;
									}

									@Override
									public String getUsername() {
										return null;
									}

									@Override
									public String getName() {
										return null;
									}

									@Override
									public String getEmail() {
										return null;
									}

									@Override
									public String getDescriptiveName() {
										return null;
									}

									@Override
									public String getExtendedName() {
										return null;
									}

									@Override
									public Date getLastLoginTimestamp() {
										return null;
									}

									@Override
									public List<String> getVisibleProjects() {
										return null;
									}

									@Override
									public List<String> getAllProjects() {
										return null;
									}

									@NotNull
									@Override
									public String describe(boolean b) {
										return null;
									}

									@Override
									public boolean isPermissionGrantedGlobally(@NotNull Permission permission) {
										return false;
									}

									@NotNull
									@Override
									public Permissions getGlobalPermissions() {
										return null;
									}

									@NotNull
									@Override
									public Map<String, Permissions> getProjectsPermissions() {
										return null;
									}

									@Override
									public boolean isPermissionGrantedForProject(@NotNull String s, @NotNull Permission permission) {
										return false;
									}

									@Override
									public boolean isPermissionGrantedForAllProjects(@NotNull Collection<String> collection, @NotNull Permission permission) {
										return false;
									}

									@Override
									public boolean isPermissionGrantedForAnyProject(@NotNull Permission permission) {
										return false;
									}

									@NotNull
									@Override
									public Permissions getPermissionsGrantedForProject(@NotNull String s) {
										return null;
									}

									@NotNull
									@Override
									public Permissions getPermissionsGrantedForAllProjects(@NotNull Collection<String> collection) {
										return null;
									}

									@Nullable
									@Override
									public User getAssociatedUser() {
										return null;
									}

									@Override
									public Collection<RoleScope> getScopes() {
										return null;
									}

									@NotNull
									@Override
									public Collection<RoleEntry> getRoles() {
										return null;
									}

									@Override
									public void addRole(@NotNull RoleScope roleScope, @NotNull Role role) {

									}

									@Override
									public void removeRole(@NotNull RoleScope roleScope, @NotNull Role role) {

									}

									@Override
									public void removeRole(@NotNull Role role) {

									}

									@Override
									public void removeRoles(@NotNull RoleScope roleScope) {

									}

									@Override
									public boolean isSystemAdministratorRoleGranted() {
										return false;
									}

									@Override
									public boolean isSystemAdministratorRoleInherited() {
										return false;
									}

									@Nullable
									@Override
									public String getPropertyValue(PropertyKey propertyKey) {
										return null;
									}

									@Override
									public boolean getBooleanProperty(PropertyKey propertyKey) {
										return false;
									}

									@NotNull
									@Override
									public Map<PropertyKey, String> getProperties() {
										return null;
									}
								};
							}
						};
					}

					@NotNull
					@Override
					public Object[] toArray() {
						return new Object[0];
					}

					@NotNull
					@Override
					public <T> T[] toArray(@NotNull T[] a) {
						return null;
					}

					@Override
					public boolean add(SUser sUser) {
						return false;
					}

					@Override
					public boolean remove(Object o) {
						return false;
					}

					@Override
					public boolean containsAll(@NotNull Collection<?> c) {
						return false;
					}

					@Override
					public boolean addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends SUser> c) {
						return false;
					}

					@Override
					public boolean removeAll(@NotNull Collection<?> c) {
						return false;
					}

					@Override
					public boolean retainAll(@NotNull Collection<?> c) {
						return false;
					}

					@Override
					public void clear() {

					}
				};
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

			@Override
			public void setDescription(@NotNull String s) throws AccessDeniedException {

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
