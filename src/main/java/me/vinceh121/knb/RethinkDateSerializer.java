package me.vinceh121.knb;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class RethinkDateSerializer extends StdSerializer<Date> {
	private static final long serialVersionUID = 135651863185709075L;

	protected RethinkDateSerializer() {
		super(Date.class);
	}

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeNumber(value.getTime());
	}

}
