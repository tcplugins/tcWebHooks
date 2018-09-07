package webhook.teamcity.testing.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import webhook.teamcity.BuildStateEnum;

/**
 *  Gson {@link TypeAdapter} which can accept a {@link BuildStateEnum} using either
 *  BuildTypeEnum.toString() or BuildTypeEnum.getShortName() <p>
 *
 *	This means the value can either be "BUILD_STARTED" or "buildStarted" and the JSON will
 *  deserialise correctly. <p>
 * 
 *  Also, serialised value will be represented by BuildTypeEnum.getShortName(),  
 *  which is "buildStarted" in the above example.<p>
 *  
 */
public class BuildStateEnumTypeAdaptor extends TypeAdapter<BuildStateEnum> {

	@Override
	public BuildStateEnum read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String buildStateName = in.nextString();
		if (BuildStateEnum.findBuildState(buildStateName) != null) {
			return BuildStateEnum.findBuildState(buildStateName);
		} else if (BuildStateEnum.valueOf(buildStateName) != null) {
			return BuildStateEnum.valueOf(buildStateName);
		}
		return null;
	}

	@Override
	public void write(JsonWriter out, BuildStateEnum src) throws IOException {
		if (src == null) {
			out.nullValue();
			return;
		}
		out.value(src.getShortName());
	}

}