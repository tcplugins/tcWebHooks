package webhook.teamcity;

import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.filters.Filter;

import java.util.Date;
import java.util.Map;

public class MockTriggeredBy implements TriggeredBy {

    String triggeredBy;

    public MockTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getAsString() {
        return this.triggeredBy;
    }

    public Map<String, String> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRawTriggeredBy() {
        // TODO Auto-generated method stub
        return null;
    }

    public Date getTriggeredDate() {
        // TODO Auto-generated method stub
        return null;
    }

    public SUser getUser() {
        // TODO Auto-generated method stub
        return new MockSUser();
    }

    public boolean isTriggeredByUser() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getAsString(Filter<String> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
