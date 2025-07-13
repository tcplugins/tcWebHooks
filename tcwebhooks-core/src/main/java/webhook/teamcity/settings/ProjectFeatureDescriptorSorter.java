package webhook.teamcity.settings;

import java.util.Comparator;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;

public class ProjectFeatureDescriptorSorter implements Comparator <SProjectFeatureDescriptor> {
    private final int projectFeaturePrefixLength = "PROJECT_EXT_".length();

    @Override
    public int compare(SProjectFeatureDescriptor o1, SProjectFeatureDescriptor o2) {
        try {
            //Try to compare based on the integer value after "PROJECT_EXT_".
            // eg, 10 in "PROJECT_EXT_10"
            int o1Id = Integer.parseInt(o1.getId().substring(projectFeaturePrefixLength));
            int o2Id = Integer.parseInt(o2.getId().substring(projectFeaturePrefixLength));
            return Integer.compare(o1Id, o2Id);
        } catch (NumberFormatException ex) {
            // If that fails, just compare the text strings as that's the default behaviour in TeamCity anyway. 
            return o1.getId().compareTo(o2.getId());
        }
    }
}