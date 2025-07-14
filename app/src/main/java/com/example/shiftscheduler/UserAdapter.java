package com.example.shiftscheduler;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.nameTextView.setText(model.getName());
        holder.salaryTextView.setText("Salary: " + model.getSalary());

        holder.removeButton.setOnClickListener(v -> {
            String docId = getSnapshots().getSnapshot(position).getId();
            db.collection("users").document(docId)
                    .delete()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(holder.itemView.getContext(), "User removed", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.updateSalaryButton.setOnClickListener(v -> {

            EditText passwordInput = new EditText(holder.itemView.getContext());
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInput.setHint("Enter your admin password");

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Re-authenticate")
                    .setView(passwordInput)
                    .setPositiveButton("Authenticate", (dialog, which) -> {
                        String adminPassword = passwordInput.getText().toString().trim();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null && !adminPassword.isEmpty()) {
                            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), adminPassword);
                            currentUser.reauthenticate(credential)
                                    .addOnSuccessListener(unused -> {
                                        // Step 2: Prompt for new salary after successful auth
                                        EditText salaryInput = new EditText(holder.itemView.getContext());
                                        salaryInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        salaryInput.setHint("Enter new salary");

                                        new AlertDialog.Builder(holder.itemView.getContext())
                                                .setTitle("Update Salary")
                                                .setView(salaryInput)
                                                .setPositiveButton("Update", (d, w) -> {
                                                    String salaryStr = salaryInput.getText().toString().trim();
                                                    if (!salaryStr.isEmpty()) {
                                                        long newSalary = Long.parseLong(salaryStr);
                                                        String docId = getSnapshots().getSnapshot(position).getId();
                                                        db.collection("users").document(docId)
                                                                .update("salary", newSalary)
                                                                .addOnSuccessListener(aVoid ->
                                                                        Toast.makeText(holder.itemView.getContext(), "Salary updated", Toast.LENGTH_SHORT).show())
                                                                .addOnFailureListener(e ->
                                                                        Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                    } else {
                                                        Toast.makeText(holder.itemView.getContext(), "Please enter a valid salary.", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null)
                                                .show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(holder.itemView.getContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.makeAdminButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Confirm Admin Promotion")
                    .setMessage("Are you sure you want to make this user an admin? You will need to confirm with your credentials.")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Prompt admin for their password for re-authentication
                        EditText input = new EditText(holder.itemView.getContext());
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        input.setHint("Enter your admin password");

                        new AlertDialog.Builder(holder.itemView.getContext())
                                .setTitle("Re-authenticate")
                                .setView(input)
                                .setPositiveButton("Authenticate", (d, w) -> {
                                    String adminPassword = input.getText().toString().trim();
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();

                                    if (currentUser != null && !adminPassword.isEmpty()) {
                                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), adminPassword);
                                        ((com.google.firebase.auth.FirebaseUser) currentUser).reauthenticate(credential)
                                                .addOnSuccessListener(unused -> {
                                                    String docId = getSnapshots().getSnapshot(position).getId();
                                                    db.collection("users").document(docId)
                                                            .update("admin", true)
                                                            .addOnSuccessListener(u ->
                                                                    Toast.makeText(holder.itemView.getContext(), "User promoted to admin.", Toast.LENGTH_SHORT).show())
                                                            .addOnFailureListener(e ->
                                                                    Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(holder.itemView.getContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                    } else {
                                        Toast.makeText(holder.itemView.getContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(v);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, salaryTextView;
        Button removeButton, updateSalaryButton;
        Button makeAdminButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            salaryTextView = itemView.findViewById(R.id.textViewSalary);
            removeButton = itemView.findViewById(R.id.buttonRemove);
            updateSalaryButton = itemView.findViewById(R.id.buttonUpdateSalary);
            makeAdminButton = itemView.findViewById(R.id.buttonMakeAdmin);
        }
    }
}
