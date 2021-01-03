package webhook.teamcity.statistics;

public class NoOpValueHasher implements ValueHasher {

	@Override
	public String hash(String plain) {
		return plain;
	}
	@Override
	public String hash(String plain, String salt) {
		return plain;
	}

}
