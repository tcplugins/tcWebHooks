package webhook.teamcity.test.springmock;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.Permissions;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.User;

public class MockSecurityContext implements SecurityContext {

	@Override
	public AuthorityHolder getAuthorityHolder() {
		// TODO Auto-generated method stub
		return new AuthorityHolder() {
			
			@Override
			public boolean isPermissionGrantedGlobally(Permission permission) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isPermissionGrantedForProject(String projectId, Permission permission) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isPermissionGrantedForAnyProject(Permission permission) {
				return true;
			}
			
			@Override
			public Map<String, Permissions> getProjectsPermissions() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Permissions getPermissionsGrantedForProject(String projectId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Permissions getGlobalPermissions() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public User getAssociatedUser() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

}
