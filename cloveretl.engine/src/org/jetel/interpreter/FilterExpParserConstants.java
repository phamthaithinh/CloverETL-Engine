/* Generated By:JJTree&JavaCC: Do not edit this line. FilterExpParserConstants.java */
package org.jetel.interpreter;

public interface FilterExpParserConstants {

  int EOF = 0;
  int OR = 6;
  int AND = 7;
  int NOT = 8;
  int INTEGER_LITERAL = 9;
  int DECIMAL_LITERAL = 10;
  int HEX_LITERAL = 11;
  int OCTAL_LITERAL = 12;
  int FLOATING_POINT_LITERAL = 13;
  int EXPONENT = 14;
  int CHARACTER_LITERAL = 15;
  int STRING_LITERAL = 16;
  int STRING_ARRAY = 17;
  int INTEGER_ARRAY = 18;
  int FLOATING_POINT_ARRAY = 19;
  int EQUAL = 20;
  int NON_EQUAL = 21;
  int LESS_THAN = 22;
  int LESS_THAN_EQUAL = 23;
  int GREATER_THAN = 24;
  int GREATER_THAN_EQUAL = 25;
  int REGEX_EQUAL = 26;
  int OPERATOR = 27;
  int FIELD_ID = 28;
  int OPEN_PAR = 29;
  int CLOSE_PAR = 30;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\n\\r\"",
    "<OR>",
    "<AND>",
    "<NOT>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "<STRING_ARRAY>",
    "<INTEGER_ARRAY>",
    "<FLOATING_POINT_ARRAY>",
    "\"==\"",
    "<NON_EQUAL>",
    "\"<\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"~=\"",
    "<OPERATOR>",
    "<FIELD_ID>",
    "\"(\"",
    "\")\"",
  };

}
