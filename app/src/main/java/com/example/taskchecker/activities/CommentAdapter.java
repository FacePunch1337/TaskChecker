package com.example.taskchecker.activities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taskchecker.R;
import com.example.taskchecker.models.BoardButtonModel;
import com.example.taskchecker.models.CommentModel;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends ArrayAdapter<CommentModel> {
    private Context mContext;
    private ArrayList<CommentModel> comments;

    public CommentAdapter(Context context, ArrayList<CommentModel> comments) {
        super(context, 0, comments);
        this.mContext = context;
        this.comments = comments;

    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.comment_view, parent, false);
        }

        final CommentModel currentComment = comments.get(position);

        TextView text = listItem.findViewById(R.id.usernameTextView);
        text.setText(currentComment.getText());



        return listItem;
    }
}
