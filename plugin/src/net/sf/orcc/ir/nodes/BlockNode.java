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
package net.sf.orcc.ir.nodes;

import java.util.ArrayList;
import java.util.List;

import net.sf.orcc.ir.CFGNode;
import net.sf.orcc.ir.Instruction;
import net.sf.orcc.ir.Location;

/**
 * This class defines a Block node. A block node is a node that contains
 * instructions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class BlockNode extends ArrayList<Instruction> implements
		Iterable<Instruction>, CFGNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static BlockNode first(List<CFGNode> nodes) {
		BlockNode block;
		if (nodes.isEmpty()) {
			block = new BlockNode();
			nodes.add(block);
		} else {
			CFGNode node = nodes.get(0);
			if (node instanceof BlockNode) {
				block = (BlockNode) node;
			} else {
				block = new BlockNode();
				nodes.add(0, block);
			}
		}

		return block;
	}

	public static BlockNode last(List<CFGNode> nodes) {
		BlockNode block;
		if (nodes.isEmpty()) {
			block = new BlockNode();
			nodes.add(block);
		} else {
			CFGNode node = nodes.get(nodes.size() - 1);
			if (node instanceof BlockNode) {
				block = (BlockNode) node;
			} else {
				block = new BlockNode();
				nodes.add(block);
			}
		}

		return block;
	}

	private Location location;
	
	private int label;

	private static int globalLabel;

	public BlockNode() {
		this(new Location());
	}

	public BlockNode(Location location) {
		super();
		this.label = globalLabel++;
		this.location = location;
	}

	@Override
	public Object accept(NodeVisitor visitor, Object... args) {
		return visitor.visit(this, args);
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Instruction instruction : this) {
			sb.append(instruction.toString());
			sb.append("\\n");
		}

		return sb.toString();
	}

	@Override
	public int getLabel() {
		return label;
	}

}
