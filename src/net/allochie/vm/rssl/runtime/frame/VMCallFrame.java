package net.allochie.vm.rssl.runtime.frame;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.statement.CallStatement;
import net.allochie.vm.rssl.ast.statement.ConditionalStatement;
import net.allochie.vm.rssl.ast.statement.LoopExitStatement;
import net.allochie.vm.rssl.ast.statement.LoopStatement;
import net.allochie.vm.rssl.ast.statement.RaiseStatement;
import net.allochie.vm.rssl.ast.statement.ReturnStatement;
import net.allochie.vm.rssl.ast.statement.SetArrayStatement;
import net.allochie.vm.rssl.ast.statement.SetStatement;
import net.allochie.vm.rssl.ast.statement.TryCatchStatement;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMClosure;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.VMUserCodeException;
import net.allochie.vm.rssl.runtime.value.VMFunction;
import net.allochie.vm.rssl.runtime.value.VMType;
import net.allochie.vm.rssl.runtime.value.VMValue;
import net.allochie.vm.rssl.runtime.value.VMVariable;

/**
 * Invocation frame on the stack.
 * 
 * @author AfterLifeLochie
 *
 */
public class VMCallFrame extends VMStackFrame {

	/** List of working statements */
	public final StatementList statements;
	/** The working closure */
	public final VMClosure closure;
	/** The frame type */
	public final VMCallFrameType type;
	/** Is this frame an exception handler? */
	private boolean isExceptionHandler;
	/** The current operation index */
	public int currentOp;
	/** The current work location */
	public final CodePlace where;
	/** If the frame has finished working */
	public boolean finished;
	/** The call parameters */
	public VMValue[] args;

	protected VMValue store0, store1;
	protected VMValue store2[];
	protected int i, j, k;
	protected VMUserCodeException caughtException, handledException;

	public VMCallFrame(VMClosure closure, CodePlace where, StatementList statements, VMCallFrameType type)
			throws VMException {
		if (where == null)
			throw new VMException(null, "Cannot create call frame without source.");
		this.closure = closure;
		this.where = where;
		this.statements = statements;
		this.type = type;
	}

	public VMCallFrame(VMClosure closure, CodePlace where, StatementList statements, VMValue[] args) throws VMException {
		if (where == null)
			throw new VMException(null, "Cannot create call frame without source.");
		this.closure = closure;
		this.statements = statements;
		this.where = where;
		this.args = args;
		type = VMCallFrameType.DEFAULT;
	}

	@Override
	public void step(RSSLMachine machine, RSSLThread thread) throws VMException {
		machine.debugger.trace("vmCallFrame.step", this, thread);
		if (getException() != null && !isExceptionHandler)
			throw getException();
		if (finished)
			throw new VMException(this, "Cannot advance finished call frame");
		Statement statement = null;
		if (statements.size() != 0)
			statement = statements.get(currentOp);
		workPlace = (statement != null) ? statement.where : null;
		if (statement instanceof CallStatement) {
			CallStatement call = (CallStatement) statement;
			VMFunction function = machine.findFunction(call.id);
			if (function == null)
				throw new VMUserCodeException(statement, "Cannot call undefined function " + call.id);
			int numParams = function.sig.params.size();
			int providedParams = (call.params != null) ? call.params.size() : 0;
			if (numParams != providedParams)
				throw new VMUserCodeException(statement, "Incorrect number of parameters for function call");
			if (store2 == null)
				store2 = new VMValue[numParams];
			while (j < numParams) {
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, call.params.get(j));
					return;
				}
				store2[j] = getPreviousCallResult();
				if (function.sig.params.get(j).type != store2[j].type)
					throw new VMUserCodeException(statement, "Incorrect parameter type for call " + function.sig.id
							+ ": got " + store2[j].type + ", expected " + function.sig.params.get(j).type);
				j++;
			}
			if (i == 0) {
				i++;
				thread.requestCall(closure, function, store2);
				return;
			}
			store0 = getPreviousCallResult();
		} else if (statement instanceof ConditionalStatement) {
			ConditionalStatement conditional = (ConditionalStatement) statement;
			while (conditional != null)
				if (conditional.conditional != null) {
					if (!hasPreviousCallResult()) {
						thread.resolveExpression(closure, conditional.conditional);
						return;
					}
					VMValue state = getPreviousCallResult();
					if (state.type != Type.booleanType)
						throw new VMUserCodeException(statement, "Cannot perform conditional on non-boolean");
					if (state.asBooleanType()) {
						thread.requestCall(closure, conditional);
						break;
					} else
						conditional = conditional.child;
				} else {
					thread.requestCall(closure, conditional);
					break;
				}
		} else if (statement instanceof LoopExitStatement) {
			LoopExitStatement exit = (LoopExitStatement) statement;
			VMCallFrame loopFrame = thread.findLatestFrame(VMCallFrameType.LOOP);
			if (loopFrame == null)
				throw new VMUserCodeException(statement, "Cannot use exitwhen outside a loop");
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, exit.conditional);
				return;
			}
			VMValue state = getPreviousCallResult();
			if (state.type != Type.booleanType)
				throw new VMUserCodeException(statement, "Cannot leave loop on non-boolean");
			if (state.asBooleanType()) {
				thread.unwindFrames(loopFrame);
				loopFrame.finished = true;
				finished = true;
			}
		} else if (statement instanceof LoopStatement) {
			LoopStatement loop = (LoopStatement) statement;
			thread.requestCall(closure, loop);
		} else if (statement instanceof TryCatchStatement) {
			TryCatchStatement tryBlock = (TryCatchStatement) statement;
			isExceptionHandler = true;
			if (j == 0) {
				thread.requestCall(closure, tryBlock.where, tryBlock.statements, false);
				j++;
				return;
			} else {
				isExceptionHandler = false;
				if (caughtException != null && k == 0) {
					handledException = caughtException;
					caughtException = null;
					thread.requestCall(closure, tryBlock.whereCatch, tryBlock.catchStatements, false);
					k++;
					return;
				} else
					handledException = null;
			}
		} else if (statement instanceof ReturnStatement) {
			ReturnStatement retn = (ReturnStatement) statement;
			VMCallFrame returnFunc = thread.findLatestFrame(VMCallFrameType.FUNCTION);
			if (returnFunc == null)
				throw new VMUserCodeException(retn, "Cannot use return outside of function scope!");
			if (retn.expression != null) {
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, retn.expression);
					return;
				}
				result = getPreviousCallResult();
			}
			thread.unwindFrames(returnFunc);
			returnFunc.finished = true;
			returnFunc.result = result;
			finished = true;
		} else if (statement instanceof RaiseStatement) {
			RaiseStatement uerr = (RaiseStatement) statement;
			if (uerr.expression == null)
				throw new VMUserCodeException(statement, "Can't raise error without expression");
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, uerr.expression);
				return;
			}
			VMValue what = getPreviousCallResult();
			if (what.type != Type.stringType)
				throw new VMUserCodeException(statement, "Can't raise error with non-string reason");
			throw new VMUserCodeException(statement, what.asStringType());
		} else if (statement instanceof SetArrayStatement) {
			SetArrayStatement arrayset = (SetArrayStatement) statement;
			VMVariable var = closure.getVariable(machine, arrayset.id);
			if (!var.dec.array)
				throw new VMUserCodeException(statement, "Not an array");
			if (!var.defined())
				throw new VMUserCodeException(statement, "Attempt to access undefined variable " + var.dec.name.image);
			HashMap<Integer, VMValue> what = var.safeValue().asArrayType();
			if (store0 == null) {
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, arrayset.idx);
					return;
				}
				store0 = getPreviousCallResult();
			}
			if (store1 == null) {
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, arrayset.val);
					return;
				}
				store1 = getPreviousCallResult();
			}
			VMValue index = store0, whatSet = store1;
			if (index.type != Type.integerType)
				throw new VMUserCodeException(statement, Type.integerType.typename + " expected, got "
						+ index.type.typename);
			Integer idx = (Integer) index.value;
			if (0 > idx)
				throw new VMUserCodeException(statement, "Index out of bounds");
			if (whatSet.type != var.dec.type)
				throw new VMUserCodeException(statement, var.dec.type + " expected, got " + whatSet.type);
			what.put(idx, whatSet);
		} else if (statement instanceof SetStatement) {
			SetStatement set = (SetStatement) statement;
			VMVariable var = closure.getVariable(machine, set.id);
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, set.val);
				return;
			}
			VMValue result = getPreviousCallResult();
			if (!VMType.instanceofType(result.type, var.dec.type))
				throw new VMUserCodeException(statement, "Incorrect type for set, got " + result.type + ", expected "
						+ var.dec.type);
			var.safeSetValue(result);
		} else if (statement != null)
			throw new VMException(statement, "Unknown statement type " + statement.getClass().getName());
		if (hasPreviousCallResult())
			throw new VMException(statement, "Detected uncollected return result in statement " + statement);
		currentOp++;
		i = 0;
		j = 0;
		k = 0;
		caughtException = null;
		store0 = null;
		store1 = null;
		store2 = null;
		if (currentOp >= statements.size())
			if (type == VMCallFrameType.LOOP)
				currentOp = 0;
			else
				finished = true;
	}

	@Override
	public boolean finished() {
		return finished && caughtException == null;
	}

	@Override
	public void frameInfo(StringBuilder place) {
		place.append("VMCallFrame: {");
		place.append("i: ").append(i).append(", ");
		place.append("j: ").append(j).append(", ");
		place.append("k: ").append(k).append(", ");
		place.append("currentOp: ").append(currentOp).append(", ");
		place.append("statements: ").append(statements.size()).append("}");
	}

	public void setException(VMUserCodeException code) {
		caughtException = code;
	}

	public VMUserCodeException getException() {
		return caughtException;
	}

	public VMUserCodeException getCaughtException() {
		return handledException;
	}
}
