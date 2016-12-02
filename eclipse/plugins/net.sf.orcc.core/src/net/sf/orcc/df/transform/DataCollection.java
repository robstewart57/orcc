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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.df.Action;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.Connection;
import net.sf.orcc.df.DfFactory;
import net.sf.orcc.df.FSM;
import net.sf.orcc.df.Instance;
import net.sf.orcc.df.Network;
import net.sf.orcc.df.Pattern;
import net.sf.orcc.df.Port;
import net.sf.orcc.df.State;
import net.sf.orcc.df.Transition;
import net.sf.orcc.df.util.DfUtil;
import net.sf.orcc.df.util.DfVisitor;
import net.sf.orcc.graph.Edge;
import net.sf.orcc.graph.Vertex;
import net.sf.orcc.ir.Block;
import net.sf.orcc.ir.BlockBasic;
import net.sf.orcc.ir.InstAssign;
import net.sf.orcc.ir.InstLoad;
import net.sf.orcc.ir.InstStore;
import net.sf.orcc.ir.Instruction;
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

	public DataCollection() {
	}

	@Override
	public Void caseNetwork(Network ignoreNetwork) {

		// actorsVersusWires("/home/rob/Documents/rathlin/cal-stats/actor-wires.txt",getWorkspaceXdfNetworks());

//		System.out.println("getting XDF network files");
		String dir = "/home/rob/Documents/rathlin-shared/cal-stats/";
		List<NetworkTuple> workspaceXdfNetworks = getWorkspaceXdfNetworks();

		//		System.out.println("actors vs wires");
//		actorsVersusWires(dir + "actors-wires.txt", workspaceXdfNetworks);
//		System.out.println("IO port counts");
//		actorInputPorts(dir + "actors-ports.txt", workspaceXdfNetworks);
//		System.out.println("action counts");
//		actorActions(dir + "actors-actions.txt", workspaceXdfNetworks);
//		System.out.println("actions mutating global vars");
//		mutableGlobalVarsAndStmts(dir + "mutating-actions.txt", dir + "statements-count.txt", workspaceXdfNetworks);
//		System.out.println("pipelinable actors");
//		pipelinableActor(dir + "pipelinableActors.txt", workspaceXdfNetworks);
//		System.out.println("independant input ports");
//		independantInputPorts(dir + "independant-input-ports.txt", workspaceXdfNetworks);
//		System.out.println("number of global variables");
//		multipleActionsNoGlobalVars(dir + "global-vars.txt", workspaceXdfNetworks);
//		
		System.out.println("FSM analysis");
		fsmAnalysis(dir + "fsm-types.txt", workspaceXdfNetworks);

		return null;
	}

	public class NetworkTuple<IProject, Network> {
		public final IProject project;
		public final Network network;

		public NetworkTuple(IProject project, Network network) {
			this.project = project;
			this.network = network;
		}
	}

	
	public void fsmAnalysis(String fsmFilename, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor fsmType";
			java.io.File f = new java.io.File(fsmFilename);
			 List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(fsmFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();
				
				for (Actor act : actors) {					
					if (! actorsRecorded.contains(act.getName())) {
					//	System.out.println("processing " + act.getName());

					FSM fsm = act.getFsm();
					String fsmType = "";
					if (fsm != null) {
						boolean isAcyclic = isAcyclic(fsm);
						boolean hasBranching = hasBranching(fsm);
						
						if (isAcyclic && hasBranching) {
							fsmType = "acyclic-branching";
						}
						else if (isAcyclic) {
							fsmType = "acyclic";
							
							System.out.println(act.getName() + " is acyclic, numver of global vars: " + act.getStateVars().size());
							
							
						}
						else {
							fsmType = "cyclic";
						}
					}
					else {
						fsmType = "runtime-fsm";
					}
					
					dataLine = act.getName() + " " + fsmType;
					Files.write(Paths.get(fsmFilename), (dataLine + "\n").getBytes(),
							StandardOpenOption.APPEND);
					
					actorsRecorded.add(act.getName());
					}
				}
			}
			
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public boolean hasBranching(FSM fsm) {
		return hasBranching(fsm,fsm.getInitialState(), new ArrayList<State>());
	}
	
	public boolean hasBranching(FSM fsm, State thisState, List<State> visited) {
		boolean hasBranches = false;
		List<Transition> transitions = fsm.getTransitions();
		List<Transition> fromHere = new ArrayList<Transition>();
		for (Transition t : transitions) {
			if (t.getSource() == thisState) {
				fromHere.add(t);
			}
		}
		if (fromHere.size() > 1) {
			hasBranches = true;
		}
		else {
			boolean b = false;
			for (Transition t : fromHere) {
				if (!b) {
					if (!visited.contains(thisState)) {
						visited.add(thisState);
					  b = hasBranching(fsm,t.getTarget(),visited);
					}
				}
			}
			hasBranches = b;
		}
		return hasBranches;
	}
	
	public boolean isAcyclic(FSM fsm) {
		//if (fsm.getInitialState() != null) {
		  State initialState = fsm.getInitialState();
		  return isAcyclic(fsm,new ArrayList<State>(), initialState,initialState);
	//}
		//else return true;
	}
	
	public boolean isAcyclic(FSM fsm, List<State> predecessors, State initialState, State thisState) {
		boolean isAcyclic = true;
		List<Transition> transitions = fsm.getTransitions();
		List<Transition> fromHere = new ArrayList<Transition>();
		for (Transition t : transitions) {
			if (t.getSource() == thisState) {
				fromHere.add(t);
			}
		}
		
		if (fromHere.size() == 0) {
			isAcyclic = true;
		}
		else if (fromHere.size() == 1
				 && fromHere.get(0).getTarget() == initialState ) {
			isAcyclic = true;
		}
		else {
			boolean stillAcyclic = true;
			for (Transition t : fromHere) {
				if (stillAcyclic) {
					// this can set stillAcyclic to false.
					stillAcyclic = ! predecessors.contains(t.getTarget());
				}
			}
			
			for (Transition t : fromHere) {
				if (stillAcyclic) {
					predecessors.add(thisState);
					stillAcyclic = isAcyclic(fsm,predecessors, initialState, t.getTarget());
				}
			}
			isAcyclic = stillAcyclic;
		}
		
		
		return isAcyclic;
	}
	
	public void multipleActionsNoGlobalVars(String noGlobalVarsFilenam, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor actions globalVars";
			java.io.File f = new java.io.File(noGlobalVarsFilenam);
			// List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(noGlobalVarsFilenam), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();
				
				for (Actor act : actors) {
					
					dataLine = act.getName() + " " + act.getActions().size() + " " + act.getStateVars().size();
					Files.write(Paths.get(noGlobalVarsFilenam), (dataLine + "\n").getBytes(),
						StandardOpenOption.APPEND);
					
//					for (Port p : act.getInputs()) {
//						int consumersOfP = 0;
//						for (Action action : act.getActions()) {
//							Pattern patt = action.getInputPattern();
//							for (Var var : patt.getVariables()) {
//								if (var.getName().equals(p.getName())) {
//									consumersOfP++;
//								}
//							}
//						}
//						dataLine = act.getName() + " " + p.getName() + " " + consumersOfP;
//						Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(),
//								StandardOpenOption.APPEND);
//					}
				}
			}
			
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	
	public void independantInputPorts(String pipelinableFilename, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor inputPort consumers";
			java.io.File f = new java.io.File(pipelinableFilename);
			// List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(pipelinableFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();
				
				for (Actor act : actors) {
					for (Port p : act.getInputs()) {
						int consumersOfP = 0;
						for (Action action : act.getActions()) {
							Pattern patt = action.getInputPattern();
							for (Var var : patt.getVariables()) {
								if (var.getName().equals(p.getName())) {
									consumersOfP++;
								}
							}
						}
						dataLine = act.getName() + " " + p.getName() + " " + consumersOfP;
						Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(),
								StandardOpenOption.APPEND);
					}
				}
			}
			
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	
	public void pipelinableActor(String pipelinableFilename, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor pipelineable";
			java.io.File f = new java.io.File(pipelinableFilename);
			List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(pipelinableFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					if (!actorsRecorded.contains(act.getName())) {
						if (act.getActions().size() != 1) {
						dataLine = act.getName() + " no";
						Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(),
								StandardOpenOption.APPEND);
						}
						else {
							Action action = act.getActions().get(0);
							Procedure proc = action.getBody();
							int basicBlocks = 0;
							for (Block block : proc.getBlocks()) {
								if (block.isBlockBasic()) {
									basicBlocks++;
								}
							}
							switch (basicBlocks) {
							case 1: dataLine = act.getName() + " yes";
							default: dataLine = act.getName() + " no";
							}
							Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(),
									StandardOpenOption.APPEND);
						}
						actorsRecorded.add(act.getName());
					}
				}
			}
			
			// hack, to make it appear
			dataLine = "fake yes";
			Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(),
					StandardOpenOption.APPEND);
			
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public void mutableGlobalVarsAndStmts(String globalVarsFilename, String statementsCountFilename,
			List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor var actions";
			java.io.File f = new java.io.File(globalVarsFilename);
			List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(globalVarsFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			titleLine = "actor action statements";
			f = new java.io.File(statementsCountFilename);
			actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(statementsCountFilename), (titleLine + "\n").getBytes(),
					StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					if (!actorsRecorded.contains(act.getName())) {
						if (act != null && act.getName() != null && act.getInputs() != null
								&& act.getOutputs() != null) {
							for (Var var : act.getStateVars()) {
								int actionsCount = howManyActionsMutateVar(var, act.getActions());
								dataLine = act.getName() + " " + var.getName() + " " + actionsCount;
								Files.write(Paths.get(globalVarsFilename), (dataLine + "\n").getBytes(),
										StandardOpenOption.APPEND);

								for (Action action : act.getActions()) {
									dataLine = act.getName() + " " + action.getName() + " " + numStmtsInAction(action);
									Files.write(Paths.get(statementsCountFilename), (dataLine + "\n").getBytes(),
											StandardOpenOption.APPEND);
								}
							}
						}
						actorsRecorded.add(act.getName());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int numStmtsInAction(Action action) {
		int i = 0;
		Procedure proc = action.getBody();
		for (Block block : proc.getBlocks()) {
			if (block.isBlockBasic()) {
				BlockBasic bb = (BlockBasic) block;
				i += bb.getInstructions().size();
			}
		}
		return i;
	}

	private int howManyActionsMutateVar(Var var, List<Action> actions) {
		int i = 0;
		for (Action action : actions) {
			Procedure proc = action.getBody();
			for (Block block : proc.getBlocks()) {
				if (block.isBlockBasic()) {
					BlockBasic bb = (BlockBasic) block;
					for (Instruction inst : bb.getInstructions()) {
						if (inst.isInstAssign()) {
							InstAssign ass = (InstAssign) inst;
							if (ass.getTarget().getVariable().getName().equals(var.getName())) {
								i++;
							}
						} else if (inst.isInstStore()) {
							InstStore st = (InstStore) inst;
							if (st.getTarget().getVariable().getName().equals(var.getName())) {
								i++;
							}
						}
					}
				}
			}
		}
		return i;
	}

	public void actorActions(String filename, List<NetworkTuple> networkTuples) {
		try {
			Network thisNetwork;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor actions";
			java.io.File f = new java.io.File(filename);
			List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(filename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					if (!actorsRecorded.contains(act.getName())) {
						if (act != null && act.getName() != null && act.getInputs() != null
								&& act.getOutputs() != null) {
							dataLine = act.getName() + " " + act.getActions().size();
							Files.write(Paths.get(filename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);
						}
						actorsRecorded.add(act.getName());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void actorInputPorts(String filename, List<NetworkTuple> networkTuples) {
		try {

			IProject project;
			Network thisNetwork;
			Actor act = null;
			String dataLine;
			Iterator<Actor> actors;
			String titleLine = "actor inputPorts outputPorts";
			java.io.File f = new java.io.File(filename);
			f.delete();
			Files.write(Paths.get(filename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			List<String> actorsRecorded = new ArrayList<String>();

			int j = 0;
			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				project = networkTuple.project;
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors().iterator();
				while (actors.hasNext())
					act = actors.next();
				// for (Actor act : thisNetwork.getAllActors()) {
				if (!actorsRecorded.contains(act.getName())) {
					if (act != null && act.getName() != null && act.getInputs() != null && act.getOutputs() != null) {
						dataLine = act.getName() + " " + act.getInputs().size() + " " + act.getOutputs().size();
						Files.write(Paths.get(filename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);
					}
					actorsRecorded.add(act.getName());
				}
				// }

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void actorsVersusWires(String filename, List<NetworkTuple> networkTuples) {
		try {

			String titleLine = "name actors wires";
			java.io.File f = new java.io.File(filename);
			f.delete();
			Files.write(Paths.get(filename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				IProject project = networkTuple.project;
				Network thisNetwork = networkTuple.network;

				String projName = project.getName();
				String netName = thisNetwork.getName();
				int numActors = thisNetwork.getAllActors().size();
				int numWires = countAllNestedConnections(thisNetwork);

				String dataLine = projName + "-" + netName + " " + numActors + " " + numWires;
				Files.write(Paths.get(filename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<NetworkTuple> getWorkspaceXdfNetworks() {
		List<NetworkTuple> networkFiles = new ArrayList<NetworkTuple>();
		Network network;
		ResourceSetImpl currentResourceSet = new ResourceSetImpl();
		List<IFile> xdfFilesInProject;

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			try {
				for (IResource res : project.members()) {
					xdfFilesInProject = processResource(project, res);
					for (IFile xdfFile : xdfFilesInProject) {
						network = EcoreHelper.getEObject(currentResourceSet, xdfFile);
						if (network != null) {
							networkFiles.add(new NetworkTuple<IProject, Network>(project, network));
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return networkFiles;
	}

	/* maybe this could be an Orcc API method? */
	int countAllNestedConnections(Network network) {
		int i = network.getConnections().size();
		for (Vertex v : network.getChildren()) {
			Instance inst = (Instance) v;
			if (inst.isNetwork()) {
				Network subNetwork = inst.getNetwork();
				i += countAllNestedConnections(subNetwork);
			}
		}
		return i;
	}

	List<IFile> processResource(IProject project, IResource resource) {
		List<IFile> xdfFiles = new ArrayList<IFile>();
		IResource[] members;
		IFolder folder;
		IFile file;
		try {
			if (resource instanceof Folder) {
				Folder fol = (Folder) resource;
				for (IResource res : fol.members()) {
					xdfFiles.addAll(processResource(project, res));
				}
			} else if (resource instanceof File) {
				File f = (File) resource;
				if (f != null && f.getFileExtension() != null && f.getFileExtension().equals("xdf")) {
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
