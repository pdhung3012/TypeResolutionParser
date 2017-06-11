package dictionary;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import utils.FileUtil;

public class APIDictionary implements Serializable {
	private static final long serialVersionUID = -8764767855443226874L;
	
	static final HashMap<String, Integer> nameIndex = new HashMap<>();
	static final HashMap<Integer, String> indexName = new HashMap<>();
	
	APIPackageNode root = new APIPackageNode(getId(""), null);
	HashMap<Integer, HashSet<APIType>> nameTypes = new HashMap<>();
	HashMap<Integer, HashSet<APIMethod>> nameMethods = new HashMap<>();
	HashMap<Integer, HashSet<APIField>> nameFields= new HashMap<>();
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
	
	static Integer getId(String name) {
		Integer id = nameIndex.get(name);
		if (id == null) {
			id = nameIndex.size();
			nameIndex.put(name, id);
			indexName.put(id, name);
		}
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
		System.out.println("Building maps");
		build(root);
		System.out.println("Done building maps");
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
		System.out.println("Building maps");
		build(root);
		System.out.println("Done building maps");
	}

	private void build(APIPackageNode pn) {
		for (APIType type : pn.types.values()) {
			build(type);
			numOfTypes++;
		}
		for (APIPackageNode sub : pn.subPackages.values())
			build(sub);
	}

	private void build(APIType type) {
		add(nameTypes, type.getNameId(), type);
		for (Integer name : type.getMethods().keySet()) {
			ArrayList<APIMethod> methods = type.getMethods().get(name);
			add(nameMethods, name, methods);
			numOfMethods += methods.size();
		}
		for (Integer name : type.getFields().keySet())
			add(nameFields, name, type.getFields().get(name));
		numOfFields += type.getFields().size();
	}
	
	private <E> void add(HashMap<Integer, HashSet<E>> map, Integer key, Collection<E> c) {
		HashSet<E> s = map.get(key);
		if (s == null) {
			s = new HashSet<>();
			map.put(key, s);
		}
		s.addAll(c);
	}
	
	private <E> void add(HashMap<Integer, HashSet<E>> map, Integer key, E e) {
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
		Integer key = nameIndex.get(name);
		return this.nameTypes.get(key);
	}
	
	public HashSet<APIMethod> getMethodsByName(String name) {
		Integer key = nameIndex.get(name);
		return this.nameMethods.get(key);
	}
	
	public HashSet<APIField> getFieldsByName(String name) {
		Integer key = nameIndex.get(name);
		return this.nameFields.get(key);
	}
	
}
