package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.UserPrivate;

class SpotifyUserAdapter extends RecyclerView.Adapter<SpotifyUserAdapter.ViewHolder>
{
	private Context context;
	private UserPrivate userPrivate;

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		private final TextView displayName;
		private final ImageView image;

		public ViewHolder(View itemView)
		{
			super(itemView);
			displayName = itemView.findViewById(R.id.entityUsername);
			image = itemView.findViewById(R.id.entityImage);
		}
	}

	public  SpotifyUserAdapter(Context context, UserPrivate userPrivate) {
		this.context = context;
		this.userPrivate = userPrivate;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position)
	{
		String displayName = userPrivate.display_name;

		holder.displayName.setText(displayName);

		Image userImage = userPrivate.images.get(0);
		if (userImage != null) {
			Picasso.get().load(userImage.url).into(holder.image);
		}
	}

	@Override
	public int getItemCount()
	{
		return 0;
	}
}
