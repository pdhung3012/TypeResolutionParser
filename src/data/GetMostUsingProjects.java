package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
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
				HashMap<String, Integer> libFileCount = new HashMap<>(), libImportCount = new HashMap<>(), libPackageCount = new HashMap<>();
				for (String lib : libs) {
					libFileCount.put(lib, 0);
					libImportCount.put(lib, 0);
					libPackageCount.put(lib, 0);
				}
				getMostUsingProjects(pdir, libFileCount, libImportCount, libPackageCount);
				for (String lib : libFileCount.keySet())
					libps.get(lib).println(udir.getName() + "/" + pdir.getName() 
					+ "," + libFileCount.get(lib)
					+ "," + libImportCount.get(lib)
					+ "," + libPackageCount.get(lib));
			}
		}
		for (Map.Entry<String, PrintStream> e : libps.entrySet()) {
			e.getValue().flush();
			e.getValue().close();
		}
	}

	private static void getMostUsingProjects(File file, HashMap<String, Integer> libFileCount, HashMap<String, Integer> libImportCount, HashMap<String, Integer> libPackageCount) {
		if (file.isDirectory()) {
			if (file.getName().equals(".git"))
				return;
			for (File sub : file.listFiles())
				getMostUsingProjects(sub, libFileCount, libImportCount, libPackageCount);
		} else if (file.getName().endsWith(".java")) {
			String content = FileUtil.getFileContent(file.getAbsolutePath());
			HashSet<String> usedLibs = new HashSet<>();
			check(content, libFileCount.keySet(), usedLibs, libImportCount, libPackageCount);
			for (String lib : usedLibs)
				libFileCount.put(lib, libFileCount.get(lib) + 1);
//			Map options = JavaCore.getOptions();
//			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
//			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
//			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
//			ASTParser parser = ASTParser.newParser(AST.JLS8);
//			parser.setCompilerOptions(options);
//			parser.setSource(content.toCharArray());
//			try {
//				CompilationUnit ast = (CompilationUnit) parser.createAST(null);
//				if (ast.getPackage() != null && !ast.types().isEmpty() && ast.types().get(0) instanceof TypeDeclaration) {
//					String pn = ast.getPackage().getName().getFullyQualifiedName();
//					check(pn, libFileCount.keySet(), usedLibs, libPackageCount);
//					for (int i = 0; i < ast.imports().size(); i++) {
//						ImportDeclaration id = (ImportDeclaration) ast.imports().get(i);
//						pn = id.getName().getFullyQualifiedName();
//						check(pn, libFileCount.keySet(), usedLibs, libImportCount);
//					}
//					for (String lib : usedLibs)
//						libFileCount.put(lib, libFileCount.get(lib) + 1);
//				}
//			} catch (Throwable t) {}
		}
		return;
	}

	private static void check(String content, Set<String> libs, HashSet<String> usedLibs, HashMap<String, Integer> libImportCount, HashMap<String, Integer> libPackageCount) {
		Scanner sc = new Scanner(content);
		boolean seen = false, hasPackage = false;
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (line.startsWith("package ") && line.endsWith(";")) {
				check(line.substring("package ".length()), libs, usedLibs, libPackageCount);
				hasPackage = true;
			}
			if (hasPackage) {
				if (line.startsWith("import ") && line.endsWith(";")) {
					seen = true;
					check(line.substring("import ".length()), libs, usedLibs, libImportCount);
				}
				else if (!line.isEmpty()) {
					if (seen)
						break;
				}
			}
		}
		sc.close();
	}

	private static void check(String pn, Set<String> libs, HashSet<String> usedLibs, HashMap<String, Integer> libCount) {
		for (String lib : libs) {
			if (pn.startsWith(lib)) {
				usedLibs.add(lib);
				libCount.put(lib, libCount.get(lib) + 1);
				break;
			}
		}
	}

}
