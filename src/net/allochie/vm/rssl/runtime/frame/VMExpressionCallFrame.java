package net.allochie.vm.rssl.runtime.frame;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.constant.BoolConst;
import net.allochie.vm.rssl.ast.constant.Constant;
import net.allochie.vm.rssl.ast.constant.IntConst;
import net.allochie.vm.rssl.ast.constant.RealConst;
import net.allochie.vm.rssl.ast.constant.StringConst;
import net.allochie.vm.rssl.ast.expression.ArrayReferenceExpression;
import net.allochie.vm.rssl.ast.expression.BinaryOpExpression;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.ast.expression.FunctionCallExpression;
import net.allochie.vm.rssl.ast.expression.FunctionReferenceExpression;
import net.allochie.vm.rssl.ast.expression.IdentifierReference;
import net.allochie.vm.rssl.ast.expression.ParenExpression;
import net.allochie.vm.rssl.ast.expression.UnaryOpExpression;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMClosure;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.VMUserCodeException;
import net.allochie.vm.rssl.runtime.value.VMFunction;
import net.allochie.vm.rssl.runtime.value.VMFunctionPointer;
import net.allochie.vm.rssl.runtime.value.VMType;
import net.allochie.vm.rssl.runtime.value.VMValue;
import net.allochie.vm.rssl.runtime.value.VMVariable;

public class VMExpressionCallFrame extends VMCallFrame {

	private Expression expression;

	public VMExpressionCallFrame(VMClosure closure, Expression expression) throws VMException {
		super(closure, expression.where, null, VMCallFrameType.EXPRESSION);
		this.expression = expression;
	}

	@Override
	public void step(RSSLMachine machine, RSSLThread thread) throws VMException {
		if (getException() != null)
			throw getException();
		machine.debugger.trace("vmExpressionCallFrame.step", this, thread);
		if (expression instanceof Constant) {
			if (expression instanceof BoolConst)
				result = new VMValue(machine, ((BoolConst) expression).identity);
			else if (expression instanceof IntConst)
				result = new VMValue(machine, ((IntConst) expression).identity);
			else if (expression instanceof RealConst)
				result = new VMValue(machine, ((RealConst) expression).identity);
			else if (expression instanceof StringConst)
				result = new VMValue(machine, ((StringConst) expression).identity);
			else
				throw new VMUserCodeException(expression, "Unknown constant type " + expression.getClass().getName());
		} else if (expression instanceof Expression)
			if (expression instanceof ArrayReferenceExpression) {
				ArrayReferenceExpression expr = (ArrayReferenceExpression) expression;
				VMVariable var = closure.getVariable(machine, expr.name);
				if (!var.dec.array)
					throw new VMUserCodeException(expression, "Not an array");
				if (!var.defined())
					throw new VMUserCodeException(expression, "Attempt to access undefined variable "
							+ var.dec.name.image);
				HashMap<Integer, VMValue> what = var.safeValue().asArrayType();
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, expr.idx);
					return;
				}
				VMValue index = getPreviousCallResult();
				if (index.type != Type.integerType)
					throw new VMUserCodeException(expression, Type.integerType.typename + " expected, got "
							+ index.type.typename);
				Integer idx = (Integer) index.value;
				if (0 > idx || idx > what.size() - 1)
					throw new VMUserCodeException(expression, "Index out of bounds: got " + idx + ", min 0, max "
							+ what.size());
				result = what.get(idx);
			} else if (expression instanceof BinaryOpExpression) {
				BinaryOpExpression expr = (BinaryOpExpression) expression;
				if (store0 == null) {
					if (!hasPreviousCallResult()) {
						thread.resolveExpression(closure, expr.lhs);
						return;
					}
					store0 = getPreviousCallResult();
				}
				if (store1 == null) {
					if (!hasPreviousCallResult()) {
						thread.resolveExpression(closure, expr.rhs);
						return;
					}
					store1 = getPreviousCallResult();
				}
				VMValue v0 = store0, v1 = store1;
				Type productType = VMType.findProductType(v0.type, v1.type);
				if (productType == null)
					throw new VMUserCodeException(expression, "Can't perform operations on " + v0.type.typename
							+ " and " + v1.type.typename);
				switch (expr.mode) {
				case ADD:
					if (productType == Type.stringType) {
						String vv0 = v0.asStringType(), vv1 = v1.asStringType();
						result = new VMValue(machine, vv0 + vv1);
					} else if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 + vv1);
						result = what.applyCast(machine, productType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator + on types " + v0.type
								+ " and " + v1.type);
					break;
				case SUB:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 - vv1);
						result = what.applyCast(machine, productType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator - on types " + v0.type
								+ " and " + v1.type);
					break;
				case DIV:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 / vv1);
						result = what.applyCast(machine, productType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator / on types " + v0.type
								+ " and " + v1.type);
					break;
				case MUL:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 * vv1);
						result = what.applyCast(machine, productType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator * on types " + v0.type
								+ " and " + v1.type);
					break;
				case BOOLAND:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						result = new VMValue(machine, vv0 && vv1);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator AND on types " + v0.type
								+ " and " + v1.type);
					break;
				case BOOLOR:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						result = new VMValue(machine, vv0 || vv1);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator OR on types " + v0.type
								+ " and " + v1.type);
					break;
				case EQUALS:
					result = new VMValue(machine, VMValue.areValuesEqual(v0, v1));
					break;
				case GT:
					if (VMType.isTypeNumeric(v0.type) && VMType.isTypeNumeric(v1.type)) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 < vv1);
						result = what.applyCast(machine, Type.booleanType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator < on types " + v0.type
								+ " and " + v1.type);
					break;
				case GTEQ:
					if (VMType.isTypeNumeric(v0.type) && VMType.isTypeNumeric(v1.type)) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 <= vv1);
						result = what.applyCast(machine, Type.booleanType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator <= on types " + v0.type
								+ " and " + v1.type);
					break;
				case LT:
					if (VMType.isTypeNumeric(v0.type) && VMType.isTypeNumeric(v1.type)) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 > vv1);
						result = what.applyCast(machine, Type.booleanType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator > on types " + v0.type
								+ " and " + v1.type);
					break;
				case LTEQ:
					if (VMType.isTypeNumeric(v0.type) && VMType.isTypeNumeric(v1.type)) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(machine, vv0 >= vv1);
						result = what.applyCast(machine, Type.booleanType);
					} else
						throw new VMUserCodeException(expression, "Unknown use of operator >= on types " + v0.type
								+ " and " + v1.type);
					break;
				case NOTEQUALS:
					result = new VMValue(machine, !VMValue.areValuesEqual(v0, v1));
					break;
				default:
					throw new VMException(expr, "Unsupported operator " + expr.mode);
				}
			} else if (expression instanceof FunctionCallExpression) {
				FunctionCallExpression expr = (FunctionCallExpression) expression;
				VMFunction function = machine.findFunction(expr.name);
				if (function == null)
					throw new VMUserCodeException(expression, "Cannot call undefined function " + expr.name);
				int numParams = function.sig.params.size();
				int providedParams = (expr.params != null) ? expr.params.size() : 0;
				if (numParams != providedParams)
					throw new VMUserCodeException(expression, "Incorrect number of parameters for function call");
				if (store2 == null)
					store2 = new VMValue[numParams];
				while (j < numParams) {
					if (!hasPreviousCallResult()) {
						thread.resolveExpression(closure, expr.params.get(j));
						return;
					}
					store2[j] = getPreviousCallResult();
					if (!VMType.instanceofType(store2[j].type, function.sig.params.get(j).type))
						throw new VMUserCodeException(expression,
								"Incorrect parameter type for function call, expected "
										+ function.sig.params.get(j).type + ", got " + store2[j].type);
					j++;
				}
				if (i == 0) {
					i++;
					thread.requestCall(closure, function, store2);
					return;
				}
				result = getPreviousCallResult();
			} else if (expression instanceof IdentifierReference) {
				IdentifierReference expr = (IdentifierReference) expression;
				VMVariable var = closure.getVariable(machine, expr.identifier);
				if (!var.defined())
					throw new VMUserCodeException(expression, "Attempt to access undefined variable "
							+ var.dec.name.image);
				result = var.safeValue();
			} else if (expression instanceof ParenExpression) {
				ParenExpression expr = (ParenExpression) expression;
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, expr.child);
					return;
				}
				result = getPreviousCallResult();
			} else if (expression instanceof UnaryOpExpression) {
				UnaryOpExpression expr = (UnaryOpExpression) expression;
				if (!hasPreviousCallResult()) {
					thread.resolveExpression(closure, expr.rhs);
					return;
				}
				VMValue v0 = getPreviousCallResult();
				switch (expr.mode) {
				case POS:
					if (v0.type == Type.integerType || v0.type == Type.realType) {
						VMValue what = new VMValue(machine, Math.abs(v0.asNumericType()));
						result = what.applyCast(machine, v0.type);
					} else
						throw new VMUserCodeException(expression, "Unknown use of unary + on type " + v0.type);
					break;
				case NEG:
					if (v0.type == Type.integerType || v0.type == Type.realType) {
						VMValue what = new VMValue(machine, -Math.abs(v0.asNumericType()));
						result = what.applyCast(machine, v0.type);
					} else
						throw new VMUserCodeException(expression, "Unknown use of unary - on type " + v0.type);
					break;
				case NOT:
					if (v0.type == Type.booleanType) {
						VMValue what = new VMValue(machine, !v0.asBooleanType());
						result = what.applyCast(machine, v0.type);
					} else
						throw new VMUserCodeException(expression, "Unknown use of unary - on type " + v0.type);
					break;
				default:
					throw new VMException(expr, "Unsupported operator " + expr.mode);

				}
			} else if (expression instanceof FunctionReferenceExpression) {
				FunctionReferenceExpression expr = (FunctionReferenceExpression) expression;
				VMFunction what = machine.findFunction(expr.name);
				VMFunctionPointer pointer = new VMFunctionPointer(what);
				result = new VMValue(machine, pointer);
			} else
				throw new VMException(expression, "Unknown expression type " + expression.getClass().getName());

		if (result != null)
			finished = true;
		else
			throw new VMException(expression, "Unknown object expression type " + expression.getClass().getName());
	}

	@Override
	public String dumpFrame() {
		StringBuilder frameInfo = new StringBuilder();
		frameInfo.append("VMExpressionCallFrame: {");
		frameInfo.append("i: ").append(i).append(", ");
		frameInfo.append("j: ").append(j).append(", ");
		frameInfo.append("k: ").append(k).append(", ");
		frameInfo.append("expression: ").append(expression).append("}");
		return frameInfo.toString();
	}
}
