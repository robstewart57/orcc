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
package net.sf.orcc.backends.llvm.aot;

import java.util.HashMap;
import java.util.Map;

import net.sf.orcc.backends.ir.InstCast;
import net.sf.orcc.df.Actor;
import net.sf.orcc.df.Pattern;
import net.sf.orcc.df.Port;
import net.sf.orcc.df.State;
import net.sf.orcc.ir.Var;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

/**
 * This class computes a map for inserting metadata information into LLVM
 * template.
 * 
 * @author Jerome GORIN
 * @author Herve Yviquel
 * 
 */
public class LLVMTemplateData {

	private Map<Var, Var> castedListReferences;

	private Map<Pattern, Map<Port, Integer>> portToIndexByPatternMap;

	private Map<State, Integer> stateToLabelMap;

	public LLVMTemplateData(Actor actor) {
		portToIndexByPatternMap = new HashMap<Pattern, Map<Port, Integer>>();

		castedListReferences = new HashMap<Var, Var>();

		stateToLabelMap = new HashMap<State, Integer>();

		computeTemplateMaps(actor);
	}

	private void computeCastedListReferences(Actor actor) {
		TreeIterator<EObject> it = actor.eAllContents();
		while (it.hasNext()) {
			EObject object = it.next();
			if (object instanceof Var) {
				Var var = (Var) object;
				if (var.getType().isList()
						&& !var.getDefs().isEmpty()
						&& (var.getDefs().get(0).eContainer() instanceof InstCast)) {
					castedListReferences.put(var, var);
				}
			}
		}
	}

	private void computePortToIndexByPatternMap(Actor actor) {
		TreeIterator<EObject> it = actor.eAllContents();
		while (it.hasNext()) {
			EObject object = it.next();
			if (object instanceof Pattern) {
				Pattern pattern = (Pattern) object;
				Map<Port, Integer> portToIndexMap = new HashMap<Port, Integer>();
				for (int i = 0; i < pattern.getPorts().size(); i++) {
					portToIndexMap.put(pattern.getPorts().get(i), i + 1);
				}
				portToIndexByPatternMap.put(pattern, portToIndexMap);
			}
		}
	}

	private void computeStateToLabelMap(Actor actor) {
		if (actor.hasFsm()) {
			for (int i = 0; i < actor.getFsm().getStates().size(); i++) {
				stateToLabelMap.put(actor.getFsm().getStates().get(i), i);
			}
		}
	}

	protected void computeTemplateMaps(Actor actor) {
		computeCastedListReferences(actor);
		computeStateToLabelMap(actor);
		computePortToIndexByPatternMap(actor);
	}

	public Map<Var, Var> getCastedListReferences() {
		return castedListReferences;
	}

	public Map<Pattern, Map<Port, Integer>> getPortToIndexByPatternMap() {
		return portToIndexByPatternMap;
	}

	public Map<State, Integer> getStateToLabelMap() {
		return stateToLabelMap;
	}

}
