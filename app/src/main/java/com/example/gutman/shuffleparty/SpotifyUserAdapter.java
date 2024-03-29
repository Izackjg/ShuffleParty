package com.example.gutman.shuffleparty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.UserPrivate;

class SpotifyUserAdapter extends RecyclerView.Adapter<SpotifyUserAdapter.ViewHolder>
{
	private Context context;
	private List<UserPrivate> users;
	private List<Boolean> permissionTypes;

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

	public SpotifyUserAdapter(Context context, List<UserPrivate> users, List<Boolean> permissionTypes)
	{
		this.context = context;
		this.users = users;
		this.permissionTypes = permissionTypes;
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
		boolean isAdmin = permissionTypes.get(position);

		if (isAdmin)
			holder.username.setTextColor(context.getResources().getColor(R.color.adminRed));

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
