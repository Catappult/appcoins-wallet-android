package com.asfoundation.wallet.ui.appcoins;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class ItemDecorator extends RecyclerView.ItemDecoration {
  private final int spaceInDp;

  public ItemDecorator(int spaceInDp) {
    this.spaceInDp = spaceInDp;
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {

    if (parent.getChildAdapterPosition(view) == 0) {
      if (isRtl(view)) {
        outRect.right = spaceInDp;
        outRect.left = 0;
      } else {
        outRect.right = 0;
        outRect.left = spaceInDp;
      }
    }

    if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
      if (isRtl(view)) {
        outRect.right = 0;
        outRect.left = spaceInDp;
      } else {
        outRect.right = spaceInDp;
        outRect.left = 0; //don't forget about recycling...
      }
    }
  }

  private boolean isRtl(View view) {
    return view.getContext()
        .getResources()
        .getConfiguration()
        .getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }
}
