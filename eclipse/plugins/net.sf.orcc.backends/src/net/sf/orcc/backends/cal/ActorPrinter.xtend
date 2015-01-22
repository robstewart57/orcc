/* 
 * XRONOS, High Level Synthesis of Streaming Applications
 * 
 * Copyright (C) 2014 EPFL SCI STI MM
 *
 * This file is part of XRONOS.
 *
 * XRONOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XRONOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XRONOS.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse (or a modified version of Eclipse or an Eclipse plugin or 
 * an Eclipse library), containing parts covered by the terms of the 
 * Eclipse Public License (EPL), the licensors of this Program grant you 
 * additional permission to convey the resulting work.  Corresponding Source 
 * for a non-source form of such a combination shall include the source code 
 * for the parts of Eclipse libraries used as well as that of the  covered work.
 * 
 */
package net.sf.orcc.backends.cal

import net.sf.orcc.backends.CommonPrinter
import net.sf.orcc.backends.ir.InstCast
import net.sf.orcc.df.Action
import net.sf.orcc.df.Actor
import net.sf.orcc.df.Pattern
import net.sf.orcc.df.Port
import net.sf.orcc.ir.BlockBasic
import net.sf.orcc.ir.InstAssign
import net.sf.orcc.ir.InstLoad
import net.sf.orcc.ir.InstStore
import net.sf.orcc.ir.Procedure
import net.sf.orcc.ir.TypeBool
import net.sf.orcc.ir.TypeFloat
import net.sf.orcc.ir.TypeInt
import net.sf.orcc.ir.TypeList
import net.sf.orcc.ir.TypeString
import net.sf.orcc.ir.TypeUint
import net.sf.orcc.ir.Var
import net.sf.orcc.util.FilesManager
import org.eclipse.emf.common.util.EMap
import org.eclipse.emf.ecore.EObject

class ActorPrinter extends CommonPrinter {

	protected Actor actor

	private EMap<Var, Port> inVarToPort;
	// private EMap<Port, Var> inPortToVarMap;
	private EMap<Var, Port> outVarToPort;

	private String actorPackage

	new(Actor actor, String actorPackage) {
		this.actor = actor
		this.actorPackage = actorPackage
	}

	def printActor(String targetFolder) {
		val content = compileActor
		FilesManager.writeFile(content, targetFolder, actor.simpleName + ".cal")
	}

	def compileActor() '''
		package «actorPackage»;
		
		actor «actor.simpleName»(«actorParameters»)
			«actorInputs»
				==>
					«actorOutputs»
		
			«stateVars»
			
			«FOR action : actor.actions»
			«actorAction(action)»
			«ENDFOR»
		
		end
	'''

    // TODO actor parameters
	def actorParameters() '''
		'''

	def actorInputs() '''
		«FOR port : actor.inputs SEPARATOR ", "»
			«actorPort(port)»
		«ENDFOR»
	'''

	def actorPort(Port port) '''
		«port.type.doSwitch»  «port.name»
	'''

	def actorOutputs() '''
		«FOR port : actor.outputs SEPARATOR ", " AFTER ":"»
			«actorPort(port)»
		«ENDFOR»
	'''

	// TODO State Variables
	def stateVars() '''
		'''

	// Actions
	def actorAction(Action action) {
		inVarToPort = action.inputPattern.varToPortMap
		outVarToPort = action.outputPattern.varToPortMap
		'''
			«action.name» : action 
								«actionPorts(action.inputPattern)» 
									==> 
										«actionPorts(action.outputPattern)»
			«IF !action.body.locals.empty»«actionLocals(action.body)»«ENDIF»
			do
				«actionBody(action.body)»
			end
		'''
	}

    // Actually it is not difficult to modify the name variables. You need to visit the input patterns with the DfVisitor for each actor
	//
	// have a look on how to visit patterns on file XronosScheduler line 144
	def actionPorts(Pattern pattern) '''
		«FOR port : pattern.ports SEPARATOR ", "»
			«actionPort(port, pattern.portToVarMap.get(port))»
		«ENDFOR»
	'''

	def actionPort(Port port, Var portVar) '''«port.name»:[«portVar.name»]'''

    // TODO action guards
    // * guard expression
    // * action expression
	def actionGuard(Action action) '''
		'''

	def actionLocals(Procedure procedure) '''
		var
			«FOR local : procedure.locals SEPARATOR ", "»
				«local.type.doSwitch» «local.name»
			«ENDFOR»
	'''

	def actionBody(Procedure proc) '''
		«proc.doSwitch»
	'''

	override caseBlockBasic(BlockBasic block) '''
		«FOR instr : block.instructions»
			«instr.doSwitch»
		«ENDFOR»
	'''

	override defaultCase(EObject object) '''
		«IF object instanceof InstCast»
			«caseInstCast(object as InstCast)»
		«ENDIF»
	'''

	override caseInstLoad(InstLoad inst) '''
		«inst.target.variable.name» := «inst.source.variable.name»«IF !isSinglePortToken(inst.source.variable)»«FOR index : inst.
			indexes»[«index.doSwitch»]«ENDFOR»«ENDIF»;
	'''

	def isSinglePortToken(Var variable) {
		if (inVarToPort.containsKey(variable) || outVarToPort.containsKey(variable))
			true
		else
			false
	}

	override caseInstStore(InstStore inst) '''
		«inst.target.variable.name»«IF !isSinglePortToken(inst.target.variable)»«FOR index : inst.indexes»[«index.doSwitch»]«ENDFOR»«ENDIF» := «inst.
			value.doSwitch»;
	'''

	override caseInstAssign(InstAssign inst) '''
		«inst.target.variable.name» := «inst.value.doSwitch»;
	'''

	def caseInstCast(InstCast inst) '''
		«inst.target.variable.name» := «inst.source.variable.name»;
	'''

	// CAL types
	override caseTypeBool(TypeBool type) {
		'''bool'''
	}

	override caseTypeFloat(TypeFloat type) {
		'''float'''
	}

	override caseTypeInt(TypeInt type) {
		'''int(size=«type.size»)'''
	}

	override caseTypeUint(TypeUint type) {
		'''uint(size=«type.size»)'''
	}

	override caseTypeString(TypeString type) {
		"String"
	}

	override caseTypeList(TypeList type) {
		'''«IF type.size == 1»«type.type.doSwitch»«ELSE»List(type:«type.type.doSwitch», size=«type.size»)«ENDIF»'''
	}

}
