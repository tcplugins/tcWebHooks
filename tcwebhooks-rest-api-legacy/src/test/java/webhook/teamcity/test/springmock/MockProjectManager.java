package webhook.teamcity.test.springmock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import jetbrains.buildServer.serverSide.BuildTypeFilter;
import jetbrains.buildServer.serverSide.BuildTypeTemplate;
import jetbrains.buildServer.serverSide.CopyOptions;
import jetbrains.buildServer.serverSide.DuplicateProjectNameException;
import jetbrains.buildServer.serverSide.InvalidIdentifierException;
import jetbrains.buildServer.serverSide.InvalidNameException;
import jetbrains.buildServer.serverSide.MaxNumberOfBuildTypesReachedException;
import jetbrains.buildServer.serverSide.NotAllIdentifiersMappedException;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.ProjectRemoveFailedException;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.identifiers.DuplicateExternalIdException;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.vcs.SVcsRoot;

public class MockProjectManager implements ProjectManager {
	
	SProject testProject = mock(SProject.class);

	@Override
	public SVcsRoot findVcsRootById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVcsRoot findVcsRootByExternalId(String externalId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SVcsRoot findVcsRootByConfigId(String configId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SVcsRoot> findVcsRootsByIds(Collection<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SVcsRoot> getAllVcsRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject getRootProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SProject> getProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SProject> getActiveProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SProject> getArchivedProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfProjects() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SProject> getProjects(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject createProject(String projectName)
			throws InvalidIdentifierException, InvalidNameException, DuplicateProjectNameException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject copyProject(SProject originalProject, SProject targetParent, CopyOptions options)
			throws MaxNumberOfBuildTypesReachedException, NotAllIdentifiersMappedException, InvalidNameException,
			DuplicateExternalIdException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject createProject(SProject originalProject, String newProjectName, CopyOptions options)
			throws InvalidIdentifierException, InvalidNameException, MaxNumberOfBuildTypesReachedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject findProjectByName(String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject findProjectById(String internalId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject findProjectByExternalId(String externalId) {
		return testProject;
	}

	@Override
	public SProject findProjectByConfigId(String configId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SProject> findProjects(Collection<String> projectsIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SProject> findProjectsByExternalIds(Collection<String> projectsIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuildType findBuildTypeById(String internalId) throws AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuildType findBuildTypeByExternalId(String externalId) throws AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBuildType findBuildTypeByConfigId(String configId) throws AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SBuildType> findBuildTypes(Collection<String> buildTypeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SBuildType> findBuildTypesByExternalIds(Collection<String> externalIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SBuildType> getAllBuildTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BuildTypeTemplate> getAllTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SBuildType> getActiveBuildTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SBuildType> getArchivedBuildTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfBuildTypes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SBuildType> getAllBuildTypes(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProject(String internalProjectId) throws ProjectRemoveFailedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getProjectIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<SProject, List<SBuildType>> getFilteredBuildTypes(User user, BuildTypeFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProjectId(String buildTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProjectExternalId(String buildTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findProjectIdForTemplate(String buildTypeTemplateId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProjectExists(String projectId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<SBuildType> getBuildTypesDependingOn(SBuildType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BuildTypeTemplate findBuildTypeTemplateById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BuildTypeTemplate findBuildTypeTemplateByExternalId(String externalId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BuildTypeTemplate findBuildTypeTemplateByConfigId(String configId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SProject getCommonParentProject(Collection<SProject> projects) {
		// TODO Auto-generated method stub
		return null;
	}

}
