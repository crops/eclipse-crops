package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ToolchainDeserializer implements JsonDeserializer<Object> {
	
	@Override
	public Toolchain deserialize(final JsonElement json, final Type typOf, final JsonDeserializationContext context)
			throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
		
		final String name = jsonObject.get("name").getAsString();
		
		// Delegate deserialization to the context
		Boards boards = context.deserialize(jsonObject.get("boards"), Boards.class);
		
		final Toolchain toolchain = new Toolchain();
		toolchain.setName(name);
		toolchain.setBoards(boards);
		return toolchain;
	}
}
