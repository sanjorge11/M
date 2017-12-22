package edu.unc.web.mobile.dreamist.memestream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
	private DatabaseReference postdb;
	private Context mContext;
	ArrayList<String> nodes;

	public static class ViewHolder extends RecyclerView.ViewHolder{
		public TextView mTextView;
		public ImageView mImageView;
		public ViewHolder (View v){
			super(v);
			mTextView = v.findViewById(R.id.memeDesc);
			mImageView = v.findViewById(R.id.memePhoto);
		}
	}

	public MyAdapter(DatabaseReference postdbinstance, Context mContext) {
		postdb = postdbinstance;
		this.mContext = mContext;

		nodes = new ArrayList<>();
		postdb.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				nodes.clear();
				for(DataSnapshot item: dataSnapshot.getChildren()){
					Log.d("MEMESTREAM", item.getKey().toString() + ":" +
					item.getValue().toString());
					nodes.add(item.getValue().toString());
				}
				notifyDataSetChanged();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
	}

	@Override
	public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.my_view_group, parent, false);
		return new ViewHolder(v);
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {

		//holder.mTextView.setText("" + position + "out of " + getItemCount() + " Memes!");
		Log.d("MEMESTREAM","onBindViewHolder called");
		Picasso.with(mContext).load(nodes.get(position))
				.error(R.drawable.ic_launcher_foreground)
				.placeholder(R.drawable.ic_launcher_background)
				.into(holder.mImageView);
		holder.mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ImageView v = (ImageView) view;
				Bitmap bm = ((BitmapDrawable) v.getDrawable()).getBitmap();
				String newfile = "meme" + (new Date()).getTime() + ".jpg";
				File file = new File(Environment.getExternalStorageDirectory() +
						"/" + Environment.DIRECTORY_DOWNLOADS, newfile);
				try {
					FileOutputStream imageFile = new FileOutputStream(file);
					bm.compress(Bitmap.CompressFormat.JPEG, 100, imageFile);
					imageFile.flush();
					imageFile.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		Log.d("MEMESTREAM", "URL:" + nodes.get(position));
	}

	@Override
	public int getItemCount() {
		Log.d("MEMESTREAM", "GetItemCount called:" + nodes.size());
		return nodes.size();
	}
}
