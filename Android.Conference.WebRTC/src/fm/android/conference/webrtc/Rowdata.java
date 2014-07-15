package fm.android.conference.webrtc;

import fm.icelink.Link;

public class Rowdata{
	private int id;
	private String row;
	private Link link;
	
	public Rowdata(int id,String row,Link link){
		this.id=id;
		this.row=row;
		this.link = link;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
}
