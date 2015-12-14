package net.allochie.vm.rssl.compiler;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.dec.Dec;
import net.allochie.vm.rssl.ast.dec.GlobalsDec;
import net.allochie.vm.rssl.ast.dec.NativeFuncDef;
import net.allochie.vm.rssl.ast.dec.TypeDec;
import net.allochie.vm.rssl.ast.dec.VarDec;

public class RSSLCompiler {

	/** List of all known VM types */
	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();

	/** List of all global variables */
	public HashMap<String, VarDec> globals = new HashMap<String, VarDec>();

	/** List of all native functions */
	public HashMap<String, NativeFuncDef> natives = new HashMap<String, NativeFuncDef>();

	/** List of all real functions */
	public HashMap<String, Function> funcs = new HashMap<String, Function>();

	public void compile(RSSLFile file) throws RSSLCompilerException {
		for (Dec dec : file.decs)
			compileDec(file, dec);

		for (Function function : file.funcs)
			compileSymbol(file, function);
	}

	private void compileDec(RSSLFile file, Dec dec) throws RSSLCompilerException {
		System.out.println("compileDec: " + dec.toString());
		if (dec instanceof TypeDec) {
			compileType((TypeDec) dec);
		} else if (dec instanceof GlobalsDec) {
			compileGlobalBlock((GlobalsDec) dec);
		} else if (dec instanceof NativeFuncDef) {
			compileNativeFunction((NativeFuncDef) dec);
		} else {
			System.out.println(">> unsupported declaration: " + dec.getClass().getName());
		}
	}

	private void compileSymbol(RSSLFile file, Function function) throws RSSLCompilerException {
		System.out.println("compileSymbol: " + function.toString());
		compileFunction((Function) function);
	}

	private boolean existsInTypeDict(Type thattype) {
		return this.types.containsKey(thattype.typename);
	}

	private void checkExistsInTypeDict(Type thattype) throws RSSLCompilerException {
		if (!existsInTypeDict(thattype))
			throw new RSSLCompilerException("Unknown type " + thattype.typename);
	}

	private void checkAllExistsInTypeDict(Type[] types) throws RSSLCompilerException {
		for (int i = 0; i < types.length; i++)
			if (!existsInTypeDict(types[i]))
				throw new RSSLCompilerException("Unknown type " + types[i].typename + " at slot " + i);
	}

	private void compileNativeFunction(NativeFuncDef dec) throws RSSLCompilerException {
		// TODO Auto-generated method stub

		checkAllExistsInTypeDict(dec.def.params.types());
		checkExistsInTypeDict(dec.def.returns);
	}

	private void compileGlobalBlock(GlobalsDec dec) throws RSSLCompilerException {
		// TODO Auto-generated method stub

	}

	private void compileType(TypeDec dec) throws RSSLCompilerException {
		// TODO Auto-generated method stub

	}

	private void compileFunction(Function function) throws RSSLCompilerException {
		// TODO Auto-generated method stub

	}

}
