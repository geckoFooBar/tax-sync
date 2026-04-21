package com.example.myapplication.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.adapters.DocumentAdapter;
import com.example.myapplication.model.DocumentItem;
import com.example.myapplication.network.ApiClient;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DocumentsFragment extends Fragment {

    private DocumentAdapter adapter;
    private View uploadProgressOverlay;
    private String currentUploadingDocName = "New Document";

    private SharedPreferences authPrefs;

    public DocumentsFragment() {}

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadToSupabaseStorage(imageUri);
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

        uploadProgressOverlay = view.findViewById(R.id.uploadProgressOverlay);
        authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);

        RecyclerView rvDocuments = view.findViewById(R.id.rvDocuments);
        ExtendedFloatingActionButton fabUploadDoc = view.findViewById(R.id.fabUploadDoc);

        rvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load the base required documents first (shows as 'Missing' by default)
        List<DocumentItem> myDocs = getBaseRequiredDocuments();

        adapter = new DocumentAdapter(myDocs, new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onUploadClicked(DocumentItem item) {
                currentUploadingDocName = item.getTitle();
                openFilePicker();
            }

            @Override
            public void onViewClicked(DocumentItem item) {
                if (item.getFileUrl() != null && !item.getFileUrl().isEmpty()) {
                    // We now pass the whole item instead of just the URL
                    openDocumentPreview(item);
                } else {
                    Toast.makeText(getContext(), "Document link is broken or syncing.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvDocuments.setAdapter(adapter);

        // Fetch cloud data using REST
        fetchUserDocuments();

        fabUploadDoc.setOnClickListener(v -> {
            currentUploadingDocName = "General Tax Receipt";
            openFilePicker();
        });
    }

    private void fetchUserDocuments() {
        String url = BuildConfig.SUPABASE_URL + "/rest/v1/uploaded_documents?select=*";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getInstance(requireContext()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to sync cloud documents", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();

                    requireActivity().runOnUiThread(() -> {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            List<DocumentItem> displayList = getBaseRequiredDocuments();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject doc = jsonArray.getJSONObject(i);
                                String docName = doc.optString("document_name");
                                int status = doc.optInt("status", 1);
                                String fileUrl = doc.optString("file_url", ""); // NEW: Grab the URL

                                boolean foundMatch = false;

                                for (DocumentItem item : displayList) {
                                    if (item.getTitle().equals(docName)) {
                                        item.setStatus(status);
                                        item.setFileUrl(fileUrl); // NEW: Attach URL to existing required doc
                                        foundMatch = true;
                                        break;
                                    }
                                }

                                if (!foundMatch) {
                                    // NEW: Pass URL to custom uploaded docs
                                    displayList.add(new DocumentItem(docName, "FILE", status, fileUrl));
                                }
                            }
                            adapter.updateData(displayList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    @android.annotation.SuppressLint("SetJavaScriptEnabled")
    private void openDocumentPreview(DocumentItem item) {
        // 1. Initialize a standard dialog (removes the full-screen theme)
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_preview);

        // 2. Make it float nicely by setting specific dimensions and a transparent background
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            // Set dimensions to 90% width and 75% height of the screen
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);
            window.setLayout(width, height);

            // Make the actual window square transparent so our rounded CardView corners show
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

            // Ensure the background dims slightly to focus the user's attention
            window.setDimAmount(0.6f);
        }

        android.widget.ImageButton btnClose = dialog.findViewById(R.id.btnClosePreview);
        android.webkit.WebView webView = dialog.findViewById(R.id.webViewPreview);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new android.webkit.WebViewClient());

        String targetUrl = item.getFileUrl();

        if (item.getFileType() != null && item.getFileType().equalsIgnoreCase("PDF")) {
            try {
                String pdfUrl = "https://docs.google.com/gview?embedded=true&url=" +
                        java.net.URLEncoder.encode(targetUrl, "UTF-8");
                webView.loadUrl(pdfUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String htmlData = "<html>" +
                    "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=5, user-scalable=yes\"></head>" +
                    "<body style=\"margin: 0; padding: 0; background-color: #F3F4F6; display: flex; justify-content: center; align-items: center; height: 100vh;\">" +
                    "<img src=\"" + targetUrl + "\" style=\"max-width: 100%; max-height: 100%; object-fit: contain;\" />" +
                    "</body></html>";

            webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
        }

        dialog.show();
    }

    private void uploadToSupabaseStorage(Uri fileUri) {
        String accessToken = authPrefs.getString("accessToken", null);
        String userEmail = authPrefs.getString("userEmail", "unknown_user");

        if (accessToken == null) {
            Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String SUPABASE_URL = BuildConfig.SUPABASE_URL;
        String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
        String BUCKET_NAME = BuildConfig.SUPABASE_BUCKET_NAME;

        uploadProgressOverlay.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
                byte[] fileBytes;
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while (true) {
                    assert inputStream != null;
                    if ((nRead = inputStream.read(data, 0, data.length)) == -1) break;
                    buffer.write(data, 0, nRead);
                }
                fileBytes = buffer.toByteArray();
                inputStream.close();

                String mimeType = requireContext().getContentResolver().getType(fileUri);
                if (mimeType == null) mimeType = "application/octet-stream";

                String extension = mimeType.contains("pdf") ? ".pdf" : ".jpg";
                // Using email as the storage folder to uniquely identify user files
                String filePath = userEmail + "/" + currentUploadingDocName.replace(" ", "_")
                        + "_" + System.currentTimeMillis() + extension;

                // We create a fresh client here because our ApiClient interceptor forces "application/json" Content-Type
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(fileBytes, MediaType.parse(mimeType));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + filePath)
                        .post(body)

                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", mimeType)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/"
                            + BUCKET_NAME + "/" + filePath;

                    requireActivity().runOnUiThread(() -> saveDocumentDataToDatabase(currentUploadingDocName, publicUrl));
                } else {
                    String finalError = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        uploadProgressOverlay.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Upload failed: " + finalError, Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    uploadProgressOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveDocumentDataToDatabase(String docName, String fileUrl) {
        String url = BuildConfig.SUPABASE_URL + "/rest/v1/uploaded_documents";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("document_name", docName);
            jsonBody.put("file_url", fileUrl);
            jsonBody.put("status", 1); // 1 = In Review
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        ApiClient.getInstance(requireContext()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    uploadProgressOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "No error body";

                requireActivity().runOnUiThread(() -> {
                    uploadProgressOverlay.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Document Secured in Vault!", Toast.LENGTH_SHORT).show();
                        // This safely refreshes the list once, because fetchUserDocuments() no longer loops back here
                        fetchUserDocuments();
                    } else {
                        Toast.makeText(getContext(), "DB Error: " + responseData, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private List<DocumentItem> getBaseRequiredDocuments() {
        List<DocumentItem> list = new ArrayList<>();
        list.add(new DocumentItem("PAN Card", "JPG", 0));
        list.add(new DocumentItem("Aadhar Card", "PDF", 0));
        list.add(new DocumentItem("Current Address Proof", "PDF", 0));
        return list;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Document"));
    }
}