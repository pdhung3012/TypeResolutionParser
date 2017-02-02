package parser;

import utils.FileUtil;

public class EvalOnWindowSize {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dirTranslate="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\outputUpdate\\";
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
		//evaluate on type 1
		String[] arrSource=FileUtil.getFileContent(fop_dirTranslate+"test_reorder.s").trim().split("\n");
		
		String[] arrOracles=FileUtil.getFileContent(fop_dirTranslate+fn_test).trim().split("\n");
		String[] arrTranslations=FileUtil.getFileContent(fop_dirTranslate+fn_translatedResult).trim().split("\n");
		String[] arrPosInLine=FileUtil.getFileContent(fop_dirTranslate+fn_order).trim().split("\n");
		
		int countCorrectCSInSentence=0,countCorrectCLInSentence=0;
		int countOfInCorrectPerSentence=0,countOfGoodPerSentence=0,countOfOutOfVocabPerSentence=0;
		
		//compare length and structure
		for(int i=0;i<arrOracles.length;i++){
			String[] arrItemTrans=arrTranslations[i].trim().split("\\s+");
			String[] arrItemOracle=arrOracles[i].trim().split("\\s+");
			boolean isCSInSentence=true,isCLInSentence=true;
			if(arrItemOracle.length!=arrItemTrans.length){
				isCLInSentence=false;
			}else {
				isCLInSentence=true;
				countCorrectCLInSentence++;
			}
			
			int numOfInCorrectPerSentence=0,numOfGoodPerSentence=0,numOfOutOfVocabPerSentence=0;
			
			int indexTypeTranslation=0;
			String strIncorrect="",strOutVocab="";
			int posEval=Integer.parseInt(arrPosInLine[i].split("\t")[1]);
			
			
			
			int j=posEval-1;
			if(arrItemOracle[j].trim().contains(".") ){
				if((!arrItemOracle[j].equals(arrItemTrans[j]))&&arrItemOracle[j].endsWith(arrItemTrans[j])){
					
					strOutVocab+=arrItemTrans[j]+" ( "+arrItemOracle[j]+" ) ";
					numOfOutOfVocabPerSentence++;
					indexTypeTranslation++;
					//break;
				} else if(arrItemOracle[j].equals(arrItemTrans[j])){
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
				
			//	FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_source.txt",arrSource[i]+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_translated.txt",arrTranslations[i]+"\n");
				
				FileUtil.appendToFile(fop_dirTranslate+fn_OutOfVocab,strOutVocab+"\n");
				FileUtil.appendToFile(fop_dirTranslate+fn_IncorrectTranslate, strIncorrect+"\n");
			//}
			
			
			
			
		}
		FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, countCorrectCLInSentence+"/"+arrOracles.length+"\t"+countCorrectCSInSentence+"/"+arrOracles.length+"\n");
		FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, countOfGoodPerSentence+"\t"+countOfInCorrectPerSentence+"\t"+countOfOutOfVocabPerSentence+"\n");

	}
		
}
