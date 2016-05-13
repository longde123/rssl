package net.allochie.vm.rssl.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.ast.Param;
import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.dec.Dec;
import net.allochie.vm.rssl.ast.dec.GlobalsDec;
import net.allochie.vm.rssl.ast.dec.NativeFuncDef;
import net.allochie.vm.rssl.ast.dec.TypeDec;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.ast.expression.ArrayReferenceExpression;
import net.allochie.vm.rssl.ast.expression.BinaryOpExpression;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.ast.expression.FunctionCallExpression;
import net.allochie.vm.rssl.ast.expression.FunctionReferenceExpression;
import net.allochie.vm.rssl.ast.expression.IdentifierReference;
import net.allochie.vm.rssl.ast.expression.ParenExpression;
import net.allochie.vm.rssl.ast.expression.UnaryOpExpression;
import net.allochie.vm.rssl.ast.statement.CallStatement;
import net.allochie.vm.rssl.ast.statement.ConditionalStatement;
import net.allochie.vm.rssl.ast.statement.LoopExitStatement;
import net.allochie.vm.rssl.ast.statement.LoopStatement;
import net.allochie.vm.rssl.ast.statement.RaiseStatement;
import net.allochie.vm.rssl.ast.statement.ReturnStatement;
import net.allochie.vm.rssl.ast.statement.SetArrayStatement;
import net.allochie.vm.rssl.ast.statement.SetStatement;
import net.allochie.vm.rssl.ast.statement.TryCatchStatement;
import net.allochie.vm.rssl.compiler.analysis.CFClosure;
import net.allochie.vm.rssl.compiler.analysis.CFFunction;
import net.allochie.vm.rssl.compiler.analysis.CFGraphNode;
import net.allochie.vm.rssl.compiler.analysis.CFGraphNodeFlag;
import net.allochie.vm.rssl.compiler.analysis.CFGraphWriter;
import net.allochie.vm.rssl.compiler.analysis.ExpressionResolution;
import net.allochie.vm.rssl.compiler.analysis.ExpressionResolver;

public class RSSLCompiler {

	/** List of all known VM types */
	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();

	/** List of the system types */
	public HashMap<String, TypeDec> sysTypes = new HashMap<String, TypeDec>();

	/** List of all global variables */
	public HashMap<String, VarDec> globals = new HashMap<String, VarDec>();

	/** List of all native functions */
	public HashMap<String, NativeFuncDef> natives = new HashMap<String, NativeFuncDef>();

	/** List of all real functions */
	public HashMap<String, CFFunction> funcs = new HashMap<String, CFFunction>();

	public CFGraphWriter writer = new CFGraphWriter();

	public void compile(RSSLFile file) throws RSSLCompilerException {
		sysTypes.put("code", genSystemType("code"));
		sysTypes.put("handle", genSystemType("handle"));
		sysTypes.put("integer", genSystemType("integer"));
		sysTypes.put("real", genSystemType("real"));
		sysTypes.put("boolean", genSystemType("boolean"));
		sysTypes.put("string", genSystemType("string"));
		sysTypes.put("nothing", genSystemType("nothing"));
		types.putAll(sysTypes);

		for (Dec dec : file.decs)
			try {
				compileDec(dec);
			} catch (RSSLCompilerException e) {
				throw new RSSLCompilerException("Unable to compile " + dec, e);
			}

		for (Function function : file.funcs)
			try {
				compileFunction(function);
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

	private void compileDec(Dec dec) throws RSSLCompilerException {
		if (dec instanceof TypeDec) {
			compileType((TypeDec) dec);
		} else if (dec instanceof GlobalsDec) {
			compileGlobalBlock((GlobalsDec) dec);
		} else if (dec instanceof NativeFuncDef) {
			compileNativeFunction((NativeFuncDef) dec);
		} else {
			throw new RSSLCompilerException("Unsupported declaration " + dec.getClass().getName());
		}
	}

	public CFFunction getFunction(String image) {
		return funcs.get(image);
	}

	public boolean existsInTypeDict(Type thattype) {
		return this.types.containsKey(thattype.typename);
	}

	public boolean existsInGlobals(String name) {
		return this.globals.containsKey(name);
	}

	public boolean functionExists(String name) {
		return this.funcs.containsKey(name);
	}

	public boolean nativeExists(String name) {
		return this.natives.containsKey(name);
	}

	public void checkExistsInTypeDict(Type thattype) throws RSSLCompilerException {
		if (!existsInTypeDict(thattype))
			throw new RSSLCompilerException("Unknown type " + thattype.typename);
	}

	public void checkNotExistsInTypeDict(Type thattype) throws RSSLCompilerException {
		if (existsInTypeDict(thattype))
			throw new RSSLCompilerException("Known type " + thattype.typename);
	}

	public void checkAllExistsInTypeDict(Type[] types) throws RSSLCompilerException {
		for (int i = 0; i < types.length; i++)
			if (!existsInTypeDict(types[i]))
				throw new RSSLCompilerException("Unknown type " + types[i].typename + " at slot " + i);
	}

	public void checkExistsInGlobals(String name, Type type) throws RSSLCompilerException {
		if (!existsInGlobals(name))
			throw new RSSLCompilerException("Unknown global " + name);
		if (type != null) {
			if (!globals.get(name).type.equals(type))
				throw new RSSLCompilerException("Type mismatch for global " + name);
		}
	}

	public void checkNotExistsInGlobals(String name, Type type) throws RSSLCompilerException {
		if (existsInGlobals(name))
			throw new RSSLCompilerException("Known global " + name);
	}

	public void checkFunctionExists(String name) throws RSSLCompilerException {
		if (!functionExists(name))
			throw new RSSLCompilerException("Unknown function " + name);
	}

	public void checkFunctionNotExists(String name) throws RSSLCompilerException {
		if (functionExists(name))
			throw new RSSLCompilerException("Known function " + name);
	}

	public void checNativeExists(String name) throws RSSLCompilerException {
		if (!nativeExists(name))
			throw new RSSLCompilerException("Unknown native function " + name);
	}

	public void checkNativeNotExists(String name) throws RSSLCompilerException {
		if (nativeExists(name))
			throw new RSSLCompilerException("Known native function " + name);
	}

	public void checkFunctionNoShadow(String name) throws RSSLCompilerException {
		checkFunctionNotExists(name);
		checkNativeNotExists(name);
	}

	private void compileNativeFunction(NativeFuncDef dec) throws RSSLCompilerException {
		checkAllExistsInTypeDict(dec.def.params.types());
		checkExistsInTypeDict(dec.def.returns.type);

		checkFunctionNoShadow(dec.def.id.image);
		natives.put(dec.def.id.image, dec);
		// TODO Auto-generated method stub
	}

	private void compileGlobalBlock(GlobalsDec dec) throws RSSLCompilerException {
		for (VarDec var : dec.decs)
			compileGlobalVarDec(var);
	}

	private void compileGlobalVarDec(VarDec var) throws RSSLCompilerException {
		checkNotExistsInGlobals(var.name.image, null);
		globals.put(var.name.image, var);
	}

	private void compileType(TypeDec dec) throws RSSLCompilerException {
		checkNotExistsInTypeDict(Type.fromIdentifier(dec.id, dec.where));
		types.put(dec.id.image, dec);
	}

	private void compileFunction(Function function) throws RSSLCompilerException {
		// TODO Auto-generated method stub
		checkAllExistsInTypeDict(function.sig.params.types());
		checkExistsInTypeDict(function.sig.returns.type);
		checkFunctionNoShadow(function.sig.id.image);
		CFFunction f0 = null;
		try {
			f0 = writer.writeNodesFromFunc(function);
		} catch (IllegalArgumentException iaex) {
			throw new RSSLCompilerException("Unable to compile source", iaex);
		}
		CFClosure closure = new CFClosure();
		f0.rebuildStack();
		f0.compile(this, closure);
		funcs.put(function.sig.id.image, f0);
	}

	public void assemble(OutputStream write, boolean debug) throws IOException, RSSLAssemblerException,
			RSSLCompilerException {
		BVMStream stream = new BVMStream();
		stream.write(write);

		for (Entry<String, TypeDec> type : types.entrySet()) {
			if (sysTypes.containsKey(type.getKey()))
				continue;
			assembleType(stream, type.getValue(), debug);
		}

		for (Entry<String, VarDec> global : globals.entrySet()) {
			assembleGlobal(stream, global.getValue(), debug);
		}

		for (Entry<String, NativeFuncDef> nativefn : natives.entrySet()) {
			assembleNativeFunc(stream, nativefn.getValue(), debug);
		}

		for (Entry<String, CFFunction> fn : funcs.entrySet()) {
			assembleFunc(stream, fn.getValue(), debug);
		}

	}

	private void assembleType(BVMStream write, TypeDec value, boolean debug) throws IOException,
			RSSLAssemblerException, RSSLCompilerException {
		write.writeString(value.where.image);
		write.writeString(value.id.image);
		write.writeInt(value.where.line);
		if (value.typename == null && value.type == null) {
			write.writeInt(0);
			write.writeString("");
		} else if (value.typename != null) {
			write.writeInt(2);
			write.writeString(value.typename.image);
		} else {
			write.writeInt(1);
			write.writeString("");
		}
	}

	private void assembleGlobal(BVMStream write, VarDec value, boolean debug) throws IOException,
			RSSLAssemblerException, RSSLCompilerException {
		write.writeString(value.where.image);
		write.writeString(value.name.image);
		write.writeString(value.type.typename);
		write.writeInt(value.where.line);
		// TODO: Store expression info?
	}

	private void assembleNativeFunc(BVMStream write, NativeFuncDef value, boolean debug) throws IOException,
			RSSLAssemblerException, RSSLCompilerException {
		write.writeString(value.where.image);
		write.writeString(value.def.id.image);
		write.writeInt(value.where.line);
		write.writeByte((byte) value.def.params.size());
		if (!debug) {
			write.writeInt(0);
		} else {
			write.writeInt(value.def.params.size());
			for (int i = 0; i < value.def.params.size(); i++) {
				write.writeInt(1);
				write.writeInt(i);
				write.writeString(value.def.params.get(i).name.image);
			}
		}
	}

	private void assembleFunc(BVMStream write, CFFunction value, boolean debug) throws IOException,
			RSSLAssemblerException, RSSLCompilerException {
		Allocator alloc = new Allocator();
		write.writeString(value.src.where.image);
		write.writeString(value.src.sig.id.image);
		write.writeInt(value.src.where.line);
		write.writeInt(value.src.where.column);
		write.writeByte((byte) value.src.sig.params.size());
		write.writeByte((byte) value.src.lvars.size());

		// TODO: Decide final register count
		write.writeInt(0);
		// TODO: List instructions

		// TODO: List constants

		if (!debug) {
			write.writeInt(0); // register_data = 0
			write.writeInt(0); // source_data = 0;
		} else {
			// TODO: Decide final register count
			write.writeInt(0);
			// TODO: Output param info
			for (int i = 0; i < value.src.sig.params.size(); i++) {
				write.writeInt(1);
				write.writeInt(i);
				write.writeString(value.src.sig.params.get(i).name.image);
			}
			// TODO: Local vars to registers

			// TODO: Write sources info for each instruction
			write.writeInt(0);
			// ... foreach instr ->
		}
	}

	public void assembleInstruction(BVMStream s, Statement statement, Allocator alloc, ArrayList<CodePlace> places)
			throws IOException, RSSLAssemblerException, RSSLCompilerException {
		places.add(statement.where);
		if (statement instanceof CallStatement) {
		} else if (statement instanceof SetStatement) {
		} else if (statement instanceof SetArrayStatement) {
		} else if (statement instanceof ConditionalStatement) {
		} else if (statement instanceof LoopStatement) {
		} else if (statement instanceof LoopExitStatement) {
		} else if (statement instanceof TryCatchStatement) {
		} else if (statement instanceof RaiseStatement) {
		} else if (statement instanceof ReturnStatement) {
			s.writeOp(new Opcode(Opcodes.RETN));
		} else {
			throw new RSSLAssemblerException("Unsupported statement type " + statement.getClass().getName());
		}
	}

	public int assembleExpression(RSSLCompiler compiler, BVMStream s, CFFunction fn, Allocator alloc,
			CFClosure closure, Expression expression) throws RSSLCompilerException {
		return assembleExpression(compiler, s, fn, alloc, closure, expression, alloc.giveNext("ExprResol"));
	}

	public int assembleExpression(RSSLCompiler compiler, BVMStream s, CFFunction fn, Allocator alloc,
			CFClosure closure, Expression expression, int workRegister) throws RSSLCompilerException {
		ExpressionResolver resolver = new ExpressionResolver(fn);
		ExpressionResolution resolved = resolver.tryResolveExpression(compiler, closure, expression);

		return workRegister;
	}

	private Object emitExpressionTree(RSSLCompiler compiler, BVMStream s, CFFunction fn, Allocator alloc,
			CFClosure closure, ExpressionResolution resolved, int destination) throws RSSLCompilerException,
			IOException {
		if (resolved.hasConstantSolution()) {
			return resolved.known_return_value;
		} else {
			if (resolved.unresolved instanceof ArrayReferenceExpression) {
				ArrayReferenceExpression ref = (ArrayReferenceExpression) resolved.unresolved;
				Opcode op = new Opcode(Opcodes.LARRI, 3);
				op.words[0] = new AZAccess(new QWord(destination, true));
				assembleExpression(compiler, s, fn, alloc, closure, ref.idx, alloc.nextScratchRegister());
				op.words[1] = new AZAccess(new QWord(alloc.lastScratchRegister(), true));
				op.words[2] = new AZAccess(new QWord(alloc.getOrAddConst(ref.name.image), true));
				s.writeOp(op);
			} else if (resolved.unresolved instanceof BinaryOpExpression) {
				BinaryOpExpression expr = (BinaryOpExpression) resolved.unresolved;
				
				Opcode op = null;
				switch (expr.mode) {
				case ADD:
					op = new Opcode(Opcodes.ADD, 3);
					break;
					
				case SUB:
					op = new Opcode(Opcodes.SUB, 3);
					break;
				case MUL:
					op = new Opcode(Opcodes.MUL, 3);
					break;
				case DIV:
					op = new Opcode(Opcodes.DIV, 3);
					break;
					
				case BOOLAND:
					op = new Opcode(Opcodes.TEST, 3);
					break;
				case BOOLOR:
					op = new Opcode(Opcodes.TEST, 3);
					break;
					
				case EQUALS:
					op = new Opcode(Opcodes.EQ, 3);
					break;
				case NOTEQUALS:
					op = new Opcode(Opcodes.NEQ, 3);
					break;
				case GT:
					op = new Opcode(Opcodes.GT, 3);
					break;
				case GTEQ:
					op = new Opcode(Opcodes.GTEQ, 3);
					break;
				case LT:
					op = new Opcode(Opcodes.LT, 3);
					break;
				case LTEQ:
					op = new Opcode(Opcodes.LTEQ, 3);
					break;
				}

				op.words[0] = new AZAccess(new QWord(destination, true));
				
				if (resolved.children.get(0).hasConstantSolution()) {
					int c_slot = alloc.getOrAddConst(resolved.children.get(0).known_return_value);
					op.words[1] = new AZAccess(new QWord(c_slot, false));
				} else {
					assembleExpression(compiler, s, fn, alloc, closure, expr.lhs, alloc.nextScratchRegister());
					op.words[1] = new AZAccess(new QWord(alloc.lastScratchRegister(), true));
				}
				
				if (resolved.children.get(1).hasConstantSolution()) {
					int c_slot = alloc.getOrAddConst(resolved.children.get(1).known_return_value);
					op.words[2] = new AZAccess(new QWord(c_slot, false));
				} else {
					assembleExpression(compiler, s, fn, alloc, closure, expr.rhs, alloc.nextScratchRegister());
					op.words[2] = new AZAccess(new QWord(alloc.lastScratchRegister(), true));
				}
				
				s.writeOp(op);
			} else if (resolved.unresolved instanceof FunctionCallExpression) {
				FunctionCallExpression invoke = (FunctionCallExpression) resolved.unresolved;
				int nparams = invoke.params.size();
				int c_slot = alloc.getOrAddConst(invoke.name.image);
				Opcode op = new Opcode(Opcodes.CALL, 4);
				op.words[0] = new AZAccess(new QWord(c_slot, false));
				op.words[1] = new AZAccess(new QWord(0, true));
				op.words[2] = new AZAccess(new QWord(0, true));
				op.words[3] = new AZAccess(new QWord(destination, true));
				if (nparams != 0) {
					int params_0 = alloc.giveRangeNext("InvokeParam", nparams);
					op.words[1] = new AZAccess(new QWord(params_0, true));
					op.words[2] = new AZAccess(new QWord(params_0 + nparams, true));
					for (int i = 0; i < invoke.params.size(); i++)
						assembleExpression(compiler, s, fn, alloc, closure, invoke.params.get(i), params_0 + i);
				}
				s.writeOp(op);
			} else if (resolved.unresolved instanceof FunctionReferenceExpression) {
				FunctionReferenceExpression ref = (FunctionReferenceExpression) resolved.unresolved;
				Opcode op = new Opcode(Opcodes.LDPTR, 2);
				op.words[0] = new AZAccess(new QWord(destination, true));
				op.words[1] = new AZAccess(new QWord(alloc.getOrAddConst(ref.name.image), false));
				s.writeOp(op);
			} else if (resolved.unresolved instanceof IdentifierReference) {
				IdentifierReference ref = (IdentifierReference) resolved.unresolved;
				Opcode op = new Opcode(Opcodes.MOVE, 2);
				op.words[0] = new AZAccess(new QWord(destination, true));
				op.words[1] = new AZAccess(new QWord(alloc.getOrAddConst(ref.identifier.image), true));
				s.writeOp(op);
			} else if (resolved.unresolved instanceof ParenExpression) {
				ParenExpression parens = (ParenExpression) resolved.unresolved;
				assembleExpression(compiler, s, fn, alloc, closure, parens.child, destination);
			} else if (resolved.unresolved instanceof UnaryOpExpression) {
				UnaryOpExpression unary = (UnaryOpExpression) resolved.unresolved;
				Opcode op;
				switch (unary.mode) {
				case NEG:
					op = new Opcode(Opcodes.UNM, 2);
					assembleExpression(compiler, s, fn, alloc, closure, unary.rhs, alloc.nextScratchRegister());
					op.words[0] = new AZAccess(new QWord(destination, true));
					op.words[1] = new AZAccess(new QWord(alloc.lastScratchRegister(), true));
					break;
				case NOT:
					op = new Opcode(Opcodes.NOT, 2);
					assembleExpression(compiler, s, fn, alloc, closure, unary.rhs, alloc.nextScratchRegister());
					op.words[0] = new AZAccess(new QWord(destination, true));
					op.words[1] = new AZAccess(new QWord(alloc.lastScratchRegister(), true));
					break;
				case POS:
					// TODO: Add new op? :(
					break;
				default:
					break;

				}
			}
		}
	}

	public void dump() {
		System.out.println("------------------------------------------------");
		System.out.println("TYPES: " + types.size());
		for (Entry<String, TypeDec> entry : types.entrySet()) {
			System.out.println(entry.getValue().toString());
		}

		System.out.println("------------------------------------------------");
		System.out.println("GLOBALS: " + globals.size());
		for (Entry<String, VarDec> entry : globals.entrySet()) {
			System.out.println(entry.getValue().toString());
		}

		System.out.println("------------------------------------------------");
		System.out.println("NATIVE FUNCTIONS: " + natives.size());
		for (Entry<String, NativeFuncDef> entry : natives.entrySet()) {
			System.out.println(entry.getValue().toString());
		}

		System.out.println("------------------------------------------------");
		System.out.println("USER FUNCTIONS: " + funcs.size());
		for (Entry<String, CFFunction> entry : funcs.entrySet()) {
			System.out.println(entry.getValue().toString());
		}

	}

}
