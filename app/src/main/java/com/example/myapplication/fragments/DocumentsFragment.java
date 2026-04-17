package com.example.myapplication.fragments;

import com.example.myapplication.BuildConfig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.DocumentAdapter;
import com.example.myapplication.model.DocumentItem;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DocumentsFragment extends Fragment {

    private RecyclerView rvDocuments;
    private DocumentAdapter adapter;
    private ExtendedFloatingActionButton fabUploadDoc;

    // Firebase Tools
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    // To remember what document name they are currently uploading
    private String currentUploadingDocName = "New Document";

    public DocumentsFragment() {}
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadToSupabase(imageUri);
                    }
                }
            }
    );



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        rvDocuments = view.findViewById(R.id.rvDocuments);
        fabUploadDoc = view.findViewById(R.id.fabUploadDoc);

        rvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load the base required documents first (shows as 'Missing' by default)
        List<DocumentItem> myDocs = getBaseRequiredDocuments();

        adapter = new DocumentAdapter(myDocs, item -> {
            currentUploadingDocName = item.getTitle();
            openFilePicker();
        });
        rvDocuments.setAdapter(adapter);

        // NEW: Ask Firebase what the user has ACTUALLY uploaded!
        fetchUserDocuments();

        fabUploadDoc.setOnClickListener(v -> {
            currentUploadingDocName = "General Tax Receipt";
            openFilePicker();
        });
    }

    private void fetchUserDocuments() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        firestore.collection("uploaded_documents")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 1. Get a fresh copy of the required baseline
                    List<DocumentItem> displayList = getBaseRequiredDocuments();

                    // 2. Loop through the documents fetched from the cloud
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docName = doc.getString("documentName");
                        Long statusLong = doc.getLong("status");
                        int status = (statusLong != null) ? statusLong.intValue() : 1; // Default to 'In Review'

                        boolean foundMatch = false;

                        // 3. If the cloud document matches a required document, update its status!
                        for (DocumentItem item : displayList) {
                            if (item.getTitle().equals(docName)) {
                                item.setStatus(status); // Changes it from Red (0) to Amber (1) or Green (2)
                                foundMatch = true;
                                break;
                            }
                        }

                        // 4. If they uploaded a custom file (like "General Tax Receipt"), add it as a new row
                        if (!foundMatch) {
                            displayList.add(new DocumentItem(docName, "IMG", status));
                        }
                    }

                    // 5. Push the merged list to the screen
                    adapter.updateData(displayList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to sync cloud documents", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadToSupabase(Uri fileUri) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String SUPABASE_URL = BuildConfig.SUPABASE_URL;
        String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
        String BUCKET_NAME = BuildConfig.SUPABASE_BUCKET_NAME;

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Securing Document");
        progressDialog.setMessage("Uploading to Cloud Vault...");
        progressDialog.show();

        new Thread(() -> {
            try {
                // Read the file bytes
                InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
                byte[] fileBytes;
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                fileBytes = buffer.toByteArray();
                inputStream.close();

                // Determine MIME type
                String mimeType = getContext().getContentResolver().getType(fileUri);
                if (mimeType == null) mimeType = "application/octet-stream";

                // Build the file path: userId/docName_timestamp.ext
                String extension = mimeType.contains("pdf") ? ".pdf" : ".jpg";
                String filePath = userId + "/" + currentUploadingDocName.replace(" ", "_")
                        + "_" + System.currentTimeMillis() + extension;

                // Upload via Supabase Storage REST API
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(fileBytes, MediaType.parse(mimeType));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", mimeType)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // Build the public URL
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/"
                            + BUCKET_NAME + "/" + filePath;

                    // Save URL to Firestore (this part stays the same!)
                    getActivity().runOnUiThread(() ->
                            saveDocumentDataToFirestore(userId, currentUploadingDocName, publicUrl, progressDialog)
                    );
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "no body";
                    String finalError = errorBody;
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Upload failed: " + finalError, Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    private List<DocumentItem> getBaseRequiredDocuments() {
        List<DocumentItem> list = new ArrayList<>();
        list.add(new DocumentItem("PAN Card", "JPG", 0)); // 0 = Missing
        list.add(new DocumentItem("Aadhar Card", "PDF", 0));
        list.add(new DocumentItem("Current Address Proof", "REQ", 0));
        return list;
    }

    /*
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading Document");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference()
                .child("users")
                .child(userId)
                .child("documents")
                .child(fileName);

        storageRef.putFile(imageUri)  // putFile() is simpler & handles URIs directly
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return storageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String downloadUrl = task.getResult().toString();
                        saveDocumentDataToFirestore(userId, currentUploadingDocName, downloadUrl, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Upload failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // ← action was missing
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Document")); // ← this line was missing
    }

    private void saveDocumentDataToFirestore(String userId, String docName, String fileUrl, ProgressDialog dialog) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("userId", userId);
        docData.put("documentName", docName);
        docData.put("fileUrl", fileUrl);
        docData.put("uploadTimestamp", System.currentTimeMillis());
        docData.put("status", 1); // 1 = In Review

        firestore.collection("uploaded_documents")
                .add(docData)
                .addOnSuccessListener(documentReference -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Document Secured in Vault!", Toast.LENGTH_SHORT).show();

                    fetchUserDocuments();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}