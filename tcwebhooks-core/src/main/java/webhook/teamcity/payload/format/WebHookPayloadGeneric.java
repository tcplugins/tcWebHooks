/**
 *
 */
package webhook.teamcity.payload.format;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tests.TestName;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

public abstract class WebHookPayloadGeneric implements WebHookPayload {

    WebHookPayloadManager myManager;

    public WebHookPayloadGeneric(WebHookPayloadManager manager) {
        this.setPayloadManager(manager);
    }

    @Override
    public void setPayloadManager(WebHookPayloadManager manager) {
        myManager = manager;
    }

    public abstract void register();


    @Override
    public String beforeBuildFinish(SBuild runningBuild, SFinishedBuild previousBuild,
                                    SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {
        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
        return getStatusAsString(content, webHookTemplate);
    }

    /**
     * buildChangedStatus has been deprecated because it alluded to build history status, which was incorrect.
     * It will no longer be called by the WebHookListener
     */
    @Deprecated
    @Override
    public String buildChangedStatus(SBuild runningBuild, SFinishedBuild previousBuild,
                                     Status oldStatus, Status newStatus,
                                     SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
        return "";
    }

    @Override
    public String buildFinished(SBuild runningBuild, SFinishedBuild previousBuild,
                                SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {
        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_FINISHED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
        return getStatusAsString(content, webHookTemplate);
    }

    @Override
    public String buildInterrupted(SBuild runningBuild, SFinishedBuild previousBuild,
                                   SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {
        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_INTERRUPTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
        return getStatusAsString(content, webHookTemplate);
    }

    @Override
    public String changesLoaded(SBuild runningBuild, SFinishedBuild previousBuild,
                                SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {
        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.CHANGES_LOADED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
        return getStatusAsString(content, webHookTemplate);
    }

    @Override
    public String buildStarted(SBuild runningBuild, SFinishedBuild previousBuild,
                               SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {
        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_STARTED, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
        return getStatusAsString(content, webHookTemplate);
    }

    /**
     * Used by versions of TeamCity less than 7.0
     *
     * @throws WebHookPayloadContentAssemblyException
     */
    @Override
    public String responsibleChanged(SBuildType buildType,
                                     ResponsibilityInfo responsibilityInfoOld,
                                     ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
                                     SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {

        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), buildType, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters, templates);
        String oldUser = "Nobody";
        String newUser = "Nobody";
        try {
            oldUser = responsibilityInfoOld.getResponsibleUser().getDescriptiveName();
        } catch (Exception e) {
        }
        try {
            newUser = responsibilityInfoNew.getResponsibleUser().getDescriptiveName();
        } catch (Exception e) {
        }
        content.setMessage("Build " + buildType.getFullName().toString()
                + " has changed responsibility from "
                + oldUser
                + " to "
                + newUser
                + " with comment '"
                + responsibilityInfoNew.getComment().toString().trim()
                + "'"
        );
        content.setText(buildType.getFullName().toString()
                + " changed responsibility from "
                + oldUser
                + " to "
                + newUser
                + " with comment '"
                + responsibilityInfoNew.getComment().toString().trim()
                + "'"
        );

        content.setComment(responsibilityInfoNew.getComment());
        return getStatusAsString(content, webHookTemplate);
    }

    /**
     * Used by versions of TeamCity 7.0 and above
     *
     * @throws WebHookPayloadContentAssemblyException
     */
    @Override
    public String responsibleChanged(SBuildType buildType,
                                     ResponsibilityEntry responsibilityEntryOld,
                                     ResponsibilityEntry responsibilityEntryNew,
                                     SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException {

        WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), buildType, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters, templates);
        String oldUser = "Nobody";
        String newUser = "Nobody";
        if (responsibilityEntryOld.getState() != ResponsibilityEntry.State.NONE) {
            oldUser = responsibilityEntryOld.getResponsibleUser().getDescriptiveName();
        }
        if (responsibilityEntryNew.getState() != ResponsibilityEntry.State.NONE) {
            newUser = responsibilityEntryNew.getResponsibleUser().getDescriptiveName();
        }
        content.setMessage("Build " + buildType.getFullName().toString()
                + " has changed responsibility from "
                + oldUser
                + " to "
                + newUser
                + " with comment '"
                + responsibilityEntryNew.getComment()
                + "'"
        );
        content.setText(buildType.getFullName().toString().toString().trim()
                + " changed responsibility from "
                + oldUser
                + " to "
                + newUser
                + " with comment '"
                + responsibilityEntryNew.getComment().toString().trim()
                + "'"
        );
        content.setResponsibilityUserOld(oldUser);
        content.setResponsibilityUserNew(newUser);
        content.setComment(responsibilityEntryNew.getComment());
        return getStatusAsString(content, webHookTemplate);
    }

    @Override
    public String responsibleChanged(SProject project,
                                     TestNameResponsibilityEntry oldTestNameResponsibilityEntry,
                                     TestNameResponsibilityEntry newTestNameResponsibilityEntry,
                                     boolean isUserAction, SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String responsibleChanged(SProject project,
                                     Collection<TestName> testNames, ResponsibilityEntry entry,
                                     boolean isUserAction, SortedMap<String, String> extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract String getStatusAsString(WebHookPayloadContent content, WebHookTemplateContent webHookTemplate) throws WebHookPayloadContentAssemblyException;

    public abstract String getContentType();

    public abstract Integer getRank();

    public abstract void setRank(Integer rank);

    public abstract String getCharset();


}
