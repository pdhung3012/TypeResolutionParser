package data;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import parser.ProjectSequencesGenerator;
import utils.FileUtil;
import utils.NotifyingBlockingThreadPoolExecutor;

public class ExtractCodeSequenceFrom5Lib {

	private static final int THREAD_POOL_SIZE = 1;

	private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			return true; // keep waiting
		}
	};
	private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

	
	static String fop_project="G:\\githubProjs3\\";
	static String fp_inputListProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\script_java_github\\";
	static String fop_outputListProject="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output20170130\\";
	static String fop_statProjects="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\Stat5Repos\\";

	static String fop_outSequence="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libSequences\\";
	static String fop_log="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\log\\";
	
	
	static String fn_source_full_success="source_full_success.txt";
	static String fn_source_success="source_success.txt";	
	static String fn_target_full_success="target_full_success.txt";
	static String fn_target_success="target_success.txt";
	static String fn_cannotResolveType="notResolved.txt";
	static String fn_fail="fail.txt";
	static String fn_noSpecFile="noSpecificationFile.txt";
	static String fn_log="log.txt";
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File fFolder=new File(fop_statProjects);
		File[] arrFileRepos=fFolder.listFiles();
		
		int focusIndex=args.length>0?Integer.parseInt(args[0]):3;
		
		for(int i=0;i<arrFileRepos.length;i++){
			if(i==focusIndex&&arrFileRepos[i].getAbsolutePath().endsWith("-dev.txt")){
				String[] arrProjects=FileUtil.getFileContent(arrFileRepos[i].getAbsolutePath()).trim().split("\n");
				
				String currentProject=FileUtil.getFileContent(fop_log+arrFileRepos[i].getName()+"_log.txt").trim();
				int indexCurProj=currentProject.equals("")?0:Integer.parseInt(currentProject);
				
				for(int j=indexCurProj;j<arrProjects.length;j++){
					String[] projInfo=arrProjects[j].split("\t");
					if(projInfo[1].equals("true")){
						String projectItem=projInfo[0].replace("/", "_");
						final String projectPath=fop_project+projectItem+"\\";			
						final String repoInfo=arrFileRepos[i].getName();
						
						final String projectName = projectItem;
						try {
							System.out.println("Begin project "+projectName);
							pool.execute(new Runnable() {
								@Override
								public void run() {
									
									ProjectSequencesGenerator psg = new ProjectSequencesGenerator(projectPath);
									File folder=new File(fop_outSequence+projectName+"\\");
									if(!folder.isDirectory()){
										folder.mkdir();
									}
									int n = psg.generateSequences(folder.getAbsolutePath()+"\\");
									//psg.generateSequences("C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\TypeResolutionTranslation\\output_5libSequences\\VanirAOSP_frameworks_base\\");
								//	psg.generateAlignment();
									FileUtil.appendToFile(fop_log+repoInfo+"_methodStatistics.txt",projectName+"\t"+n+"\n");
									
								}
							});
							System.out.println("End project "+projectName);
							FileUtil.writeToFile(fop_log+arrFileRepos[i].getName()+"_log.txt",j+"");
						} catch (Throwable ex){
							System.err.println(ex.getMessage());
							//ex.printStackTrace();
						}
						break;
						
					}
				}
				
			}
		}
		
		
		
	}

}
