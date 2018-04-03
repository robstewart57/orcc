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

import net.sf.orcc.ir.ExprVar;
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
import net.sf.orcc.ir.BlockIf;
import net.sf.orcc.ir.BlockWhile;
import net.sf.orcc.ir.ExprBinary;
import net.sf.orcc.ir.InstAssign;
import net.sf.orcc.ir.InstLoad;
import net.sf.orcc.ir.InstStore;
import net.sf.orcc.ir.Instruction;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.OpBinary;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.util.IrUtil;
import net.sf.orcc.util.OrccLogger;
import net.sf.orcc.util.OrccUtil;
import net.sf.orcc.util.Void;
import net.sf.orcc.util.util.EcoreHelper;

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

		// System.out.println("getting XDF network files");
		String dir = "/home/rob/Documents/rathlin-shared/cal-stats/";
		List<NetworkTuple> workspaceXdfNetworks = getWorkspaceXdfNetworks();

//		 System.out.println("actors vs wires");
//		 actorsVersusWires(dir + "actors-wires.txt", workspaceXdfNetworks);
//		 System.out.println("IO port counts");
//		 actorInputOutputPorts(dir + "actors-ports.txt", workspaceXdfNetworks);
		 System.out.println("action counts");
		 actorActions(dir + "actors-actions.txt", workspaceXdfNetworks);
		// System.out.println("actions mutating global vars");
		// mutableGlobalVarsAndStmts(dir + "mutating-actions.txt", dir +
		// "statements-count.txt", workspaceXdfNetworks);
		// System.out.println("pipelinable actors");
		// pipelinableActor(dir + "pipelinableActors.txt",
		// workspaceXdfNetworks);
		// System.out.println("independant input ports");
		// independantInputPorts(dir + "independant-input-ports.txt",
		// workspaceXdfNetworks);

		// System.out.println("independant output ports");
		// independantOutputPorts(dir + "independant-output-ports.txt",
		// workspaceXdfNetworks);

		// System.out.println("number of global variables");
		// multipleActionsNoGlobalVars(dir + "global-vars.txt",
		// workspaceXdfNetworks);
		//
		// System.out.println("FSM analysis");
		// fsmAnalysis(dir + "fsm-types.txt", workspaceXdfNetworks);
		//
		// System.out.println("Purity analysis");
		// isPure(dir + "purity.txt", workspaceXdfNetworks);
		//
		// System.out.println("Repeat pattern refactor potential");
		// hasRepeatPatternPotential(dir + "repeat-potential.txt",
		// workspaceXdfNetworks);

//		 System.out.println("Determinate action");
//		 nonDeterminateAction(dir + "determinate-action.txt",
//		 workspaceXdfNetworks);
		 
//		 System.out.println("Global variable count");
//		 actorHasGlobalVariables(dir + "global-variable-count.txt",
//		 workspaceXdfNetworks);
		 

//		System.out.println("Guard analysis");

//		guards(dir + "guards.txt", workspaceXdfNetworks);

		
		System.out.println("Acyclic non-branching consumption");
		consumptionAcyclicNoBranching(dir + "acyclic-non-branching.txt", workspaceXdfNetworks);

//		guards(dir + "guards.txt", workspaceXdfNetworks);	
		//System.out.println("Acyclic non-branching consumption");
		//consumptionAcyclicNoBranching(dir + "acyclic-non-branching.txt", workspaceXdfNetworks);


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
	
	private int consumptionAcyclicNoBranching(String acyclicNoBranching, List<NetworkTuple> networkTuples) {
		int consumption = 0;
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor consumption";
			java.io.File f = new java.io.File(acyclicNoBranching);
			// List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(acyclicNoBranching), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				System.out.println("network: " + thisNetwork.getName() + " " + thisNetwork.getAllActors().size());
				actors = thisNetwork.getAllActors();
				FSM fsm;
				for (Actor act : actors) {
					fsm = act.getFsm();
					System.out.println("analysing " + act.getName());
					if (fsm != null) {
					System.out.println(isAcyclic(fsm) + " " + !hasBranching(fsm));
					}
					if (fsm != null && isAcyclic(fsm) && !hasBranching(fsm)) {
						
						List<State> visited = new ArrayList<State>();
						State thisState = fsm.getInitialState();
						// State nextState;
						Transition tran;
						int consumed = 0;
						while (!visited.contains(thisState)) {
							tran = getTargetTransitions(fsm,thisState).get(0);
							consumed += actionConsumption(tran.getAction());
							visited.add(thisState);
							thisState = tran.getTarget();
						}
						dataLine = act.getName() + " " + consumed;
						Files.write(Paths.get(acyclicNoBranching), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);

						
					}
				}
				
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return consumption;
	}
	
	private int actionConsumption(Action action) {
		return action.getInputPattern().getVariables().size();
	}
	
	private List<Transition> getTargetTransitions(FSM fsm, State source) {
		List<Transition> targets = new ArrayList<Transition>();
		for (Transition tran : fsm.getTransitions()) {
			if (tran.getSource() == source) {
				targets.add(tran);
			}
		}
		
		return targets;
	}

	public void guards(String guardCounts, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor actions tokenGuards sharedVarGuards";
			java.io.File f = new java.io.File(guardCounts);
			// List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(guardCounts), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {

					for (Action action : act.getActions()) {
						int tokenGuards = 0;
						int sharedVarGuards = 0;
						BlockBasic guardBlock = (BlockBasic) action.getScheduler().getBlocks().get(0);
						for (Instruction inst : guardBlock.getInstructions()) {
							// System.out.println(inst);
							List<String> loadedVars = new ArrayList<String>();
							if (inst instanceof InstLoad) {
								InstLoad instLoad = (InstLoad) inst;
								Var loadedVar = instLoad.getSource().getVariable();

								if (!loadedVars.contains(loadedVar.getName())) {
									// if
									// (act.getName().equals("avs.decode.intra.Algo_IntraPred_CHROMA_8x8_AVSJZ"))
									// {
									// System.out.println(action.getName() + " :
									// " + loadedVar.getName());
									// }

									boolean isGlobal = false;
									boolean isTokenVal = false;
									for (Var stateVar : act.getStateVars()) {
										if (stateVar.getName().equals(loadedVar.getName())) {
											isGlobal |= true;
										}
									}
									if (isGlobal) {
										sharedVarGuards++;
									} else {
										for (Var tokenVar : action.getInputPattern().getVariables()) {
											if (tokenVar.getName().equals(loadedVar.getName())) {
												isTokenVal |= true;
											}
										}
										if (isTokenVal) {
											tokenGuards++;
										}
									}
									loadedVars.add(loadedVar.getName());
								}
							}
						}

						dataLine = act.getName() + " " + action.getName() + " " + tokenGuards + " " + sharedVarGuards;
						Files.write(Paths.get(guardCounts), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);

					}

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void nonDeterminateAction(String nonDeterminateAction, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor actions isDeterminate";
			java.io.File f = new java.io.File(nonDeterminateAction);
			// List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(nonDeterminateAction), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					
					List<String> globVarNames = new ArrayList<String>();
					for (Var v : act.getStateVars()) {
						globVarNames.add(v.getName());
					}

					for (Action action : act.getActions()) {
						String s = isDeterminate(action.getBody(),globVarNames) ? "yes" : "no";
						dataLine = act.getName() + " " + action.getName() + " " + s;
						Files.write(Paths.get(nonDeterminateAction), (dataLine + "\n").getBytes(),
								StandardOpenOption.APPEND);

					}

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isDeterminate(Procedure proc,List<String> globalVars) {
		boolean b = true;
		for (Block block : proc.getBlocks()) {
			b &= isDeterminate(block,globalVars);
		}
		return b;
	}

	private boolean isDeterminate(Block block,List<String> globalVars) {
		boolean b = true;
		if (block instanceof BlockWhile) {
			for (Block bl : ((BlockWhile) block).getBlocks()) {
				b &= isDeterminate(bl,globalVars);
			}
			//b = isDeterminate(block.getProcedure(),globalVars);
		} else if (block instanceof BlockIf) {
			BlockIf ifBlock = (BlockIf) block;
			List<Block> thenElseBlocks = ifBlock.getThenBlocks();
			thenElseBlocks.addAll(ifBlock.getElseBlocks());
			for (Block bl : thenElseBlocks) {
				b &= isDeterminate(bl,globalVars);
			}
			// b = isDeterminate(block.getProcedure(),globalVars);
		}
		else if (block instanceof BlockBasic) {
			b &= !doesBlockAssignToGlobalVar((BlockBasic) block, globalVars);
		}
		return b;
	}
	
	private boolean doesBlockAssignToGlobalVar(BlockBasic block,List<String> globalVars) {
		boolean b = false;
		
		for (Instruction inst : block.getInstructions()) {
			if (inst.isInstAssign()) {
				InstAssign ass = (InstAssign) inst;
				boolean assignsToGlobalVar = false;
				for(String str: globalVars) {
				    if(str.trim().contains(ass.getTarget().getVariable().getName()))
				    {
				      assignsToGlobalVar = true;
				      System.out.println("TTRRRUUUEEE");
				    }
				}
				
				b |= assignsToGlobalVar;
			}
		}
		
		return b;
	}

	public void hasRepeatPatternPotential(String repeatFilename, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor action repeatCandidate";
			java.io.File f = new java.io.File(repeatFilename);
			List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(repeatFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			BlockBasic basicBlock;
			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					if (!actorsRecorded.contains(act.getName())) {
						for (Action action : act.getActions()) {
							String isRepeatCandidate = "no";
							Var countVar = null;
							basicBlock = (BlockBasic) action.getScheduler().getBlocks().get(0);
							// LOAD, ASSIGN (==), or RETURN
							List<Instruction> insts = basicBlock.getInstructions();

							for (Instruction inst : insts) {
								if (inst instanceof InstAssign) {
									InstAssign ass = (InstAssign) inst;

									if (ass.getValue() instanceof ExprBinary) {
										ExprBinary exprBin = (ExprBinary) ass.getValue();
										if ((exprBin.getOp() == OpBinary.LT
												&& action.getPeekPattern().getVariables().size() == 1)
												|| (exprBin.getOp() == OpBinary.EQ
														&& action.getPeekPattern().getVariables().isEmpty())) {
											if (exprBin.getE1().isExprVar() && exprBin.getE1().getType().isInt()) {
												countVar = ((ExprVar) exprBin.getE1()).getUse().getVariable();
												if (isAssignedTo(countVar, action.getBody())) {
													// System.out.println("FOUND
													// " + act.getName() + " " +
													// action.getName() + " " +
													// countVar.getName());
													isRepeatCandidate = "yes";
												}
											} else if (exprBin.getE2().isExprVar()
													&& exprBin.getE2().getType().isInt()) {
												countVar = ((ExprVar) exprBin.getE2()).getUse().getVariable();
												if (isAssignedTo(countVar, action.getBody())) {
													isRepeatCandidate = "yes";
													// System.out.println("FOUND
													// " + act.getName() + " " +
													// action.getName() + " " +
													// countVar.getName());
												}
											}
										}
									}
								}
							}

							dataLine = act.getName() + " " + action.getName() + " " + isRepeatCandidate;
							Files.write(Paths.get(repeatFilename), (dataLine + "\n").getBytes(),
									StandardOpenOption.APPEND);

						}

						actorsRecorded.add(act.getName());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isGlobalConst(Actor actor, String localVarName) {
		boolean isConst = false;

		String varName = localVarName.substring(6, localVarName.length());
		for (Var globalVar : actor.getStateVars()) {

			System.out.println("GLOBAL: " + globalVar.getInitialValue());
			if (globalVar.getName().equals(varName) && globalVar.isAssignable()) {

			}
		}

		return isConst;
	}

	private boolean isAssignedTo(Var countVar, Procedure proc) {
		boolean isAssignedByBody = false;

		List<Block> blocks = proc.getBlocks();
		blocks.add(proc.getFirst());
		blocks.add(proc.getLast());

		Iterator<Block> iter = blocks.iterator();
		Block block;
		while (!isAssignedByBody && iter.hasNext()) {
			block = iter.next();
			isAssignedByBody |= isAssignedTo(countVar, block);
			// ignore BlockIf and BlockWhile
			// if (block instanceof BlockBasic) {
			// isAssignedByBody |= isAssignedTo(countVar,(BlockBasic) block);
			// }
			// else {
			// System.out.println(block.getClass().toString());
			// }
		}

		return isAssignedByBody;
	}

	private boolean isAssignedTo(Var countVar, Block block) {
		boolean isAssignedByBody = false;
		if (block instanceof BlockIf) {
			// isAssignedByBody = isAssignedTo(countVar, (BlockIf) block);
		} else if (block instanceof BlockWhile) {
			// isAssignedByBody = isAssignedTo(countVar, (BlockWhile) block);
		} else {
			isAssignedByBody = isAssignedTo(countVar, (BlockBasic) block);
		}

		return isAssignedByBody;
	}

	private boolean isAssignedTo(Var countVar, BlockIf block) {
		boolean isAssignedByBody = false;
		for (Block b : block.getThenBlocks()) {
			isAssignedByBody |= isAssignedTo(countVar, b);
		}
		for (Block b : block.getElseBlocks()) {
			isAssignedByBody |= isAssignedTo(countVar, b);
		}
		return isAssignedByBody;
	}

	private boolean isAssignedTo(Var countVar, BlockWhile block) {
		boolean isAssignedByBody = false;
		for (Block b : block.getBlocks()) {
			isAssignedByBody |= isAssignedTo(countVar, b);
		}
		return isAssignedByBody;
	}

	private boolean isAssignedTo(Var countVar, BlockBasic block) {
		boolean isAssignedByBody = false;
		for (Instruction inst : block.getInstructions()) {
			if (inst.isInstAssign()) {
				InstAssign ass = (InstAssign) inst;
				String s = countVar.getName();
				if (s.startsWith("local_")) {
					s = s.substring(6, s.length());
				}
				// System.out.println(s + " , " + ass);
				isAssignedByBody |= (ass.getTarget().getVariable().getName().equals(s));
			} else if (inst.isInstStore()) {
				InstStore store = (InstStore) inst;
				String s = countVar.getName();
				if (s.startsWith("local_")) {
					s = s.substring(6, s.length());
				}
				// System.out.println(s + " , " +
				// store.getTarget().getVariable().getName());
				isAssignedByBody |= (store.getTarget().getVariable().getName().equals(s));
			}
		}
		return isAssignedByBody;
	}

	public void isPure(String pureFilename, List<NetworkTuple> networkTuples) {
		try {

			// IProject project;
			Network thisNetwork;
			// Actor act = null;
			String dataLine;
			List<Actor> actors;
			String titleLine = "actor pure";
			java.io.File f = new java.io.File(pureFilename);
			List<String> actorsRecorded = new ArrayList<String>();
			f.delete();
			Files.write(Paths.get(pureFilename), (titleLine + "\n").getBytes(), StandardOpenOption.CREATE_NEW);

			for (NetworkTuple<IProject, Network> networkTuple : networkTuples) {
				thisNetwork = networkTuple.network;
				actors = thisNetwork.getAllActors();

				for (Actor act : actors) {
					if (!actorsRecorded.contains(act.getName())) {
						String isPure = "false";

						if (act.getActions().size() == 1 && act.getStateVars().isEmpty()) {
							isPure = "true";
						} else if (act.getFsm() != null && act.getStateVars().isEmpty() && (!hasBranching(act.getFsm()))
								&& isAcyclic(act.getFsm())) {
							isPure = "true";
						}

						dataLine = act.getName() + " " + isPure;
						Files.write(Paths.get(pureFilename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);

						actorsRecorded.add(act.getName());
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
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
					if (!actorsRecorded.contains(act.getName())) {
						// System.out.println("processing " + act.getName());

						FSM fsm = act.getFsm();
						String fsmType = "";
						if (fsm != null) {
							boolean isAcyclic = isAcyclic(fsm);
							boolean hasBranching = hasBranching(fsm);

							if (isAcyclic && hasBranching) {
								fsmType = "acyclic-branching";
							} else if (isAcyclic) {
								fsmType = "acyclic";

								System.out.println(act.getName() + " is acyclic, numver of global vars: "
										+ act.getStateVars().size());

							} else {
								fsmType = "cyclic";
							}
						} else {
							fsmType = "runtime-fsm";
						}

						dataLine = act.getName() + " " + fsmType;
						Files.write(Paths.get(fsmFilename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);

						actorsRecorded.add(act.getName());
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasBranching(FSM fsm) {
		return hasBranching(fsm, fsm.getInitialState(), new ArrayList<State>());
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
		} else {
			boolean b = false;
			for (Transition t : fromHere) {
				if (!b) {
					if (!visited.contains(thisState)) {
						visited.add(thisState);
						b = hasBranching(fsm, t.getTarget(), visited);
					}
				}
			}
			hasBranches = b;
		}
		return hasBranches;
	}

	public boolean isAcyclic(FSM fsm) {
		// if (fsm.getInitialState() != null) {
		State initialState = fsm.getInitialState();
		return isAcyclic(fsm, new ArrayList<State>(), initialState, initialState);
		// }
		// else return true;
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
		} else if (fromHere.size() == 1 && fromHere.get(0).getTarget() == initialState) {
			isAcyclic = true;
		} else {
			boolean stillAcyclic = true;
			for (Transition t : fromHere) {
				if (stillAcyclic) {
					// this can set stillAcyclic to false.
					stillAcyclic = !predecessors.contains(t.getTarget());
				}
			}

			for (Transition t : fromHere) {
				if (stillAcyclic) {
					predecessors.add(thisState);
					stillAcyclic = isAcyclic(fsm, predecessors, initialState, t.getTarget());
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

	public void independantOutputPorts(String pipelinableFilename, List<NetworkTuple> networkTuples) {
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
					for (Port p : act.getOutputs()) {
						int producersToP = 0;
						for (Action action : act.getActions()) {
							Pattern patt = action.getOutputPattern();
							for (Var var : patt.getVariables()) {
								if (var.getName().equals(p.getName())) {
									producersToP++;
								}
							}
						}
						dataLine = act.getName() + " " + p.getName() + " " + producersToP;
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
						} else {
							Action action = act.getActions().get(0);
							Procedure proc = action.getBody();
							int basicBlocks = 0;
							for (Block block : proc.getBlocks()) {
								if (block.isBlockBasic()) {
									basicBlocks++;
								}
							}
							switch (basicBlocks) {
							case 1:
								dataLine = act.getName() + " yes";
							default:
								dataLine = act.getName() + " no";
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
			Files.write(Paths.get(pipelinableFilename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);

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

	public void actorHasGlobalVariables(String filename, List<NetworkTuple> networkTuples) {
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
						if (act != null && act.getName() != null) {
							dataLine = act.getName() + " " + act.getStateVars().size();
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
						if (act != null && act.getName() != null) {
							if (act.getActions().size() > 0) { // ignore native actors that have no actions
							dataLine = act.getName() + " " + act.getActions().size();
							Files.write(Paths.get(filename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);
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

	public void actorInputOutputPorts(String filename, List<NetworkTuple> networkTuples) {
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
				while (actors.hasNext()) {
					act = actors.next();
				// for (Actor act : thisNetwork.getAllActors()) {
				if (!actorsRecorded.contains(act.getName())) {
					int inputs = 0;
					int outputs = 0;
					if (act != null && act.getName() != null && act.getInputs() != null) {
						inputs = act.getInputs().size();
					}
					if (act != null && act.getName() != null && act.getOutputs() != null) {
						outputs = act.getOutputs().size();
					}
					dataLine = act.getName() + " " + inputs + " " + outputs;
					Files.write(Paths.get(filename), (dataLine + "\n").getBytes(), StandardOpenOption.APPEND);
					actorsRecorded.add(act.getName());
					j++;
				}
				}
				// }
			}
			System.out.println(j + " actors added");
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
