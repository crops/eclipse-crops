package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BoardsDeserializer implements JsonDeserializer<Boards> {
	@Override
	public Boards deserialize(final JsonElement json, final Type typeOf, final JsonDeserializationContext context)
		throws JsonParseException {
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Board.class, new BoardDeserializer());
		Gson gson = gsonBuilder.create();
		
		final JsonArray jsonArray = json.getAsJsonArray();
		Board[] boardArray = (Board[]) gson.fromJson(jsonArray.getAsJsonArray(), Board[].class);
		Set<Board> boardSet = new HashSet<>(Arrays.asList(boardArray));
		final Boards boards = new Boards();
		boards.setBoards(boardSet);
		return boards;
	}
}
