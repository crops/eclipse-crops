/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.yocto.crops.zephyr.templateengine.processes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.zephyr.ZephyrPlugin;
import org.yocto.crops.zephyr.ZephyrUtils;
import org.yocto.crops.zephyr.internal.ZephyrConsole;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecParameter;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Creates a clone of the zephyr source into the project.
 */
public class ContainerGitCloneZephyrSourceFolder extends ProcessRunner {
	private static DockerClient docker;
	private static Container container;
	private static String container_id;
	private static String execId;
	private static final ZephyrConsole console = ZephyrPlugin.getConsole();
	
	/* TODO: make this into a Runnable ? */
	public static int git_clone(DockerClient client, Container container, String gitURI, String gitBranch, String target) {
		int exitCode = -1;
		LogMessage message = null;
    	try {
			execId = client.execCreate(container_id, new String[] {"git", "clone", "--depth", "1", "--no-local", "--branch", gitBranch, gitURI, target},
					ExecParameter.STDOUT,
					ExecParameter.STDERR);
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			LogStream stream = client.execStart(execId);
			while(client.execInspect(execId).running() && stream.hasNext()) {
				message = stream.peek();
				/* TODO need to find new line characters and print one line */
				ByteBuffer bb = message.content();
				while(bb.remaining() > 0) {
					int start = bb.position(), end;
					for (end = start; end < bb.limit(); end++) {
						byte b = bb.get(end);
						if (b == '\r' || b == '\n')
							break;
					}
					byte[] bytes = new byte[end - start];
					bb.get(bytes);
					String line = new String(bytes, "UTF-8");
					/* TODO: if end == \r just print not println? */
					byte b = bb.get(end);
					if (b == '\n' || b == '\r')
						/* TODO: output to process log or dialog box, not System.out */
						console.writeOutput(line + "\n");
//						System.out.println(line);
					end++;
					bb.position(end);
				}
				stream.next();
			}
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
    		exitCode = docker.execInspect(execId).exitCode();
    		console.writeOutput("Exited with exit code: " + exitCode + "\n");
    		/* We should only use System.out when debugging/development of the plugin */
//			System.out.println("Exited with exit code: " + exitCode + "\n");
			return exitCode;
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return exitCode;
		
	}

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {

			String projectName = args[0].getSimpleValue();
			String target = args[1].getSimpleValue();
			String gitURI = args[2].getSimpleValue();
			String gitBranch = args[3].getSimpleValue();
			String containerName = args[4].getSimpleValue();
			
			/* see http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F */
//			IConsole console = 
//			IWorkbenchPage page = ResourcesPlugin.getWorkspace().
//			String id = IConsoleConstants.ID_CONSOLE_VIEW;
//			IConsoleView view = (IConsoleView) page.showView(id);
//			view.display(console);
					
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

			// we WANT the project to exist
			if (project.exists()) {
				// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
		    	// This assumes you have run 'eval $(docker-machine env default)'
				try {
					if (!CropsUtils.isLinux()) 
						/* default URI is 192.168.99.100 and PORT is 2376 */
						/* This version of docker-client (3.1.10) requires
						 * HTTPS type URI, tcp:// throws an error
						 */
						docker = DefaultDockerClient.builder()
						.uri(URI.create("https://192.168.99.100:2376"))
						.dockerCertificates(new DockerCertificates(Paths.get(ZephyrUtils.getDefaultHome() + "/.docker/machine/machines/default/")))
						.build();
					} catch (DockerCertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	try {
					List<Container> containers = docker.listContainers();
					for(Container container : containers) {
						for( String name : container.names())
						{
							if(name.contains(containerName))
							{
								container_id = container.id();
								this.container = container;
								/* TODO need to create a dialog box or progress monitor here */
								break;
							} else {
								container_id = null;
							}
						}
						if (container_id != null)
							break;
					}
				} catch (DockerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	monitor.beginTask("git clone", 100);
				git_clone(docker, container, gitURI, gitBranch, target);
				monitor.done();

			} else {
//				throw new ProcessFailureException(Messages.getString("NewManagedProject.5") + projectName); //$NON-NLS-1$
			}
		}
	
}
