package org.yocto.crops.zephyr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.core.preferences.CropsPreferences;
import org.yocto.crops.zephyr.model.Board;
import org.yocto.crops.zephyr.model.BoardDeserializer;
import org.yocto.crops.zephyr.model.Boards;
import org.yocto.crops.zephyr.model.KernelType;
import org.yocto.crops.zephyr.model.KernelTypeDeserializer;
import org.yocto.crops.zephyr.model.Toolchain;
import org.yocto.crops.zephyr.model.ToolchainDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ZephyrUtils {
	
	private static String defaultHome;
	
	public static String getDefaultHome() {
		if (CropsUtils.isWin()) {
			defaultHome = Paths.get(System.getenv("USERPROFILE")).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			defaultHome = Paths.get(System.getProperty("user.home")).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return defaultHome;
	}
	
	public static Set<String> getBoardNames() {
		try {
			Set<String> boardNames = new HashSet<String>();
			FileInputStream jsonInput = new FileInputStream(CropsPreferences.getCropsHome() + "/toolchains.json");
			final GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Toolchain.class, new ToolchainDeserializer());
			gsonBuilder.registerTypeAdapter(Board.class, new BoardDeserializer());
			gsonBuilder.registerTypeAdapter(KernelType.class, new KernelTypeDeserializer());
			
			final Gson gson = gsonBuilder.create();
			Boards boards = gson.fromJson(jsonInput.toString(), Boards.class);
			for(Board board : boards.getBoards()) {
				boardNames.add(board.getName());
			}
			jsonInput.close();
			return boardNames;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
}
