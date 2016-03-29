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

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.yocto.crops.dsf.gdb.service.command.commands.CLIGDBVersion;
import org.yocto.crops.dsf.gdb.service.command.commands.MIEnvironmentCD;

public class GdbExtendedCommandFactory_6_8 extends CommandFactory_6_8 {
	
	@Override
	public ICommand<MIInfo> createMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		/* Call our own version which translates host path to container path
		 * 
		 * Thanks to Jonah Graham <jonah@kichwacoders.com> for the suggestion.
		 */
		return new MIEnvironmentCD(ctx, path);
	}
	
	@Override
	public ICommand<MIBreakInsertInfo> createMIDPrintfInsert(
			IBreakpointsTargetDMContext ctx, boolean isTemporary,
			String condition, int ignoreCount, int tid, boolean disabled,
			String location, String printfStr) {
		// Prefix all dynamic printf with the [EX] tag
		printfStr = printfStr.replaceFirst("^\"", "\"[EX] "); //$NON-NLS-1$ //$NON-NLS-2$
		return super.createMIDPrintfInsert(ctx, isTemporary, condition, ignoreCount,
				tid, disabled, location, printfStr);
	}
	
	public ICommand<MIGDBVersionInfo> createCLIGDBVersion(ICommandControlDMContext ctx) {
		return new CLIGDBVersion(ctx);
	}
}
