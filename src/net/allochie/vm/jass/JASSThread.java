package net.allochie.vm.jass;

import java.util.Stack;

import net.allochie.vm.jass.VMVariable.VMSetInitFrame;
import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.Param;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.dec.GlobalsDec;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;
import net.allochie.vm.jass.ast.dec.TypeDec;
import net.allochie.vm.jass.ast.dec.VarDec;
import net.allochie.vm.jass.ast.expression.Expression;
import net.allochie.vm.jass.ast.statement.ConditionalStatement;
import net.allochie.vm.jass.ast.statement.LoopStatement;
import net.allochie.vm.jass.global.TypeRegistry;

public class JASSThread {

	/** Number of operations per advance */
	public int frequency = 1;
	/** Interrupt flag */
	public boolean interrupt = false;

	/** Init flag */
	private boolean isInit = false;
	/** Dead flag */
	private boolean isDead = false;

	/** The JASS machine being worked on */
	private final JASSMachine machine;
	/** The thread's name */
	public final String name;
	/** Function to invoke */
	public final VMFunctionPointer invokeFunc;
	/** Current frame stack */
	public final Stack<VMStackFrame> callStack = new Stack<VMStackFrame>();
	/** The top closure */
	public final VMClosure top;

	/**
	 * Create a new JASSThread scheduler
	 * 
	 * @param machine
	 *            The JASS machine
	 * @param threadName
	 *            The thread name
	 * @param closure
	 *            The closure
	 * @param runFunc
	 *            The function to invoke
	 */
	public JASSThread(JASSMachine machine, String threadName, VMClosure closure, VMFunctionPointer runFunc) {
		this.machine = machine;
		this.name = threadName;
		this.top = closure;
		this.invokeFunc = runFunc;
	}

	public void runThread() throws VMException {
		VMFunction function = machine.findFunction(invokeFunc);
		if (function == null)
			throw new VMException("Can't start thread, no such function exists");
		requestCall(top, function, new VMValue[0]);
	}

	public void doFile(JASSFile file) throws VMException {
		for (Dec what : file.decs) {
			if (what instanceof TypeDec) {
				TypeDec type = (TypeDec) what;
				if (type.type == null)
					if (!machine.types.containsKey(type.typename.image))
						throw new VMException("Cannot extend unknown type " + type.typename.image);
				machine.types.put(type.id.image, (VMType) TypeRegistry.fromString(type.id.image));
				if (type.type == null)
					machine.types.get(type.id.image).setExtensionOf(type.typename.image);
				else {
					switch (type.type) {
					case HANDLE:
						machine.types.get(type.id.image).setExtensionOf(Type.handleType);
						break;
					default:
						break;

					}
				}
			} else if (what instanceof GlobalsDec) {
				GlobalsDec heap = (GlobalsDec) what;
				for (VarDec var : heap.decs) {
					if (machine.globals.containsKey(var.name.image) && machine.globals.get(var.name.image).dec.constant)
						throw new VMException("Cannot redeclare existing variable " + var.name.image);
					machine.globals.put(var.name.image, new VMVariable(machine, top, var));
					VMStackFrame topFrame = getCurrentFrame();
					machine.globals.get(var.name.image).init(this, var, top);
					advanceUntilFrame(topFrame);
				}
			} else if (what instanceof NativeFuncDef) {
				NativeFuncDef nativeFn = (NativeFuncDef) what;
				machine.natives.put(nativeFn.def.id.image, new VMNativeFunction(nativeFn));
			} else
				throw new VMException("Unknown definition type " + what.getClass().getName());
		}

		for (Function func : file.funcs)
			machine.funcs.put(func.sig.id.image, new VMFunction(func));
	}

	public void setFrequency(int speed) {
		this.frequency = speed;
	}

	public void interrupt() {
		this.interrupt = true;
	}

	public VMStackFrame getCurrentFrame() throws VMException {
		if (callStack.size() == 0)
			return null;
		return callStack.peek();
	}

	public void requestCall(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		if (function instanceof VMNativeFunction) {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(machine, pvar);
				child.getVariable(param.name).safeSetValue(args[i]);
			}
			VMNativeCallFrame frame = new VMNativeCallFrame(child, (VMNativeFunction) function);
			callStack.push(frame);
		} else {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(machine, pvar);
				child.getVariable(param.name).safeSetValue(args[i]);
			}

			VMStackFrame topFrame = getCurrentFrame();
			if (function.lvars != null)
				for (VarDec var : function.lvars) {
					child.createVariable(machine, var);
					child.getVariable(var.name).init(this, var, child);
					advanceUntilFrame(topFrame);
				}

			VMStackFrame callframe = new VMCallFrame(child, function.statements, args);
			callStack.push(callframe);
		}
	}

	public void requestCallImmediate(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		VMStackFrame topFrame = getCurrentFrame();
		requestCall(closure, function, args);
		advanceUntilFrame(topFrame);
	}

	public void requestCall(VMClosure closure, ConditionalStatement conditional) {
		VMStackFrame callframe = new VMCallFrame(closure, conditional.statements, false);
		callStack.push(callframe);
	}

	public void requestCall(VMClosure closure, LoopStatement loop) {
		VMStackFrame callframe = new VMCallFrame(closure, loop.statements, true);
		callStack.push(callframe);
	}

	public void resolveExpression(VMClosure closure, Expression expression) throws VMException {
		VMExpressionCallFrame callframe = new VMExpressionCallFrame(closure, expression);
		callStack.push(callframe);
	}

	public boolean dead() {
		return isDead;
	}

	public void requestFrame(VMStackFrame frame) {
		callStack.push(frame);
	}

	private void advanceUntilFrame(VMStackFrame frame) throws VMException {
		while (true) {
			if (callStack.size() == 0)
				break;
			VMStackFrame current = callStack.peek();
			if (current == frame)
				break;
			advanceFrame();
		}
	}

	private boolean interrupted() {
		return this.interrupt;
	}

	private void flushInterrupts() {
		this.interrupt = false;
	}

	private void init() throws VMException {
		isInit = true;
	}

	public void advance() throws VMException {
		if (isDead)
			throw new VMException("Can't resume dead thread");
		if (!isInit)
			init();
		if (!isInit)
			throw new VMException("Failed to init thread");

		int count = frequency;
		while (count >= 0) {
			if (interrupted())
				break;
			if (callStack.size() == 0)
				break;
			advanceFrame();
			count--;
		}

		flushInterrupts();
		if (callStack.size() == 0) {
			System.out.println("Finished thread.");
			isDead = true;
		}
	}

	private void advanceFrame() throws VMException {
		if (callStack.size() != 0)
			callStack.peek().step(machine, this);
		while (callStack.size() != 0 && callStack.peek().finished()) {
			VMStackFrame last = callStack.pop();
			if (callStack.size() != 0)
				callStack.peek().setInvokeResult(last.getReturnResult());
		}
	}

}
