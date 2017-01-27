package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Random;

import utils.FileUtil;

public class RandomTrainTest {

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
	
	public static void main_old(String[] args) {
		String fp_input="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\outputCodeSequence\\0_50_target_full_success.txt";
		int indexChoose=0;
		int numAble=0;
    	int numTotal=0;
		HashSet<Integer> setChoose=new HashSet<Integer>();
    	try (BufferedReader br = new BufferedReader(new FileReader(fp_input))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	String[] arrItem=line.split("\t");
		    	
		    	if(arrItem.length>=3){
		    		int i1=Integer.parseInt(arrItem[2].split("/")[0]);		    	
		    		int i2=Integer.parseInt(arrItem[2].split("/")[1]);
		    	//	numAble+=i1;
		    		//numTotal+=i2;
		    		if(i1==i2){
		    			setChoose.add(indexChoose);
		    			numTotal++;
		    		}else{
		    			//System.out.println(line);
		    		}
		    	}
		    	indexChoose++;
		    	
		    }
		    
		    System.out.println(numTotal);
		    
		    br.close();
		}catch(Exception ex){
			
		}
    	
    	String[] arrSource=FileUtil.getFileContent(fp_input.replace("0_50_target_full_success.txt", "0_50_source_success.txt")).split("\n");
    	String[] arrTarget=FileUtil.getFileContent(fp_input.replace("0_50_target_full_success.txt", "0_50_target_success.txt")).split("\n");
    	
    	StringBuilder sourceB=new StringBuilder();
    	StringBuilder targetB=new StringBuilder();
    	
    	int countBuf=0;
    	for(int i=0;i<arrSource.length;i++){
    		if(setChoose.contains(i)&&arrTarget[i].split("\\s").length>10&&arrTarget[i].split("\\s").length<100&&arrTarget[i].contains(".")){
    			sourceB.append(arrSource[i]+"\n");
    			targetB.append(arrTarget[i]+"\n");
    		//	System.out.println(arrTarget[i]);
    			countBuf++;
    			if(countBuf==1000){
        			FileUtil.appendToFile(fp_input.replace("0_50_target_full_success.txt", "181_source.txt"), sourceB.toString());
        			FileUtil.appendToFile(fp_input.replace("0_50_target_full_success.txt", "181_target.txt"), targetB.toString());
        			
        			sourceB=new StringBuilder();
        			targetB=new StringBuilder();
        			countBuf=0;
        		}
    		}
    		
    	}
    	if(sourceB.length()>0){
    		FileUtil.appendToFile(fp_input.replace("0_50_target_full_success.txt", "181_source.txt"), sourceB.toString());
			FileUtil.appendToFile(fp_input.replace("0_50_target_full_success.txt", "181_target.txt"), targetB.toString());
			
			sourceB=new StringBuilder();
			targetB=new StringBuilder();
			
    	}
    	
    	
    	
	
	}
	//_createTrainTestSet
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop_dir="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170125\\";
		//String[] arrFilesSource=FileUtil.getFileContent(fop_dir+"full_source.txt").trim().split("\n");
		//String[] arrFilesTarget=FileUtil.getFileContent(fop_dir+"full_target.txt").trim().split("\n");
		int totalRowForDataset=3000;
		int numberGetRandom=totalRowForDataset/10;
		HashSet<Integer> setTest=new HashSet<Integer>();
		int indexCountTest=0,indexCountTrain=0,indexCount=0,countBufferTest=0,countBufferTrain=0;
		while(indexCount<numberGetRandom){
			int ran=randInt(0, totalRowForDataset-1);
			if(!setTest.contains(ran)){
				setTest.add(ran);
				indexCount++;
			}
			
		}
		StringBuilder bdTestSource=new StringBuilder();
		StringBuilder bdTestTarget=new StringBuilder();
		
		StringBuilder bdTrainSource=new StringBuilder();
		StringBuilder bdTrainTarget=new StringBuilder();
		int indexChoose=0;
		try (BufferedReader br = new BufferedReader(new FileReader(fop_dir+"source.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	
		    	if(setTest.contains(indexChoose)){
		    		bdTestSource.append(line+"\n");
			    	countBufferTest++;
			    	if(countBufferTest==10000){
						FileUtil.appendToFile(fop_dir+"test.s",  bdTestSource.toString());
						bdTestSource=new StringBuilder();
						countBufferTest=0;
					}
		    	}else{
		    		bdTrainSource.append(line+"\n");
			    	countBufferTrain++;
			    	if(countBufferTrain==10000){
						FileUtil.appendToFile(fop_dir+"train.s",  bdTrainSource.toString());
						bdTrainSource=new StringBuilder();
						countBufferTrain=0;
					}
		    	}
		    	indexChoose++;
		    	if(indexChoose==totalRowForDataset){
		    		break;
		    	}
		    	
		    }
		    
		    if(bdTestSource.length()>0){
				FileUtil.appendToFile(fop_dir+"test.s", bdTestSource.toString());
			//	FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
				bdTestSource=new StringBuilder();
				//bufferTarget=new StringBuilder();
				
				countBufferTest=0;
			}
		    
		    if(bdTrainSource.length()>0){
				FileUtil.appendToFile(fop_dir+"train.s", bdTrainSource.toString());
			//	FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
				bdTrainSource=new StringBuilder();
				//bufferTarget=new StringBuilder();
				
				countBufferTrain=0;
			}
		    
		    br.close();
		}catch(Exception ex){
			
		}
		
		indexChoose=0;
		try (BufferedReader br = new BufferedReader(new FileReader(fop_dir+"target.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	
		    	if(setTest.contains(indexChoose)){
		    		bdTestTarget.append(line+"\n");
			    	countBufferTest++;
			    	if(countBufferTest==10000){
						FileUtil.appendToFile(fop_dir+"test.t",  bdTestTarget.toString());
						bdTestTarget=new StringBuilder();
						countBufferTest=0;
					}
		    	}else{
		    		bdTrainTarget.append(line+"\n");
			    	countBufferTrain++;
			    	if(countBufferTrain==10000){
						FileUtil.appendToFile(fop_dir+"train.t",  bdTrainTarget.toString());
						bdTrainTarget=new StringBuilder();
						countBufferTrain=0;
					}
		    	}
		    	indexChoose++;
		    	if(indexChoose==totalRowForDataset){
		    		break;
		    	}
		    	
		    }
		    
		    if(bdTestTarget.length()>0){
				FileUtil.appendToFile(fop_dir+"test.t", bdTestTarget.toString());
			//	FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
				bdTestTarget=new StringBuilder();
				//bufferTarget=new StringBuilder();
				
				countBufferTest=0;
			}
		    
		    if(bdTrainTarget.length()>0){
				FileUtil.appendToFile(fop_dir+"train.t", bdTrainTarget.toString());
			//	FileUtil.appendToFile(fop_output+"full_target.txt", bufferTarget.toString());
				bdTrainSource=new StringBuilder();
				//bufferTarget=new StringBuilder();
				
				countBufferTrain=0;
			}
		    
		    br.close();
		}catch(Exception ex){
			
		}
		System.out.println("Test size: "+setTest.size());
		
//		FileUtil.appendToFile(fop_dir+"train_source.txt", bdTrainSource.toString().trim());
//		FileUtil.appendToFile(fop_dir+"train_target.txt", bdTrainTarget.toString().trim());
//		FileUtil.appendToFile(fop_dir+"test_source.txt", bdTestSource.toString().trim());
//		FileUtil.appendToFile(fop_dir+"test_target.txt", bdTestTarget.toString().trim());		
	}

}
