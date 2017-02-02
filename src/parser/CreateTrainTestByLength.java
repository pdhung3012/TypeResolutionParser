package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import utils.FileUtil;

public class CreateTrainTestByLength {

	public static int randInt(int min, int max) {

	    // NOTE: This will (intentionally) not run as written so that folks
	    // copy-pasting have to think about how to initialize their
	    // Random instance.  Initialization of the Random instance is outside
	    // the main scope of the question, but some decent options are to have
	    // a field that is initialized once and then re-used as needed or to
	    // use ThreadLocalRandom (if using at least Java 1.7).
		Random rn = new Random();
		int range = max - min + 1;
		int randomNum =  rn.nextInt(range) + min;

	    return randomNum;
	}
	

	//_createTrainTestSet
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dir="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\";
		String[] arrLocations=FileUtil.getFileContent(fop_dir+"locations.txt").trim().split("\n");		
		String[] arrFilesSource=FileUtil.getFileContent(fop_dir+"source.txt").trim().split("\n");
		String[] arrFilesTarget=FileUtil.getFileContent(fop_dir+"target.txt").trim().split("\n");
		String[] arrAlignmentStoT=FileUtil.getFileContent(fop_dir+"\\originAlignment\\training.s-t.A3").trim().split("\n");
		String[] arrAlignmentTtoS=FileUtil.getFileContent(fop_dir+"\\originAlignment\\training.t-s.A3").trim().split("\n");
		
		System.out.println(arrAlignmentStoT.length);
		
		int totalRowForDataset=3300;
		int numberGetRandom=300, numberTrain=3000;
		int lengthMax=25;
		HashSet<Integer> setTest=new HashSet<Integer>();
		HashSet<Integer> setTrain=new HashSet<Integer>();
		int indexCountTest=0,indexCountTrain=0,indexCount=0,countBufferTest=0,countBufferTrain=0;
		HashMap<Integer,Integer> mapRequirements=new HashMap<Integer, Integer>();
		mapRequirements.put(1, 0);
		mapRequirements.put(2, 0);
		mapRequirements.put(3, 0);
		mapRequirements.put(4, 0);
		mapRequirements.put(5, 0);
		
		//HashMap<Integer,Integer> mapRequirement=new HashMap<Integer, Integer>();
		int indexRunTest=0;
		while(indexRunTest<arrFilesSource.length&&indexCount<numberGetRandom){
			int ran=indexRunTest;//randInt(0, arrFilesSource.length-1);
			//check test satisfy
			boolean percentResolved=arrLocations[ran].trim().endsWith("100%");
			if(percentResolved&&!setTest.contains(ran)){
				int length=arrFilesSource[ran].split("\\s+").length;
				if(length>=3&&length<=5){
					if(mapRequirements.get(1)<=(numberGetRandom/5)){
						mapRequirements.put(1, mapRequirements.get(1));
						setTest.add(ran);
						indexCount++;
					}
				} else if (length>5&&length<=10){
					if(mapRequirements.get(2)<(numberGetRandom/5)){
						mapRequirements.put(2, mapRequirements.get(2));
						setTest.add(ran);
						indexCount++;
					}
				} else if (length>10&&length<=15){
					if(mapRequirements.get(3)<(numberGetRandom/5)){
						mapRequirements.put(3, mapRequirements.get(3));
						setTest.add(ran);
						indexCount++;
					}
					
				} else if (length>15&&length<=20){
					
					if(mapRequirements.get(4)<(numberGetRandom/5)){
						mapRequirements.put(4, mapRequirements.get(4));
						setTest.add(ran);
						indexCount++;
					}
					
				} else if (length>20&&length<=25){
					
					if(mapRequirements.get(5)<(numberGetRandom/5)){
						mapRequirements.put(5, mapRequirements.get(5));
						setTest.add(ran);
						indexCount++;
					}
					
				}
				
			}
			indexRunTest++;
			
		}
		
		mapRequirements=new HashMap<Integer, Integer>();
		mapRequirements.put(1, 0);
		mapRequirements.put(2, 0);
		mapRequirements.put(3, 0);
		mapRequirements.put(4, 0);
		mapRequirements.put(5, 0);
		mapRequirements.put(6, 0);
	//	setTest=new HashSet<Integer>();
		//HashMap<Integer,Integer> mapRequirement=new HashMap<Integer, Integer>();
		indexCount=0;
		int indexRun=0;
		while(indexRun<arrFilesSource.length){
		//	index
			//int ran=randInt(0, totalRowForDataset-1);
			//check test satisfy
			boolean percentResolved=arrLocations[indexRun].trim().endsWith("100%");
			if(percentResolved&&!setTest.contains(indexRun)){
				int length=arrFilesSource[indexRun].split("\\s+").length;
				if(length>=3&&length<=5){
					if(mapRequirements.get(1)<=(numberGetRandom/5)){
						mapRequirements.put(1, mapRequirements.get(1));
						setTrain.add(indexRun);
						indexCount++;
					
					}
				} else if (length>5&&length<=10){
					if(mapRequirements.get(2)<(numberGetRandom/5)){
						mapRequirements.put(2, mapRequirements.get(2));
						setTrain.add(indexRun);
						indexCount++;
					}
				} else if (length>10&&length<=15){
					if(mapRequirements.get(3)<(numberGetRandom/5)){
						mapRequirements.put(3, mapRequirements.get(3));
						setTrain.add(indexRun);
						indexCount++;
					}
					
				} else if (length>15&&length<=20){
					
					if(mapRequirements.get(4)<(numberGetRandom/5)){
						mapRequirements.put(4, mapRequirements.get(4));
						setTrain.add(indexRun);
						indexCount++;
					}
					
				} else if (length>20&&length<=25){
					
					if(mapRequirements.get(5)<(numberGetRandom/5)){
						mapRequirements.put(5, mapRequirements.get(5));
						setTrain.add(indexRun);
						indexCount++;
					}
				}	
//				}else{
//					//if(mapRequirements.get(6)<(numberGetRandom/5)){
//						mapRequirements.put(6, mapRequirements.get(6)+1);
//						setTrain.add(indexRun);
//						indexCount++;
//					//}
//				}
				
			}
			
			if(indexCount==numberTrain){
				break;
			}
			
			indexRun++;
		}
		
		System.out.println("Token length 1 "+mapRequirements.get(1));
		System.out.println("Token length 2 "+mapRequirements.get(2));
		System.out.println("Token length 3 "+mapRequirements.get(3));
		System.out.println("Token length 4 "+mapRequirements.get(4));
		System.out.println("Token length 5 "+mapRequirements.get(5));
		System.out.println("Token length 6 "+mapRequirements.get(6));
		System.out.println("Total "+indexRun);
		FileUtil.writeToFile(fop_dir+"test.s", "");
		FileUtil.writeToFile(fop_dir+"test.t", "");
		FileUtil.writeToFile(fop_dir+"train.s", "");
		FileUtil.writeToFile(fop_dir+"train.t", "");
		FileUtil.writeToFile(fop_dir+"train_location.txt", "");
		FileUtil.writeToFile(fop_dir+"test_location.txt", "");
		FileUtil.writeToFile(fop_dir+"training.t-s.A3", "");
		FileUtil.writeToFile(fop_dir+"training.s-t.A3", "");
		for(Integer itemInt:setTest){
			
			FileUtil.appendToFile(fop_dir+"test.s", arrFilesSource[itemInt]+"\n");
			FileUtil.appendToFile(fop_dir+"test.t", arrFilesTarget[itemInt]+"\n");
			FileUtil.appendToFile(fop_dir+"test_location.txt", arrLocations[itemInt]+"\n");
			

		}
		int indexTrain=0;
		for(Integer itemInt:setTrain){
			FileUtil.appendToFile(fop_dir+"train.s", arrFilesSource[itemInt]+"\n");
			FileUtil.appendToFile(fop_dir+"train.t", arrFilesTarget[itemInt]+"\n");
			
			
			FileUtil.appendToFile(fop_dir+"train_location.txt", arrLocations[itemInt]+"\n");
			
			FileUtil.appendToFile(fop_dir+"training.s-t.A3","# sentence pair ("+indexTrain+") source"+ arrAlignmentStoT[(itemInt)*3].trim().split("\\) source")[1]+"\n");
			FileUtil.appendToFile(fop_dir+"training.s-t.A3", arrAlignmentStoT[(itemInt)*3+1].trim()+"\n");
			FileUtil.appendToFile(fop_dir+"training.s-t.A3", arrAlignmentStoT[(itemInt)*3+2].trim()+"\n");
			

			FileUtil.appendToFile(fop_dir+"training.t-s.A3","# sentence pair ("+indexTrain+") source"+ arrAlignmentTtoS[(itemInt)*3].trim().split("\\) source")[1]+"\n");
			FileUtil.appendToFile(fop_dir+"training.t-s.A3", arrAlignmentTtoS[(itemInt)*3+1].trim()+"\n");
			FileUtil.appendToFile(fop_dir+"training.t-s.A3", arrAlignmentTtoS[(itemInt)*3+2].trim()+"\n");
			indexTrain++;
			

		}
		
		System.out.println("Test size: "+setTest.size());
		
//		FileUtil.appendToFile(fop_dir+"train_source.txt", bdTrainSource.toString().trim());
//		FileUtil.appendToFile(fop_dir+"train_target.txt", bdTrainTarget.toString().trim());
//		FileUtil.appendToFile(fop_dir+"test_source.txt", bdTestSource.toString().trim());
//		FileUtil.appendToFile(fop_dir+"test_target.txt", bdTestTarget.toString().trim());		
	}

}
