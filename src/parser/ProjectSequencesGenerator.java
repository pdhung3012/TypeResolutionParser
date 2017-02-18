package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import parser.ClassPathUtil.PomFile;
import utils.FileUtil;

public class ProjectSequencesGenerator {
	private static final boolean PARSE_INDIVIDUAL_SRC = false, SCAN_FILES_FRIST = false;
	
	private String inPath, outPath;
	private boolean testing = false;
	private PrintStream stLocations, stSourceSequences, stTargetSequences, stLog;
	private HashSet<String> badFiles = new HashSet<>();
	
	public ProjectSequencesGenerator(String inPath) {
		this.inPath = inPath;
	}
	
	public ProjectSequencesGenerator(String inPath, boolean testing) {
		this(inPath);
		this.testing = testing;
	}

	public int  generateSequences(String outPath) {
		return generateSequences(true, null, outPath);
	}

	public int generateSequences(final boolean keepUnresolvables, final String lib, final String outPath) {
		this.outPath = outPath;
		String[] jarPaths = getJarPaths();
		ArrayList<String> rootPaths = getRootPaths();
		
		new File(outPath).mkdirs();
		try {
			stLocations = new PrintStream(new FileOutputStream(outPath + "/locations.txt"));
			stSourceSequences = new PrintStream(new FileOutputStream(outPath + "/source.txt"));
			stTargetSequences = new PrintStream(new FileOutputStream(outPath + "/target.txt"));
			stLog = new PrintStream(new FileOutputStream(outPath + "/log.txt"));
		} catch (FileNotFoundException e) {
			if (testing)
				System.err.println(e.getMessage());
			return 0;
		}
		int numOfSequences = 0;
		for (String rootPath : rootPaths) {
			String[] sourcePaths = getSourcePaths(rootPath, new String[]{".java"});
			
			@SuppressWarnings("rawtypes")
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setCompilerOptions(options);
			parser.setEnvironment(jarPaths, new String[]{}, new String[]{}, true);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(false);
			
			StatTypeFileASTRequestor r = new StatTypeFileASTRequestor(keepUnresolvables, lib);
			try {
				parser.createASTs(sourcePaths, null, new String[0], r, null);
			} catch (Throwable t) {
				t.printStackTrace(stLog);
				if (testing) {
					System.err.println(t.getMessage());
					t.printStackTrace();
				}
			}
			numOfSequences += r.numOfSequences;
		}
		return numOfSequences;
	}
	
	private class StatTypeFileASTRequestor extends FileASTRequestor {
		int numOfSequences = 0;
		private boolean keepUnresolvables;
		private String lib;
		
		public StatTypeFileASTRequestor(boolean keepUnresolvables, String lib) {
			this.keepUnresolvables = keepUnresolvables;
			this.lib = lib;
		}

		@Override
		public void acceptAST(String sourceFilePath, CompilationUnit ast) {
			if (ast.getPackage() == null)
				return;
			if (lib != null) {
				boolean hasLib = false;
				if (ast.getPackage().getName().getFullyQualifiedName().startsWith(lib))
					hasLib = true;
				if (!hasLib && ast.imports() != null) {
					for (int i = 0; i < ast.imports().size(); i++) {
						ImportDeclaration ic = (ImportDeclaration) ast.imports().get(i);
						if (ic.getName().getFullyQualifiedName().startsWith(lib)) {
							hasLib = true;
							break;
						}
					}
				}
				if (!hasLib)
					return;
			}
			if (testing)
				System.out.println(sourceFilePath);
			stLog.println(sourceFilePath);
			for (int i = 0; i < ast.types().size(); i++) {
				if (ast.types().get(i) instanceof TypeDeclaration) {
					TypeDeclaration td = (TypeDeclaration) ast.types().get(i);
					numOfSequences += generateSequence(keepUnresolvables, lib, td, sourceFilePath, ast.getPackage().getName().getFullyQualifiedName(), "");
				}
			}
		}
	}

	private ArrayList<String> getRootPaths() {
		ArrayList<String> rootPaths = new ArrayList<>();
		if (PARSE_INDIVIDUAL_SRC)
			getRootPaths(new File(inPath), rootPaths);
		else {
			if (SCAN_FILES_FRIST)
				getRootPaths(new File(inPath), rootPaths);
			rootPaths = new ArrayList<>();
			rootPaths.add(inPath);
		}
		return rootPaths;
	}

	private void getRootPaths(File file, ArrayList<String> rootPaths) {
		if (file.isDirectory()) {
			System.out.println(rootPaths);
			for (File sub : file.listFiles())
				getRootPaths(sub, rootPaths);
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
					String name = ast.getPackage().getName().getFullyQualifiedName();
					name = name.replace('.', '\\');
					String p = file.getParentFile().getAbsolutePath();
					if (p.endsWith(name))
						add(p.substring(0, p.length() - name.length() - 1), rootPaths);
				} /*else 
					badFiles.add(file.getAbsolutePath());*/
			} catch (Throwable t) {
				badFiles.add(file.getAbsolutePath());
			}
		}
	}

	private void add(String path, ArrayList<String> rootPaths) {
		int index = Collections.binarySearch(rootPaths, path);
		if (index < 0) {
			index = - index - 1;
			int i = rootPaths.size() - 1;
			while (i > index) {
				if (rootPaths.get(i).startsWith(path))
					rootPaths.remove(i);
				i--;
			}
			i = index - 1;
			while (i >= 0) {
				if (path.startsWith(rootPaths.get(i)))
					return;
				i--;
			}
			rootPaths.add(index, path);
		}
	}

	private int generateSequence(boolean keepUnresolvables, String lib, TypeDeclaration td, String path, String packageName, String outer) {
		int numOfSequences = 0;
		String name = outer.isEmpty() ? td.getName().getIdentifier() : outer + "." + td.getName().getIdentifier();
		String className = td.getName().getIdentifier(), superClassName = null;
		if (td.getSuperclassType() != null)
			superClassName = SequenceGenerator.getUnresolvedType(td.getSuperclassType());
		for (MethodDeclaration method : td.getMethods()) {
			stLog.println(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method));
			SequenceGenerator sg = new SequenceGenerator(className, superClassName);
			method.accept(sg);
			int numofExpressions = sg.getNumOfExpressions(), numOfResolvedExpressions = sg.getNumOfResolvedExpressions();
			String source = sg.getPartialSequence(), target = sg.getFullSequence();
			String[] sTokens = sg.getPartialSequenceTokens(), tTokens = sg.getFullSequenceTokens();
			if (sTokens.length == tTokens.length 
					&& sTokens.length > 2 && numofExpressions > 0 
					&& (keepUnresolvables || numofExpressions == numOfResolvedExpressions)) {
				boolean hasLib = true;
				if (lib != null && !lib.isEmpty()) {
					hasLib = false;
					for (String t : tTokens) {
						if (t.startsWith(lib)) {
							hasLib = true;
							break;
						}
					}
				}
				if (hasLib) {
//					this.locations.add(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%");
//					this.sourceSequences.add(source);
//					this.targetSequences.add(target);
//					this.sourceSequenceTokens.add(sTokens);
//					this.targetSequenceTokens.add(tTokens);
					stLocations.print(path + "\t" + packageName + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%" + "\n");
					stSourceSequences.print(source + "\n");
					stTargetSequences.print(target + "\n");
					numOfSequences++;
				}
			}
			if (testing) {
				if (sTokens.length != tTokens.length)
					throw new AssertionError("Source and target sequences do not have the same length!");
				for (int j = 0; j < sTokens.length; j++) {
					String s = sTokens[j], t = tTokens[j];
//					if (!t.equals(s) && !t.endsWith(s))
//					if (t.length() < s.length())
					if (!t.contains(".") && !s.contains(".") && !t.equals(s))
						throw new AssertionError("Corresponding source and target tokens do not match!");
				}
			}
		}
		for (TypeDeclaration inner : td.getTypes())
			numOfSequences += generateSequence(keepUnresolvables, lib, inner, path, packageName, name);
		return numOfSequences;
	}

	private String getParameters(MethodDeclaration method) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < method.parameters().size(); i++) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) (method.parameters().get(i));
			String type = SequenceGenerator.getUnresolvedType(d.getType());
			sb.append("\t" + type);
		}
		sb.append("\t)");
		return sb.toString();
	}

	private String[] getSourcePaths(String path, String[] extensions) {
		HashSet<String> exts = new HashSet<>();
		for (String e : extensions)
			exts.add(e);
		HashSet<String> paths = new HashSet<>();
		getSourcePaths(new File(path), paths, exts);
		paths.removeAll(badFiles);
		return (String[]) paths.toArray(new String[0]);
	}

	private void getSourcePaths(File file, HashSet<String> paths, HashSet<String> exts) {
		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				getSourcePaths(sub, paths, exts);
		} else if (exts.contains(getExtension(file.getName())))
			paths.add(file.getAbsolutePath());
	}

	private Object getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index < 0)
			index = 0;
		return name.substring(index);
	}

	private String[] getJarPaths() {
		HashMap<String, File> jarFiles = new HashMap<>();
		HashSet<String> globalRepoLinks = new HashSet<>();
		globalRepoLinks.add("http://central.maven.org/maven2/");
		HashMap<String, String> globalProperties = new HashMap<>();
		HashMap<String, String> globalManagedDependencies = new HashMap<>();
		Stack<ClassPathUtil.PomFile> parentPomFiles = new Stack<>();
		getJarFiles(new File(inPath), jarFiles, globalRepoLinks, globalProperties, globalManagedDependencies, parentPomFiles);
		String[] paths = new String[jarFiles.size()];
		int i = 0;
		for (File file : jarFiles.values())
			paths[i++] = file.getAbsolutePath();
		return paths;
	}

	private void getJarFiles(File file, HashMap<String, File> jarFiles, 
			HashSet<String> globalRepoLinks, HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			Stack<PomFile> parentPomFiles) {
		if (file.isDirectory()) {
			int size = parentPomFiles.size();
			ArrayList<File> dirs = new ArrayList<>();
			for (File sub : file.listFiles()) {
				if (sub.isDirectory())
					dirs.add(sub);
				else
					getJarFiles(sub, jarFiles, globalRepoLinks, globalProperties, globalManagedDependencies, parentPomFiles);
			}
			for (File dir : dirs)
				getJarFiles(dir, jarFiles, globalRepoLinks, globalProperties, globalManagedDependencies, parentPomFiles);
			if (parentPomFiles.size() > size)
				parentPomFiles.pop();
		} else if (file.getName().endsWith(".jar")) {
			File f = jarFiles.get(file.getName());
			if (f == null || file.lastModified() > f.lastModified())
				jarFiles.put(file.getName(), file);
		} else if (file.getName().equals("build.gradle")) {
			try {
				ClassPathUtil.getGradleDependencies(file, this.inPath + "/lib");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else if (file.getName().equals("pom.xml")) {
			try {
				ClassPathUtil.getPomDependencies(file, this.inPath + "/lib", globalRepoLinks, globalProperties, globalManagedDependencies, parentPomFiles);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param inPath
	 * @param doVerify
	 * @return 	numbers[0]: 0-same number of sequences, 1-different numbers of sequences;
	 * 			numbers[1]: number of sequences with different lengths;
	 * 			numbers[2]: number of sequences with non-aligned tokens;
	 * 			numbers[3]: number of non-aligned tokens 
	 */
	
	public int[] generateAlignment(boolean doVerify) {
		return generateAlignment(outPath, doVerify);
	}
	
	/**
	 * 
	 * @param inPath
	 * @param doVerify
	 * @return 	numbers[0]: 0-same number of sequences, 1-different numbers of sequences;
	 * 			numbers[1]: number of sequences with different lengths;
	 * 			numbers[2]: number of sequences with non-aligned tokens;
	 * 			numbers[3]: number of non-aligned tokens 
	 */
	public static int[] generateAlignment(String inPath, boolean doVerify) {
		int[] numbers = new int[]{0, 0, 0, 0};
		ArrayList<String> sourceSequences = FileUtil.getFileStringArray(inPath + "/source.txt"), 
				targetSequences = FileUtil.getFileStringArray(inPath + "/target.txt");
		if (doVerify)
			if (sourceSequences.size() != targetSequences.size()) {
				numbers[0]++;
//				throw new AssertionError("Numbers of source and target sequences are not the same!!!");
			}
		File dir = new File(inPath + "-alignment");
		if (!dir.exists())
			dir.mkdirs();
		PrintStream psS2T = null, psT2S = null;
		try {
			psS2T = new PrintStream(new FileOutputStream(dir.getAbsolutePath() + "/training.s-t.A3"));
			psT2S = new PrintStream(new FileOutputStream(dir.getAbsolutePath() + "/training.t-s.A3"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (psS2T != null)
				psS2T.close();
			if (psT2S != null)
				psT2S.close();
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < sourceSequences.size(); i++) {
			String source = sourceSequences.get(i), target = targetSequences.get(i);
			String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
			if (doVerify) {
				if (sTokens.length != tTokens.length) {
					numbers[1]++;
//					throw new AssertionError("Lengths of source and target sequences are not the same!!!");
				}
				boolean aligned = true;
				for (int j = 0; j < sTokens.length; j++) {
					String s = sTokens[j], t = tTokens[j];
					if ((t.contains(".") && !t.substring(t.lastIndexOf('.')+1).equals(s.substring(s.lastIndexOf('.')+1))) || (!t.contains(".") && !t.equals(s))) {
						numbers[3]++;
						aligned = false;
//						throw new AssertionError("Source and target are not aligned!!!");
					}
				}
				if (!aligned)
					numbers[2]++;
			}
			String headerS2T = generateHeader(sTokens, tTokens, i), headerT2S = generateHeader(tTokens, sTokens, i);
			psS2T.println(headerS2T);
			psT2S.println(headerT2S);
			psS2T.println(target);
			psT2S.println(source);
			String alignmentS2T = generateAlignment(sTokens), alignmentT2S = generateAlignment(tTokens);
			psS2T.println(alignmentS2T);
			psT2S.println(alignmentT2S);
		}
		psS2T.flush();
		psT2S.flush();
		psS2T.close();
		psT2S.close();
		if (doVerify) {
			if (sourceSequences.size()*3 != FileUtil.countNumberOfLines(dir.getAbsolutePath() + "/training.s-t.A3")
					|| targetSequences.size()*3 != FileUtil.countNumberOfLines(dir.getAbsolutePath() + "/training.t-s.A3"))
				numbers[0]++;
		}
		return numbers;
	}
	
	/**
	 * 
	 * @param inPath
	 * @param doVerify
	 * @return 	numbers[0]: 0-same number of sequences, 1-different numbers of sequences;
	 * 			numbers[1]: number of sequences with different lengths;
	 * 			numbers[2]: number of sequences with non-aligned tokens;
	 * 			numbers[3]: number of non-aligned tokens 
	 */
	public static int[] generateAlignmentForCrossValidation(String inPath, boolean doVerify) {
		int[] numbers = new int[]{0, 0, 0, 0};
		ArrayList<String> sourceSequences = FileUtil.getFileStringArray(inPath + "/train.s"), 
				targetSequences = FileUtil.getFileStringArray(inPath + "/train.t");
		if (doVerify)
			if (sourceSequences.size() != targetSequences.size()) {
				numbers[0]++;
//				throw new AssertionError("Numbers of source and target sequences are not the same!!!");
			}
		File dir = new File(inPath + "-alignment");
		if (!dir.exists())
			dir.mkdirs();
		PrintStream psS2T = null, psT2S = null;
		try {
			psS2T = new PrintStream(new FileOutputStream(dir.getAbsolutePath() + "/training.s-t.A3"));
			psT2S = new PrintStream(new FileOutputStream(dir.getAbsolutePath() + "/training.t-s.A3"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (psS2T != null)
				psS2T.close();
			if (psT2S != null)
				psT2S.close();
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < sourceSequences.size(); i++) {
			String source = sourceSequences.get(i), target = targetSequences.get(i);
			String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
			if (doVerify) {
				if (sTokens.length != tTokens.length) {
					numbers[1]++;
//					throw new AssertionError("Lengths of source and target sequences are not the same!!!");
				}
				boolean aligned = true;
				for (int j = 0; j < sTokens.length; j++) {
					String s = sTokens[j], t = tTokens[j];
					if ((t.contains(".") && !t.substring(t.lastIndexOf('.')+1).equals(s.substring(s.lastIndexOf('.')+1))) || (!t.contains(".") && !t.equals(s))) {
						numbers[3]++;
						aligned = false;
//						throw new AssertionError("Source and target are not aligned!!!");
					}
				}
				if (!aligned)
					numbers[2]++;
			}
			String headerS2T = generateHeader(sTokens, tTokens, i), headerT2S = generateHeader(tTokens, sTokens, i);
			psS2T.println(headerS2T);
			psT2S.println(headerT2S);
			psS2T.println(target);
			psT2S.println(source);
			String alignmentS2T = generateAlignment(sTokens), alignmentT2S = generateAlignment(tTokens);
			psS2T.println(alignmentS2T);
			psT2S.println(alignmentT2S);
		}
		psS2T.flush();
		psT2S.flush();
		psS2T.close();
		psT2S.close();
		if (doVerify) {
			if (sourceSequences.size()*3 != FileUtil.countNumberOfLines(dir.getAbsolutePath() + "/training.s-t.A3")
					|| targetSequences.size()*3 != FileUtil.countNumberOfLines(dir.getAbsolutePath() + "/training.t-s.A3"))
				numbers[0]++;
		}
		sourceSequences.clear();
		targetSequences.clear();
		return numbers;
	}
	
	private static String generateAlignment(String[] tokens) {
		StringBuilder sb = new StringBuilder();
		sb.append("NULL ({  })");
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i];
			sb.append(" " + t + " ({ " + (i+1) + " })");
		}
		return sb.toString();
	}

	private static String generateHeader(String[] sTokens, String[] tTokens, int i) {
		return "# sentence pair (" + i + ") source length " + sTokens.length + " target length " + tTokens.length + " alignment score : 0";
	}
}
