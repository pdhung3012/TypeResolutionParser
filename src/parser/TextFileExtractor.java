package parser;

import java.io.*;
import java.util.HashSet;

import utils.FileUtil;

public class TextFileExtractor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dir="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\outputCodeSequence\\";
		String fop_output="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\";
		File dir=new File(fop_dir);
		File[] arrFiles=dir.listFiles();
		FileUtil.writeToFile(fop_output+"full_source.txt", "");
		FileUtil.writeToFile(fop_output+"full_target.txt", "");
		String[] arrSource,arrTarget;
		int totalRow=0;
		for(int i=0;i<arrFiles.length;i++){
			if(arrFiles[i].getAbsolutePath().endsWith("target_success.txt")){
				String fp_source=arrFiles[i].getAbsolutePath().replace("target", "source");
				//String strSource=FileUtil.getFileContent(fp_source).trim();
				//String strTarget=FileUtil.getFileContent(arrFiles[i].getAbsolutePath()).trim();
			//	arrSource=FileUtil.getFileContent(fp_source).trim().split("\n");
				//arrTarget=FileUtil.getFileContent(arrFiles[i].getAbsolutePath()).trim().split("\n");
				
				StringBuilder bufferSource=new StringBuilder();
				StringBuilder bufferTarget=new StringBuilder();
				int countBuffer=0;			
				int indexChoose=0;
				HashSet<Integer> setIndexRemove=new HashSet<Integer>();
				try (BufferedReader br = new BufferedReader(new FileReader(arrFiles[i].getAbsolutePath()))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				       // process the line
				    	indexChoose++;
						if(line.split("\\s").length>2&&line.contains(".")){
							bufferTarget.append(line+"\n");
					    	countBuffer++;
					    	if(countBuffer==10000){
							//	FileUtil.appendToFile(fop_output+"full_source.txt", bufferSource.toString());
								FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
							//	bufferSource=new StringBuilder();
								bufferTarget=new StringBuilder();
								
								countBuffer=0;
							}
						}else{
							setIndexRemove.add(indexChoose);
						}
				    	
				    }
				    
				    if(bufferTarget.length()>0){
					//	FileUtil.appendToFile(fop_output+"full_source.txt", bufferSource.toString());
						FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
						bufferTarget=new StringBuilder();
					//	bufferTarget=new StringBuilder();
						
						countBuffer=0;
					}
				    br.close();
				}catch(Exception ex){
					
				}
				
				countBuffer=0;
				indexChoose=0;
				try (BufferedReader br = new BufferedReader(new FileReader(fp_source))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				       // process the line.
				    	indexChoose++;
				    	if(!setIndexRemove.contains(indexChoose)){
				    		bufferSource.append(line+"\n");
					    	countBuffer++;
					    	if(countBuffer==10000){
								FileUtil.appendToFile(fop_output+"full_source.txt", bufferSource.toString());
						//		FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
					//			bufferSource=new StringBuilder();
								bufferSource=new StringBuilder();
								
								countBuffer=0;
							}
				    	}
				    	
				    }
				    
				    if(bufferSource.length()>0){
						FileUtil.appendToFile(fop_output+"full_source.txt", bufferSource.toString());
					//	FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
						bufferSource=new StringBuilder();
						bufferTarget=new StringBuilder();
						
						countBuffer=0;
					}
				    br.close();
				}catch(Exception ex){
					
				}
				
			//	System.out.println(arrSource.length);
				totalRow+=indexChoose;
//				
//				FileUtil.appendToFile(fop_output+"full_source.txt", strSource+"\n");
//				FileUtil.appendToFile(fop_output+"full_target.txt", strTarget+"\n");

//				for(int j=0;j<arrSource.length;j++){
//					String strCheck=arrTarget[i].trim();
//					if(strCheck.split("\\s").length>2&&strCheck.contains(".")){
////						FileUtil.appendToFile(fop_output+"full_source.txt", arrSource[i].trim()+"\n");
////						FileUtil.appendToFile(fop_output+"full_target.txt", strCheck.trim()+"\n");
////						
//						bufferTarget.append(arrTarget[j].trim()+"\n");
//						bufferSource.append(arrSource[j].trim()+"\n");
//						countBuffer++;
//						
//					}
//					if(countBuffer==10000||j==arrSource.length-1){
//						FileUtil.appendToFile(fop_output+"full_source.txt", bufferSource.toString());
//						FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
//						bufferSource=new StringBuilder();
//						bufferTarget=new StringBuilder();
//						
//						countBuffer=0;
//					}
//					
//				}
				
				System.out.println("total row chose: "+totalRow);
			//	break;
			}
			
		}
	}

}
