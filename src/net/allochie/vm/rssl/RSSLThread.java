package net.allochie.vm.rssl;

import java.util.Stack;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.Param;
import net.allochie.vm.rssl.ast.RSSLFile;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.dec.Dec;
import net.allochie.vm.rssl.ast.dec.GlobalsDec;
import net.allochie.vm.rssl.ast.dec.NativeFuncDef;
import net.allochie.vm.rssl.ast.dec.TypeDec;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.ast.statement.ConditionalStatement;
import net.allochie.vm.rssl.ast.statement.LoopStatement;
import net.allochie.vm.rssl.global.TypeRegistry;

public class RSSLThread {

	/** Number of operations per advance */
	public int frequency = 1;
	/** Interrupt flag */
	public boolean interrupt = false;

	/** Init flag */
	private boolean isInit = false;
	/** Dead flag */
	private boolean isDead = false;

	/** The JASS machine being worked on */
	private final RSSLMachine machine;
	/** The thread's name */
	public final String name;
	/** Function to invoke */
	public final VMFunctionPointer invokeFunc;
	/** Current frame stack */
	public final Stack<VMStackFrame> callStack = new Stack<VMStackFrame>();
	/** The top closure */
	public final VMClosure top;

	/**
	 * Create a new RSSLThread scheduler
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
	public RSSLThread(RSSLMachine machine, String threadName, VMClosure closure, VMFunctionPointer runFunc) {
		this.machine = machine;
		name = threadName;
		top = closure;
		invokeFunc = runFunc;
	}

	public void runThread() throws VMException {
		machine.debugger.trace("thread.runThread", this);
		VMFunction function = machine.findFunction(invokeFunc);
		if (function == null)
			throw new VMException(invokeFunc, "Can't start thread, no function named " + invokeFunc);
		requestCall(top, function, new VMValue[0]);
	}

	public void doFile(RSSLFile file) throws VMException {
		machine.debugger.trace("thread.doFile", this, file);
		for (Dec what : file.decs)
			if (what instanceof TypeDec) {
				TypeDec type = (TypeDec) what;
				if (type.type == null)
					if (!machine.types.containsKey(type.typename.image))
						throw new VMUserCodeException(type, "Cannot extend unknown type " + type.typename.image);
				machine.debugger.trace("thread.doFile.registerType", this, file, type);
				machine.types.put(type.id.image, (VMType) TypeRegistry.fromString(type.id.image));
				if (type.type == null)
					machine.types.get(type.id.image).setExtensionOf(type.typename.image);
				else
					switch (type.type) {
					case HANDLE:
						machine.types.get(type.id.image).setExtensionOf(Type.handleType);
						break;
					default:
						break;

					}
			} else if (what instanceof GlobalsDec) {
				GlobalsDec heap = (GlobalsDec) what;
				for (VarDec var : heap.decs) {
					if (machine.globals.containsKey(var.name.image) && machine.globals.get(var.name.image).dec.constant)
						throw new VMUserCodeException(var, "Cannot redeclare existing variable " + var.name.image);
					machine.debugger.trace("thread.doFile.registerGlobal", this, file, var);
					machine.globals.put(var.name.image, new VMVariable(machine, top, var));
					VMStackFrame topFrame = getCurrentFrame();
					machine.globals.get(var.name.image).init(this, var, top);
					advanceUntilFrame(topFrame);
				}
			} else if (what instanceof NativeFuncDef) {
				NativeFuncDef nativeFn = (NativeFuncDef) what;
				machine.debugger.trace("thread.doFile.registerNative", this, nativeFn);
				machine.natives.put(nativeFn.def.id.image, new VMNativeFunction(nativeFn));
			} else
				throw new VMException(what, "Unknown definition type " + what.getClass().getName());

		for (Function func : file.funcs) {
			machine.debugger.trace("thread.doFile.registerFunction", this, func);
			machine.funcs.put(func.sig.id.image, new VMFunction(func));
		}
	}

	public void setFrequency(int speed) {
		machine.debugger.trace("thread.setFrequency", this, speed);
		frequency = speed;
	}

	public void interrupt() {
		machine.debugger.trace("thread.interrupt", this);
		interrupt = true;
	}

	public VMStackFrame getCurrentFrame() throws VMException {
		if (callStack.size() == 0)
			return null;
		return callStack.peek();
	}

	public void requestCall(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		machine.debugger.trace("thread.requestCall", this, closure, function, args);
		if (function instanceof VMNativeFunction) {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(machine, pvar);
				child.getVariable(machine, param.name).safeSetValue(args[i]);
			}
			VMNativeCallFrame frame = new VMNativeCallFrame(child, (VMNativeFunction) function);
			callStack.push(frame);
		} else {
			VMClosure child = new VMClosure(closure);
			for (int i = 0; i < args.length; i++) {
				Param param = function.sig.params.get(i);
				VarDec pvar = new VarDec(param.name, param.type, param.array, false, null);
				child.createVariable(machine, pvar);
				child.getVariable(machine, param.name).safeSetValue(args[i]);
			}

			VMStackFrame topFrame = getCurrentFrame();
			if (function.lvars != null)
				for (VarDec var : function.lvars) {
					child.createVariable(machine, var);
					child.getVariable(machine, var.name).init(this, var, child);
					advanceUntilFrame(topFrame);
				}

			VMStackFrame callframe = new VMCallFrame(child, function.where, function.statements, args);
			callStack.push(callframe);
		}
	}

	public void foreignCallImmediately(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		machine.debugger.trace("thread.foreignCallImmediately", this, closure, function, args);
		VMStackFrame topFrame = getCurrentFrame();
		requestCall(closure, function, args);
		advanceUntilFrame(topFrame);
	}

	public void requestCall(VMClosure closure, ConditionalStatement conditional) throws VMException {
		machine.debugger.trace("thread.requestCall<conditional>", this, closure, conditional);
		VMStackFrame callframe = new VMCallFrame(closure, conditional.where, conditional.statements,
				VMCallFrameType.CONDITIONAL);
		callStack.push(callframe);
	}

	public void requestCall(VMClosure closure, LoopStatement loop) throws VMException {
		machine.debugger.trace("thread.requestCall<loop>", this, closure, loop);
		VMStackFrame callframe = new VMCallFrame(closure, loop.where, loop.statements, VMCallFrameType.LOOP);
		callStack.push(callframe);
	}

	public void requestCall(VMClosure closure, CodePlace where, StatementList statements, boolean loop)
			throws VMException {
		machine.debugger.trace("thread.requestCall<slist>", this, closure, statements, loop);
		VMStackFrame callframe = new VMCallFrame(closure, where, statements, VMCallFrameType.DEFAULT);
		callStack.push(callframe);
	}

	public void resolveExpression(VMClosure closure, Expression expression) throws VMException {
		machine.debugger.trace("thread.resolveExpression<expression>", this, closure, expression);
		VMExpressionCallFrame callframe = new VMExpressionCallFrame(closure, expression);
		callStack.push(callframe);
	}

	public boolean dead() {
		return isDead;
	}

	public void requestFrame(VMStackFrame frame) {
		machine.debugger.trace("thread.requestFrame", this, frame);
		callStack.push(frame);
	}

	public VMCallFrame findLatestFrame(VMCallFrameType typeof) {
		if (callStack.size() != 0)
			for (int i = callStack.size() - 1; i >= 0; i--) {
				VMStackFrame frame = callStack.get(i);
				if (frame instanceof VMCallFrame) {
					VMCallFrame call = (VMCallFrame) frame;
					if (call.type == typeof)
						return call;
					if (call.type == VMCallFrameType.FUNCTION)
						return null;
				}
				if (frame instanceof VMNativeBoundaryFrame)
					return null;
			}
		return null;
	}

	public void unwindFrames(VMStackFrame stop) throws VMException {
		machine.debugger.trace("thread.unwindFrames", this, stop);
		while (true) {
			VMStackFrame what = callStack.peek();
			if (what == null)
				throw new VMException(this, "Nothing on the stack.");
			if (what.equals(stop))
				return;
			callStack.pop();
		}
	}

	public void removeFrame(VMStackFrame frame) throws VMException {
		machine.debugger.trace("thread.removeFrame", this, frame);
		VMStackFrame what = callStack.peek();
		if (!what.equals(frame))
			throw new VMException(this, "Attempt to remove unmatched frame.");
		callStack.pop();
	}

	private void advanceUntilFrame(VMStackFrame frame) throws VMException {
		machine.debugger.trace("thread.advanceUntilFrame", this, frame);
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
		return interrupt;
	}

	private void flushInterrupts() {
		machine.debugger.trace("thread.flushInterrupts", this);
		interrupt = false;
	}

	private void init() throws VMException {
		machine.debugger.trace("thread.init", this);
		isInit = true;
	}

	public void advance() throws VMException {
		machine.debugger.trace("thread.advance", this);
		if (isDead)
			throw new VMException(this, "Can't resume dead thread");
		if (!isInit)
			init();
		if (!isInit)
			throw new VMException(this, "Failed to init thread");
		try {
			int count = frequency;
			while (count > 0) {
				if (interrupted())
					break;
				if (callStack.size() == 0)
					break;
				advanceFrame();
				count--;
			}
		} catch (VMException e) {
			isDead = true;
			throw e;
		}

		flushInterrupts();
		if (callStack.size() == 0)
			isDead = true;
	}

	private void advanceFrame() throws VMException {
		machine.debugger.trace("thread.advanceFrame", this);
		if (callStack.size() != 0)
			try {
				callStack.peek().step(machine, this);
			} catch (VMUserCodeException code) {
				boolean flag = false;
				callStack.pop();
				if (callStack.size() != 0)
					for (int i = callStack.size() - 1; i >= 0; i--) {
						VMStackFrame frame = callStack.get(i);
						if (frame instanceof VMCallFrame) {
							VMCallFrame call = (VMCallFrame) frame;
							int w = callStack.size() - (1 + i);
							while (w > 0) {
								VMStackFrame f0 = callStack.pop();
								code.pushFrame(f0);
								w--;
							}
							code.pushFrame(call);
							if (callStack.peek() != call)
								throw new VMException(this, "Failed to rewind stack!");
							machine.debugger.trace("thread.advanceFrame.handleException", this, call, code);
							call.setException(code);
							flag = true;
							break;
						}
					}
				if (!flag) {
					for (int i = callStack.size() - 1; i >= 0; i--)
						code.pushFrame(callStack.get(i));
					throw code;
				}
			}
		while (callStack.size() != 0 && callStack.peek().finished()) {
			VMStackFrame last = callStack.pop();
			machine.debugger.trace("thread.stack.finished", this, last);
			if (callStack.size() != 0)
				callStack.peek().setInvokeResult(last.getReturnResult());
		}
	}

	public boolean running() {
		return isInit && !isDead;
	}

}