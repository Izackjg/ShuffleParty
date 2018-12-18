package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.test.espresso.core.internal.deps.guava.base.Joiner;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.Track;

class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>
{
	public interface TrackItemSelectedListener
	{
		void onItemSelected(View itemView, Track track, int position);
	}


	private final List<Track> items = new ArrayList<>();
	private final Context context;
	private final TrackItemSelectedListener trackListener;
	private int index;

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private final TextView title;
		private final TextView artist;
		private final TextView explicity;
		private final ImageView image;

		public ViewHolder(View itemView)
		{
			super(itemView);
			title = itemView.findViewById(R.id.entityTitle);
			artist = itemView.findViewById(R.id.entityArtist);
			image = itemView.findViewById(R.id.entityImage);
			explicity = itemView.findViewById(R.id.entityExplicity);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			notifyItemChanged(getLayoutPosition());
			trackListener.onItemSelected(v, items.get(getAdapterPosition()), getLayoutPosition());
		}
	}

	public SearchResultsAdapter(Context context, TrackItemSelectedListener listener)
	{
		this.context = context;
		this.trackListener = listener;
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

		holder.title.setText(SpotifyUtils.formatUnwantedCharsFromTitle(item.name, "("));

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

	public int getIndex()
	{
		return index;
	}
}
