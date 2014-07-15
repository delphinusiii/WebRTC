package fm.android.conference.webrtc;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fm.icelink.Link;

public class Adapter extends BaseAdapter {

	ArrayList<Rowdata> array;
	LayoutInflater inflater;
	Context ctx;
	public Adapter(){
		array = new ArrayList<Rowdata>();
	}
	public void addItem(String[] obj,Link[] links){
		for(int i=0;i<obj.length;i++){
			array.add(new Rowdata(i+1, obj[i],links[i]));
			this.notifyDataSetChanged();
		}
	}
	public void removeAll(){
		array.clear();
		this.notifyDataSetChanged();
	}
	public void MyBaseAdapter(Context context, ArrayList<Rowdata> array) {
		this.array = array;
		this.ctx = context;
		inflater = LayoutInflater.from(this.ctx);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return array.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return array.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.item_row, parent, false);
		}
		final Rowdata rowdata = array.get(position);
		
		TextView id = (TextView) convertView.findViewById(R.id.id);
		TextView row = (TextView) convertView.findViewById(R.id.row);
		if (rowdata != null) {
			id.setText(String.valueOf(rowdata.getId()));
			row.setText(rowdata.getRow());
		}
		OnClickListener selectPeer = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		};
		return convertView;
	}
}
