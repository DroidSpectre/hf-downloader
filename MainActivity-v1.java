package com.my.hf.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private EditText searchEdit;
    private Button searchButton;
    private ListView modelsListView;
    private ProgressBar loadingProgress;
    private TextView statusText;

    private List<HuggingFaceModel> modelsList;
    private ModelsAdapter modelsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();

        modelsList = new ArrayList<HuggingFaceModel>();
        modelsAdapter = new ModelsAdapter(this, modelsList);
        modelsListView.setAdapter(modelsAdapter);
    }

    private void initializeViews() {
        searchEdit = (EditText) findViewById(R.id.searchEdit);
        searchButton = (Button) findViewById(R.id.searchButton);
        modelsListView = (ListView) findViewById(R.id.modelsListView);
        loadingProgress = (ProgressBar) findViewById(R.id.loadingProgress);
        statusText = (TextView) findViewById(R.id.statusText);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        modelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HuggingFaceModel model = modelsList.get(position);
                showModelDetails(model);
            }
        });
    }

    private void performSearch() {
        String searchQuery = searchEdit.getText().toString().trim();
        if (TextUtils.isEmpty(searchQuery)) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        new SearchModelsTask().execute(searchQuery);
    }

    private void showModelDetails(HuggingFaceModel model) {
        new ModelDetailsTask().execute(model);
    }

    private void downloadModelFile(String repoId, String filename) {
        try {
            String downloadUrl = "https://huggingface.co/" + repoId + "/resolve/main/" + filename;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setDescription("Downloading " + filename);
            request.setTitle(filename);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(this, "Download started: " + filename, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SearchModelsTask extends AsyncTask<String, Void, List<HuggingFaceModel>> {

        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(View.VISIBLE);
            statusText.setText("Searching models...");
            searchButton.setEnabled(false);
        }

        @Override
        protected List<HuggingFaceModel> doInBackground(String... params) {
            String searchQuery = params[0];
            return searchModels(searchQuery);
        }

        @Override
        protected void onPostExecute(List<HuggingFaceModel> results) {
            loadingProgress.setVisibility(View.GONE);
            searchButton.setEnabled(true);

            if (results != null && !results.isEmpty()) {
                modelsList.clear();
                modelsList.addAll(results);
                modelsAdapter.notifyDataSetChanged();
                statusText.setText("Found " + results.size() + " models");
            } else {
                statusText.setText("No models found");
            }
        }
    }

    private class ModelDetailsTask extends AsyncTask<HuggingFaceModel, Void, List<String>> {
        private HuggingFaceModel model;

        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(View.VISIBLE);
            statusText.setText("Loading model files...");
        }

        @Override
        protected List<String> doInBackground(HuggingFaceModel... params) {
            model = params[0];
            return getModelFiles(model.getId());
        }

        @Override
        protected void onPostExecute(List<String> files) {
            loadingProgress.setVisibility(View.GONE);

            if (files != null && !files.isEmpty()) {
                showFilesDialog(model, files);
            } else {
                Toast.makeText(MainActivity.this, "No files found for this model", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<HuggingFaceModel> searchModels(String query) {
        List<HuggingFaceModel> models = new ArrayList<HuggingFaceModel>();

        try {
            String apiUrl = "https://huggingface.co/api/models?search=" + query + "&limit=20";
            String jsonResponse = makeHttpRequest(apiUrl);

            if (jsonResponse != null) {
                JSONArray jsonArray = new JSONArray(jsonResponse);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject modelJson = jsonArray.getJSONObject(i);

                    String id = modelJson.optString("id", "");
                    String modelId = modelJson.optString("modelId", id);
                    int downloads = modelJson.optInt("downloads", 0);
                    String lastModified = modelJson.optString("lastModified", "");

                    JSONArray tags = modelJson.optJSONArray("tags");
                    String tagsStr = "";
                    if (tags != null) {
                        List<String> tagsList = new ArrayList<String>();
                        for (int j = 0; j < tags.length(); j++) {
                            tagsList.add(tags.getString(j));
                        }
                        tagsStr = TextUtils.join(", ", tagsList);
                    }

                    models.add(new HuggingFaceModel(modelId, downloads, lastModified, tagsStr));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return models;
    }

    private List<String> getModelFiles(String repoId) {
        List<String> files = new ArrayList<String>();

        try {
            String apiUrl = "https://huggingface.co/api/models/" + repoId;
            String jsonResponse = makeHttpRequest(apiUrl);

            if (jsonResponse != null) {
                JSONObject modelJson = new JSONObject(jsonResponse);
                JSONArray siblings = modelJson.optJSONArray("siblings");

                if (siblings != null) {
                    for (int i = 0; i < siblings.length(); i++) {
                        JSONObject file = siblings.getJSONObject(i);
                        String filename = file.optString("rfilename", "");
                        if (!TextUtils.isEmpty(filename)) {
                            files.add(filename);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return files;
    }

    private String makeHttpRequest(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "HFModelDownloader/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    private void showFilesDialog(final HuggingFaceModel model, final List<String> files) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Model Files - " + model.getId());

        String[] fileArray = files.toArray(new String[files.size()]);

        builder.setItems(fileArray, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String selectedFile = files.get(which);
                downloadModelFile(model.getId(), selectedFile);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
