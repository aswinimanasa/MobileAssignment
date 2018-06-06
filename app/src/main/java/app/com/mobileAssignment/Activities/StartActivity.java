package app.com.mobileAssignment.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import app.com.mobileAssignment.Adapters.ListAdapter;
import app.com.mobileAssignment.Models.Model;
import app.com.mobileAssignment.R;

/**
 * Created by aswinimanasa
 */

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private AmazonS3 amazonS3;
    private TransferUtility transferUtility;
    private String imagePath;
    private Button camera, gallery;
    private ListView listView;
    private ArrayList<File> imageArrayList;
    private File storageDir;
    ListAdapter adapter;
    private String awsBucketName = "testsampleapp"; //Bucket Name
    private String timeStamp;
    private String identityPoolId = "e99a969f-d6aa-4158-ba97-0454ba45e7fa"; //ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);
        listView = (ListView) findViewById(R.id.listView);
        imageArrayList = new ArrayList<>();
        adapter = new ListAdapter(StartActivity.this, imageArrayList);
        listView.setAdapter(adapter);
        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        credentialsProvider(); // callback method to call credentialsProvider method.
        setTransferUtility(); // callback method to call the setTransferUtility method
        new DownloadImages().execute();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Model.requestType.camera.getValue());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Model.requestType.camera.getValue() && resultCode == RESULT_OK) {
            try {
                createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == Model.requestType.gallery.getValue()) {
                String[] imagesPath = data.getStringExtra("data").split("\\|");
                try {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < imagesPath.length; i++) {

                    uploadImages(createImageFileName(), imagesPath[i]);
                }
            }
        }
    }

    private void createImageFile() throws IOException {
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String imageFileName = createImageFileName();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imagePath = image.getAbsolutePath();
        Log.d("IMAGE FILE", imagePath);
        uploadImages(imageFileName, imagePath);
    }

    private String createImageFileName(){
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return  "JPEG_" + timeStamp + "_";
    }

    private void credentialsProvider() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(), "us-east-1:" + identityPoolId,
                Regions.US_EAST_1);
        setAmazonS3Client(credentialsProvider);
    }



    /**
     * Create a AmazonS3Client constructor and pass the credentialsProvider.
     *
     * @param credentialsProvider
     */
    private void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        amazonS3 = new AmazonS3Client(credentialsProvider); // Create an S3 client
        amazonS3.setRegion(Region.getRegion(Regions.US_EAST_1));// Set the region of your S3 bucket
    }

    private void setTransferUtility() {
        transferUtility = new TransferUtility(amazonS3, getApplicationContext());
    }

    private void uploadImages(final String imageFileName, String imagePath) {
        TransferObserver transferObserver = transferUtility.upload(
                awsBucketName,     /* The bucket to upload to */
                imageFileName,/* The key for the uploaded object */
                new File(imagePath),/* The file where the data to upload exists */
                CannedAccessControlList.PublicRead

        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                downloadImages(imageFileName);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    /**
     * This method is used to Download the file to S3 by using transferUtility class
     **/
    private void downloadImages(final String fileName) {
        TransferObserver transferObserver = transferUtility.download(
                awsBucketName,     /* The bucket to download from */
                fileName,    /* The key for the object to download */
                new File(storageDir + "/" + fileName + ".jpg")      /* The file to download the object to */
        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                imageArrayList.add(new File(storageDir + "/" + fileName + ".jpg"));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                dispatchTakePictureIntent();
                break;
            case R.id.gallery:
                Intent intent = new Intent(StartActivity.this, CustomGalleryActivity.class);
                startActivityForResult(intent, Model.requestType.gallery.getValue());
                break;
        }

    }

    /**
     * This async task queries S3 for all files in the given bucket so that they
     * can be displayed on the screen
     */
    private class DownloadImages extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            for (S3ObjectSummary objectList : amazonS3.listObjects(awsBucketName).getObjectSummaries()) {
                downloadImages(objectList.getKey());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(StartActivity.this, "Loading", "Please Wait");
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }
}
