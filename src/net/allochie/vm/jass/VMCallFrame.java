package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;
import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.statement.CallStatement;
import net.allochie.vm.jass.ast.statement.ConditionalStatement;
import net.allochie.vm.jass.ast.statement.LoopExitStatement;
import net.allochie.vm.jass.ast.statement.LoopStatement;
import net.allochie.vm.jass.ast.statement.ReturnStatement;
import net.allochie.vm.jass.ast.statement.SetArrayStatement;
import net.allochie.vm.jass.ast.statement.SetStatement;

public class VMCallFrame {

	/** List of working statements */
	public final StatementList statements;
	/** The working closure */
	public final VMClosure closure;
	/** Is this a function? */
	public final boolean isFunc;
	/** Is this a loop? */
	public final boolean isLoop;
	/** The current operation index */
	public int currentOp;
	/** If the frame has finished working */
	public boolean finished;
	/** The call parameters */
	public VMValue[] args;
	/** The resultant value */
	public VMValue result;
	/** The last call result */
	public VMValue callResult;

	protected VMValue store0, store1, store2;
	protected int i, j, k;

	public VMCallFrame(VMClosure closure, StatementList statements, boolean loop) {
		this.closure = closure;
		this.statements = statements;
		this.isFunc = false;
		this.isLoop = loop;
	}

	public VMCallFrame(VMClosure closure, StatementList statements, VMValue[] args) {
		this.closure = closure;
		this.statements = statements;
		this.args = args;
		this.isFunc = true;
		this.isLoop = false;
	}

	public boolean hasPreviousCallResult() {
		return (callResult != null);
	}

	public VMValue getPreviousCallResult() {
		VMValue rt = callResult;
		callResult = null;
		return rt;
	}

	public void step(JASSMachine machine) throws VMException {
		if (finished)
			throw new VMException("Cannot advance finished call frame");
		Statement statement = statements.get(currentOp);
		if (statement instanceof CallStatement) {
			CallStatement call = (CallStatement) statement;
			VMFunction function = machine.findFunction(call.id);
			if (function == null)
				throw new VMException("Cannot call undefined function " + call.id);
			int numParams = function.sig.params.size();
			if (numParams != call.params.size())
				throw new VMException("Incorrect number of parameters for function call");
			VMValue[] fparams = new VMValue[numParams];
			while (j < numParams) {
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, call.params.get(j));
					return;
				}
				fparams[j] = getPreviousCallResult();
				if (function.sig.params.get(j).type != fparams[j].type)
					throw new VMException("Incorrect parameter type for function call");
				j++;
			}
			machine.requestCall(closure, function, args);
		} else if (statement instanceof ConditionalStatement) {
			ConditionalStatement conditional = (ConditionalStatement) statement;
			while (conditional != null) {
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, conditional.conditional);
					return;
				}
				VMValue state = getPreviousCallResult();
				if (state.type != Type.booleanType)
					throw new VMException("Cannot perform conditional on non-boolean");
				if (state.asBooleanType())
					machine.requestCall(closure, conditional);
				else
					conditional = conditional.child;
			}
		} else if (statement instanceof LoopExitStatement) {
			LoopExitStatement exit = (LoopExitStatement) statement;
			if (!hasPreviousCallResult()) {
				machine.resolveExpression(closure, exit.conditional);
				return;
			}
			VMValue state = getPreviousCallResult();
			if (state.type != Type.booleanType)
				throw new VMException("Cannot leave loop on non-boolean");
			if (state.asBooleanType())
				finished = true;
		} else if (statement instanceof LoopStatement) {
			LoopStatement loop = (LoopStatement) statement;
			machine.requestCall(closure, loop);
		} else if (statement instanceof ReturnStatement) {
			ReturnStatement retn = (ReturnStatement) statement;
			if (retn.expression != null) {
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, retn.expression);
					return;
				}
				result = getPreviousCallResult();
			}
			finished = true;
		} else if (statement instanceof SetArrayStatement) {
			SetArrayStatement arrayset = (SetArrayStatement) statement;
			VMVariable var = closure.getVariable(arrayset.id);
			if (!var.dec.array)
				throw new VMException("Not an array");
			Object[] what = (Object[]) var.safeValue().value;
			if (store0 == null) {
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, arrayset.idx);
					return;
				}
				store0 = getPreviousCallResult();
			}
			if (store1 == null) {
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, arrayset.val);
					return;
				}
				store1 = getPreviousCallResult();
			}
			VMValue index = store0, whatSet = store1;
			if (index.type != Type.integerType)
				throw new VMException(Type.integerType.typename + " expected, got " + index.type.typename);
			Integer idx = (Integer) index.value;
			if (0 > idx || idx < what.length - 1)
				throw new VMException("Index out of bounds");
			if (whatSet.type != var.dec.type)
				throw new VMException(var.dec.type + " expected, got " + whatSet.type);
			what[idx] = whatSet.value;
			var.safeSetValue(new VMValue(what));
		} else if (statement instanceof SetStatement) {
			SetStatement set = (SetStatement) statement;
			VMVariable var = closure.getVariable(set.id);
			if (!hasPreviousCallResult()) {
				machine.resolveExpression(closure, set.val);
				return;
			}
			var.safeSetValue(getPreviousCallResult());
		} else
			throw new VMException("Unknown statement type " + statement.getClass().getName());
		currentOp++;
		i = j = k = 0;
		store0 = store1 = store2 = null;
		if (currentOp == statements.size()) {
			if (isLoop)
				currentOp = 0;
			else
				finished = true;
		}

	}
}
