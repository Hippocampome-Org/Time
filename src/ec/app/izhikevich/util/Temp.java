package ec.app.izhikevich.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Temp {

	public static void main(String[] args) throws IOException {
		String directory = "C:\\Users\\sivav\\Projects\\TIMES\\output\\11\\mcdone";
		String copyToDir = "C:\\Users\\sivav\\Projects\\TIMES\\output\\11\\mcdonev2";
		File dir = new File(directory);
		File[] subDirs = dir.listFiles();
		
		for(File sub_dir: subDirs) {
			File fileName = new File(sub_dir.getAbsolutePath()+"\\job.0.Full");
			String dest_sub_dir_name = sub_dir.getName().substring(8);
			File dest_sub_dir = new File(copyToDir+"\\"+dest_sub_dir_name);
			if(!dest_sub_dir.exists()) {
				dest_sub_dir.mkdir();
			}
			File destFile = new File(dest_sub_dir+"\\job.0.Full");
			Files.copy(fileName.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		}
	}

}
