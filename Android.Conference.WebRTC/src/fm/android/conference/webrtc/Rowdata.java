package fm.android.conference.webrtc;

public class Rowdata{
	private int id;
	private String row;
	
	public Rowdata(int id,String row){
		this.id=id;
		this.row=row;
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
	
}
