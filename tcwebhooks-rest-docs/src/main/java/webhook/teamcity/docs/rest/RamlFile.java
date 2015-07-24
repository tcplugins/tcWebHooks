package webhook.teamcity.docs.rest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RamlFile {
	String name;
	String path;
	String type = "file";
	List<RamlFile> children;
	String content;
	
	public RamlFile(File file) {
		this.name = file.getName();
		if (file.isDirectory()){
			this.type = "folder";
		}
		this.path = file.getPath();
		//this.path = "%2F";
		//this.contents = "#%25RAML%200.8%0Atitle:%20%20%20DONE!!!";
	}
	
	public void setChildren(List<RamlFile> children) {
		this.children = children;
	}
	
//	public String getContents(){
//		return "#%25RAML%200.8%0Atitle:%20%20%20DONE!!!";
//	}

}
