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
 * about
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF YUSE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.orcc.backends.fiacre

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Map.Entry
import net.sf.orcc.OrccRuntimeException
import net.sf.orcc.df.Action
import net.sf.orcc.df.Actor
import net.sf.orcc.df.Connection
import net.sf.orcc.df.Instance
import net.sf.orcc.df.Port
import net.sf.orcc.ir.Arg
import net.sf.orcc.ir.ArgByVal
import net.sf.orcc.ir.Block
import net.sf.orcc.ir.BlockBasic
import net.sf.orcc.ir.BlockIf
import net.sf.orcc.ir.BlockWhile
import net.sf.orcc.ir.ExprBinary
import net.sf.orcc.ir.ExprBool
import net.sf.orcc.ir.ExprFloat
import net.sf.orcc.ir.ExprInt
import net.sf.orcc.ir.ExprList
import net.sf.orcc.ir.ExprString
import net.sf.orcc.ir.ExprUnary
import net.sf.orcc.ir.ExprVar
import net.sf.orcc.ir.Expression
import net.sf.orcc.ir.InstAssign
import net.sf.orcc.ir.InstCall
import net.sf.orcc.ir.InstLoad
import net.sf.orcc.ir.InstPhi
import net.sf.orcc.ir.InstReturn
import net.sf.orcc.ir.InstStore
import net.sf.orcc.ir.Instruction
import net.sf.orcc.ir.OpBinary
import net.sf.orcc.ir.OpUnary
import net.sf.orcc.ir.Type
import net.sf.orcc.ir.TypeBool
import net.sf.orcc.ir.TypeFloat
import net.sf.orcc.ir.TypeInt
import net.sf.orcc.ir.TypeList
import net.sf.orcc.ir.TypeString
import net.sf.orcc.ir.TypeUint
import net.sf.orcc.ir.TypeVoid
import net.sf.orcc.ir.Var
import net.sf.orcc.util.Attributable
import org.eclipse.emf.common.util.EList
import net.sf.orcc.backends.CommonPrinter
import net.sf.orcc.df.Pattern
import net.sf.orcc.df.FSM
import net.sf.orcc.df.State
import net.sf.orcc.df.Transition
import net.sf.orcc.ir.Procedure

/**
 * Generate and print instance source files for the CAL actors,
 * primarily to serve the dataflow transformations in 
 * net.sf.orcc.df.transform driven by the Graphiti interface.
 *  
 * @author Rob Stewart
 * 
 */
class InstancePrinter extends CommonPrinter {

	protected var String packageDir
	protected var String actorName
	protected var Instance instance
	protected var Actor actor
	protected var Attributable attributable
	protected var Map<Port, Connection> incomingPortMap
	protected var Map<Port, List<Connection>> outgoingPortMap
	protected var String entityName
	
	protected var List<String> topLevelStateTransitions = new ArrayList

	def public setActor(Actor actor) {
		this.actor = actor;
	}

	def public getFileContent() {

		var int numInputs = actor.getInputs().size()
		var int numOutputs = actor.getOutputs().size()
		packageDir = actor.package.replaceAll("\\.", "/") + "/"

		'''
process «actor.simpleName» [«printPorts(actor.inputs)» , «printPorts(actor.outputs)» ] is
    state «printStates(actor)»
    var «printPrivateVars(actor.stateVars)» token : int
    «printTransitions(actor)»
    
    «printTopLevelAtomicTransitions(topLevelStateTransitions)»

/* simulation process to communicate with actor process */
process «actor.simpleName»_dual [«listPortNamesDual(actor)»] is
    var x : int
    «printDualConsumersProducers(actor)»


/* simulate IO with the actor */
component «actor.simpleName»_interaction is
   port «componentPorts(actor)»
   par * in
       «actor.simpleName» [«listPortNames(actor)»]
    || «actor.simpleName»_dual [«listPortNamesDual(actor)»]
    end
    
/* entry point */
«actor.simpleName»_interaction
'''
	}
	
	def private printTopLevelAtomicTransitions(List<String> atomicTransitions) {
		delimitWith(atomicTransitions, "\n")
	}

	def private printTransitions(Actor actor) {
		var List<String> fiacreTransitions = new ArrayList()
		var Map<State, List<ActionTargetState>> inlinedFSM = inlineActionsIntoFSM(actor.fsm)
		var entries = inlinedFSM.entrySet
		for (Entry<State,List<ActionTargetState>> entry : entries) {
			fiacreTransitions.add(printTransitionsFromState(entry).toString)
		}

		delimitWith(fiacreTransitions, "\n")
	}

	def private printTransitionsFromState(Entry<State, List<ActionTargetState>> entry) {
		'''
			from «entry.key» select 
			«printTransition(entry.key, entry.value.get(0))»
			«printChoices(entry.key, entry.value.subList(1,entry.value.size))»
			end
		'''
	}

	def private firstCommPattern(Action action) {
		var String fiacrePattern = ""
		if (action.inputPattern.ports.size == 0 && action.outputPattern.ports.size == 0) {
			fiacrePattern = "true"
		} else if (action.inputPattern.ports.size == 0) {
			fiacrePattern = action.outputPattern.ports.get(0).name + "!0"
		} else {
			var Var inVar = action.inputPattern.getVariable(
				action.inputPattern.ports.get(0)
			)
			var String firstInVar = getFirstToken(action, action.inputPattern.ports.get(0).name, inVar)
			fiacrePattern = firstInVar
		}
		fiacrePattern
	}

	def getFirstToken(Action action, String portName, Var inVar) {
		var String tokenVar = ""
		var String pattern = ""
		var BlockBasic block = action.body.first
		var instIter = block.instructions.listIterator
		var found = false
		while (!found && instIter.hasNext) {
			var Instruction inst = instIter.next
			switch (inst) {
				InstLoad: {
					if (!inst.indexes.isEmpty) {
						var ExprInt index = (inst.indexes.get(0) as ExprInt)
						if (index == 0) {
							tokenVar = inst.target.variable.name
							found = true
						}
					}
				}
			}
		}
		if (!found) {
			pattern = "true"
		} else {
			pattern = portName + "?" + tokenVar
		}
		pattern
	}

	def private printTransition(State sourceState, ActionTargetState transition) {
		var Action transAction = transition.getTransitionAction
		var State transDestState = transition.getTargetState
		var List<Instruction> statementsInTransitionAction = allInstructionsInBody(transAction.body)

		'''
			on guard «firstCommPattern(transAction)»
			«printStatements(statementsInTransitionAction)»
			«printTo(transAction, transAction.inputPattern, transAction.outputPattern,
			sourceState, transDestState)»
		'''
	}
	
	static class PortTokens {
		String p
	    List<String> tokenVars

		new(String p, List<String> ts) {
			this.p = p;
			this.tokenVars = ts;
		}

		def getPort() { p; }

		def getTokens() { tokenVars; }
	}
	

	def private printTo(Action transAction, Pattern inputPattern, Pattern outputPattern, State sourceState, State destState) {
		var String fiacreTo = ""
		if (inputPattern.ports.size == 0 && outputPattern.ports.size == 0) {
			fiacreTo = "to " + destState.name
		} else {
			var inputs = inputPatternMap(transAction)
			var outputs = outputPatternMap(transAction)
			topLevelStateTransitions.add(
				printSingleComm(inputs, outputs, sourceState,
					destState, 2)
				)
			fiacreTo = "to " + sourceState.name + "_2"
		}

		fiacreTo
	}
	
	def private String printSingleComm(List<PortTokens> inputs, List<PortTokens> outputs, State sourceState,
		State destState, int n) {
		var String str = ""
		if (inputs.size == 0 && outputs.size == 0) {
			str = " from " + sourceState.name + "_" + n + " ; to " + destState.name
		} else if (inputs.size > 0 && inputs.get(0).getTokens.length == 1) {
			str = " from " + sourceState.name + "_" + n + " " + inputs.get(0).getPort + "?token" + " to " + sourceState.name +
				"_" + (n + 1) + "\n " + printSingleComm(inputs.subList(1,inputs.size), outputs, sourceState, destState, n + 1)
		} else if (inputs.size > 0) {
			str = " from " + sourceState.name + "_" + n + " " + inputs.get(0).getPort + "?token" + " to " + sourceState.name +
				"_" + (n + 1)

			inputs.get(0).getTokens.remove(0)
			str += "\n " + printSingleComm(inputs, outputs, sourceState, destState, n + 1)
		} else if (inputs.size == 0 && outputs.size == 1 && outputs.get(0).getTokens.size==1) {
			str = " from " + sourceState.name + "_" + n + " " + outputs.get(0).getPort + "!0" + " to " + sourceState + "_" +
				(n + 1) + "\n " + printSingleComm(new ArrayList, new ArrayList, sourceState, destState, n + 1)
		} else if (inputs.size == 0 && outputs.size > 0) {
			str = " from " + sourceState.name + "_" + n + " " + outputs.get(0).getPort + "!0" + " to " + sourceState + "_" +
				(n + 1)
				if (outputs.get(0).getTokens.size > 0) {
			outputs.get(0).getTokens.remove(0)
			} else if (outputs.get(0).getTokens.size == 0) {
				outputs.remove(0)
			}
			str += "\n " + printSingleComm(inputs, outputs, sourceState, destState, n+1)
			}
		else{
			System.out.println("printSingleComm: non exhaustive pattern matching.")
		}

		str
	}

	def private inputPatternMap(Action action) {
		var Map<String, List<String>> portTokenMap = new HashMap<String, List<String>>
		for (Instruction inst : allInstructionsInBody(action.body)) {
			switch (inst) {
				InstLoad: {
					var instLoad = (inst as InstLoad)
					if (portTokenMap.containsKey(inst.source.variable.name)) {
						portTokenMap.get(instLoad.source.variable.name).add(instLoad.target.variable.name)
					} else {
						portTokenMap.put(inst.source.variable.name, new ArrayList)
						portTokenMap.get(inst.source.variable.name).add(instLoad.target.variable.name)
					}
				}
				default: {
				}
			}
		}

		var List<PortTokens> patternMap = new ArrayList<PortTokens>
		for (Entry<String,List<String>> entry : portTokenMap.entrySet) {
			patternMap.add(new PortTokens(entry.key, entry.value))
		}
		
		patternMap
	}

	def private outputPatternMap(Action action) {
		var Map<String, List<String>> portTokenMap = new HashMap<String, List<String>>
		for (Instruction inst : allInstructionsInBody(action.body)) {
			switch (inst) {
				InstStore: {
					var instStore = (inst as InstStore)
					if (portTokenMap.containsKey(instStore.target.variable.name)) {
						portTokenMap.get(instStore.target.variable.name).add(printExpression(instStore.value).toString)
					} else {
						portTokenMap.put(instStore.target.variable.name, new ArrayList)
						portTokenMap.get(instStore.target.variable.name).add(printExpression(instStore.value).toString)
					}
				}
				default: {
				}
			}
		}
	
		
		var List<PortTokens> patternMap = new ArrayList<PortTokens>
		for (Entry<String,List<String>> entry : portTokenMap.entrySet) {
			patternMap.add(new PortTokens(entry.key, entry.value))
		}
		
		
		patternMap
	}

	def private printStatements(List<Instruction> statements) {
		var List<String> fiacreStatements = new ArrayList()
		for (Instruction inst : statements) {
			fiacreStatements.add(printInstruction(inst).toString)
		}
		delimitWith(fiacreStatements, "\n")
	}

	def protected printInstruction(Instruction inst) {
		// TODO implement printer for all instructions
		val String instPrinted = switch inst {
			InstAssign:
				inst.target.variable.name + " := " + printExpression(inst.value) + ";"
			InstCall:
				inst.procedure.name + "(" + printArgs(inst.arguments) + ");"
			InstLoad:
				"" // loads are ignored.
			InstPhi:
				""
			InstReturn:
				""
			InstStore:
				if (isTargetOutputPort(inst.target.variable.name))
					""
				else
					inst.target.variable.name + " := " + printExpression(inst.value) + ";"
			default:
				"" // covers backend specific instructions
		}

		'''«instPrinted»'''
	}

	def protected isTargetInputPort(String str) {
		var boolean b
		for (Port p : actor.inputs) {
			if (str.equals(p.name)) {
				b = true
			}
		}
		return b
	}

	def protected isTargetOutputPort(String str) {
		var boolean b
		for (Port p : actor.outputs) {
			if (str.equals(p.name)) {
				b = true
			}
		}
		return b
	}

	def protected printArgs(EList<Arg> args) {
		var List<String> argsStrs = new ArrayList()
		for (Arg arg : args) {
			if (arg.isByVal()) {
				val exp = (arg as ArgByVal).value
				argsStrs.add(printExpression(exp).toString)
			}
		}
		delimitWith(argsStrs, "+")
	}

    // TODO
	def private printChoices(State source, List<ActionTargetState> rest) {
	}

	static class ActionTargetState {
		Action transitionAction
		State targetState

		new(Action a, State s) {
			transitionAction = a;
			targetState = s;
		}

		def getTransitionAction() { transitionAction; }

		def getTargetState() { targetState; }
	}

	def private inlineActionsIntoFSM(FSM fsm) {
		var Map<State, List<ActionTargetState>> inlinedFSM = new HashMap()
		if (fsm != null) {
			var i = 0
			for (Transition t : fsm.transitions) {
				if (inlinedFSM.containsKey(t.source)) {
					var List<ActionTargetState> existingList = inlinedFSM.get(t.source)
					existingList.add(new ActionTargetState(t.action, t.target))
					inlinedFSM.put(t.source, existingList)
				} else {
					var List<ActionTargetState> newList = new ArrayList
					newList.add(new ActionTargetState(t.action, t.target))
					inlinedFSM.put(t.source, newList)
				}
				i++
			}
		}
		inlinedFSM
	}

	def private listPortNames(Actor actor) {
		var List<String> fiacreVars = new ArrayList()
		for (Port p : actor.inputs) {
			fiacreVars.add(p.name)
		}
		for (Port p : actor.outputs) {
			fiacreVars.add(p.name)
		}
		delimitWith(fiacreVars, ",")
	}

	def private listPortNamesDual(Actor actor) {
		var List<String> fiacreVars = new ArrayList()
		for (Port p : actor.outputs) {
			fiacreVars.add(p.name + " : " + printType(p.type))
		}
		for (Port p : actor.inputs) {
			fiacreVars.add(p.name + " : " + printType(p.type))
		}
		delimitWith(fiacreVars, ",")
	}
	
	def private printDualConsumersProducers(Actor actor) {
		var List<String> fiacreConsumerProducer = new ArrayList()
		for (Port p : actor.inputs) {
			fiacreConsumerProducer.add("from s0 " + p.name + "!0 to s0")
		}
		for (Port p : actor.outputs) {
			fiacreConsumerProducer.add("from s0 " + p.name + "?x to s0")
		}
		delimitWith(fiacreConsumerProducer, "\n")
	}

	def private componentPorts(Actor actor) {
		var List<String> fiacreVars = new ArrayList()
		for (Port p : actor.inputs) {
			fiacreVars.add(p.name + " : " + printType(p.type))
		}
		for (Port p : actor.outputs) {
			fiacreVars.add(p.name + " : " + printType(p.type))
		}
		delimitWith(fiacreVars, ",")
	}

	def private printStates(Actor actor) {
		var int numComms = numCommunications(actor)
		var List<String> fiacreStates = new ArrayList()
		for (i : 0 ..< numComms) {
			fiacreStates.add("s" + i)
		}
		delimitWith(fiacreStates, ",")
	}

	def private numCommunications(Actor actor) {
		var int i = 0
		for (Action action : actor.actions) {
			var Pattern inPattern = action.inputPattern
			for (Port p : actor.inputs) {
				i += inPattern.getNumTokens(p)
			}
			for (Port p : actor.outputs) {
				i += inPattern.getNumTokens(p)
			}
			var Pattern outPattern = action.outputPattern
			for (Port p : actor.inputs) {
				i += outPattern.getNumTokens(p)
			}
			for (Port p : actor.outputs) {
				i += outPattern.getNumTokens(p)
			}
		}
		i
	}

	def private printPrivateVars(EList<Var> vars) {
		var List<String> fiacreVars = new ArrayList()
		for (Var v : vars) {
			fiacreVars.add(printPrivateVar(v))
		}
		terminateWith(fiacreVars, ",")
	}

	def private printPrivateVar(Var v) {
		v.name + " : " + printType(v.type)
	}

	def private printPorts(EList<Port> ports) {
		var List<String> fiacrePorts = new ArrayList()
		for (Port port : ports) {
			fiacrePorts.add(port.name + ": " + printType(port.type))
		}
		delimitWith(fiacrePorts, ",")
	}

	def private printType(Type type) {
		switch type {
			TypeBool: "bool"
			// TypeFloat: "float"
			TypeInt: "int"
			TypeUint: "int"
			default: "int" // what should this be?
		}
	}

	def protected List<Instruction> allInstructionsInBody(Procedure body) {
		var List<Instruction> actionInstructions = new ArrayList
		for (Block block : body.blocks) {
			for (Instruction inst : allInstructionsInBlock(block)) {
				actionInstructions.add(inst)
			}
		}
		actionInstructions
	}

	def protected List<Instruction> allInstructionsInBlock(Block block) {
		switch block {
			BlockBasic: instructionsFromBlockBasic(block)
			BlockIf: instructionsFromBlockIf(block)
			BlockWhile: instructionsFromBlockWhile(block)
		}
	}

	def protected instructionsFromBlockBasic(BlockBasic block) {
		block.instructions
	}

	def protected instructionsFromBlockIf(BlockIf block) {
		var List<Instruction> instructions = new ArrayList
		for (Block thenBlock : block.thenBlocks) {
			instructions.addAll(allInstructionsInBlock(thenBlock))
		}
		for (Block elseBlock : block.elseBlocks) {
			instructions.addAll(allInstructionsInBlock(elseBlock))
		}
		instructions
	}

	def protected instructionsFromBlockWhile(BlockWhile block) {
		var List<Instruction> instructions = new ArrayList
		for (Block subBlock : block.blocks) {
			instructions.addAll(allInstructionsInBlock(subBlock))
		}
		instructions
	}

//
	def protected printList(ExprList list) {
		var List<String> elems = new ArrayList<String>()
		for (Expression exp : list.value) {
			elems.add(printExpression(exp).toString)
		}
		delimitWith(elems, ",")
	}

//
	def protected CharSequence printExpression(Expression exp) {
		val String expPrinted = switch exp {
			ExprBinary: printBinaryExpr(exp).toString
			ExprBool: if(exp.isValue) "true" else "false"
			ExprFloat: exp.value.toString
			ExprInt: Integer.toString(exp.intValue)
			ExprList: printList(exp)
			ExprString: "\"" + exp.value + "\""
			ExprUnary: printUnaryOp(exp.op).toString
			ExprVar: printLocalVarExpr(exp)
		}
		'''«expPrinted»'''
	}

//
	def protected printLocalVarExpr(ExprVar exp) {
		exp.use.variable.name.replace("local_", "")
	}

//
	def protected printUnaryOp(OpUnary op) {
		val String opPrinted = switch (op) {
			case BITNOT: "not"
			case LOGIC_NOT: "not"
			case MINUS: "-"
			case NUM_ELTS: ""
			default: ""
		}
		'''«opPrinted»'''
	}

//
	def protected printBinaryExpr(ExprBinary exp) {
		val e1 = exp.getE1()
		val e1WellFormed = if (e1.isExprBinary)
				("(" + printExpression(e1).toString + ")")
			else
				printExpression(e1).toString

		val e2 = exp.getE2()
		val e2WellFormed = if (e2.isExprBinary)
				("(" + printExpression(e2).toString + ")")
			else
				printExpression(e2).toString
		val expWellFormed = e1WellFormed + " " + printBinaryOp(exp.getOp) + " " + e2WellFormed

		'''«expWellFormed»'''
	}

//
//	def protected printLocalVars(Action action) {
//		val EList<Var> locals = action.body.locals
//		var List<String> varDecls = new ArrayList<String>()
//		var List<String> inPatternVars = inputPatternVars(action)
//		for (Var localVar : locals) {
//			if (!inPatternVars.contains(localVar.name))
//				varDecls.add(printType(localVar.type) + " " + formatVarName(localVar.name))
//		}
//		if (varDecls.size() > 0) '''
//			var
//			«delimitWith(varDecls,",")»
//		''' else ''''''
//	}
//
//	def protected formatVarName(String s) {
//		s.replace("local_", "")
//	}
//
//	def protected inputPatternVars(Action action) {
//		var Map<String, List<String>> inputMap = new HashMap
//		for (Instruction inst : action.body.first.instructions) {
//			if (inst instanceof InstLoad) {
//				var instLoad = inst as InstLoad
//				if (isTargetInputPort(instLoad.source.variable.name)) {
//					var List<String> previousList = new ArrayList
//					if (inputMap.get(instLoad.source.variable.name) != null) {
//						previousList = inputMap.get(instLoad.source.variable.name)
//					}
//					val token = formatVarName(instLoad.target.variable.name)
//					previousList.add(token)
//					inputMap.put(instLoad.source.variable.name, previousList)
//				}
//			}
//		}
//
//		var List<String> inPatternVars = new ArrayList
//		for (Entry<String,List<String>> entry : inputMap.entrySet) {
//			for (String varStr : entry.value) {
//				inPatternVars.add(varStr)
//			}
//		}
//		inPatternVars
//	}
//
//	def protected isTargetInputPort(String str) {
//		var boolean b
//		for (Port p : actor.inputs) {
//			if (str.equals(p.name)) {
//				b = true
//			}
//		}
//		return b
//	}
//
//	def protected isTargetOutputPort(String str) {
//		var boolean b
//		for (Port p : actor.outputs) {
//			if (str.equals(p.name)) {
//				b = true
//			}
//		}
//		return b
//	}
//
	def protected printBinaryOp(OpBinary op) {
		val String opPrinted = switch (op) {
			case BITAND: "&"
			case BITOR: "|"
			case BITXOR: "^"
			case DIV: "/"
			case DIV_INT: "/"
			case EQ: "="
			case GE: ">="
			case GT: ">"
			case LE: "<"
			case LOGIC_AND: "&&"
			case LOGIC_OR: "||"
			case LT: "<"
			case MINUS: "-"
			case MOD: "mod"
			case NE: "!="
			case PLUS: "+"
			case SHIFT_LEFT: "<<"
			case SHIFT_RIGHT: ">>"
			case TIMES: "*"
			default: ""
		}
		'''«opPrinted»'''
	}

def protected delimitWith(List<String> strList, String delimChar) {
		var StringBuilder sb = new StringBuilder();
		var String delim = "";
		for (String i : strList) {
			sb.append(delim).append(i);
			delim = delimChar;
		}
		sb.toString()
	}

//
	def protected terminateWith(List<String> strList, String delimChar) {
		var StringBuilder sb = new StringBuilder();
		var String delim = "";
		for (String i : strList) {
			sb.append(delim).append(i);
			delim = delimChar;
		}
		if (sb.toString().equals("")) {
			""
		}
		else {
			sb.toString() + ","
		}
	}

}
