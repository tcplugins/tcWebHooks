package webhook.teamcity.payload.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.StatusDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsFileModification;
import jetbrains.buildServer.vcs.VcsRootInstance;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;

public class WebHookPayloadContentChangesTest {
	
	@Mock
	VariableResolverFactory variableResolverFactory;
	
	@Mock
	VariableResolver variableResolver;
	
	@Mock
	VariableMessageBuilder variableMessageBuilder;
	
	@Mock
	SBuildServer sBuildServer;
	
	@Mock
	SFinishedBuild previousBuild;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(sBuildServer.getRootUrl()).thenReturn("http://localhost/");
		when(variableResolverFactory.createVariableMessageBuilder(any())).thenReturn(variableMessageBuilder);
	}

	@Test
	public void testTenChangedFilesFoundWithDefaultLimits() {
		
		SBuild sRunningBuild = getMockedBuild();
		List<SVcsModification> mod = getMockedChanges(10);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				new ExtraParameters(), 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(100, content.getMaxChangeFileListSize());
		assertEquals(10, content.getChangeFileListCount());
		assertEquals(10, content.getChanges().get(0).getChange().getFiles().size());
		// Verify that the changes were loaded once
		Mockito.verify(mod.get(0), Mockito.times(1)).getChanges();
		assertFalse(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void test80ChangedFilesFoundAcross4VcsRootsWithDefaultLimits() {
		
		SBuild sRunningBuild = getMockedBuild();
		List<SVcsModification> mod = getMockedChanges(4, 20);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				new ExtraParameters(), 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(100, content.getMaxChangeFileListSize());
		assertEquals(80, content.getChangeFileListCount());
		assertEquals(20, content.getChanges().get(0).getChange().getFiles().size());
		// Verify that the changes were loaded once
		Mockito.verify(mod.get(0), Mockito.times(1)).getChanges();
		assertFalse(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesIsNullWithDefaultLimitsAnd500Files() {
		
		SBuild sRunningBuild = getMockedBuild();
		List<SVcsModification> mod = getMockedChanges(500);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				new ExtraParameters(),
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(100, content.getMaxChangeFileListSize());
		assertEquals(500, content.getChangeFileListCount());
		assertNull(content.getChanges().get(0).getChange().getFiles());
		// Verify that the changes were never loaded. 
		// This is the expensive operation we are trying to avoid.
		Mockito.verify(mod.get(0), Mockito.times(0)).getChanges();
		assertTrue(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesIsNullWithDefaultLimitsAnd120FilesAcrossSixVcsRoots() {
		
		SBuild sRunningBuild = getMockedBuild();
		List<SVcsModification> mod = getMockedChanges(6,20);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				new ExtraParameters(),
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(100, content.getMaxChangeFileListSize());
		assertEquals(120, content.getChangeFileListCount());
		assertNull(content.getChanges().get(0).getChange().getFiles());
		// Verify that the changes were never loaded. 
		// This is the expensive operation we are trying to avoid.
		Mockito.verify(mod.get(0), Mockito.times(0)).getChanges();
		assertTrue(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesIsNullWith50FilesButLimitIs10SetViaTeamCityBuildParameter() {
		
		SBuild sRunningBuild = getMockedBuild();
		ExtraParameters teamCityMap = new ExtraParameters();
		teamCityMap.put(ExtraParameters.TEAMCITY, "webhook.maxChangeFileListSize", "10");
		List<SVcsModification> mod = getMockedChanges(50);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				teamCityMap, 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(10, content.getMaxChangeFileListSize());
		assertEquals(50, content.getChangeFileListCount());
		assertNull(content.getChanges().get(0).getChange().getFiles());
		// Verify that the changes were never loaded. 
		// This is the expensive operation we are trying to avoid.
		Mockito.verify(mod.get(0), Mockito.times(0)).getChanges();
		assertTrue(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesIsNullWith50FilesButLimitIs20SetViaWebHookProperty() {
		
		SBuild sRunningBuild = getMockedBuild();
		ExtraParameters propertiesMap = new ExtraParameters();
		propertiesMap.put("maxChangeFileListSize", "20");
		List<SVcsModification> mod = getMockedChanges(50);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				propertiesMap, 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(20, content.getMaxChangeFileListSize());
		assertEquals(50, content.getChangeFileListCount());
		assertNull(content.getChanges().get(0).getChange().getFiles());
		// Verify that the changes were never loaded. 
		// This is the expensive operation we are trying to avoid.
		Mockito.verify(mod.get(0), Mockito.times(0)).getChanges();
		assertTrue(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesContainsAllWhenUnlimitedViaWebHookProperty() {
		
		SBuild sRunningBuild = getMockedBuild();
		ExtraParameters propertiesMap = new ExtraParameters();
		propertiesMap.put("maxChangeFileListSize", "-1");
		List<SVcsModification> mod = getMockedChanges(500);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				propertiesMap, 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(-1, content.getMaxChangeFileListSize());
		assertEquals(500, content.getChangeFileListCount());
		assertEquals(500, content.getChanges().get(0).getChange().getFiles().size());
		Mockito.verify(mod.get(0), Mockito.times(1)).getChanges();
		assertFalse(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesContainsAllFromMulitpleChangesWhenUnlimitedViaWebHookProperty() {
		
		SBuild sRunningBuild = getMockedBuild();
		ExtraParameters propertiesMap = new ExtraParameters();
		propertiesMap.put("maxChangeFileListSize", "-1");
		List<SVcsModification> mod = getMockedChanges(5, 500);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				propertiesMap, 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(-1, content.getMaxChangeFileListSize());
		assertEquals(2500, content.getChangeFileListCount());
		
		assertEquals(500, content.getChanges().get(0).getChange().getFiles().size());
		assertEquals(500, content.getChanges().get(1).getChange().getFiles().size());
		assertEquals(500, content.getChanges().get(2).getChange().getFiles().size());
		assertEquals(500, content.getChanges().get(3).getChange().getFiles().size());
		assertEquals(500, content.getChanges().get(4).getChange().getFiles().size());
		Mockito.verify(mod.get(0), Mockito.times(1)).getChanges();
		Mockito.verify(mod.get(1), Mockito.times(1)).getChanges();
		Mockito.verify(mod.get(2), Mockito.times(1)).getChanges();
		Mockito.verify(mod.get(3), Mockito.times(1)).getChanges();
		Mockito.verify(mod.get(4), Mockito.times(1)).getChanges();
		assertFalse(content.isMaxChangeFileListCountExceeded());
	}
	
	@Test
	public void testChangedFilesIsNullWhenDisabledViaTeamCityBuildParameter() {
		
		SBuild sRunningBuild = getMockedBuild();
		ExtraParameters teamcityBuildParameters = new ExtraParameters();
		teamcityBuildParameters.put("teamcity", "webhook.maxChangeFileListSize", "0");
		List<SVcsModification> mod = getMockedChanges(50);
		when(sRunningBuild.getContainingChanges()).thenReturn(mod);
		
		WebHookPayloadContent content = new WebHookPayloadContent(
				variableResolverFactory, sBuildServer, 
				sRunningBuild, previousBuild, 
				BuildStateEnum.BEFORE_BUILD_FINISHED, 
				teamcityBuildParameters, 
				WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		
		assertEquals(0, content.getMaxChangeFileListSize());
		assertEquals(50, content.getChangeFileListCount());
		assertNull(content.getChanges().get(0).getChange().getFiles());
		// Verify that the changes were never loaded. 
		// This is the expensive operation we are trying to avoid.
		Mockito.verify(mod.get(0), Mockito.times(0)).getChanges();
		assertTrue(content.isMaxChangeFileListCountExceeded());
	}

	private List<SVcsModification> getMockedChanges(int fileNumber) {
		return getMockedChanges(1, fileNumber);
	}
	
	private List<SVcsModification> getMockedChanges(int vcsNumber, int fileNumber) {
		List<SVcsModification> mods = new ArrayList<>();
		for (int i = 0; i < vcsNumber; i++) {
			SVcsModification mod = mock(SVcsModification.class);
			VcsRootInstance vcs = mock(VcsRootInstance.class);
			when(vcs.getVcsName()).thenReturn("myVcsName" + i);
			when(mod.getVcsRoot()).thenReturn(vcs);
			when(mod.getChangeCount()).thenReturn(fileNumber);
			List<VcsFileModification> files = new ArrayList<>();
			for (int j = 0; j < fileNumber; j++) {
				VcsFileModification fileMod = mock(VcsFileModification.class);
				when(fileMod.getRelativeFileName()).thenReturn("myFile" + j + ".txt");
				files.add(fileMod);
			}
			when(mod.getChanges()).thenReturn(files);
			mods.add(mod);
		}
		return mods;
	}

	private SBuild getMockedBuild() {
		SProject sProject = mock(SProject.class);
		SBuild sBuild = mock(SRunningBuild.class);
		MockSBuildType sBuildType = new MockSBuildType("My Name", "My Description", "bt01");
		SBuildAgent buildAgent = mock(SBuildAgent.class);
		TriggeredBy triggeredBy = mock(TriggeredBy.class);
		sBuildType.setProject(sProject);
		when(sBuild.getStartDate()).thenReturn(new Date());
		when(sBuild.getBuildType()).thenReturn(sBuildType);
		when(sBuild.getAgent()).thenReturn(buildAgent);
		when(sBuild.getTriggeredBy()).thenReturn(triggeredBy);
		when(sBuild.getStatusDescriptor()).thenReturn(new StatusDescriptor(Status.NORMAL, "Running"));
		return sBuild;
	}

}
