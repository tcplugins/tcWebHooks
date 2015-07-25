package webhook.teamcity.docs.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.annotations.Expose;

public class RamlFile {
	private File file;
	
	@Expose
	String name;
	
	@Expose
	String path;
	
	@Expose
	String type = "file";
	
	@Expose
	List<RamlFile> children;
	
	@Expose
	String contents;
	
	@Expose
	String meta = "some meta meta meta";
	
	public RamlFile(File file){
		this.file = file;
		this.name = file.getName();
		if (file.isDirectory()){
			this.type = "folder";
		}
		this.path = file.getPath().substring(WebHookApiServer.RAML_FILE_LOCATION.length());
		

		//this.path = "%2F";
		//this.contents = "#%25RAML%200.8%0Atitle:%20%20%20DONE!!!";
	}
	
	public void setChildren(List<RamlFile> children) {
		this.children = children;
	}
	
	public void loadContents() throws IOException {
		FileInputStream inputStream = new FileInputStream(this.file);
		try {
		    this.contents = IOUtils.toString(inputStream).replaceAll("%", "%25");
		} finally {
		    inputStream.close();
		}
	}
	
	public String getContents() {
		return this.contents;
	}

}
