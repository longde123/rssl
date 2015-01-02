package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Type;
import net.allochie.vm.jass.ast.constant.BoolConst;
import net.allochie.vm.jass.ast.constant.Constant;
import net.allochie.vm.jass.ast.constant.IntConstant;
import net.allochie.vm.jass.ast.constant.RealConst;
import net.allochie.vm.jass.ast.constant.StringConst;
import net.allochie.vm.jass.ast.expression.ArrayReferenceExpression;
import net.allochie.vm.jass.ast.expression.BinaryOpExpression;
import net.allochie.vm.jass.ast.expression.Expression;
import net.allochie.vm.jass.ast.expression.FunctionCallExpression;
import net.allochie.vm.jass.ast.expression.FunctionReferenceExpression;
import net.allochie.vm.jass.ast.expression.IdentifierReference;
import net.allochie.vm.jass.ast.expression.ParenExpression;
import net.allochie.vm.jass.ast.expression.UnaryOpExpression;

public class VMExpressionCallFrame extends VMCallFrame {

	private Expression expression;

	public VMExpressionCallFrame(VMClosure closure, Expression expression) {
		super(closure, null, false);
	}

	@Override
	public void step(JASSMachine machine) throws VMException {
		if (expression instanceof Constant) {
			if (expression instanceof BoolConst)
				result = new VMValue(((BoolConst) expression).identity);
			else if (expression instanceof IntConstant)
				result = new VMValue(((IntConstant) expression).identity);
			else if (expression instanceof RealConst)
				result = new VMValue(((RealConst) expression).identity);
			else if (expression instanceof StringConst)
				result = new VMValue(((StringConst) expression).identity);
			else
				throw new VMException("Unknown constant type " + expression.getClass().getName());
		} else if (expression instanceof Expression) {
			if (expression instanceof ArrayReferenceExpression) {
				ArrayReferenceExpression expr = (ArrayReferenceExpression) expression;
				VMVariable var = closure.getVariable(expr.name);
				if (!var.dec.array)
					throw new VMException("Not an array");
				Object[] what = (Object[]) var.safeValue().value;
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, expr.idx);
					return;
				}
				VMValue index = getPreviousCallResult();
				if (index.type != Type.integerType)
					throw new VMException(Type.integerType.typename + " expected, got " + index.type.typename);
				Integer idx = (Integer) index.value;
				if (0 > idx || idx < what.length - 1)
					throw new VMException("Index out of bounds");
				result = new VMValue(what[idx]);
			} else if (expression instanceof BinaryOpExpression) {
				BinaryOpExpression expr = (BinaryOpExpression) expression;
				if (store0 == null) {
					if (!hasPreviousCallResult()) {
						machine.resolveExpression(closure, expr.lhs);
						return;
					}
					store0 = getPreviousCallResult();
				}
				if (store1 == null) {
					if (!hasPreviousCallResult()) {
						machine.resolveExpression(closure, expr.rhs);
						return;
					}
					store1 = getPreviousCallResult();
				}
				VMValue v0 = store0, v1 = store1;
				Type productType = VMType.findProductType(v0.type, v1.type);
				if (productType == null)
					throw new VMException("Can't perform operations on " + v0.type.typename + " and "
							+ v1.type.typename);
				switch (expr.mode) {
				case ADD:
					if (productType == Type.stringType) {
						String vv0 = v0.asStringType(), vv1 = v1.asStringType();
						result = new VMValue(vv0 + vv1);
					} else if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 + vv1);
						result = what.applyCast(productType);
					} else
						throw new VMException("Unknown use of operator + on types " + v0.type + " and " + v1.type);
					break;
				case SUB:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 - vv1);
						result = what.applyCast(productType);
					} else
						throw new VMException("Unknown use of operator - on types " + v0.type + " and " + v1.type);
					break;
				case DIV:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 / vv1);
						result = what.applyCast(productType);
					} else
						throw new VMException("Unknown use of operator / on types " + v0.type + " and " + v1.type);
					break;
				case MUL:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 * vv1);
						result = what.applyCast(productType);
					} else
						throw new VMException("Unknown use of operator * on types " + v0.type + " and " + v1.type);
					break;
				case BOOLAND:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						result = new VMValue(vv0 && vv1);
					} else
						throw new VMException("Unknown use of operator AND on types " + v0.type + " and " + v1.type);
					break;
				case BOOLOR:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						result = new VMValue(vv0 || vv1);
					} else
						throw new VMException("Unknown use of operator OR on types " + v0.type + " and " + v1.type);
					break;
				case EQUALS:
					result = new VMValue(VMValue.areValuesEqual(v0, v1));
				case GT:
					throw new VMException("Unknown use of operator < on types " + v0.type + " and " + v1.type);
				case GTEQ:
					throw new VMException("Unknown use of operator <= on types " + v0.type + " and " + v1.type);
				case LT:
					throw new VMException("Unknown use of operator > on types " + v0.type + " and " + v1.type);
				case LTEQ:
					throw new VMException("Unknown use of operator >= on types " + v0.type + " and " + v1.type);
				case NOTEQUALS:
					result = new VMValue(!VMValue.areValuesEqual(v0, v1));
					break;
				default:
					throw new VMException("Unsupported operator " + expr.mode);
				}
			} else if (expression instanceof FunctionCallExpression) {
				FunctionCallExpression expr = (FunctionCallExpression) expression;
				VMFunction function = machine.findFunction(expr.name);
				int numParams = function.sig.params.size();
				if (numParams != expr.params.size())
					throw new VMException("Incorrect number of parameters for function call");
				VMValue[] fparams = new VMValue[numParams];
				while (j < numParams) {
					if (!hasPreviousCallResult()) {
						machine.resolveExpression(closure, expr.params.get(j));
						return;
					}
					fparams[j] = getPreviousCallResult();
					if (function.sig.params.get(j).type != fparams[j].type)
						throw new VMException("Incorrect parameter type for function call");
					j++;
				}
				machine.requestCall(closure, function, args);
			} else if (expression instanceof FunctionReferenceExpression) {
				FunctionReferenceExpression expr = (FunctionReferenceExpression) expression;
			} else if (expression instanceof IdentifierReference) {
				IdentifierReference expr = (IdentifierReference) expression;
				VMVariable var = closure.getVariable(expr.identifier);
				result = var.safeValue();
			} else if (expression instanceof ParenExpression) {
				ParenExpression expr = (ParenExpression) expression;
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, expr.child);
					return;
				}
				result = getPreviousCallResult();
			} else if (expression instanceof UnaryOpExpression) {
				UnaryOpExpression expr = (UnaryOpExpression) expression;
				if (!hasPreviousCallResult()) {
					machine.resolveExpression(closure, expr.rhs);
					return;
				}
				VMValue v0 = getPreviousCallResult();
				switch (expr.mode) {
				case POS:
					if (v0.type == Type.integerType || v0.type == Type.realType) {
						VMValue what = new VMValue(Math.abs(v0.asNumericType()));
						result = what.applyCast(v0.type);
					} else
						throw new VMException("Unknown use of unary + on type " + v0.type);
					break;
				case NEG:
					if (v0.type == Type.integerType || v0.type == Type.realType) {
						VMValue what = new VMValue(-Math.abs(v0.asNumericType()));
						result = what.applyCast(v0.type);
					} else
						throw new VMException("Unknown use of unary - on type " + v0.type);
					break;
				case NOT:
					if (v0.type == Type.booleanType) {
						VMValue what = new VMValue(!v0.asBooleanType());
						result = what.applyCast(v0.type);
					} else
						throw new VMException("Unknown use of unary - on type " + v0.type);
					break;
				default:
					throw new VMException("Unsupported operator " + expr.mode);

				}
			} else
				throw new VMException("Unknown expression type " + expression.getClass().getName());
		}

		if (result != null)
			finished = true;
		else
			throw new VMException("Unknown object expression type " + expression.getClass().getName());
	}
}
