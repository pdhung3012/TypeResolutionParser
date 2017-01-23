package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	private String path;
	private ArrayList<String> locations = new ArrayList<>();
	private ArrayList<String> sourceSequences = new ArrayList<>(), targetSequences = new ArrayList<>();
	private ArrayList<String[]> sourceSequenceTokens = new ArrayList<>(), targetSequenceTokens = new ArrayList<>();
	private StringBuilder sbLocations, sbSourceSequences, sbTargetSequences;
	
	public ProjectSequencesGenerator(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public ArrayList<String> getLocations() {
		return locations;
	}

	public ArrayList<String> getSourceSequences() {
		return sourceSequences;
	}

	public ArrayList<String> getTargetSequences() {
		return targetSequences;
	}

	public ArrayList<String[]> getSourceSequenceTokens() {
		return sourceSequenceTokens;
	}

	public ArrayList<String[]> getTargetSequenceTokens() {
		return targetSequenceTokens;
	}

	public void generateSequences() {
		String[] sourcePaths = getSourcePaths(new String[]{".java"});
		String[] jarPaths = getJarPaths();

		final HashMap<String, CompilationUnit> cus = new HashMap<>();
		FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				cus.put(sourceFilePath, ast);
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
		parser.setBindingsRecovery(true);
		
		parser.createASTs(sourcePaths, null, new String[0], r, null);
		
		sbLocations = new StringBuilder();
		sbSourceSequences = new StringBuilder();
		sbTargetSequences = new StringBuilder();
		for(String path : cus.keySet()){
			CompilationUnit ast = cus.get(path);
			for (int i = 0; i < ast.types().size(); i++) {
				if (ast.types().get(i) instanceof TypeDeclaration) {
					TypeDeclaration td = (TypeDeclaration) ast.types().get(i);
					generateSequence(td, path, "");
				}
			}
		}
		FileUtil.writeToFile("T:/temp/statType/test/locations.txt", sbLocations.toString());
		FileUtil.writeToFile("T:/temp/statType/test/source.txt", sbSourceSequences.toString());
		FileUtil.writeToFile("T:/temp/statType/test/target.txt", sbTargetSequences.toString());
	}

	private void generateSequence(TypeDeclaration td, String path, String outer) {
		String name = outer.isEmpty() ? td.getName().getIdentifier() : outer + "." + td.getName().getIdentifier();
		for (MethodDeclaration method : td.getMethods()) {
			SequenceGenerator sg = new SequenceGenerator();
			method.accept(sg);
			int numofExpressions = sg.getNumOfExpressions(), numOfResolvedExpressions = sg.getNumOfResolvedExpressions();
			String source = sg.getPartialSequence(), target = sg.getFullSequence();
			String[] sTokens = sg.getPartialSequenceTokens(), tTokens = sg.getFullSequenceTokens();
			/*if (numofExpressions == numOfResolvedExpressions)*/ {
				this.locations.add(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%");
				this.sourceSequences.add(source);
				this.targetSequences.add(target);
				this.sourceSequenceTokens.add(sTokens);
				this.targetSequenceTokens.add(tTokens);
				sbLocations.append(path + "\t" + name + "\t" + method.getName().getIdentifier() + "\t" + getParameters(method) + "\t" + numofExpressions + "\t" + numOfResolvedExpressions + "\t" + (numOfResolvedExpressions * 100 / numofExpressions) + "%" + "\n");
				sbSourceSequences.append(source + "\n");
				sbTargetSequences.append(target + "\n");
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

	private String[] getSourcePaths(String[] extensions) {
		HashSet<String> exts = new HashSet<>();
		for (String e : extensions)
			exts.add(e);
		HashSet<String> paths = new HashSet<>();
		getSourcePaths(new File(path), paths, exts);
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
		getJarFiles(new File(path), jarFiles);
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
}
