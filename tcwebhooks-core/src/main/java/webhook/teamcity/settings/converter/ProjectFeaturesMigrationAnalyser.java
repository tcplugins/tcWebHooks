
package webhook.teamcity.settings.converter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.With;
import webhook.teamcity.TeamCityCoreFacade.NextMigrationStep;
import webhook.teamcity.TeamCityCoreFacade.ProjectVcsStatus;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;

public class ProjectFeaturesMigrationAnalyser {
    
    public ProjectMigrationStatus populateMigrationStatusDetail(ProjectMigrationStatus projectMigrationStatus) {
        
        // Check if project is able to be migrated (eg, VCS is writable.
        // Check if project should be migrated (VCS is writable, and is not Kotlin)
        // Check if project contains old webhooks
        // Check if project contains new webhooks and old webhooks
        // Check if project contains the same old webhooks and new webhooks (can be just cleaned up)
        // Check if old webhooks refer to a BT that no longer exists.
        
        if (!projectMigrationStatus.projectVcsStatus.getMigrationStatus().isMigratable()) {
            // We don't have a migratable project, but now let's check if it appears to be migrated already.
            if (getCandidatesSize(projectMigrationStatus.webhooks) == 0 && migatedAndCachedMatch(projectMigrationStatus.webhooks)) {
                projectMigrationStatus.status = MigrationStatus.WARNING;
                projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CHECK;
                projectMigrationStatus.reason = "It is not possible to automatically migrate this project due to the mechanism used for versioning project settings. "
                        + "However this project appears to have been migrated already. 0 webhooks were found in the old configuration file "
                        + "(<code>plugin-settings.xml</code>) and " + getCanchedSize(projectMigrationStatus.webhooks) + " migrated webhooks "
                        + "were found in the project. Please check the  projects <code>settings.kts</code> file to confirm. (" + projectMigrationStatus.getProjectVcsStatus().getMigrationStatus() + ")";
            } else if (getCandidatesSize(projectMigrationStatus.webhooks) > 0 && candidatesMigratedAndCachedMatch(projectMigrationStatus.webhooks)) {
                // All our old webhooks still exist even though they are migrated
                projectMigrationStatus.reason = "It is not possible to automatically migrate this project due to the mechanism used for versioning project settings. "
                        + "However this project appears to have been migrated already. The same Webhooks were found in the old configuration file "
                        + "(<code>plugin-settings.xml</code>) and in the new ProjectFeatures. The <code>plugin-settings.xml</code> file needs to be removed.";
                projectMigrationStatus.status = MigrationStatus.WARNING;
                projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CLEANUP_REQUIRED;
            }  else {
                projectMigrationStatus.status = MigrationStatus.ERROR;
                projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_MIGRATION_REQUIRED;
                projectMigrationStatus.reason = "It is not possible to automatically migrate this project due to the mechanism used for versioning project settings. "
                        + "(" + projectMigrationStatus.getProjectVcsStatus().getMigrationStatus() + ")";
            }
            return projectMigrationStatus;
        }
        
        // If we get this far, we have a project that should be able to be migrated.
        // Check and see how we got on.
        
        // Now check if old ones are cleaned up, and the migrated ones and cached ones match.
        // We would consider this a successful migration.
        if (getCandidatesSize(projectMigrationStatus.webhooks) == 0 && migatedAndCachedMatch(projectMigrationStatus.webhooks)) {
        	if(projectMigrationStatus.projectVcsStatus.isKotlin()) {
                projectMigrationStatus.status = MigrationStatus.WARNING;
                projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CHECK;
                projectMigrationStatus.reason = "All webhooks appear to be migrated to the new ProjectFeatures. However, since this project is Kotlin these should be manually checked and might need to be manually migrated to KotlinDSL in <code>settings.kts</code>.";
        	} else {
        		projectMigrationStatus.status = MigrationStatus.OK;
        		projectMigrationStatus.nextMigrationStep = NextMigrationStep.COMPLETED;
        	}
        } else if (getCandidatesSize(projectMigrationStatus.webhooks) > 0 && candidatesMigratedAndCachedMatch(projectMigrationStatus.webhooks)) {
            // All our old webhooks still exist even though they are migrated
            projectMigrationStatus.reason = "The same Webhooks were found in the old configuration file (<code>plugin-settings.xml</code>) and in the new ProjectFeatures. These should have been cleaned up as part of the migration process and will need to be manually removed from <code>plugin-settings.xml</code>.";
            projectMigrationStatus.status = MigrationStatus.WARNING;
            projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_MIGRATION_REQUIRED;
        } else if (getCandidatesSize(projectMigrationStatus.webhooks) > 0 && migatedAndCachedMatch(projectMigrationStatus.webhooks)) {
            // We have both old and new. but they don't exactly match.
            projectMigrationStatus.reason = "Some old Webhooks were found in the old configuration file (<code>plugin-settings.xml</code>) and some different webhooks were found in the new ProjectFeatures. These will need to be  manually cleaned up from <code>plugin-settings.xml</code>.";
            projectMigrationStatus.status = MigrationStatus.WARNING;
            projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CLEANUP_REQUIRED;
        } else if (getCandidatesSize(projectMigrationStatus.webhooks) == 0 && getCanchedSize(projectMigrationStatus.webhooks) == 0) {
            // No old and no new WebHooks
            projectMigrationStatus.reason = "No old Webhooks were found in the old configuration file (<code>plugin-settings.xml</code>) and no webhooks were found in the new ProjectFeatures. It is recommended that this project is checked to see if there should be any webhooks configured.";
            projectMigrationStatus.status = MigrationStatus.WARNING;
            projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CHECK;
            
        } else {
            // Unexpected scenario
            projectMigrationStatus.reason = "The project is in an unknown state. Please raise a bug on GitHub, and include the technical details on the right.";
            projectMigrationStatus.status = MigrationStatus.ERROR;
            projectMigrationStatus.nextMigrationStep = NextMigrationStep.MANUAL_CHECK;
        }
        return projectMigrationStatus;
    }
    
    private boolean candidatesMigratedAndCachedMatch(Map<String, WebHookTriple> webhooks) {
        List<String> candidates = webhooks.values().stream().filter(t -> t.getCandidate() != null).map(t -> t.getCandidate().getUniqueKey()).collect(Collectors.toList());
        List<String> migrated = webhooks.values().stream().filter(t -> t.getMigrated() != null).map(t -> t.getMigrated().getUniqueKey()).collect(Collectors.toList());
        List<String> cached = webhooks.values().stream().filter(t -> t.getCached() != null).map(t -> t.getCached().getWebHookConfig().getUniqueKey()).collect(Collectors.toList());
        return candidates.size() == migrated.size() 
                && candidates.equals(cached)    
                && migrated.equals(cached);    
    }

    private boolean migatedAndCachedMatch(Map<String, WebHookTriple> webhooks) {
        List<String> migrated = webhooks.values().stream().filter(t -> t.getMigrated() != null).map(t -> t.getMigrated().getUniqueKey()).collect(Collectors.toList());
        List<String> cached = webhooks.values().stream().filter(t -> t.getCached() != null).map(t -> t.getCached().getWebHookConfig().getUniqueKey()).collect(Collectors.toList());
        return !migrated.isEmpty() && migrated.equals(cached);
    }

    private long getCandidatesSize(Map<String, WebHookTriple> webhooks) {
        return webhooks.values().stream().filter(t -> t.candidate != null).count();
    }
    
    private long getCanchedSize(Map<String, WebHookTriple> webhooks) {
        return webhooks.values().stream().filter(t -> t.cached != null).count();
    }
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ProjectMigrationStatus {
        @With
        SProject project;
        @With
        ProjectVcsStatus projectVcsStatus;
        @With
        boolean migrated;
        @With
        MigrationStatus status;
        @With
        String reason;
        @With @Singular
        Map<String, WebHookTriple> webhooks = new LinkedHashMap<>();
        @With
        String kotlinDsls;
        @With
        String projectFeaturesXml;
        @With
        NextMigrationStep nextMigrationStep;
    }
   
    /** A data class that holds the same webhook
     * from  three different sources.
     * candidate - from plugin-settings.xml
     * migrated - from ProjectFeatures
     * cached - from WebHook settings cache 
     */
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class WebHookTriple {
        @With
        String reason;
        @With
        WebHookConfig candidate;
        @With
        WebHookConfig migrated;
        @With
        WebHookConfigEnhanced cached;
    }
    public enum MigrationStatus {
        ERROR, WARNING, OK;
    }
}
