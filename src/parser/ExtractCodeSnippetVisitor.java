package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ExtractCodeSnippetVisitor extends ASTVisitor {

	protected StringBuffer buffer;
	private int indent = 0;
	private HashMap<String, String> setSequencesOfMethods, setOfUnResolvedType;
	private String strSplitCharacter = " ";
	private String fop_jdk="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\JDK_source\\";
	private String fop_project;
	private boolean isParsingType;
	private boolean isVisitInsideMethodDeclaration = false,
			isSimpleNameMethod = false;
	private StringBuffer unresolvedBuffer;

	ASTParser parser = ASTParser.newParser(AST.JLS4);
	String[] classpath = { "C:\\Program Files\\Java\\jre1.8.0_51\\lib\\rt.jar" };
	HashMap<String, CompilationUnit> mapCU;
	private int numTotalTypeResolve = 0, numAbleTypeResolve = 0;
	private HashMap<String,String> mapMethods;
	String fp_currentMethod="";
	
	public HashMap<String, String> getMapMethods() {
		return mapMethods;
	}

	public void setMapMethods(HashMap<String, String> mapMethods) {
		this.mapMethods = mapMethods;
	}

	public void parseTypeInformationOfProject(File dir,String fop_methodSig) {
		String[] arrAllExtension = { ".java", ".jar" };

		String[] sources = { fop_jdk };

		HashMap<String, List<File>> arrAllSources = getFilteredRecursiveFiles2(
				dir, arrAllExtension);
		List<File> files = arrAllSources.get(".java");
		if (files == null) {
			files = new ArrayList<File>();
		}
		List<File> arrJars = arrAllSources.get(".jar");
		if (arrJars == null) {
			arrJars = new ArrayList<File>();
		}
		String[] classpath = new String[arrJars.size() + 1];
		classpath[0] = "C:\\Program Files\\Java\\jre1.8.0_51\\lib\\rt.jar";

		for (int i = 0; i < arrJars.size(); i++) {
			classpath[i + 1] = arrJars.get(i).getAbsolutePath();
		}

		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
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
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser = ASTParser.newParser(AST.JLS4);
		parser.setCompilerOptions(options);
		parser.setEnvironment(classpath == null ? new String[0] : classpath,
				sources, new String[] { "UTF-8" }, true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		System.out.println("path "+paths.length);
		parser.createASTs(paths, null, new String[0], r, null);

		setSequencesOfMethods = new HashMap<String, String>();
		for (String item : cus.keySet()) {
			CompilationUnit ast = cus.get(item);
			fp_currentMethod=item;
			ast.accept(this);

		}
	}
	
	public static List<File> getFilteredRecursiveFiles(File parentDir, String [] sourceFileExt)
	{
		List<File> recursiveFiles = new ArrayList<File>();
		
		File[] childFiles = parentDir.listFiles();
		if (childFiles==null)
			return recursiveFiles;
		for (File file:childFiles)
		{
			if (file.isFile())
			{

				if (isPassFile(file, sourceFileExt))
				{
					recursiveFiles.add(file);
				}
			}
			else
			{
				List<File> subList = getFilteredRecursiveFiles(file, sourceFileExt);
				recursiveFiles.addAll(subList);
			}
		}		
		return recursiveFiles;
	}
	
	public static HashMap<String,List<File>> getFilteredRecursiveFiles2(File parentDir, String [] sourceFileExt)
	{
		HashMap<String,List<File>> recursiveFiles = new HashMap<String,List<File>>();
		
		File[] childFiles = parentDir.listFiles();
		if (childFiles==null)
			return recursiveFiles;
		for (File file:childFiles)
		{
			if (file.isFile())
			{
				String ext=isPassFileReturn(file, sourceFileExt);
				if (!ext.isEmpty())
				{
				//	System.out.println(ext);
					if(!recursiveFiles.containsKey(ext)){
						List<File> lstFiles=new ArrayList<File>();
						lstFiles.add(file);
						recursiveFiles.put(ext,lstFiles);
					}else{
						recursiveFiles.get(ext).add(file);
					}
					
				}
			}
			else
			{
				HashMap<String,List<File>> subList = getFilteredRecursiveFiles2(file, sourceFileExt);
				for(String strKey:subList.keySet()){
					List<File> lstSubListFile=subList.get(strKey);
					if(lstSubListFile.size()>0){
						if(!recursiveFiles.containsKey(strKey)){
//							List<File> lstFiles=new ArrayList<File>();
//							lstFiles.addAll(lstSubListFile);
							recursiveFiles.put(strKey, lstSubListFile);
						} else{
							recursiveFiles.get(strKey).addAll(lstSubListFile);
						}
					}
					
				}
				
			}
		}		
		return recursiveFiles;
	}

	public static boolean isPassFile(File file, String [] sourceFileExt)
	{
		String name = file.getName();
		for (String fileExt:sourceFileExt)
		{
			if (name.endsWith(fileExt))
			{
				return true;
			}
		}
		return false;
	}
	
	public static String isPassFileReturn(File file, String [] sourceFileExt)
	{
		String name = file.getName();
		for (String fileExt:sourceFileExt)
		{
			if (name.endsWith(fileExt))
			{
				return fileExt;
			}
		}
		return "";
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
	
	@Override
	public boolean visit(MethodDeclaration node) {
		String methodSig= fp_currentMethod+"\t"+ node.getName().getIdentifier() + "\t" + getParameters(node);
		System.out.println(methodSig);
		if(mapMethods.containsKey(methodSig)){
			mapMethods.put(methodSig, node.toString());
		}
//		if (node.getBody() != null && !node.getBody().statements().isEmpty())
//			node.getBody().accept(this);
		return false;
	}
	
	public boolean visit(TypeDeclaration node) {

		return true;
	}
}
