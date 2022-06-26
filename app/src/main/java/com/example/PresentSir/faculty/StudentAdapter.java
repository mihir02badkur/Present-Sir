package com.raina.PresentSir.faculty;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.raina.PresentSir.R;

public class StudentAdapter extends FirestoreRecyclerAdapter<Student, StudentAdapter.StudentViewHolder> {
    Context context;
    String[] cardColors = {"#FFF59D", "#8D6E63", "#B9F6CA", "#D1C4E9", "#F8BBD0"};
    int[] cardBackground = {R.drawable.student1, R.drawable.student2, R.drawable.student3,
            R.drawable.student4, R.drawable.student5};

    public StudentAdapter(@NonNull FirestoreRecyclerOptions<Student> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_card, parent, false);
        return new StudentViewHolder(view);
    }

    // Set text from the data fetched
    @Override
    protected void onBindViewHolder(@NonNull StudentViewHolder holder, int position, @NonNull Student model) {
        holder.name.setText(model.getName());
        holder.email.setText(model.getEmail());
        holder.rollNo.setText(model.getRollNumber());
        holder.cardView.setCardBackgroundColor(Color.parseColor(cardColors[(position) % 5]));
        holder.image.setBackgroundResource(cardBackground[position % 5]);
    }

    // ViewHolder class to get views
    class StudentViewHolder
            extends RecyclerView.ViewHolder {
        TextView name, email, rollNo;
        CardView cardView;
        ImageView image;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.studentName);
            email = itemView.findViewById(R.id.studentEmail);
            rollNo = itemView.findViewById(R.id.studentRollNo);
            cardView = itemView.findViewById(R.id.cardView);
            image = itemView.findViewById(R.id.cardImage);

            cardView.setOnClickListener(view -> {
                Intent intent = new Intent(context, AttendanceFacultyActivity.class);
                intent.putExtra("email", email.getText().toString());
                context.startActivity(intent);
            });
        }
    }
}
