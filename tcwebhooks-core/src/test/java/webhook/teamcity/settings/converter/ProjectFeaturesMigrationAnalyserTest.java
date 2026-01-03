package webhook.teamcity.settings.converter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.teamcity.TeamCityCoreFacade.ProjectIsMigratable;
import webhook.teamcity.TeamCityCoreFacade.ProjectVcsStatus;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser.MigrationStatus;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser.ProjectMigrationStatus;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser.WebHookTriple;
import webhook.testframework.util.ConfigLoaderUtil;

public class ProjectFeaturesMigrationAnalyserTest {

    @Test
    public void testPopulateMigrationStatusDetailWhenProjectIsKotlinAndAppearsAlreadyMigated() throws JDOMException, IOException {
        Map<String, WebHookTriple> webhooks = new HashMap<>();
        
        String projectDir = "src/test/resources/testMigrationConfigurations/projects/FirstProject";
        WebHookConfig config = ConfigLoaderUtil.getFirstWebHookInConfig(new File(projectDir + "/pluginData/plugin-settings.xml"));
        webhooks.put(config.getUniqueKey(), new WebHookTriple("", config, config, enhanceConfig(config)));
        
        ProjectFeaturesMigrationAnalyser analyser = new ProjectFeaturesMigrationAnalyser();
        ProjectMigrationStatus projectMigrationStatus = new ProjectMigrationStatus()
                .withProjectVcsStatus(new ProjectVcsStatus(true, "kotlin", true))
                .withWebhooks(webhooks);
        ProjectMigrationStatus updatedStatus = analyser.populateMigrationStatusDetail(projectMigrationStatus);
        assertEquals(ProjectIsMigratable.VCS_KOTLIN_SYNC_ENABLED_MIGRATABLE, updatedStatus.getProjectVcsStatus().getMigrationStatus());
        assertEquals(MigrationStatus.WARNING, updatedStatus.status);
    }
    
    @Test
    public void testPopulateMigrationStatusDetailWhenProjectIsKotlinButNotMigrated() {
        ProjectFeaturesMigrationAnalyser analyser = new ProjectFeaturesMigrationAnalyser();
        ProjectMigrationStatus projectMigrationStatus = new ProjectMigrationStatus().withProjectVcsStatus(new ProjectVcsStatus(true, "kotlin", false));
        ProjectMigrationStatus updatedStatus = analyser.populateMigrationStatusDetail(projectMigrationStatus);
        assertEquals(ProjectIsMigratable.VCS_KOTLIN_SYNC_DISABLED_NOT_MIGRATABLE, updatedStatus.getProjectVcsStatus().getMigrationStatus());
        assertEquals(MigrationStatus.ERROR, updatedStatus.status);
    }
    
    @Test
    public void testPopulateMigrationStatusDetailWhenProjectIsWritableXml() throws JDOMException, IOException {
        ProjectFeaturesMigrationAnalyser analyser = new ProjectFeaturesMigrationAnalyser();
        
        String projectDir = "src/test/resources/testMigrationConfigurations/projects/FirstProject";
        
        WebHookConfig config = ConfigLoaderUtil.getFirstWebHookInConfig(new File(projectDir + "/pluginData/plugin-settings.xml"));
        
        WebHookTriple t = new WebHookTriple().withMigrated(config).withCached(enhanceConfig(config));
        ProjectMigrationStatus projectMigrationStatus = new ProjectMigrationStatus()
                .withProjectVcsStatus(new ProjectVcsStatus(true, "xml", true))
                .withWebhooks(Collections.singletonMap("", t));
        
        ProjectMigrationStatus updatedStatus = analyser.populateMigrationStatusDetail(projectMigrationStatus);
        
        assertEquals(ProjectIsMigratable.VCS_XML_SYNC_ENABLED_MIGRATABLE, updatedStatus.getProjectVcsStatus().getMigrationStatus());
        assertEquals(MigrationStatus.OK, updatedStatus.status);
    }
    private WebHookConfigEnhanced enhanceConfig(WebHookConfig config) {
        return WebHookConfigEnhanced.builder()
        .webHookConfig(config)
        .build();
    }

    @Test
    public void testPopulateMigrationStatusDetailWhenProjectIsReadonlyXml() {
        ProjectFeaturesMigrationAnalyser analyser = new ProjectFeaturesMigrationAnalyser();
        ProjectMigrationStatus projectMigrationStatus = new ProjectMigrationStatus().withProjectVcsStatus(new ProjectVcsStatus(true, "xml", false));
        ProjectMigrationStatus updatedStatus = analyser.populateMigrationStatusDetail(projectMigrationStatus);
        assertEquals(ProjectIsMigratable.VCS_XML_SYNC_DISABLE_NOT_MIGRATABLE, updatedStatus.getProjectVcsStatus().getMigrationStatus());
        assertEquals(MigrationStatus.ERROR, updatedStatus.status);
    }

}
