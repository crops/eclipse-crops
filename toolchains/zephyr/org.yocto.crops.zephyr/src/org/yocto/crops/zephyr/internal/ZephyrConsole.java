package org.yocto.crops.zephyr.internal;

import java.io.IOException;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.yocto.crops.zephyr.Messages;

public class ZephyrConsole implements IResourceChangeListener {
	
	private static MessageConsole console;
	private static MessageConsoleStream out;
	private static MessageConsoleStream err;
	
	private IFolder buildDirectory;
	
	IProject project;
	IBuildConsoleManager fConsoleManager;
	
	public ZephyrConsole() {
		if (console == null) {
			console = new MessageConsole(Messages.ZephyrConsole_0, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
			out = console.newMessageStream();
			err = console.newMessageStream();
			
			// set the colors
			final Display display = Display.getDefault();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					out.setColor(display.getSystemColor(SWT.COLOR_BLACK));
					err.setColor(display.getSystemColor(SWT.COLOR_RED));
				}
			});
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_BUILD);
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.PRE_BUILD:
			if (event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD) {
				// TODO: this really should be done from the core and only
				// when our projects are buing built
				console.clearConsole();
			}
			break;
		}
	}
	
	public IFolder getBuildDirectory() {
		return buildDirectory;
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
	
	public void writeOutput(String msg) throws IOException {
		out.write(msg);
	}

	public void writeError(String msg) throws IOException {
		err.write(msg);
	}

}
