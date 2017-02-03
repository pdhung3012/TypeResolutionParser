package parser;

import java.util.HashSet;

import utils.FileUtil;

public class EvalOnWindowSize {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dirTranslate="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output100K\\reordered\\";
		String fn_test="test.t";
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
		//evaluate on type 1
		String[] arrSource=FileUtil.getFileContent(fop_dirTranslate+"test.s").trim().split("\n");
		String[] arrTrainSource=FileUtil.getFileContent(fop_dirTranslate+"train.s").trim().split("\n");
		String[] arrTrainTarget=FileUtil.getFileContent(fop_dirTranslate+"train.t").trim().split("\n");
		String[] arrOracles=FileUtil.getFileContent(fop_dirTranslate+fn_test).trim().split("\n");
		String[] arrTranslations=FileUtil.getFileContent(fop_dirTranslate+fn_translatedResult).trim().split("\n");
		String[] arrPosInLine=FileUtil.getFileContent(fop_dirTranslate+fn_order).trim().split("\n");
		
		String[] arrEvaluatedTypes=FileUtil.getFileContent(fop_dirTranslate+"evaluatedResults.txt").trim().split("\n");
		
				
		int countCorrectCSInSentence=0,countCorrectCLInSentence=0;
		int countOfInCorrectPerSentence=0,countOfGoodPerSentence=0,countOfOutOfVocabPerSentence=0,countOfOutSourcePerSentence=0,countOfOutTargetPerSentence=0;
		
		HashSet<String> setTrainTarget=new HashSet<String>(); 
		HashSet<String> setTrainSource=new HashSet<String>(); 
		
		for(int i=0;i<arrTrainTarget.length;i++){
			String[] arrItemTTarget=arrTrainTarget[i].trim().split("\\s+");
			String[] arrItemTSource=arrTrainSource[i].trim().split("\\s+");
			
			for(int j=0;j<arrItemTSource.length;j++){
				if(!setTrainSource.contains(arrItemTSource[j])){
					setTrainSource.add(arrItemTSource[j]);
				}
			}
			
			for(int j=0;j<arrItemTTarget.length;j++){
				if(!setTrainTarget.contains(arrItemTTarget[j])){
					setTrainTarget.add(arrItemTTarget[j]);
				}
			}
			
		}
		
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
			
			int numOfInCorrectPerSentence=0,numOfGoodPerSentence=0,numOfOutOfVocabPerSentence=0,numOfOutOfSourcePerSentence=0,numOfOutOfTargetPerSentence=0;
			
			int indexTypeTranslation=0;
			String strIncorrect="",strOutVocab="";
			int posEval=Integer.parseInt(arrPosInLine[i].split("\t")[1]);
			
			
			
			int j=posEval-1;
			if(arrItemOracle[j].trim().contains(".") ){
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
			

			FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, isCLInSentence+"\t"+isCSInSentence+"\t"+arrItemOracle.length+"\t"+arrItemTrans.length+"\n");
			FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, numOfGoodPerSentence+"\t"+numOfInCorrectPerSentence+"\t"+numOfOutOfVocabPerSentence+"\n");
			
			//if(!isCSInSentence){
				countOfGoodPerSentence+=numOfGoodPerSentence;
				countOfInCorrectPerSentence+=numOfInCorrectPerSentence;
				countOfOutOfVocabPerSentence+=numOfOutOfVocabPerSentence;
				countOfOutSourcePerSentence+=numOfOutOfSourcePerSentence;
				countOfOutTargetPerSentence+=numOfOutOfTargetPerSentence;
				
				
			//	FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_source.txt",arrSource[i]+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_translated.txt",arrTranslations[i]+"\n");
				
				FileUtil.appendToFile(fop_dirTranslate+fn_OutOfVocab,strOutVocab+"\n");
				FileUtil.appendToFile(fop_dirTranslate+fn_IncorrectTranslate, strIncorrect+"\n");
			//}
			
			
			
			
		}
		FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, countCorrectCLInSentence+"/"+arrOracles.length+"\t"+countCorrectCSInSentence+"/"+arrOracles.length+"\n");
		FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, countOfGoodPerSentence+"\t"+countOfInCorrectPerSentence+"\t"+countOfOutSourcePerSentence+"\t"+countOfOutTargetPerSentence+"\t"+countOfOutOfVocabPerSentence+"\n");

	}
		
}
