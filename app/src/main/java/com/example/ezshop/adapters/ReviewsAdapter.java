package com.example.ezshop.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Review;
import com.example.ezshop.models.User;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private Context context;
    private ArrayList<Pair<Review,String>>reviewList;
    private boolean isPreview;  // to control amount of reviews shown on recycler view


    public ReviewsAdapter(Context context, ArrayList<Pair<Review, String> > reviewList, boolean preview) {
        this.context = context;
        this.reviewList = reviewList;
        this.isPreview = preview;
    }


    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Pair<Review,String> currentobj = reviewList.get(position);
        Review currentReview = currentobj.first;
        String UserName = currentobj.second;
        holder.tvName.setText(UserName);
        holder.tvRating.setText(String.valueOf(currentReview.getRating()));
        holder.tvDate.setText(currentReview.getReviewDate());
        holder.tvComment.setText(currentReview.getComment());
    }

    @Override
    public int getItemCount() {
        if (isPreview) {
            return Math.min(3, reviewList.size());
        } else {
            return reviewList.size();
        }
    }

    void setPreview(boolean val){
        this.isPreview = val;
    }




    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvDate, tvComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvReviewerName);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
        }
    }

}
