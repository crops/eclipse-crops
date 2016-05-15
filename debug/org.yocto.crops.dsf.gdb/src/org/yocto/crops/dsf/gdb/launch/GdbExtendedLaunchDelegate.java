/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc Khouzam (Ericsson)   - Initial API and implementation
 *******************************************************************************/
package org.yocto.crops.dsf.gdb.launch; 

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.yocto.crops.dsf.gdb.GDBExamplePlugin;
import org.yocto.crops.dsf.gdb.service.GdbExtendedDebugServicesFactory;
import org.yocto.crops.dsf.gdb.service.GdbExtendedDebugServicesFactoryNS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
 
public class GdbExtendedLaunchDelegate extends GdbLaunchDelegate {
	public GdbExtendedLaunchDelegate() {
		super();
	}

    @Override
	protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) throws CoreException {
    	return new GdbExtendedLaunch(configuration, mode, locator);
    }

    @Override
    protected Sequence getServicesSequence(DsfSession session, ILaunch launch, IProgressMonitor rm) {
   		return new GdbExtendedServicesLaunchSequence(session, (GdbLaunch)launch, rm);
    }

    @Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		boolean nonStop = LaunchUtils.getIsNonStopMode(config);
		if (nonStop && isNonStopSupportedInGdbVersion(version)) {
			return new GdbExtendedDebugServicesFactoryNS(version, config);
		}
		return new GdbExtendedDebugServicesFactory(version, config);
	}

	@Override
	protected String getPluginID() {
		return GDBExamplePlugin.PLUGIN_ID;
	}
	
	@Override
	protected IPath checkBinaryDetails(final ILaunchConfiguration config) throws CoreException {
		// First verify we are dealing with a proper project.
		ICProject project = verifyCProject(config);
		// Now verify we know the program to debug.
		// We can verify the Host local path
		// but then we must translate that into the container path
		IPath hostPath = LaunchUtils.verifyProgramPath(config, project);
		String[] segments = hostPath.segments();
		boolean foundworkspace = false;
		Path exePath = new Path("/crops"); //$NON-NLS-1$
		for (String segment : segments) {
			if (segment.equals(project.getElementName()))
					foundworkspace = true;
			if (foundworkspace)
				exePath = new Path(exePath.toPortableString() + Path.SEPARATOR + segment);
		}
		// To allow users to debug with binary parsers turned off, we don't call
		// LaunchUtils.verifyBinary here. Instead we simply rely on the debugger to
		// report any issues with the binary.
		return exePath;
	}
	
//	@Override
//	protected void launchDebugSession( final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor ) throws CoreException {
//		if ( monitor.isCanceled() ) {
//			cleanupLaunch();
//			return;
//		}
//		
//		SessionType sessionType = LaunchUtils.getSessionType(config);
//		boolean attach = LaunchUtils.getIsAttach(config);
//		
//        final GdbLaunch launch = (GdbLaunch)l;
//
//        if (sessionType == SessionType.REMOTE) {
//            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.1") );  //$NON-NLS-1$
//        } else if (sessionType == SessionType.CORE) {
//            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.2") );  //$NON-NLS-1$
//        } else {
//        	assert sessionType == SessionType.LOCAL : "Unexpected session type: " + sessionType.toString(); //$NON-NLS-1$
//            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.3") );  //$NON-NLS-1$
//        }
//        
//        // An attach session does not need to necessarily have an
//        // executable specified.  This is because:
//        // - In remote multi-process attach, there will be more than one executable
//        //   In this case executables need to be specified differently.
//        //   The current solution is to use the solib-search-path to specify
//        //   the path of any executable we can attach to.
//        // - In local single process, GDB has the ability to find the executable
//        //   automatically.
//        if (!attach) {
//        	checkBinaryDetails(config);
//        }
//    	
//        monitor.worked(1);
//
//        // Must set this here for users that call directly the deprecated newServiceFactory(String)
//        fIsNonStopSession = LaunchUtils.getIsNonStopMode(config);
//
//        String gdbVersion = getGDBVersion(config);
//        
//        // First make sure non-stop is supported, if the user want to use this mode
//        if (LaunchUtils.getIsNonStopMode(config) && !isNonStopSupportedInGdbVersion(gdbVersion)) {
//			cleanupLaunch();
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
//            		"Non-stop mode is not supported for GDB " + gdbVersion + ", GDB " + NON_STOP_FIRST_VERSION + " or higher is required.", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        	
//        }
//
//        if (LaunchUtils.getIsPostMortemTracing(config) && !isPostMortemTracingSupportedInGdbVersion(gdbVersion)) {
//			cleanupLaunch();
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
//            		"Post-mortem tracing is not supported for GDB " + gdbVersion + ", GDB " + NON_STOP_FIRST_VERSION + " or higher is required.", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        	
//        }
//
//        launch.setServiceFactory(newServiceFactory(config, gdbVersion));
//
//        // Create and invoke the launch sequence to create the debug control and services
//        IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK); 
//        Sequence servicesLaunchSequence = getServicesSequence(launch.getSession(), launch, subMon1);
//        
//        launch.getSession().getExecutor().execute(servicesLaunchSequence);
//        boolean succeed = false;
//        try {
//            servicesLaunchSequence.get();
//            succeed = true;
//        } catch (InterruptedException e1) {
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
//        } catch (ExecutionException e1) {
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
//        } catch (CancellationException e1) {
//        	// Launch aborted, so exit cleanly
//        	return;
//        } finally {
//        	if (!succeed) {
//        		cleanupLaunch();
//        	}
//        }
//        
//        if (monitor.isCanceled()) {
//			cleanupLaunch();
//			return;
//        }
//        
//        // The initializeControl method should be called after the ICommandControlService
//        // is initialized in the ServicesLaunchSequence above.  This is because it is that
//        // service that will trigger the launch cleanup (if we need it during this launch)
//        // through an ICommandControlShutdownDMEvent
//        launch.initializeControl();
//
//        // Add the GDB process object to the launch.
//        launch.addCLIProcess(getCLILabel(config, gdbVersion));
//
//        monitor.worked(1);
//        
//        // Create and invoke the final launch sequence to setup GDB
//        final IProgressMonitor subMon2 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK); 
//
//        Query<Object> completeLaunchQuery = new Query<Object>() {
//            @Override
//            protected void execute(final DataRequestMonitor<Object> rm) {
//            	DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), launch.getSession().getId());
//            	IGDBControl control = tracker.getService(IGDBControl.class);
//            	tracker.dispose();
//            	control.completeInitialization(new RequestMonitorWithProgress(ImmediateExecutor.getInstance(), subMon2) {
//            		@Override
//            		protected void handleCompleted() {
//            			if (isCanceled()) {
//            				rm.cancel();
//            			} else {
//            				rm.setStatus(getStatus());
//            			}
//            			rm.done();
//            		}
//            	});
//            }
//        };
//
//        launch.getSession().getExecutor().execute(completeLaunchQuery);
//        succeed = false;
//        try {
//        	completeLaunchQuery.get();
//        	succeed = true;
//        } catch (InterruptedException e1) {
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
//        } catch (ExecutionException e1) {
//            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in final launch sequence", e1.getCause())); //$NON-NLS-1$
//        } catch (CancellationException e1) {
//        	// Launch aborted, so exit cleanly
//        	return;
//        } finally {
//            if (!succeed) {
//                // finalLaunchSequence failed. Shutdown the session so that all started
//                // services including any GDB process are shutdown. (bug 251486)
//                cleanupLaunch();
//            }        
//        }
//	}
}
