package webhook.teamcity.extension.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;

public class ProjectHistoryResolver {
	private ProjectHistoryResolver(){}
	
	private static final int MAX_BUILDS_PER_BUILD_TYPE = 5;

	public static ProjectHistoryBean getProjectHistory(SProject project) {
		Date now = new Date();
		List<ProjectHistoryItemBean> finishedBuilds = new ArrayList<ProjectHistoryItemBean>();
		List<SBuildType> buildTypes = project.getOwnBuildTypes();
		for (SBuildType type : buildTypes){
			List<SFinishedBuild> builds = type.getHistory();
			for (int i = 0; i < builds.size(); i++) {
				finishedBuilds.add(ProjectHistoryItemBean.build(now, builds.get(i)));
				if (i > MAX_BUILDS_PER_BUILD_TYPE){
					break;
				}
			}
		}
		return new ProjectHistoryBean(project.getProjectId(), finishedBuilds);
	}
	
	public static ProjectHistoryBean getBuildHistory(SBuildType type) {
		Date now = new Date();
		List<ProjectHistoryItemBean> finishedBuilds = new ArrayList<ProjectHistoryItemBean>();

		List<SFinishedBuild> builds = type.getHistory();
		for (int i = 0; i < builds.size(); i++) {
			finishedBuilds.add(ProjectHistoryItemBean.build(now, builds.get(i)));
			if (i > MAX_BUILDS_PER_BUILD_TYPE){
				break;
			}
		}
		return new ProjectHistoryBean(type.getProjectId(), type.getBuildTypeId(), finishedBuilds);
	}
	
	public static class ProjectHistoryBean{
		List<ProjectHistoryItemBean> recentBuilds;
		String projectId;
		String buildTypeId;
		
		public ProjectHistoryBean(String projectId, List<ProjectHistoryItemBean> history) {
			this.projectId = projectId;
			this.recentBuilds = history;
		}
		public ProjectHistoryBean(String projectId, String buildTypeId, List<ProjectHistoryItemBean> history) {
			this.projectId = projectId;
			this.buildTypeId = buildTypeId;
			this.recentBuilds = history;
		}
	}

	public static class ProjectHistoryItemBean {
		public long buildId;
		public String title;
		public String buildNumber;
		public String buildDate;
		
		public static ProjectHistoryItemBean build(Date now, SFinishedBuild build){
			ProjectHistoryItemBean bean = new ProjectHistoryItemBean();
			bean.buildId = build.getBuildId();
			bean.title = build.getBuildTypeName();
			bean.buildNumber = build.getBuildNumber();
			bean.buildDate = toDuration(now.getTime() - build.getFinishDate().getTime());
			
			return bean;
		}
	}
	
	public static final List<Long> times = Arrays.asList(
	        TimeUnit.DAYS.toMillis(365),
	        TimeUnit.DAYS.toMillis(30),
	        TimeUnit.DAYS.toMillis(1),
	        TimeUnit.HOURS.toMillis(1),
	        TimeUnit.MINUTES.toMillis(1),
	        TimeUnit.SECONDS.toMillis(1) );
	
	public static final List<String> timesString = Arrays.asList("year","month","day","hour","minute","second");

	public static String toDuration(long duration) {

	    StringBuffer res = new StringBuffer();
	    for(int i=0;i< times.size(); i++) {
	        Long current = times.get(i);
	        long temp = duration/current;
	        if(temp>0) {
	            res.append(temp).append(" ").append( timesString.get(i) ).append(temp != 1 ? "s" : "").append(" ago");
	            break;
	        }
	    }
	    if("".equals(res.toString()))
	        return "0 seconds ago";
	    else
	        return res.toString();
	}
}
