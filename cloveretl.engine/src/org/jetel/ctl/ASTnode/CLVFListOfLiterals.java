/* Generated By:JJTree: Do not edit this line. CLVFListOfLiterals.java */

package org.jetel.ctl.ASTnode;

import java.util.List;

import org.jetel.ctl.TransformLangParser;
import org.jetel.ctl.TransformLangParserVisitor;

public class CLVFListOfLiterals extends SimpleNode {

	private List<Object> value;
	
	public CLVFListOfLiterals(int id) {
		super(id);
	}

	public CLVFListOfLiterals(TransformLangParser p, int id) {
		super(p, id);
	}

	public CLVFListOfLiterals(CLVFListOfLiterals node) {
		super(node);
	}

	/** Accept the visitor. * */
	public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public SimpleNode duplicate() {
		return new CLVFListOfLiterals(this);
	}

	public List<Object> getValue() {
		return value;
	}
	
	public void setValue(List<Object> value) {
		this.value = value;
	}

	
}
