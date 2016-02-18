package webhook.teamcity.auth;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UsernamePasswordAuthenticatorFactoryTest {
	WebHookAuthenticatorProvider provider;

	@Before
	public void setup(){
		provider = new WebHookAuthenticatorProvider();
	}
	
	@Test
	public void testRegister() {
		UsernamePasswordAuthenticatorFactory authenticatorFactory = new UsernamePasswordAuthenticatorFactory(provider);
		authenticatorFactory.register();
		assertTrue(provider.getRegisteredTypes().contains(authenticatorFactory.getName()));
	}

	@Test
	public void testGetName() {
		UsernamePasswordAuthenticatorFactory authenticatorFactory = new UsernamePasswordAuthenticatorFactory(provider);
		assertEquals("userpass", authenticatorFactory.getName());
	}

	@Test
	public void testGetAuthenticatorInstance() {
		UsernamePasswordAuthenticatorFactory authenticatorFactory = new UsernamePasswordAuthenticatorFactory(provider);
		authenticatorFactory.register();
		WebHookAuthenticator factory = provider.getAuthenticator("userpass");
		WebHookAuthenticator factory2 = provider.getAuthenticator("userpass");
		assertTrue(factory instanceof UsernamePasswordAuthenticator);
		assertTrue(factory2 instanceof UsernamePasswordAuthenticator);
		assertNotSame(factory, factory2);
	}

}
