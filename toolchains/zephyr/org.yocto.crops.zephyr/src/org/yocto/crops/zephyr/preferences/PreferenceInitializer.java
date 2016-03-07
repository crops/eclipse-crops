package org.yocto.crops.zephyr.preferences;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.zephyr.ZephyrPlugin;
import org.yocto.crops.zephyr.ZephyrUtils;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ZephyrPlugin.getDefault().getPreferenceStore();
		/* TODO: use ${HOME} or $(user.home) variable expansion in path */
		/* should be System.getProperty("user.home"); */
		//store.setDefault(PreferenceConstants.P_CEED_PATH, System.getProperty("user.home").concat(".crops/ceed/ceed"));
//		if(CropsUtils.isWin()) {
//			String USER_HOME = System.getenv("USERPROFILE"); // C:/Users/<username>
//			try {
//				store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CEED_PATH, Paths.get(USER_HOME).resolve(".crops").resolve("ceed").resolve("ceed.exe").toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else if(CropsUtils.isMac() || CropsUtils.isLinux()){
//			String USER_HOME = System.getProperty("user.home");
//			try {
//				store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CEED_PATH, Paths.get(USER_HOME).resolve(".crops").resolve("ceed").resolve("ceed").toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		String root;
//		try {
//			root = CropsUtils.getCropsRoot();
//			store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CROPS_ROOT, root);
//		} catch (CoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		if(CropsUtils.isLinux())
//			store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CODI_IP_ADDR, "127.0.0.1");
//		else /* mac, win */
//			store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CODI_IP_ADDR, "192.168.99.100");
//		store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_CODI_SOCKET, "10000");
//		store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_EXPERT_MODE, false);
//		store.setDefault(org.yocto.crops.core.preferences.PreferenceConstants.P_MAKE_TYPE, "makefile");
//		
		/* TODO: move to zephyr specific Preferences store ? */
		store.setDefault(PreferenceConstants.P_TOOLCHAIN_CONTAINER_ID, "crops-zephyr-0-7-2-src");
		store.setDefault(PreferenceConstants.P_ZEPHYR_BOARD, "arduino_101");
		store.setDefault(PreferenceConstants.P_ZEPHYR_GCC_VARIANT, "zephyr");
		store.setDefault(PreferenceConstants.P_ZEPHYR_INSTALL_DIR, "/opt/zephyr-sdk");
		/* TODO: switch ZEPHYR_BASE depending on type of container...
		 * "fat" containers will have ZEPYHYR_BASE internal
		 * "skinny" containers will need external ZEPHYR_BASE
		 */
		try {
			store.setDefault(PreferenceConstants.P_ZEPHYR_BASE, CropsUtils.getCropsRoot() + "/zephyr-project/");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
