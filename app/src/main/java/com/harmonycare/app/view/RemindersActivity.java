package com.harmonycare.app.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Reminder;
import com.harmonycare.app.data.repository.ReminderRepository;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.ReminderHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Reminders Activity for managing scheduled reminders
 */
public class RemindersActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnAddReminder;
    private TextView tvEmptyState;
    private ReminderAdapter adapter;
    private ReminderRepository reminderRepository;
    private ReminderHelper reminderHelper;
    private AuthViewModel authViewModel;
    private List<Reminder> reminderList = new ArrayList<>();
    private int currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        reminderRepository = new ReminderRepository(this);
        reminderHelper = new ReminderHelper(this);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        loadReminders();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnAddReminder = findViewById(R.id.btnAddReminder);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter();
        recyclerView.setAdapter(adapter);
        
        btnAddReminder.setOnClickListener(v -> showAddReminderDialog(null));
    }
    
    private void loadReminders() {
        reminderRepository.getActiveRemindersByUser(currentUserId, new ReminderRepository.RepositoryCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                reminderList = reminders != null ? reminders : new ArrayList<>();
                adapter.notifyDataSetChanged();
                updateEmptyState();
                
                // Reschedule all reminders
                reminderHelper.scheduleAllReminders(reminderList);
            }
            
            @Override
            public void onError(Exception error) {
                showToast("Error loading reminders");
            }
        });
    }
    
    private void updateEmptyState() {
        if (reminderList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void showAddReminderDialog(Reminder reminder) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);
        
        android.widget.EditText etTitle = dialogView.findViewById(R.id.etTitle);
        android.widget.EditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextView tvDateTime = dialogView.findViewById(R.id.tvDateTime);
        Spinner spinnerRepeat = dialogView.findViewById(R.id.spinnerRepeat);
        
        final Calendar calendar = Calendar.getInstance();
        final long[] selectedTime = {System.currentTimeMillis()};
        
        boolean isEdit = reminder != null;
        if (isEdit) {
            etTitle.setText(reminder.getTitle());
            etDescription.setText(reminder.getDescription());
            calendar.setTimeInMillis(reminder.getReminderTime());
            selectedTime[0] = reminder.getReminderTime();
            updateDateTimeText(tvDateTime, selectedTime[0]);
            
            // Set repeat type
            String[] repeatTypes = getResources().getStringArray(R.array.repeat_types);
            for (int i = 0; i < repeatTypes.length; i++) {
                if (repeatTypes[i].equalsIgnoreCase(reminder.getRepeatType())) {
                    spinnerRepeat.setSelection(i);
                    break;
                }
            }
        } else {
            updateDateTimeText(tvDateTime, selectedTime[0]);
        }
        
        tvDateTime.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RemindersActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                RemindersActivity.this,
                                (view1, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    selectedTime[0] = calendar.getTimeInMillis();
                                    updateDateTimeText(tvDateTime, selectedTime[0]);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                        );
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Edit Reminder" : "Add Reminder");
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String repeatType = spinnerRepeat.getSelectedItem().toString().toLowerCase();
            
            if (title.isEmpty()) {
                ErrorHandler.showErrorDialog(this, "Invalid Title", "Please enter a title");
                return;
            }
            
            if (isEdit) {
                reminder.setTitle(title);
                reminder.setDescription(description);
                reminder.setReminderTime(selectedTime[0]);
                reminder.setRepeatType(repeatType);
                updateReminder(reminder);
            } else {
                Reminder newReminder = new Reminder(currentUserId, title, description, selectedTime[0], repeatType);
                addReminder(newReminder);
            }
        });
        builder.setNegativeButton("Cancel", null);
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> deleteReminder(reminder));
        }
        builder.show();
    }
    
    private void updateDateTimeText(TextView tv, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tv.setText(sdf.format(new Date(time)));
    }
    
    private void addReminder(Reminder reminder) {
        reminderRepository.insertReminder(reminder, new ReminderRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                reminder.setId(id.intValue());
                reminderHelper.scheduleReminder(reminder);
                showToast("Reminder added");
                loadReminders();
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(RemindersActivity.this, "Error", "Failed to add reminder");
            }
        });
    }
    
    private void updateReminder(Reminder reminder) {
        reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                reminderHelper.scheduleReminder(reminder);
                showToast("Reminder updated");
                loadReminders();
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(RemindersActivity.this, "Error", "Failed to update reminder");
            }
        });
    }
    
    private void deleteReminder(Reminder reminder) {
        ErrorHandler.showConfirmationDialog(this, "Delete Reminder", 
                "Are you sure you want to delete this reminder?",
                (dialog, which) -> {
                    reminderHelper.cancelReminder(reminder);
                    reminderRepository.deleteReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Reminder deleted");
                            loadReminders();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            ErrorHandler.showErrorDialog(RemindersActivity.this, "Error", "Failed to delete reminder");
                        }
                    });
                });
    }
    
    private void toggleReminder(Reminder reminder) {
        reminder.setActive(!reminder.isActive());
        reminderRepository.updateReminder(reminder, new ReminderRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (reminder.isActive()) {
                    reminderHelper.scheduleReminder(reminder);
                } else {
                    reminderHelper.cancelReminder(reminder);
                }
                loadReminders();
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(RemindersActivity.this, "Error", "Failed to update reminder");
            }
        });
    }
    
    private class ReminderAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reminder, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Reminder reminder = reminderList.get(position);
            holder.tvTitle.setText(reminder.getTitle());
            holder.tvDescription.setText(reminder.getDescription() != null ? reminder.getDescription() : "");
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(reminder.getReminderTime())));
            
            String repeatText = "Repeat: " + reminder.getRepeatType().substring(0, 1).toUpperCase() + 
                    reminder.getRepeatType().substring(1);
            holder.tvRepeat.setText(repeatText);
            
            if (reminder.isActive()) {
                holder.tvStatus.setText("Active");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.success_green, getTheme()));
                } else {
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.success_green));
                }
            } else {
                holder.tvStatus.setText("Inactive");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
                } else {
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.text_secondary));
                }
            }
            
            holder.btnEdit.setOnClickListener(v -> showAddReminderDialog(reminder));
            holder.btnToggle.setOnClickListener(v -> toggleReminder(reminder));
        }
        
        @Override
        public int getItemCount() {
            return reminderList.size();
        }
        
        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvTime, tvRepeat, tvStatus;
            ImageButton btnEdit, btnToggle;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvRepeat = itemView.findViewById(R.id.tvRepeat);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnToggle = itemView.findViewById(R.id.btnToggle);
            }
        }
    }
}

