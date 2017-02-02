package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.internal.ExactComparisonCriteria;

import utils.FileUtil;

public class ExtractingCodeSnippet {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\";
		String fop_project="C:\\githubWellMaintainedProject\\apache_abdera\\";
		String fn_testLocation="test_location.txt";
		String fop_full_source=fop_input+"full_source\\";
		String fop_snippet_source=fop_input+"snippet_source\\";
		File fDir1=new File(fop_full_source);
		File fDir2=new File(fop_snippet_source);
		if(!fDir1.isDirectory()){
			fDir1.mkdir();
		}
		if(!fDir2.isDirectory()){
			fDir2.mkdir();
		}
		ArrayList<String> lstMethods=new ArrayList<String>(); 
		ArrayList<String> lstFiles=new ArrayList<String>(); 
		HashMap<String,String> mapCotent=new HashMap<String,String>();
		String[] arrTestLocation=FileUtil.getFileContent(fop_input+fn_testLocation).trim().split("\n");
		mapCotent=new HashMap<String, String>();
		
		for(int i=0;i<arrTestLocation.length;i++){
			String[] arrItems=arrTestLocation[i].split("\\s+");
			String fp_file=arrItems[0];
			String strMethod="";
			for(int j=2;j<arrItems.length-3;j++){
				strMethod+=arrItems[j]+"\t";
			}
			String methodSig=strMethod.trim();
			
			FileUtil.writeToFile(fop_full_source+(i+1)+".java","//method: "+methodSig+"\n"+FileUtil.getFileContent(fp_file));
			lstMethods.add(methodSig);
			lstFiles.add(fp_file);
			mapCotent.put(fp_file+"\t"+methodSig, "");
			System.out.println(fp_file+"\t"+methodSig);
			
			
			
		}
		
		ExtractCodeSnippetVisitor visitor=new ExtractCodeSnippetVisitor();
		visitor.setMapMethods(mapCotent);
		
		visitor.parseTypeInformationOfProject(new File(fop_project), fop_snippet_source);
		
		System.out.println("Prepare source code");
		for(int i=0;i<arrTestLocation.length;i++){
			String strWrite=visitor.getMapMethods().get(lstFiles.get(i)+"\t"+lstMethods.get(i)).toString();
			System.out.println(strWrite);
			FileUtil.writeToFile(fop_snippet_source+(i+1)+".java","//location: "+lstFiles.get(i)+"\n\\Method name: "+lstMethods.get(i)+"\n"+strWrite);
			
		}
		System.out.println("End");
		
		
		
	}

}
