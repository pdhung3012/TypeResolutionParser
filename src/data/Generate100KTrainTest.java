package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import utils.FileUtil;



public class Generate100KTrainTest {
	
	public static void evaluateLibraryPerMethod(HashMap<String,Integer> mapMethod,String targetToken){
		String[] arrTargetTokens=targetToken.split("\\s+");
		for(int i=0;i<arrTargetTokens.length;i++){
			
			for(String itemKey:mapMethod.keySet()){
				if(arrTargetTokens[i].contains(itemKey)){
					mapMethod.put(itemKey, mapMethod.get(itemKey)+1);
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_logProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\log_extractData\\";
		String fop_logProgram="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\log_program\\";
		String fop_sequenceInput="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libSequences\\";
		String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output100K\\";
		HashMap<String,Integer> map5LibrariesCount=new HashMap<String,Integer>();
		map5LibrariesCount.put("org.joda.time", 0);
		map5LibrariesCount.put("android", 0);
		map5LibrariesCount.put("gwt",0);
		map5LibrariesCount.put("xstream", 0);
		map5LibrariesCount.put("hibernate", 0);
		
		File dirRepo=new File(fop_logProject);
		File[] arrLstFiles=dirRepo.listFiles();
		int indexLstFile=0;
//		FileUtil.writeToFile(fop_output+"all.source.txt", "");
//		FileUtil.writeToFile(fop_output+"all.target.txt", "");
//		FileUtil.writeToFile(fop_output+"all.location.txt", "");
//		FileUtil.writeToFile(fop_output+"all.training.s-t.A3", "");
//		FileUtil.writeToFile(fop_output+"all.training.t-s.A3", "");
		
		PrintStream stSource=null,stTarget=null,stLocation=null,stTrainSt=null,stTrainTs=null;
		
		try {
			stSource = new PrintStream(new FileOutputStream(fop_output+"all.source.txt"));
			stTarget = new PrintStream(new FileOutputStream(fop_output+"all.target.txt"));
			stLocation = new PrintStream(new FileOutputStream(fop_output+"all.location.txt"));
			stTrainSt = new PrintStream(new FileOutputStream(fop_output+"all.training.s-t.A3"));
			stTrainTs = new PrintStream(new FileOutputStream(fop_output+"all.training.t-s.A3"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		HashSet<String> setMethodInfo=new HashSet<String>();

		for(int i=0;i<arrLstFiles.length;i++){
			int numberMethodsRequired=80000;
			int indexMethodRequired=0;
			int numberOfMethods=0;
			//int numberL1PerProject=0,numberL2PerProject=0,numberL3PerProject=0,numberL4PerProject=0,numberL5PerProject=0;
			
			
			String[] arrProjects=FileUtil.getFileContent(arrLstFiles[i].getAbsolutePath()).trim().split("\n");
			String fn_libName=arrLstFiles[i].getName().split("\\.\\.csv")[0].replace("repos-5stars-50commits-lib-", "").replace("..csv", "");
			String[] arrLibName=fn_libName.split("\\.");
			fn_libName=arrLibName[arrLibName.length-1];
			for(int j=0;j<arrProjects.length;j++){
				
				System.out.println(arrProjects[j]);
				String projName=arrProjects[j].split("\t")[0].replace("/", "_");
//				if(existProjects.contains(projName)){
//					continue;
//				} else{
//					existProjects.add(projName);
//					
//				}
				numberOfMethods=0;
				String fp_locations=fop_sequenceInput+"\\"+projName+"\\locations.txt";
				String fp_source=fop_sequenceInput+"\\"+projName+"\\source.txt";
				String fp_target=fop_sequenceInput+"\\"+projName+"\\target.txt";
				String fp_alignmentST=fop_sequenceInput+"\\"+projName+"\\-alignment\\training.s-t.A3";
				String fp_alignmentTS=fop_sequenceInput+"\\"+projName+"\\-alignment\\training.t-s.A3";
				
				String[] arrLocation=utils.FileUtil.getFileContent(fp_locations).split("\n");
				String[] arrSource=utils.FileUtil.getFileContent(fp_source).split("\n");
				String[] arrTarget=utils.FileUtil.getFileContent(fp_target).split("\n");
				String[] arrAlignST=utils.FileUtil.getFileContent(fp_alignmentST).split("\n");
				String[] arrAlignTS=utils.FileUtil.getFileContent(fp_alignmentTS).split("\n");
				
				HashMap<String,Integer> map5LibrariesPerProject=new HashMap<String,Integer>();
				map5LibrariesPerProject.put("org.joda.time", 0);
				map5LibrariesPerProject.put("android", 0);
				map5LibrariesPerProject.put("gwt",0);
				map5LibrariesPerProject.put("xstream", 0);
				map5LibrariesPerProject.put("hibernate", 0);
				
				for(int k=0;k<arrTarget.length;k++){
					String[] arrLocationInfo=arrLocation[k].split("\t");
					String percentResolve=arrLocationInfo[arrLocationInfo.length-1];
					String[] arrTokenTarget=arrTarget[k].trim().split("\\s+");
				//	System.out.println(percentResolve);
					boolean checkStartWithTokens=false;
					for(int q=0;q<arrTokenTarget.length;q++){
						if(arrTokenTarget[q].contains(fn_libName)){
							checkStartWithTokens=true;
							break;
						}
					}
					if(percentResolve.equals("100%")&&arrTokenTarget.length>=3&&arrTokenTarget.length<=50&&checkStartWithTokens&&!setMethodInfo.contains(arrLocation[k])){
						//add to corpus
						
						//if(k*3<arrAlignST.length){
							setMethodInfo.add(arrLocation[k]);
							numberOfMethods++;
							stSource.print(arrSource[k]+"\n");
							stTarget.print(arrTarget[k]+"\n");
							stLocation.print( arrLocation[k]+"\n");
							stTrainSt.print(  arrAlignST[k*3]+"\n");
							stTrainSt.print( arrAlignST[k*3+1]+"\n");
							stTrainSt.print( arrAlignST[k*3+2]+"\n");						
							stTrainTs.print(  arrAlignTS[k*3]+"\n");
							stTrainTs.print( arrAlignTS[k*3+1]+"\n");
							stTrainTs.print( arrAlignTS[k*3+2]+"\n");
							indexMethodRequired++;
							
						//}
						
					}
				}
				FileUtil.appendToFile(fop_logProgram+"log_projects.txt",  fn_libName+"\t"+projName+"\t"+numberOfMethods+"\n");
				if(indexMethodRequired>numberMethodsRequired){
					break;
				}
			
			}
			
		}
		
	}

}
