package com.dubiel.sample.googlebookviewerrx.viewadapter;


import com.dubiel.sample.googlebookviewerrx.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ViewHolder> {
    private String[] dataset;
    private OnDrawerItemClickListener listener;

    public interface OnDrawerItemClickListener {
        public void onDrawerItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    public DrawerListAdapter(String[] dataset, OnDrawerItemClickListener listener) {
        this.dataset = dataset;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.drawer_list_item, parent, false);
        TextView tv = (TextView) v.findViewById(android.R.id.text1);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText(dataset[position]);
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDrawerItemClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
