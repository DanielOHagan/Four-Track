package com.example.daniel.fourtrack.track;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.daniel.fourtrack.R;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final ArrayList<Track> mTrackList;
    private OnTrackItemClickListener mListener;

    public interface OnTrackItemClickListener {
        //list of listeners that will be available to the list item's views
        void onPlayToggle(int position, boolean isChecked);
        void onRecordToggle(int position, boolean isChecked);
        void onSettingsClick(int position);
        void onSeekBarChange(int position, SeekBar seekBar, int progress, boolean fromUser);
        void onSeekBarTouchStart(int position, SeekBar seekBar);
        void onSeekBarTouchEnd(int position, SeekBar seekBar);
    }

    public void setOnTrackItemClickListener (OnTrackItemClickListener listener) {
        mListener = listener;
    }

    public TrackAdapter(ArrayList<Track> mTrackList) {
        this.mTrackList = mTrackList;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.track_list_item, parent, false
        );
        return new TrackViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        //when the child item is bound to the recycler view
        //set some of the members of that particular instance of the Track class
        Track currentItem = mTrackList.get(position);

        holder.mTrackTitleTextView.setText(currentItem.getTrackName());
        currentItem.setPlayToggleBtn(holder.mTrackPlayToggleBtn);
        currentItem.setRecordToggleBtn(holder.mTrackRecordToggleBtn);
        currentItem.setSeekBar(holder.mSeekBar);
        currentItem.setTrackCurrentTimeTextView(holder.mTrackCurrentTimeTextView);
        currentItem.setTrackDurationTextView(holder.mTrackDurationTextView);
        currentItem.setTrackTitleTextView(holder.mTrackTitleTextView);
    }

    @Override
    public int getItemCount() {
        return mTrackList.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTrackTitleTextView;
        public final TextView mTrackCurrentTimeTextView;
        public final TextView mTrackDurationTextView;
        public final ToggleButton mTrackPlayToggleBtn;
        public final ToggleButton mTrackRecordToggleBtn;
        public final Button mTrackSettingsBtn;
        public final SeekBar mSeekBar;

        public TrackViewHolder(final View itemView, final OnTrackItemClickListener listener) {
            super(itemView);
            mTrackTitleTextView = itemView.findViewById(R.id.trackNameTextView);
            mTrackPlayToggleBtn = itemView.findViewById(R.id.trackPlayToggleBtn);
            mTrackRecordToggleBtn = itemView.findViewById(R.id.trackRecordToggleBtn);
            mTrackSettingsBtn = itemView.findViewById(R.id.trackSettingsBtn);
            mSeekBar = itemView.findViewById(R.id.trackSeekBar);
            mTrackCurrentTimeTextView = itemView.findViewById(R.id.trackCurrentTime);
            mTrackDurationTextView = itemView.findViewById(R.id.trackDurationTime);

            //assign listeners to each view using the OnTrackItemClickListener interface
            mTrackPlayToggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onPlayToggle(position, mTrackPlayToggleBtn.isChecked());
                        }
                    }
                }
            });

            mTrackRecordToggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onRecordToggle(position, mTrackRecordToggleBtn.isChecked());
                        }
                    }
                }
            });

            mTrackSettingsBtn.setOnClickListener(new View.OnClickListener() {
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

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSeekBarChange(position, seekBar, progress, fromUser);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSeekBarTouchStart(position, seekBar);
                        }
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSeekBarTouchEnd(position, seekBar);
                        }
                    }
                }
            });
        }
    }
}