package webhook;

public interface ResponseEvent {
		public abstract int getReponseCode();
		public abstract void updateRepsoneCode(int responseCode);
}