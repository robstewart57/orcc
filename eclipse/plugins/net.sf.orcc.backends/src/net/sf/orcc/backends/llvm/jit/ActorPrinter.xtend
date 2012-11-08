/*
 * Copyright (c) 2012, IETR/INSA of Rennes
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
package net.sf.orcc.backends.llvm.jit

import java.util.ArrayList
import java.util.List
import net.sf.orcc.backends.ir.InstCast
import net.sf.orcc.backends.llvm.aot.InstancePrinter
import net.sf.orcc.df.Action
import net.sf.orcc.df.Actor
import net.sf.orcc.df.Pattern
import net.sf.orcc.df.Port
import net.sf.orcc.df.State
import net.sf.orcc.df.Transition
import net.sf.orcc.ir.InstLoad
import net.sf.orcc.ir.InstReturn
import net.sf.orcc.ir.InstStore
import net.sf.orcc.ir.Param
import net.sf.orcc.ir.Procedure
import net.sf.orcc.ir.Type
import net.sf.orcc.ir.TypeInt
import net.sf.orcc.ir.TypeList
import net.sf.orcc.ir.TypeUint
import net.sf.orcc.ir.Var
import net.sf.orcc.moc.CSDFMoC
import net.sf.orcc.moc.MoC
import net.sf.orcc.moc.QSDFMoC

/**
 * Generate Jade content
 * 
 * @author Antoine Lorence
 */
class ActorPrinter extends InstancePrinter {
	
	val Actor actor
	
	val List<Integer> objRefList = new ArrayList<Integer>
	val List<Pattern> patternList = new ArrayList<Pattern>
	
	new(Actor actor) {
		super()
		
		this.actor = actor
		
		computePatterns
		computeCastedList
	}
	
	/**
	 * Return the unique id associated with the given object, prepended with '!'
	 * 
	 * @param object the object
	 * @return unique reference to the given object
	 */
	def getObjectReference(Object object) {
		// We use hashCode instead of object itself in the list to ensure
		// for example 2 instances of Type with same parameters will be
		// duplicated in the list
		var id = objRefList.indexOf(object.hashCode)
		if(id == -1) {
			objRefList.add(object.hashCode)
			id = objRefList.size - 1
		}
		return '''!«id»'''
	}
	
	def getActorFileContent() '''
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		; Generated from "«actor.name»"
		declare i32 @printf(i8* noalias , ...) nounwind 
		
		«IF ! actor.inputs.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; Input FIFOs
			«FOR input : actor.inputs»
				«input.fifo»
			«ENDFOR»
		«ENDIF»
		
		«IF ! actor.outputs.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; Output FIFOs
			«FOR output : actor.outputs»
				«output.fifo»
			«ENDFOR»
		«ENDIF»
		
		«IF ! actor.parameters.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; Parameter values of the instance
			«FOR param : actor.parameters»
				«param.actorParameter»
			«ENDFOR»
		«ENDIF»
		
		«IF ! actor.stateVars.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; State variables of the actor
			«FOR variable : actor.stateVars»
				«variable.stateVar»
			«ENDFOR»
		«ENDIF»
		
		«IF ! actor.procs.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; Functions/procedures
			«FOR proc : actor.procs»
				«proc.print»
				
			«ENDFOR»
		«ENDIF»
		
		«IF ! actor.initializes.empty»
			;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; Initializes
			«FOR action : actor.initializes»
				«action.print»
				
			«ENDFOR»
		«ENDIF»
		
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		; Actions
		«FOR action : actor.actions»
			«action.print»
			
		«ENDFOR»
		
		«decl_MD»
	'''
	
	def decl_MD() '''
		!source = !{«actor.file.fullPath.getObjectReference»}
		!name = !{«actor.name.objectReference»}
		!action_scheduler = !{«actor.objectReference»}
		«IF ! actor.inputs.empty»
			!inputs = !{«actor.inputs.join(", ", [objectReference])»}
		«ENDIF»
		«IF ! actor.outputs.empty»
			!outputs = !{«actor.outputs.join(", ", [objectReference])»}
		«ENDIF»
		«IF ! actor.parameters.empty»
			!parameters = !{«actor.parameters.join(", ", [objectReference])»}
		«ENDIF»
		«IF ! actor.stateVars.empty»
			!state_variables = !{«actor.stateVars.join(", ", [objectReference])»}
		«ENDIF»
		
		«IF ! actor.procs.empty»
			!procedures = !{«actor.procs.join(", ", [objectReference])»}
		«ENDIF»
		«IF ! actor.initializes.empty»
			!initializes = !{«actor.initializes.join(", ", [objectReference])»}
		«ENDIF»
		«IF ! actor.actions.empty»
			!actions = !{«actor.actions.join(", ", [objectReference])»}
		«ENDIF»
		
		«IF actor.moC != null»
			!MoC = !{«actor.moC.objectReference»}
		«ENDIF»
		
		«actor.file.fullPath.objectReference» = metadata !{«actor.file.fullPath.toString.name_MD»}
		«actor.name.objectReference» = metadata !{«actor.name.name_MD»}
		
		; Action-scheduler
		«actionScheduler_MD»
		«IF ! actor.inputs.empty»
			
			; Input ports
			«FOR port : actor.inputs»
				«port.objectReference» = metadata !{metadata «port.type.objectReference», «port.name.name_MD», «port.type.doSwitch»** «port.fifoVarName»}
				«port.type.objectReference» = metadata !{«port.type.varType_MD», «port.type.varSize_MD»}
			«ENDFOR»
		«ENDIF»
		«IF ! actor.outputs.empty»
			
			; Output ports
			«FOR port : actor.outputs»
				«port.objectReference» = metadata !{metadata «port.type.objectReference», «port.name.name_MD», «port.type.doSwitch»** «port.fifoVarName»}
				«port.type.objectReference» = metadata !{«port.type.varType_MD», «port.type.varSize_MD»}
			«ENDFOR»
		«ENDIF»
		«IF ! actor.parameters.empty»
			
			; Parameters
			«FOR param : actor.parameters»
				; «param.name»
				«param.objectReference» = metadata !{metadata «param.name.objectReference», metadata «param.type.objectReference», «param.type.doSwitch»* @«param.name»}
				«param.name.objectReference» = metadata !{«param.name.name_MD», «param.varAssignable_MD», i32 0, «param.varIndex_MD»}
				«param.type.objectReference» = metadata  !{«param.type.varType_MD», «param.type.varSize_MD»}
			«ENDFOR»
		«ENDIF»
		«IF ! actor.stateVars.empty»
			
			; State Variables
			«FOR stateVar : actor.stateVars»
				;; «stateVar.name»
				«stateVar.objectReference» = metadata !{metadata «stateVar.name.objectReference», metadata «stateVar.type.objectReference», «IF stateVar.initialized»metadata «stateVar.initialValue.objectReference»«ELSE»null«ENDIF», «stateVar.type.doSwitch»* @«stateVar.name»}
				«stateVar.name.objectReference» = metadata !{«stateVar.name.name_MD», «stateVar.varAssignable_MD», i32 0, «stateVar.varIndex_MD»}
				«stateVar.type.objectReference» = metadata !{«stateVar.type.varType_MD», «stateVar.type.varSize_MD»}
				«IF stateVar.initialized»«stateVar.initialValue.objectReference» = metadata !{«stateVar.type.doSwitch» «stateVar.initialValue.doSwitch»}«ENDIF»
			«ENDFOR»
		«ENDIF»
		«IF ! actor.procs.empty»
			
			; Functions and procedures
			«FOR proc : actor.procs»
				«proc.objectReference» = metadata !{«proc.name.name_MD», «proc.procNative_MD», «proc.returnType.doSwitch»(«proc.parameters.join(", ", [argumentTypeDeclaration]).wrap»)* @«proc.name»}
			«ENDFOR»
		«ENDIF»
		«IF ! actor.initializes.empty»
			
			; Initializes
			«FOR action : actor.initializes»
				«action.action_MD»
				
			«ENDFOR»
		«ENDIF»
		; Actions
		«FOR action : actor.actions»
			«action.action_MD»
			
		«ENDFOR»
		; Patterns
		«FOR pattern : patternList»
			«pattern.pattern_MD»
		«ENDFOR»
		; Variables of patterns
		«FOR pattern : patternList.filter[portToVarMap != null]»
			«pattern.portToVarMap.objectReference» = metadata !{«pattern.ports.join(", ", ['''metadata «objectReference»'''])»}
		«ENDFOR»
		
		; Number of tokens of patterns
		«FOR pattern : patternList.filter[numTokensMap != null]»
			«pattern.numTokensMap.objectReference» = metadata !{«pattern.ports.join(", ", ['''metadata «objectReference», i32 «pattern.numTokensMap.get(it)»'''])»}
		«ENDFOR»
		«IF actor.hasMoC»
			
			; MoC
			«moC_MD»
		«ENDIF»
	'''
	
	def moC_MD() {
		val value =
			if (actor.moC.CSDF) ''' , metadata «actor.moC.objectReference»'''
			else if (actor.moC.quasiStatic) ''' , «(actor.moC as QSDFMoC).actions.join(", ", ['''metadata «objectReference»'''])»'''
			else ""
		'''
			«actor.moC.objectReference» = metadata !{«actor.moC.MoCName_MD»«value»}
		'''
	}

	def MoCName_MD(MoC moc) {
		val mocValue = switch(moc) {
			case moc.quasiStatic : "QuasiStatic"
			case moc.DPN : "DPN"
			case moc.KPN : "KPN"
			case moc.SDF : "SDF"
			case moc.CSDF : "CSDF"
		}
		'''metadata !"«mocValue»"'''
	}
		
	def pattern_MD(Pattern pattern) {
		val numTokens =
			if (pattern.numTokensMap != null) '''metadata «pattern.numTokensMap.objectReference»'''
			else "null"
		val variables =
			if ( pattern.portToVarMap != null) '''metadata «pattern.portToVarMap.objectReference»'''
			else "null"
		'''
			«pattern.objectReference» = metadata !{«numTokens», «variables»}
		'''
	}
	
	def action_MD(Action action) {
		val tag = if (action.tag.identifiers.empty) "null"
				else '''metadata «action.tag.objectReference»'''
		val input = if (action.inputPattern.empty) "null"
				else '''metadata «action.inputPattern.objectReference»'''
		val output = if (action.outputPattern.empty) "null"
				else '''metadata «action.outputPattern.objectReference»'''
		val peek = if (action.peekPattern.empty) "null"
				else '''metadata «action.peekPattern.objectReference»'''
		'''
			;; «action.name»
			«action.objectReference» = metadata !{«tag», «input», «output», «peek», metadata «action.scheduler.objectReference», metadata «action.body.objectReference»}
			«IF ! action.tag.identifiers.empty»
				«action.tag.objectReference» = metadata  !{«action.tag.identifiers.join(", ", [name_MD])»}
			«ENDIF»
			«action.scheduler.objectReference» = metadata  !{«action.scheduler.name.name_MD», «action.scheduler.procNative_MD», i1()* @«action.scheduler.name»}
			«action.body.objectReference» = metadata  !{«action.body.name.name_MD», «action.body.procNative_MD», void()* @«action.body.name»}
		'''
	}

	def argumentTypeDeclaration(Param param) {
		if (param.variable.type.string) "i8*"
		else if (param.variable.type.list) '''«param.variable.type.doSwitch»*'''
		else '''«param.variable.type.doSwitch»'''
	}

	def procNative_MD(Procedure proc)
		'''«IF proc.native»i1 1«ELSE»i1 0«ENDIF»'''

	
	def varIndex_MD(Var variable)
		'''i32 «IF variable.index != null»«variable.index»«ELSE»0«ENDIF»'''
	
	def varAssignable_MD(Var variable) 
		'''i1 «IF variable.assignable»1«ELSE»0«ENDIF»'''
	
	def varSize_MD(Type type)
		'''«IF type.list»«(type as TypeList).dimensions.join(", ", ['''i32 «it»'''])»«ELSE»null«ENDIF»'''
	
	def name_MD(String name)
		'''metadata !"«name»"'''
	
	def actionScheduler_MD() {
		val outsideFSM = if ( ! actor.actionsOutsideFsm.empty) '''metadata «actor.actionsOutsideFsm.objectReference»''' else "null"
		val fsm = if (actor.fsm != null) '''metadata «actor.fsm.objectReference»''' else "null"
		'''
			«actor.objectReference» = metadata !{«outsideFSM», «fsm»}
			«IF ! actor.actionsOutsideFsm.empty»
				
				;; Actions outside FSM
				«actor.actionsOutsideFsm.objectReference» = metadata !{«actor.actionsOutsideFsm.join(", ", ['''metadata «objectReference»'''])»}
			«ENDIF»
			«IF actor.fsm != null»
				
				;; FSM
				«FSM_MD»
			«ENDIF»
		'''
	}
	
	// In some case, «actor.fsm.transitions» can be the same object list than «actor.fsm.states.get(0).outgoing». We use «actor.name.concat("_transitions")» to prevent this
	def FSM_MD() '''
		«actor.fsm.objectReference» = metadata !{«actor.fsm.initialState.name.name_MD», metadata «actor.fsm.states.objectReference», metadata «actor.name.concat("_transitions").objectReference»}
		;;; States
		«actor.fsm.states.objectReference» = metadata !{«actor.fsm.states.join(", ", [name.name_MD])»}
		;;; All transitions
		«actor.name.concat("_transitions").objectReference» = metadata !{«actor.fsm.states.join(", ", ['''metadata «objectReference»'''])»}
		«FOR state : actor.fsm.states»
			«state.transition_MD»
		«ENDFOR»
	'''
	
	def transition_MD(State state) '''
		;;;; Transitions from «state.name»
		«IF state.outgoing.empty»
			«state.objectReference» = metadata !{«state.name.name_MD», null}
		«ELSE»
			«state.objectReference» = metadata !{«state.name.name_MD», metadata «state.outgoing.objectReference»}
			«state.outgoing.objectReference» = metadata !{«state.outgoing.join(", ", ['''metadata «(it as Transition).objectReference»'''])»}
			«FOR transition : state.outgoing»
				«(transition as Transition).objectReference» = metadata !{metadata «(transition as Transition).action.objectReference», «(transition as Transition).target.name.name_MD»}
			«ENDFOR»
		«ENDIF»
	'''

	def varType_MD(Type type) {
		switch type {
			case type.isInt: '''i32 «(type as TypeInt).size»'''
			case type.isUint: '''i32 «(type as TypeUint).size»'''
			case type.bool: '''i32 1'''
			case type.list: '''«(type as TypeList).innermostType.varType_MD»'''
			case type.string: '''i32 8'''
			default: ""
		}
	}
	
	override caseInstLoad(InstLoad load) '''
		«IF load.source.variable.type.list»
			«load.source.variable.varName(load)» = getelementptr «load.source.variable.type.doSwitch»* «load.source.variable.print», i32 0«load.indexes.join(", ", ", ", "", [printIndex])»
			«load.target.variable.print» = load «(load.source.variable.type as TypeList).innermostType.doSwitch»* «load.source.variable.varName(load)»
		«ELSE»
			«load.target.variable.print» = load «load.source.variable.type.doSwitch»* «load.source.variable.print»
		«ENDIF»
	'''
	
	override caseInstStore(InstStore store) '''
		«IF store.target.variable.type.list»
			«store.target.variable.varName(store)» = getelementptr «store.target.variable.type.doSwitch»* «store.target.variable.print», i32 0«store.indexes.join(", ", ", ", "", [printIndex])»
			store «(store.target.variable.type as TypeList).innermostType.doSwitch» «store.value.doSwitch», «(store.target.variable.type as TypeList).innermostType.doSwitch»* «store.target.variable.varName(store)»
		«ELSE»
			store «store.target.variable.type.doSwitch» «store.value.doSwitch», «store.target.variable.type.doSwitch»* «store.target.variable.print»
		«ENDIF»
	'''
	
	override caseInstReturn(InstReturn retInst) {
		if(retInst.value == null)
			'''ret void'''
		else
			'''ret «retInst.value.type.doSwitch» «retInst.value.doSwitch»'''
	}
	
	override print(Action action) '''
		define «action.scheduler.returnType.doSwitch» @«action.scheduler.name»(«action.scheduler.parameters.join(", ", [argumentDeclaration])») nounwind {
		entry:
			«FOR local : action.scheduler.locals»
				«local.variableDeclaration»
			«ENDFOR»
			«FOR port : action.peekPattern.ports.filter[ ! native]»
				«port.fifoVar(action.inputPattern.portToVarMap.get(port))»
			«ENDFOR»
			br label %b«action.scheduler.blocks.head.label»
			
		«FOR block : action.scheduler.blocks»
			«block.doSwitch»
		«ENDFOR»
		}
		
		define void @«action.body.name»(«action.body.parameters.join(", ", [argumentDeclaration])») nounwind {
		entry:
			«FOR local : action.body.locals»
				«local.variableDeclaration»
			«ENDFOR»
			«FOR port : action.inputPattern.ports.filter[ ! native]»
				«port.fifoVar(action.inputPattern.portToVarMap.get(port))»
			«ENDFOR»
			«FOR port : action.outputPattern.ports.filter[ ! native]»
				«port.fifoVar(action.outputPattern.portToVarMap.get(port))»
			«ENDFOR»
			br label %b«action.body.blocks.head.label»
		
		«FOR block : action.body.blocks»
			«block.doSwitch»
		«ENDFOR»
		}
	'''
	
	def actorParameter(Var variable)
		'''@«variable.name» = global «variable.type.doSwitch» undef'''

	
	def fifo(Port port) '''
		«port.fifoVarName» = global «port.type.doSwitch»* null
	'''
	
	def fifoVarName(Port port)
		'''@«port.name»_ptr'''
	
	def fifoVar(Port port, Var variable) '''
		%«variable.name»_ptr = load «port.type.doSwitch»** «port.fifoVarName»
		%«variable.name» = bitcast «port.type.doSwitch»* %«variable.name»_ptr to «variable.type.doSwitch»*
	'''

	def void computePatterns() {
		for (init : actor.initializes) {
			init.inputPattern.compute
			init.outputPattern.compute
			init.peekPattern.compute
		}
		for (action : actor.actions) {
			action.inputPattern.compute
			action.outputPattern.compute
			action.peekPattern.compute
		}
		if (actor.hasMoC) {
			val moc = actor.moC
			if(moc.SDF || moc.CSDF) {
				(moc as CSDFMoC).inputPattern.compute;
				(moc as CSDFMoC).outputPattern.compute;
			} else if (moc.quasiStatic) {
				for (action : (moc as QSDFMoC).actions) {
					((moc as QSDFMoC).getMoC(action) as CSDFMoC).inputPattern.compute;
					((moc as QSDFMoC).getMoC(action) as CSDFMoC).outputPattern.compute;
				}
			}
		}
	}
	
	def compute(Pattern pattern) {
		if( ! pattern.empty)
			patternList.add(pattern)
	}
	
	override computeCastedList() {
		castedList.clear
		for (variable : actor.eAllContents.toIterable.filter(typeof(Var))) {
			if(variable.type.list && ! variable.defs.empty && variable.defs.head.eContainer instanceof InstCast) {
				castedList.add(variable)
			}
		}
	}
}