package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyConstants;
import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

class SpotifyTrackAdapter extends RecyclerView.Adapter<SpotifyTrackAdapter.ViewHolder>
{
	public interface TrackSelectedListener
	{
		void onItemSelected(View itemView, Track item, int position);
	}

	private Context context;
	private List<Track> items = new ArrayList<>();
	private TrackSelectedListener itemSelectedListener;

	private Track recentlyDeletedItem;
	private int recentlyDeletedPos;

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private CardView rootCardView;
		private final TextView title;
		private final TextView artist;
		private final TextView explicit;
		private final ImageView image;
		private int index;

		public ViewHolder(View itemView)
		{
			super(itemView);
			rootCardView = itemView.findViewById(R.id.itemRootCardView);
			title = itemView.findViewById(R.id.entityTitle);
			artist = itemView.findViewById(R.id.entityArtist);
			image = itemView.findViewById(R.id.entityImage);
			explicit = itemView.findViewById(R.id.entityExplicity);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			if (itemSelectedListener == null)
				return;

			index = getAdapterPosition();
			notifyItemChanged(index);
			itemSelectedListener.onItemSelected(v, items.get(index), index);
		}
	}

	public SpotifyTrackAdapter(Context context, TrackSelectedListener listener)
	{
		this.context = context;
		this.itemSelectedListener = listener;
	}

	public SpotifyTrackAdapter(Context context, List<Track> items)
	{
		this.context = context;
		this.items = items;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spotify_list_item, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Track item = items.get(position);

		holder.title.setText(item.name);

		if (item.explicit)
			holder.explicit.setVisibility(View.VISIBLE);
		else
			holder.explicit.setVisibility(View.GONE);

		holder.artist.setText(SpotifyUtils.toStringFromArtists(item) + SpotifyConstants.SEPERATOR + item.album.name);

		if (item.album.images.get(0) == null)
			return;

		Image image = item.album.images.get(0);
		Picasso.get().load(image.url).into(holder.image);
	}

	public void setItemSelectedListener(TrackSelectedListener listener)
	{
		this.itemSelectedListener = listener;
	}

	public void setData(List<Track> items)
	{
		this.items = items;
		notifyDataSetChanged();
	}

	public void addData(List<Track> items)
	{
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	public void deleteItem(int pos)
	{
		recentlyDeletedItem = items.get(pos);
		recentlyDeletedPos = pos;
		items.remove(pos);
		notifyItemRemoved(pos);
	}

	public void clearData()
	{
		items.clear();
	}

	public Track getRecentlyDeletedItem()
	{
		return recentlyDeletedItem;
	}

	@Override
	public int getItemCount()
	{
		if (items == null)
			return 0;

		return items.size();
	}

	public List<Track> getItems()
	{
		return items;
	}
}
