package webhook.teamcity.testing;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.Permissions;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.User;

public class TestingMockUser implements User {

	private String mockUserName;

	public TestingMockUser(String mockUserName) {
		this.mockUserName = mockUserName;
	}

	@Override
	public boolean isPermissionGrantedGlobally(Permission permission) {
		return false;
	}

	@Override
	public Permissions getGlobalPermissions() {
		return null;
	}

	@Override
	public Map<String, Permissions> getProjectsPermissions() {
		return null;
	}

	@Override
	public boolean isPermissionGrantedForProject(String projectId, Permission permission) {
		return false;
	}

	public boolean isPermissionGrantedForAllProjects(Collection<String> projectIds, Permission permission) {
		return false;
	}

	@Override
	public boolean isPermissionGrantedForAnyProject(Permission permission) {
		return false;
	}

	@Override
	public Permissions getPermissionsGrantedForProject(String projectId) {
		return null;
	}

	public Permissions getPermissionsGrantedForAllProjects(Collection<String> projectIds) {
		return null;
	}

	@Override
	public User getAssociatedUser() {
		return this;
	}

	@Override
	public String getPropertyValue(PropertyKey propertyKey) {
		return null;
	}

	@Override
	public boolean getBooleanProperty(PropertyKey propertyKey) {
		return false;
	}

	@Override
	public Map<PropertyKey, String> getProperties() {
		return null;
	}

	@Override
	public String describe(boolean verbose) {
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
		return this.mockUserName;
	}

	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getDescriptiveName() {
		return getUsername();
	}

	@Override
	public String getExtendedName() {
		return getUsername();
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

}
