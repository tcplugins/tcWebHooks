package webhook.teamcity.statistics;

import java.io.IOException;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTypeAdaptor extends TypeAdapter<LocalDate> {
	
	final DateTimeFormatter fmt = ISODateTimeFormat.date();

	@Override
	public LocalDate read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		return fmt.parseLocalDate(in.nextString());
	}

	@Override
	public void write(JsonWriter out, LocalDate src) throws IOException {
		if (src == null) {
			out.nullValue();
			return;
		}
		out.value(fmt.print(src));
	}

}