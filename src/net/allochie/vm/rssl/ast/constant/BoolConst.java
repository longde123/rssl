package net.allochie.vm.rssl.ast.constant;

public class BoolConst extends Constant {

	public static BoolConst constFalse = new BoolConst(false);
	public static BoolConst constTrue = new BoolConst(true);

	public final boolean identity;

	private BoolConst(boolean identity) {
		this.identity = identity;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BoolConst))
			return false;
		return ((BoolConst) o).identity == identity;
	}

}
