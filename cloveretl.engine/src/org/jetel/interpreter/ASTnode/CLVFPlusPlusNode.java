/* Generated By:JJTree: Do not edit this line. CLVFPlusPlusNode.java */

package org.jetel.interpreter.ASTnode;
import org.jetel.interpreter.ExpParser;
import org.jetel.interpreter.TransformLangParserVisitor;
public class CLVFPlusPlusNode extends SimpleNode {
    public boolean prefix;
    public boolean statement;
    
  public CLVFPlusPlusNode(int id) {
    super(id);
  }

  public CLVFPlusPlusNode(ExpParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public void setPrefix(boolean _prefix) {
      this.prefix=_prefix;
  }
  
  public void setStatementExpr(boolean _stm) {
      this.statement=_stm;
  }
  
}
