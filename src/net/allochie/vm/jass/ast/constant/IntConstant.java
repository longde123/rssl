package net.allochie.vm.jass.ast.constant;

import java.util.HashMap;

import net.allochie.vm.jass.parser.ParseException;
import net.allochie.vm.jass.parser.Token;

public class IntConstant extends Constant {

	private static HashMap<Integer, IntConstant> map = new HashMap<Integer, IntConstant>();

	public final int identity;

	private IntConstant(Integer identity) {
		this.identity = identity;
	}

	public static IntConstant fromToken(Token inttoken, IntConstType decimal) throws ParseException {
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
				map.put(val, new IntConstant(val));
			return map.get(val);
		} catch (Throwable t) {
			throw new ParseException("Invalid number format");
		}
	}

	@Override
	public String toString() {
		return Integer.toString(identity);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntConstant))
			return false;
		return ((IntConstant) o).identity == identity;
	}
}
