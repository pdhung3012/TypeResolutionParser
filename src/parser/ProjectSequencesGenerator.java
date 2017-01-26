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
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileUtil;

public class ProjectSequencesGenerator {
	private static final boolean PARSE_INDIVIDUAL_SRC = false, SCAN_FILES_FRIST = false;
	private static final int MAX_TOKEN_MAPPINGS = 1;
	private static final double MAX_ENTROPY = 1.0;
	
	private String inPath, outPath;
	private boolean testing = false;
//	private ArrayList<String> locations = new ArrayList<>();
//	private ArrayList<String> sourceSequences = new ArrayList<>(), targetSequences = new ArrayList<>();
//	private ArrayList<String[]> sourceSequenceTokens = new ArrayList<>(), targetSequenceTokens = new ArrayList<>();
	private PrintStream stLocations, stSourceSequences, stTargetSequences, stLog;
	private HashSet<String> badFiles = new HashSet<>();
	
	public ProjectSequencesGenerator(String inPath) {
		this.inPath = inPath;
	}
	
	public ProjectSequencesGenerator(String inPath, boolean testing) {
		this(inPath);
		this.testing = testing;
	}
	
//	public ArrayList<String> getLocations() {
//		return locations;
//	}
//
//	public ArrayList<String> getSourceSequences() {
//		return sourceSequences;
//	}
//
//	public ArrayList<String> getTargetSequences() {
//		return targetSequences;
//	}
//
//	public ArrayList<String[]> getSourceSequenceTokens() {
//		return sourceSequenceTokens;
//	}
//
//	public ArrayList<String[]> getTargetSequenceTokens() {
//		return targetSequenceTokens;
//	}

	public void generateSequences(String outPath) {
		this.outPath = outPath;
		ArrayList<String> rootPaths = getRootPaths();
		String[] jarPaths = getJarPaths();
		
		try {
			stLocations = new PrintStream(new FileOutputStream(outPath + "/locations.txt"));
			stSourceSequences = new PrintStream(new FileOutputStream(outPath + "/source.txt"));
			stTargetSequences = new PrintStream(new FileOutputStream(outPath + "/target.txt"));
			stLog = new PrintStream(new FileOutputStream("T:/temp/statType/test/log.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (String rootPath : rootPaths) {
			String[] sourcePaths = getSourcePaths(rootPath, new String[]{".java"});
			FileASTRequestor r = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (ast.getPackage() == null)
						return;
					System.out.println(sourceFilePath);
					stLog.println(sourceFilePath);
					for (int i = 0; i < ast.types().size(); i++) {
						if (ast.types().get(i) instanceof TypeDeclaration) {
							TypeDeclaration td = (TypeDeclaration) ast.types().get(i);
							generateSequence(td, sourceFilePath, "");
						}
					}
				}
			};
			
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
			
			parser.createASTs(sourcePaths, null, new String[0], r, null);
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

	private void generateSequence(TypeDeclaration td, String path, String outer) {
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
			if (sTokens.length > 0 && tTokens.length > 0 && numofExpressions > 0/* && numofExpressions == numOfResolvedExpressions*/) {
//				this.locations.add(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%");
//				this.sourceSequences.add(source);
//				this.targetSequences.add(target);
//				this.sourceSequenceTokens.add(sTokens);
//				this.targetSequenceTokens.add(tTokens);
				stLocations.print(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%" + "\n");
				stSourceSequences.print(source + "\n");
				stTargetSequences.print(target + "\n");
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
			generateSequence(inner, path, name);
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
		getJarFiles(new File(inPath), jarFiles);
		String[] paths = new String[jarFiles.size()];
		int i = 0;
		for (File file : jarFiles.values())
			paths[i++] = file.getAbsolutePath();
		return paths;
	}

	private void getJarFiles(File file, HashMap<String, File> jarFiles) {

		if (file.isDirectory()) {
			for (File sub : file.listFiles())
				getJarFiles(sub, jarFiles);
		} else if (file.getName().endsWith(".jar")) {
			File f = jarFiles.get(file.getName());
			if (f == null || file.lastModified() > f.lastModified())
				jarFiles.put(file.getName(), file);
		}
	}
	
	public void updateTokens() {
		HashMap<String, HashMap<String, Integer>> tokenMapCount = new HashMap<>();
		ArrayList<String> sourceSequences = readSource(outPath + "/source.txt"), targetSequences = readSource(outPath + "/target.txt");
		for (int i = 0; i < sourceSequences.size(); i++) {
			String source = sourceSequences.get(i), target = targetSequences.get(i);
			String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
			for (int j = 0; j < sTokens.length; j++) {
				String s = sTokens[j], t = tTokens[j];
				HashMap<String, Integer> mapCount = tokenMapCount.get(s);
				if (mapCount == null) {
					mapCount = new HashMap<>();
					tokenMapCount.put(s, mapCount);
				}
				if (mapCount.containsKey(t))
					mapCount.put(t, mapCount.get(t) + 1);
				else
					mapCount.put(t, 1);
			}
		}
		HashSet<String> commonTokens = new HashSet<>();
		for (String s : tokenMapCount.keySet()) {
			HashMap<String, Integer> mapCount = tokenMapCount.get(s);
//			if (mapCount.size() > MAX_TOKEN_MAPPINGS)
//				commonTokens.add(s);
			double entropy = entropy(s, mapCount);
			if (entropy > MAX_ENTROPY)
				commonTokens.add(s);
		}
		ArrayList<String> updatedSequences = new ArrayList<>();
		for (int i = 0; i < sourceSequences.size(); i++) {
			StringBuilder sb = new StringBuilder();
			String source = sourceSequences.get(i), target = targetSequences.get(i);
			String[] sTokens = source.trim().split(" "), tTokens = target.trim().split(" ");
			for (int j = 0; j < sTokens.length; j++) {
				String s = sTokens[j], t = tTokens[j];
				if (commonTokens.contains(s))
					sb.append(s + " ");
				else
					sb.append(t + " ");
			}
			updatedSequences.add(sb.toString().trim());
		}
		File dir = new File(outPath + "_updated");
		if (!dir.exists())
			dir.mkdirs();
		FileUtil.writeToFile(dir.getAbsolutePath() + "/source.txt", sourceSequences);
		FileUtil.writeToFile(dir.getAbsolutePath() + "/target.txt", updatedSequences);
	}

	private double entropy(String s, HashMap<String, Integer> mapCount) {
		double[] ps = new double[mapCount.size()];
		double sum = 0;
		for (int v : mapCount.values())
			sum += v;
		int i = 0;
		for (int v : mapCount.values()) {
			ps[i] = v / sum;
			i++;
		}
		double e = 0;
		for (double p : ps)
			e += p * Math.log10(p) / Math.log10(2);
		return -e;
	}

	private ArrayList<String> readSource(String path) {
		ArrayList<String> sequences = new ArrayList<>();
		String sources = FileUtil.getFileContent(path);
		Scanner sc = new Scanner(sources);
		while (sc.hasNextLine()) {
			sequences.add(sc.nextLine());
		}
		sc.close();
		return sequences;
	}
}
