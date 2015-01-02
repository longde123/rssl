package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.JASSFile;
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

public class JASSMachine {

	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();

	public HashMap<String, VMVariable> globals = new HashMap<String, VMVariable>();

	public HashMap<String, NativeFuncDef> natives = new HashMap<String, NativeFuncDef>();
	public HashMap<String, Function> funcs = new HashMap<String, Function>();

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
					globals.get(var.name.image).init(this, closure);
				}
			} else if (what instanceof NativeFuncDef) {
				NativeFuncDef nativeFn = (NativeFuncDef) what;
				natives.put(nativeFn.def.id.image, nativeFn);
			} else
				throw new VMException("Unknown definition type " + what.getClass().getName());
		}

		for (Function func : file.funcs)
			funcs.put(func.sig.id.image, func);

		for (VMVariable var : globals.values()) {
			System.out.println(var);
		}
	}

	public VMVariable findGlobal(Identifier identifier) {
		return globals.get(identifier.image);
	}

	public VMValue resolveExpression(VMClosure closure, Expression init) throws VMException {
		if (init instanceof Constant) {
			if (init instanceof BoolConst)
				return new VMValue(((BoolConst) init).identity);
			if (init instanceof IntConstant)
				return new VMValue(((IntConstant) init).identity);
			if (init instanceof RealConst)
				return new VMValue(((RealConst) init).identity);
			if (init instanceof StringConst)
				return new VMValue(((StringConst) init).identity);
			throw new VMException("Unknown constant type " + init.getClass().getName());
		}

		if (init instanceof Expression) {
			if (init instanceof ArrayReferenceExpression) {
				ArrayReferenceExpression expr = (ArrayReferenceExpression) init;

				VMVariable var = closure.getVariable(expr.name);
				if (!var.dec.array)
					throw new VMException("Not an array");
				Object[] what = (Object[]) var.safeValue().value;
				VMValue index = resolveExpression(closure, expr.idx);
				if (index.type != Type.integerType)
					throw new VMException(Type.integerType.typename + " expected, got " + index.type.typename);
				Integer idx = (Integer) index.value;
				if (0 > idx || idx < what.length - 1)
					throw new VMException("Index out of bounds");
				return new VMValue(what[idx]);
			}

			if (init instanceof BinaryOpExpression) {
				BinaryOpExpression expr = (BinaryOpExpression) init;
				VMValue v0 = resolveExpression(closure, expr.lhs);
				VMValue v1 = resolveExpression(closure, expr.rhs);
				Type productType = VMType.findProductType(v0.type, v1.type);
				if (productType == null)
					throw new VMException("Can't perform operations on " + v0.type.typename + " and "
							+ v1.type.typename);
				switch (expr.mode) {
				case ADD:
					if (productType == Type.stringType) {
						String vv0 = v0.asStringType(), vv1 = v1.asStringType();
						return new VMValue(vv0 + vv1);
					}
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 + vv1);
						return what.applyCast(productType);
					}
					throw new VMException("Unknown use of operator + on types " + v0.type + " and " + v1.type);
				case SUB:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 - vv1);
						return what.applyCast(productType);
					}
					throw new VMException("Unknown use of operator - on types " + v0.type + " and " + v1.type);
				case DIV:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 / vv1);
						return what.applyCast(productType);
					}
					throw new VMException("Unknown use of operator / on types " + v0.type + " and " + v1.type);
				case MUL:
					if (productType == Type.integerType || productType == Type.realType) {
						double vv0 = v0.asNumericType(), vv1 = v1.asNumericType();
						VMValue what = new VMValue(vv0 * vv1);
						return what.applyCast(productType);
					}
					throw new VMException("Unknown use of operator * on types " + v0.type + " and " + v1.type);
				case BOOLAND:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						return new VMValue(vv0 && vv1);
					}
					throw new VMException("Unknown use of operator AND on types " + v0.type + " and " + v1.type);
				case BOOLOR:
					if (productType == Type.booleanType) {
						boolean vv0 = v0.asBooleanType(), vv1 = v1.asBooleanType();
						return new VMValue(vv0 || vv1);
					}
					throw new VMException("Unknown use of operator OR on types " + v0.type + " and " + v1.type);
				case EQUALS:
					return new VMValue(VMValue.areValuesEqual(v0, v1));
				case GT:
					throw new VMException("Unknown use of operator < on types " + v0.type + " and " + v1.type);
				case GTEQ:
					throw new VMException("Unknown use of operator <= on types " + v0.type + " and " + v1.type);
				case LT:
					throw new VMException("Unknown use of operator > on types " + v0.type + " and " + v1.type);
				case LTEQ:
					throw new VMException("Unknown use of operator >= on types " + v0.type + " and " + v1.type);
				case NOTEQUALS:
					return new VMValue(!VMValue.areValuesEqual(v0, v1));
				default:
					throw new VMException("Unsupported operator " + expr.mode);
				}
			}

			if (init instanceof FunctionCallExpression) {
				FunctionCallExpression expr = (FunctionCallExpression) init;
			}

			if (init instanceof FunctionReferenceExpression) {
				FunctionReferenceExpression expr = (FunctionReferenceExpression) init;
			}

			if (init instanceof IdentifierReference) {
				IdentifierReference expr = (IdentifierReference) init;
				VMVariable var = closure.getVariable(expr.identifier);
				return var.safeValue();
			}

			if (init instanceof ParenExpression) {
				ParenExpression expr = (ParenExpression) init;
				return resolveExpression(closure, expr.child);
			}

			if (init instanceof UnaryOpExpression) {
				UnaryOpExpression expr = (UnaryOpExpression) init;
			}
			throw new VMException("Unknown expression type " + init.getClass().getName());
		}

		throw new VMException("Unknown object expression type " + init.getClass().getName());
	}

}
