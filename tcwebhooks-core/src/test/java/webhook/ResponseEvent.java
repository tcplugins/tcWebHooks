package webhook;

public interface ResponseEvent {
		public abstract int getReponseCode();
		public abstract void updateResponseCode(int responseCode);
		public abstract String getRequestBody();
		public abstract void updateRequestBody(String requsetBody);
}