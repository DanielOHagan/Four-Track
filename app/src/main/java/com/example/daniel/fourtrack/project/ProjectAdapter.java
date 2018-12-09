package com.example.daniel.fourtrack.project;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.daniel.fourtrack.R;

import java.util.ArrayList;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final ArrayList<Project> mProjectList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onSettingsClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTitleTextView;
        public final Button mSettingsBtn;

        public ProjectViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.projectItemTitle);
            mSettingsBtn = itemView.findViewById(R.id.projectItemSettingsBtn);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            mSettingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSettingsClick(position);
                        }
                    }
                }
            });
        }
    }

    public ProjectAdapter(ArrayList<Project> projectList) {
        mProjectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.project_list_item, parent, false
        );
        return new ProjectViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project currentItem = mProjectList.get(position);

        currentItem.setProjectTitleTextView(holder.mTitleTextView);
        currentItem.getProjectTitleTextView().setText(currentItem.getProjectName());
    }

    @Override
    public int getItemCount() {
        return mProjectList.size();
    }
}