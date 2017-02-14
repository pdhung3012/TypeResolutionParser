package data;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utils.FileUtil;

public class CountAPIs {

	private static String fop_jdk="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> arrSources=FileUtil.getFileStringArray(fop_jdk+"train.s");		
		ArrayList<String> arrResults=FileUtil.getFileStringArray(fop_jdk+"train.t");
		ArrayList<String> arrLocation=FileUtil.getFileStringArray(fop_jdk+"train.locations.txt");
		
		PrintStream ptTokens=null;
		try{
			ptTokens=new PrintStream(new FileOutputStream(fop_jdk+"vocabulary.txt"));
			
		}catch(Exception ex){
			
		}
		
		HashMap<String,Integer> setTokens=new HashMap<String,Integer>();
		HashMap<String,HashSet<String>> setTokens2=new HashMap<String,HashSet<String>>();
		for(int i=0;i<arrResults.size();i++){
			String[] arrItemsToken=arrSources.get(i).split("\\s+");
			String[] arrResultsToken=arrResults.get(i).split("\\s+");
			for(int j=0;j<arrItemsToken.length;j++){
			//	if(!arrItemsToken[j].startsWith(".")&&arrItemsToken[j].endsWith(")")){
					if(!setTokens.containsKey(arrItemsToken[j])){
						System.out.println(arrItemsToken[j]);
						ArrayList<String> lstMap=new ArrayList<String>();
						lstMap.add(arrItemsToken[j]);
						setTokens.put(arrItemsToken[j], 1);
						HashSet<String> set=new HashSet<String>();
						set.add(arrResultsToken[j]);
						setTokens2.put(arrItemsToken[j],set);
					}else{
					//	setTokens.get(arrItemsToken[j]));
						setTokens.put(arrItemsToken[j],setTokens.get(arrItemsToken[j])+1);
						setTokens2.get(arrItemsToken[j]).add(arrResultsToken[j]);

					}
				//}
				
			}
			
		}
		for(String strKey:setTokens.keySet()){
			ptTokens.print(strKey+"\t"+setTokens2.get(strKey).size()+"\n");
		}
		
		try{
			ptTokens.close();
		}catch(Exception ex){
			
		}
	}

}
