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

	def public getFileContent() {

		var int numInputs = actor.getInputs().size()
		var int numOutputs = actor.getOutputs().size()
		packageDir = actor.package.replaceAll("\\.", "/") + "/"

'''
process blur [in1:in int, out1:out int] is
'''
		
		}
		}