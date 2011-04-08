/*
 * Copyright (c) 2011, IETR/INSA of Rennes
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
package net.sf.orcc.backends.vhdl.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.orcc.backends.instructions.InstSplit;
import net.sf.orcc.ir.Action;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.FSM;
import net.sf.orcc.ir.FSM.State;
import net.sf.orcc.ir.InstSpecific;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.NodeBlock;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.Tag;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.impl.IrFactoryImpl;
import net.sf.orcc.ir.util.AbstractActorVisitor;
import net.sf.orcc.util.UniqueEdge;

import org.jgrapht.DirectedGraph;

/**
 * This class defines a transformation that splits actions each time a
 * SplitInstruction is encountered.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ActionSplitter extends AbstractActorVisitor {

	/**
	 * This class contains an abstract branch visitor.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	public class AbstractBranchVisitor extends AbstractActorVisitor {

		/**
		 * name of the branch being visited
		 */
		private String branchName;

		/**
		 * action being visited
		 */
		private Action currentAction;

		/**
		 * action to visit next (may be null)
		 */
		private Action nextAction;

		/**
		 * name of the source state
		 */
		private String sourceName;

		/**
		 * name of the target state
		 */
		private String targetName;

		public AbstractBranchVisitor(String sourceName, String targetName) {
			this.sourceName = sourceName;
			this.targetName = targetName;
		}

		/**
		 * Creates a new empty action with the given name.
		 * 
		 * @param name
		 *            action name
		 * @return a new empty action with the given name
		 */
		private Action createNewAction(Expression condition, String name) {
			// scheduler
			Procedure scheduler = IrFactory.eINSTANCE.createProcedure(
					"isSchedulable_" + name,
					IrFactory.eINSTANCE.createLocation(),
					IrFactory.eINSTANCE.createTypeBool());
			Var result = scheduler.newTempLocalVariable(
					IrFactory.eINSTANCE.createTypeBool(), "result");
			result.setIndex(1);

			NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();
			block.add(IrFactory.eINSTANCE.createInstAssign(result, condition));
			block.add(IrFactory.eINSTANCE.createInstReturn(IrFactory.eINSTANCE
					.createExprVar(result)));
			scheduler.getNodes().add(block);

			// body
			Procedure body = IrFactory.eINSTANCE.createProcedure(name,
					IrFactory.eINSTANCE.createLocation(),
					IrFactory.eINSTANCE.createTypeVoid());
			block = IrFactoryImpl.eINSTANCE.createNodeBlock();
			block.add(IrFactory.eINSTANCE.createInstReturn());
			body.getNodes().add(block);

			// tag
			Tag tag = IrFactory.eINSTANCE.createTag(name);

			Action action = IrFactory.eINSTANCE.createAction(
					IrFactory.eINSTANCE.createLocation(), tag,
					IrFactory.eINSTANCE.createPattern(),
					currentAction.getOutputPattern(), scheduler, body);
			currentAction.setOutputPattern(IrFactory.eINSTANCE.createPattern());

			// add action to actor's actions
			ActionSplitter.this.actor.getActions().add(action);

			return action;
		}

		/**
		 * Returns a new unique state name.
		 * 
		 * @return a new unique state name
		 */
		private String getNewStateName() {
			String stateName = branchName;
			Integer count = stateNames.get(stateName);
			if (count == null) {
				count = 1;
			}
			stateNames.put(stateName, count + 1);

			return stateName + "_" + count;
		}

		/**
		 * Replaces the transition <code>source</code> -&gt; <code>target</code>
		 * by a transition <code>source</code> -&gt; <code>newState</code> -&gt;
		 * <code>target</code>.
		 * 
		 * @param newAction
		 *            the newly-created action
		 */
		private void replaceTransition(Action newAction) {
			// add an FSM if the actor does not have one
			if (fsm == null) {
				addFsm();
			}

			// add state and transitions
			String newStateName = newAction.getName();
			fsm.addState(newStateName);

			fsm.replaceTarget(sourceName, currentAction, newStateName);
			fsm.addTransition(newStateName, newAction, targetName);
		}

		/**
		 * Split the current action
		 */
		private void splitAction() {
			String newActionName = getNewStateName();

			// create new action
			nextAction = createNewAction(
					IrFactory.eINSTANCE.createExprBool(true), newActionName);

			// remove the SplitInstruction
			itInstruction.previous();
			itInstruction.remove();

			// move code
			CodeMover codeMover = new CodeMover();
			codeMover.setTargetProcedure(nextAction.getBody());
			codeMover.moveInstructions(itInstruction);
			codeMover.moveNodes(itNode);

			// update transitions
			replaceTransition(nextAction);

			// set new source state to the new state name
			sourceName = newActionName;
		}

		@Override
		public void visit(Action action) {
			this.branchName = targetName + "_" + action.getName();
			nextAction = action;
			visitInBranch();
		}

		@Override
		public void visit(InstSpecific instruction) {
			if (instruction instanceof InstSplit) {
				splitAction();
			}
		}

		/**
		 * Visits the next action(s) without updating the branch name.
		 */
		private void visitInBranch() {
			while (nextAction != null) {
				currentAction = nextAction;
				nextAction = null;

				visit(currentAction.getBody());
			}
		}

	}

	/**
	 * FSM of the actor. May be null if the actor has no FSM. May be updated if
	 * an FSM is added to the actor.
	 */
	private FSM fsm;

	/**
	 * Map used to create new unique state names.
	 */
	private Map<String, Integer> stateNames;

	/**
	 * Adds an FSM to the given action scheduler.
	 * 
	 * @param actionScheduler
	 *            action scheduler
	 */
	private void addFsm() {
		fsm = IrFactory.eINSTANCE.createFSM();
		fsm.setInitialState("init");
		fsm.addState("init");
		for (Action action : actor.getActionsOutsideFsm()) {
			fsm.addTransition("init", action, "init");
		}

		actor.getActionsOutsideFsm().clear();
		actor.setFsm(fsm);
	}

	@Override
	public void visit(Actor actor) {
		this.actor = actor;
		stateNames = new HashMap<String, Integer>();
		visitAllActions();

		DataMover mover = new DataMover(actor);
		for (Action action : actor.getActions()) {
			mover.visit(action);
		}
	}

	/**
	 * Visits the given transition characterized by its source name, target name
	 * and action.
	 * 
	 * @param sourceName
	 *            name of source state
	 * @param targetName
	 *            name of target state
	 * @param action
	 *            action associated with transition
	 */
	private void visit(String sourceName, String targetName, Action action) {
		new AbstractBranchVisitor(sourceName, targetName).visit(action);
	}

	/**
	 * Visits all actions of this actor.
	 */
	private void visitAllActions() {
		fsm = actor.getFsm();
		if (fsm == null) {
			// no FSM: simply visit all the actions
			List<Action> actions = new ArrayList<Action>(
					actor.getActionsOutsideFsm());
			for (Action action : actions) {
				// an FSM will be created if needed, from "init" to "init" (and
				// intermediate transitions created by the BranchVisitor)

				String sourceName = "init";
				String targetName = "init";
				visit(sourceName, targetName, action);
			}
		} else {
			// with an FSM: visits all transitions
			DirectedGraph<State, UniqueEdge> graph = fsm.getGraph();
			Set<UniqueEdge> edges = graph.edgeSet();
			for (UniqueEdge edge : edges) {
				State source = graph.getEdgeSource(edge);
				String sourceName = source.getName();

				State target = graph.getEdgeTarget(edge);
				String targetName = target.getName();

				Action action = (Action) edge.getObject();
				visit(sourceName, targetName, action);
			}
		}
	}

}
