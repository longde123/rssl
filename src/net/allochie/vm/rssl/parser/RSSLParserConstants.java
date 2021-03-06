/* Generated By:JavaCC: Do not edit this line. RSSLParserConstants.java */
package net.allochie.vm.rssl.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface RSSLParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int TYPE = 11;
  /** RegularExpression Id. */
  int EXTENDS = 12;
  /** RegularExpression Id. */
  int GLOBALS = 13;
  /** RegularExpression Id. */
  int ENDGLOBALS = 14;
  /** RegularExpression Id. */
  int CONSTANT = 15;
  /** RegularExpression Id. */
  int NATIVE = 16;
  /** RegularExpression Id. */
  int TAKES = 17;
  /** RegularExpression Id. */
  int RETURNS = 18;
  /** RegularExpression Id. */
  int NOTHING = 19;
  /** RegularExpression Id. */
  int FUNCTION = 20;
  /** RegularExpression Id. */
  int ENDFUNCTION = 21;
  /** RegularExpression Id. */
  int LOCAL = 22;
  /** RegularExpression Id. */
  int ARRAY = 23;
  /** RegularExpression Id. */
  int SET = 24;
  /** RegularExpression Id. */
  int CALL = 25;
  /** RegularExpression Id. */
  int IF = 26;
  /** RegularExpression Id. */
  int THEN = 27;
  /** RegularExpression Id. */
  int ELSE = 28;
  /** RegularExpression Id. */
  int ELSEIF = 29;
  /** RegularExpression Id. */
  int ENDIF = 30;
  /** RegularExpression Id. */
  int LOOP = 31;
  /** RegularExpression Id. */
  int ENDLOOP = 32;
  /** RegularExpression Id. */
  int EXITWHEN = 33;
  /** RegularExpression Id. */
  int RETURN = 34;
  /** RegularExpression Id. */
  int DEBUG = 35;
  /** RegularExpression Id. */
  int HANDLE = 36;
  /** RegularExpression Id. */
  int TRY = 37;
  /** RegularExpression Id. */
  int CATCH = 38;
  /** RegularExpression Id. */
  int ENDTRY = 39;
  /** RegularExpression Id. */
  int RAISE = 40;
  /** RegularExpression Id. */
  int DECIMALINT = 62;
  /** RegularExpression Id. */
  int OCTALINT = 63;
  /** RegularExpression Id. */
  int HEXINT = 64;
  /** RegularExpression Id. */
  int FOURCCINT = 65;
  /** RegularExpression Id. */
  int REALCONST = 66;
  /** RegularExpression Id. */
  int STRING_LITERAL = 69;
  /** RegularExpression Id. */
  int IDENTIFIER = 75;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int WithinComment0 = 1;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\\n\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"//\"",
    "\"\\r\\n\"",
    "\"\\n\"",
    "\"\\r\"",
    "<token of kind 10>",
    "\"type\"",
    "\"extends\"",
    "\"globals\"",
    "\"endglobals\"",
    "\"constant\"",
    "\"native\"",
    "\"takes\"",
    "\"returns\"",
    "\"nothing\"",
    "\"function\"",
    "\"endfunction\"",
    "\"local\"",
    "\"array\"",
    "\"set\"",
    "\"call\"",
    "\"if\"",
    "\"then\"",
    "\"else\"",
    "\"elseif\"",
    "\"endif\"",
    "\"loop\"",
    "\"endloop\"",
    "\"exitwhen\"",
    "\"return\"",
    "\"debug\"",
    "\"handle\"",
    "\"try\"",
    "\"catch\"",
    "\"endtry\"",
    "\"raise\"",
    "\"=\"",
    "\"[]\"",
    "\",\"",
    "\"[\"",
    "\"]\"",
    "\"(\"",
    "\")\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"==\"",
    "\"!=\"",
    "\">\"",
    "\"<\"",
    "\">=\"",
    "\"<=\"",
    "\"and\"",
    "\"or\"",
    "\"not\"",
    "\"null\"",
    "<DECIMALINT>",
    "<OCTALINT>",
    "<HEXINT>",
    "<FOURCCINT>",
    "<REALCONST>",
    "\"true\"",
    "\"false\"",
    "<STRING_LITERAL>",
    "\"code\"",
    "\"integer\"",
    "\"real\"",
    "\"boolean\"",
    "\"string\"",
    "<IDENTIFIER>",
  };

}
