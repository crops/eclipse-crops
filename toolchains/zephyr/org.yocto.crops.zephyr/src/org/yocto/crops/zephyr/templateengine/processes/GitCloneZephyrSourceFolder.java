/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * 
 * Portions Copyright (c) 2007, 2010 Symbian and others.
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.yocto.crops.zephyr.templateengine.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.templateengine.ProjectCreatedActions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.widgets.Shell;
import org.yocto.crops.core.CropsCorePlugin;


/**
 * Clones Zephyr source tree from a "fat" container Git repository into the project.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GitCloneZephyrSourceFolder extends ProcessRunner {
	private static final String ZEPHYR_SRC = "zephyr-project";
	private static final String CEED_PATH_OPTION = "org.yocto.crops.cdt.core.option.ceed.path";
	private static final String CROPS_ROOT_OPTION = "crops.cdt.managedbuild.option.crops_root";
	private static final String TOOLCHAIN_CONTAINER_ID_OPTION = "crops.cdt.managedbuild.option.toolchain_container_id";
	protected boolean savedAutoBuildingValue;
	protected ProjectCreatedActions pca;
	protected IManagedBuildInfo info;
	
	public GitCloneZephyrSourceFolder() {
		pca = new ProjectCreatedActions();
	}
	
	public void setEnvironment(Map<String, String> env) throws CoreException {
		
	}
	
	// org.eclipse.e4.ui.di.UISynchronize;
	@Inject UISynchronize sync;
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String location = args[1].getSimpleValue();
		String gitURI = args[2].getSimpleValue();
		String gitBranch = args[3].getSimpleValue();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		String workspaceName = ResourcesPlugin.getWorkspace().getRoot().getName();
		//IFolder zephyrSourceDirectory = project.getFolder(ZEPHYR_SRC);
		//if (!project.exists()) {
			try {
				turnOffAutoBuild(workspace);
				
				/* TODO: setEnvironment(Map<String, String> DockerUtils.dockerEnvironment()) */
//				Map<String, String> env = DockerUtils.getDockerEnvironment();

				// get UISynchronize injected as field
				//@Inject UISynchronize sync;

				// more code

				IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
				IToolChain toolchain = buildInfo.getDefaultConfiguration().getToolChain();
				IOption ceed_path = toolchain.getOptionById(CEED_PATH_OPTION);
				IOption crops_root = toolchain.getOptionById(CROPS_ROOT_OPTION);
				IOption toolchain_container_id = toolchain.getOptionById(TOOLCHAIN_CONTAINER_ID_OPTION);
				StringBuffer command = new StringBuffer();
//				command.append(ceed_path.getStringValue().trim());
				command.append("dir");
				/* TODO: spin up the toolchain container on the fly if needed
				 * Needed if workspace has changed (i.e. spin up on workspace start)
				 * Needed if new toolchain has been chosen (i.e spin up on project creation)
				 * 
				 * Because the volume mapped to /crops needs to be the $(workspace_loc)!!!
				 * docker run <toolchain> -v //path//to//workspace:/crops
				 */
//				command.append(" " + "-l");
//				command.append(" " + "-d"); //$NON-NLS-1$
//				
//				command.append(" " + toolchain_container_id.getStringValue().trim());
//				command.append(" " + "-g");
//				command.append(" " + "ls");
//				command.append(" " + "\"git clone --depth 1 --branch" 
//						+ " " + gitBranch + " " + "/zephyr-src/" 
//						+ " " + crops_root.getStringValue().trim() 
//						+  "/" + projectName
//						+ "/" + ZEPHYR_SRC + "\"");
				System.out.println(command);
//				ProgressMonitorDialog dialog = new ProgressMonitorDialog( getActiveWorkbenchShell());
//				IRunnableWithProgress runnable = new IRunnableWithProgress() {
//					@Override
//					public void run(IProgressMonitor monitor)
//						throws InterruptedException {
				// TODO: would need to get an idea of the number of objects... not easy
//						monitor.beginTask("Begin cloning zephyr-project", arg1 /* e.g. number of objects */);
//					}
//				}
				String[] commandArray = {"dir"};
				String commandStr = command.toString();
				Job job = new Job(CropsCorePlugin.getId() + "CeedGitCloneZephyrSourceFolder.Job") {
					private boolean started;
					private boolean done;
					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
						
						while(!done) {
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							if (started) {
								done = true;
							}
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								monitor.done();
								return Status.CANCEL_STATUS;
							}
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				ProcessBuilder processBuilder = new ProcessBuilder(commandArray).inheritIO().redirectErrorStream(true);
//				setEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
//				try {
//				       IRunnableWithProgress op = new Runnable();
//				       new ProgressMonitorDialog(activeShell).run(true, true, op);
//				    } catch (InvocationTargetException e) {
//				       // handle exception
//				    } catch (InterruptedException e) {
//				       // handle cancelation
//				    }
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader (process.getInputStream()));
//					BufferedReader reader = new BufferedReader(new InputStreamReader (process.getErrorStream()));
					String line=null;
					while(reader != null && (line=reader.readLine()) != null) {
//						sync.asyncExec(new Runnable());
						System.out.println(line);
						System.out.flush();
						// TODO: do a UI thread update to Console
					}
					int exitVal = process.waitFor();
					System.out.println("Exited with error code " + exitVal);;
				} catch (IOException e) {
					throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
				} catch (InterruptedException e) {
					throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
				}
				//
//				IPath locationPath = null;
//				if (location != null && !location.trim().equals("")) { //$NON-NLS-1$
//					locationPath = Path.fromPortableString(location);
//				}
//				
//				@SuppressWarnings("unchecked")
//				List<IConfiguration> configs = (List<IConfiguration>) template.getTemplateInfo().getConfigurations();
//				if (configs == null || configs.size() == 0) {
//					throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.4") + projectName); //$NON-NLS-1$
//				}
//				
//				pca.setProject(project);
//				pca.setProjectLocation(locationPath);
//				pca.setConfigs(configs.toArray(new IConfiguration[configs.size()]));
//				pca.setArtifactExtension(artifactExtension);
				// FIXME: projectDescription is invalid
//				info = pca.createProject(monitor, CCorePlugin.DEFAULT_INDEXER, isCProject);
				//ProgressMonitor cloneMonitor = new TextProgressMonitor();
//				if (monitor == null) {
//					cloneMonitor = (ProgressMonitor) new NullProgressMonitor();
//				} else {
//					cloneMonitor = (ProgressMonitor) monitor;
//				}
/* TODO: if repo directory already exists... what do we do? check it or clobber it? */
//				Git projectRepo = Git.cloneRepository()
//				         .setURI(gitURI)
//				         .setProgressMonitor(cloneMonitor)
//				         .setDirectory(new File(location + "/" + projectName + "/zephyr-project"))
//				         .setBranch(gitBranch)
//				         .call();
//				projectRepo.close();
				
//
//				info.setValid(true);
//				ManagedBuildManager.saveBuildInfo(project, true);
//				turnOffGenerateMakefiles(info);
//				restoreAutoBuild(workspace);
				
			} catch (CoreException | IOException e) {
				throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
//			} catch (BuildException e) {
//				throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
//			} catch (InvalidRemoteException e) {
//				throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
//			} catch (TransportException e) {
//				throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
//			} catch (GitAPIException e) {
//				throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.3") + e.getMessage(), e); //$NON-NLS-1$
			} finally {
//		} else {
//			throw new ProcessFailureException(Messages.getString("CeedGitCloneZephyrSourceFolder.5") + projectName); //$NON-NLS-1$
		}
	}


	protected final void turnOffGenerateMakefiles(IManagedBuildInfo info) throws CoreException {
		info.getManagedProject().getBuildProperties().setProperty("managedBuildOn", "false");
		//cProject.setOption("managedBuildOn", "false");
	}
	
	protected final void turnOffAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		savedAutoBuildingValue = workspaceDesc.isAutoBuilding();
		workspaceDesc.setAutoBuilding(false);
		workspace.setDescription(workspaceDesc);
	}
	
	protected final void restoreAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		workspaceDesc.setAutoBuilding(savedAutoBuildingValue);
		workspace.setDescription(workspaceDesc);
	}
	
	/**
	 * setOptionValue
	 */
	protected void setOptionValue(IConfiguration config, IOption option, String val) throws BuildException {
		if (val != null) {
			if (!option.isExtensionElement()) {
				option.setValue(val);
			} else {
				IOption newOption = config.getToolChain().createOption(option, option.getId() + "." + ManagedBuildManager.getRandomNumber(), option.getName(), false); //$NON-NLS-1$
				newOption.setValue(val);
			}
		}
	}
	
	protected Shell getActiveWorkbenchShell() {
		return CropsCorePlugin.getActiveWorkbenchShell();
	}

}
