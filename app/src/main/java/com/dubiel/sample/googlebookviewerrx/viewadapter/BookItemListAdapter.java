package com.dubiel.sample.googlebookviewerrx.viewadapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dubiel.sample.googlebookviewerrx.R;
import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivity;
import com.dubiel.sample.googlebookviewerrx.bookdetail.BookDetailActivityFragment;
import com.dubiel.sample.googlebookviewerrx.data.BookListItem;
import com.dubiel.sample.googlebookviewerrx.data.BookListItems;
import com.google.common.cache.Cache;

import java.util.Collections;

import static com.dubiel.sample.googlebookviewerrx.MainActivity.MAX_RESULTS;

public class BookItemListAdapter extends RecyclerView.Adapter<BookItemListAdapter.ViewHolder> {

    static final private String TAG = "BookItemListAdapter";

    private Context context;
    private Cache<Integer, BookListItems> bookListItems;

    public BookItemListAdapter(Context context, Cache<Integer, BookListItems> bookListItems) {
        this.context = context;
        this.bookListItems = bookListItems;
    }

    @Override
    public BookItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookItemListAdapter.ViewHolder viewHolder, int i) {
        System.out.println("onBindViewHolder, i: " + i);
        System.out.println("onBindViewHolder, min keyset: " + Collections.min(bookListItems.asMap().keySet()));

        int offset = Collections.min(bookListItems.asMap().keySet()) * MAX_RESULTS;
        System.out.println("onBindViewHolder, offset: " + offset);

        int position = i + offset;
        System.out.println("onBindViewHolder, position: " + position);

        int key = (int)Math.floor(position / MAX_RESULTS);

        System.out.println("onBindViewHolder, key: " + key);

        try {
            BookListItems currentBookListItems = bookListItems.getIfPresent(key);

            int bookListItemIndex = position % MAX_RESULTS;
            System.out.println("onBindViewHolder, bookListItemIndex: " + bookListItemIndex);

            if(bookListItemIndex >= currentBookListItems.getItems().length) {
                return;
            }

            BookListItem currentBookListItem = currentBookListItems.getItems()[bookListItemIndex];

            viewHolder.volumeId = currentBookListItem.getId();
            try {
                Glide.with(context)
                        .load(currentBookListItem.getVolumeInfo().getImageLinks().getSmallThumbnail())
                        .into(viewHolder.smallThumbnail);
            } catch(NullPointerException e) {
                Log.i(BookItemListAdapter.TAG, "small thumbnail image of " + currentBookListItem.getSelfLink() + " not present");
            }
            viewHolder.title.setText(currentBookListItem.getVolumeInfo().getTitle());

            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra(BookDetailActivityFragment.ARG_VOLUME_ID, viewHolder.volumeId);
                    context.startActivity(intent);
                }
            });
        } catch(Exception e) {
            Log.e(BookItemListAdapter.TAG, e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if(bookListItems.size() == 0) {
            return 0;
        }
        return (int)bookListItems.size() * MAX_RESULTS;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView smallThumbnail;
        private TextView title;

        public final View view;
        public String volumeId;

        public ViewHolder(View view) {
            super(view);

            this.view = view;

            title = (TextView)view.findViewById(R.id.book_item_title);
            smallThumbnail = (ImageView) view.findViewById(R.id.book_item_small_thumbnail);
        }
    }

}
