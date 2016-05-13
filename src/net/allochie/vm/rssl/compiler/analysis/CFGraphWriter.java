package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.ast.Statement;
import net.allochie.vm.rssl.ast.StatementList;
import net.allochie.vm.rssl.ast.statement.CallStatement;
import net.allochie.vm.rssl.ast.statement.ConditionalStatement;
import net.allochie.vm.rssl.ast.statement.LoopExitStatement;
import net.allochie.vm.rssl.ast.statement.LoopStatement;
import net.allochie.vm.rssl.ast.statement.RaiseStatement;
import net.allochie.vm.rssl.ast.statement.ReturnStatement;
import net.allochie.vm.rssl.ast.statement.SetArrayStatement;
import net.allochie.vm.rssl.ast.statement.SetStatement;
import net.allochie.vm.rssl.ast.statement.TryCatchStatement;
import net.allochie.vm.rssl.compiler.RSSLCompilerException;

public class CFGraphWriter {

	public CFFunction writeNodesFromFunc(Function funcdef) throws RSSLCompilerException {
		ArrayList<CFGraphNode> nodes = new ArrayList<CFGraphNode>();
		HashMap<CFGraphNode, CFClosure> closures = new HashMap<CFGraphNode, CFClosure>();

		CFGraphNode exception_node = new CFGraphNode(null, EnumSet.of(CFGraphNodeFlag.EXCEPTION), "CEF");
		CFGraphNode exit_node = new CFGraphNode(null, EnumSet.of(CFGraphNodeFlag.RETURN), "NRT");
		CFClosure top = new CFClosure();
		walk(nodes, closures, exit_node, exception_node, top, funcdef.statements);

		return new CFFunction(funcdef, exception_node, exit_node, nodes, top, closures);
	}

	private void walk(ArrayList<CFGraphNode> nodes, HashMap<CFGraphNode, CFClosure> closures, CFGraphNode exit_node,
			CFGraphNode exception_node, CFClosure closure, StatementList statements) throws RSSLCompilerException {
		CFGraphNode node = new CFGraphNode(exception_node);
		nodes.add(node);
		closures.put(node, closure);

		for (Statement statement : statements) {
			if (statement instanceof CallStatement) {
				CallStatement call = (CallStatement) statement;
				CFGraphNode frame = new CFGraphNode(exception_node, EnumSet.of(CFGraphNodeFlag.INVOKE), call.id.image);
				node.addExit(frame, 0, null);
				closures.put(frame, closure);
				node = new CFGraphNode(exception_node);
				closures.put(node, closure);
				frame.addExit(node, 0, null);
			} else if (statement instanceof SetStatement) {
				node.addStatement(statement);
			} else if (statement instanceof SetArrayStatement) {
				node.addStatement(statement);
			} else if (statement instanceof ConditionalStatement) {
				ConditionalStatement cond = (ConditionalStatement) statement;
				ArrayList<CFGraphNode> lasts = new ArrayList<CFGraphNode>();
				CFGraphNode cond_exit_node = new CFGraphNode(exception_node);

				while (cond != null) {
					ArrayList<CFGraphNode> cond_nodes = new ArrayList<CFGraphNode>();
					CFClosure cond_top = new CFClosure(closure);
					walk(cond_nodes, closures, cond_exit_node, exception_node, cond_top, cond.statements);
					CFGraphNode jmp_0 = cond_nodes.get(0), jmp_n = cond_nodes.get(cond_nodes.size() - 1);
					if (jmp_n == cond_exit_node)
						jmp_n = cond_nodes.get(cond_nodes.size() - 2);
					node.addExit(jmp_0, lasts.size(), cond.conditional);
					lasts.add(jmp_n);
					cond = cond.child;
				}

				if (!node.hasUnconditionalExit()) /* no ELSE block */
					node.addExit(cond_exit_node, 0, null);

				node = cond_exit_node;
				nodes.add(node);
				closures.put(node, closure);

				for (CFGraphNode last : lasts)
					if (!last.hasUnconditionalExit())
						last.addExit(node, 0, null);
			} else if (statement instanceof LoopStatement) {
				LoopStatement loop = (LoopStatement) statement;
				ArrayList<CFGraphNode> loop_nodes = new ArrayList<CFGraphNode>();
				CFGraphNode loop_exit = new CFGraphNode(exception_node);
				CFClosure loop_top = new CFClosure(closure);
				walk(loop_nodes, closures, loop_exit, exception_node, loop_top, loop.statements);
				CFGraphNode jmp_0 = loop_nodes.get(0);
				node.addExit(jmp_0, 0, null);
				node = loop_exit;
				nodes.add(node);
				closures.put(node, closure);
			} else if (statement instanceof LoopExitStatement) {
				LoopExitStatement exit = (LoopExitStatement) statement;
				node.addExit(exit_node, 0, exit.conditional);
			} else if (statement instanceof TryCatchStatement) {
				TryCatchStatement tcs = (TryCatchStatement) statement;

				CFGraphNode inner_exception_node = new CFGraphNode(null);
				ArrayList<CFGraphNode> exception_nodes = new ArrayList<CFGraphNode>();
				CFClosure exception_closure = new CFClosure(closure);
				walk(exception_nodes, closures, exit_node, inner_exception_node, exception_closure, tcs.catchStatements);
				CFGraphNode zero = exception_nodes.get(0), last = exception_nodes.get(exception_nodes.size() - 1);

				ArrayList<CFGraphNode> child_nodes = new ArrayList<CFGraphNode>();
				CFClosure child_closure = new CFClosure(closure);
				walk(child_nodes, closures, exit_node, zero, child_closure, tcs.statements);
				CFGraphNode jmp_0 = child_nodes.get(0), jmp_n = child_nodes.get(child_nodes.size() - 1);

				node.addExit(jmp_0, 0, null);
				node = new CFGraphNode(exception_node);
				nodes.add(node);
				closures.put(node, closure);
				last.addExit(node, 0, null);
				jmp_n.addExit(node, 0, null);
			} else if (statement instanceof RaiseStatement) {
				// Potentially unwind the current block
				node.addStatement(statement);
			} else if (statement instanceof ReturnStatement) {
				CFGraphNode exit = new CFGraphNode(exception_node, EnumSet.of(CFGraphNodeFlag.RETURN), null);
				closures.put(exit, closure);
				node.addExit(exit, 0, null);
				exit.addStatement(statement);
			} else {
				throw new RSSLCompilerException("Unsupported statement type " + statement.getClass().getName());
			}
		}

		if (!node.hasUnconditionalExit()) {
			nodes.add(exit_node);
			closures.put(exit_node, closure);
			node.addExit(exit_node, 0, null);
		}
	}
}
