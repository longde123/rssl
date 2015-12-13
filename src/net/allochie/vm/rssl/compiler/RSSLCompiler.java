package net.allochie.vm.rssl.compiler;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.ast.dec.Dec;

public class RSSLCompiler {

	public void compile(RSSLFile file) throws RSSLCompilerException {
		for (Dec dec : file.decs)
			compileDec(file, dec);

		for (Function function : file.funcs)
			compileSymbol(file, function);
	}

	private void compileDec(RSSLFile file, Dec dec) throws RSSLCompilerException {
		System.out.println("compileDec: " + dec.toString());
	}

	private void compileSymbol(RSSLFile file, Function function) throws RSSLCompilerException {
		System.out.println("compileSymbol: " + function.toString());
	}

}
