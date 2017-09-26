package com.zxaryo.projects.qr_ecsa_zx;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import android.view.Menu;
        import android.view.MenuInflater;

        import com.google.zxing.integration.android.IntentIntegrator;
        import com.google.zxing.integration.android.IntentResult;

        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.net.URLEncoder;
        import java.util.Iterator;

        import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String scannedData,result0s="",result1s="",result2s="",message;

    Button scanBtn;
    TextView result0, result1, result2, cstatus;

     @Override
     public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu,menu);
        return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){
             case R.id.about_:
                 Toast.makeText(getApplicationContext(),"such wow",Toast.LENGTH_LONG).show();
                 setContentView(R.layout.about);
                 return true;
             default :
                 return super.onOptionsItemSelected(item);
         }
     }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity activity =this;
        scanBtn = (Button)findViewById(R.id.scan_btn);
        result0 = (TextView) findViewById(R.id.result0);
        result1 = (TextView) findViewById(R.id.result1);
        result2 = (TextView) findViewById(R.id.result2);
        cstatus = (TextView) findViewById(R.id.cstatus);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan ECSA ID");
                integrator.setBeepEnabled(false);
                integrator.setCameraId(0);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null) {
            scannedData = result.getContents();
            if (scannedData != null) {
                // Here we need to handle scanned data...
                result0s=scannedData;
                result2.post(new Runnable() {
                    @Override
                    public void run() {
                        result2.setText(result2s);
                    }
                });
                result1.post(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(result1s);
                        result2s=result1s;
                    }
                });
                result0.post(new Runnable() {
                    @Override
                    public void run() {
                        result0.setText(result0s);
                        result1s=result0s;
                    }
                });
                new SendRequest().execute();


            }else {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                //Enter script URL Here
                URL url = new URL(" ");

                JSONObject postDataParams = new JSONObject();

                //Passing scanned code as parameter

                postDataParams.put("sdata",scannedData);


                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            if(new String(result).equals("Success"))
                message = "Attendance Marked\n" + scannedData;
            else message = "No Internet";
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            cstatus.post(new Runnable() {
                @Override
                public void run() {
                    cstatus.setText(message);
                }
            });

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}









