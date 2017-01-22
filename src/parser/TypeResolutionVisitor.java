package parser;

/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.FileImageOutputStream;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

import utils.FileUtil;

/**
 * Internal AST visitor for serializing an AST in a quick and dirty fashion.
 * For various reasons the resulting string is not necessarily legal
 * Java code; and even if it is legal Java code, it is not necessarily the string
 * that corresponds to the given AST. Although useless for most purposes, it's
 * fine for generating debug print strings.
 * <p>
 * Example usage:
 * <code>
 * <pre>
 *    NaiveASTFlattener p = new NaiveASTFlattener();
 *    node.accept(p);
 *    String result = p.getResult();
 * </pre>
 * </code>
 * Call the <code>reset</code> method to clear the previous result before reusing an
 * existing instance.
 * </p>
 *
 * @since 2.0
 */

class AnnotationType{
	public static String Literal="#lit";
	public static String Variable="#var";
	public static String Type="#type";
}

@SuppressWarnings("rawtypes")
public class TypeResolutionVisitor extends ASTVisitor {
	
	
	/**
	 * Internal synonym for {@link AST#JLS2}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static final int JLS2 = AST.JLS2;
	
	/**
	 * Internal synonym for {@link AST#JLS3}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static final int JLS3 = AST.JLS3;

	/**
	 * Internal synonym for {@link AST#JLS4}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.10
	 */
	private static final int JLS4 = AST.JLS4;
	/**
	 * The string buffer into which the serialized representation of the AST is
	 * written.
	 */
	protected StringBuffer buffer;
	private int indent = 0;
	private HashMap<String,String> setSequencesOfMethods,setOfUnResolvedType;
	private String strSplitCharacter=" ";
	private String fop_jdk;
	private String fop_project;
	private boolean isParsingType;
	private boolean isVisitInsideMethodDeclaration=false,isSimpleNameMethod=false;
	private StringBuffer unresolvedBuffer;
	
	ASTParser parser = ASTParser.newParser(AST.JLS4);
	String[] classpath = {"C:\\Program Files\\Java\\jre1.8.0_51\\lib\\rt.jar"};	 
	HashMap<String, CompilationUnit> mapCU;
	private int numTotalTypeResolve=0,numAbleTypeResolve=0;
	
	public HashMap<String, String> getSetSequencesOfMethods() {
		return setSequencesOfMethods;
	}

	public void setSetSequencesOfMethods(
			HashMap<String, String> setSequencesOfMethods) {
		this.setSequencesOfMethods = setSequencesOfMethods;
	}

	public HashMap<String, String> getSetOfUnResolvedType() {
		return setOfUnResolvedType;
	}

	public void setSetOfUnResolvedType(HashMap<String, String> setOfUnResolvedType) {
		this.setOfUnResolvedType = setOfUnResolvedType;
	}

	
	public boolean isParsingType() {
		return isParsingType;
	}

	public void setParsingType(boolean isParsingType) {
		this.isParsingType = isParsingType;
	}

	/**
	 * Creates a new AST printer.
	 */
	public TypeResolutionVisitor(String fop_jdk,String fop_project,boolean isParsingType) {
		this.buffer = new StringBuffer();
		this.unresolvedBuffer=new StringBuffer();
		setSequencesOfMethods=new HashMap<String, String>();
		setOfUnResolvedType=new HashMap<String, String>();
		this.isParsingType=isParsingType;
		this.fop_jdk=fop_jdk;
		this.fop_project=fop_jdk;
		this.isParsingType=false;
	}
	
	
	private String getMethodSignature(MethodDeclaration node){
		String strResult="";
		if(node!=null){
			strResult=node.getName()+" ";
			//System.out.println(strResult);
			for(Object obj: node.parameters().toArray()){
				VariableDeclaration var=(VariableDeclaration) obj;
				IVariableBinding varBinding=var.resolveBinding();
				if(varBinding!=null){
					strResult+=varBinding.getType().getName().split("<")[0].trim()+" ";
				}
				
				//strResult+=obj.toString().split("<")[0].trim()+" ";
			}
		}
		return strResult.trim();
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


	String fp_currentFile="";
	
	public void parseTypeInformationOfProject(File dir,boolean isParsingType){
		String[] arrSourceExtension={".java"};
		String[] arrJarExtension={".jar"};
		String[] arrAllExtension={".java",".jar"};
		
		String[] sources = { fop_jdk};
		
		HashMap<String,List<File>> arrAllSources =getFilteredRecursiveFiles2(dir,arrAllExtension);
		List<File> files = arrAllSources.get(".java");
		if(files==null){
			files=new ArrayList<File>();
		}
		List<File> arrJars = arrAllSources.get(".jar");
		if(arrJars==null){
			arrJars=new ArrayList<File>();
		}
		classpath=new String[arrJars.size()+1];
		classpath[0]="C:\\Program Files\\Java\\jre1.8.0_51\\lib\\rt.jar";
		
		this.isParsingType=isParsingType;
		
		for(int i=0;i<arrJars.size();i++){
			classpath[i+1]=arrJars.get(i).getAbsolutePath();
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
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser = ASTParser.newParser(AST.JLS4);
		parser.setCompilerOptions(options);
		parser.setEnvironment(
				classpath == null ? new String[0] : classpath,
						 sources, new String[] { "UTF-8"}, 
				true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
//		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);			
//		parser.setResolveBindings(true);
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.createASTs(paths, null, new String[0], r, null);
		
		setSequencesOfMethods=new HashMap<String, String>();
		for(String item:cus.keySet()){
			CompilationUnit ast =cus.get(item);
			fp_currentFile=item;
			ast.accept(this);
			
		}
		
	}
	
	
	
//	public HashMap<String,String> parseFile(String fp_input){
//		try {		
////			File f=new File(fp_input);
//////			//System.out.println("Package name: "+packageName);
////			String s = new String(Files.readAllBytes(f.toPath()));
////			String[] sources = { fop_jdk }; 
////			parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);			
////			parser.setResolveBindings(true);
////			parser.setKind(ASTParser.K_COMPILATION_UNIT);
////			parser.setUnitName(f.getName());
////			parser.setSource(s.toCharArray());
////			// Visit
////			setSequencesOfMethods=new HashMap<String, String>();
////			CompilationUnit ast = (CompilationUnit) parser.createAST(null);
////			ast.accept(this);			
//		}
//		catch (Exception e)
//		{
//			System.out.println(e);
//			e.printStackTrace();
//		}
//		return setSequencesOfMethods;
//	}
	

	/**
	 * Internal synonym for {@link ClassInstanceCreation#getName()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private Name getName(ClassInstanceCreation node) {
		return node.getName();
	}

	/**
	 * Returns the string accumulated in the visit.
	 *
	 * @return the serialized
	 */
	public String getResult() {
		return this.buffer.toString();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#getReturnType()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static Type getReturnType(MethodDeclaration node) {
		return node.getReturnType();
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#getSuperclass()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static Name getSuperclass(TypeDeclaration node) {
		return node.getSuperclass();
	}

	/**
	 * Internal synonym for {@link TypeDeclarationStatement#getTypeDeclaration()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static TypeDeclaration getTypeDeclaration(TypeDeclarationStatement node) {
		return node.getTypeDeclaration();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#thrownExceptions()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.10
	 */
	private static List thrownExceptions(MethodDeclaration node) {
		return node.thrownExceptions();
	}

	void printIndent() {
		for (int i = 0; i < this.indent; i++)
			this.buffer.append("  "); //$NON-NLS-1$
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by a single space.
	 * Used for JLS2 modifiers.
	 *
	 * @param modifiers the modifier flags
	 */
	void printModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			this.buffer.append("public ");//$NON-NLS-1$
		}
		if (Modifier.isProtected(modifiers)) {
			this.buffer.append("protected ");//$NON-NLS-1$
		}
		if (Modifier.isPrivate(modifiers)) {
			this.buffer.append("private ");//$NON-NLS-1$
		}
		if (Modifier.isStatic(modifiers)) {
			this.buffer.append("static ");//$NON-NLS-1$
		}
		if (Modifier.isAbstract(modifiers)) {
			this.buffer.append("abstract ");//$NON-NLS-1$
		}
		if (Modifier.isFinal(modifiers)) {
			this.buffer.append("final ");//$NON-NLS-1$
		}
		if (Modifier.isSynchronized(modifiers)) {
			this.buffer.append("synchronized ");//$NON-NLS-1$
		}
		if (Modifier.isVolatile(modifiers)) {
			this.buffer.append("volatile ");//$NON-NLS-1$
		}
		if (Modifier.isNative(modifiers)) {
			this.buffer.append("native ");//$NON-NLS-1$
		}
		if (Modifier.isStrictfp(modifiers)) {
			this.buffer.append("strictfp ");//$NON-NLS-1$
		}
		if (Modifier.isTransient(modifiers)) {
			this.buffer.append("transient ");//$NON-NLS-1$
		}
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by a single space.
	 * Used for 3.0 modifiers and annotations.
	 *
	 * @param ext the list of modifier and annotation nodes
	 * (element type: <code>IExtendedModifiers</code>)
	 */
	void printModifiers(List ext) {
		for (Iterator it = ext.iterator(); it.hasNext(); ) {
			ASTNode p = (ASTNode) it.next();
			p.accept(this);
			this.buffer.append(" ");//$NON-NLS-1$
		}
	}

	/**
	 * reference node helper function that is common to all
	 * the difference reference nodes.
	 * 
	 * @param typeArguments list of type arguments 
	 */
	private void visitReferenceTypeArguments(List typeArguments) {
		this.buffer.append("::");//$NON-NLS-1$
//		if (!typeArguments.isEmpty()) {
//			this.buffer.append('<');
//			for (Iterator it = typeArguments.iterator(); it.hasNext(); ) {
//				Type t = (Type) it.next();
//				t.accept(this);
//				if (it.hasNext()) {
//					this.buffer.append(',');
//				}
//			}
//			this.buffer.append('>');
//		}
	}
	
	private void visitTypeAnnotations(AnnotatableType node) {
		if (node.getAST().apiLevel() >= AST.JLS8) {
			visitAnnotationsList(node.annotations());
		}
	}

	private void visitAnnotationsList(List annotations) {
		for (Iterator it = annotations.iterator(); it.hasNext(); ) {
			Annotation annotation = (Annotation) it.next();
			annotation.accept(this);
			this.buffer.append(' ');
		}
	}
	
	/**
	 * Resets this printer so that it can be used again.
	 */
	public void reset() {
		this.buffer.setLength(0);
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#superInterfaces()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private List superInterfaces(TypeDeclaration node) {
		return node.superInterfaces();
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeDeclaration)
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		printModifiers(node.modifiers());
//		this.buffer.append("@interface ");//$NON-NLS-1$
//		node.getName().accept(this);
//		this.buffer.append(" {");//$NON-NLS-1$
//		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
//			BodyDeclaration d = (BodyDeclaration) it.next();
//			d.accept(this);
//		}
//		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeMemberDeclaration)
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		printModifiers(node.modifiers());
//		node.getType().accept(this);
//		this.buffer.append(" ");//$NON-NLS-1$
//		node.getName().accept(this);
//		this.buffer.append("()");//$NON-NLS-1$
//		if (node.getDefault() != null) {
//			this.buffer.append(" default ");//$NON-NLS-1$
//			node.getDefault().accept(this);
//		}
//		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		//this.buffer.append("{\n");//$NON-NLS-1$
		this.indent++;
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration b = (BodyDeclaration) it.next();
			b.accept(this);
		}
		this.indent--;
		//printIndent();
	//	this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		this.buffer.append(strSplitCharacter);
		node.getArray().accept(this);		
		this.buffer.append(strSplitCharacter);
		this.buffer.append("[");
		this.buffer.append(strSplitCharacter);		
		node.getIndex().accept(this);
		this.buffer.append(strSplitCharacter);
		this.buffer.append("]");//$NON-NLS-1$
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		this.buffer.append(" new ");//$NON-NLS-1$
		ArrayType at = node.getType();
		int dims = at.getDimensions();
		Type elementType = at.getElementType();
		elementType.accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(strSplitCharacter);
		for (Iterator it = node.dimensions().iterator(); it.hasNext(); ) {
			this.buffer.append(" [ ");//$NON-NLS-1$
			Expression e = (Expression) it.next();
			e.accept(this);
			this.buffer.append(" ] ");//$NON-NLS-1$
			dims--;
		}
		// add empty "[]" for each extra array dimension
		for (int i= 0; i < dims; i++) {
			this.buffer.append(" [] ");//$NON-NLS-1$
		}
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$

		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$

		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$
		for (Iterator it = node.expressions().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(" , ");//$NON-NLS-1$
			}
		}
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		this.buffer.append(strSplitCharacter);
		if (node.getAST().apiLevel() < AST.JLS8) {
			visitComponentType(node);
			this.buffer.append("[]");//$NON-NLS-1$
		} else {
			node.getElementType().accept(this);
			List dimensions = node.dimensions();
			int size = dimensions.size();
			for (int i = 0; i < size; i++) {
				Dimension aDimension = (Dimension) dimensions.get(i);
				aDimension.accept(this);
			}
		}
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		//printIndent();
		this.buffer.append(" assert ");//$NON-NLS-1$
		node.getExpression().accept(this);
		if (node.getMessage() != null) {
			this.buffer.append(" : ");//$NON-NLS-1$
			node.getMessage().accept(this);
			this.buffer.append(strSplitCharacter);
		}
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		this.buffer.append(" ");
		node.getLeftHandSide().accept(this);
		this.buffer.append(" ");
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(" ");
		node.getRightHandSide().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		this.buffer.append(" ");//$NON-NLS-1$
		this.indent++;
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			this.buffer.append(strSplitCharacter);
			
			s.accept(this);
			this.buffer.append(strSplitCharacter);
			
		}
		this.indent--;
		printIndent();
		this.buffer.append(" ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BlockComment)
	 * @since 3.0
	 */
	public boolean visit(BlockComment node) {
		printIndent();
		this.buffer.append("/* */");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		
		if(isParsingType){
			this.buffer.append("java.lang.Boolean#lit");
			
		}else{
			this.buffer.append("Boolean#lit");
			
		}
		
//		if (node.booleanValue() == true) {
//			this.buffer.append("true");//$NON-NLS-1$
//		} else {
//			this.buffer.append("false");//$NON-NLS-1$
//		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		printIndent();
		this.buffer.append("break");//$NON-NLS-1$
		if (node.getLabel() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getLabel().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		this.buffer.append(" ( ");//$NON-NLS-1$
		node.getType().accept(this);
		this.buffer.append(AnnotationType.Type);//$NON-NLS-1$		
		this.buffer.append(" ) ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		this.buffer.append(" catch ");//$NON-NLS-1$
		node.getException().accept(this);
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$		
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		if(isParsingType){
			this.buffer.append("java.lang.");
		}
		this.buffer.append("Char");
		
		//this.buffer.append(node.getEscapedValue());
		this.buffer.append(AnnotationType.Literal);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		this.buffer.append(strSplitCharacter);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			//this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append(" .new ");//$NON-NLS-1$
		if (node.getAST().apiLevel() == JLS2) {
			getName(node).accept(this);
			this.buffer.append(AnnotationType.Type);
			
		}
		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeArguments().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
			node.getType().accept(this);
			this.buffer.append(AnnotationType.Type);
			
		}
		this.buffer.append(" ( ");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(" , ");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ) ");//$NON-NLS-1$
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		if (node.getPackage() != null) {
			node.getPackage().accept(this);
		}
		for (Iterator it = node.imports().iterator(); it.hasNext(); ) {
			ImportDeclaration d = (ImportDeclaration) it.next();
			d.accept(this);
		}
		for (Iterator it = node.types().iterator(); it.hasNext(); ) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			d.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		this.buffer.append(strSplitCharacter);
		node.getExpression().accept(this);
		this.buffer.append(" ? ");//$NON-NLS-1$
		node.getThenExpression().accept(this);
		this.buffer.append(" : ");//$NON-NLS-1$
		node.getElseExpression().accept(this);
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		printIndent();
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeArguments().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
//		}
		this.buffer.append(" this ( ");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ) ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		printIndent();
		this.buffer.append(" continue ");//$NON-NLS-1$
		if (node.getLabel() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getLabel().accept(this);
			this.buffer.append("#lit");
		}
		//this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}
	
	/*
	 * @see ASTVisitor#visit(CreationReference)
	 * 
	 * @since 3.10
	 */
	public boolean visit(CreationReference node) {
		node.getType().accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		this.buffer.append("new");//$NON-NLS-1$
		return false;
	}

	public boolean visit(Dimension node) {
		List annotations = node.annotations();
		if (annotations.size() > 0)
			this.buffer.append(' ');
		visitAnnotationsList(annotations);
		this.buffer.append("[]"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		printIndent();
		this.buffer.append(" do ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(" while ( ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ) ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		printIndent();
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnhancedForStatement)
	 * @since 3.1
	 */
	public boolean visit(EnhancedForStatement node) {
		printIndent();
		this.buffer.append(" for ( ");//$NON-NLS-1$
		node.getParameter().accept(this);
		this.buffer.append(" : ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ) ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(" ");
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumConstantDeclaration)
	 * @since 3.1
	 */
	public boolean visit(EnumConstantDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		printModifiers(node.modifiers());
//		node.getName().accept(this);
//		if (!node.arguments().isEmpty()) {
//			this.buffer.append("(");//$NON-NLS-1$
//			for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
//				Expression e = (Expression) it.next();
//				e.accept(this);
//				if (it.hasNext()) {
//					this.buffer.append(",");//$NON-NLS-1$
//				}
//			}
//			this.buffer.append(")");//$NON-NLS-1$
//		}
//		if (node.getAnonymousClassDeclaration() != null) {
//			node.getAnonymousClassDeclaration().accept(this);
//		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumDeclaration)
	 * @since 3.1
	 */
	public boolean visit(EnumDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		printModifiers(node.modifiers());
//		this.buffer.append("enum ");//$NON-NLS-1$
//		node.getName().accept(this);
//		this.buffer.append(" ");//$NON-NLS-1$
//		if (!node.superInterfaceTypes().isEmpty()) {
//			this.buffer.append("implements ");//$NON-NLS-1$
//			for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
//				Type t = (Type) it.next();
//				t.accept(this);
//				if (it.hasNext()) {
//					this.buffer.append(", ");//$NON-NLS-1$
//				}
//			}
//			this.buffer.append(" ");//$NON-NLS-1$
//		}
//		this.buffer.append("{");//$NON-NLS-1$
//		for (Iterator it = node.enumConstants().iterator(); it.hasNext(); ) {
//			EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
//			d.accept(this);
//			// enum constant declarations do not include punctuation
//			if (it.hasNext()) {
//				// enum constant declarations are separated by commas
//				this.buffer.append(", ");//$NON-NLS-1$
//			}
//		}
//		if (!node.bodyDeclarations().isEmpty()) {
//			this.buffer.append("; ");//$NON-NLS-1$
//			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
//				BodyDeclaration d = (BodyDeclaration) it.next();
//				d.accept(this);
//				// other body declarations include trailing punctuation
//			}
//		}
//		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionMethodReference)
	 * 
	 * @since 3.10
	 */
	public boolean visit(ExpressionMethodReference node) {
		node.getExpression().accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		node.getName().accept(this);
		return false;
	}	

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		printIndent();
		this.buffer.append(strSplitCharacter);
		
		node.getExpression().accept(this);
		this.buffer.append(strSplitCharacter);
		
		//this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		this.buffer.append(strSplitCharacter);
		
		node.getExpression().accept(this);
		this.buffer.append(strSplitCharacter);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//		}
//		node.getType().accept(this);
//		this.buffer.append(" ");//$NON-NLS-1$
//		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
//			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
//			f.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append(", ");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		printIndent();
		this.buffer.append(strSplitCharacter);
		this.buffer.append("for ( ");//$NON-NLS-1$
		for (Iterator it = node.initializers().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) this.buffer.append(" , ");//$NON-NLS-1$
		}
		//this.buffer.append("; ");//$NON-NLS-1$
		this.buffer.append(strSplitCharacter);		
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		this.buffer.append(strSplitCharacter);
		
		//this.buffer.append("; ");//$NON-NLS-1$
		for (Iterator it = node.updaters().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			this.buffer.append(strSplitCharacter);			
			e.accept(this);
			this.buffer.append(strSplitCharacter);
			
			if (it.hasNext()) this.buffer.append(" , ");//$NON-NLS-1$
		}
		this.buffer.append(" ) ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(strSplitCharacter);
		
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		printIndent();
		this.buffer.append(strSplitCharacter);		
		this.buffer.append(" if ( ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ) ");//$NON-NLS-1$
		node.getThenStatement().accept(this);
		if (node.getElseStatement() != null) {
			this.buffer.append(" else ");//$NON-NLS-1$
			node.getElseStatement().accept(this);
		}
		this.buffer.append(strSplitCharacter);
		
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
//		printIndent();
//		this.buffer.append(" import ");//$NON-NLS-1$
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (node.isStatic()) {
//				this.buffer.append("static ");//$NON-NLS-1$
//			}
//		}
//		node.getName().accept(this);
//		if (node.isOnDemand()) {
//			this.buffer.append(".*");//$NON-NLS-1$
//		}
//		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		this.buffer.append(strSplitCharacter);
		node.getLeftOperand().accept(this);
		this.buffer.append(' ');  // for cases like x= i - -1; or x= i++ + ++i;
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(' ');
		node.getRightOperand().accept(this);
		final List extendedOperands = node.extendedOperands();
		if (extendedOperands.size() != 0) {
			this.buffer.append(' ');
			for (Iterator it = extendedOperands.iterator(); it.hasNext(); ) {
				this.buffer.append(node.getOperator().toString()).append(' ');
				Expression e = (Expression) it.next();
				e.accept(this);
			}
		}
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//		}
//		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		this.buffer.append(" instanceof ");//$NON-NLS-1$
		node.getRightOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IntersectionType)
	 * @since 3.7
	 */
	public boolean visit(IntersectionType node) {
		for (Iterator it = node.types().iterator(); it.hasNext(); ) {
			Type t = (Type) it.next();
			t.accept(this);
			if (it.hasNext()) {
				this.buffer.append(" & "); //$NON-NLS-1$
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
//		printIndent();
//		this.buffer.append("/** ");//$NON-NLS-1$
//		for (Iterator it = node.tags().iterator(); it.hasNext(); ) {
//			ASTNode e = (ASTNode) it.next();
//			e.accept(this);
//		}
//		this.buffer.append("\n */\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		printIndent();
		this.buffer.append(strSplitCharacter);
		node.getLabel().accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(" : ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LambdaExpression)
	 */
	public boolean visit(LambdaExpression node) {
		boolean hasParentheses = node.hasParentheses();
		this.buffer.append(strSplitCharacter);
		if (hasParentheses){
			this.buffer.append('(');
			this.buffer.append(strSplitCharacter);
		}
		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
			
			VariableDeclaration v = (VariableDeclaration) it.next();
			v.accept(this);
			this.buffer.append(strSplitCharacter);
			if (it.hasNext()) {
				this.buffer.append(strSplitCharacter);
				this.buffer.append(",");//$NON-NLS-1$
				this.buffer.append(strSplitCharacter);
			}
		}
		if (hasParentheses){
			this.buffer.append(strSplitCharacter);
			this.buffer.append(')');
			this.buffer.append(strSplitCharacter);
		
		}
		
		this.buffer.append(" -> "); //$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LineComment)
	 * @since 3.0
	 */
	public boolean visit(LineComment node) {
		//this.buffer.append("//\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MarkerAnnotation)
	 * @since 3.1
	 */
	public boolean visit(MarkerAnnotation node) {
		//this.buffer.append("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberRef)
	 * @since 3.0
	 */
	public boolean visit(MemberRef node) {
//		if (node.getQualifier() != null) {
//			node.getQualifier().accept(this);
//		}
//		this.buffer.append("#");//$NON-NLS-1$
//		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberValuePair)
	 * @since 3.1
	 */
	public boolean visit(MemberValuePair node) {
		this.buffer.append(strSplitCharacter);
		node.getName().accept(this);
		this.buffer.append("=");//$NON-NLS-1$
		this.buffer.append(strSplitCharacter);
		node.getValue().accept(this);
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		//System.out.println("Node information");
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		printIndent();
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//			if (!node.typeParameters().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeParameters().iterator(); it.hasNext(); ) {
//					TypeParameter t = (TypeParameter) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
//		}
//		if (!node.isConstructor()) {
//			if (node.getAST().apiLevel() == JLS2) {
//				getReturnType(node).accept(this);
//			} else {
//				if (node.getReturnType2() != null) {
//					node.getReturnType2().accept(this);
//				} else {
//					// methods really ought to have a return type
//					this.buffer.append("void");//$NON-NLS-1$
//				}
//			}
//			this.buffer.append(" ");//$NON-NLS-1$
//		}
//		node.getName().accept(this);
//		this.buffer.append("(");//$NON-NLS-1$
//		if (node.getAST().apiLevel() >= AST.JLS8) {
//			Type receiverType = node.getReceiverType();
//			if (receiverType != null) {
//				receiverType.accept(this);
//				this.buffer.append(' ');
//				SimpleName qualifier = node.getReceiverQualifier();
//				if (qualifier != null) {
//					qualifier.accept(this);
//					this.buffer.append('.');
//				}
//				this.buffer.append("this"); //$NON-NLS-1$
//				if (node.parameters().size() > 0) {
//					this.buffer.append(',');
//				}
//			}
//		}
//		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
//			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
//			v.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append(",");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(")");//$NON-NLS-1$
//		int size = node.getExtraDimensions();
//		if (node.getAST().apiLevel() >= AST.JLS8) {
//			List dimensions = node.extraDimensions();
//			for (int i = 0; i < size; i++) {
//				visit((Dimension) dimensions.get(i));
//			}
//		} else {
//			for (int i = 0; i < size; i++) {
//				this.buffer.append("[]"); //$NON-NLS-1$
//			}
//		}
//		if (node.getAST().apiLevel() < AST.JLS8) {
//			if (!thrownExceptions(node).isEmpty()) {
//				this.buffer.append(" throws ");//$NON-NLS-1$
//				for (Iterator it = thrownExceptions(node).iterator(); it.hasNext(); ) {
//					Name n = (Name) it.next();
//					n.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(", ");//$NON-NLS-1$
//					}
//				}				
//				this.buffer.append(" ");//$NON-NLS-1$
//			} 
//		} else {
//			if (!node.thrownExceptionTypes().isEmpty()) {				
//				this.buffer.append(" throws ");//$NON-NLS-1$
//				for (Iterator it = node.thrownExceptionTypes().iterator(); it.hasNext(); ) {
//					Type n = (Type) it.next();
//					n.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(", ");//$NON-NLS-1$
//					}
//				}	
//				this.buffer.append(" ");//$NON-NLS-1$				
//			}
//		}
		
		
		
		if (node.getBody() == null) {
		//	this.buffer.append(";\n");//$NON-NLS-1$
		} else {
			numTotalTypeResolve=0;
			numAbleTypeResolve=0;
			String strMethodSig=getMethodSignature(node);
		//	System.out.println("Method sig: "+strMethodSig);
			isVisitInsideMethodDeclaration=true;
			
			setOfUnResolvedType=new HashMap<String, String>();
			node.getBody().accept(this);
			isVisitInsideMethodDeclaration=false;
			
//			this.buffer.append(strSplitCharacter);
//			this.buffer.append(numAbleTypeResolve+"/"+numTotalTypeResolve);
			setSequencesOfMethods.put(fp_currentFile+"\t"+strMethodSig+"\t"+numAbleTypeResolve+"/"+numTotalTypeResolve,buffer.toString().trim());			
			setOfUnResolvedType.put(fp_currentFile+"\t"+strMethodSig+"\t"+numAbleTypeResolve+"/"+numTotalTypeResolve,this.unresolvedBuffer.toString().trim());
			buffer=new StringBuffer();
			this.unresolvedBuffer=new StringBuffer();
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		this.buffer.append(strSplitCharacter);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		//	if(node.getExpression())
			this.buffer.append(AnnotationType.Variable);
			this.buffer.append(strSplitCharacter);
			this.buffer.append(".");//$NON-NLS-1$
		}
		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeArguments().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
		}
		//this.buffer.append(strSplitCharacter);
		isSimpleNameMethod=true;
		node.getName().accept(this);
		isSimpleNameMethod=false;
		this.buffer.append(strSplitCharacter);//$NON-NLS-1$		
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			this.buffer.append(strSplitCharacter);//$NON-NLS-1$		
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(strSplitCharacter);
				this.buffer.append(",");

				//$NON-NLS-1$
			}
		}
		this.buffer.append(strSplitCharacter);
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRef)
	 * @since 3.0
	 */
	public boolean visit(MethodRef node) {
//		if (node.getQualifier() != null) {
//			node.getQualifier().accept(this);
//		}
//		this.buffer.append("#");//$NON-NLS-1$
//		node.getName().accept(this);
//		this.buffer.append("(");//$NON-NLS-1$
//		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
//			MethodRefParameter e = (MethodRefParameter) it.next();
//			e.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append(",");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRefParameter)
	 * @since 3.0
	 */
	public boolean visit(MethodRefParameter node) {
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= JLS3) {
			if (node.isVarargs()) {
				this.buffer.append("...");//$NON-NLS-1$
			}
		}
		if (node.getName() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Modifier)
	 * @since 3.1
	 */
	public boolean visit(Modifier node) {
		//this.buffer.append(node.getKeyword().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NameQualifiedType)
	 * @since 3.10
	 */
	public boolean visit(NameQualifiedType node) {
		this.buffer.append(strSplitCharacter);
		node.getQualifier().accept(this);
		this.buffer.append('.');
		//visitTypeAnnotations(node);
		node.getName().accept(this);
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NormalAnnotation)
	 * @since 3.1
	 */
	public boolean visit(NormalAnnotation node) {
//		this.buffer.append("@");//$NON-NLS-1$
//		node.getTypeName().accept(this);
//		this.buffer.append("(");//$NON-NLS-1$
//		for (Iterator it = node.values().iterator(); it.hasNext(); ) {
//			MemberValuePair p = (MemberValuePair) it.next();
//			p.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append(",");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 * ST(Literal)#lit
	 */
	public boolean visit(NullLiteral node) {
		this.buffer.append(" ");
		this.buffer.append("null#lit");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		this.buffer.append(" ");
		boolean checkInteger=false,checkLong=false;
		int intVal=0;
		long longVal=0;
		double douVal=0;
		String inputLiteral=node.getToken();
		String strType="";
	//	this.buffer.append(" abc ");
		
		try{
			intVal=Integer.parseInt(inputLiteral);
			
			checkInteger=true;
			strType="Integer";
			if(isParsingType){
				strType="java.lang."+strType;
			}
			this.buffer.append(strType+AnnotationType.Literal);
			this.buffer.append(strSplitCharacter);
		}catch(Exception ex){
			
		}
		if(!checkInteger){
			try{
				longVal=Long.parseLong(inputLiteral);
				checkLong=true;
				strType="Long";
				if(isParsingType){
					strType="java.lang."+strType;
				}
				this.buffer.append(strType+AnnotationType.Literal);
				this.buffer.append(strSplitCharacter);
			}catch(Exception ex){
				
			}
		}
		
		
		
		if(!checkLong&&!checkInteger){
			try{
				douVal=Double.parseDouble(inputLiteral);
				checkLong=true;
				strType="Double";
				if(isParsingType){
					strType="java.lang."+strType;
				}
				//inputLiteral="Double";
				this.buffer.append(strType+AnnotationType.Literal);
				this.buffer.append(strSplitCharacter);
			}catch(Exception ex){
				
			}
		}
		
		//this.buffer.append(node.getToken());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (node.getJavadoc() != null) {
//				node.getJavadoc().accept(this);
//			}
//			for (Iterator it = node.annotations().iterator(); it.hasNext(); ) {
//				Annotation p = (Annotation) it.next();
//				p.accept(this);
//				this.buffer.append(" ");//$NON-NLS-1$
//			}
//		}
//		printIndent();
//		this.buffer.append("package ");//$NON-NLS-1$
//		node.getName().accept(this);
	//	this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParameterizedType)
	 * @since 3.1
	 */
	public boolean visit(ParameterizedType node) {
		this.buffer.append(strSplitCharacter);
		node.getType().accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(strSplitCharacter);
		//		this.buffer.append("<");//$NON-NLS-1$
//		for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//			Type t = (Type) it.next();
//			t.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append(",");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(">");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		this.buffer.append(" ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		this.buffer.append(" ");
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(" ");
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		this.buffer.append(strSplitCharacter);
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(strSplitCharacter);		
		node.getOperand().accept(this);
		this.buffer.append(strSplitCharacter);
		
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		visitTypeAnnotations(node);
		this.buffer.append(node.getPrimitiveTypeCode().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedType)
	 * @since 3.1
	 */
	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		visitTypeAnnotations(node);
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		printIndent();
		this.buffer.append("return");//$NON-NLS-1$
		if (node.getExpression() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getExpression().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
//		node.accept(this);
		if(isVisitInsideMethodDeclaration){
			
			//IType typeBind=iBind.
			if(isSimpleNameMethod){
				this.buffer.append(node.getIdentifier());
			}else{
				ITypeBinding iTypeBind=node.resolveTypeBinding();
				numTotalTypeResolve++;
				if(iTypeBind!=null){
					
					numAbleTypeResolve++;
					if(isParsingType){
						this.buffer.append(iTypeBind.getQualifiedName());
					}else{
						this.buffer.append(iTypeBind.getName());
					}
					
				} else{
					this.unresolvedBuffer.append(node.getIdentifier());
					this.unresolvedBuffer.append(strSplitCharacter);
//					if(!setOfUnResolvedType.containsKey(node.getIdentifier())){
//						setOfUnResolvedType.put(node.getIdentifier(), node.getIdentifier());					
//					}
				}
				
			}
			
		//	node.
		//	this.buffer.append(node.getIdentifier());			
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		
	//	visitTypeAnnotations(node);
		//this.buffer.append(strSplitCharacter);
		
	//	if(isVisitInsideMethodDeclaration){
//			ITypeBinding iType=node.resolveBinding();
//			if(iType!=null){
//				node.getName().accept(this);
//			//	this.buffer.append(iType.getName());
//			}
			
	//	}
		//this.buffer.append(strSplitCharacter);
//		ITypeBinding iTypeBind=node.resolveBinding();
//		numTotalTypeResolve++;
//		if(iTypeBind!=null){
//			numAbleTypeResolve++;
//			if(isParsingType){
//				this.buffer.append(iTypeBind.getQualifiedName());
//			}else{
//				this.buffer.append(iTypeBind.getName());
//			}
//		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SingleMemberAnnotation)
	 * @since 3.1
	 */
	public boolean visit(SingleMemberAnnotation node) {
		//this.buffer.append("@");//$NON-NLS-1$
//		node.getTypeName().accept(this);
//		this.buffer.append("(");//$NON-NLS-1$
//		node.getValue().accept(this);
//		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
//		printIndent();
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//		}
		node.getType().accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(" ");
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (node.isVarargs()) {
//				if (node.getAST().apiLevel() >= AST.JLS8) {
//					List annotations = node.varargsAnnotations();
//					if (annotations.size() > 0) {
//						this.buffer.append(' ');
//					}
//					visitAnnotationsList(annotations);
//				}
//				this.buffer.append("...");//$NON-NLS-1$
//			}
//		}
		this.buffer.append(" ");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append(AnnotationType.Variable);
		int size = node.getExtraDimensions();
		if (node.getAST().apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for (int i = 0; i < size; i++) {
				visit((Dimension) dimensions.get(i));
			}
		} else {
			for (int i = 0; i < size; i++) {
				this.buffer.append("[]"); //$NON-NLS-1$
			}
		}
		if (node.getInitializer() != null) {
			this.buffer.append(" = ");//$NON-NLS-1$
			node.getInitializer().accept(this);
		//	this.buffer.append(AnnotationType.Variable);
			this.buffer.append(" ");
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		this.buffer.append(strSplitCharacter);
		//this.buffer.append(node.getEscapedValue());
		if(isParsingType){
			this.buffer.append("java.lang.String"+AnnotationType.Literal);
			
		}else{
			this.buffer.append("String"+AnnotationType.Literal);
			
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		printIndent();
		this.buffer.append(strSplitCharacter);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(strSplitCharacter);
			//this.buffer.append(".");//$NON-NLS-1$
		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeArguments().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
//		}
		this.buffer.append(" .super ( ");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(" , ");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ) ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("super.");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		this.buffer.append(strSplitCharacter);
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
		//	this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append(strSplitCharacter);
		this.buffer.append("super");//$NON-NLS-1$
		this.buffer.append(strSplitCharacter);
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeArguments().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
//		}
		node.getName().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(" , ");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ) ");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodReference)
	 * 
	 * @since 3.10
	 */
	public boolean visit(SuperMethodReference node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append('.');
		}
		this.buffer.append("super");//$NON-NLS-1$
		visitReferenceTypeArguments(node.typeArguments());
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		if (node.isDefault()) {
			this.buffer.append("default : ");//$NON-NLS-1$
		} else {
			this.buffer.append("case ");//$NON-NLS-1$
			node.getExpression().accept(this);
			this.buffer.append(": ");//$NON-NLS-1$
		}
		this.indent++; //decremented in visit(SwitchStatement)
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		this.buffer.append(" switch ( ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ) ");//$NON-NLS-1$
		//this.buffer.append("{\n");//$NON-NLS-1$
		this.indent++;
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			this.buffer.append(strSplitCharacter);
			s.accept(this);
			this.buffer.append(strSplitCharacter);
			this.indent--; // incremented in visit(SwitchCase)
		}
		this.indent--;
		//printIndent();
		this.buffer.append(strSplitCharacter);
		//this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		this.buffer.append(" synchronized ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TagElement)
	 * @since 3.0
	 */
	public boolean visit(TagElement node) {
//		if (node.isNested()) {
//			// nested tags are always enclosed in braces
//			this.buffer.append("{");//$NON-NLS-1$
//		} else {
//			// top-level tags always begin on a new line
//			this.buffer.append("\n * ");//$NON-NLS-1$
//		}
//		boolean previousRequiresWhiteSpace = false;
//		if (node.getTagName() != null) {
//			this.buffer.append(node.getTagName());
//			previousRequiresWhiteSpace = true;
//		}
//		boolean previousRequiresNewLine = false;
//		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
//			ASTNode e = (ASTNode) it.next();
//			// Name, MemberRef, MethodRef, and nested TagElement do not include white space.
//			// TextElements don't always include whitespace, see <https://bugs.eclipse.org/206518>.
//			boolean currentIncludesWhiteSpace = false;
//			if (e instanceof TextElement) {
//				String text = ((TextElement) e).getText();
//				if (text.length() > 0 && ScannerHelper.isWhitespace(text.charAt(0))) {
//					currentIncludesWhiteSpace = true; // workaround for https://bugs.eclipse.org/403735
//				}
//			}
//			if (previousRequiresNewLine && currentIncludesWhiteSpace) {
//				this.buffer.append("\n * ");//$NON-NLS-1$
//			}
//			previousRequiresNewLine = currentIncludesWhiteSpace;
//			// add space if required to separate
//			if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
//				this.buffer.append(" "); //$NON-NLS-1$
//			}
//			e.accept(this);
//			previousRequiresWhiteSpace = !currentIncludesWhiteSpace && !(e instanceof TagElement);
//		}
//		if (node.isNested()) {
//			this.buffer.append("}");//$NON-NLS-1$
//		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TextElement)
	 * @since 3.0
	 */
	public boolean visit(TextElement node) {
		this.buffer.append(node.getText());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		this.buffer.append(strSplitCharacter);
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		//this.buffer.append(strSplitCharacter);
		this.buffer.append("#this");//$NON-NLS-1$
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		//printIndent();
		this.buffer.append(" throw ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ");
		//this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		printIndent();
		this.buffer.append("try ");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= JLS4) {
			List resources = node.resources();
			if (!resources.isEmpty()) {
				this.buffer.append('(');
				for (Iterator it = resources.iterator(); it.hasNext(); ) {
					VariableDeclarationExpression variable = (VariableDeclarationExpression) it.next();
					variable.accept(this);
					if (it.hasNext()) {
						this.buffer.append(';');
					}
				}
				this.buffer.append(')');
			}
		}
		node.getBody().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator it = node.catchClauses().iterator(); it.hasNext(); ) {
			CatchClause cc = (CatchClause) it.next();
			cc.accept(this);
		}
		if (node.getFinally() != null) {
			this.buffer.append(" finally ");//$NON-NLS-1$
			node.getFinally().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
//		if (node.getJavadoc() != null) {
//			node.getJavadoc().accept(this);
//		}
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//		}
//		this.buffer.append(node.isInterface() ? "interface " : "class ");//$NON-NLS-2$//$NON-NLS-1$
//		node.getName().accept(this);
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (!node.typeParameters().isEmpty()) {
//				this.buffer.append("<");//$NON-NLS-1$
//				for (Iterator it = node.typeParameters().iterator(); it.hasNext(); ) {
//					TypeParameter t = (TypeParameter) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(",");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(">");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append(" ");//$NON-NLS-1$
//		if (node.getAST().apiLevel() == JLS2) {
//			if (getSuperclass(node) != null) {
//				this.buffer.append("extends ");//$NON-NLS-1$
//				getSuperclass(node).accept(this);
//				this.buffer.append(" ");//$NON-NLS-1$
//			}
//			if (!superInterfaces(node).isEmpty()) {
//				this.buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$//$NON-NLS-1$
//				for (Iterator it = superInterfaces(node).iterator(); it.hasNext(); ) {
//					Name n = (Name) it.next();
//					n.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(", ");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(" ");//$NON-NLS-1$
//			}
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			if (node.getSuperclassType() != null) {
//				this.buffer.append("extends ");//$NON-NLS-1$
//				node.getSuperclassType().accept(this);
//				this.buffer.append(" ");//$NON-NLS-1$
//			}
//			if (!node.superInterfaceTypes().isEmpty()) {
//				this.buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$//$NON-NLS-1$
//				for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
//					Type t = (Type) it.next();
//					t.accept(this);
//					if (it.hasNext()) {
//						this.buffer.append(", ");//$NON-NLS-1$
//					}
//				}
//				this.buffer.append(" ");//$NON-NLS-1$
//			}
//		}
//		this.buffer.append("{\n");//$NON-NLS-1$
//		this.indent++;
//		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
//			BodyDeclaration d = (BodyDeclaration) it.next();
//			d.accept(this);
//		}
//		this.indent--;
//		printIndent();
//		this.buffer.append("}\n");//$NON-NLS-1$
		return true;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		if (node.getAST().apiLevel() == JLS2) {
			getTypeDeclaration(node).accept(this);
		}
		if (node.getAST().apiLevel() >= JLS3) {
			node.getDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		node.getType().accept(this);
		this.buffer.append(".class");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeMethodReference)
	 * 
	 * @since 3.10
	 */
	public boolean visit(TypeMethodReference node) {
		node.getType().accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeParameter)
	 * @since 3.1
	 */
	public boolean visit(TypeParameter node) {
//		if (node.getAST().apiLevel() >= AST.JLS8) {
//			printModifiers(node.modifiers());
//		}
//		node.getName().accept(this);
//		if (!node.typeBounds().isEmpty()) {
//			this.buffer.append(" extends ");//$NON-NLS-1$
//			for (Iterator it = node.typeBounds().iterator(); it.hasNext(); ) {
//				Type t = (Type) it.next();
//				t.accept(this);
//				if (it.hasNext()) {
//					this.buffer.append(" & ");//$NON-NLS-1$
//				}
//			}
//		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(UnionType)
	 * @since 3.7
	 */
	public boolean visit(UnionType node) {
//		for (Iterator it = node.types().iterator(); it.hasNext(); ) {
//			Type t = (Type) it.next();
//			t.accept(this);
//			if (it.hasNext()) {
//				this.buffer.append('|');
//			}
//		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
//		if (node.getAST().apiLevel() == JLS2) {
//			printModifiers(node.getModifiers());
//		}
//		if (node.getAST().apiLevel() >= JLS3) {
//			printModifiers(node.modifiers());
//		}
		this.buffer.append(strSplitCharacter);
		node.getType().accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			this.buffer.append(strSplitCharacter);
			f.accept(this);
			this.buffer.append(strSplitCharacter);
			if (it.hasNext()) {
				this.buffer.append(" , ");//$NON-NLS-1$
			}
		}
		this.buffer.append(strSplitCharacter);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		node.getName().accept(this);
		this.buffer.append(AnnotationType.Variable);
		int size = node.getExtraDimensions();
		if (node.getAST().apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for (int i = 0; i < size; i++) {
				visit((Dimension) dimensions.get(i));
			}
		} else {
			for (int i = 0; i < size; i++) {
				this.buffer.append("[]");//$NON-NLS-1$
			}
		}
		if (node.getInitializer() != null) {
			this.buffer.append(" = ");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		printIndent();
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		this.buffer.append(AnnotationType.Type);
		this.buffer.append(" ");//$NON-NLS-1$
		
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
			if (it.hasNext()) {
				this.buffer.append(", ");//$NON-NLS-1$
			}
		}
	//	this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		//printIndent();
		this.buffer.append(" while ( ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(" ) ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(strSplitCharacter);		
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WildcardType)
	 * @since 3.1
	 */
	public boolean visit(WildcardType node) {
//		visitTypeAnnotations(node);
//		this.buffer.append("?");//$NON-NLS-1$
//		Type bound = node.getBound();
//		if (bound != null) {
//			if (node.isUpperBound()) {
//				this.buffer.append(" extends ");//$NON-NLS-1$
//			} else {
//				this.buffer.append(" super ");//$NON-NLS-1$
//			}
//			bound.accept(this);
//		}
		return false;
	}

	/**
	 * @deprecated
	 */
	private void visitComponentType(ArrayType node) {
		node.getComponentType().accept(this);
	}

}
