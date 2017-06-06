package data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import utils.FileUtil;

public class UnionSignatures {

	public static void main(String[] args) {
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		HashSet<String> types = new HashSet<>(), methods = new HashSet<>(), fields = new HashSet<>();
		File in = new File("T:/type-resolution");
		int i = 0;
		for (File file : in.listFiles()) {
			if (file.getName().endsWith("-types")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String s : content)
					types.add(s);
			} else if (file.getName().endsWith("-methods")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String s : content)
					methods.add(s);
			} else if (file.getName().endsWith("-fields")) {
				ArrayList<String> content = FileUtil.getFileStringArray(file.getAbsolutePath());
				for (String s : content)
					fields.add(s);
			}
			long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			System.out.println("Memory usage: " + (afterUsedMem - beforeUsedMem) / 1000 / 1000);
		}
		long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Types: " + types.size());
		System.out.println("Methods: " + methods.size());
		System.out.println("Fields: " + fields.size());
		System.out.println("Memory usage: " + (afterUsedMem - beforeUsedMem) / 1000 / 1000);
		
		FileUtil.writeObjectToFile(types, "T:/temp/types.dat", false);
		FileUtil.writeObjectToFile(methods, "T:/temp/methods.dat", false);
		FileUtil.writeObjectToFile(fields, "T:/temp/fields.dat", false);
	}

}
