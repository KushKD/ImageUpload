package imageupload.hp.nishikanttech.com.imageupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kuush on 8/7/2016.
 */
public class ImageUpload extends Activity{

    ProgressDialog prgDialog;
    private ImageView imgPreview;
    private Button back;
    private String filePath = null;
    Bitmap bitmap;

    Photo photo_Details = new Photo();

    URL url_;
    HttpURLConnection conn_;
    StringBuilder sb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageupload);

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        back = (Button) findViewById(R.id.button2);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ImageUpload.this, MainActivity.class);
                startActivity(i);
            }
        });

        // Receiving the data from previous activity
        Intent i = getIntent();
        // image or video path that is captured in previous activity
        // boolean flag to identify the media type, image or video
        //boolean isImage = i.getBooleanExtra("isImage", true);
        if(MainActivity.flag==1) {
            filePath = i.getStringExtra("filePath");
        }else if(MainActivity.flag==2) {
            filePath = i.getStringExtra("picturePath");
        }else{
            Toast.makeText(this,"Flag isn't getting into ImageUpload.java!!!",Toast.LENGTH_LONG).show();
        }

        if (filePath != null) {
            // Displaying the image or video on the screen
            Toast.makeText(this,"File path is : "+filePath,Toast.LENGTH_LONG).show();
            previewMedia();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia() {
        // Checking whether captured media is image or video
        //imgPreview.setVisibility(View.VISIBLE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        // down sizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Toast.makeText(this,filePath,Toast.LENGTH_LONG).show();
        imgPreview.setImageBitmap(bitmap);
    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (filePath != null && !filePath.isEmpty()) {
            Toast.makeText(this,"Inside upload image",Toast.LENGTH_LONG).show();
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            String fileNameSegments[] = filePath.split("/");
            String fileName = fileNameSegments[fileNameSegments.length - 1];

            // Put file name in Async Http Post Param which will used in Php web app
            photo_Details.setPhotoname(fileName);
            Log.e("Params",photo_Details.getPhotoname().toString());
            //photo_Details.setPhotobase64encode(enco);

            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, Photo>() {

            String encodedString = null;
            protected void onPreExecute() {

            };

            @Override
            protected Photo doInBackground(Void... params) {

                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(filePath,options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                photo_Details.setPhotobase64encode(encodedString);
                return photo_Details;
            }

            @Override
            protected void onPostExecute(Photo msg) {
               // prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
               // params.put("image", encodedString);
                Log.e("Image in String msg: ",photo_Details.getPhotobase64encode());
                Log.e("Image Name msg: ",photo_Details.getPhotoname());
                prgDialog.dismiss();
                // Trigger Image upload
                triggerImageUpload(msg);
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload(Photo photo_Details) {
        Upload_Photo UP  = new Upload_Photo();
        UP.execute(photo_Details);
    }

class Upload_Photo extends AsyncTask<Object,String,String>{

    JSONStringer userJson = null;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prgDialog.setMessage("Sending Photo to Server");
    }

    @Override
    protected String doInBackground(Object... objects) {
        Photo photo_server = (Photo)objects[0];
        Log.e("Image in String: ",photo_Details.getPhotobase64encode());
        Log.e("Image Name: ",photo_Details.getPhotoname());

        try {
            url_ =new URL("http://10.0.2.2"+"/getConfirmParkOutStatus_JSON");
            conn_ = (HttpURLConnection)url_.openConnection();
            conn_.setDoOutput(true);
            conn_.setRequestMethod("POST");
            conn_.setUseCaches(false);
            conn_.setConnectTimeout(10000);
            conn_.setReadTimeout(10000);
            conn_.setRequestProperty("Content-Type", "application/json");
            conn_.connect();

            userJson = new JSONStringer()
                    .object().key("Photo")
                    .object()
                    .key("photoName").value(photo_server.getPhotoname())
                    .key("photoasBase64").value(photo_server.getPhotobase64encode())
                    .endObject()
                    .endObject();


            System.out.println(userJson.toString());
            Log.e("Object",userJson.toString());
            OutputStreamWriter out = new OutputStreamWriter(conn_.getOutputStream());
            out.write(userJson.toString());
            out.close();

            try{
                int HttpResult =conn_.getResponseCode();
                if(HttpResult ==HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn_.getInputStream(),"utf-8"));
                    String line = null;
                    sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println(sb.toString());

                }else{
                    System.out.println("Server Connection failed.");
                }

            } catch(Exception e){
                return "Server Connection failed.";
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally{
            if(conn_!=null)
                conn_.disconnect();
        }
        return sb.toString();

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        prgDialog.dismiss();
    }
}

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
            photo_Details = null;

        }else{
            photo_Details = null;
        }
    }

}
