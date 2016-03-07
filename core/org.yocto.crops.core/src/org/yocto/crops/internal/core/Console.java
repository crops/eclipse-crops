package org.yocto.crops.internal.core;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class Console implements IConsole {
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	public Console(String consoleName, String contextId) {
		fConsoleManager = CUIPlugin.getDefault().getConsoleManager(consoleName, 
				contextId);
	}
	
//	/**
//	 * Constructor for ConfigureConsole.
//	 */
//	public CConfigureConsole() {
//		fConsoleManager = AutotoolsPlugin.getDefault().getConsoleManager();
//	}
//

	public void start(IProject project ) {
		this.project = project;
		fConsoleManager.getConsole(project).start(project);
	}
	
	/**
	 * @throws CoreException
	 * @see org.eclipse.cdt.core.resources.IConsole#getOutputStream()
	 */
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return fConsoleManager.getConsole(project).getOutputStream();
	}

	public ConsoleOutputStream getInfoStream() throws CoreException {
		return fConsoleManager.getConsole(project).getInfoStream();
	}

	public ConsoleOutputStream getErrorStream() throws CoreException {
		return fConsoleManager.getConsole(project).getErrorStream();
	}
}
