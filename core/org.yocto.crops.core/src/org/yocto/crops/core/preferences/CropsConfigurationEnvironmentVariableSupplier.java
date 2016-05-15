package org.yocto.crops.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.core.preferences.PreferenceConstants;

public class CropsConfigurationEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (PathEnvironmentVariable.isVar(variableName))
			return PathEnvironmentVariable.create(configuration);
		if (CropsRootEnvironmentVariable.isVar(variableName))
			return CropsRootEnvironmentVariable.create(configuration);
		if (ContainerDispatcherIPAddressEnvironmentVariable.isVar(variableName))
			return ContainerDispatcherIPAddressEnvironmentVariable.create(configuration);
		if (ContainerDispatcherSocketEnvironmentVariable.isVar(variableName))
			return ContainerDispatcherSocketEnvironmentVariable.create(configuration);
		if (ToolchainContainerIDEnvironmentVariable.isVar(variableName))
			return ToolchainContainerIDEnvironmentVariable.create(configuration);
		else
			return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		
		IBuildEnvironmentVariable ceed = PathEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable crops_root = CropsRootEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable codi_ip_addr = ContainerDispatcherIPAddressEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable codi_socket = ContainerDispatcherSocketEnvironmentVariable.create(configuration);
		IBuildEnvironmentVariable toolchain_container_id = ToolchainContainerIDEnvironmentVariable.create(configuration);
		
		List<IBuildEnvironmentVariable> variables = new ArrayList<IBuildEnvironmentVariable>();
		if (ceed != null)
			variables.add(ceed);
		if (crops_root != null)
			variables.add(crops_root);
		if (codi_ip_addr != null)
			variables.add(codi_ip_addr);
		if (codi_socket != null)
			variables.add(codi_socket);
		if (toolchain_container_id != null)
			variables.add(toolchain_container_id);

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
			String path = store.getDefaultString(PreferenceConstants.P_CEED_PATH);
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
			String system_root = store.getDefaultString(PreferenceConstants.P_CROPS_ROOT);
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
			String system_ip_addr = store.getDefaultString(PreferenceConstants.P_CODI_IP_ADDR);
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
			String system_socket = store.getDefaultString(PreferenceConstants.P_CODI_SOCKET);

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
			String system_toolchain_container_id = store.getDefaultString(PreferenceConstants.P_TOOLCHAIN_CONTAINER_ID);

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
}
