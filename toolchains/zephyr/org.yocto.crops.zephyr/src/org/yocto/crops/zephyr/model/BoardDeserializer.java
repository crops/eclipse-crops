package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class BoardDeserializer implements JsonDeserializer<Board> {
	@Override
	public Board deserialize(final JsonElement json, final Type typOf, final JsonDeserializationContext context)
			throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
	
		final String name = jsonObject.get("name").getAsString();
		
		// Delegate deserialization to the context
		Arch arch = context.deserialize(jsonObject.get("arch"), Arch.class);
		SoC soc = context.deserialize(jsonObject.get("soc"), SoC.class);
		KernelTypes kernelTypes = context.deserialize(jsonObject.get("kernelTypes"), KernelTypes.class);
		
		final Board board = new Board();
		board.setName(name);
		board.setArch(arch);
		board.setSoC(soc);
		board.setKernelTypes(kernelTypes);
		return board;
	}
}
