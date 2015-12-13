package rssl.tests.compiler;

import java.io.FileInputStream;

import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.compiler.RSSLCompiler;
import net.allochie.vm.rssl.parser.RSSLParser;

public class RSSLCompilerTest {
	public static void main(String[] args) {
		try {
			RSSLParser parse = new RSSLParser(new FileInputStream("rt.master.jass"));
			RSSLFile file = parse.file();

			RSSLCompiler compiler = new RSSLCompiler();
			compiler.compile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
