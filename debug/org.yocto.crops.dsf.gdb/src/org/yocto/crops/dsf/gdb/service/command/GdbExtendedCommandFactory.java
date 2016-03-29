/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation and others.
 * 
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Intel Corporation - CROPS implementation
 *******************************************************************************/
package org.yocto.crops.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.yocto.crops.dsf.gdb.service.command.commands.MIFileExecAndSymbols;

public class GdbExtendedCommandFactory extends CommandFactory {
	
	@Override
	public ICommand<MIInfo> createMIFileExecAndSymbols(IMIContainerDMContext dmc, String file) {
		/* Call our own version which translates host file to container file
		 * 
		 */
		return new MIFileExecAndSymbols(dmc, file);
	}
}
