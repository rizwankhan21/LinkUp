package com.azwa.vibesent;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewholder> {
    MainActivity mainActivity;
    ArrayList<Users> usersArrayList;

    public UserAdapter(MainActivity mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public UserAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.viewholder holder, int position) {
        Users users = usersArrayList.get(position);

        // Hide current user's own entry (optional)
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(users.getUserId())) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // Collapse height too
            return;
        }

        //  Show last message if available, otherwise show status
        if (users.getLastMessage() != null && !users.getLastMessage().isEmpty()) {
            holder.userstatus.setText(users.getLastMessage());
        } else {
            holder.userstatus.setText(users.getStatus());
        }


        holder.username.setText(users.getUserName());
        Picasso.get().load(users.getProfilepic()).into(holder.userimg);

        // On user click, open chat screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, chatWin.class);
            intent.putExtra("nameeee", users.getUserName());
            intent.putExtra("reciverImg", users.getProfilepic());
            intent.putExtra("uid", users.getUserId());
            mainActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username;
        TextView userstatus;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}
