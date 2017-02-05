package parser;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import utils.FileUtil;

public class EvalOnWindowSize {

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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dirTranslate="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_stackoverflow\\windows_translation\\";
		String fn_test="test_reorder.t";
		String fn_order="test_order_line.txt";
		//check length
		String fn_translatedResult="test.tune.baseline.trans";
		String fn_evaluatedResult="evaluatedResults.txt";
		String fn_typeStructureResults="typeStructureResults.txt";
		String fn_OutOfVocab="typeOutVocab.txt";
		String fn_IncorrectTranslate="typeIncorrectTranslate.txt";
		FileUtil.writeToFile(fop_dirTranslate+fn_evaluatedResult, "");
		FileUtil.writeToFile(fop_dirTranslate+fn_typeStructureResults, "");
		FileUtil.writeToFile(fop_dirTranslate+fn_OutOfVocab, "");
		FileUtil.writeToFile(fop_dirTranslate+fn_IncorrectTranslate, "");
		
		PrintStream ptEvalResult=null,ptTypeStructure=null,ptOutOfVocab=null,ptIncorrectTranslate=null;
		
		try{
			ptEvalResult=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_evaluatedResult));
			ptTypeStructure=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_typeStructureResults));
			ptOutOfVocab=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_OutOfVocab));
			ptIncorrectTranslate=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_IncorrectTranslate));
		}catch(Exception ex){
			
		}
		
		//evaluate on type 1
		String[] arrSource=FileUtil.getFileContent(fop_dirTranslate+"test_reorder.s").trim().split("\n");
		String[] arrOracles=FileUtil.getFileContent(fop_dirTranslate+fn_test).trim().split("\n");
		String[] arrTranslations=FileUtil.getFileContent(fop_dirTranslate+fn_translatedResult).trim().split("\n");
		String[] arrPosInLine=FileUtil.getFileContent(fop_dirTranslate+fn_order).trim().split("\n");
		
		HashSet<String> set5Libraries=new HashSet<String>();
		set5Libraries.add("android.");
		set5Libraries.add("com.google.gwt.");
		set5Libraries.add("com.thoughtworks.xstream.");
		set5Libraries.add("org.hibernate.");
		set5Libraries.add("org.joda.time.");
		set5Libraries.add("java.");
		set5Libraries.add("org.apache.");
				
		int countCorrectCSInSentence=0,countCorrectCLInSentence=0;
		int countOfInCorrectPerSentence=0,countOfGoodPerSentence=0,countOfOutOfVocabPerSentence=0,countOfOutSourcePerSentence=0,countOfOutTargetPerSentence=0;
		
		HashSet<String> setTrainTarget=new HashSet<String>(); 
		HashSet<String> setTrainSource=new HashSet<String>(); 
		
		ArrayList<String> arrTrainSource=FileUtil.getFileStringArray(fop_dirTranslate+"train.s");
		
		for(int i=0;i<arrTrainSource.size();i++){
			String[] arrItemTSource=arrTrainSource.get(i).trim().split("\\s+");
			
			for(int j=0;j<arrItemTSource.length;j++){
				if(!setTrainSource.contains(arrItemTSource[j])){
					setTrainSource.add(arrItemTSource[j]);
				}
			}
			
		
			
		}
		
		arrTrainSource.clear();
		ArrayList<String> arrTrainTarget=FileUtil.getFileStringArray(fop_dirTranslate+"train.t");
		
		for(int i=0;i<arrTrainTarget.size();i++){
			String[] arrItemTTarget=arrTrainTarget.get(i).trim().split("\\s+");
		
			
			for(int j=0;j<arrItemTTarget.length;j++){
				if(!setTrainTarget.contains(arrItemTTarget[j])){
					setTrainTarget.add(arrItemTTarget[j]);
				}
			}
			
		}
		arrTrainTarget.clear();
		
		//compare length and structure
		for(int i=0;i<arrOracles.length;i++){
			String[] arrItemTrans=arrTranslations[i].trim().split("\\s+");
			String[] arrItemOracle=arrOracles[i].trim().split("\\s+");
			String[] arrItemSource=arrSource[i].trim().split("\\s+");
			boolean isCSInSentence=true,isCLInSentence=true;
			if(arrItemOracle.length!=arrItemTrans.length){
				isCLInSentence=false;
			}else {
				isCLInSentence=true;
				countCorrectCLInSentence++;
			}
			
			 
			for(int j=0;j<arrItemOracle.length;j++){
				
				if(arrItemOracle[j].trim().contains(".")){
				
				}
				else if(arrItemOracle[j].endsWith("()")){
					if(j<arrItemTrans.length&&arrItemTrans[j].endsWith("()")){
						//isCSInSentence=true;
						
					} else{
					//	System.out.println(arrItemOracle[j]+" -- "+arrItemTrans[j]);
						isCSInSentence=false;
					}
				}
				else {
					if(j<arrItemTrans.length&&arrItemOracle[j].equals(arrItemTrans[j])){
						//isCSInSentence=true;
						
						
					} else{
					//	System.out.println(arrItemOracle[j]+" -- "+arrItemTrans[j]);
						isCSInSentence=false;
					}
				}
				
			}
			if(isCSInSentence){
				countCorrectCSInSentence++;
			}
			
			
			int numOfInCorrectPerSentence=0,numOfGoodPerSentence=0,numOfOutOfVocabPerSentence=0,numOfOutOfSourcePerSentence=0,numOfOutOfTargetPerSentence=0;
			
			int indexTypeTranslation=0;
			String strIncorrect="",strOutVocab="";
			int posEval=Integer.parseInt(arrPosInLine[i].split("\t")[1]);
			
			
			
			int j=posEval-1;
			//if( arrItemOracle[j].trim().contains(".") ){
			if( checkAPIsInLibrary(set5Libraries,arrItemOracle[j])){
				//if((!arrItemOracle[j].equals(arrItemTrans[j]))&&arrItemOracle[j].endsWith(arrItemTrans[j])){
				if(!setTrainSource.contains(arrItemSource[j])){	
					strOutVocab+=arrItemTrans[j]+" ( "+arrItemOracle[j]+" ) ";
					numOfOutOfSourcePerSentence++;
					numOfOutOfVocabPerSentence++;
					indexTypeTranslation++;
					//break;
				} 
				else if(!setTrainTarget.contains(arrItemOracle[j])){
					strOutVocab+=arrItemTrans[j]+" ( "+arrItemOracle[j]+" ) ";
					numOfOutOfTargetPerSentence++;
					numOfOutOfVocabPerSentence++;
					indexTypeTranslation++;
				
				}
				else if(arrItemOracle[j].equals(arrItemTrans[j])){
				
					numOfGoodPerSentence++;
				} else {
					strIncorrect+=arrItemTrans[j]+" ( "+arrItemOracle[j]+" ) ";
					numOfInCorrectPerSentence++;
				}
					
			}
			

//			FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, isCLInSentence+"\t"+isCSInSentence+"\t"+arrItemOracle.length+"\t"+arrItemTrans.length+"\n");
//			FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, numOfGoodPerSentence+"\t"+numOfInCorrectPerSentence+"\t"+numOfOutOfVocabPerSentence+"\n");
			ptEvalResult.print("Line "+arrPosInLine[i].split("\t")[2]+" token "+arrPosInLine[i].split("\t")[0]+" (correct length/ correct structure/ lengthOracle/lengthTranslated): "+isCLInSentence+"\t"+isCSInSentence+"\t"+arrItemOracle.length+"\t"+arrItemTrans.length+"\n");
			ptTypeStructure.print("Line "+arrPosInLine[i].split("\t")[2]+" token "+arrPosInLine[i].split("\t")[0]+" (correct/incorrect/OOV): "+numOfGoodPerSentence+"\t"+numOfInCorrectPerSentence+"\t"+numOfOutOfVocabPerSentence+"\n");
			
			//if(!isCSInSentence){
				countOfGoodPerSentence+=numOfGoodPerSentence;
				countOfInCorrectPerSentence+=numOfInCorrectPerSentence;
				countOfOutOfVocabPerSentence+=numOfOutOfVocabPerSentence;
				countOfOutSourcePerSentence+=numOfOutOfSourcePerSentence;
				countOfOutTargetPerSentence+=numOfOutOfTargetPerSentence;
				
				
			//	FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_source.txt",arrSource[i]+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_translated.txt",arrTranslations[i]+"\n");
				
				ptOutOfVocab.print("Line "+arrPosInLine[i].split("\t")[2]+" token "+arrPosInLine[i].split("\t")[0]+" (correct/incorrect/OOS/OOT/OOV): "+strOutVocab+"\n");
				ptIncorrectTranslate.print("Line "+arrPosInLine[i].split("\t")[2]+" token "+arrPosInLine[i].split("\t")[0]+" (correct/incorrect/OOS/OOT/OOV): "+strIncorrect+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+fn_OutOfVocab,strOutVocab+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+fn_IncorrectTranslate, strIncorrect+"\n");
			//}
			
			
			
			
		}
		
		ptEvalResult.print(countCorrectCLInSentence+"/"+arrOracles.length+"\t"+countCorrectCSInSentence+"/"+arrOracles.length+"\n");
		ptTypeStructure.print(countOfGoodPerSentence+"\t"+countOfInCorrectPerSentence+"\t"+countOfOutSourcePerSentence+"\t"+countOfOutTargetPerSentence+"\t"+countOfOutOfVocabPerSentence+"\n");
		
		try{
			ptEvalResult.close();
			ptTypeStructure.close();
			ptOutOfVocab.close();
			ptIncorrectTranslate.close();
		}catch(Exception ex){
			
		}
		
//		FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, countCorrectCLInSentence+"/"+arrOracles.length+"\t"+countCorrectCSInSentence+"/"+arrOracles.length+"\n");
//		FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, countOfGoodPerSentence+"\t"+countOfInCorrectPerSentence+"\t"+countOfOutSourcePerSentence+"\t"+countOfOutTargetPerSentence+"\t"+countOfOutOfVocabPerSentence+"\n");

	}
		
}
