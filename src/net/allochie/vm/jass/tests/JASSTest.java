package net.allochie.vm.jass.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.statement.ConditionalStatement;
import net.allochie.vm.jass.ast.statement.LoopStatement;
import net.allochie.vm.jass.parser.JASSParser;
import net.allochie.vm.jass.parser.ParseException;

public class JASSTest {

	public static void main(String[] args) {
		try {
			JASSParser parse = new JASSParser(new FileInputStream("rt.jass"));
			JASSFile file = parse.file();

			for (Dec dec : file.decs)
				System.out.println("decl: " + dec);
			for (Function func : file.funcs) {
				System.out.println("funcdef: " + func);
				listStatements(func.statements, "  ");

			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void listStatements(StatementList list, String prefix) {
		for (Statement s : list) {
			System.out.println(prefix + s);
			if (s instanceof LoopStatement)
				listStatements(((LoopStatement) s).statements, prefix + "  ");
			if (s instanceof ConditionalStatement) 
				listStatements(((ConditionalStatement) s).statements, prefix + "  ");
		}
	}
}
