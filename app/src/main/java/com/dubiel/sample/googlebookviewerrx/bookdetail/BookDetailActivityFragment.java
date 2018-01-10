package com.dubiel.sample.googlebookviewerrx.bookdetail;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dubiel.sample.googlebookviewerrx.GoogleBooksClient;
import com.dubiel.sample.googlebookviewerrx.R;
import com.google.common.base.Joiner;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookDetailActivityFragment extends Fragment {

    public static final String ARG_VOLUME_ID = "volume_id";
    private static final String TAG = BookDetailActivityFragment.class.getSimpleName();

    private String volumeId;
    private int smallImageWidth, smallImageHeight;

    public BookDetailActivityFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_VOLUME_ID)) {
            smallImageWidth = getContext().getResources().getInteger(R.integer.small_image_width);
            smallImageHeight = getContext().getResources().getInteger(R.integer.small_image_height);

            volumeId = getArguments().getString(ARG_VOLUME_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

        final ImageView small = (ImageView) rootView.findViewById(R.id.book_detail_item_small);
        final TextView title = (TextView) rootView.findViewById(R.id.book_detail_item_title);
        final TextView authors = (TextView) rootView.findViewById(R.id.book_detail_item_author);
        final WebView description = (WebView) rootView.findViewById(R.id.book_detail_item_description);
        final TextView infoLink = (TextView) rootView.findViewById(R.id.book_detail_item_info_link);

        GoogleBooksClient.getInstance().getVolume(volumeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bookDetailItem -> {
                    if(bookDetailItem.getVolumeInfo() == null) {
                        return;
                    }

                    try {
                        Glide.with(this)
                                .load(bookDetailItem.getVolumeInfo().getImageLinks().getSmall())
                                .into(small);
                    } catch(NullPointerException e) {
                        Log.i(TAG, "npe " + volumeId + ", " + e.getMessage());
                    }

                    if(bookDetailItem.getVolumeInfo().getAuthors() != null && bookDetailItem.getVolumeInfo().getAuthors().length > 0) {
                        authors.setText(Joiner.on("\n").join(bookDetailItem.getVolumeInfo().getAuthors()));
                    }

                    if(bookDetailItem.getVolumeInfo().getDescription() != null && bookDetailItem.getVolumeInfo().getDescription().length() > 0) {
                        description.loadDataWithBaseURL(null, bookDetailItem.getVolumeInfo().getDescription(), "text/html", "utf-8", null);
                    } else {
                        description.loadDataWithBaseURL(null, getContext().getResources().getString(R.string.no_description_available), "text/html", "utf-8", null);
                    }

                    if(bookDetailItem.getVolumeInfo().getInfoLink() != null) {
                        infoLink.setText(bookDetailItem.getVolumeInfo().getInfoLink());
                    }
                }, throwable -> {
                    Log.i(TAG, throwable.getMessage());
                });

        return rootView;
    }
}
