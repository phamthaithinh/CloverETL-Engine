/* Generated By:JJTree: Do not edit this line. CLVFIsNullNode.java */

package org.jetel.interpreter.ASTnode;
import org.jetel.interpreter.ExpParser;
import org.jetel.interpreter.TransformLangParserVisitor;

public class CLVFIsNullNode extends SimpleNode {
  public CLVFIsNullNode(int id) {
    super(id);
  }

  public CLVFIsNullNode(ExpParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
