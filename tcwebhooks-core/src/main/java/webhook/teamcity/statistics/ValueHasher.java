package webhook.teamcity.statistics;

public interface ValueHasher {
	public String hash(String plain);
	public String hash(String plain, String salt);

}
