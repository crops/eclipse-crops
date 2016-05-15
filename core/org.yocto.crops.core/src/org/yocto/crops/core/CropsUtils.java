package org.yocto.crops.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.yocto.crops.core.Messages;

public class CropsUtils {
	
	private static String platform = System.getProperty("os.name").toLowerCase();
	private static String CODI_IPADDR_FLAG = " -i 127.0.0.1";
	private static String CROPS = "/crops";
	
	public static Set<String> getToolchainContainers()
	{
		/* For now, parse the response from ceed
		 * $ ../crops/ceed/ceed -i 192.168.99.100 -l
		 * 
		 * [INFO] Connected to CODI on 192.168.99.100 port : 10000
		 * [INFO] Host Architecture : mac
		 * [INFO] TURFF Node ID: crops/zephyr:0.6 
		 * [INFO] TURFF Node ID: default 
		 * [INFO] TURFF Node ID: crops/toolchain:i586 
		 */
		//get object which represents the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		 
		//get location of workspace (java.io.File)
		File workspaceDirectory = workspace.getRoot().getLocation().toFile();
		
		/* use http://debuggex.com/ to check regex... but take care with escaped characters */
		Pattern codi_pattern = Pattern.compile("^\\[INFO\\] Connected to CODI on ([0-9.]+) port : ([0-9]+)$");
		Pattern host_arch_pattern = Pattern.compile("^\\[INFO\\] Host Architecture : (linux|mac|win)$");
		Pattern toolchain_pattern = Pattern.compile("^\\[INFO\\] TURFF Node ID: (.+)$");

		java.lang.Runtime rt = java.lang.Runtime.getRuntime();
		try {
			File ceed = new File(workspaceDirectory.getParent() + "/crops/ceed/ceed");
			if (!ceed.exists()) {
				return null;
			}
			if (!CropsUtils.isLinux()) {
				/* on mac and win ip addr is docker default */
				/* TODO: allow a user supplied arbitrary value */
				CODI_IPADDR_FLAG = " -i 192.168.99.100";
			}
			else
			{
				/* no flag needed default of 127.0.0.1 is implied */
				CODI_IPADDR_FLAG = " -i 127.0.0.1";
			}
			java.lang.Process proc = rt.exec(ceed + CODI_IPADDR_FLAG + " -l");
			long timeout = 30L;
			TimeUnit unit = TimeUnit.SECONDS;
			proc.waitFor(timeout, unit);
	        // Get process' output: its InputStream
	        java.io.InputStream stream = proc.getInputStream();
	        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(stream));
	        // And print each line
	        String line = null;
	        Set<String> toolchains = new HashSet<>();
	        while ((line = reader.readLine()) != null) {
	        	Matcher matcher = codi_pattern.matcher(line);
	        	if (matcher.find()) {
	        		String ip_addr = matcher.group(1);
	        		String port = matcher.group(2);
	        		continue;
	        	}
	        	matcher = host_arch_pattern.matcher(line);
	        	if (matcher.find()) {
	        		String host_arch = matcher.group(1);
	        		continue;
	        	}
	        	matcher = toolchain_pattern.matcher(line);
	        	if (matcher.find()){
	        		toolchains.add(matcher.group(1));
	        		continue;
	        	}
	        }
	        stream.close();
	        if (proc.isAlive()) proc.destroy();
	        return toolchains;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isLinux() {
		return (platform.indexOf("linux") >= 0);
	}
	
	public static boolean isMac() {
		return (platform.indexOf("mac") >= 0);
	}

	public static boolean isWin() {
		return (platform.indexOf("win") >= 0);
	}	

	/* "platform" might be, e.g. "windows 8.1"... cast it to short version */
	public static String getPlatform() throws CoreException{
		if (isLinux())
			return "linux";
		else if (isMac())
			return "mac";
		else if (isWin())
			return "win";
		else
			throw new CoreException(new Status(IStatus.ERROR, CropsCorePlugin.getId(), Messages.CropsUtils_platform_not_supported + platform));
	}
	
	public static String getCropsRoot() throws CoreException {
		return CROPS;
	}
	
}
