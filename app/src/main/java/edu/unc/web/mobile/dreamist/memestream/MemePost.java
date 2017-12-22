package edu.unc.web.mobile.dreamist.memestream;

public class MemePost {
	private String image_url;
	private String description;

	public MemePost(String url, String desc){
			image_url = url;
			description = desc;
	}

	public String getUrl(){
		return image_url;
	}
	public String getDescription(){
		return description;
	}
}
