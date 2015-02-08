package net.allochie.vm.rssl.runtime;

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
import net.allochie.vm.rssl.runtime.api.TypeRegistry;
import net.allochie.vm.rssl.runtime.frame.VMCallFrame;
import net.allochie.vm.rssl.runtime.frame.VMCallFrameType;
import net.allochie.vm.rssl.runtime.frame.VMExpressionCallFrame;
import net.allochie.vm.rssl.runtime.frame.VMNativeBoundaryFrame;
import net.allochie.vm.rssl.runtime.frame.VMNativeCallFrame;
import net.allochie.vm.rssl.runtime.frame.VMStackFrame;
import net.allochie.vm.rssl.runtime.value.VMFunction;
import net.allochie.vm.rssl.runtime.value.VMFunctionPointer;
import net.allochie.vm.rssl.runtime.value.VMNativeFunction;
import net.allochie.vm.rssl.runtime.value.VMType;
import net.allochie.vm.rssl.runtime.value.VMValue;
import net.allochie.vm.rssl.runtime.value.VMVariable;

/**
 * Virtual machine thread.
 * 
 * @author AfterLifeLochie
 *
 */
public class RSSLThread {

	/** Number of operations per advance */
	public int frequency = 1;
	/** Interrupt flag */
	public boolean interrupt = false;

	/** Init flag */
	private boolean isInit = false;
	/** Suspended flag */
	private boolean suspended = false;
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

	/**
	 * Runs the thread.
	 * 
	 * @throws VMException
	 *             Thrown if the thread cannot find the main function or if the
	 *             call cannot be placed on the stack.
	 */
	public void runThread() throws VMException {
		machine.debugger.trace("thread.runThread", this);
		VMFunction function = machine.findFunction(invokeFunc);
		if (function == null)
			throw new VMException(invokeFunc, "Can't start thread, no function named " + invokeFunc);
		requestCall(top, function, new VMValue[0]);
	}

	/**
	 * Runs a file and imports the globals, functions and other objects into the
	 * local thread scope.
	 * 
	 * @param file
	 *            The parsed file.
	 * @throws VMException
	 *             Thrown if the thread cannot copy the file data, or if the
	 *             code includes invalid or incorrect syntax, illegal access or
	 *             other malformed behavior.
	 */
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

	/**
	 * Sets the operating frequency of the thread.
	 * 
	 * @param speed
	 *            The frequency.
	 */
	public void setFrequency(int speed) {
		machine.debugger.trace("thread.setFrequency", this, speed);
		frequency = speed;
	}

	/**
	 * Interrupts the thread. If the VM is currently advancing the frames on the
	 * thread, the current frame will be completed and the interrupt will be
	 * processed. If the VM is not doing anything, the interrupt is ignored.
	 */
	public void interrupt() {
		machine.debugger.trace("thread.interrupt", this);
		interrupt = true;
	}

	/**
	 * Gets the current frame on the thread's stack.
	 * 
	 * @return The current frame on the stack, or null if the stack is empty.
	 */
	public VMStackFrame getCurrentFrame() {
		if (callStack.size() == 0)
			return null;
		return callStack.peek();
	}

	/**
	 * Requests the invocation of a function. The function frame is placed on
	 * the top of the thread stack and is executed when the VM next advances the
	 * machine.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param function
	 *            The function to invoke.
	 * @param args
	 *            The arguments to provide to the function.
	 * @throws VMException
	 *             Any exception created when creating the function call frame,
	 *             pushing the function call frame to the stack or when
	 *             validating the behavior of the invocation.
	 */
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

	/**
	 * Requests the invocation of a function. The function frame is placed on
	 * the top of the thread stack and is executed immediately on top of the
	 * stack.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param function
	 *            The function to invoke.
	 * @param args
	 *            The arguments to provide to the function.
	 * @throws VMException
	 *             Any exception created when creating the function call frame,
	 *             pushing the function call frame to the stack, when validating
	 *             the behavior of the invocation or when invoking the function
	 *             on the stack.
	 */
	public void foreignCallImmediately(VMClosure closure, VMFunction function, VMValue[] args) throws VMException {
		machine.debugger.trace("thread.foreignCallImmediately", this, closure, function, args);
		VMStackFrame topFrame = getCurrentFrame();
		requestCall(closure, function, args);
		advanceUntilFrame(topFrame);
	}

	/**
	 * Requests the invocation of a conditional statement on the stack. The
	 * conditional container frame is placed on the thread stack and is executed
	 * when the VM next advances the machine.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param conditional
	 *            The conditional statement to evaluate through.
	 * @throws VMException
	 *             Thrown when any condition prevents the conditional from being
	 *             placed on the top of the execution stack.
	 */
	public void requestCall(VMClosure closure, ConditionalStatement conditional) throws VMException {
		machine.debugger.trace("thread.requestCall<conditional>", this, closure, conditional);
		VMStackFrame callframe = new VMCallFrame(closure, conditional.where, conditional.statements,
				VMCallFrameType.CONDITIONAL);
		callStack.push(callframe);
	}

	/**
	 * Requests the invocation of a loop statement on the stack. The loop
	 * container frame is placed on the thread stack and is executed when the VM
	 * next advances the machine.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param loop
	 *            The conditional statement to evaluate through.
	 * @throws VMException
	 *             Thrown when any condition prevents the loop from being placed
	 *             on the top of the execution stack.
	 */
	public void requestCall(VMClosure closure, LoopStatement loop) throws VMException {
		machine.debugger.trace("thread.requestCall<loop>", this, closure, loop);
		VMStackFrame callframe = new VMCallFrame(closure, loop.where, loop.statements, VMCallFrameType.LOOP);
		callStack.push(callframe);
	}

	/**
	 * Requests the invocation of a list of statements on the stack. The
	 * container frame is placed on the thread stack and is executed when the VM
	 * next advances the machine.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param where
	 *            The source of the list.
	 * @param statements
	 *            The list of statements.
	 * @param loop
	 *            The loop mode.
	 * @throws VMException
	 *             Thrown when any condition prevents the list from being placed
	 *             on the top of the execution stack.
	 */
	public void requestCall(VMClosure closure, CodePlace where, StatementList statements, boolean loop)
			throws VMException {
		machine.debugger.trace("thread.requestCall<slist>", this, closure, statements, loop);
		VMStackFrame callframe = new VMCallFrame(closure, where, statements, VMCallFrameType.DEFAULT);
		callStack.push(callframe);
	}

	/**
	 * Requests the resolution of an expression on the stack. The container
	 * frame is placed on the thread stack and is executed when the VM next
	 * advances the machine.
	 * 
	 * @param closure
	 *            The closure scope.
	 * @param expression
	 *            The expression to evaluate.
	 * @throws VMException
	 *             Thrown when any condition prevents the expression resolution
	 *             wrapper frame from being placed on the top of the execution
	 *             stack.
	 */
	public void resolveExpression(VMClosure closure, Expression expression) throws VMException {
		machine.debugger.trace("thread.resolveExpression<expression>", this, closure, expression);
		VMExpressionCallFrame callframe = new VMExpressionCallFrame(closure, expression);
		callStack.push(callframe);
	}

	/**
	 * Check if the thread has died.
	 * 
	 * @return If the thread has died.
	 */
	public boolean dead() {
		return isDead;
	}

	/**
	 * Request a frame be placed on the top of the stack.
	 * 
	 * @param frame
	 *            The frame.
	 */
	public void requestFrame(VMStackFrame frame) {
		machine.debugger.trace("thread.requestFrame", this, frame);
		callStack.push(frame);
	}

	/**
	 * Find the most recent frame of a type on the top of the stack.
	 * 
	 * @param typeof
	 *            The frame type.
	 * @return The most recent instance of the frame.
	 */
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

	/**
	 * Unwind the stack to the frame specified. Leaves the frame specified on
	 * the top of the stack.
	 * 
	 * @param stop
	 *            The frame to unwind to.
	 * @throws VMException
	 *             Thrown when nothing is on the stack to remove.
	 */
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

	/**
	 * Remove a single frame from the head of the stack. If the head of the
	 * stack does not match the frame provided, an exception will be raised.
	 * 
	 * @param frame
	 *            The frame to remove from the head of the stack.
	 * @throws VMException
	 *             Thrown when the frame on the top of the stack is not the
	 *             frame provided as a parameter.
	 */
	public void removeFrame(VMStackFrame frame) throws VMException {
		machine.debugger.trace("thread.removeFrame", this, frame);
		VMStackFrame what = callStack.peek();
		if (!what.equals(frame))
			throw new VMException(this, "Attempt to remove unmatched frame.");
		callStack.pop();
	}

	/**
	 * Advance the machine until the frame on the top of the stack matches the
	 * frame provided.
	 * 
	 * @param frame
	 *            The frame to halt at.
	 * @throws VMException
	 *             Thrown when the frame cannot be matched, when the stack
	 *             becomes empty or when any frame raises an exception.
	 */
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

	/**
	 * Steps the machine by one cycle.
	 * 
	 * @throws VMException
	 *             Thrown if any frame in the execution cycle throws an
	 *             exception.
	 */
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

	/**
	 * Checks if the thread is currently running.
	 * 
	 * @return If the thread is running.
	 */
	public boolean running() {
		return isInit && !isDead;
	}

}