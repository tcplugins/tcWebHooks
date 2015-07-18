package webhook.teamcity.docs.rest;

import java.io.File;

public class RamlFile {
	String id;
	String name;
	String path;
	String type = "file";
	//String contents;
	
	
	public RamlFile(File file) {
		this.id = file.getName();
		this.name = file.getName();
		if (file.isDirectory()){
			this.type = "directory";
		}
		this.path = file.getPath();
		//this.path = "%2F";
		//this.contents = "#%25RAML%200.8%0Atitle:%20%20%20DONE!!!";
	}
	
//	public String getContents(){
//		return "#%25RAML%200.8%0Atitle:%20%20%20DONE!!!";
//	}

}
