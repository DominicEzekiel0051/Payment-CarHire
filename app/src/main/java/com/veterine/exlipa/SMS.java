package com.veterine.exlipa;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SMS extends AppCompatActivity implements OnItemClickListener {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1 ;
    private static SMS inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static SMS instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SMS.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);


        } else{

            refreshSmsInbox();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:

                if(grantResults.length>0 & grantResults[0]== PackageManager.PERMISSION_GRANTED){

                    refreshSmsInbox();

                }else{

                }
        }
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {


            try {
                String text= smsInboxCursor.getString(indexBody);
                // String smallText =text.substring(27,34);

                if(smsInboxCursor.getString(indexAddress).equals("M-PESA") && text.contains("Umepokea")) {
                    String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                            "\n" + smsInboxCursor.getString(indexBody) + "\n";
                    arrayAdapter.add(str);
                    //Toast.makeText(this,text+"", Toast.LENGTH_SHORT).show();
                    SendSms(text);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();


            }
        } while (smsInboxCursor.moveToNext());


    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = (pos+1)+"\n"+address + "\n";
            smsMessageStr += smsMessage;
            if(address.equals("SMS From: M-PESA")) {
                Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
                SendSms(smsMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void SendSms(final String sms_) {


        String url = "http://192.168.43.240/excarental/exLipa.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String feed) {

                        try {
                            JSONArray jsonarray = new JSONArray(feed);
                            JSONObject jsonobject = jsonarray.getJSONObject(0);
                            int code=jsonobject.getInt("code");

                            Toast toast = Toast.makeText(SMS.this, "code: "+code, Toast.LENGTH_LONG);
                            toast.show();


                            //Context context;
                        } catch (JSONException e) {
                            //        Toast toast = Toast.makeText(c,"basi tena", Toast.LENGTH_LONG);
                            //        toast.show();
                            e.printStackTrace();
                            Log.e("Send sms fail: ", e.toString());
                        }


                        //do stuffs with response of post
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //do stuffs with response error

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                // params.put(KEY_PASSWORD,password);
                params.put("text", sms_);

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

}
