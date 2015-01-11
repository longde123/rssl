package net.allochie.vm.rssl;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.statement.CallStatement;
import net.allochie.vm.rssl.ast.statement.ConditionalStatement;
import net.allochie.vm.rssl.ast.statement.LoopExitStatement;
import net.allochie.vm.rssl.ast.statement.LoopStatement;
import net.allochie.vm.rssl.ast.statement.ReturnStatement;
import net.allochie.vm.rssl.ast.statement.SetArrayStatement;
import net.allochie.vm.rssl.ast.statement.SetStatement;
import net.allochie.vm.rssl.ast.statement.TryCatchStatement;

public class VMCallFrame extends VMStackFrame {

	/** List of working statements */
	public final StatementList statements;
	/** The working closure */
	public final VMClosure closure;
	/** Is this a function? */
	public final boolean isFunc;
	/** Is this a loop? */
	public final boolean isLoop;
	/** Is this frame an exception handler? */
	public boolean isExceptionHandler;
	/** The current operation index */
	public int currentOp;
	/** If the frame has finished working */
	public boolean finished;
	/** The call parameters */
	public VMValue[] args;

	protected VMValue store0, store1;
	protected VMValue store2[];
	protected int i, j, k;
	protected VMUserCodeException caught;

	public VMCallFrame(VMClosure closure, StatementList statements, boolean loop, boolean catcher) {
		this.closure = closure;
		this.statements = statements;
		isFunc = false;
		isLoop = loop;
		isExceptionHandler = catcher;
	}

	public VMCallFrame(VMClosure closure, StatementList statements, VMValue[] args) {
		this.closure = closure;
		this.statements = statements;
		this.args = args;
		isFunc = true;
		isLoop = false;
		isExceptionHandler = false;
	}

	@Override
	public void step(RSSLMachine machine, RSSLThread thread) throws VMException {
		machine.debugger.trace("vmCallFrame.step", this, thread);
		if (finished)
			throw new VMException(this, "Cannot advance finished call frame");
		Statement statement = null;
		if (statements.size() != 0)
			statement = statements.get(currentOp);
		workPlace = statement.where;
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
			if (!isLoop)
				throw new VMUserCodeException(statement, "Cannot use exitwhen outside a loop");
			if (!hasPreviousCallResult()) {
				thread.resolveExpression(closure, exit.conditional);
				return;
			}
			VMValue state = getPreviousCallResult();
			if (state.type != Type.booleanType)
				throw new VMUserCodeException(statement, "Cannot leave loop on non-boolean");
			if (state.asBooleanType())
				finished = true;
		} else if (statement instanceof LoopStatement) {
			LoopStatement loop = (LoopStatement) statement;
			thread.requestCall(closure, loop);
		} else if (statement instanceof TryCatchStatement) {
			TryCatchStatement tryBlock = (TryCatchStatement) statement;
			isExceptionHandler = true;
			if (j == 0) {
				thread.requestCall(closure, tryBlock.statements, false, false);
				j++;
				return;
			} else {
				isExceptionHandler = false;
				if (caught != null && k == 0) {
					thread.requestCall(closure, tryBlock.catchStatements, false, false);
					k++;
					return;
				}
			}
		} else if (statement instanceof ReturnStatement) {
			ReturnStatement retn = (ReturnStatement) statement;
			if (retn.expression != null) {
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, retn.expression);
					return;
				}
				result = getPreviousCallResult();
			}
			finished = true;
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
		caught = null;
		store0 = null;
		store1 = null;
		store2 = null;
		if (currentOp >= statements.size())
			if (isLoop)
				currentOp = 0;
			else
				finished = true;
	}

	@Override
	public boolean finished() {
		return finished;
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
		this.caught = code;
	}

	public VMUserCodeException getException() {
		return caught;
	}
}
