package net.allochie.vm.jass;

import java.util.HashMap;
import java.util.Stack;

import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.Param;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.constant.BoolConst;
import net.allochie.vm.jass.ast.constant.Constant;
import net.allochie.vm.jass.ast.constant.IntConstant;
import net.allochie.vm.jass.ast.constant.RealConst;
import net.allochie.vm.jass.ast.constant.StringConst;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.dec.GlobalsDec;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;
import net.allochie.vm.jass.ast.dec.TypeDec;
import net.allochie.vm.jass.ast.dec.VarDec;
import net.allochie.vm.jass.ast.expression.ArrayReferenceExpression;
import net.allochie.vm.jass.ast.expression.BinaryOpExpression;
import net.allochie.vm.jass.ast.expression.Expression;
import net.allochie.vm.jass.ast.expression.FunctionCallExpression;
import net.allochie.vm.jass.ast.expression.FunctionReferenceExpression;
import net.allochie.vm.jass.ast.expression.IdentifierReference;
import net.allochie.vm.jass.ast.expression.ParenExpression;
import net.allochie.vm.jass.ast.expression.UnaryOpExpression;
import net.allochie.vm.jass.ast.statement.ConditionalStatement;
import net.allochie.vm.jass.ast.statement.LoopStatement;

public class JASSMachine {

	/** List of all known VM types */
	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();
	/** List of all global variables */
	public HashMap<String, VMVariable> globals = new HashMap<String, VMVariable>();
	/** List of all native functions */
	public HashMap<String, VMNativeFunction> natives = new HashMap<String, VMNativeFunction>();
	/** List of all real functions */
	public HashMap<String, VMFunction> funcs = new HashMap<String, VMFunction>();

	public Stack<VMCallFrame> callStack = new Stack<VMCallFrame>();

	public void doFile(VMClosure closure, JASSFile file) throws VMException {
		for (Dec what : file.decs) {
			if (what instanceof TypeDec) {
				TypeDec type = (TypeDec) what;
				if (type.type == null)
					if (!types.containsKey(type.typename.image))
						throw new VMException("Cannot extend unknown type " + type.typename.image);
				types.put(type.id.image, type);
			} else if (what instanceof GlobalsDec) {
				GlobalsDec heap = (GlobalsDec) what;
				for (VarDec var : heap.decs) {
					if (globals.containsKey(var.name.image) && globals.get(var.name.image).dec.constant)
						throw new VMException("Cannot redeclare existing variable " + var.name.image);
					globals.put(var.name.image, new VMVariable(closure, var));
					VMCallFrame topFrame = getCurrentFrame();
					globals.get(var.name.image).init(this, closure);
					advanceUntilFrame(topFrame);
				}
			} else if (what instanceof NativeFuncDef) {
				NativeFuncDef nativeFn = (NativeFuncDef) what;
				natives.put(nativeFn.def.id.image, new VMNativeFunction(nativeFn));
			} else
				throw new VMException("Unknown definition type " + what.getClass().getName());
		}

		for (Function func : file.funcs)
			funcs.put(func.sig.id.image, new VMFunction(func));

		VMFunction function = findFunction(Identifier.fromString("main"));
		try {
			if (function != null)
				requestCall(closure, function, new VMValue[0]);
			while (getCurrentFrame() != null) {
				advanceVM();
			}
		} catch (VMException e) {
			e.printStackTrace();
			for (int i = 0; i < callStack.size(); i++) {
				VMCallFrame frame = callStack.get(i);
				System.out.println("frame: " + frame);
			}
		}

	}

	public void advanceVM() throws VMException {
		if (callStack.size() == 0)
			throw new VMException("Nothing to execute on stack");

		if (callStack.size() != 0)
			callStack.peek().step(this);

		while (callStack.size() != 0 && callStack.peek().finished) {
			VMCallFrame last = callStack.pop();
			if (callStack.size() != 0)
				callStack.peek().callResult = last.result;
		}

	}

	public VMCallFrame getCurrentFrame() throws VMException {
		if (callStack.size() == 0)
			return null;
		return callStack.peek();
	}

	public void advanceUntilFrame(VMCallFrame frame) throws VMException {
		while (true) {
			if (callStack.size() == 0)
				break;
			VMCallFrame current = callStack.peek();
			if (current == frame)
				break;
			advanceVM();
		}
	}

	public VMFunction findFunction(Identifier identifier) {
		VMFunction function = funcs.get(identifier.image);
		if (function != null)
			return function;
		function = natives.get(identifier.image);
		if (function != null)
			return function;
		return null;
	}

	public VMVariable findGlobal(Identifier identifier) {
		return globals.get(identifier.image);
	}

	public void requestCall(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		if (function instanceof VMNativeFunction) {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(pvar);
				child.getVariable(param.name).safeSetValue(args[i]);
			}
			VMSpecialFrame frame = new VMSpecialFrame(child) {
				public VMNativeFunction nfunc;

				public VMSpecialFrame setFunc(VMNativeFunction nfunc) {
					this.nfunc = nfunc;
					return this;
				}

				@Override
				public void doSpecialStep(JASSMachine machine) throws VMException {
					result = nfunc.executeNative(this.closure);
					finished = true;
				}
			}.setFunc((VMNativeFunction) function);
			callStack.push(frame);
		} else {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(pvar);
				child.getVariable(param.name).safeSetValue(args[i]);
			}

			VMCallFrame topFrame = getCurrentFrame();
			if (function.lvars != null)
				for (VarDec var : function.lvars) {
					child.createVariable(var);
					child.getVariable(var.name).init(this, child);
					advanceUntilFrame(topFrame);
				}

			VMCallFrame callframe = new VMCallFrame(child, function.statements, args);
			callStack.push(callframe);
		}
	}

	public void requestCall(VMClosure closure, ConditionalStatement conditional) {
		VMCallFrame callframe = new VMCallFrame(closure, conditional.statements, false);
		callStack.push(callframe);
	}

	public void requestCall(VMClosure closure, LoopStatement loop) {
		VMCallFrame callframe = new VMCallFrame(closure, loop.statements, true);
		callStack.push(callframe);
	}

	public void resolveExpression(VMClosure closure, Expression expression) throws VMException {
		VMExpressionCallFrame callframe = new VMExpressionCallFrame(closure, expression);
		callStack.push(callframe);
	}

	public void requestFrame(VMSpecialFrame frame) {
		callStack.push(frame);
	}

}
