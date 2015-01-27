package net.allochie.vm.rssl.ast.constant;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.parser.ParseException;
import net.allochie.vm.rssl.parser.Token;

public class IntConst extends Constant {

	private static HashMap<Integer, IntConst> map = new HashMap<Integer, IntConst>();

	public final int identity;

	private IntConst(Integer identity) {
		this.identity = identity;
		this.where = new CodePlace("<system>", 0, 0);
	}

	public static IntConst fromToken(Token inttoken, CodePlace place, IntConstType decimal) throws ParseException {
		try {
			Integer val = null;
			switch (decimal) {
			case DECIMAL:
				val = Integer.parseInt(inttoken.image);
				break;
			case HEXADECIMAL:
				val = Integer.parseInt(inttoken.image, 16);
				break;
			case OCTAL:
				val = Integer.parseInt(inttoken.image, 8);
				break;
			case FOURCC:
				char[] blobs = inttoken.image.toCharArray();
				val = (blobs[0] << 24) | (blobs[1] << 16) | (blobs[2] << 8) | blobs[3];
				break;
			}
			if (!map.containsKey(val))
				map.put(val, new IntConst(val));
			return map.get(val);
		} catch (Throwable t) {
			throw new ParseException("Invalid number format at line " + place.line + ", column " + place.column);
		}
	}

	@Override
	public String toString() {
		return Integer.toString(identity);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntConst))
			return false;
		return ((IntConst) o).identity == identity;
	}
}
