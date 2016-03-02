package net.allochie.vm.rssl.compiler;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.dec.Dec;
import net.allochie.vm.rssl.ast.dec.GlobalsDec;
import net.allochie.vm.rssl.ast.dec.NativeFuncDef;
import net.allochie.vm.rssl.ast.dec.TypeDec;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.compiler.analysis.CFFunction;
import net.allochie.vm.rssl.compiler.analysis.CFGraphWriter;

public class RSSLCompiler {

	/** List of all known VM types */
	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();

	/** List of all global variables */
	public HashMap<String, VarDec> globals = new HashMap<String, VarDec>();

	/** List of all native functions */
	public HashMap<String, NativeFuncDef> natives = new HashMap<String, NativeFuncDef>();

	/** List of all real functions */
	public HashMap<String, Function> funcs = new HashMap<String, Function>();

	public CFGraphWriter writer = new CFGraphWriter();

	public void compile(RSSLFile file) throws RSSLCompilerException {
		types.put("code", genSystemType("code"));
		types.put("handle", genSystemType("handle"));
		types.put("integer", genSystemType("integer"));
		types.put("real", genSystemType("real"));
		types.put("boolean", genSystemType("boolean"));
		types.put("string", genSystemType("string"));
		types.put("nothing", genSystemType("nothing"));

		for (Dec dec : file.decs)
			try {
				compileDec(file, dec);
			} catch (RSSLCompilerException e) {
				throw new RSSLCompilerException("Unable to compile " + dec, e);
			}

		for (Function function : file.funcs)
			try {
				compileSymbol(file, function);
			} catch (RSSLCompilerException e) {
				throw new RSSLCompilerException("Unable to compile " + function, e);
			}
	}

	private TypeDec genSystemType(String typename) {
		TypeDec me = new TypeDec();
		me.id = Identifier.fromString(typename);
		me.where = new CodePlace("<<system>>", 0, 0);
		return me;
	}

	private void compileDec(RSSLFile file, Dec dec) throws RSSLCompilerException {
		// System.out.println("compileDec: " + dec.toString());
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
		// System.out.println("compileSymbol: " + function.toString());
		compileFunction((Function) function);
	}

	private boolean existsInTypeDict(Type thattype) {
		return this.types.containsKey(thattype.typename);
	}

	private boolean existsInGlobals(String name) {
		return this.globals.containsKey(name);
	}

	private void checkExistsInTypeDict(Type thattype) throws RSSLCompilerException {
		if (!existsInTypeDict(thattype))
			throw new RSSLCompilerException("Unknown type " + thattype.typename);
	}

	private void checkNotExistsInTypeDict(Type thattype) throws RSSLCompilerException {
		if (existsInTypeDict(thattype))
			throw new RSSLCompilerException("Known type " + thattype.typename);
	}

	private void checkAllExistsInTypeDict(Type[] types) throws RSSLCompilerException {
		for (int i = 0; i < types.length; i++)
			if (!existsInTypeDict(types[i]))
				throw new RSSLCompilerException("Unknown type " + types[i].typename + " at slot " + i);
	}

	private void checkExistsInGlobals(String name, Type type) throws RSSLCompilerException {
		if (!existsInGlobals(name))
			throw new RSSLCompilerException("Unknown global " + name);
		if (type != null) {
			if (!globals.get(name).type.equals(type))
				throw new RSSLCompilerException("Type mismatch for global " + name);
		}
	}

	private void checkNotExistsInGlobals(String name, Type type) throws RSSLCompilerException {
		if (existsInGlobals(name))
			throw new RSSLCompilerException("Known global " + name);
	}

	private void compileNativeFunction(NativeFuncDef dec) throws RSSLCompilerException {
		// TODO Auto-generated method stub
		checkAllExistsInTypeDict(dec.def.params.types());
		checkExistsInTypeDict(dec.def.returns.type);
	}

	private void compileGlobalBlock(GlobalsDec dec) throws RSSLCompilerException {
		for (VarDec var : dec.decs)
			compileVarDec(var);
	}

	private void compileVarDec(VarDec var) throws RSSLCompilerException {
		checkNotExistsInGlobals(var.name.image, null);
		globals.put(var.name.image, var);
	}

	private void compileType(TypeDec dec) throws RSSLCompilerException {
		// TODO Auto-generated method stub
		checkNotExistsInTypeDict(Type.fromIdentifier(dec.id, dec.where));
		types.put(dec.id.image, dec);
	}

	private void compileFunction(Function function) throws RSSLCompilerException {
		// TODO Auto-generated method stub

		CFFunction f0 = writer.writeNodesFromFunc(function);
		f0.dump();
	}

}
