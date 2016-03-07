package org.yocto.crops.zephyr.model;

import java.util.HashSet;
import java.util.Set;

import org.yocto.crops.zephyr.ZephyrConstants.Boards.board_id;

public class Toolchain {
		private String name;
		private static Boards boards;
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setBoards(Boards boards) {
			Toolchain.boards = boards;
		}
		
		public static Boards getBoards() {
			return boards;
		}
		
		public static Set<String> getBoardNames() {
			Set<String> boardNames = new HashSet<>();
			//for(Board board : getBoards().getBoards()) {
			//	boardNames.add(board.getName());
			//}
			boardNames.add("arduino_101");
			boardNames.add("arduino_101_sss");
			boardNames.add("basic_cortex_m3");
			boardNames.add("basic_minuteia");
			boardNames.add("frdm_k64f");
			boardNames.add("galileo");
			boardNames.add("minnowboard");
			boardNames.add("qemu_cortex_m3");
			boardNames.add("qemu_x86");
			boardNames.add("quark_d2000_crb");
			boardNames.add("quark_se_ctb");
			boardNames.add("quark_se_sss_ctb");
			return boardNames;
		}
}