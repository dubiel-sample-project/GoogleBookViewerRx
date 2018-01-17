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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dubiel.sample.googlebookviewerrx.MainActivity.MAX_RESULTS;

public class BookItemListAdapter extends RecyclerView.Adapter<BookItemListAdapter.ViewHolder> {

    static final private String TAG = "BookItemListAdapter";

    private Context context;
//    private Cache<Integer, BookListItems> bookListItems;
    private List<BookListItem> bookListItems = new ArrayList<>();
    private int smallThumbnailWidth, smallThumbnailHeight;
    private int itemCount;

    public BookItemListAdapter(Context context) {
        this.context = context;

        smallThumbnailWidth = context.getResources().getInteger(R.integer.small_thumbnail_width);
        smallThumbnailHeight = context.getResources().getInteger(R.integer.small_thumbnail_height);
    }

    @Override
    public BookItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookItemListAdapter.ViewHolder viewHolder, int i) {
        System.out.println("onBindViewHolder, i: " + i);
//        int key = (int)Math.floor(i / (this.bookListItems.size() * MAX_RESULTS));

        try {
//            BookListItems currentBookListItems = bookListItems.get(key);
//            if(currentBookListItems == null) {
//                return;
//            }
//
//            int bookListItemIndex = i % MAX_RESULTS;
//
//            if(bookListItemIndex >= currentBookListItems.getItems().length) {
//                return;
//            }

            BookListItem currentBookListItem = bookListItems.get(i);

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
//        if(bookListItems.size() == 0) {
//            return 0;
//        }
        return bookListItems.size();
    }

    public void setItemCount(int itemCount) {
//        this.itemCount = itemCount;
    }

    public void add(BookListItems bookListItems) {
//        int key = this.bookListItems.size();
//        if(!this.bookListItems.isEmpty() && this.bookListItems.size() > 1) {
//            int minKey = Collections.min(this.bookListItems.keySet());
//            this.bookListItems.remove(minKey);
//        }
//        this.bookListItems.put(key, bookListItems);
//        notifyItemRangeInserted(key * MAX_RESULTS, MAX_RESULTS);
        for(BookListItem item : bookListItems.getItems()) {
            this.bookListItems.add(item);
        }
        notifyDataSetChanged();

//        int start = key * MAX_RESULTS;
//        if(key < 0) {
//            key = 0;
//        }
//        int end = start + MAX_RESULTS;
//        for(int i = start; i < end; i++) {
//        }
    }

    public void removeKey(int key) {
//        int start = key * MAX_RESULTS;
        int start = 0;
        int end = start + MAX_RESULTS;
        for(int i = start; i < end; i++) {
            this.bookListItems.remove(i);
        }
        notifyDataSetChanged();

//        if(this.bookListItems.containsKey(key)) {
//            this.bookListItems.remove(key);
//            int positionStart = (key - 1) * MAX_RESULTS;
//            if(positionStart < 0) {
//                positionStart = 0;
//            }
//            notifyItemRangeRemoved(positionStart, MAX_RESULTS);
//        }
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
