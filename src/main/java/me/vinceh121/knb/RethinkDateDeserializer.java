package me.vinceh121.knb;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class RethinkDateDeserializer extends StdDeserializer<Date> {
	private static final long serialVersionUID = 4618975337162629562L;

	protected RethinkDateDeserializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(final JsonParser p, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		return new Date(p.getLongValue());
		// final ObjectNode tree = p.readValueAsTree();
		// final ZoneOffset timezone =
		// ZoneOffset.of(tree.get("offset").get("id").asText());
		// return Date
		// .from(OffsetDateTime
		// .of(tree.get("year").asInt(), tree.get("monthValue").asInt(),
		// tree.get("dayOfMonth").asInt(),
		// tree.get("hour").asInt(), tree.get("minute").asInt(),
		// tree.get("second").asInt(),
		// tree.get("nano").asInt(), timezone)
		// .toInstant());
	}

}
