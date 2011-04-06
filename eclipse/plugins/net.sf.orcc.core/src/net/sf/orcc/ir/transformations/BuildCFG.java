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
package net.sf.orcc.ir.transformations;

import java.util.List;

import net.sf.orcc.ir.CFG;
import net.sf.orcc.ir.Node;
import net.sf.orcc.ir.NodeBlock;
import net.sf.orcc.ir.NodeIf;
import net.sf.orcc.ir.NodeWhile;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.impl.NodeInterpreter;
import net.sf.orcc.ir.util.AbstractActorVisitor;

/**
 * This class defines a transformation to build the CFG of procedures.
 * 
 * @author Matthieu Wipliez
 * @author Jerome Gorin
 * 
 */
public class BuildCFG extends AbstractActorVisitor {

	/**
	 * This class defines a CFG builder.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class CFGBuilder implements NodeInterpreter {

		/**
		 * Creates a new CFG builder.
		 */
		public CFGBuilder() {
			graph = new CFG();
		}

		@Override
		public Object interpret(NodeBlock node, Object... args) {
			Node previous = (Node) args[0];
			graph.addVertex(node);
			if (previous != null) {
				graph.addEdge(previous, node);
			}

			return node;
		}

		@Override
		public Object interpret(NodeIf node, Object... args) {
			Node previous = (Node) args[0];
			Node last;
			graph.addVertex(node);
			if (previous != null) {
				graph.addEdge(previous, node);
			}

			Node join = node.getJoinNode();
			graph.addVertex(join);

			if (node.getThenNodes().isEmpty()) {
				graph.addEdge(previous, join);
			} else {
				last = (Node) visit(node.getThenNodes(), node);
				graph.addEdge(last, join);
			}

			if (node.getElseNodes().isEmpty()) {
				graph.addEdge(previous, join);
			} else {
				last = (Node) visit(node.getElseNodes(), node);
				graph.addEdge(last, join);
			}

			return join;
		}

		@Override
		public Object interpret(NodeWhile node, Object... args) {
			Node previous = (Node) args[0];

			Node join = node.getJoinNode();
			graph.addVertex(join);

			if (previous != null) {
				graph.addEdge(previous, join);
			}

			graph.addVertex(node);
			graph.addEdge(join, node);

			Node last = (Node) visit(node.getNodes(), join);
			graph.addEdge(last, join);

			return node;
		}

		/**
		 * Visits the given node list.
		 * 
		 * @param nodes
		 *            a list of nodes
		 * @param previous
		 *            the previous node, or <code>null</code> if there is none
		 * @return the last node of the node list
		 */
		public Object visit(List<Node> nodes, Node previous) {
			Object last = previous;
			for (Node node : nodes) {
				last = node.accept(this, last);
			}

			return last;
		}

	}

	private CFG graph;

	@Override
	public void visit(Procedure procedure) {
		CFGBuilder builder = new CFGBuilder();
		builder.visit(procedure.getNodes(), null);
		procedure.setGraph(graph);
	}

}
