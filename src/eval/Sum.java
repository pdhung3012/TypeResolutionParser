package eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import utils.FileUtil;

public class Sum {

	public static void main(String[] args) {
		HashMap<Integer, Integer> sequenceLengthCount = new HashMap<>();
		
		File dir = new File("T:/type-sequences-6-libs");
		for (File sublib : dir.listFiles()) {
			String lib = sublib.getName();
			if (!lib.endsWith("."))
				lib += ".";
			final HashMap<String, Integer> packageCount = new HashMap<>();
			for (File subp : sublib.listFiles()) {
				String content = FileUtil.getFileContent(subp.getAbsolutePath() + "/target.txt");
				Scanner sc = new Scanner(content);
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] tokens = line.split(" ");
					int l = tokens.length;
					if (l > 255)
						l = 256;
					update(sequenceLengthCount, l);
					HashSet<String> usedPackages = new HashSet<>();
					for (String token : tokens) {
						if (token.startsWith(lib)) {
							int index = lib.length()-1;
							for (int i = 0; i < 2; i++) {
								String p = token.substring(0, index+1);
								usedPackages.add(p);
								if (Character.isUpperCase(token.charAt(index+1)))
									break;
								index = token.indexOf('.', index+1);
								if (index == -1)
									break;
							}
						}
					}
					for (String p : usedPackages)
						update(packageCount, p);
				}
				sc.close();
			}
			ArrayList<String> list = new ArrayList<>(packageCount.keySet());
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return packageCount.get(s2) - packageCount.get(s1);
				}
			});
			System.out.println("\n"+ lib);
			for (String p : list)
				System.out.println(p + "\t" + packageCount.get(p));
		}
		ArrayList<Integer> l = new ArrayList<>(sequenceLengthCount.keySet());
		Collections.sort(l);
		for (int i : l)
			System.out.println(i + "\t " + sequenceLengthCount.get(i));
	}

	public static <K> void update(HashMap<K, Integer> countMap, K key) {
		int c = 1;
		if (countMap.containsKey(key))
			c += countMap.get(key);
		countMap.put(key, c);
	}

}
