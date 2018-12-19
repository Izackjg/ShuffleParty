package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

class SpotifyItemAdapter extends RecyclerView.Adapter<SpotifyItemAdapter.ViewHolder>
{
	public interface TrackItemSelectedListener
	{
		void onItemSelected(View itemView, Track item, int position);
	}

	private List<Track> items = new ArrayList<>();
	private Context context;
	private TrackItemSelectedListener trackListener;

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private final View itemView;
		private final CardView root;
		private final TextView title;
		private final TextView artist;
		private final TextView explicity;
		private final ImageView image;
		private int index;

		public ViewHolder(View itemView)
		{
			super(itemView);
			this.itemView = itemView;
			root = itemView.findViewById(R.id.rootView);
			title = itemView.findViewById(R.id.entityTitle);
			artist = itemView.findViewById(R.id.entityArtist);
			image = itemView.findViewById(R.id.entityImage);
			explicity = itemView.findViewById(R.id.entityExplicity);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			if (trackListener == null)
				return;

			notifyItemChanged(getLayoutPosition());
			index = getLayoutPosition();
			trackListener.onItemSelected(v, items.get(getAdapterPosition()), index);
		}
	}

	public SpotifyItemAdapter(Context context, TrackItemSelectedListener listener)
	{
		this.context = context;
		this.trackListener = listener;
	}

	public SpotifyItemAdapter(Context context, List<Track> items)
	{
		this.context = context;
		this.items = items;
	}

	public void clearData()
	{
		items.clear();
	}

	public void addData(List<Track> items)
	{
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	public void setData(List<Track> items)
	{
		this.items = items;
	}

	public void setTrackListener(TrackItemSelectedListener listener)
	{
		this.trackListener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Track item = items.get(position);

		holder.title.setText(item.name);

		if (item.explicit)
			holder.explicity.setText("EXPLICIT");
		else
			holder.explicity.setVisibility(View.GONE);


		holder.artist.setText(SpotifyUtils.toStringFromArtists(item) + " • " + item.album.name);

		Image image = item.album.images.get(0);
		if (image != null)
		{
			Picasso.get().load(image.url).into(holder.image);
		}
	}

	@Override
	public int getItemCount()
	{
		return items.size();
	}
}
