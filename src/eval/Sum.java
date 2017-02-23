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
		HashMap<String, HashMap<String, Integer>> gram1Map = new HashMap<>(), gram2Map = new HashMap<>(), gram3Map = new HashMap<>(); 
		int N = 50;
		File dir = new File("T:/type-sequences-6-libs");
		for (File sublib : dir.listFiles()) {
			String lib = sublib.getName();
			if (!lib.endsWith("."))
				lib += ".";
			String names = FileUtil.getFileContent("T:/github/repos-5stars-50commits-lib-" + lib + ".csv");
			Scanner scnames = new Scanner(names);
			final HashMap<String, Integer> packageCount = new HashMap<>();
			for (int j = 0; j < N; j++) {
				String name = scnames.nextLine();
				int index = name.indexOf(',');
				if (index == -1)
					index = name.length();
				name = name.substring(0, index);
				File subp = new File(sublib, name.replace('/', '_'));
				String content = FileUtil.getFileContent(subp.getAbsolutePath() + "/target.txt");
				String contentSource = FileUtil.getFileContent(subp.getAbsolutePath() + "/source.txt");
				Scanner sc = new Scanner(content), scSource = new Scanner(contentSource);
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String source = scSource.nextLine();
					String[] tokens = line.split(" "), sTokens = source.split(" ");
					int l = tokens.length;
					if (l > 255)
						l = 256;
					update(sequenceLengthCount, l);
					HashSet<String> usedPackages = new HashSet<>();
					for (int k = 0; k < tokens.length; k++) {
						String tToken = tokens[k], sToken = sTokens[k];
						if (sToken.equals(".getLocale()")) {
							update(gram1Map, sToken, tToken);
							if (sTokens[k-1].equals("String")) {
								update(gram2Map, sTokens[k-1] + " " + sToken, tokens[k-1] + " " + tToken);
								update(gram3Map, sTokens[k-2] + " " + sTokens[k-1] + " " + sToken, tokens[k-2] + " " + tokens[k-1] + " " + tToken);
							}
						}
						if (tToken.startsWith(lib)) {
							index = lib.length()-1;
							for (int i = 0; i < 2; i++) {
								String p = tToken.substring(0, index+1);
								usedPackages.add(p);
								if (Character.isUpperCase(tToken.charAt(index+1)))
									break;
								index = tToken.indexOf('.', index+1);
								if (index == -1)
									break;
							}
						}
					}
					for (String p : usedPackages)
						update(packageCount, p);
				}
				sc.close();
				scSource.close();
			}
			scnames.close();
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
		print(gram1Map);
		print(gram2Map);
		System.out.println("3-gram");
		print(gram3Map);
	}

	public static void print(HashMap<String, HashMap<String, Integer>> gram1Map) {
		for (String g : gram1Map.keySet()) {
			final HashMap<String, Integer> map = gram1Map.get(g);
			ArrayList<String> list = new ArrayList<>(map.keySet());
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return map.get(s2) - map.get(s1);
				}
			});
			System.out.println(g);
			for (String g2 : list)
				System.out.println(g + "\t" + g2 + "\t" + map.get(g2));
			System.out.println(map.size());
		}
	}

	public static void update(HashMap<String, HashMap<String, Integer>> gram1Map, String sToken, String tToken) {
		HashMap<String, Integer> map = gram1Map.get(sToken);
		if (map == null) {
			map = new HashMap<>();
			gram1Map.put(sToken, map);
		}
		update(map, tToken);
	}

	public static <K> void update(HashMap<K, Integer> countMap, K key) {
		int c = 1;
		if (countMap.containsKey(key))
			c += countMap.get(key);
		countMap.put(key, c);
	}

}
