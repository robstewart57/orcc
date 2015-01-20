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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.sf.orcc.backends.AbstractBackend;
import net.sf.orcc.backends.util.Alignable;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.Instance;
import net.sf.orcc.df.Network;
import net.sf.orcc.df.util.XdfWriter;
import net.sf.orcc.util.Result;

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
		srcPath = outputPath + File.separator + "src";
	
	// -----------------------------------------------------
	// Transformations that will be applied on the Network
	// -----------------------------------------------------
    //
    // e.g. networkTransfos.add(new UnitImporter());
	
	// -------------------------------------------------------------------
	// Transformations that will be applied on children (instances/actors)
	// -------------------------------------------------------------------
    //
    // e.g. childrenTransfos.add(new DfVisitor<Void>(new LoopUnrolling()));
	}
	
	@Override
	protected void beforeGeneration(Network network) {
		network.computeTemplateMaps(); // is this necessary?
	}
	
	@Override
	protected Result doGenerateNetwork(Network network) {
		XdfWriter writer = new XdfWriter();
		File file = new File( pkgNameToPath(network) );
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {e.printStackTrace();}
		writer.write(network, os);
		return Result.newInstance(); // what's actually appropriate here?
	}

	// what is this for? I.e. what's the difference between this and doGenerateInstance(..) ?
	/* @Override
    	protected Result doGenerateActor(Actor actor) {
    		return Result.newInstance();
    	}
    */
	
	@Override
	protected Result doGenerateInstance(Instance instance) {
		Actor actor = instance.getActor();
		String targetPath = srcPath + "/" + actor.getPackage().replace(".", "/");
		new ActorPrinter(actor,actor.getPackage()).printActor( targetPath );
		return Result.newInstance(); 
	}

	@Override
	protected void beforeGeneration(Actor actor) {
		// update "vectorizable" information
		Alignable.setAlignability(actor);
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
		String srcFilePath = path + "/" + fileName ;
		new File(path).mkdirs();
		return srcFilePath ;
	}
}
