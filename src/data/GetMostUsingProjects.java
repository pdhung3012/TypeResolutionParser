package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileUtil;

public class GetMostUsingProjects {
	private static HashMap<String, PrintStream> libps = new HashMap<>();
	
	public static void main(String[] args) throws FileNotFoundException {
		String[] libs = new String[]{"org.apache.commons.", "android.", "com.google.gwt.", "org.hibernate.", "org.joda.time.", "com.thoughtworks.xstream."};
		for (String lib : libs)
			libps.put(lib, new PrintStream(new FileOutputStream("T:/github/repos-5stars-50commits-lib-" + lib + ".csv")));
		File dir = new File("G:/github/repos-5stars-50commits");
		for (File udir : dir.listFiles()) {
			for (File pdir : udir.listFiles()) {
				HashMap<String, Integer> libCount = new HashMap<>();
				for (String lib : libs)
					libCount.put(lib, 0);
				getMostUsingProjects(pdir, libCount);
				for (String lib : libCount.keySet())
					libps.get(lib).println(udir.getName() + "/" + pdir.getName() + "," + libCount.get(lib));
			}
		}
		for (Map.Entry<String, PrintStream> e : libps.entrySet()) {
			e.getValue().flush();
			e.getValue().close();
		}
	}

	private static void getMostUsingProjects(File file, HashMap<String, Integer> counts) {
		if (file.isDirectory()) {
			if (file.getName().equals(".git"))
				return;
			for (File sub : file.listFiles())
				getMostUsingProjects(sub, counts);
		} else if (file.getName().endsWith(".java")) {
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setCompilerOptions(options);
			parser.setSource(FileUtil.getFileContent(file.getAbsolutePath()).toCharArray());
			try {
				CompilationUnit ast = (CompilationUnit) parser.createAST(null);
				if (ast.getPackage() != null && !ast.types().isEmpty() && ast.types().get(0) instanceof TypeDeclaration) {
					HashSet<String> usedLibs = new HashSet<>();
					String pn = ast.getPackage().getName().getFullyQualifiedName();
					check(pn, counts.keySet(), usedLibs);
					for (int i = 0; i < ast.imports().size(); i++) {
						ImportDeclaration id = (ImportDeclaration) ast.imports().get(i);
						pn = id.getName().getFullyQualifiedName();
						check(pn, counts.keySet(), usedLibs);
					}
					for (String lib : usedLibs)
						counts.put(lib, counts.get(lib) + 1);
				}
			} catch (Throwable t) {}
		}
		return;
	}

	private static void check(String pn, Set<String> libs, HashSet<String> usedLibs) {
		for (String lib : libs) {
			if (pn.startsWith(lib)) {
				usedLibs.add(lib);
				break;
			}
		}
	}

}
