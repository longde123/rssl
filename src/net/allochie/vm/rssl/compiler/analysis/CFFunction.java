package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.Param;
import net.allochie.vm.rssl.ast.ParamInvokeList;
import net.allochie.vm.rssl.ast.ReturnType;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.ast.VarList;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.ast.expression.Expression;
import net.allochie.vm.rssl.ast.statement.ReturnStatement;
import net.allochie.vm.rssl.compiler.RSSLCompiler;
import net.allochie.vm.rssl.compiler.RSSLCompilerException;
import net.allochie.vm.rssl.compiler.analysis.CFGraphNode.JumpLabel;

public class CFFunction {

	public Function src;
	public CFGraphNode atop_exception_frame;
	public CFGraphNode atop_return_frame;

	public ArrayList<CFGraphNode> top_nodes;
	public CFClosure scope;
	public HashMap<CFGraphNode, CFClosure> closures;

	public CFFunction(Function src, CFGraphNode atop_exception_frame, CFGraphNode atop_return_frame,
			ArrayList<CFGraphNode> top_nodes, CFClosure scope, HashMap<CFGraphNode, CFClosure> closures) {
		this.src = src;
		this.atop_exception_frame = atop_exception_frame;
		this.atop_return_frame = atop_return_frame;
		this.top_nodes = top_nodes;
		this.scope = scope;
		this.closures = closures;
	}

	public void rebuildStack() throws RSSLCompilerException {
		for (Param var : src.sig.params)
			if (!scope.existsInLocal(var.name.image))
				scope.createVarInLocal(src.where, var);

		for (VarDec var : src.lvars)
			if (!scope.existsInLocal(var.name.image))
				scope.createVarInLocal(var);
	}

	private void checkAllReturns(RSSLCompiler cmp, ArrayList<CFGraphNode> all_nodes) throws RSSLCompilerException {
		ReturnType expect_type = src.sig.returns;
		ExpressionResolver resolver = new ExpressionResolver(this);
		for (CFGraphNode node : all_nodes) {
			if (node.flags.contains(CFGraphNodeFlag.RETURN)) {
				if (node.equals(atop_return_frame)) {
					// TODO: Default return
				} else {
					// TODO: Return expression
					if (node.statements.size() == 0)
						throw new RSSLCompilerException("Expected return statement.");

					Statement stmt = node.statements.get(node.statements.size() - 1);
					if (stmt == null) {
						throw new RSSLCompilerException("Expected return statement.");
					} else {
						if (!(stmt instanceof ReturnStatement))
							throw new RSSLCompilerException(
									"Expected return as last statement for block with return bit.");
						ReturnStatement rs = (ReturnStatement) stmt;
						CFClosure closure = getClosureForNode(node);
						ExpressionResolution er = resolver.tryResolveExpression(cmp, closure, rs.expression);
						ReturnType rtype = er.getReturnType();
						if (rtype == null || rtype.type == null) {
							if (expect_type != null && expect_type.type != Type.nullType)
								throw new RSSLCompilerException("Can't return null from non-null return function.");
						} else {
							if (expect_type == null || expect_type.type == Type.nullType)
								throw new RSSLCompilerException("Can't return value from null return function.");
							if (expect_type.type != rtype.type)
								throw new RSSLCompilerException("Can't return " + rtype.type.typename
										+ " from function type " + expect_type.type.typename);
						}
					}
				}
			}
		}

	}

	private void checkAllNodesScoped(ArrayList<CFGraphNode> all_nodes) throws RSSLCompilerException {
		if (all_nodes.size() != closures.size())
			throw new RSSLCompilerException("Could not reach all code paths, got " + all_nodes.size() + " expected "
					+ closures.size());
		for (CFGraphNode node : all_nodes)
			if (!closures.containsKey(node))
				throw new RSSLCompilerException("Missing closure specification for child block.");
	}

	public CFClosure getClosureForNode(CFGraphNode node) {
		return closures.get(node);
	}

	public void allNodes(ArrayList<CFGraphNode> list, CFGraphNode top) {
		if (!list.contains(top)) {
			list.add(top);
			if (top.exit_nodes != null && top.exit_nodes.size() != 0) {
				for (int i = 0; i < top.exit_nodes.size(); i++)
					allNodes(list, top.exit_nodes.get(i).next);
			}
			if (top.next_node != null)
				allNodes(list, top.next_node);
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("CFFunction [");
		b.append(this.src.sig.id.image).append("(");
		b.append(this.src.sig.params.toString()).append("):");
		b.append(this.src.sig.returns.toString()).append(" @");
		b.append(this.src.where.toString()).append("]");
		return b.toString();
	}

	public void dump() {
		CFGraphNode entry = top_nodes.get(0);
		System.out.println("function_" + entry.hashCode() + " [label=\"<f0>Function `" + src.sig.id.image + "`\"];");
		System.out.println("function_" + entry.hashCode() + " -> node_" + entry.hashCode());
		System.out.println(recurseTree(entry, new ArrayList<String>()));
	}

	private String recurseTree(CFGraphNode node, ArrayList<String> mu_table) {
		StringBuilder d0 = new StringBuilder();
		int qq = node.hashCode();

		StringBuilder flags = new StringBuilder();
		for (CFGraphNodeFlag flag : node.flags)
			flags.append(flag.toString().substring(0, 1));

		if (node.image == null) {
			emit(d0,
					"node_" + qq + " [label=\"<f0>Chunk " + qq + "|<f1>" + node.statements.size() + "|<f2>"
							+ flags.toString() + "\"];");
		} else {
			emit(d0, "node_" + qq + " [label=\"<f0>" + node.image + " " + qq + "|<f1>" + node.statements.size()
					+ "|<f2>" + flags.toString() + "\"];");
		}
		mu_table.add("node_" + qq);

		if (node.exit_nodes != null && node.exit_nodes.size() != 0) {

			for (int i = 0; i < node.exit_nodes.size(); i++) {
				JumpLabel l0 = node.exit_nodes.get(i);

				if (i == 0)
					emit(d0, "node_" + qq + ":f0 -> node_" + l0.expr.hashCode() + ":f0;");
				emit(d0, "node_" + l0.expr.hashCode() + " [label=\"<f0>Cond " + l0.expr.hashCode() + "|<f1>T|<f2>F\"];");

				emit(d0, "node_" + l0.expr.hashCode() + ":f1 -> node_" + l0.next.hashCode() + ":f0;");
				if (!mu_table.contains("node_" + l0.next.hashCode()))
					d0.append(recurseTree(l0.next, mu_table));

				if (i == node.exit_nodes.size() - 1) {
					emit(d0, "node_" + l0.expr.hashCode() + ":f2 -> node_" + node.next_node.hashCode() + ":f0;");
					if (!mu_table.contains("node_" + node.next_node.hashCode()))
						d0.append(recurseTree(node.next_node, mu_table));
				} else {
					emit(d0, "node_" + l0.expr.hashCode() + ":f2 -> node_" + node.exit_nodes.get(i + 1).expr.hashCode()
							+ ":f0;");
				}
			}

		} else {
			if (node.next_node != null) {
				emit(d0, "node_" + qq + ":f0 -> node_" + node.next_node.hashCode() + ":f0;");
				if (!mu_table.contains("node_" + node.next_node.hashCode()))
					d0.append(recurseTree(node.next_node, mu_table));
			}
		}

		return d0.toString();
	}

	private void emit(StringBuilder b0, String data) {
		b0.append(data);
		b0.append("\r\n");
	}

	public void compile(RSSLCompiler cmp, CFClosure closure) throws RSSLCompilerException {
		ArrayList<CFGraphNode> alist = new ArrayList<>();
		for (CFGraphNode elem : this.top_nodes)
			allNodes(alist, elem);
		checkAllNodesScoped(alist);
		checkAllReturns(cmp, alist);
	}

	public void checkCanInvoke(RSSLCompiler cmp, CFClosure scope, ParamInvokeList params) throws RSSLCompilerException {
		ExpressionResolver resolver = new ExpressionResolver(this);
		if (src.sig.params.size() != params.size())
			throw new RSSLCompilerException("Incorrect parameter count: got " + params.size() + ", expect "
					+ src.sig.params.size());
		for (int x = 0; x < params.size(); x++) {
			ExpressionResolution future = resolver.tryResolveExpression(cmp, scope, params.get(x));
			if (future.known_return_type.type != src.sig.params.get(x).type
					|| future.known_return_type.array != src.sig.params.get(x).array)
				throw new RSSLCompilerException("Incorrect param type " + x + ": got " + future.known_return_type.type
						+ ", expect " + src.sig.params.get(x).type);
		}
	}
}
