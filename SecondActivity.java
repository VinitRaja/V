package mynewproject.vinit.com.logindemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class SecondActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView resultImageView;
    private Bitmap capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
            // Get reference to the Button
            Button myButton = findViewById(R.id.myButton);
        resultImageView = new ImageView(this);
        // Optionally add resultImageView to your layout
        // ((LinearLayout) findViewById(R.id.rootLayout)).addView(resultImageView);
            // Set OnClickListener to handle click event
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                // Start camera intent
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            // Upload image to backend
            new UploadImageTask().execute();
        }
    }

    private class UploadImageTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                JSONObject json = new JSONObject();
                json.put("image", encodedImage);

                // Generate a random server IP for demonstration (not for production use)
                String[] ips = {"192.168.1.10", "192.168.1.20", "192.168.1.30"};
                String randomIp = ips[(int)(Math.random() * ips.length)];
                URL url = new URL("http://" + randomIp + ":5000/face_match");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(json.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                return responseCode == 200;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean match) {
            if (match) {
                // Show green signal
                resultImageView.setBackgroundColor(0xFF00FF00); // Green
                Toast.makeText(SecondActivity.this, "Face Matched!", Toast.LENGTH_SHORT).show();
            } else {
                // Show red signal
                resultImageView.setBackgroundColor(0xFFFF0000); // Red
                Toast.makeText(SecondActivity.this, "Face Not Matched!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

