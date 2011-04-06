/*
 * Copyright (c) 2009, IETR/INSA of Rennes
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
package net.sf.orcc.backends.llvm.transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.sf.orcc.backends.llvm.instructions.GEP;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.Instruction;
import net.sf.orcc.ir.VarLocal;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.TypeList;
import net.sf.orcc.ir.Use;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.instructions.Load;
import net.sf.orcc.ir.instructions.Store;
import net.sf.orcc.ir.util.AbstractActorVisitor;

/**
 * Add GetElementPtr instructions in actor IR.
 * 
 * 
 * @author Jerome Gorin
 * 
 */
public class AddGEPTransformation extends AbstractActorVisitor {

	private String file;

	private Var addGEP(Var array, Type type,
			List<Expression> indexes, ListIterator<Instruction> it) {
		List<Expression> GepIndexes = new ArrayList<Expression>(indexes);

		// Make a new localVariable that will contains the elt to access
		VarLocal eltVar = procedure.newTempLocalVariable(file, type,
				array.getName() + "_" + "elt");

		GEP gepInstr = new GEP(eltVar, new Use(array), GepIndexes);

		it.add(gepInstr);

		return eltVar;
	}

	private void removeIndexes(Instruction instr, List<Expression> indexes) {
		Use.removeUses(instr, indexes);
		indexes.clear();
	}

	@Override
	public void visit(Actor actor) {
		this.file = actor.getFile();
		super.visit(actor);
	}

	@Override
	public void visit(Load load) {
		Use source = load.getSource();
		VarLocal target = load.getTarget();
		List<Expression> indexes = load.getIndexes();

		if (!indexes.isEmpty()) {
			itInstruction.previous();

			Var newSource = addGEP(source.getVariable(), target.getType(),
					indexes, itInstruction);

			load.setSource(new Use(newSource));
			removeIndexes(load, indexes);

			itInstruction.next();
		}

	}

	@Override
	public void visit(Store store) {
		Var target = store.getTarget();
		List<Expression> indexes = store.getIndexes();

		if (!indexes.isEmpty()) {
			itInstruction.previous();
			TypeList typeList = (TypeList) target.getType();

			Var newTarget = addGEP(target, typeList.getElementType(),
					indexes, itInstruction);

			store.setTarget(newTarget);
			removeIndexes(store, indexes);

			itInstruction.next();
		}
	}

}
