package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.VMClosure;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMValue;
import net.allochie.vm.jass.ast.Type;

public class BinaryOpExpression extends Expression {

	/** The left hand side expression */
	public Expression lhs;
	/** The operation mode */
	public BinaryOp mode;
	/** The right hand side expression */
	public Expression rhs;

	@Override
	public String toString() {
		return "BinaryOpExpression: " + lhs + " " + mode + " " + rhs;
	}

	@Override
	public VMValue resolve(VMClosure closure) throws VMException {
		VMValue v0 = lhs.resolve(closure);
		VMValue v1 = rhs.resolve(closure);
		Type productType = Type.findProductType(v0.type, v1.type);
		if (productType == null)
			throw new VMException("Can't perform operations on " + v0.type.typename + " and " + v1.type.typename);
		VMValue product = new VMValue();
		product.type = productType;
		switch (mode) {
		case ADD:
			break;
		case BOOLAND:
			break;
		case BOOLOR:
			break;
		case DIV:
			break;
		case EQUALS:
			break;
		case GT:
			break;
		case GTEQ:
			break;
		case LT:
			break;
		case LTEQ:
			break;
		case MUL:
			break;
		case NOTEQUALS:
			break;
		case SUB:
			break;
		default:
			break;

		}

	}

}
