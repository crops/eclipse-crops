package org.yocto.crops.core.builder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeCommandLauncher implements ICommandLauncher {

	public MakeCommandLauncher() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setProject(IProject project) {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showCommand(boolean show) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorMessage(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getCommandArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getEnvironment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandLine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env, IPath workingDirectory,
			IProgressMonitor monitor) throws CoreException {
		ProcessBuilder process_builder = new ProcessBuilder().command("C:/Users/ttorling/.crops/ceed/ceed.exe").redirectErrorStream(true);
		try {
			return process_builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int waitAndRead(OutputStream out, OutputStream err) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return 0;
	}

}
