package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.expression.Expression;

/**
 * Call flow graph node (segment of statements).
 * 
 * @author AfterLifeLochie
 *
 */
public class CFGraphNode {

	public static class JumpLabel {
		public JumpLabel(Expression e, CFGraphNode n) {
			this.expr = e;
			this.next = n;
		}

		public Expression expr;
		public CFGraphNode next;
	}

	/**
	 * The nodes which lead to this node, directly or indirectly (jump,
	 * expression jump, goto, etc).
	 */
	public ArrayList<CFGraphNode> entry_nodes = new ArrayList<CFGraphNode>();
	/**
	 * The next node. Must always be set to a future node.
	 */
	public CFGraphNode next_node = null;

	/**
	 * The node to jump to if an exception occurs.
	 */
	public CFGraphNode exception_node = null;

	/**
	 * The list of conditional jump nodes. All expressions are evaluated in
	 * order in map, given the jump label (tuple of expr => next block). If no
	 * conditional jumps apply to this block, this list will be empty.
	 */
	public HashMap<Integer, JumpLabel> exit_nodes = new HashMap<Integer, JumpLabel>();

	/**
	 * The list of statements in the block.
	 */
	public StatementList statements = new StatementList();

	public EnumSet<CFGraphNodeFlag> flags;
	public String image;

	public CFGraphNode(CFGraphNode exception_node) {
		this(exception_node, EnumSet.noneOf(CFGraphNodeFlag.class), null);
	}

	public CFGraphNode(CFGraphNode exception_node, EnumSet<CFGraphNodeFlag> flags, String image) {
		this.exception_node = exception_node;
		this.flags = flags;
		this.image = image;
	}
	
	@Override
	public String toString() {
		return "CFGraphNode [" + flags + "]";
	}

	/**
	 * Push a statement onto the block
	 * 
	 * @param statement
	 *            The statement
	 */
	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	/**
	 * Add the block exit, or a conditional block exit.
	 * 
	 * @param destination
	 *            The destination node
	 * @param slot
	 *            The exit slot
	 * @param condition
	 *            The conditional guard
	 */
	public void addExit(CFGraphNode destination, int slot, Expression condition) {
		if (condition == null) {
			if (this.next_node != null)
				throw new IllegalArgumentException("Already set: " + this.next_node.toString());
			this.next_node = destination;
			this.next_node.addEntry(this);
		} else {
			if (this.exit_nodes.containsKey(slot))
				throw new IllegalArgumentException("Cannot define exit node in existing lcond slot.");
			JumpLabel label = new JumpLabel(condition, destination);
			this.exit_nodes.put(slot, label);
			label.next.addEntry(this);
		}
	}

	public boolean hasUnconditionalExit() {
		return next_node != null;
	}

	public void addEntry(CFGraphNode source) {
		if (!this.entry_nodes.contains(source))
			this.entry_nodes.add(source);
	}

}
