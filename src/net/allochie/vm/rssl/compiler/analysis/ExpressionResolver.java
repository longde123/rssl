package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;

import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.constant.BoolConst;
import net.allochie.vm.rssl.ast.constant.Constant;
import net.allochie.vm.rssl.ast.constant.IntConst;
import net.allochie.vm.rssl.ast.constant.RealConst;
import net.allochie.vm.rssl.ast.constant.StringConst;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.ast.expression.ArrayReferenceExpression;
import net.allochie.vm.rssl.ast.expression.BinaryOpExpression;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.ast.expression.FunctionCallExpression;
import net.allochie.vm.rssl.ast.expression.FunctionReferenceExpression;
import net.allochie.vm.rssl.ast.expression.IdentifierReference;
import net.allochie.vm.rssl.ast.expression.ParenExpression;
import net.allochie.vm.rssl.ast.expression.UnaryOpExpression;
import net.allochie.vm.rssl.compiler.RSSLCompiler;
import net.allochie.vm.rssl.compiler.RSSLCompilerException;
import net.allochie.vm.rssl.runtime.value.VMType;

public class ExpressionResolver {

	private CFFunction ctx;

	public ExpressionResolver(CFFunction ctx) {
		this.ctx = ctx;
	}

	public ExpressionResolution tryResolveExpression(RSSLCompiler cmp, CFClosure closure, Expression e)
			throws RSSLCompilerException {
		ExpressionResolution er = new ExpressionResolution();
		resolveExpression(cmp, e, closure, er);
		return er;
	}

	private void resolveExpression(RSSLCompiler cmp, Expression e, CFClosure closure, ExpressionResolution er)
			throws RSSLCompilerException {
		if (e instanceof Constant) {
			if (e instanceof BoolConst) {
				er.setValueAndType(((BoolConst) e).identity);
			} else if (e instanceof IntConst) {
				er.setValueAndType(((IntConst) e).identity);
			} else if (e instanceof RealConst) {
				er.setValueAndType(((RealConst) e).identity);
			} else if (e instanceof StringConst) {
				er.setValueAndType(((StringConst) e).identity);
			} else
				throw new RSSLCompilerException("Unable to expand constant " + e.getClass());
		} else {
			if (e instanceof ArrayReferenceExpression) {
				VarDec which = closure.getCheckedInLocal(((ArrayReferenceExpression) e).name.image);
				er.setType(which.type, false);
				er.setExpression(e);
				return;
			} else if (e instanceof BinaryOpExpression) {
				BinaryOpExpression expr = (BinaryOpExpression) e;
				ExpressionResolution alpha = tryResolveExpression(cmp, closure, expr.lhs);
				ExpressionResolution beta = tryResolveExpression(cmp, closure, expr.rhs);
				Type productType = VMType.findProductType(alpha.known_return_type.type, beta.known_return_type.type);
				if (productType == null)
					throw new RSSLCompilerException("Can't perform operations on "
							+ alpha.known_return_type.type.typename + " and " + beta.known_return_type.type.typename);

				if (!alpha.hasConstantSolution() || !beta.hasConstantSolution()) {
					er.children = new ArrayList<ExpressionResolution>();
					er.children.add(alpha);
					er.children.add(beta);
					er.setType(productType, false);
					return;
				}

				switch (expr.mode) {
				case ADD:
					if (productType == Type.stringType) {
						String vv0 = alpha.asStringType(), vv1 = beta.asStringType();
						er.setValueAndType(vv0 + vv1);
					} else if (productType == Type.integerType || productType == Type.realType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 + vv1, productType);
						} else
							er.setType(productType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator + on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case SUB:
					if (productType == Type.integerType || productType == Type.realType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 - vv1, productType);
						} else
							er.setType(productType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator - on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case DIV:
					if (productType == Type.integerType || productType == Type.realType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 / vv1, productType);
						} else
							er.setType(productType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator / on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case MUL:
					if (productType == Type.integerType || productType == Type.realType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 * vv1, productType);
						} else
							er.setType(productType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator * on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case BOOLAND:
					if (productType == Type.booleanType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							boolean vv0 = alpha.asBooleanType(), vv1 = beta.asBooleanType();
							er.setRaw(vv0 && vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator AND on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case BOOLOR:
					if (productType == Type.booleanType) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							boolean vv0 = alpha.asBooleanType(), vv1 = beta.asBooleanType();
							er.setRaw(vv0 || vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator OR on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case EQUALS:
					if (alpha.hasConstantSolution() && beta.hasConstantSolution())
						er.setRaw(alpha.equalsOtherVal(beta), Type.booleanType);
					else
						er.setType(Type.booleanType, false);
					break;
				case GT:
					if (VMType.isTypeNumeric(alpha.known_return_type.type)
							&& VMType.isTypeNumeric(beta.known_return_type.type)) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 < vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator < on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case GTEQ:
					if (VMType.isTypeNumeric(alpha.known_return_type.type)
							&& VMType.isTypeNumeric(beta.known_return_type.type)) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 <= vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator <= on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case LT:
					if (VMType.isTypeNumeric(alpha.known_return_type.type)
							&& VMType.isTypeNumeric(beta.known_return_type.type)) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 > vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator > on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case LTEQ:
					if (VMType.isTypeNumeric(alpha.known_return_type.type)
							&& VMType.isTypeNumeric(beta.known_return_type.type)) {
						if (alpha.hasConstantSolution() && beta.hasConstantSolution()) {
							double vv0 = alpha.asNumericType(), vv1 = beta.asNumericType();
							er.setRaw(vv0 >= vv1, Type.booleanType);
						} else
							er.setType(Type.booleanType, false);
					} else
						throw new RSSLCompilerException("Unknown use of operator >= on types "
								+ alpha.known_return_type.type + " and " + beta.known_return_type.type);
					break;
				case NOTEQUALS:
					if (alpha.hasConstantSolution() && beta.hasConstantSolution())
						er.setRaw(!alpha.equalsOtherVal(beta), Type.booleanType);
					else
						er.setType(Type.booleanType, false);

					break;
				default:
					throw new RSSLCompilerException("Unsupported operator " + expr.mode);
				}
			} else if (e instanceof FunctionCallExpression) {
				cmp.checkFunctionExists(((FunctionCallExpression) e).name.image);
				CFFunction func = cmp.getFunction(((FunctionCallExpression) e).name.image);
				func.checkCanInvoke(cmp, closure, ((FunctionCallExpression) e).params);
				er.setType(func.src.sig.returns.type, func.src.sig.returns.array);
				return;
			} else if (e instanceof FunctionReferenceExpression) {
				cmp.checkFunctionExists(((FunctionReferenceExpression) e).name.image);
				CFFunction func = cmp.getFunction(((FunctionReferenceExpression) e).name.image);
				er.setRaw(func, Type.codeType);
				er.setType(Type.codeType, false);
				return;
			} else if (e instanceof IdentifierReference) {
				VarDec which = closure.getCheckedInLocal(((IdentifierReference) e).identifier.image);
				er.setType(which.type, which.array);
				return;
			} else if (e instanceof ParenExpression) {
				resolveExpression(cmp, ((ParenExpression) e).child, closure, er);
				return;
			} else if (e instanceof UnaryOpExpression) {
				UnaryOpExpression expr = (UnaryOpExpression) e;
				ExpressionResolution val = tryResolveExpression(cmp, closure, expr.rhs);
				switch (expr.mode) {
				case POS:
					if (val.known_return_type.type == Type.integerType || val.known_return_type.type == Type.realType) {
						if (val.hasConstantSolution())
							er.setRaw(Math.abs(val.asNumericType()), val.known_return_type.type);
						else
							er.setType(val.known_return_type.type, false);
					} else
						throw new RSSLCompilerException("Unknown use of unary + on type " + val.known_return_type.type);
					break;
				case NEG:
					if (val.known_return_type.type == Type.integerType || val.known_return_type.type == Type.realType) {
						if (val.hasConstantSolution())
							er.setRaw(-Math.abs(val.asNumericType()), val.known_return_type.type);
						else
							er.setType(val.known_return_type.type, false);
					} else
						throw new RSSLCompilerException("Unknown use of unary - on type " + val.known_return_type.type);
					break;
				case NOT:
					if (val.known_return_type.type == Type.booleanType) {
						if (val.hasConstantSolution())
							er.setRaw(!val.asBooleanType(), val.known_return_type.type);
						else
							er.setType(val.known_return_type.type, false);
					} else
						throw new RSSLCompilerException("Unknown use of unary - on type " + val.known_return_type.type);
					break;
				default:
					throw new RSSLCompilerException("Unsupported operator " + expr.mode);
				}
			} else
				throw new RSSLCompilerException("Unable to expand complex " + e.getClass());
		}
	}
}
