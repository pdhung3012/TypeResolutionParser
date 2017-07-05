package dictionary;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import utils.FileUtil;

public class APIDictionary implements Serializable {
	private static final long serialVersionUID = -8764767855443226874L;
	
	static final TObjectIntHashMap<String> nameIndex = new TObjectIntHashMap<>();
	static final TIntObjectHashMap<String> indexName = new TIntObjectHashMap<>();
	
	APIPackageNode root = new APIPackageNode(getId(""), null);
	TIntObjectHashMap<HashSet<APIType>> nameTypes = new TIntObjectHashMap<>();
	TIntObjectHashMap<HashSet<APIMethod>> nameMethods = new TIntObjectHashMap<>();
	TIntObjectHashMap<HashSet<APIField>> nameFields= new TIntObjectHashMap<>();
	int numOfTypes = 0, numOfMethods = 0, numOfFields = 0;
	
	public int getNumOfTypes() {
		return numOfTypes;
	}

	public int getNumOfMethods() {
		return numOfMethods;
	}

	public int getNumOfFields() {
		return numOfFields;
	}
	
	static int getId(String name) {
		int id = 0;
		if (!nameIndex.containsKey(name)) {
			id = nameIndex.size();
			nameIndex.put(name, id);
			indexName.put(id, name);
		} else
			id = nameIndex.get(name);
		return id;
	}

	static String getName(int id) {
		return indexName.get(id);
	}

	public void build(File dir) {
		System.out.println("Building types");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith("-types")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addType(line);
			}
		}
		System.out.println("Done building types");
		System.gc();
		System.out.println("Building methods and fields");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith("-methods")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addMethod(line);
			} else if (file.getName().endsWith("-fields")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addField(line);
			}
		}
		System.out.println("Done building methods and fields");
		System.gc();
		System.out.println("Building maps");
		build(root);
		System.out.println("Done building maps");
		System.gc();
	}

	public void build(File dir, String list, int max) {
		System.out.println("Building types");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".jar-types")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addType(line);
			}
		}
		List<String> names = FileUtil.getFileStringArray(list);
		names = names.subList(0, Math.min(max, names.size()));
		for (String name : names) {
			ArrayList<String> content = FileUtil.getFileStringArray(dir.getAbsolutePath() + "/" + name.replace("/", "___") + "-types");
			for (String line : content)
				addType(line);
		}
		System.out.println("Done building types");
		System.gc();
		System.out.println("Building fields");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".jar-fields")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addField(line);
			}
		}
		for (String name : names) {
			ArrayList<String> content = FileUtil.getFileStringArray(dir.getAbsolutePath() + "/" + name.replace("/", "___") + "-fields");
			for (String line : content)
				addField(line);
		}
		System.out.println("Done building fields");
		System.gc();
		System.out.println("Building methods");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".jar-methods")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String line : content)
					addMethod(line);
			} 
		}
		for (String name : names) {
			ArrayList<String> content = FileUtil.getFileStringArray(dir.getAbsolutePath() + "/" + name.replace("/", "___") + "-methods");
			for (String line : content)
				addMethod(line);
		}
		System.out.println("Done building methods");
		System.gc();
		System.out.println("Building maps");
		build(root);
		System.out.println("Done building maps");
		System.gc();
	}

	private void build(APIPackageNode pn) {
		for (APIType type : pn.types.values(new APIType[0])) {
			build(type);
			numOfTypes++;
		}
		for (APIPackageNode sub : pn.subPackages.values(new APIPackageNode[0]))
			build(sub);
	}

	private void build(APIType type) {
		add(nameTypes, type.getNameId(), type);
		for (int name : type.getMethods().keys()) {
			ArrayList<APIMethod> methods = type.getMethods().get(name);
			add(nameMethods, name, methods);
			numOfMethods += methods.size();
		}
		for (int name : type.getFields().keys())
			add(nameFields, name, type.getFields().get(name));
		numOfFields += type.getFields().size();
	}
	
	private <E> void add(TIntObjectHashMap<HashSet<E>> map, int key, Collection<E> c) {
		HashSet<E> s = map.get(key);
		if (s == null) {
			s = new HashSet<>();
			map.put(key, s);
		}
		s.addAll(c);
	}
	
	private <E> void add(TIntObjectHashMap<HashSet<E>> map, int key, E e) {
		HashSet<E> s = map.get(key);
		if (s == null) {
			s = new HashSet<>();
			map.put(key, s);
		}
		s.add(e);
	}

	private APIMethod addMethod(String methodInfo) {
		String[] parts = methodInfo.split(" ");
		String methodName = parts[0];
		int index = methodName.lastIndexOf('.');
		String typeName = methodName.substring(0, index);
		APIType type = addType(typeName);
		APIType returnType = addType(parts[2]);
		String name = methodName.substring(index + 1);
		if (parts[1].length() > 2)
			parts = parts[1].substring(1, parts[1].length()-2).split(",");
		else
			parts = new String[0];
		APIType[] parameterTypes = new APIType[parts.length];
		for (int i = 0; i < parts.length; i++) {
			parameterTypes[i] = addType(parts[i]);
		}
		APIMethod method = type.addMethod(name, parameterTypes, returnType);
		return method;
	}

	private APIField addField(String fieldInfo) {
		String[] parts = fieldInfo.split(" ");
		String fieldName = parts[0], fieldTypeName = parts[1];
		int index = fieldName.lastIndexOf('.');
		String typeName = fieldName.substring(0, index);
		APIType type = addType(typeName);
		APIType fieldType = addType(fieldTypeName);
		String name = fieldName.substring(index + 1);
		APIField field = type.addField(name, fieldType);
		return field;
	}

	private APIType addType(String typeName) {
		String[] parts = typeName.split("\\.");
		APIPackageNode p = root;
		int i = 0;
		while (i < parts.length - 1) {
			APIPackageNode sub = p.addSubPackage(parts[i]);
			p = sub;
			i++;
		}
		APIType type = p.addType(parts[i]);
		return type;
	}
	
	public HashSet<APIType> getTypesByName(String name) {
		int key = nameIndex.get(name);
		return this.nameTypes.get(key);
	}
	
	/**
	 * 
	 * @param name the fully qualified name of a type
	 * @return only a single type
	 */
	public APIType getTypeByFullName(String name)
	{
		String simpleName = name.substring(name.lastIndexOf('.')+1);
		HashSet<APIType> types = this.getTypesByName(simpleName);
		if (types != null)
		{
			for(APIType type: types)
			{
				if(type.getFQN().equals(name))
				{
					return type;
				}
			}
		}
		return null;
	}
	
	public HashSet<APIMethod> getMethodsByName(String name) {
		int key = nameIndex.get(name);
		return this.nameMethods.get(key);
	}
	
	public HashSet<APIField> getFieldsByName(String name) {
		int key = nameIndex.get(name);
		return this.nameFields.get(key);
	}
	
	/**
	 * @param name fully qualified name of method
	 * @return only single type
	 */
	public APIType getReturnTypeByMethod(String name)
	{
		String methodName = name.substring(name.lastIndexOf('.')+1);
		String fullName = name.substring(0, name.lastIndexOf('.'));
		HashSet<APIMethod> methods = this.getMethodsByName(methodName);
		if(methods == null)
		{
			return null;
		}
		else{
		for(APIMethod method: methods)
		{
			if(method.getType().getFQN().equals(fullName))
			{
				return method.getReturnType();
			}
		}}
		return null;
	}
	
	/**
	 * 
	 * @param name fully quallified name of a method
	 * @return APIMethod the method
	 */
	public APIMethod getMethodByFullName(String name)
	{
		String methodName = name.substring(name.lastIndexOf('.')+1);
		String fullName = name.substring(0, name.lastIndexOf('.'));
		APIType theType = this.getTypeByFullName(fullName);
		if( theType == null)
		{
			return null;
		}
		else
		{
			if (theType.getMethods(methodName) != null)
			{
				for(APIMethod method: theType.getMethods(methodName))
				{
					if( method.getName().equals(methodName))
					{
						return method;
					}
				}
			}
		}
		return null;
//		HashSet<APIMethod> methods = this.getMethodsByName(methodName);
//		if(methods == null)
//		{
//			return null;
//		}
//		else
//		{
//			for(APIMethod method: methods)
//			{
//				if(method.getType().getFQN().equals(fullName))
//				{
//					return method;
//				}
//			}
//		}
//		return null;
	}
	public HashSet<APIType> getTypesbyMethod(String name) {
		HashSet<APIType> types = new HashSet<APIType>();
		HashSet<APIMethod> methods = this.getMethodsByName(name);
		if (methods != null)
		{
		for(APIMethod method: methods)
			types.add(method.getType());
		return types;
		}
		else
		{
			return null;
		}
	}
	
	public HashSet<APIField> getFields(String className, String fieldName) {
		HashSet<APIField> fields = new HashSet<>();
		HashSet<APIType> types = getTypesByName(className);
		for (APIType type : types)
			fields.add(type.getField(fieldName));
		return fields;
	}
	
	public HashSet<APIMethod> getMethods(String className, String methodName) {
		HashSet<APIMethod> methods = new HashSet<>();
		HashSet<APIType> types = getTypesByName(className);
		if(types != null)
		{
		for (APIType type : types)
			if (type.getMethods(methodName)!= null){
			methods.addAll(type.getMethods(methodName));}
			else return null;
		return methods;
		}
		return null;
	}
}
