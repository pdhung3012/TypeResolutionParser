package data;

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
	
	APIPackageNode root = new APIPackageNode("", null);
	HashMap<String, HashSet<APIType>> nameTypes = new HashMap<>();
	HashMap<String, HashSet<APIMethod>> nameMethods = new HashMap<>();
	HashMap<String, HashSet<APIField>> nameFields= new HashMap<>();
	int numOfTypes = 0, numOfMethods = 0, numOfFields = 0;
	
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
		add(nameTypes, type.name, type);
		for (String name : type.methods.keySet()) {
			ArrayList<APIMethod> methods = type.methods.get(name);
			add(nameMethods, name, methods);
			numOfMethods += methods.size();
		}
		for (String name : type.fields.keySet())
			add(nameFields, name, type.fields.get(name));
		numOfFields += type.fields.size();
	}
	
	private <E> void add(HashMap<String, HashSet<E>> map, String key, Collection<E> c) {
		HashSet<E> s = map.get(key);
		if (s == null) {
			s = new HashSet<>();
			map.put(key, s);
		}
		s.addAll(c);
	}
	
	private <E> void add(HashMap<String, HashSet<E>> map, String key, E e) {
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
		return this.nameTypes.get(name);
	}
	
	public HashSet<APIMethod> getMethodsByName(String name) {
		return this.nameMethods.get(name);
	}
	
	public HashSet<APIField> getFieldsByName(String name) {
		return this.nameFields.get(name);
	}
	
}
