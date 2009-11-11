package webhook.teamcity.settings;


import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class WebHookProjectSettingsTest {
	ProjectSettingsManager psm = mock(ProjectSettingsManager.class);
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void TestFactory(){
		WebHookProjectSettingsFactory psf = new WebHookProjectSettingsFactory(psm);
		psf.createProjectSettings("project1");
	}
	
	@Test
	public void TestSettings(){
		
	}
	
}
