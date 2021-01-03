package webhook.teamcity.statistics;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValueHasherTest {

	@Test
	public void testCryptHash() {
		final String expectedHash = "$6$12345$g0kLcsUERkNLco5qujeRBK2.U0CyhN64RSgG9wr9sySsdqJoylw2EMAWYlqaj7TaivWXJX9DE.TYz4fX/ccZB0";
		ValueHasher hasher = new CryptValueHasher();
		assertEquals(expectedHash, hasher.hash("xyz", "$6$12345"));
	}
	
	@Test
	public void testCryptHashEqalityWithRandomSalt() {
		ValueHasher hasher = new CryptValueHasher();
		String hash = hasher.hash("xyz");
		assertEquals(hash, hasher.hash("xyz", hash));
	}
	
	@Test
	public void testCryptHashEqalityWithSuppliedSalt() {
		ValueHasher hasher = new CryptValueHasher();
		String hash = hasher.hash("xyz","$6$12345");
		assertEquals(hash, hasher.hash("xyz", hash));
	}
	
	@Test
	public void testNoOpValueHasher() {
		ValueHasher hasher = new NoOpValueHasher();
		assertEquals("xyz", hasher.hash("xyz"));
	}
	@Test
	public void testNoOpValueHasherWithSuppliedSalt() {
		ValueHasher hasher = new NoOpValueHasher();
		assertEquals("xyz", hasher.hash("xyz", "$6$12345"));
	}
	
	@Test
	public void testCryptHasherSalt() {
		CryptValueHasher hasher = new CryptValueHasher();
		assertEquals(11, hasher.getSalt().length());
	}

}
