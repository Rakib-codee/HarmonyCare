package com.harmonycare.app.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.EmergencyContact;
import com.harmonycare.app.data.repository.EmergencyContactRepository;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.ValidationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Emergency Contacts Management Activity
 */
public class EmergencyContactsActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnAddContact;
    private TextView tvEmptyState;
    private EmergencyContactAdapter adapter;
    private EmergencyContactRepository contactRepository;
    private AuthViewModel authViewModel;
    private List<EmergencyContact> contactList = new ArrayList<>();
    private int currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        contactRepository = new EmergencyContactRepository(this);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        loadContacts();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnAddContact = findViewById(R.id.btnAddContact);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyContactAdapter();
        recyclerView.setAdapter(adapter);
        
        btnAddContact.setOnClickListener(v -> showAddContactDialog(null));
    }
    
    private void loadContacts() {
        contactRepository.getContactsByUser(currentUserId, new EmergencyContactRepository.RepositoryCallback<List<EmergencyContact>>() {
            @Override
            public void onSuccess(List<EmergencyContact> contacts) {
                contactList = contacts != null ? contacts : new ArrayList<>();
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }
            
            @Override
            public void onError(Exception error) {
                showToast("Error loading contacts");
            }
        });
    }
    
    private void updateEmptyState() {
        if (contactList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void showAddContactDialog(EmergencyContact contact) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        
        android.widget.EditText etName = dialogView.findViewById(R.id.etName);
        android.widget.EditText etPhone = dialogView.findViewById(R.id.etPhone);
        android.widget.EditText etRelationship = dialogView.findViewById(R.id.etRelationship);
        android.widget.CheckBox cbPrimary = dialogView.findViewById(R.id.cbPrimary);
        
        boolean isEdit = contact != null;
        if (isEdit) {
            etName.setText(contact.getName());
            etPhone.setText(contact.getPhoneNumber());
            etRelationship.setText(contact.getRelationship());
            cbPrimary.setChecked(contact.isPrimary());
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Edit Contact" : "Add Emergency Contact");
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();
            boolean isPrimary = cbPrimary.isChecked();
            
            if (!ValidationHelper.isValidName(name)) {
                ErrorHandler.showErrorDialog(this, "Invalid Name", "Please enter a valid name");
                return;
            }
            
            ValidationHelper.ValidationResult phoneResult = ValidationHelper.validateContact(phone);
            if (!phoneResult.isValid()) {
                ErrorHandler.showErrorDialog(this, "Invalid Phone", phoneResult.getMessage());
                return;
            }
            
            if (isEdit) {
                contact.setName(name);
                contact.setPhoneNumber(phone);
                contact.setRelationship(relationship);
                contact.setPrimary(isPrimary);
                updateContact(contact, isPrimary);
            } else {
                EmergencyContact newContact = new EmergencyContact(currentUserId, name, phone, relationship);
                newContact.setPrimary(isPrimary);
                addContact(newContact, isPrimary);
            }
        });
        builder.setNegativeButton("Cancel", null);
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> deleteContact(contact));
        }
        builder.show();
    }
    
    private void addContact(EmergencyContact contact, boolean isPrimary) {
        contactRepository.insertContact(contact, new EmergencyContactRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                if (isPrimary) {
                    contactRepository.setPrimaryContact(currentUserId, id.intValue(), new EmergencyContactRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Contact added successfully");
                            loadContacts();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            showToast("Contact added but failed to set as primary");
                            loadContacts();
                        }
                    });
                } else {
                    showToast("Contact added successfully");
                    loadContacts();
                }
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(EmergencyContactsActivity.this, "Error", "Failed to add contact");
            }
        });
    }
    
    private void updateContact(EmergencyContact contact, boolean isPrimary) {
        contactRepository.updateContact(contact, new EmergencyContactRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isPrimary) {
                    contactRepository.setPrimaryContact(currentUserId, contact.getId(), new EmergencyContactRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Contact updated successfully");
                            loadContacts();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            showToast("Contact updated but failed to set as primary");
                            loadContacts();
                        }
                    });
                } else {
                    showToast("Contact updated successfully");
                    loadContacts();
                }
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(EmergencyContactsActivity.this, "Error", "Failed to update contact");
            }
        });
    }
    
    private void deleteContact(EmergencyContact contact) {
        ErrorHandler.showConfirmationDialog(this, "Delete Contact", 
                "Are you sure you want to delete " + contact.getName() + "?",
                (dialog, which) -> {
                    contactRepository.deleteContact(contact, new EmergencyContactRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Contact deleted");
                            loadContacts();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            ErrorHandler.showErrorDialog(EmergencyContactsActivity.this, "Error", "Failed to delete contact");
                        }
                    });
                });
    }
    
    private void callContact(EmergencyContact contact) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
        startActivity(intent);
    }
    
    private class EmergencyContactAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emergency_contact, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            EmergencyContact contact = contactList.get(position);
            holder.tvName.setText(contact.getName());
            holder.tvPhone.setText(contact.getPhoneNumber());
            holder.tvRelationship.setText(contact.getRelationship() != null ? contact.getRelationship() : "");
            
            if (contact.isPrimary()) {
                holder.tvPrimary.setVisibility(View.VISIBLE);
            } else {
                holder.tvPrimary.setVisibility(View.GONE);
            }
            
            holder.btnEdit.setOnClickListener(v -> showAddContactDialog(contact));
            holder.btnCall.setOnClickListener(v -> callContact(contact));
        }
        
        @Override
        public int getItemCount() {
            return contactList.size();
        }
        
        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvRelationship, tvPrimary;
            ImageButton btnEdit, btnCall;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvRelationship = itemView.findViewById(R.id.tvRelationship);
                tvPrimary = itemView.findViewById(R.id.tvPrimary);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnCall = itemView.findViewById(R.id.btnCall);
            }
        }
    }
}

