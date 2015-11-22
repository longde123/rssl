package net.allochie.vm.rssl.compiler;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.RSSLFile;

public class RSSLCompiler {

	public void compile(RSSLFile file) throws RSSLCompilerException {

		for (Function function : file.funcs)
			compileSymbol(file, function);
	}

	private void compileSymbol(RSSLFile file, Function function) {

	}

}
