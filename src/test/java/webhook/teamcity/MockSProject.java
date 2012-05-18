package webhook.teamcity;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.BuildTypeDescriptor.CheckoutType;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.BuildTypeTemplate;
import jetbrains.buildServer.serverSide.CopyOptions;
import jetbrains.buildServer.serverSide.DuplicateBuildTypeNameException;
import jetbrains.buildServer.serverSide.DuplicateTemplateNameException;
import jetbrains.buildServer.serverSide.InvalidVcsRootScopeException;
import jetbrains.buildServer.serverSide.MaxNumberOfBuildTypesReachedException;
import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.PersistFailedException;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.TemplateCannotBeRemovedException;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.vcs.SVcsRoot;
import jetbrains.buildServer.vcs.VcsRootInstance;

public class MockSProject implements SProject {

	private String name;
	private String description;
	private String projectId;
	private File configDirectory;
	private Status status;
	private SBuildType buildType;

	public MockSProject(String name, String description, String projectId, 
						SBuildType buildType)
	{
		this.name = name;
		this.description = description;
		this.projectId = projectId;
		this.buildType = buildType;
	}
	
	public boolean containsBuildType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public SBuildType createBuildType(SBuildType arg0, String arg1,
			CopyOptions arg2) throws MaxNumberOfBuildTypesReachedException {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuildType createBuildType(String arg0, String arg1, int arg2,
			CheckoutType arg3) throws DuplicateBuildTypeNameException,
			MaxNumberOfBuildTypesReachedException {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuildType createBuildType(SBuildType arg0, String arg1,
			boolean arg2, boolean arg3)
			throws MaxNumberOfBuildTypesReachedException {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuildType findBuildTypeById(String arg0) {
		return this.buildType;
	}

	public SBuildType findBuildTypeByName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public File getArtifactsDirectory() {
		// TODO Auto-generated method stub
		return this.buildType.getArtifactsDirectory();
	}

	public List<SBuildType> getBuildTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getConfigDirectory() {
		return this.configDirectory;
	}

	public File getConfigurationFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SVcsRoot> getVcsRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasBuildTypes() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInModel() {
		// TODO Auto-generated method stub
		return false;
	}

	public void persist() throws PersistFailedException {
		// TODO Auto-generated method stub

	}

	public void removeBuildType(String arg0) {
		// TODO Auto-generated method stub

	}

	public void removeBuildTypes() {
		// TODO Auto-generated method stub

	}

	public void setDescription(String arg0) {
		this.description = arg0;
	}

	public void setName(String arg0) {
		this.name = arg0;
	}

	public void updateProjectInTransaction(ProjectUpdater arg0)
			throws PersistFailedException {
		// TODO Auto-generated method stub

	}

	public void writeTo(Element arg0) {
		// TODO Auto-generated method stub

	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public Status getStatus() {
		return this.status;
	}

	public int compareTo(BuildProject o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addParameter(Parameter arg0) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, String> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Parameter> getParametersCollection() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeParameter(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public SBuildType createBuildType(String arg0)
			throws DuplicateBuildTypeNameException,
			MaxNumberOfBuildTypesReachedException {
		// TODO Auto-generated method stub
		return null;
	}

	public SBuildType createBuildTypeFromTemplate(BuildTypeTemplate arg0,
			String arg1, CopyOptions arg2)
			throws MaxNumberOfBuildTypesReachedException,
			InvalidVcsRootScopeException {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildTypeTemplate createBuildTypeTemplate(String arg0)
			throws DuplicateTemplateNameException {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildTypeTemplate createBuildTypeTemplate(BuildTypeTemplate arg0,
			String arg1, CopyOptions arg2) throws InvalidVcsRootScopeException {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildTypeTemplate createBuildTypeTemplate(SBuildType arg0,
			String arg1, CopyOptions arg2) throws InvalidVcsRootScopeException {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildTypeTemplate findBuildTypeTemplateById(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public BuildTypeTemplate findBuildTypeTemplateByName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getArchivingTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getArchivingUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<BuildTypeTemplate> getBuildTypeTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getExtendedName() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<User> getPotentiallyResponsibleUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VcsRootInstance> getVcsRootInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isArchived() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeBuildTypeTemplate(String arg0)
			throws TemplateCannotBeRemovedException {
		// TODO Auto-generated method stub
		
	}

	public void setArchived(boolean arg0, User arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ParametersProvider getParametersProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueResolver getValueResolver() {
		// TODO Auto-generated method stub
		return null;
	}

}
