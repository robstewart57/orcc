/*
 * Copyright (c) 2015, Heriot-Watt Univerisity, EPFL SCI STI MM
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.orcc.backends.cal;

import java.io.File;

import net.sf.orcc.backends.AbstractBackend;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.Network;
import net.sf.orcc.df.util.XdfWriter;

/**
 * CAL back-end for CAL source to CAL source transformations
 * 
 * @author Rob Stewart
 * @author Endri Bezati
 * 
 * TODO:
 * 1. create .project file and write to project root i.e. srcPath
 * 2. complete CAL language coverage, see TODO in ActorPrinter.xtend .
 */

public class CALBackend extends AbstractBackend {

	/**
	 * Path to target "src" folder
	 */
	protected String srcPath;

	@Override
	protected void doInitializeOptions() {
		srcPath = path + File.separator + "src";
	}

	@Override
	protected void doTransformActor(Actor actor) {
		// apply actor transformations, e.g.
		/*
		List<DfSwitch<?>> transformations = new ArrayList<DfSwitch<?>>();
		transformations.add(new TypeResizer(true, false, true, false));
		// < .. more transformations .. >
		for (DfSwitch<?> transformation : transformations) {
			transformation.doSwitch(actor);
			if (debug) {
				OrccUtil.validateObject(transformation.toString() + " on "
						+ actor.getName(), actor);
			}
		}
      */
	}

	private String pkgNameToPath(Network network){
		String pkgName = network.getPackage();
		String fileName = new File(network.getFileName()).getName() ;
		return pkgNameToPath(pkgName,fileName);
	}
	
	private String pkgNameToPath(Actor actor){
		String pkgName = actor.getPackage();
		String fileName = new File(actor.getFileName()).getName() ;
		return pkgNameToPath(pkgName,fileName);
	}
	
	private String pkgNameToPath(String pkgName, String fileName){
		String path = srcPath + "/" + pkgName.replace(".", "/") ;
		String srcFilePath = path + fileName ;
		new File(path).mkdirs();
		return srcFilePath ;
	}
	
	@Override
	protected void doXdfCodeGeneration(Network network) {
		XdfWriter writer = new XdfWriter();
		File file = new File( pkgNameToPath(network) );
		writer.write(file, network);
	}

	@Override
	protected boolean printActor(Actor actor) {
		new ActorPrinter(actor,actor.getPackage()).printActor( pkgNameToPath(actor) );
		return true;
	}
}
