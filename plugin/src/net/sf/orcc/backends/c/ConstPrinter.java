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
package net.sf.orcc.backends.c;

import java.util.List;

import net.sf.orcc.ir.consts.AbstractConst;
import net.sf.orcc.ir.consts.BoolConst;
import net.sf.orcc.ir.consts.ConstVisitor;
import net.sf.orcc.ir.consts.IntConst;
import net.sf.orcc.ir.consts.ListConst;
import net.sf.orcc.ir.consts.StringConst;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * Sets the "value" attribute of the given top-level template to the value of
 * the constant visited. If it is a list, uses the "listValue" template.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ConstPrinter implements ConstVisitor {

	/**
	 * template group
	 */
	private StringTemplateGroup group;

	/**
	 * current template
	 */
	protected StringTemplate template;

	/**
	 * Creates a new const printer from the given template group.
	 * 
	 * @param group
	 *            template group
	 */
	public ConstPrinter(StringTemplateGroup group) {
		this.group = group;
	}

	/**
	 * Sets the top-level template.
	 * 
	 * @param template
	 *            top-level template
	 */
	public void setTemplate(StringTemplate template) {
		this.template = template;
	}

	@Override
	public void visit(BoolConst constant) {
		template.setAttribute("value", constant.getValue() ? "1" : "0");
	}

	@Override
	public void visit(IntConst constant) {
		template.setAttribute("value", constant.getValue());
	}

	@Override
	public void visit(ListConst constant) {
		// save current template
		StringTemplate previousTempl = template;

		// set instance of list template as current template
		StringTemplate listTempl = group.getInstanceOf("listValue");
		template = listTempl;

		List<AbstractConst> list = constant.getValue();
		for (AbstractConst cst : list) {
			cst.accept(this);
		}

		// restore previous template as current template, and set attribute
		// "value" to the instance of the list template
		template = previousTempl;
		template.setAttribute("value", listTempl);
	}

	@Override
	public void visit(StringConst constant) {
		// escape backslashes
		String val = constant.getValue();
		String res = "\"" + val.replaceAll("\\\\", "\\\\\\\\") + "\"";
		template.setAttribute("value", res);
	}

}
