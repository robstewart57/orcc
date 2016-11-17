/*
 * Copyright (c) 2016, Heriot-Watt University Edinburgh
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
package net.sf.orcc.df.transform;

import static net.sf.orcc.OrccLaunchConstants.PROJECT;
import static net.sf.orcc.util.OrccUtil.getFile;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.df.Action;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.Connection;
import net.sf.orcc.df.DfFactory;
import net.sf.orcc.df.Instance;
import net.sf.orcc.df.Network;
import net.sf.orcc.df.Pattern;
import net.sf.orcc.df.Port;
import net.sf.orcc.df.util.DfUtil;
import net.sf.orcc.df.util.DfVisitor;
import net.sf.orcc.graph.Edge;
import net.sf.orcc.graph.Vertex;
import net.sf.orcc.ir.BlockBasic;
import net.sf.orcc.ir.InstLoad;
import net.sf.orcc.ir.InstStore;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.util.IrUtil;
import net.sf.orcc.util.OrccLogger;
import net.sf.orcc.util.OrccUtil;
import net.sf.orcc.util.Void;
import net.sf.orcc.util.util.EcoreHelper;
import net.sf.orcc.df.util.TransformPreconditionPredicates;

/*
* @author Rob Stewart
* @author Idris Ibrahim
*/
public class DataCollection extends DfVisitor<Void> {
	private static DfFactory dfFactory = DfFactory.eINSTANCE;
	private static IrFactory irFactory = IrFactory.eINSTANCE;

	Network network;
	int dataParallelismCopies;
	List<Instance> instancesToTransform;
	List<Actor> actorsToWriteToFile = new ArrayList<Actor>();

	// IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	// project = root.getProject(getOption(PROJECT, ""));
	// currentResourceSet = new ResourceSetImpl();
	
	/*
	final IFile xdfFile = getFile(project, networkQName, OrccUtil.NETWORK_SUFFIX);
	if (xdfFile == null) {
		throw new OrccRuntimeException(
				"Unable to find the XDF file " + "corresponding to the network " + networkQName + ".");
	} else {
		network = EcoreHelper.getEObject(currentResourceSet, xdfFile);
	}
	*/
	
	public DataCollection()
	{
	}

	@Override
	public Void caseNetwork(Network ignoreNetwork) {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			System.out.println("project: " + project.getName());
			
			try {
				
			for (IResource res : project.members()) {
				List<IFile> xdfFilesInProject = processResource(project,res);
				for (IFile xdfFile : xdfFilesInProject) {
					ResourceSet currentResourceSet = new ResourceSetImpl();
					Network network = EcoreHelper.getEObject(currentResourceSet, xdfFile);
					System.out.println("Actors in XDF file: " + network.getAllActors().size());
				}
			}
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
		// this.network = network;
		/*
		String fileName = "/home/rob/Documents/rathlin/cal-stats/actor-wires.txt";
		File dataFile = new File(fileName);
		try {
			String line = "name actors wires";
			Files.write(Paths.get(fileName), (line+"\n").getBytes(), StandardOpenOption.CREATE);
			line = network.getName() + " "  + network.getAllActors().size() + " " + network.getConnections().size();
			Files.write(Paths.get(fileName), (line+"\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		return null;
	}

	/*
	 * IFolder folder = project.getFolder("Folder1");
       IFile file = folder.getFile("hello.txt");
	 */
	
	
	List<IFile> processResource(IProject project, IResource resource)
	{
	List xdfFiles = new ArrayList<IFile>();
	   IResource[] members;
	   IFolder folder;
	   IFile file;
	 try {
		
	      if (resource instanceof Folder) 
	       {
	         Folder fol = (Folder) resource;
	         for (IResource res : fol.members()) {
	        	 xdfFiles.addAll(processResource(project,res));
	         }
	       }
	      else if (resource instanceof File)
	       {
	    	  File f = (File) resource;
	    	  if (f.getFileExtension().equals("xdf")) {
	    	  // System.out.println("FILE: " + f.getFullPath().toString());

	    	  IFile ifile;
	    	  ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(f.getFullPath());
	    	  if (ifile != null) {
	    	  xdfFiles.add(ifile);
	    	  }
	       }
	       }
	    
	} catch (CoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return xdfFiles;
	}
	
}
