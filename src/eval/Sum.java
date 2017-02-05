package eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import utils.FileUtil;

public class Sum {

	public static void main(String[] args) {
		HashMap<Integer, Integer> map = new HashMap<>();
		File dir = new File("T:/type-sequences");
		for (File sublib : dir.listFiles()) {
			for (File subp : sublib.listFiles()) {
				String content = FileUtil.getFileContent(subp.getAbsolutePath() + "/target.txt");
				Scanner sc = new Scanner(content);
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] tokens = line.split(" ");
					int l = tokens.length;
					int c = 1;
					if (map.containsKey(l))
						c += map.get(l);
					map.put(l, c);
				}
				sc.close();
			}
		}
		ArrayList<Integer> l = new ArrayList<>(map.keySet());
		Collections.sort(l);
		for (int i : l)
			System.out.println(i + "\t " + map.get(i));
	}

}
