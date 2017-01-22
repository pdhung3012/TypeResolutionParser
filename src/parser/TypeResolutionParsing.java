package parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import utils.FileUtil;

public class TypeResolutionParsing {
	
	static String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\outputCodeSequence\\";
	static String fop_jdk="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\JDK_coreSource\\";
	//C:\githubWellMaintainedProject
	//static String fop_project="C:\\githubWellMaintainedProject\\";
	
	//static String fop_project="C:\\Users\\pdhung\\workspace_ee\\TestJavaProject\\";
	static String fn_source_full_success="source_full_success.txt";
	static String fn_source_success="source_success.txt";	
	static String fn_target_full_success="target_full_success.txt";
	static String fn_target_success="target_success.txt";
	static String fn_cannotResolveType="notResolved.txt";
	static String fn_fail="fail.txt";
	static String fn_noSpecFile="noSpecificationFile.txt";
	static int numFile=0;
	static int numOfTotalMethod=0;
	static int numOfSpecMethod=0;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Start");
			numFile=0;
			numOfTotalMethod=0;
			numOfSpecMethod=0;
			int beginIndex=0;
			int endIndex=50;
			if(args.length>=2){
				beginIndex=Integer.parseInt(args[0].trim());
				endIndex=Integer.parseInt(args[1].trim());
				
				fn_source_success=beginIndex+"_"+endIndex+"_"+fn_source_success;
				fn_source_full_success=beginIndex+"_"+endIndex+"_"+fn_source_full_success;
				fn_target_success=beginIndex+"_"+endIndex+"_"+fn_target_success;
				fn_target_full_success=beginIndex+"_"+endIndex+"_"+fn_target_full_success;
				fn_cannotResolveType=beginIndex+"_"+endIndex+"_"+fn_cannotResolveType;
			}
			
			FileUtil.writeToFile(fop_output+fn_fail, "");
			FileUtil.writeToFile(fop_output+fn_source_full_success, "");
			FileUtil.writeToFile(fop_output+fn_source_success, "");
			FileUtil.writeToFile(fop_output+fn_target_full_success, "");
			FileUtil.writeToFile(fop_output+fn_target_success, "");
			FileUtil.writeToFile(fop_output+fn_cannotResolveType, "");
			File fDir=new File(fop_project);
			File[] arrFileChildren=fDir.listFiles();
			for(int i=0;i<arrFileChildren.length;i++){
				if(beginIndex!=-1&&(beginIndex>i||i>endIndex)){
					continue;
				}
				
				TypeResolutionVisitor visitor=new TypeResolutionVisitor(fop_jdk,arrFileChildren[i].getAbsolutePath(),true);			
		     	 //  	visitor.parseTypeInformationOfProject(new File(fop_project));	        
					//System.out.println("Number file: "+walk(fop_jdk+"java\\",visitor));
					System.out.println(arrFileChildren[i].getName()+": "+walk(arrFileChildren[i].getAbsolutePath(),visitor));
			}
//			TypeResolutionVisitor visitor=new TypeResolutionVisitor(fop_jdk,fop_project,true);			
//     	 //  	visitor.parseTypeInformationOfProject(new File(fop_project));	        
//			//System.out.println("Number file: "+walk(fop_jdk+"java\\",visitor));
//			System.out.println("Number file: "+walk(fop_project,visitor));			
//			System.out.println("Number of spec method:" +numOfSpecMethod);
//			System.out.println("Number of all method:" +numOfTotalMethod);
			System.out.println("End");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 public static int walk( String projPath,TypeResolutionVisitor visitor) {
		 	File root = new File( projPath );
	        File[] list = root.listFiles();

	        if (list == null) return 0;
	        int numWalk=0;
	        try{
            	//numWalk+= walk( f.getAbsolutePath() ,visitor);
			   //source tokens
	        	StringBuffer buferFullSuccess=new StringBuffer();
	        	StringBuffer buferRaw=new StringBuffer();
	        	StringBuffer buferNotResolved=new StringBuffer();
	        	visitor.parseTypeInformationOfProject(root,false);
				HashMap<String,String> setMethods=visitor.getSetSequencesOfMethods();
				System.out.println( "Number methods:" + setMethods.size() );
				
//				System.out.println(++numFile+" file parsed");	
				if(setMethods.size()>0){
					for(String strItemKey:setMethods.keySet()){
						String strItemValue=setMethods.get(strItemKey);
						buferFullSuccess.append(strItemKey+"\t"+strItemValue +"\t"+"\n");
						buferRaw.append(strItemValue+"\n");
					}
				}else{
					//FileUtil.appendToFile(fop_output+fn_noSpecFile, root.getAbsolutePath().replace(fop_jdk, "")+"\n");
				}
				FileUtil.appendToFile(fop_output+fn_source_full_success,buferFullSuccess.toString());
				FileUtil.appendToFile(fop_output+fn_source_success, buferRaw.toString());
				
				
				//target tokens
				
				buferFullSuccess=new StringBuffer();
	        	buferRaw=new StringBuffer();
	        	buferNotResolved=new StringBuffer();
				
				visitor.parseTypeInformationOfProject(root,true);
				setMethods=visitor.getSetSequencesOfMethods();
				HashMap<String,String> setNotResolved=visitor.getSetOfUnResolvedType();
				
				System.out.println( "End project: " + root.getAbsoluteFile() );
//				System.out.println(++numFile+" file parsed");	
				if(setMethods.size()>0){
					for(String strItemKey:setMethods.keySet()){
						String strItemValue=setMethods.get(strItemKey);
						String strTypeNotResult=setNotResolved.get(strItemKey);
						
						buferFullSuccess.append(strItemKey+"\t"+strItemValue +"\t"+"\n");
						buferRaw.append(strItemValue+"\n");
						buferNotResolved.append(strTypeNotResult+"\n");

					}
				}else{
					//FileUtil.appendToFile(fop_output+fn_noSpecFile, root.getAbsolutePath().replace(fop_jdk, "")+"\n");
				}
				FileUtil.appendToFile(fop_output+fn_target_full_success,buferFullSuccess.toString());
				FileUtil.appendToFile(fop_output+fn_target_success, buferRaw.toString());
				FileUtil.appendToFile(fop_output+fn_cannotResolveType, buferNotResolved.toString());

				
            } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return numWalk;
	    }

}
