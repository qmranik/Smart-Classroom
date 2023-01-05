package com.example.smartclassroom.ui.recording;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartclassroom.R;

import java.io.File;
import java.util.Date;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.RecordingViewHolder> {

    private File[] allFiles;

    private onItemListClick onitemlistclick;

    public RecordingListAdapter(File[] allFiles,  onItemListClick onitemlistclick){
        this.allFiles = allFiles;
        this.onitemlistclick = onitemlistclick;
    }


    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item,parent,false);

        return new RecordingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {


        Date d = new Date(allFiles[position].lastModified() * 1000);

        holder.listTitle.setText(allFiles[position].getName());
        holder.listDate.setText(d.toString().substring(0,20));

    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class RecordingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView img;
        private TextView listTitle;
        private TextView listDate;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.listImageView);
            listTitle = itemView.findViewById(R.id.listTitle);
            listDate = itemView.findViewById(R.id.listDate);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            onitemlistclick.onClickListener(allFiles[getAdapterPosition()],getAdapterPosition());

        }
    }

    public interface onItemListClick{
        void onClickListener(File file,int position);
    }
}
