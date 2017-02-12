package webhook.teamcity;

import jetbrains.buildServer.groups.UserGroup;
import jetbrains.buildServer.notification.DuplicateNotificationRuleException;
import jetbrains.buildServer.notification.NotificationRule;
import jetbrains.buildServer.notification.NotificationRulesHolder;
import jetbrains.buildServer.notification.WatchedBuilds;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.*;
import jetbrains.buildServer.users.*;
import jetbrains.buildServer.vcs.SVcsModification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by amakhov on 10.02.17.
 */
public class MockSUser implements SUser {
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
    public boolean isSystemAdministratorRoleGrantedDirectly() {
        return false;
    }

    @Override
    public boolean isSystemAdministratorRoleInherited() {
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
        return "username";
    }

    @Override
    public String getName() {
        return "Name";
    }

    @Override
    public String getEmail() {
        return "user@email.com";
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
}
