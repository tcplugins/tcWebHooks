package webhook.teamcity.payload.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testStripTrailingSlash() {
		assertEquals("//blah", StringUtils.stripTrailingSlash("//blah/"));
		assertEquals("//fred", StringUtils.stripTrailingSlash("//fred"));
		assertEquals("//blah/blah", StringUtils.stripTrailingSlash("//blah/blah/"));
		assertEquals("//blah/fred", StringUtils.stripTrailingSlash("//blah/fred"));
	}

	@Test
	public void testSubString() {
		assertEquals("Normal String should return substring", "abc", StringUtils.subString("abcdefghijklmnop", 0, 3, 10));
		assertEquals("String shorter than minlength should return whole string", "abcdefghijklmnop", StringUtils.subString("abcdefghijklmnop", 0, 3, 30));
		assertEquals("String shorter than startIndex should return empty string", "", StringUtils.subString("abcdefghijklmnopqrstuvwxyz", 29, 31, 10));
		assertEquals("String shorter than endIndex should return whole string", "abcdefghijklmnopqrstuvwxyz", StringUtils.subString("abcdefghijklmnopqrstuvwxyz", 0, 39, 20));
		assertEquals("String endIndex of -1 should return whole string", "abcdefghijklmnopqrstuvwxyz", StringUtils.subString("abcdefghijklmnopqrstuvwxyz", 0, -1, 20));
	}

	@Test
	public void testCapitaliseFirst() {
		assertEquals("Capitalise me please!", StringUtils.capitaliseFirstWord("capitalise me please!"));
	}

	@Test
	public void testCapitaliseAll() {
		assertEquals("Capitalise Me Please!", StringUtils.capitaliseAllWords("capitalise me please!"));
	}

}
