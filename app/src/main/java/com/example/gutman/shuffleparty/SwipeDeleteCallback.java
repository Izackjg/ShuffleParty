package com.example.gutman.shuffleparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class SwipeDeleteCallback extends ItemTouchHelper.SimpleCallback
{
	// src: https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e

	private SpotifyTrackAdapter adapter;

	private Drawable icon;
	private final ColorDrawable background;
	private int deletePos;

	public SwipeDeleteCallback(SpotifyTrackAdapter adapter, Drawable icon)
	{
		// DRAG DIRS IS 0 SINCE IT CONTROLS RECYCLER VIEW UP OR DOWN - HENCE THE 0.

		// SECOND PARAM TELL ITEMTOUCHHELPER
		// TO PASS SIMPLECALLBACK INFO
		// ABOUT LEFT AND RIGHT SWIPES
		super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
		this.adapter = adapter;

		this.icon = icon;
		background = new ColorDrawable(Color.RED);
	}

	// CALLED WHEN AN ITEM IS SWIPED OFF SCREEN.
	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
	{
		if (adapter.getItemCount() == 1)
			return;

		deletePos = viewHolder.getAdapterPosition();
		adapter.deleteItem(deletePos);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
	{
		return false;
	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
	{
		if (adapter.getItemCount() == 1)
			return;

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		// USED TO CALC BOUNDS FOR ICON & BACKGROUND.
		View itemView = viewHolder.itemView;

		// USED TO PUSH THE BACKGROUND BEHIND
		// THE EDGE OF PARENT VIEW
		// SO IT APPEARS UNDER ROUNDED CORNERS.
		int backgroundCornerOffset = 20;

		int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
		int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
		int iconBottom = iconTop + icon.getIntrinsicHeight();

		// SWIPE TO RIGHT.
		if (dX > 0)
		{
			int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
			int iconRight = itemView.getLeft() + iconMargin;
			icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

			background.setBounds(itemView.getLeft(), itemView.getTop(),
					itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
					itemView.getBottom());

			// SWIPE TO LEFT.
		} else if (dX < 0)
		{
			int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
			int iconRight = itemView.getRight() - iconMargin;
			icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

			background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
					itemView.getTop(), itemView.getRight(), itemView.getBottom());
		// VIEW IS UNSWIPED.
		} else
		{
			background.setBounds(0, 0, 0, 0);
		}

		background.draw(c);
		icon.draw(c);
	}

	public int getDeletePos()
	{
		return deletePos;
	}
}
