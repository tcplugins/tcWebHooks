package webhook.teamcity.statistics;

import org.apache.commons.codec.digest.Crypt;

import lombok.Getter;

public class CryptValueHasher implements ValueHasher {
	
	static final String SHA512_PREFIX = "$6$";
	
	@Getter
	String salt;
	public CryptValueHasher() {
		// Generate a salt that we can re-use with this instance. 
		String hash = Crypt.crypt("0CyhN64RSgG9wr9sySsdqJoylw2EMAWYlqaj7");
		// Extract the first 16 (or less) chars in form of "$6$abcdefg"
		// So start from 0 and then the third $ (which marks the end of the salt.
		this.salt = extractSaltFromCrpytString(hash);
	}

	private String extractSaltFromCrpytString(String hash) {
		return hash.substring(0, hash.indexOf('$', hash.indexOf('$', 3)));
	}

	@Override
	public String hash(String plain) {
		return Crypt.crypt(plain, this.salt);
	}
	public String hash(String plain, String salt) {
		return Crypt.crypt(plain, salt);
	}

}
