package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.UserPrivate;

class SpotifyUserAdapter extends RecyclerView.Adapter<SpotifyUserAdapter.ViewHolder>
{
	private Context context;
	private List<UserPrivate> users;

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		private CardView rootCardView;
		private final TextView username;
		private final TextView country;
		private final ImageView image;

		public ViewHolder(View itemView)
		{
			super(itemView);
			rootCardView = itemView.findViewById(R.id.userRootCardView);
			username = itemView.findViewById(R.id.entityUsername);
			country = itemView.findViewById(R.id.entityCountry);
			image = itemView.findViewById(R.id.entityProfilePicture);
		}
	}

	public SpotifyUserAdapter(Context context, List<UserPrivate> users)
	{
		this.context = context;
		this.users = users;
	}

	public void clearData()
	{
		users.clear();
	}

	public void addData(List<UserPrivate> users)
	{
		this.users.addAll(users);
		notifyDataSetChanged();
	}

	public void setData(List<UserPrivate> users)
	{
		this.users = users;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spotify_user_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		UserPrivate user = users.get(position);

		holder.username.setText("Username: " + user.display_name);
		holder.country.setText("Country: " + user.country);

		if (user.images == null)
			holder.image.setImageResource(R.drawable.round_person);
		else
		{
			Image image = user.images.get(0);
			Picasso.get().load(image.url).into(holder.image);
		}
	}

	@Override
	public int getItemCount()
	{
		if (users == null)
			return 0;

		return users.size();
	}
}
