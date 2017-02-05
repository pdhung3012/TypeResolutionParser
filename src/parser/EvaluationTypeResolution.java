package parser;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;

import utils.FileUtil;

public class EvaluationTypeResolution {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dirTranslate="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_stackoverflow\\";
		String fn_test="test.t";
		
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
		
		String[] arrOracles=FileUtil.getFileContent(fop_dirTranslate+fn_test).trim().split("\n");
		String[] arrTranslations=FileUtil.getFileContent(fop_dirTranslate+fn_translatedResult).trim().split("\n");
	
		PrintStream ptResult=null,ptTypeStructure=null;
		try{
			ptResult=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_evaluatedResult));
			ptTypeStructure=new PrintStream(new FileOutputStream(fop_dirTranslate+fn_typeStructureResults));
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		int countCorrectCSInSentence=0,countCorrectCLInSentence=0;
		int countOfInCorrectPerSentence=0,countOfGoodPerSentence=0,countOfOutOfVocabPerSentence=0;
		
		//compare length and structure
		for(int i=0;i<arrOracles.length;i++){
//			setTranslations=new HashSet<String>();
//			setOracles=new HashSet<String>();
			String[] arrItemTrans=arrTranslations[i].trim().split("\\s+");
			String[] arrItemOracle=arrOracles[i].trim().split("\\s+");
			boolean isCSInSentence=true,isCLInSentence=true;
//			System.out.println(arrTranslations[i]);
//			System.out.println(arrOracles[i]);
//			System.out.println();
			if(arrItemOracle.length!=arrItemTrans.length){
				isCLInSentence=false;
			}else {
				isCLInSentence=true;
				countCorrectCLInSentence++;
			}
			
			int numOfInCorrectPerSentence=0,numOfGoodPerSentence=0,numOfOutOfVocabPerSentence=0;
			
			int indexTypeTranslation=0;
			String strIncorrect="",strOutVocab="";
			
			for(int j=0;j<arrItemOracle.length;j++){
				
				if(arrItemOracle[j].trim().contains(".")||arrItemOracle[j].trim().equals("Node#type")||arrItemOracle[j].trim().equals("Node") ){
				
				} else{
					if(j<arrItemTrans.length&&arrItemOracle[j].equals(arrItemTrans[j])){
						//isCSInSentence=true;
						
						
					} else{
					//	System.out.println(arrItemOracle[j]+" -- "+arrItemTrans[j]);
						isCSInSentence=false;
					}
				}
				
			}
			
			if(isCSInSentence){
				for(int j=0;j<arrItemOracle.length;j++){
					
					if(arrItemOracle[j].trim().contains(".") ){
						//setOracles.add(arrItemOracle[j].trim());
						boolean isRunInWhile=false;
					//	System.out.println("Index type translation: "+indexTypeTranslation);
						while(indexTypeTranslation<arrItemTrans.length){
							isRunInWhile=true;
							
							if((!arrItemOracle[j].equals(arrItemTrans[indexTypeTranslation]))&&arrItemOracle[j].endsWith(arrItemTrans[indexTypeTranslation])){
								numOfOutOfVocabPerSentence++;
								strOutVocab+=arrItemTrans[indexTypeTranslation]+" ( "+arrItemOracle[j]+" ) ";
								indexTypeTranslation++;
								break;
							} else{
								if(arrItemTrans[indexTypeTranslation].trim().contains(".")){									
									if(arrItemOracle[j].equals(arrItemTrans[indexTypeTranslation])){
										numOfGoodPerSentence++;								
									} else{
										numOfInCorrectPerSentence++;
										strIncorrect+=arrItemTrans[indexTypeTranslation]+" ( "+arrItemOracle[j]+" ) ";
									}
									indexTypeTranslation++;
									break;
									
								} else{
//									if((!arrItemTrans[indexTypeTranslation].trim().startsWith("."))&&(!arrItemOracle[j].equals(arrItemTrans[indexTypeTranslation]))&&arrItemOracle[j].endsWith(arrItemTrans[indexTypeTranslation])){
//										numOfOutOfVocabPerSentence++;
//										strOutVocab+=arrItemTrans[indexTypeTranslation]+" ( "+arrItemOracle[j]+" ) ";
//										break;
//									}
									indexTypeTranslation++;
								}
							}
							
							
							
						}
						
						if(!isRunInWhile){
							numOfInCorrectPerSentence++;
						}
						
					} else{
						if(j<arrItemTrans.length&&(arrItemOracle[j].equals(arrItemTrans[j])||arrItemOracle[j].endsWith(arrItemTrans[j]))){
							//isCSInSentence=true;
							
							
						} else{
						//	System.out.println(arrItemOracle[j]+" -- "+arrItemTrans[j]);
							isCSInSentence=false;
						}
					}
					
				}
			}
			
			
			if(isCSInSentence){
				countCorrectCSInSentence++;
			}

			ptResult.print(isCLInSentence+"\t"+isCSInSentence+"\t"+arrItemOracle.length+"\t"+arrItemTrans.length+"\n");
			ptTypeStructure.print(numOfGoodPerSentence+"\t"+numOfInCorrectPerSentence+"\t"+numOfOutOfVocabPerSentence+"\n");
//			FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, isCLInSentence+"\t"+isCSInSentence+"\t"+arrItemOracle.length+"\t"+arrItemTrans.length+"\n");
//			FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, numOfGoodPerSentence+"\t"+numOfInCorrectPerSentence+"\t"+numOfOutOfVocabPerSentence+"\n");
			
			//if(!isCSInSentence){
				countOfGoodPerSentence+=numOfGoodPerSentence;
				countOfInCorrectPerSentence+=numOfInCorrectPerSentence;
				countOfOutOfVocabPerSentence+=numOfOutOfVocabPerSentence;
				
			//	FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_source.txt",arrSource[i]+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+"67_incorrect_translated.txt",arrTranslations[i]+"\n");
				
//				FileUtil.appendToFile(fop_dirTranslate+fn_OutOfVocab,strOutVocab+"\n");
//				FileUtil.appendToFile(fop_dirTranslate+fn_IncorrectTranslate, strIncorrect+"\n");
			//}
			
			
			
			
		}
		
		try{
			ptResult.close();
			ptTypeStructure.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		FileUtil.appendToFile(fop_dirTranslate+fn_evaluatedResult, countCorrectCLInSentence+"/"+arrOracles.length+"\t"+countCorrectCSInSentence+"/"+arrOracles.length+"\n");
		FileUtil.appendToFile(fop_dirTranslate+fn_typeStructureResults, countOfGoodPerSentence+"\t"+countOfInCorrectPerSentence+"\t"+countOfOutOfVocabPerSentence+"\n");

		
	}

}
