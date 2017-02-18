package data;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import utils.FileUtil;

public class CountAPIs {

	private static String fop_jdk="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libs_newApp\\fold-1\\";
	
	public static boolean checkAPIsInLibrary(HashSet<String> setLib,String token){
		boolean check=false;
		for(String str:setLib){
			if(token.startsWith(str)){
				//System.out.println(token);
				check=true;
				break;
			}
		}
		return check;
	}
	
	public static String getPackageAPIsInLibrary(HashSet<String> setLib,String token){
		String result="";
		for(String str:setLib){
			if(token.startsWith(str)){
				//System.out.println(token);
				result=str;
				break;
			}
		}
		return result;
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> arrSources=FileUtil.getFileStringArray(fop_jdk+"train.s");		
		ArrayList<String> arrResults=FileUtil.getFileStringArray(fop_jdk+"train.t");
		ArrayList<String> arrLocation=FileUtil.getFileStringArray(fop_jdk+"train.locations.txt");
		
		HashMap<String,HashMap<String,HashSet<String>>> setPackages=new HashMap<String,HashMap<String,HashSet<String>>>();
		
		setPackages.put("android.",new HashMap<String,HashSet<String>>());
		setPackages.put("com.google.gwt.",new HashMap<String,HashSet<String>>());
		setPackages.put("com.thoughtworks.xstream.",new HashMap<String,HashSet<String>>());
		setPackages.put("org.hibernate.",new HashMap<String,HashSet<String>>());
		setPackages.put("org.joda.time.",new HashMap<String,HashSet<String>>());
		setPackages.put("java.",new HashMap<String,HashSet<String>>());
		
		HashSet<String> set5Libraries=new HashSet<String>();
		set5Libraries.add("android.");
		set5Libraries.add("com.google.gwt.");
		set5Libraries.add("com.thoughtworks.xstream.");
		set5Libraries.add("org.hibernate.");
		set5Libraries.add("org.joda.time.");		
		set5Libraries.add("java.");
		
		
		PrintStream ptTokens=null,ptLibraries[];
		try{
			ptTokens=new PrintStream(new FileOutputStream(fop_jdk+"vocabulary.txt"));
			
		}catch(Exception ex){
			
		}
		
		HashMap<String,Integer> setTokens=new HashMap<String,Integer>();
		HashMap<String,HashSet<String>> setTokens2=new HashMap<String,HashSet<String>>();
		for(int i=0;i<arrResults.size();i++){
			String[] arrItemsToken=arrSources.get(i).split("\\s+");
			String[] arrResultsToken=arrResults.get(i).split("\\s+");
			
//			if(i==20325){
//				System.out.println(arrResults.get(i));
//				Scanner sc=new Scanner(System.in);
//				sc.next();
//			}
			
			for(int j=0;j<arrItemsToken.length;j++){
			//	if(checkAPIsInLibrary(set5Libraries, arrResultsToken[j])){
					if(arrItemsToken[j].equals("OnPreDrawListener()")){
						System.out.println("line "+i);
						
						System.out.println(arrResultsToken[j]+": "+arrSources.get(i));
						System.out.println(arrResultsToken[j]+": "+arrResults.get(i));
						Scanner sc=new Scanner(System.in);
						sc.next();
					}
					
//					if(arrResultsToken[j].equals("android.view.ViewTreeObserver.OnPreDrawListener()")){
//						System.out.println("line target "+i);
//						System.out.println(arrResultsToken[j]+": "+arrLocation.get(i));						
//						System.out.println(arrResultsToken[j]+": "+arrSources.get(i));
//						System.out.println(arrResultsToken[j]+": "+arrResults.get(i));
//						Scanner sc=new Scanner(System.in);
//						sc.next();
//					}
					
					//String packageName=getPackageAPIsInLibrary(set5Libraries, arrResultsToken[j]);
					//HashMap<String,HashSet<String>> mapResult=setPackages.get(packageName);
					if(!setTokens2.containsKey(arrItemsToken[j])){
					//	System.out.println(arrItemsToken[j]);
						
						ArrayList<String> lstMap=new ArrayList<String>();
						lstMap.add(arrItemsToken[j]);
						setTokens.put(arrItemsToken[j], 1);
						HashSet<String> set=new HashSet<String>();
						set.add(arrResultsToken[j]);
						setTokens2.put(arrItemsToken[j],set);
						
//						HashSet<String> setResults=new HashSet<String>();
//						setResults.add(arrResultsToken[j]);
//						mapResult.put(arrItemsToken[j], setResults);
						
					}else{
					//	setTokens.get(arrItemsToken[j]));
						
						setTokens.put(arrItemsToken[j],setTokens.get(arrItemsToken[j])+1);
						HashSet<String> setResults=setTokens2.get(arrItemsToken[j]);
						setResults.add(arrResultsToken[j]);
						setTokens2.put(arrItemsToken[j], setResults);
						//mapResult.get(arrItemsToken[j]).add(arrResultsToken[j]);
					}
				}
				
		//	}
			
		}
		
		
		for(String strKey:setTokens.keySet()){
			ptTokens.print(strKey+"\t"+setTokens2.get(strKey).size()+"\t"+setTokens2.get(strKey).toString()+"\n");
		}
		
		
		
		for(String strPackageName:setPackages.keySet()){
			
			PrintStream ptLibrary=null;
			try{
				ptLibrary=new PrintStream(new FileOutputStream(fop_jdk+"vocabulary_"+strPackageName+"txt"));
				
			}catch(Exception ex){
				
			}
			HashMap<String,HashSet<String>> mapResult=setPackages.get(strPackageName);
			for(String strKey:mapResult.keySet()){
				HashSet<String> setResultTokens=mapResult.get(strKey);
				ptLibrary.print(strKey+"\t"+setResultTokens.toString()+"\t"+setResultTokens.size()+"\n");
			}
			
			try{
				ptLibrary.close();				
			}catch(Exception ex){
				
			}
			
			//ptTokens.print(strKey+"\t"+setTokens2.get(strKey).size()+"\t"+setTokens2.get(strKey).toString()+"\n");
		}
		
		try{
			ptTokens.close();
		}catch(Exception ex){
			
		}
	}

}
