package webhook.teamcity.extension.bean;

import jetbrains.buildServer.serverSide.SBuildType;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.settings.WebHookConfig;

import java.util.List;

public class BuildWebhooksBean {

    private SBuildType sBuildType;
    private List<WebHookConfig> buildConfigs;

    public BuildWebhooksBean(SBuildType b, List<WebHookConfig> c) {
        this.setsBuildType(b);
        this.setBuildConfigs(c);
    }

    public SBuildType getsBuildType() {
        return sBuildType;
    }

    public void setsBuildType(SBuildType sBuildType) {
        this.sBuildType = sBuildType;
    }

    public List<WebHookConfig> getBuildWebHookList() {
        return buildConfigs;
    }

    public void setBuildConfigs(List<WebHookConfig> buildConfigs) {
        this.buildConfigs = buildConfigs;
    }

    public boolean hasBuilds() {
        return this.buildConfigs.size() > 0;
    }

    public boolean isHasBuilds() {
        return hasBuilds();
    }

    public boolean hasNoBuildWebHooks() {
        return this.buildConfigs.size() == 0;
    }

    public boolean isHasNoBuildWebHooks() {
        return hasNoBuildWebHooks();
    }

    public boolean hasBuildWebHooks() {
        return this.buildConfigs.size() > 0;
    }

    public boolean isHasBuildWebHooks() {
        return hasBuildWebHooks();
    }

    public int getBuildCount() {
        return this.buildConfigs.size();
    }

    public String getBuildExternalId() {
        return TeamCityIdResolver.getExternalBuildId(sBuildType);
    }

    public String getBuildName() {
        return sBuildType.getName();
    }

}