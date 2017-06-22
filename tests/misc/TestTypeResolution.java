package misc;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class TestTypeResolution {

	public static void main(String[] args) {
		HashSet<String> sourcePaths = new HashSet<>(), jarPaths = new HashSet<>();
		getSourcePaths(new File("D:/Projects/TypeResolution_Oracle/src/androidExamples/Android39.java"), sourcePaths, ".java");
		getSourcePaths(new File("D:/Projects/TypeResolution_Oracle/lib"), jarPaths, ".jar");
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(jarPaths.toArray(new String[0]), new String[]{}, new String[]{}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(false);
		
		try {
			parser.createASTs(sourcePaths.toArray(new String[0]), null, new String[0], new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					ast.accept(new ASTVisitor(false) {
						@Override
						public boolean visit(VariableDeclarationFragment node) {
							IVariableBinding vb = node.resolveBinding();
							System.out.println(vb.getType().getTypeDeclaration().getQualifiedName());
							return false;
						}
					});
				}
			}, null);
		} catch (Throwable t) {
		}

	}

	private static void getSourcePaths(File file, HashSet<String> paths, String ext) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				getSourcePaths(sub, paths, ext);
		} else if (ext.equals(getExtension(file.getName())))
			paths.add(file.getAbsolutePath());
	}

	private static Object getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index < 0)
			index = 0;
		return name.substring(index);
	}

}
