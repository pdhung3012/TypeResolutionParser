package data;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import parser.ProjectSequencesGenerator;
import utils.FileUtil;
import utils.NotifyingBlockingThreadPoolExecutor;

public class EvaluateListProject {

	static String fop_repositories="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\5Repositories\\";
	static String fop_project="G:\\githubProjs3\\";
	static String fop_outputListProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output100K\\";
	private static final int THREAD_POOL_SIZE = 2;

	private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			return true; // keep waiting
		}
	};
	private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File folder=new File(fop_repositories);
		File[] arrFiles=folder.listFiles();
		String[] arrPackageNames={"android","gwt","xstream","hibernate","joda"};
		
		
		for(int i=0;i<arrFiles.length;i++){
			FileUtil.writeToFile(fop_outputListProject+arrFiles[i].getName()+"-core.txt", "");
			FileUtil.writeToFile(fop_outputListProject+arrFiles[i].getName()+"-dev.txt", "");
			
			String[] arrProjectInEachLibrary=FileUtil.getFileContent(arrFiles[i].getAbsolutePath()).trim().split("\n");
			int numProjectCoreHasInRepo=0;
			int numProjectDevHasInRepo=0;
			int numProjectCoreAll=0;
			int numProjectDevAll=0;
			for(int j=0;j<arrProjectInEachLibrary.length;j++){
				String projectItems=arrProjectInEachLibrary[j].split(",")[0];
				File fProj=new File(fop_project+projectItems.replace("/","_"));
				boolean isInRepo=false;
				if(fProj.getName().contains(arrPackageNames[i])){
					numProjectCoreAll++;
					
					if(fProj.isDirectory()){
						numProjectCoreHasInRepo++;
						isInRepo=true;
					}
					FileUtil.appendToFile(fop_outputListProject+arrFiles[i].getName()+"-core.txt", projectItems+"\t"+isInRepo+"\n");
				} else{
					numProjectDevAll++;
					if(fProj.isDirectory()){
						numProjectDevHasInRepo++;
						isInRepo=true;
						
						
						
					}
					
					
					
				}
				
				
			}
		}

	}

}
