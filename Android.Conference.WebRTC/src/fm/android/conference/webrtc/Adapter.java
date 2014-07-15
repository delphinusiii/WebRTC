package fm.android.conference.webrtc;

import java.util.ArrayList;

import android.R.layout;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Adapter extends BaseAdapter {

	ArrayList<Rowdata> array;
	LayoutInflater inflater;
	Context ctx;

	public Adapter() {
		array = new ArrayList<Rowdata>();
		array.add(new Rowdata(1, "row1"));
		array.add(new Rowdata(2, "row2"));
		array.add(new Rowdata(3, "row3"));
		array.add(new Rowdata(4, "row4"));
		array.add(new Rowdata(5, "row5"));
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
		Rowdata rowdata = array.get(position);

		TextView id = (TextView) convertView.findViewById(R.id.id);
		TextView row = (TextView) convertView.findViewById(R.id.row);
		if (rowdata != null) {
			id.setText(String.valueOf(rowdata.getId()));
			row.setText(rowdata.getRow());
		}
		return convertView;
	}
}
