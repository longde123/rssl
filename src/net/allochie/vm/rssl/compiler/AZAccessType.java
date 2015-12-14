package net.allochie.vm.rssl.compiler;

public enum AZAccessType {
	/** Any type */
	UNDEFINED,
	/** Register */
	REGISTER,
	/** Constant pool */
	CONSTANT,
	/** Global var */
	GLOBAL,
	/** Pointer ref */
	PTRREF;
}
