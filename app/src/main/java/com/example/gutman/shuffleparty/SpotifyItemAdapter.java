package com.example.gutman.shuffleparty;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gutman.shuffleparty.utils.SpotifyUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

class SpotifyItemAdapter<T> extends RecyclerView.Adapter<SpotifyItemAdapter<T>.ViewHolder>
{
	public interface ItemSelectedListener<T>
	{
		void onItemSelected(View itemView, T item, int position);
	}

	private List<T> items = new ArrayList<>();
	private Context context;
	private ItemSelectedListener itemSelectedListener;

	private T recentlyDeletedItem;
	private int recentlyDeletedPos;

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private final TextView title;
		private final TextView artist;
		private final TextView explicity;
		private final ImageView image;
		private int index;

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
			if (itemSelectedListener == null)
				return;

			notifyItemChanged(getLayoutPosition());
			index = getLayoutPosition();
			itemSelectedListener.onItemSelected(v, items.get(getAdapterPosition()), index);
		}
	}

	public SpotifyItemAdapter(Context context, ItemSelectedListener listener)
	{
		this.context = context;
		this.itemSelectedListener = listener;
	}

	public SpotifyItemAdapter(Context context, List<T> items)
	{
		this.context = context;
		this.items = items;
	}

	public void clearData()
	{
		items.clear();
	}

	public void addItem(T item) {
		this.items.add(item);
		notifyDataSetChanged();
	}

	public void addData(List<T> items)
	{
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	public void setData(List<T> items){
		this.items = items;
	}

	public void setItemSelectedListener(ItemSelectedListener listener)
	{
		this.itemSelectedListener = listener;
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
		T item = items.get(position);

		if (item instanceof Track)
		{
			instanceOfTrack(holder, item);
		}

		else if (item instanceof AlbumSimple) {
			instanceOfAlbumSimple(holder, item);
		}
	}

	public void deleteItem(int pos)
	{
		recentlyDeletedItem = items.get(pos);
		recentlyDeletedPos = pos;
		items.remove(pos);
		notifyItemRemoved(pos);
	}

	@Override
	public int getItemCount()
	{
		return items.size();
	}

	private void instanceOfTrack(ViewHolder holder, T item){
		Track tItem = (Track)item;

		holder.title.setText(tItem.name);

		if (tItem.explicit)
			holder.explicity.setText("EXPLICIT");
		else
			holder.explicity.setVisibility(View.GONE);


		holder.artist.setText(SpotifyUtils.toStringFromArtists(tItem) + " • " + tItem.album.name);

		Image image = tItem.album.images.get(0);
		if (image != null)
		{
			Picasso.get().load(image.url).into(holder.image);
		}
	}

	private void instanceOfAlbumSimple(ViewHolder holder, T item){
		AlbumSimple aItem = (AlbumSimple) item;

		holder.title.setText(aItem.name);

		holder.explicity.setVisibility(View.GONE);

		Image image = aItem.images.get(0);
		if (image != null) {
			Picasso.get().load(image.url).into(holder.image);
		}
	}
}
