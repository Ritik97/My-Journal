package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournal.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl;
        holder.title.setText(journal.getTitle());
        holder.thought.setText(journal.getThought());
        holder.name.setText(journal.getUsername());
        imageUrl = journal.getImageUrl();

        // https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);
        holder.dateAdded.setText(timeAgo);

        /*
        Use Picasso library to download and show image
        https://square.github.io/picasso/
         */
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.deer_forest_fog)
                .fit()
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView
                thought,
                title,
                dateAdded,
                name;
        public ImageButton shareButton;
        public ImageView image;
        String userId;
        String userName;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;
            title = itemView.findViewById(R.id.journal_title_list);
            thought = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            name = itemView.findViewById(R.id.journal_user_name);
            shareButton = itemView.findViewById(R.id.journal_share_button);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }
}
