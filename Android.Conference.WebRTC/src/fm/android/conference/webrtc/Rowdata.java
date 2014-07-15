package fm.android.conference.webrtc;

import fm.icelink.Link;

public class Rowdata{
	private int id;
	private String peerId;
	private Link link;
	
	public Rowdata(int id,String peerId,Link link){
		this.id=id;
		this.setPeerId(peerId);
		this.link = link;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	
}
