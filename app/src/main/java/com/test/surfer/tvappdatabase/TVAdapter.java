package com.test.surfer.tvappdatabase;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by surfer on 03/10/2015.
 */
public class TVAdapter extends CursorAdapter {

    public TVAdapter(Context context, Cursor c, int flags) {super(context, c, flags);}

    static final String POSTER_SIZE = "w500";

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.grid_item_layout;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // content description for icon field
        viewHolder.posterView.setContentDescription(cursor.getString(TVFragment.COL_TITLE));

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.tv_exp_launcher)
                .showImageOnFail(R.drawable.tv_exp_launcher)
                .showImageOnLoading(R.drawable.tv_exp_launcher2).build();
        imageLoader.displayImage(getImageURL(cursor), viewHolder.posterView, options);
    }

    private String getImageURL(Cursor cursor) {
        return "http://image.tmdb.org/t/p/"+POSTER_SIZE+"/" + cursor.getString(TVFragment.COL_POSTER);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.image);
        }
    }

    public class ImageItem {
        private Bitmap image;

        public ImageItem(Bitmap image) {
            super();
            this.image = image;
        }

        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }
    }
}
