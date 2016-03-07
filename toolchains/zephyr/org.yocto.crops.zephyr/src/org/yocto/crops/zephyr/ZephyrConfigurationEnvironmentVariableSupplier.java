package org.yocto.crops.zephyr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.core.preferences.CropsConfigurationEnvironmentVariableSupplier;
import org.yocto.crops.zephyr.preferences.PreferenceConstants;

public class ZephyrConfigurationEnvironmentVariableSupplier extends CropsConfigurationEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
//		if (PathEnvironmentVariable.isVar(variableName))
//			return PathEnvironmentVariable.create(configuration);
//		if (CropsRootEnvironmentVariable.isVar(variableName))
//			return CropsRootEnvironmentVariable.create(configuration);
//		if (ContainerDispatcherIPAddressEnvironmentVariable.isVar(variableName))
//			return ContainerDispatcherIPAddressEnvironmentVariable.create(configuration);
//		if (ContainerDispatcherSocketEnvironmentVariable.isVar(variableName))
//			return ContainerDispatcherSocketEnvironmentVariable.create(configuration);
//		if (ToolchainContainerIDEnvironmentVariable.isVar(variableName))
//			return ToolchainContainerIDEnvironmentVariable.create(configuration);
		
		/* zephyr specific environment variables */
		if (ZephyrBoardEnvironmentVariable.isVar(variableName))
			return ZephyrBoardEnvironmentVariable.create(configuration);
		if (ZephyrSdkInstallDirEnvironmentVariable.isVar(variableName))
			return ZephyrSdkInstallDirEnvironmentVariable.create(configuration);
		if (ZephyrGccVariantEnvironmentVariable.isVar(variableName))
			return ZephyrGccVariantEnvironmentVariable.create(configuration);
		if (ZephyrBaseEnvironmentVariable.isVar(variableName))
			return ZephyrBaseEnvironmentVariable.create(configuration);

		/* zephyr base only makes sense in a project basis for now */
		/* TODO: figure out how to switch from App Developer to OS Developer context */
		
		/* zephyr project template variables */
//		if (ShortKernelTypeEnvironmentVariable.isVar(variableName))
//			return ShortKernelTypeEnvironmentVariable.create(configuration);
//		if (LongKernelTypeEnvironmentVariable.isVar(variableName))
//			return LongKernelTypeEnvironmentVariable.create(configuration);
//		if (ProjectConfigFilenameEnvironmentVariable.isVar(variableName))
//			return ProjectConfigFilenameEnvironmentVariable.create(configuration);
//		if (ProjectMDefFilenameEnvironmentVariable.isVar(variableName))
//			return ProjectMDefFilenameEnvironmentVariable.create(configuration);
		else
			return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		
//		IBuildEnvironmentVariable ceed = PathEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable crops_root = CropsRootEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable codi_ip_addr = ContainerDispatcherIPAddressEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable codi_socket = ContainerDispatcherSocketEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable toolchain_container_id = ToolchainContainerIDEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable board = ZephyrBoardEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable install_dir = ZephyrSdkInstallDirEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable gcc_variant = ZephyrGccVariantEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable zephyr_base = ZephyrBaseEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable short_kernel = ShortKernelTypeEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable long_kernel = LongKernelTypeEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable prj_conf = ProjectConfigFilenameEnvironmentVariable.create(configuration);
//		IBuildEnvironmentVariable prj_mdef = ProjectMDefFilenameEnvironmentVariable.create(configuration);
		
		List<IBuildEnvironmentVariable> variables = new ArrayList<IBuildEnvironmentVariable>();
//		if (ceed != null)
//			variables.add(ceed);
//		if (crops_root != null)
//			variables.add(crops_root);
//		if (codi_ip_addr != null)
//			variables.add(codi_ip_addr);
//		if (codi_socket != null)
//			variables.add(codi_socket);
//		if (toolchain_container_id != null)
//			variables.add(toolchain_container_id);
		if (board != null)
			variables.add(board);
		if (install_dir != null)
			variables.add(install_dir);
		if (gcc_variant != null)
			variables.add(gcc_variant);
		if (zephyr_base != null)
			variables.add(zephyr_base);
//		if (short_kernel != null)
//			variables.add(short_kernel);
//		if (long_kernel != null)
//			variables.add(long_kernel);
//		if (prj_conf != null)
//			variables.add(prj_conf);
//		if (prj_mdef != null)
//			variables.add(prj_mdef);
		return variables.toArray(new IBuildEnvironmentVariable[0]);
	}

	private static class PathEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "ceed"; //$NON-NLS-1$
		
		private File path;
		
		private PathEnvironmentVariable(File path) {
			this.path = path;
		}
		
		public static PathEnvironmentVariable create(IConfiguration configuration) {
			/* TODO: this should only happen on new project creation */
			IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
			String path = store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_CEED_PATH);
			File ceedExecutable = new File(path);
			if (ceedExecutable.isFile() && ceedExecutable.canExecute()) {
				return new PathEnvironmentVariable(ceedExecutable);
			}
			else
				return null;
		} 
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin() 
					? name.equalsIgnoreCase(PathEnvironmentVariable.name)
				    : name.equals(PathEnvironmentVariable.name);
		}
		
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}
		
		public String getValue() {
			return path.getPath();
		}
	}

	private static class CropsRootEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "crops_root"; //$NON-NLS-1$
		
		private String root;
		
		private CropsRootEnvironmentVariable(String root) {
			this.root = root;
		}
		
		public static CropsRootEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
			String system_root = store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_CROPS_ROOT);
			// TODO: actually check the path WRT VM or container to validate
			return new CropsRootEnvironmentVariable(system_root);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(CropsRootEnvironmentVariable.name)
					: name.equals(CropsRootEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return root;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ContainerDispatcherIPAddressEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "codi_ip_addr"; //$NON-NLS-1$
		
		private String ip_addr;
		
		private ContainerDispatcherIPAddressEnvironmentVariable(String ip_addr) {
			this.ip_addr = ip_addr;
		}
		
		public static ContainerDispatcherIPAddressEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
			String system_ip_addr = store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_CODI_IP_ADDR);
			return new ContainerDispatcherIPAddressEnvironmentVariable(system_ip_addr);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ContainerDispatcherIPAddressEnvironmentVariable.name)
					: name.equals(ContainerDispatcherIPAddressEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return ip_addr;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ContainerDispatcherSocketEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "codi_socket"; //$NON-NLS-1$
		
		private String socket;
		
		private ContainerDispatcherSocketEnvironmentVariable(String socket) {
			this.socket = socket;
		}
		
		public static ContainerDispatcherSocketEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
			String system_socket = store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_CODI_SOCKET);

			// TODO: actually ping the socket to validate
			return new ContainerDispatcherSocketEnvironmentVariable(system_socket);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ContainerDispatcherSocketEnvironmentVariable.name)
					: name.equals(ContainerDispatcherSocketEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return socket;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ToolchainContainerIDEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "toolchain_container_id"; //$NON-NLS-1$
		
		private String toolchain_container_id;
		
		private ToolchainContainerIDEnvironmentVariable(String toolchain_container_id) {
			this.toolchain_container_id = toolchain_container_id;
		}
		
		public static ToolchainContainerIDEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
			String system_toolchain_container_id = store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_TOOLCHAIN_CONTAINER_ID);

			// TODO: get toolchain id from docker-machine or codi to validate
			return new ToolchainContainerIDEnvironmentVariable(system_toolchain_container_id);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ToolchainContainerIDEnvironmentVariable.name)
					: name.equals(ToolchainContainerIDEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return toolchain_container_id;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private static class ZephyrBoardEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "board"; //$NON-NLS-1$
		
		private String zephyr_board;
		
		private ZephyrBoardEnvironmentVariable(String zephyr_board) {
			this.zephyr_board = zephyr_board;
		}
		
		public static ZephyrBoardEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = ZephyrPlugin.getDefault().getPreferenceStore();
			String system_zephyr_board = store.getDefaultString(PreferenceConstants.P_ZEPHYR_BOARD);
			// TODO: use regex to validate based on boards queried from codi or JSON
			return new ZephyrBoardEnvironmentVariable(system_zephyr_board);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ZephyrBoardEnvironmentVariable.name)
					: name.equals(ZephyrBoardEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return zephyr_board;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ZephyrSdkInstallDirEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "zephyr_install_dir"; //$NON-NLS-1$
		
		private String zephyr_install_dir;
		
		private ZephyrSdkInstallDirEnvironmentVariable(String zephyr_install_dir) {
			this.zephyr_install_dir = zephyr_install_dir;
		}
		
		public static ZephyrSdkInstallDirEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = ZephyrPlugin.getDefault().getPreferenceStore();
			String system_zephyr_install_dir = store.getDefaultString(PreferenceConstants.P_ZEPHYR_INSTALL_DIR);
			// TODO: use regex to validate based on boards queried from codi or JSON
			return new ZephyrSdkInstallDirEnvironmentVariable(system_zephyr_install_dir);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ZephyrSdkInstallDirEnvironmentVariable.name)
					: name.equals(ZephyrSdkInstallDirEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return zephyr_install_dir;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ZephyrGccVariantEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "zephyr_gcc_variant"; //$NON-NLS-1$
		
		private String zephyr_gcc_variant;
		
		private ZephyrGccVariantEnvironmentVariable(String zephyr_gcc_variant) {
			this.zephyr_gcc_variant = zephyr_gcc_variant;
		}
		
		public static ZephyrGccVariantEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = ZephyrPlugin.getDefault().getPreferenceStore();
			String system_zephyr_gcc_variant = store.getDefaultString(PreferenceConstants.P_ZEPHYR_GCC_VARIANT);
			
			// TODO: use regex to validate based on boards queried from codi or JSON
			return new ZephyrGccVariantEnvironmentVariable(system_zephyr_gcc_variant);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ZephyrGccVariantEnvironmentVariable.name)
					: name.equals(ZephyrGccVariantEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return zephyr_gcc_variant;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ZephyrBaseEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "zephyr_base"; //$NON-NLS-1$
		
		private static String zephyr_base;
		
		private ZephyrBaseEnvironmentVariable(String zephyr_base) {
			ZephyrBaseEnvironmentVariable.zephyr_base = zephyr_base;
		}
		
		public static ZephyrBaseEnvironmentVariable create(IConfiguration configuration) {
			IPreferenceStore store = ZephyrPlugin.getDefault().getPreferenceStore();
			String system_zephyr_base = store.getDefaultString(PreferenceConstants.P_ZEPHYR_BASE);
			IPreferenceStore crops_store = CropsCorePlugin.getDefault().getPreferenceStore();
			String system_root = crops_store.getDefaultString(org.yocto.crops.core.preferences.PreferenceConstants.P_CROPS_ROOT);
			/* FIXME: this is not what we expect... this is literally "${ProjName}" not the resolved name*/
			String project_name = configuration.getManagedProject().getDefaultArtifactName();
			if (zephyr_base == null) {
				zephyr_base = system_root + "/" + project_name + "/zephyr-project";//system_zephyr_base;
			}
			// TODO: use regex to validate based on boards queried from codi or JSON
			return new ZephyrBaseEnvironmentVariable(zephyr_base);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ZephyrBaseEnvironmentVariable.name)
					: name.equals(ZephyrBaseEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return zephyr_base;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/* Zephyr Project Template env var methods */
	private static class ShortKernelTypeEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "short_kernel_type"; //$NON-NLS-1$
		
		private String short_kernel_type;
		
		private ShortKernelTypeEnvironmentVariable(String short_kernel_type) {
			this.short_kernel_type = short_kernel_type;
		}
		
		public static ShortKernelTypeEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			// TODO: implement the option in extensions, etc.
			IOption option = toolchain.getOptionBySuperClassId("crops.cdt.managedbuild.option.short_kernel_type"); //$NON-NLS-1$
			String short_kernel_type = (String)option.getValue();
//			IPreferenceStore store = CropsCDTCorePlugin.getDefault().getPreferenceStore();
//			String system_short_kernel_type = store.getDefaultString(PreferenceConstants.P_SHORT_KERNEL_TYPE);
//			if (short_kernel_type == "") {
//				short_kernel_type = system_short_kernel_type;
//				try {
//					option.setValue(short_kernel_type);
//				} catch (BuildException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

			// TODO: use regex to validate based on kernel types queried from codi or JSON
			return new ShortKernelTypeEnvironmentVariable(short_kernel_type);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ShortKernelTypeEnvironmentVariable.name)
					: name.equals(ShortKernelTypeEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return short_kernel_type;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class LongKernelTypeEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "long_kernel_type"; //$NON-NLS-1$
		
		private String long_kernel_type;
		
		private LongKernelTypeEnvironmentVariable(String long_kernel_type) {
			this.long_kernel_type = long_kernel_type;
		}
		
		public static LongKernelTypeEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			// TODO: implement the option in extensions, etc.
			IOption option = toolchain.getOptionBySuperClassId("crops.cdt.managedbuild.option.long_kernel_type"); //$NON-NLS-1$
			String long_kernel_type = (String)option.getValue();
//			IPreferenceStore store = CropsCDTCorePlugin.getDefault().getPreferenceStore();
//			String system_long_kernel_type = store.getDefaultString(PreferenceConstants.P_LONG_KERNEL_TYPE);
//			if (long_kernel_type == "") {
//				long_kernel_type = system_long_kernel_type;
//				try {
//					option.setValue(long_kernel_type);
//				} catch (BuildException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

			// TODO: use regex to validate based on kernel types queried from codi or JSON
			return new LongKernelTypeEnvironmentVariable(long_kernel_type);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(LongKernelTypeEnvironmentVariable.name)
					: name.equals(LongKernelTypeEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return long_kernel_type;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class ProjectConfigFilenameEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "prj_conf"; //$NON-NLS-1$
		
		private String prj_conf;
		
		private ProjectConfigFilenameEnvironmentVariable(String prj_conf) {
			this.prj_conf = prj_conf;
		}
		
		public static ProjectConfigFilenameEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			// TODO: implement the option in extensions, etc.
			IOption option = toolchain.getOptionBySuperClassId("crops.cdt.managedbuild.option.project_config_filename"); //$NON-NLS-1$
			String prj_conf = (String)option.getValue();
			// TODO: use regex to validate based on kernel types queried from codi or JSON
			return new ProjectConfigFilenameEnvironmentVariable(prj_conf);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ProjectConfigFilenameEnvironmentVariable.name)
					: name.equals(ProjectConfigFilenameEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return prj_conf;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	private static class ProjectMDefFilenameEnvironmentVariable implements IBuildEnvironmentVariable {
		
		public static String name = "prj_mdef"; //$NON-NLS-1$
		
		private String prj_mdef;
		
		private ProjectMDefFilenameEnvironmentVariable(String prj_mdef) {
			this.prj_mdef = prj_mdef;
		}
		
		public static ProjectMDefFilenameEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			// TODO: implement the option in extensions, etc.
			IOption option = toolchain.getOptionBySuperClassId("crops.cdt.managedbuild.option.project_mdef_filename"); //$NON-NLS-1$
			String prj_mdef = (String)option.getValue();
			// TODO: use regex to validate based on kernel types queried from codi or JSON
			return new ProjectMDefFilenameEnvironmentVariable(prj_mdef);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return CropsUtils.isWin()
					? name.equalsIgnoreCase(ProjectMDefFilenameEnvironmentVariable.name)
					: name.equals(ProjectMDefFilenameEnvironmentVariable.name);
		}
		
		public String getName() {
			return name;
		}
		
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}
		
		public String getValue() {
			return prj_mdef;
		}

		@Override
		public String getDelimiter() {
			return CropsUtils.isWin() ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
