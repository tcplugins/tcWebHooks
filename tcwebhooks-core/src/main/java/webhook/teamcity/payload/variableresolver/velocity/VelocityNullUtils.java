package webhook.teamcity.payload.variableresolver.velocity;

import java.util.Objects;

public class VelocityNullUtils {
	
	private static final String UNRESOLVED = "UNRESOLVED";

	public Object toUnResolved(Object value) {
		if (Objects.isNull(value)) {
			return UNRESOLVED;
		}
		return value;
	}
	
	public Object toUnResolved(Object value, boolean wrapWithQuotes) {
		if (Objects.isNull(value)) {
			return wrapWithQuotes ? "\"" +  toUnResolved(value) + "\"" : toUnResolved(value);
		}
		return value;
	}
	
}
