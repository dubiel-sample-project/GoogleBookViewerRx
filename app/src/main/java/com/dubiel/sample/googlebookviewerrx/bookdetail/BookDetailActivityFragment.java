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

import com.dubiel.sample.googlebookviewerrx.R;
import com.dubiel.sample.googlebookviewerrx.data.BookDetailItem;

public class BookDetailActivityFragment extends Fragment {

    public static final String ARG_SELF_LINK = "self_link";
    static final private String TAG = "BookDetailFragment";

    private String selfLink;
    private int smallImageWidth, smallImageHeight;

    public BookDetailActivityFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SELF_LINK)) {
            smallImageWidth = getContext().getResources().getInteger(R.integer.small_image_width);
            smallImageHeight = getContext().getResources().getInteger(R.integer.small_image_height);

            selfLink = getArguments().getString(ARG_SELF_LINK);
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

        return rootView;
    }
}
