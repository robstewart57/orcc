/*
 * Copyright (c) 2010, IETR/INSA of Rennes
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

package net.sf.orcc.backends.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.orcc.ir.Action;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.ExprVar;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.FSM;
import net.sf.orcc.ir.InstAssign;
import net.sf.orcc.ir.InstLoad;
import net.sf.orcc.ir.InstReturn;
import net.sf.orcc.ir.InstStore;
import net.sf.orcc.ir.Instruction;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.Node;
import net.sf.orcc.ir.NodeBlock;
import net.sf.orcc.ir.OpBinary;
import net.sf.orcc.ir.Pattern;
import net.sf.orcc.ir.Port;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.State;
import net.sf.orcc.ir.Tag;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.Use;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.impl.IrFactoryImpl;
import net.sf.orcc.ir.util.AbstractActorVisitor;
import net.sf.orcc.ir.util.EcoreHelper;
import net.sf.orcc.util.UniqueEdge;

import org.eclipse.emf.common.util.EList;
import org.jgrapht.DirectedGraph;

/**
 * This class defines a visitor that transforms multi-token to mono-token data
 * transfer
 * 
 * @author Khaled Jerbi
 * 
 */
public class Multi2MonoToken extends AbstractActorVisitor<Object> {
	/**
	 * This class defines a visitor that substitutes the peek from the port to
	 * the new buffer and changes the index from (index) to
	 * (index+writeIndex&maskValue)
	 * 
	 * @author Khaled Jerbi
	 * 
	 */
	private class ModifyActionScheduler extends AbstractActorVisitor<Object> {
		private Var buffer;
		private int bufferSize;
		private Port currentPort;
		private Var writeIndex;

		public ModifyActionScheduler(Var buffer, Var writeIndex,
				Port currentPort, int bufferSize) {
			this.buffer = buffer;
			this.writeIndex = writeIndex;
			this.currentPort = currentPort;
			this.bufferSize = bufferSize;
		}

		@Override
		public Object caseInstLoad(InstLoad load) {
			Var varSource = load.getSource().getVariable();
			Pattern pattern = EcoreHelper.getContainerOfType(varSource,
					Pattern.class);
			if (pattern != null) {
				Port testPort = pattern.getPort(varSource);
				if (currentPort.equals(testPort)) {
					// change tab Name
					load.getSource().setVariable(buffer);
					// change index --> writeIndex+index
					Expression maskValue = IrFactory.eINSTANCE
							.createExprInt(bufferSize - 1);
					Expression index = IrFactory.eINSTANCE
							.createExprVar(writeIndex);
					if (!load.getIndexes().isEmpty()) {
						Expression expression1 = load.getIndexes().get(0);
						Expression sum = IrFactory.eINSTANCE.createExprBinary(
								expression1, OpBinary.PLUS, index,
								IrFactory.eINSTANCE.createTypeInt(32));
						Expression mask = IrFactory.eINSTANCE.createExprBinary(
								sum, OpBinary.BITAND, maskValue,
								IrFactory.eINSTANCE.createTypeInt(32));

						load.getIndexes().add(mask);
					} else {
						Expression mask2 = IrFactory.eINSTANCE
								.createExprBinary(index, OpBinary.BITAND,
										maskValue,
										IrFactory.eINSTANCE.createTypeInt(32));
						load.getIndexes().add(mask2);
					}
				}
			}
			return null;
		}
	}

	/**
	 * This class defines a visitor that substitutes process variable names with
	 * those of the newly defined actions for InstStore
	 * 
	 * @author Khaled Jerbi
	 * 
	 */
	private class ModifyProcessActionStore extends AbstractActorVisitor<Object> {
		private Var tab;

		public ModifyProcessActionStore(Var tab) {
			this.tab = tab;
		}

		@Override
		public Object caseInstLoad(InstLoad load) {
			Var varSource = load.getSource().getVariable();
			Pattern pattern = EcoreHelper.getContainerOfType(varSource,
					Pattern.class);
			if (pattern != null) {
				Port testPort = pattern.getPort(varSource);
				if (port.equals(testPort)) {
					// change tab Name
					load.getSource().setVariable(tab);
				}
			}
			return null;
		}
	}

	/**
	 * This class defines a visitor that substitutes process variable names with
	 * those of the newly defined actions for write
	 * 
	 * @author Khaled JERBI
	 * 
	 */
	private class ModifyProcessActionWrite extends AbstractActorVisitor<Object> {

		private Var tab;

		public ModifyProcessActionWrite(Var tab) {
			this.tab = tab;
		}

		@Override
		
		public Object caseInstStore(InstStore store) {
			Var varTarget = store.getTarget().getVariable();
			Pattern pattern = EcoreHelper.getContainerOfType(varTarget,
					Pattern.class);
			if (pattern != null) {
				Port testPort = pattern.getPort(varTarget);
				if (port.equals(testPort)) {
					// change tab Name
					store.getTarget().setVariable(tab);
				}
			}
			return null;
		}
		
	}

	private List<Action> AddedUntaggedActions;
	private int bufferSize;
	private Action done;
	private Type entryType;
	private FSM fsm;
	private List<Var> inputBuffers;
	private int inputIndex;
	private List<Port> inputPorts;
	private List<Action> noRepeatActions;
	private int numTokens;
	private int outputIndex;
	private Port port;
	private Action process;
	private List<Var> readIndexes;
	private boolean repeatInput;
	private boolean repeatOutput;
	private Var result;
	private Action store;
	private Map<String, State> statesMap;
	private Action write;
	private List<Var> writeIndexes;

	/**
	 * transforms the transformed action to a transition action
	 * 
	 * @param action
	 *            modified action
	 * @param buffer
	 *            current store buffer
	 * @param writeIndex
	 *            write index of the buffer
	 * @param readIndex
	 *            read index of the buffer
	 */
	private void actionToTransition(Port port, Action action, Var buffer, Var writeIndex,
			Var readIndex) {
		ModifyActionScheduler modifyActionScheduler = new ModifyActionScheduler(
				buffer, writeIndex, port, bufferSize);
		modifyActionScheduler.doSwitch(action.getScheduler());
		modifyActionSchedulability(action, writeIndex, readIndex, OpBinary.GE,
				IrFactory.eINSTANCE.createExprInt(numTokens), port);
	}

	/**
	 * Adds an FSM to an actor if it has not already
	 * 
	 */
	private void addFsm() {
		fsm = IrFactory.eINSTANCE.createFSM();
		State initState = statesMap.get("init");
		fsm.getStates().add(initState);
		fsm.setInitialState(initState);
		for (Action action : actor.getActionsOutsideFsm()) {
			fsm.addTransition(initState, action, initState);
		}

		actor.getActionsOutsideFsm().clear();
		actor.setFsm(fsm);
	}

	@Override
	public Object caseActor(Actor actor) {
		this.actor = actor;
		inputIndex = 0;
		outputIndex = 0;
		repeatInput = false;
		repeatOutput = false;
		bufferSize = 0;
		AddedUntaggedActions = new ArrayList<Action>();
		inputBuffers = new ArrayList<Var>();
		inputPorts = new ArrayList<Port>();
		noRepeatActions = new ArrayList<Action>();
		readIndexes = new ArrayList<Var>();
		statesMap = new HashMap<String, State>();
		writeIndexes = new ArrayList<Var>();
		modifyRepeatActionsInFSM();
		modifyUntaggedActions(actor);
		return null;
	}

	/**
	 * This method adds instructions for an action to read from a specific
	 * buffer at a specific index
	 * 
	 * @param body
	 *            body of the action
	 * @param position
	 *            position of the buffer in inputBuffers list
	 */
	private void consumeToken(Procedure body, int position, Port port) {
		NodeBlock bodyNode = body.getFirst();
		EList<Var> locals = body.getLocals();
		Var index = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32),
				"index" + port.getName(), true, 1);
		locals.add(index);
		Var writeIndex = writeIndexes.get(position);
		Instruction loadInd = IrFactory.eINSTANCE.createInstLoad(index,
				writeIndex);
		bodyNode.add(loadInd);

		Var indexInc = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32),
				"index" + port.getName(), true, 2);
		locals.add(indexInc);
		Expression value = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(index), OpBinary.PLUS,
				IrFactory.eINSTANCE.createExprInt(1),
				IrFactory.eINSTANCE.createTypeInt(32));
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(indexInc,
				value);
		bodyNode.add(assign);

		Instruction store = IrFactory.eINSTANCE.createInstStore(writeIndex,
				IrFactory.eINSTANCE.createExprVar(indexInc));
		bodyNode.add(store);
	}

	/**
	 * This method copies the output patterns from a source action to a target
	 * one
	 * 
	 * @param source
	 *            source action
	 * @param target
	 *            target action
	 */
	private void copyOutputPattern(Action source, Action target) {
		Pattern targetPattern = target.getOutputPattern();
		Pattern sourcePattern = source.getOutputPattern();
		targetPattern.getNumTokensMap().putAll(sourcePattern.getNumTokensMap());
		targetPattern.getPorts().addAll(sourcePattern.getPorts());
		targetPattern.getPortToVarMap().putAll(sourcePattern.getPortToVarMap());
		targetPattern.getVarToPortMap().putAll(sourcePattern.getVarToPortMap());
	}

	/**
	 * This method creates an action with the given name.
	 * 
	 * @param name
	 *            name of the action
	 * @return a new action created with the given name
	 */
	private Action createAction(Expression condition, String name) {
		// scheduler
		Procedure scheduler = IrFactory.eINSTANCE.createProcedure(
				"isSchedulable_" + name, 0,
				IrFactory.eINSTANCE.createTypeBool());
		Var result = scheduler.newTempLocalVariable(
				IrFactory.eINSTANCE.createTypeBool(), "actionResult");
		result.setIndex(1);

		NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();
		block.add(IrFactory.eINSTANCE.createInstAssign(result, condition));
		block.add(IrFactory.eINSTANCE.createInstReturn(IrFactory.eINSTANCE
				.createExprVar(result)));
		scheduler.getNodes().add(block);

		// body
		Procedure body = IrFactory.eINSTANCE.createProcedure(name, 0,
				IrFactory.eINSTANCE.createTypeVoid());
		block = IrFactoryImpl.eINSTANCE.createNodeBlock();
		block.add(IrFactory.eINSTANCE.createInstReturn());
		body.getNodes().add(block);

		// tag
		Tag tag = IrFactory.eINSTANCE.createTag(name);

		Action action = IrFactory.eINSTANCE.createAction(tag,
				IrFactory.eINSTANCE.createPattern(),
				IrFactory.eINSTANCE.createPattern(),
				IrFactory.eINSTANCE.createPattern(), scheduler, body);

		// add action to actor's actions
		this.actor.getActions().add(action);

		return action;
	}

	/**
	 * This method creates the required InstStore, done, untagged and process
	 * actions
	 * 
	 * @param action
	 *            the action getting transformed
	 */
	private void createActionsSet(Action action, State sourceState,
			State targetState) {
		scanInputs(action, sourceState, targetState);
		scanOutputs(action, sourceState, targetState);
	}

	/**
	 * This method creates a global variable counter for store with the given
	 * name.
	 * 
	 * @param name
	 *            name of the counter
	 * @return new counter with the given name
	 */
	private Var createCounter(String name) {
		Var newCounter = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), name, true,
				IrFactory.eINSTANCE.createExprInt(0));

		Expression expression = IrFactory.eINSTANCE.createExprInt(0);
		newCounter.setInitialValue(expression);
		if (!actor.getStateVars().contains(newCounter.getName())) {
			actor.getStateVars().add(newCounter);
		}
		return newCounter;
	}

	/**
	 * This method creates the done action that is schedulable when required
	 * number of tokens is read (written)
	 * 
	 * @param actionName
	 *            name of the action
	 * @param counter
	 *            global variable counter used for reading (writing) tokens
	 * @param numTokens
	 *            repeat value
	 * @return new done action
	 */
	private Action createDoneAction(String name, Var counter, int numTokens) {
		// body
		Procedure body = IrFactory.eINSTANCE.createProcedure(name, 0,
				IrFactory.eINSTANCE.createTypeVoid());
		NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();
		InstStore store = IrFactory.eINSTANCE.createInstStore(counter,
				IrFactory.eINSTANCE.createExprInt(0));
		block.add(store);
		block.add(IrFactory.eINSTANCE.createInstReturn());
		body.getNodes().add(block);

		// scheduler
		Procedure scheduler = IrFactory.eINSTANCE.createProcedure(
				"isSchedulable_" + name, 0,
				IrFactory.eINSTANCE.createTypeBool());
		Var temp = scheduler.newTempLocalVariable(
				IrFactory.eINSTANCE.createTypeBool(), "temp");
		temp.setIndex(1);

		scheduler.getLocals().add(temp);
		result = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeBool(), "res", true, 0);

		scheduler.getLocals().add(result);
		Var localCounter = IrFactory.eINSTANCE.createVar(0, counter.getType(),
				"localCounter", true, 1);
		scheduler.getLocals().add(localCounter);
		block = IrFactoryImpl.eINSTANCE.createNodeBlock();
		InstLoad schedulerLoad = IrFactory.eINSTANCE.createInstLoad(
				localCounter, counter);
		block.add(0, schedulerLoad);

		Expression guardValue = IrFactory.eINSTANCE.createExprInt(numTokens);
		Expression counterExpression = IrFactory.eINSTANCE
				.createExprVar(localCounter);
		Expression expression = IrFactory.eINSTANCE.createExprBinary(
				counterExpression, OpBinary.EQ, guardValue,
				IrFactory.eINSTANCE.createTypeBool());
		block.add(IrFactory.eINSTANCE.createInstAssign(temp, expression));
		block.add(IrFactory.eINSTANCE.createInstAssign(result,
				IrFactory.eINSTANCE.createExprVar(temp)));
		block.add(IrFactory.eINSTANCE.createInstReturn(IrFactory.eINSTANCE
				.createExprVar(result)));
		scheduler.getNodes().add(block);

		// tag
		Tag tag = IrFactory.eINSTANCE.createTag(name);

		Action action = IrFactory.eINSTANCE.createAction(tag,
				IrFactory.eINSTANCE.createPattern(),
				IrFactory.eINSTANCE.createPattern(),
				IrFactory.eINSTANCE.createPattern(), scheduler, body);

		// add action to actor's actions
		this.actor.getActions().add(action);

		return action;
	}

	/**
	 * This method creates the process action using the nodes & locals of the
	 * action getting transformed
	 * 
	 * @param action
	 *            currently transforming action
	 * @return new process action
	 */
	private Action createProcessAction(Action action) {
		Expression expression = IrFactory.eINSTANCE.createExprBool(true);
		Action newProcessAction = createAction(expression, "newProcess_"
				+ action.getName());
		Procedure body = newProcessAction.getBody();

		ListIterator<Node> listIt = action.getBody().getNodes().listIterator();
		moveNodes(listIt, body);
		Iterator<Var> it = action.getBody().getLocals().iterator();
		moveLocals(it, body);
		
		return newProcessAction;
	}

	/**
	 * This method defines a new store action that reads 1 token on the repeat
	 * port
	 * 
	 * @param actionName
	 *            name of the new store action
	 * @param numTokens
	 *            repeat number
	 * @param port
	 *            repeat port
	 * @param readCounter
	 *            global variable counter
	 * @param storeList
	 *            global variable list of store (write)
	 * @return new store action
	 */
	private Action createStoreAction(String actionName, Var readCounter,
			Var storeList, Var buffer, Var writeIndex) {
		String storeName = actionName + port.getName() + "_NewStore";
		Expression guardValue = IrFactory.eINSTANCE.createExprInt(numTokens);
		Expression counterExpression = IrFactory.eINSTANCE
				.createExprVar(readCounter);
		Expression expression = IrFactory.eINSTANCE.createExprBinary(
				counterExpression, OpBinary.LT, guardValue,
				IrFactory.eINSTANCE.createTypeBool());

		Action newStoreAction = createAction(expression, storeName);
		defineStoreBody(readCounter, storeList, newStoreAction.getBody(),
				buffer, writeIndex);
		return newStoreAction;
	}

	/**
	 * This method creates a global variable counter for data storing (writing)
	 * 
	 * @param name
	 *            name of the list
	 * @param numTokens
	 *            size of the list
	 * @param entryType
	 *            type of the list
	 * @return a global variable list
	 */
	private Var createTab(String name, Type entryType, int size) {
		Type type = IrFactory.eINSTANCE.createTypeList(size, entryType);
		Var newList = IrFactory.eINSTANCE.createVar(0, type, name, true, true);
		if (!actor.getStateVars().contains(newList.getName())) {
			actor.getStateVars().add(newList);
		}

		return newList;
	}

	/**
	 * creates an untagged action to store tokens
	 * 
	 * @param storeCounter
	 *            global variable counter
	 * @param storeList
	 *            global variable list to store
	 * @param priority
	 *            whether to put the untagged action as high priority or not
	 * @return new untagged action
	 */

	private Action createUntaggedAction(Var readIndex, Var writeIndex,
			Var storeList, Port port, boolean priority) {
		Expression expression = IrFactory.eINSTANCE.createExprBool(true);
		Action newUntaggedAction = createAction(expression,
				"untagged_" + port.getName());
		Var localINPUT = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeList(1, port.getType()),
				port.getName(), true, 0);

		defineUntaggedBody(readIndex, storeList, newUntaggedAction.getBody(),
				localINPUT, port);
		modifyActionSchedulability(newUntaggedAction, writeIndex, readIndex,
				OpBinary.LT, IrFactory.eINSTANCE.createExprInt(bufferSize),
				port);
		Pattern pattern = newUntaggedAction.getInputPattern();
		pattern.setNumTokens(port, 1);
		pattern.setVariable(port, localINPUT);
		if (priority) {
			actor.getActionsOutsideFsm().add(0, newUntaggedAction);
		} else {
			actor.getActionsOutsideFsm().add(newUntaggedAction);
		}
		AddedUntaggedActions.add(newUntaggedAction);
		return newUntaggedAction;
	}

	/**
	 * This method creates the new write action
	 * 
	 * @param actionName
	 *            action name
	 * @param writeCounter
	 *            global variable write counter
	 * @param writeList
	 *            global variable write list
	 * @return new write action
	 */
	private Action createWriteAction(String actionName, Var writeCounter,
			Var writeList) {
		String writeName = actionName + port.getName() + "_NewWrite";
		Expression guardValue = IrFactory.eINSTANCE.createExprInt(numTokens);
		Expression counterExpression = IrFactory.eINSTANCE
				.createExprVar(writeCounter);
		Expression expression = IrFactory.eINSTANCE.createExprBinary(
				counterExpression, OpBinary.LT, guardValue,
				IrFactory.eINSTANCE.createTypeBool());
		Action newWriteAction = createAction(expression, writeName);

		Var OUTPUT = IrFactory.eINSTANCE.createVar(0, port.getType(),
				port.getName() + "_OUTPUT", true, 0);
		defineWriteBody(writeCounter, writeList, newWriteAction.getBody(),
				OUTPUT);
		// add output pattern
		Pattern pattern = newWriteAction.getOutputPattern();
		pattern.setNumTokens(port, 1);
		pattern.setVariable(port, OUTPUT);
		return newWriteAction;
	}

	/**
	 * This method creates the instructions for the body of the new store action
	 * 
	 * @param port
	 *            repeat port
	 * @param readCounter
	 *            global variable counter
	 * @param storeList
	 *            global store (write) list
	 * @param body
	 *            new store action body
	 */
	private void defineStoreBody(Var readCounter, Var storeList,
			Procedure body, Var buffer, Var writeIndex) {

		NodeBlock bodyNode = body.getFirst();

		EList<Var> locals = body.getLocals();
		Var counter = IrFactory.eINSTANCE.createVar(0, readCounter.getType(),
				port.getName() + "_Local_counter", true, 1);
		locals.add(counter);
		Instruction load1 = IrFactory.eINSTANCE.createInstLoad(counter,
				readCounter);
		bodyNode.add(load1);
		Var index = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "writeIndex", true, 1);
		locals.add(index);
		Instruction loadIndex = IrFactory.eINSTANCE.createInstLoad(index,
				writeIndex);
		bodyNode.add(loadIndex);

		Var mask = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "mask", true, 1);
		locals.add(mask);
		Expression exprMask = IrFactory.eINSTANCE.createExprInt(bufferSize - 1);
		Expression maskValue = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(index), OpBinary.BITAND,
				exprMask, IrFactory.eINSTANCE.createTypeInt(32));
		InstAssign assignMask = IrFactory.eINSTANCE.createInstAssign(mask,
				maskValue);
		bodyNode.add(assignMask);

		Var input = IrFactory.eINSTANCE.createVar(0, port.getType(),
				port.getName() + "_Input", true, 1);
		locals.add(input);
		List<Expression> load2Index = new ArrayList<Expression>(1);
		Expression expression1 = IrFactory.eINSTANCE.createExprVar(mask);

		load2Index.add(expression1);
		Instruction load2 = IrFactory.eINSTANCE.createInstLoad(input, buffer,
				load2Index);
		bodyNode.add(load2);
		List<Expression> store1Index = new ArrayList<Expression>(1);
		store1Index.add(IrFactory.eINSTANCE.createExprVar(counter));
		Instruction store1 = IrFactory.eINSTANCE.createInstStore(0, storeList,
				store1Index, IrFactory.eINSTANCE.createExprVar(input));
		bodyNode.add(store1);
		// globalCounter= globalCounter + 1
		Var counter2 = IrFactory.eINSTANCE.createVar(0, readCounter.getType(),
				port.getName() + "_Local_counter", true, 2);
		locals.add(counter2);
		Expression storeIndexElement = IrFactory.eINSTANCE
				.createExprVar(counter);
		Expression inc1 = IrFactory.eINSTANCE.createExprInt(1);
		Expression assignValue = IrFactory.eINSTANCE.createExprBinary(
				storeIndexElement, OpBinary.PLUS, inc1,
				IrFactory.eINSTANCE.createTypeInt(32));
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(counter2,
				assignValue);
		bodyNode.add(assign);
		Instruction store2 = IrFactory.eINSTANCE.createInstStore(readCounter,
				IrFactory.eINSTANCE.createExprVar(counter2));
		bodyNode.add(store2);
		Var tmp = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "tmp", true, 1);
		locals.add(tmp);

		Expression inc2 = IrFactory.eINSTANCE.createExprInt(1);
		Expression incValue = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(index), OpBinary.PLUS, inc2,
				IrFactory.eINSTANCE.createTypeInt(32));

		Instruction assign2 = IrFactory.eINSTANCE.createInstAssign(tmp,
				incValue);
		bodyNode.add(assign2);
		Instruction store3 = IrFactory.eINSTANCE.createInstStore(writeIndex,
				IrFactory.eINSTANCE.createExprVar(tmp));
		bodyNode.add(store3);
	}

	/**
	 * This method creates the instructions for the body of the new untagged
	 * action
	 * 
	 * @param port
	 *            repeat port
	 * @param readCounter
	 *            global variable counter
	 * @param storeList
	 *            global store list
	 * @param body
	 *            new untagged action body
	 */
	private void defineUntaggedBody(Var readCounter, Var storeList,
			Procedure body, Var localINPUT, Port port) {
		NodeBlock bodyNode = body.getFirst();

		EList<Var> locals = body.getLocals();
		Var counter = IrFactory.eINSTANCE.createVar(0, readCounter.getType(),
				port.getName() + "_Local_counter", true, 1);
		locals.add(counter);
		Instruction load1 = IrFactory.eINSTANCE.createInstLoad(counter,
				readCounter);
		bodyNode.add(load1);

		Var mask = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "mask", true, 1);
		locals.add(mask);
		Expression exprmask = IrFactory.eINSTANCE.createExprInt(bufferSize - 1);
		Expression maskValue = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(counter), OpBinary.BITAND,
				exprmask, IrFactory.eINSTANCE.createTypeInt(32));
		InstAssign assignMask = IrFactory.eINSTANCE.createInstAssign(mask,
				maskValue);
		bodyNode.add(assignMask);

		Var input = IrFactory.eINSTANCE.createVar(0, port.getType(),
				port.getName() + "_Input", true, 1);
		locals.add(input);
		List<Expression> load2Index = new ArrayList<Expression>(1);
		load2Index.add(IrFactory.eINSTANCE.createExprInt(0));
		Instruction load2 = IrFactory.eINSTANCE.createInstLoad(input,
				localINPUT, load2Index);
		bodyNode.add(load2);

		List<Expression> store1Index = new ArrayList<Expression>(1);
		Expression e1 = IrFactory.eINSTANCE.createExprVar(counter);

		Expression e2 = IrFactory.eINSTANCE.createExprInt(1);
		Expression indexInc = IrFactory.eINSTANCE.createExprBinary(e1,
				OpBinary.PLUS, e2, readCounter.getType());
		store1Index.add(IrFactory.eINSTANCE.createExprVar(mask));

		Instruction store1 = IrFactory.eINSTANCE.createInstStore(0, storeList,
				store1Index, IrFactory.eINSTANCE.createExprVar(input));
		bodyNode.add(store1);

		Var counter2 = IrFactory.eINSTANCE.createVar(0, readCounter.getType(),
				port.getName() + "_Local_counter", true, 2);
		locals.add(counter2);
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(counter2,
				indexInc);
		bodyNode.add(assign);

		Instruction store2 = IrFactory.eINSTANCE.createInstStore(readCounter,
				IrFactory.eINSTANCE.createExprVar(counter2));
		bodyNode.add(store2);
	}

	/**
	 * This method defines the instructions of the new write action body
	 * 
	 * @param writeCounter
	 *            global variable counter
	 * @param writeList
	 *            global variable list for write
	 * @param body
	 *            body of the new write action
	 */

	private void defineWriteBody(Var writeCounter, Var writeList,
			Procedure body, Var OUTPUT) {
		NodeBlock bodyNode = body.getFirst();
		EList<Var> locals = body.getLocals();
		Var counter1 = IrFactory.eINSTANCE.createVar(0, writeCounter.getType(),
				port.getName() + "_Local_writeCounter", true, outputIndex);
		locals.add(counter1);
		Instruction load1 = IrFactory.eINSTANCE.createInstLoad(counter1,
				writeCounter);
		bodyNode.add(load1);

		Var output = IrFactory.eINSTANCE.createVar(0, port.getType(),
				port.getName() + "_LocalOutput", true, outputIndex);
		locals.add(output);
		List<Expression> load2Index = new ArrayList<Expression>(1);
		load2Index.add(IrFactory.eINSTANCE.createExprVar(writeCounter));
		Instruction load2 = IrFactory.eINSTANCE.createInstLoad(output,
				writeList, load2Index);
		bodyNode.add(load2);

		Var out = IrFactory.eINSTANCE.createVar(0, port.getType(),
				"_LocalTemp", true, outputIndex);
		locals.add(out);
		Use assign1Expr = IrFactory.eINSTANCE.createUse(output);
		Expression assign1Value = IrFactory.eINSTANCE
				.createExprVar(assign1Expr);
		Instruction assign1 = IrFactory.eINSTANCE.createInstAssign(out,
				assign1Value);
		bodyNode.add(assign1);

		Var counter2 = IrFactory.eINSTANCE.createVar(0, writeCounter.getType(),
				port.getName() + "_Local_writeCounter_2", true, outputIndex);
		locals.add(counter2);
		Expression assign2IndexElement = IrFactory.eINSTANCE
				.createExprVar(counter1);
		Expression e2Assign2 = IrFactory.eINSTANCE.createExprInt(1);
		Expression assign2Value = IrFactory.eINSTANCE.createExprBinary(
				assign2IndexElement, OpBinary.PLUS, e2Assign2,
				IrFactory.eINSTANCE.createTypeInt(32));
		Instruction assign2 = IrFactory.eINSTANCE.createInstAssign(counter2,
				assign2Value);
		bodyNode.add(assign2);

		// locals.put(OUTPUT.getName(), OUTPUT);
		ExprVar store1Expression = IrFactory.eINSTANCE.createExprVar(out);
		List<Expression> store1Index = new ArrayList<Expression>(1);
		store1Index.add(IrFactory.eINSTANCE.createExprInt(0));
		Instruction store1 = IrFactory.eINSTANCE.createInstStore(0, OUTPUT,
				store1Index, store1Expression);
		bodyNode.add(store1);

		Expression store2Expression = IrFactory.eINSTANCE
				.createExprVar(counter2);
		Instruction store2 = IrFactory.eINSTANCE.createInstStore(writeCounter,
				store2Expression);
		bodyNode.add(store2);
	}

	/**
	 * this method changes the schedulability of the action accordingly to
	 * tokens disponibility in the buffer
	 * 
	 * @param writeIndex
	 *            write index of the buffer
	 * @param readIndex
	 *            read index of the buffer
	 */
	private void modifyActionSchedulability(Action action, Var writeIndex,
			Var readIndex, OpBinary op, Expression reference, Port port) {
		Procedure scheduler = action.getScheduler();
		NodeBlock bodyNode = scheduler.getLast();
		EList<Var> locals = scheduler.getLocals();

		Var localRead = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32),
				"readIndex_" + port.getName() + "_" + inputIndex, true,
				inputIndex);

		locals.add(localRead);
		Instruction InstLoad = IrFactory.eINSTANCE.createInstLoad(localRead,
				readIndex);
		int index = 0;
		bodyNode.add(index, InstLoad);
		index++;

		Var localWrite = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32),
				"writeIndex_" + port.getName() + "_" + inputIndex, true,
				inputIndex);
		locals.add(localWrite);
		Instruction Load2 = IrFactory.eINSTANCE.createInstLoad(localWrite,
				writeIndex);
		bodyNode.add(index, Load2);
		index++;

		Var diff = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "diff" + port.getName()
						+ "_" + inputIndex, true, inputIndex);
		locals.add(diff);
		Expression value = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(localRead), OpBinary.MINUS,
				IrFactory.eINSTANCE.createExprVar(localWrite),
				IrFactory.eINSTANCE.createTypeInt(32));
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(diff, value);
		bodyNode.add(index, assign);
		index++;

		Var conditionVar = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeBool(),
				"condition_" + port.getName(), true, inputIndex);
		locals.add(conditionVar);
		Expression value2 = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(diff), op, reference,
				IrFactory.eINSTANCE.createTypeBool());
		Instruction assign2 = IrFactory.eINSTANCE.createInstAssign(
				conditionVar, value2);
		bodyNode.add(index, assign2);
		index++;

		Var myResult = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeBool(),
				"myResult_" + port.getName(), true, inputIndex);
		locals.add(myResult);
		int returnIndex = bodyNode.getInstructions().size() - 1;
		InstReturn actionReturn = (InstReturn) bodyNode.getInstructions().get(
				returnIndex);
		Expression returnExpr = actionReturn.getValue();
		// VarLocal currentResult
		Expression e = IrFactory.eINSTANCE.createExprBinary(returnExpr,
				OpBinary.LOGIC_AND,
				IrFactory.eINSTANCE.createExprVar(conditionVar),
				IrFactory.eINSTANCE.createTypeBool());
		Instruction assign3 = IrFactory.eINSTANCE.createInstAssign(myResult, e);

		bodyNode.add(returnIndex, assign3);
		actionReturn.setValue(IrFactory.eINSTANCE.createExprVar(myResult));
	}

	/**
	 * This method changes the schedulability of the done action
	 * 
	 * @param counter
	 *            Global Var counter
	 */
	private void modifyDoneAction(Var counter, int portIndex, String portName) {

		NodeBlock blkNode = done.getBody().getFirst();
		Expression storeValue = IrFactory.eINSTANCE.createExprInt(0);
		Instruction store = IrFactory.eINSTANCE.createInstStore(counter,
				storeValue);
		blkNode.add(store);

		blkNode = done.getScheduler().getFirst();
		EList<Var> schedulerLocals = done.getScheduler().getLocals();
		Var localCounter = IrFactory.eINSTANCE.createVar(0, counter.getType(),
				"localCounterModif" + portName, true, portIndex);
		schedulerLocals.add(localCounter);

		Instruction load = IrFactory.eINSTANCE.createInstLoad(localCounter,
				counter);
		blkNode.add(1, load);

		Var temp = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeBool(), "temp" + portName, true, portIndex);
		schedulerLocals.add(temp);
		Expression guardValue = IrFactory.eINSTANCE.createExprInt(numTokens);
		Expression counterExpression = IrFactory.eINSTANCE
				.createExprVar(localCounter);
		Expression schedulerValue = IrFactory.eINSTANCE.createExprBinary(
				counterExpression, OpBinary.EQ, guardValue,
				IrFactory.eINSTANCE.createTypeBool());
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(temp,
				schedulerValue);
		int index = blkNode.getInstructions().size() - 1;
		blkNode.add(index, assign);
		index++;

		Expression buffrerExpression = IrFactory.eINSTANCE
				.createExprVar(result);
		Expression resultExpression = IrFactory.eINSTANCE.createExprVar(temp);
		Expression expression = IrFactory.eINSTANCE.createExprBinary(
				buffrerExpression, OpBinary.LOGIC_AND, resultExpression,
				IrFactory.eINSTANCE.createTypeBool());
		Instruction bufferAssign = IrFactory.eINSTANCE.createInstAssign(result,
				expression);
		blkNode.add(index, bufferAssign);
	}

	/**
	 * This method modifies the no-repeat-actions to read from buffers instead
	 * of ports
	 * 
	 */
	private void modifyNoRepeatActionsInFSM() {
		DirectedGraph<State, UniqueEdge> graph = fsm.getGraph();
		Set<UniqueEdge> edges = graph.edgeSet();
		for (UniqueEdge edge : edges) {
			Action action = (Action) edge.getObject();
			if (noRepeatActions.contains(action)) {
				ListIterator<Port> it = action.getInputPattern().getPorts()
						.listIterator();
				while (it.hasNext()) {
					Port port = it.next();
					if (inputPorts.contains(port)) {
						int position = portPosition(inputPorts, port);
						Procedure body = action.getBody();
						Var buffer = inputBuffers.get(position);
						Var writeIndex = writeIndexes.get(position);
						Var readIndex = readIndexes.get(position);

						ModifyActionScheduler modifyActionScheduler = new ModifyActionScheduler(
								buffer, writeIndex, port, bufferSize);
						modifyActionScheduler.doSwitch(action.getBody());
						modifyActionScheduler.doSwitch(action.getScheduler());
						modifyActionSchedulability(action, writeIndex,
								readIndex, OpBinary.NE,
								IrFactory.eINSTANCE.createExprInt(0), port);
						consumeToken(body, position, port);
						noRepeatActions.remove(action);

						int index = it.previousIndex();
						action.getInputPattern().remove(port);
						it = action.getInputPattern().getPorts()
								.listIterator(index);
					}
				}
			}
		}
	}

	/**
	 * this method transforms tagged actions in the FSM
	 * 
	 * @param actor
	 *            the actor containing the FSM to modify
	 */
	private void modifyRepeatActionsInFSM() {
		fsm = actor.getFsm();

		if (fsm == null) {
			List<Action> actions = new ArrayList<Action>(
					actor.getActionsOutsideFsm());
			boolean transform = false;
			for (Action verifAction : actions) {
				for (Entry<Port, Integer> verifEntry : verifAction
						.getInputPattern().getNumTokensMap().entrySet()) {
					int verifNumTokens = verifEntry.getValue();
					if (verifNumTokens > 1) {
						transform = true;
						break;
					}
				}
				for (Entry<Port, Integer> verifEntry : verifAction
						.getOutputPattern().getNumTokensMap().entrySet()) {
					int verifNumTokens = verifEntry.getValue();
					if (verifNumTokens > 1) {
						transform = true;
						break;
					}
				}
			}
			if (transform == true) {
				// ////////
				State initState = IrFactory.eINSTANCE.createState("init");
				statesMap.put("init", initState);
				// statesMap.put("init", initState);
				// no FSM: simply visit all the actions
				addFsm();
				for (Action action : actions) {
					visitTransition(initState, initState, action);
				}
				modifyNoRepeatActionsInFSM();
				transform = false;
			}
			// //////
		} else {
			// with an FSM: visits all transitions
			DirectedGraph<State, UniqueEdge> graph = fsm.getGraph();
			Set<UniqueEdge> edges = graph.edgeSet();
			for (UniqueEdge edge : edges) {
				State source = graph.getEdgeSource(edge);
				State target = graph.getEdgeTarget(edge);
				Action action = (Action) edge.getObject();
				visitTransition(source, target, action);
			}
			modifyNoRepeatActionsInFSM();
		}

	}

	/**
	 * this method transforms untagged actions containing repeats
	 * 
	 * @param actor
	 *            the actor containing the untagged actions to modify
	 */
	private void modifyUntaggedActions(Actor actor) {
		List<Action> actions = new ArrayList<Action>(
				actor.getActionsOutsideFsm());
		for (Action action : actions) {
			// modify only untagged actions existing before transformation
			if (!AddedUntaggedActions.contains(action)) {
				scanUntaggedInputs(action);
				scanUntaggedOutputs(action);
			}
		}
	}

	/**
	 * This method moves the local variables of a procedure to another using a
	 * VarLocal iterator
	 * 
	 * @param itVar
	 *            source VarLocal iterator
	 * @param newProc
	 *            target procedure
	 */
	private void moveLocals(Iterator<Var> itVar, Procedure newProc) {
		while (itVar.hasNext()) {
			Var var = itVar.next();
			itVar.remove();
			newProc.getLocals().add(var);
		}
	}

	/**
	 * This method moves the nodes of a procedure to another using a Node
	 * iterator
	 * 
	 * @param itNode
	 *            source node iterator
	 * @param newProc
	 *            target procedure
	 */
	private void moveNodes(ListIterator<Node> itNode, Procedure newProc) {
		while (itNode.hasNext()) {
			Node node = itNode.next();
			itNode.remove();
			newProc.getNodes().add(node);
		}
	}

	/**
	 * returns the position of a port in a port list
	 * 
	 * @param list
	 *            list of ports
	 * @param seekPort
	 *            researched port
	 * @return position of a port in a list
	 */
	private int portPosition(List<Port> list, Port seekPort) {
		int position = 0;
		for (Port inputPort : list) {
			if (inputPort == seekPort) {
				break;
			} else {
				position++;
			}
		}
		return position;
	}

	/**
	 * For every Input of the action this method creates the new required
	 * actions
	 * 
	 * @param action
	 *            action to transform
	 * @param sourceName
	 *            name of the source state of the action in the actor fsm
	 * @param targetName
	 *            name of the target state of the action in the actor fsm
	 */
	private void scanInputs(Action action, State sourceState, State targetState) {
		for (Entry<Port, Integer> verifEntry : action.getInputPattern()
				.getNumTokensMap().entrySet()) {
			int verifNumTokens = verifEntry.getValue();
			if (verifNumTokens > 1) {
				repeatInput = true;
				// create new process action
				process = createProcessAction(action);
				copyOutputPattern(action, process);
				//process.setOutputPattern(EcoreHelper.copy(action.getOutputPattern()));
				
				String storeName = "newStateStore" + action.getName();
				State storeState = IrFactory.eINSTANCE.createState(storeName);
				fsm.getStates().add(storeState);
				String processName = "newStateProcess" + action.getName();
				State processState = IrFactory.eINSTANCE
						.createState(processName);
				statesMap.put(processName, processState);
				fsm.getStates().add(processState);
				fsm.addTransition(processState, process, targetState);

				// move action's Output pattern to new process action
				

				Var untagBuffer = IrFactory.eINSTANCE.createVar(0, entryType,
						"buffer", true, true);
				Var untagReadIndex = IrFactory.eINSTANCE.createVar(0,
						IrFactory.eINSTANCE.createTypeInt(32),
						"UntagReadIndex", true,
						IrFactory.eINSTANCE.createExprInt(0));
				Var untagWriteIndex = IrFactory.eINSTANCE.createVar(0,
						IrFactory.eINSTANCE.createTypeInt(32),
						"UntagWriteIndex", true,
						IrFactory.eINSTANCE.createExprInt(0));
				// if input repeat detected --> treat all input ports

				for (Entry<Port, Integer> entry : action.getInputPattern()
						.getNumTokensMap().entrySet()) {
					numTokens = entry.getValue();
					inputIndex = inputIndex + 100;
					port = entry.getKey();
					bufferSize = 512;// OptimalBufferSize(action, port);
					entryType = port.getType();

					if (inputPorts.contains(port)) {
						int position = portPosition(inputPorts, port);
						untagBuffer = inputBuffers.get(position);
						untagReadIndex = readIndexes.get(position);
						untagWriteIndex = writeIndexes.get(position);
					} else {
						inputPorts.add(port);
						untagBuffer = createTab(port.getName() + "_buffer",
								entryType, bufferSize);
						inputBuffers.add(untagBuffer);
						untagReadIndex = createCounter("readIndex_"
								+ port.getName());
						readIndexes.add(untagReadIndex);
						untagWriteIndex = createCounter("writeIndex_"
								+ port.getName());
						writeIndexes.add(untagWriteIndex);
						createUntaggedAction(untagReadIndex, untagWriteIndex,
								untagBuffer, port, true);
					}

					String counterName = action.getName() + "NewStoreCounter"
							+ inputIndex;
					Var counter = createCounter(counterName);
					String listName = action.getName() + "NewStoreList"
							+ inputIndex;
					Var tab = createTab(listName, entryType, numTokens);

					store = createStoreAction(action.getName(), counter, tab,
							untagBuffer, untagWriteIndex);

					ModifyProcessActionStore modifyProcessAction = new ModifyProcessActionStore(
							tab);
					modifyProcessAction.doSwitch(process.getBody());
					fsm.addTransition(storeState, store, storeState);
					// create a new store done action once
					if (inputIndex == 100) {
						done = createDoneAction(action.getName()
								+ "newStoreDone", counter, numTokens);
						fsm.addTransition(storeState, done, processState);
					} else {
						// the new done action already exists --> modify
						// schedulability
						modifyDoneAction(counter, inputIndex, port.getName());
					}
					actionToTransition(port, action, untagBuffer, untagWriteIndex,
							untagReadIndex);
					// action.getInputPattern().remove(port);
				}

				action.getBody().getNodes().clear();
				NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();
				block = IrFactoryImpl.eINSTANCE.createNodeBlock();
				block.add(IrFactory.eINSTANCE.createInstReturn());
				action.getBody().getNodes().add(block);
				fsm.replaceTarget(sourceState, action, storeState);
				action.getInputPattern().clear();
				break;

			}
		}
		inputIndex = 0;
	}

	/**
	 * For every output of the action this method creates the new required
	 * actions
	 * 
	 * @param action
	 *            action to transform
	 * @param sourceName
	 *            name of the source state of the action in the actor fsm
	 * @param targetName
	 *            name of the target state of the action in the actor fsm
	 */
	private void scanOutputs(Action action, State sourceState, State targetState) {
		for (Entry<Port, Integer> verifEntry : action.getOutputPattern()
				.getNumTokensMap().entrySet()) {
			int verifNumTokens = verifEntry.getValue();
			if (verifNumTokens > 1) {
				repeatOutput = true;
				String processName = "newStateProcess" + action.getName();
				String writeName = "newStateWrite" + action.getName();
				State writeState = IrFactory.eINSTANCE.createState(writeName);
				fsm.getStates().add(writeState);
				// create new process action if not created while treating
				// inputs
				if (!repeatInput) {
					fsm.replaceTarget(sourceState, action, writeState);
				} else {
					State processState = statesMap.get(processName);
					fsm.replaceTarget(processState, process, writeState);
					process.getOutputPattern().clear();
				}
				for (Entry<Port, Integer> entry : action.getOutputPattern()
						.getNumTokensMap().entrySet()) {
					numTokens = entry.getValue();
					outputIndex = outputIndex + 100;
					port = entry.getKey();
					entryType = port.getType();
					String counterName = action.getName() + "NewWriteCounter"
							+ outputIndex;
					Var counter = createCounter(counterName);
					String listName = action.getName() + "NewWriteList"
							+ outputIndex;
					Var tab = createTab(listName, entryType, numTokens);
					write = createWriteAction(action.getName(), counter, tab);
					write.getOutputPattern().setNumTokens(port, 1);
					if (!repeatInput) {
						ModifyProcessActionWrite modifyProcessActionWrite = new ModifyProcessActionWrite(
								tab);
						modifyProcessActionWrite.doSwitch(action.getBody());
					}else{
					ModifyProcessActionWrite modifyProcessActionWrite = new ModifyProcessActionWrite(
							tab);
					modifyProcessActionWrite.doSwitch(process.getBody());
					}
					fsm.addTransition(writeState, write, writeState);

					// create a new write done action once
					if (outputIndex == 100) {
						done = createDoneAction(action.getName()
								+ "newWriteDone", counter, numTokens);
						fsm.addTransition(writeState, done, targetState);

					} else {
						modifyDoneAction(counter, outputIndex, port.getName());
					}
				}
				// remove outputPattern from transition action
				action.getOutputPattern().clear();
				break;
			}
		}
		outputIndex = 0;
	}

	/**
	 * this method visits the inputs of an untagged action to check repeats
	 * 
	 * @param action
	 *            action containing the inputs to check
	 */
	private void scanUntaggedInputs(Action action) {
		for (Entry<Port, Integer> verifEntry : action.getInputPattern()
				.getNumTokensMap().entrySet()) {
			int verifNumTokens = verifEntry.getValue();
			Port verifPort = verifEntry.getKey();
			Type entryType = verifPort.getType();
			bufferSize = 512;// OptimalBufferSize(action, verifPort);
			if (inputPorts.contains(verifPort)) {
				int position = portPosition(inputPorts, verifPort);
				Var buffer = inputBuffers.get(position);
				Var writeIndex = writeIndexes.get(position);
				Var readIndex = readIndexes.get(position);
				ModifyActionScheduler modifyActionScheduler = new ModifyActionScheduler(
						buffer, writeIndex, verifPort, bufferSize);
				modifyActionScheduler.doSwitch(action.getBody());
				modifyActionScheduler.doSwitch(action.getScheduler());
				modifyActionSchedulability(action, writeIndex, readIndex,
						OpBinary.GE,
						IrFactory.eINSTANCE.createExprInt(verifNumTokens),
						verifPort);
				updateUntagIndex(action, writeIndex, verifNumTokens);
				action.getInputPattern().remove(verifPort);
			} else {
				if (verifNumTokens > 1) {
					Var untagBuffer = IrFactory.eINSTANCE.createVar(0,
							entryType, "buffer", true, true);
					Var untagReadIndex = IrFactory.eINSTANCE.createVar(0,
							IrFactory.eINSTANCE.createTypeInt(32),
							"UntagReadIndex", true,
							IrFactory.eINSTANCE.createExprInt(0));
					Var untagWriteIndex = IrFactory.eINSTANCE.createVar(0,
							IrFactory.eINSTANCE.createTypeInt(32),
							"UntagWriteIndex", true,
							IrFactory.eINSTANCE.createExprInt(0));
					inputPorts.add(verifPort);
					untagBuffer = createTab(verifPort.getName() + "_buffer",
							entryType, bufferSize);
					inputBuffers.add(untagBuffer);
					untagReadIndex = createCounter("readIndex_"
							+ verifPort.getName());
					readIndexes.add(untagReadIndex);
					untagWriteIndex = createCounter("writeIndex_"
							+ verifPort.getName());
					writeIndexes.add(untagWriteIndex);
					createUntaggedAction(untagReadIndex, untagWriteIndex,
							untagBuffer, verifPort, false);

					ModifyActionScheduler modifyActionScheduler = new ModifyActionScheduler(
							untagBuffer, untagWriteIndex, verifPort, bufferSize);
					modifyActionScheduler.doSwitch(action.getBody());
					modifyActionScheduler.doSwitch(action.getScheduler());
					modifyActionSchedulability(action, untagWriteIndex,
							untagReadIndex, OpBinary.GE,
							IrFactory.eINSTANCE.createExprInt(verifNumTokens),
							verifPort);
					updateUntagIndex(action, untagWriteIndex, verifNumTokens);
					action.getInputPattern().remove(verifPort);
				}
			}
		}
		AddedUntaggedActions.add(action);
	}

	/**
	 * this method visits the outputs of an untagged action to check repeats
	 * 
	 * @param action
	 *            action containing the outputs to check
	 */
	private void scanUntaggedOutputs(Action action) {
		for (Entry<Port, Integer> verifEntry : action.getInputPattern()
				.getNumTokensMap().entrySet()) {
			int verifNumTokens = verifEntry.getValue();
			if (verifNumTokens > 1) {
				for (Entry<Port, Integer> entry : action.getInputPattern()
						.getNumTokensMap().entrySet()) {
					numTokens = entry.getValue();
					Port verifPort = entry.getKey();
					String name = "TokensToSend" + verifPort.getName();
					Var tokensToSend = createCounter(name);
					Expression condition = IrFactory.eINSTANCE
							.createExprBinary(IrFactory.eINSTANCE
									.createExprVar(tokensToSend), OpBinary.GT,
									IrFactory.eINSTANCE.createExprInt(0),
									IrFactory.eINSTANCE.createTypeBool());
					String actionName = "untaggedWrite_" + verifPort.getName();
					Action untaggedWrite = createAction(condition, actionName);
					Pattern pattern = untaggedWrite.getOutputPattern();
					pattern.setNumTokens(verifPort, 1);
					Var OUTPUT = IrFactory.eINSTANCE.createVar(0,
							verifPort.getType(),
							verifPort.getName() + "OUTPUT", true, 0);
					pattern.setVariable(verifPort, OUTPUT);
					// add instruction: tokensToSend = tokensToSend - 1 ;
					Var numTokenToSend = IrFactory.eINSTANCE.createVar(0,
							IrFactory.eINSTANCE.createTypeInt(32),
							"numTokensToSend", true, 0);
					Instruction load = IrFactory.eINSTANCE.createInstLoad(
							numTokenToSend, tokensToSend);
					NodeBlock untaggedBlkNode = untaggedWrite.getBody()
							.getLast();
					untaggedBlkNode.add(load);
					Var decNbTokensToSend = IrFactory.eINSTANCE.createVar(0,
							IrFactory.eINSTANCE.createTypeInt(32),
							"decNbTokensToSend", true, 0);
					Expression value = IrFactory.eINSTANCE.createExprBinary(
							IrFactory.eINSTANCE.createExprVar(numTokenToSend),
							OpBinary.MINUS,
							IrFactory.eINSTANCE.createExprInt(1),
							IrFactory.eINSTANCE.createTypeInt(32));
					Instruction assign = IrFactory.eINSTANCE.createInstAssign(
							decNbTokensToSend, value);
					untaggedBlkNode.add(assign);
					Instruction store = IrFactory.eINSTANCE.createInstStore(
							tokensToSend, IrFactory.eINSTANCE
									.createExprVar(decNbTokensToSend));
					untaggedBlkNode.add(store);
					// add untagged action in high priority
					actor.getActionsOutsideFsm().add(0, untaggedWrite);
					// add write condition to untagged action
					NodeBlock blkNode = action.getBody().getLast();
					Instruction store2 = IrFactory.eINSTANCE.createInstStore(
							tokensToSend,
							IrFactory.eINSTANCE.createExprInt(numTokens));
					blkNode.add(store2);
				}
			}
		}
		AddedUntaggedActions.add(action);
	}

	/**
	 * This method updates the write index of the buffer after reading tokens
	 * 
	 * @param action
	 *            untagged action to change
	 * @param writeIndex
	 *            index to update
	 * @param numTokens
	 *            number of tokens read from the buffer
	 */
	private void updateUntagIndex(Action action, Var writeIndex, int numTokens) {
		NodeBlock blkNode = action.getBody().getLast();
		EList<Var> locals = action.getBody().getLocals();
		Var localWriteIndex = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "localWriteIndex", true,
				0);

		locals.add(localWriteIndex);
		Instruction load = IrFactory.eINSTANCE.createInstLoad(localWriteIndex,
				writeIndex);
		blkNode.add(load);
		Var updatedIndex = IrFactory.eINSTANCE.createVar(0,
				IrFactory.eINSTANCE.createTypeInt(32), "updatedIndex", true, 0);
		locals.add(updatedIndex);
		Expression value = IrFactory.eINSTANCE.createExprBinary(
				IrFactory.eINSTANCE.createExprVar(localWriteIndex),
				OpBinary.PLUS, IrFactory.eINSTANCE.createExprInt(numTokens),
				IrFactory.eINSTANCE.createTypeInt(32));
		Instruction assign = IrFactory.eINSTANCE.createInstAssign(updatedIndex,
				value);
		blkNode.add(assign);
		Instruction store = IrFactory.eINSTANCE.createInstStore(writeIndex,
				IrFactory.eINSTANCE.createExprVar(updatedIndex));
		blkNode.add(store);
	}

	/**
	 * visits a transition characterized by its source name, target name and
	 * action
	 * 
	 * @param sourceName
	 *            source state
	 * @param targetName
	 *            target state
	 * @param action
	 *            action of the transition
	 */
	private void visitTransition(State sourceState, State targetState,
			Action action) {
		createActionsSet(action, sourceState, targetState);
		if (repeatInput && !repeatOutput) {
			// output pattern already copied in process action
			action.getOutputPattern().clear();
		}

		if (!repeatInput && !noRepeatActions.contains(action)) {
			noRepeatActions.add(action);
		}
		repeatInput = false;
		repeatOutput = false;
	}
}