package webhook.teamcity;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public interface TeamCityCoreFacade {

	// PROJECTS
	@Nullable
	SProject findProjectByExtId(@Nullable String projectExtId);

	@Nullable
	SProject findProjectByIntId(String projectIntId);

	@NotNull
	List<SProject> getActiveProjects();

	void persist(@NotNull String project, @NotNull String description);

	@NotNull ProjectVcsStatus getProjectVcsStatus(SProject sProject);
	int getMaxDescripterId(SProject sProject);

	@Data @AllArgsConstructor
	public static class ProjectVcsStatus {
		private boolean vcsEnabled;
		private String vcsFormat;
		private boolean vcsSyncEnabled;
		public boolean isKotlin() {
			return vcsFormat != null && vcsFormat.equalsIgnoreCase("kotlin");
		}
		public ProjectIsMigratable getMigrationStatus() {
		    if (vcsEnabled && vcsSyncEnabled) {
		        if (isKotlin() ) {
		            return ProjectIsMigratable.VCS_KOTLIN_SYNC_ENABLED_NOT_MIGRATABLE;
		        } else {
		            return ProjectIsMigratable.VCS_XML_SYNC_ENABLED_MIGRATABLE;
		        }
		    } else if (vcsEnabled) {
		        if (isKotlin() ) {
		            return ProjectIsMigratable.VCS_KOTLIN_SYNC_DISABLED_NOT_MIGRATABLE;
		        } else {
		            return ProjectIsMigratable.VCS_XML_SYNC_DISABLE_NOT_MIGRATABLE;
		        }
		    }
		    return ProjectIsMigratable.NON_VCS_XML_MIGRATABLE;
		}
	}
	
	public enum ProjectIsMigratable {
	    
	    VCS_KOTLIN_SYNC_ENABLED_NOT_MIGRATABLE (false),
	    VCS_KOTLIN_SYNC_DISABLED_NOT_MIGRATABLE (false),
	    VCS_XML_SYNC_ENABLED_MIGRATABLE (true),
	    VCS_XML_SYNC_DISABLE_NOT_MIGRATABLE (false),
	    NON_VCS_XML_MIGRATABLE (true);
	    
	    @Getter
	    private final boolean isMigratable;
	    
	    private ProjectIsMigratable(boolean migratable) {
	        isMigratable = migratable;
	    }
	}
	
	public enum NextMigrationStep {
	    COMPLETED, 
	    RERUN_MIGRATION,
	    MANUAL_CHECK,
	    MANUAL_MIGRATION_REQUIRED,
	    MANUAL_CLEANUP_REQUIRED,
	    NONE
	}

}
