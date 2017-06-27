package baker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.jdt.core.dom.*;
import data.*;
import dictionary.*;

public class BakerVisitor extends ASTVisitor {
	private static final String SEPARATOR = "#";
	public HashMap<String, HashSet<APIType>> candTypes;
	public HashMap<String, HashSet<APIMethod>> candMethods;
	public HashMap<String, HashSet<APIField>> candFields;
	public HashMap<String, String> trueTypes;
	private APIDictionary dictionary;
	private String className, superClassName;
	private int numOfExpressions = 0, numOfResolvedExpressions = 0;
	private StringBuilder fullTokens = new StringBuilder(), partialTokens = new StringBuilder();
	private String fullSequence = null, partialSequence = null;
	private String[] fullSequenceTokens, partialSequenceTokens;
	private boolean testing = true;

	public BakerVisitor(String className, String superClassName, HashMap<String, HashSet<APIType>> candTypes, HashMap<String, String> trueTypes, APIDictionary dictionary) {
		super(false);
		this.className = className;
		this.superClassName = superClassName;
		this.candTypes = candTypes;
		this.trueTypes = trueTypes;
		this.dictionary = dictionary;
	}

	public HashMap<String, HashSet<APIType>> getTypes ()
	{
		return candTypes;
	}

	public HashMap<String, HashSet<APIMethod>> getMethods ()
	{
		return candMethods;
	}

	public HashMap<String, HashSet<APIField>> getFields ()
	{
		return candFields;
	}
	public String[] getFullSequenceTokens() {
		if (fullSequenceTokens == null)
			buildFullSequence();
		return fullSequenceTokens;
	}

	public String[] getPartialSequenceTokens() {
		if (partialSequenceTokens == null)
			buildPartialSequence();
		return partialSequenceTokens;
	}

	public String getFullSequence() {
		if (fullSequence == null)
			buildFullSequence();
		return fullSequence;
	}

	public String getPartialSequence() {
		if (partialSequence == null)
			buildPartialSequence();
		return partialSequence;
	}

	private void buildFullSequence() {
		ArrayList<String> parts = buildSequence(fullTokens);
		this.fullSequence = parts.get(0);
		this.fullSequenceTokens = new String[parts.size() - 1];
		for (int i = 1; i < parts.size(); i++)
			this.fullSequenceTokens[i-1] = parts.get(i);
	}

	private void buildPartialSequence() {
		ArrayList<String> parts = buildSequence(partialTokens);
		this.partialSequence = parts.get(0);
		this.partialSequenceTokens = new String[parts.size() - 1];
		for (int i = 1; i < parts.size(); i++)
			this.partialSequenceTokens[i-1] = parts.get(i);
	}

	private ArrayList<String> buildSequence(StringBuilder tokens) {
		tokens.append(" ");
		ArrayList<String> l = new ArrayList<>();
		StringBuilder sequence = new StringBuilder(), token = null;
		for (int i = 0; i < tokens.length(); i++) {
			char ch = tokens.charAt(i);
			if (ch == ' ') {
				if (token != null) {
					String t = token.toString();
					l.add(t);
					sequence.append(t + " ");
					token = null;
				}
			} else {
				if (token == null)
					token = new StringBuilder();
				token.append(ch);
			}
		}
		l.add(0, sequence.toString());
		return l;
	}

	public int getNumOfExpressions() {
		return numOfExpressions;
	}

	public int getNumOfResolvedExpressions() {
		return numOfResolvedExpressions;
	}

	private Type getType(VariableDeclarationFragment node) {
		ASTNode p = node.getParent();
		if (p instanceof VariableDeclarationExpression)
			return ((VariableDeclarationExpression) p).getType();
		if (p instanceof VariableDeclarationStatement)
			return ((VariableDeclarationStatement) p).getType();
		return null;
	}

	private String getSignature(IMethodBinding method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getTypeDeclaration().getQualifiedName());
		sb.append("." + method.getName());
		sb.append("(");
		sb.append(SEPARATOR);
		for (ITypeBinding tb : method.getParameterTypes())
			sb.append(tb.getTypeDeclaration().getName() + "#");
		sb.append(")");
		return sb.toString();
	}

	static String getUnresolvedType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getUnresolvedType(t.getElementType()) + getDimensions(t.getDimensions());
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getUnresolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " & " + getUnresolvedType(types.get(i));
			return s;
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getUnresolvedType(t.getType());
		} else if (type.isUnionType()) {
			UnionType it = (UnionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getUnresolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " | " + getUnresolvedType(types.get(i));
			return s;
		} else if (type.isNameQualifiedType()) {
			NameQualifiedType qt = (NameQualifiedType) type;
			return qt.getQualifier().getFullyQualifiedName() + "." + qt.getName().getIdentifier();
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) type;
			return getUnresolvedType(qt.getQualifier()) + "." + qt.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			return type.toString();
		} else if (type.isWildcardType()) {
			WildcardType wt = (WildcardType) type;
			String s = "?";
			if (wt.getBound() != null) {
				if (wt.isUpperBound())
					s += "extends ";
				else
					s += "super ";
				s += getUnresolvedType(wt.getBound());
			}
			return s;
		}

		return null;
	}

	private static String getDimensions(int dimensions) {
		String s = "";
		for (int i = 0; i < dimensions; i++)
			s += "[]";
		return s;
	}

	static String getResolvedType(Type type) {
		ITypeBinding tb = type.resolveBinding();
		if (tb == null || tb.isRecovered())
			return getUnresolvedType(type);
		tb = tb.getTypeDeclaration();
		if (tb.isLocal() || tb.getQualifiedName().isEmpty())
			return getUnresolvedType(type);
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			return getResolvedType(t.getElementType()) + getDimensions(t.getDimensions());
		} else if (type.isIntersectionType()) {
			IntersectionType it = (IntersectionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getResolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " & " + getResolvedType(types.get(i));
			return s;
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getResolvedType(t.getType());
		} else if (type.isUnionType()) {
			UnionType it = (UnionType) type;
			@SuppressWarnings("unchecked")
			ArrayList<Type> types = new ArrayList<>(it.types());
			String s = getResolvedType(types.get(0));
			for (int i = 1; i < types.size(); i++)
				s += " | " + getResolvedType(types.get(i));
			return s;
		} else if (type.isNameQualifiedType()) {
			return tb.getQualifiedName();
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			return tb.getQualifiedName();
		} else if (type.isSimpleType()) {
			return tb.getQualifiedName();
		} else if (type.isWildcardType()) {
			WildcardType wt = (WildcardType) type;
			String s = "?";
			if (wt.getBound() != null) {
				if (wt.isUpperBound())
					s += "extends ";
				else
					s += "super ";
				s += getResolvedType(wt.getBound());
			}
			return s;
		}

		return null;
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Expression) {
			numOfExpressions++;
			Expression e = (Expression) node;
			if (e.resolveTypeBinding() != null && !e.resolveTypeBinding().isRecovered())
				numOfResolvedExpressions++;
		} else if (node instanceof Statement) {
			if (node instanceof ConstructorInvocation) {
				numOfExpressions++;
				if (((ConstructorInvocation) node).resolveConstructorBinding() != null && !((ConstructorInvocation) node).resolveConstructorBinding().isRecovered())
					numOfResolvedExpressions++;
			} else if (node instanceof SuperConstructorInvocation) {
				numOfExpressions++;
				if (((SuperConstructorInvocation) node).resolveConstructorBinding() != null && !((SuperConstructorInvocation) node).resolveConstructorBinding().isRecovered())
					numOfResolvedExpressions++;
			}
		} else if (node instanceof Type) {
			numOfExpressions++;
			Type t = (Type) node;
			if (t.resolveBinding() != null && !t.resolveBinding().isRecovered())
				numOfResolvedExpressions++;
		}
	}

	@Override
	public boolean visit(ArrayAccess node) {
		//node.getArray() instanceof Name
		//then resolve, and return false

		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.partialTokens.append(" new " + utype + " ");
		this.fullTokens.append(" new " + rtype + " ");
		if (node.getInitializer() != null)
			node.getInitializer().accept(this);
		else
			for (int i = 0; i < node.dimensions().size(); i++)
				((Expression) (node.dimensions().get(i))).accept(this);
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		this.fullTokens.append(" assert ");
		this.partialTokens.append(" assert ");
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		Expression left = node.getLeftHandSide();
		Expression right = node.getRightHandSide();
		left.accept(this);
		this.fullTokens.append(" = ");
		this.partialTokens.append(" = ");
		right.accept(this);
		//variable = variable
		if( left instanceof SimpleName )
		{
			String leftKey = ((SimpleName) left).getIdentifier().toString();
			HashSet<APIType> leftList = candTypes.get(leftKey);
			String rightKey;
			//variable assigns to variable
			if( right instanceof SimpleName)
			{
				System.out.println("blablabalbalabl");
				rightKey =  ((SimpleName) right).getIdentifier().toString();
				if ( candTypes.containsKey(rightKey) )
				{
					HashSet<APIType> rightList = candTypes.get(rightKey);
					if(leftList!= null && rightList != null)
					{
						leftList.retainAll(rightList);
						rightList.retainAll(leftList);
						candTypes.put(leftKey, leftList);
						candTypes.put(rightKey, rightList);
						if(testing){
							System.out.println("Key "+ leftKey + " and its candidate list: ");
							for(APIType type: candTypes.get(leftKey))
							{
								System.out.print(type.toString() + ", ");
							}
							System.out.println();
						}}
				}
			}

			//variable assigns to methodInvocation
			if( right instanceof MethodInvocation)
			{
				rightKey = ((MethodInvocation) right).getName().toString();
			}
			//variable assigns to fieldAccess
			//variable assigns to 'new' keyword
			//variable assigns to Literal -- not handling
		}
		return false;
	}

	@Override
	public boolean visit(Block node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		this.fullTokens.append(" boolean ");
		this.partialTokens.append(" boolean ");
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.fullTokens.append(" " + rtype + " <cast> ");
		this.partialTokens.append(" " + utype + " <cast> ");
		node.getExpression().accept(this);
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		this.fullTokens.append(" char ");
		this.partialTokens.append(" char ");
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType());
		IMethodBinding b = node.resolveConstructorBinding();
		if (b == null)
			this.fullTokens.append(" new " + utype + "(" + node.arguments().size() + ") ");
		else
			this.fullTokens.append(" new " + getSignature(b.getMethodDeclaration()) + " ");
		this.partialTokens.append(" new " + utype + "(" + node.arguments().size() + ") ");
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		if (node.getAnonymousClassDeclaration() != null)
			node.getAnonymousClassDeclaration().accept(this);
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		IMethodBinding b = node.resolveConstructorBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
		}
		String name = "." + className + "(" + node.arguments().size() + ")";
		this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getSignature(b.getMethodDeclaration());
		this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return false;
	}

	@Override
	public boolean visit(CreationReference node) {
		return false;
	}

	@Override
	public boolean visit(Dimension node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding b = node.resolveFieldBinding();
		ITypeBinding tb = null;
		if (b != null) {
			tb = b.getDeclaringClass();
			if (tb != null) {
				tb = tb.getTypeDeclaration();
				if (tb.isLocal() || tb.getQualifiedName().isEmpty())
					return false;
			}
		}
		this.fullTokens.append(" ");
		this.partialTokens.append(" ");
		node.getExpression().accept(this);
		String name = "." + node.getName().getIdentifier();
		this.partialTokens.append(" " + name + " ");
		if (b != null) {
			if (tb != null)
				name = getQualifiedName(tb.getTypeDeclaration()) + name;
			/*else
				name = "Array" + name;*/
		}
		this.fullTokens.append(" " + name + " ");
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		this.fullTokens.append(" ");
		this.partialTokens.append(" ");
		node.getLeftOperand().accept(this);
		this.fullTokens.append(" <instanceof> ");
		this.partialTokens.append(" <instanceof> ");
		String rtype = getResolvedType(node.getRightOperand()), utype = getUnresolvedType(node.getRightOperand());
		this.fullTokens.append(rtype + " ");
		this.partialTokens.append(utype + " ");
		return false;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getBody() != null && !node.getBody().statements().isEmpty())
			node.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null && node.getExpression() instanceof TypeLiteral) {
			TypeLiteral lit = (TypeLiteral) node.getExpression();
			String utype = getUnresolvedType(lit.getType()), rtype = getResolvedType(lit.getType());
			this.fullTokens.append(" " + rtype + ".class." + node.getName().getIdentifier() + "() ");
			this.partialTokens.append(" " + utype + ".class." + node.getName().getIdentifier() + "(" + node.arguments().size() + ") ");
		} else {
			IMethodBinding b = node.resolveMethodBinding();
			ITypeBinding tb = null;
			if (b != null) {
				tb = b.getDeclaringClass();
				if (tb != null) {
					tb = tb.getTypeDeclaration();
					if (tb.isLocal() || tb.getQualifiedName().isEmpty())
						return false;
				}
			}
			this.fullTokens.append(" ");
			this.partialTokens.append(" ");
			if (node.getExpression() != null) 
			{
				//the object was invoked on
				String key = node.getExpression().toString();
				if (node.getExpression() instanceof MethodInvocation)  //Nested method invocation
				{
					MethodInvocation tempNode = ((MethodInvocation) node.getExpression());
					//Visit from left to right
					tempNode.accept(this);
					//Parent receiver
					if (tempNode.getExpression() != null)
					{
						String subkey =  tempNode.getExpression().toString();	
						//Parent method
						String tempMethod = node.getName().getIdentifier() + "(" + node.arguments().size() + ")";
						HashSet<APIType> receiver = dictionary.getTypesbyMethod(tempMethod);
						//Receiver must match return type of parent method
						if (candTypes.containsKey(subkey) && receiver != null)
						{
							if(candTypes.get(subkey) != null){
							HashSet<APIType> candidateList = new HashSet<APIType>();
							for(APIType type: candTypes.get(subkey))
							{
								APIType returnType = null;
								//android.webkit.WebView.getSettings + abc.getSettings
								String fullMethodName = type.getFQN() + "."+tempNode.getName().getIdentifier() + "(" + tempNode.arguments().size() + ")";
								returnType = dictionary.getReturnTypeByMethod(fullMethodName);
								if(returnType != null){
								if(receiver.contains(returnType))
								{
									candidateList.add(type);
								}}
							}
							//Update candidate List
							candTypes.put(subkey, candidateList);
							}
						}
					}
				}
				else //Single method invocation
				{
					if (!candTypes.containsKey(key))
					{
						HashSet<APIType> candidateList = dictionary.getTypesByName(key);
						candTypes.put(key, candidateList);
						if (!trueTypes.containsKey(key))
						{
							String value = "";
							if(node.getExpression().resolveTypeBinding() != null)
							{
								value = node.getExpression().resolveTypeBinding().getQualifiedName();
								if(!node.getExpression().resolveTypeBinding().isPrimitive())
								{trueTypes.put(key, value);}
							}
							else
							{
								if(testing){
									System.out.println("Cant resolve " + key);}
							}

						}
					}
					HashSet<APIType> candidateList = candTypes.get(key);
					String method = node.getName().getIdentifier() + "(" + node.arguments().size() + ")";
					if(testing){System.out.println("Method: " + method);}
					HashSet<APIType> matchedType = dictionary.getTypesbyMethod(method);

					if(matchedType != null)
					{
						if(candidateList == null)
						{
							candidateList = matchedType;
						}
						else
						{
							candidateList.retainAll(matchedType);
						}
					}
					//Update candidate types with new candidate list
					candTypes.put(key, candidateList);
					node.getExpression().accept(this);
					if(candTypes.get(key) != null){
						if(testing){
							System.out.println("Key "+ key + " and its candidate list: ");
							for(APIType type: candTypes.get(key))
							{
								System.out.print(type.toString() + ", ");
							}
							System.out.println();
						}}
					
					//Deductive Linking through argument
					for (int i = 0; i < node.arguments().size(); i++)
					{
						ASTNode argNode = (ASTNode) node.arguments().get(i);
						if ( argNode instanceof SimpleName )
						{
							String argKey = ((SimpleName) argNode).getIdentifier();
							
							if ( candTypes.containsKey(argKey))
							{
								System.out.println("Arg Key " + argKey);
								HashSet<APIMethod> methods = new HashSet();
								HashSet<APIType> matchedTypes = new HashSet();
								HashSet<APIType> argTypes = candTypes.get(argKey);
								if( candidateList != null)
								{
									for( APIType type: candidateList)
									{
										//method = node.getName().getIdentifier();
										String tempMethod = type.getFQN() + "." + method;
										System.out.println(tempMethod);
										if (dictionary.getMethodByFullName(tempMethod) != null)
											{ methods.add(dictionary.getMethodByFullName(tempMethod));}
									}
									
									if(methods != null){
									for( APIMethod aMethod: methods )
									{
										System.out.println("Here " + aMethod.getFQN());
										APIType[] tempTypes = aMethod.getParameterTypes();
										if(tempTypes[i] != null){
										matchedTypes.add(aMethod.getParameterTypes()[i]);}
									}
									if(matchedTypes != null){
									argTypes.retainAll(matchedTypes);
									candTypes.put(argKey, argTypes);}
									}
								}
							}
						}
					}
				}
			} 
			else 
			{
				if (tb != null) {
					this.partialTokens.append(" " + getName(tb) + " ");
					this.fullTokens.append(" " + getQualifiedName(tb) + " ");

				} else {
					this.partialTokens.append(" this ");
					this.fullTokens.append(" this ");
				}
			}

			String name = "."+ node.getName().getIdentifier() + "(" + node.arguments().size() + ")";	
			this.partialTokens.append(" " + name + " ");
			if (tb != null)
				name = getSignature(b.getMethodDeclaration());
			this.fullTokens.append(" " + name + " ");

		}
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);

		return false;
	}



	@Override
	public boolean visit(Modifier node) {
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		this.fullTokens.append(" null ");
		this.partialTokens.append(" null ");
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		this.fullTokens.append(" number ");
		this.partialTokens.append(" number ");
		return false;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		IBinding b = node.resolveBinding();
		IVariableBinding vb = null;
		ITypeBinding tb = null;
		if (b != null) {
			if (b instanceof IVariableBinding) {
				vb = (IVariableBinding) b;
				tb = vb.getDeclaringClass();
				if (tb != null) {
					tb = tb.getTypeDeclaration();
					if (tb.isLocal() || tb.getQualifiedName().isEmpty())
						return false;
				}
			} else if (b instanceof ITypeBinding) {
				tb = ((ITypeBinding) b).getTypeDeclaration();
				if (tb.isLocal() || tb.getQualifiedName().isEmpty())
					return false;
				this.partialTokens.append(" " + node.getFullyQualifiedName() + " ");
				this.fullTokens.append(" " + getQualifiedName(tb) + " ");
				return false;
			}
		} else {
			this.partialTokens.append(" " + node.getFullyQualifiedName() + " ");
			this.fullTokens.append(" " + node.getFullyQualifiedName() + " ");
			return false;
		}
		node.getQualifier().accept(this);
		String name = "." + node.getName().getIdentifier();
		this.partialTokens.append(" " + name + " ");
		if (b != null) {
			if (b instanceof IVariableBinding) {
				if (tb != null)
					name = getQualifiedName(tb.getTypeDeclaration()) + name;
				/*else
					name = "Array" + name;*/
			}
		}
		this.fullTokens.append(" " + name + " ");
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		//Not really resolve itself but its parent
		IBinding b = node.resolveBinding();
		if (b != null) {
			if (b instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding) b;
				ITypeBinding tb = vb.getType();
				if (tb != null) {
					tb = tb.getTypeDeclaration();
					if (tb.isLocal() || tb.getQualifiedName().isEmpty())
						return false;
					this.fullTokens.append(" " + getQualifiedName(tb) + " ");
					this.partialTokens.append(" " + getName(tb) + " ");
				}
			} else if (b instanceof ITypeBinding) {
				ITypeBinding tb = (ITypeBinding) b;
				tb = tb.getTypeDeclaration();
				if (tb.isLocal() || tb.getQualifiedName().isEmpty())
					return false;
				this.fullTokens.append(" " + getQualifiedName(tb) + " ");
				this.partialTokens.append(" " + getName(tb) + " ");
			}
		} else {
			this.fullTokens.append(" " + node.getIdentifier() + " ");
			this.partialTokens.append(" " + node.getIdentifier() + " ");
		}
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.partialTokens.append(" " + utype + " ");
		this.fullTokens.append(" " + rtype + " ");
		if (node.getInitializer() != null) {
			this.partialTokens.append("= ");
			this.fullTokens.append("= ");
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		this.fullTokens.append(" java.lang.String ");
		this.partialTokens.append(" java.lang.String ");
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		IMethodBinding b = node.resolveConstructorBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
		}
		String name = "." + superClassName + "(" + node.arguments().size() + ")";
		this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getSignature(b.getMethodDeclaration());
		this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		IVariableBinding b = node.resolveFieldBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null) {
			tb = b.getDeclaringClass().getTypeDeclaration();
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
			this.partialTokens.append(" " + getName(tb) + " ");
			this.fullTokens.append(" " + getQualifiedName(tb) + " ");
		} else {
			this.partialTokens.append(" super ");
			this.fullTokens.append(" super ");
		}
		String name = "." + node.getName().getIdentifier();
		this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getQualifiedName(tb) + name;
		this.fullTokens.append(" " + name + " ");
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding b = node.resolveMethodBinding();
		ITypeBinding tb = null;
		if (b != null && b.getDeclaringClass() != null)
			tb = b.getDeclaringClass().getTypeDeclaration();
		if (tb != null) {
			if (tb.isLocal() || tb.getQualifiedName().isEmpty())
				return false;
			this.partialTokens.append(" " + getName(tb) + " ");
			this.fullTokens.append(" " + getQualifiedName(tb) + " ");
		} else {
			this.partialTokens.append(" super ");
			this.fullTokens.append(" super ");
		}
		String name = "." + node.getName().getIdentifier() + "(" + node.arguments().size() + ")";
		this.partialTokens.append(" " + name + " ");
		if (tb != null)
			name = getSignature(b.getMethodDeclaration());
		this.fullTokens.append(" " + name + " ");
		for (int i = 0; i < node.arguments().size(); i++)
			((ASTNode) node.arguments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		//can refer to super's field
		ITypeBinding b = node.resolveTypeBinding();
		if (b != null) {
			b = b.getTypeDeclaration();
			if (b.isLocal() || b.getQualifiedName().isEmpty())
				return false;
			this.partialTokens.append(" " + getName(b) + " ");
			this.fullTokens.append(" " + getQualifiedName(b) + " ");
		} else {
			this.partialTokens.append(" this ");
			this.fullTokens.append(" this ");
		}
		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.fullTokens.append(" " + rtype + ".class ");
		this.partialTokens.append(" " + utype + ".class ");
		return false;
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.partialTokens.append(" " + utype + " ");
		this.fullTokens.append(" " + rtype + " ");

		for (int i = 0; i < node.fragments().size(); i++)
			((ASTNode) node.fragments().get(i)).accept(this);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding tb = node.getType().resolveBinding();
		if (tb != null && tb.getTypeDeclaration().isLocal())
			return false;
		String utype = getUnresolvedType(node.getType()), rtype = getResolvedType(node.getType());
		this.partialTokens.append(" " + utype + " ");
		this.fullTokens.append(" " + rtype + " ");
		for (int i = 0; i < node.fragments().size(); i++)
			((ASTNode) node.fragments().get(i)).accept(this);

		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Type type = getType(node);
		String utype = getUnresolvedType(type), rtype = getResolvedType(type);
		this.partialTokens.append(" " + utype + " ");
		this.fullTokens.append(" " + rtype + " ");


		if (node.getInitializer() != null) {
			this.partialTokens.append("= ");
			this.fullTokens.append("= ");
			node.getInitializer().accept(this);
		}

		String key = node.getName().toString();
		if(!candTypes.containsKey(key))
		{
			HashSet<APIType> candList = dictionary.getTypesByName(type.toString());
			if(candList != null)
			{
				candTypes.put(key, candList);
			}
			else
			{
				if(type.toString().contains("."))
				{
					candList = new HashSet<APIType>();
					candList.add(dictionary.getTypeByFullName(type.toString()));
					candTypes.put(key, candList);
				}
				else
				{
					candTypes.put(key, new HashSet<APIType>());
				}
			}
			if(candList != null)
			{if(testing){
				System.out.println("Key "+ key + " and its candidate list: ");
				for(APIType type1: candTypes.get(key))
				{
					if(type1 != null){
						System.out.print(type1.toString() + ", ");}
				}
				System.out.println();
			}}
		}
		if (!trueTypes.containsKey(key))
		{
			String value = "";
			if(node.resolveBinding() != null)
			{
				value = node.resolveBinding().getType().getTypeDeclaration().getQualifiedName();
				if(testing){System.out.println("True type for key " + key + " is " + value);}
				if(! node.resolveBinding().getType().isPrimitive()){
					trueTypes.put(key, value);}
			}
			else
			{
				if(testing){System.out.println("Cant resolve " + key);}
			}


		}
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		return false;
	}

	@Override
	public boolean visit(IntersectionType node) {
		return false;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return false;
	}

	@Override
	public boolean visit(UnionType node) {
		return false;
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		return false;
	}

	@Override
	public boolean visit(WildcardType node) {
		return false;
	}

	private String getQualifiedName(ITypeBinding tb) {
		if (tb.isArray())
			return getQualifiedName(tb.getComponentType().getTypeDeclaration()) + getDimensions(tb.getDimensions());
		return tb.getQualifiedName();
	}

	private String getName(ITypeBinding tb) {
		if (tb.isArray())
			return getName(tb.getComponentType().getTypeDeclaration()) + getDimensions(tb.getDimensions());
		return tb.getName();
	}

}
