package com.yatsuhashi.openschedule;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
    final Handler handler=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Uri uri =getIntent().getData();
        resolve(uri);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Uri uri =intent.getData();
        resolve(uri);
    }

    private void resolve(final Uri uri ){
        new Thread(){
            @Override
            public void run() {
                URL url ;
                HttpsURLConnection connection = null;
                try {
                    url = new URL(uri.toString());

                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");
                    if(connection.getResponseCode()==200){
                        String temp=connection.getURL().toString();
                        if(!temp.startsWith("https://schedule.line.me/")){
                            onWrongURL(temp);
                            return;
                        }

                        final String id=temp.substring(temp.lastIndexOf("/")+1,temp.length());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onParsed(id);
                            }
                        });
                    }else {
                        //200じゃないときリダイレクトを探る
                        String redirect=connection.getHeaderField("Location");
                        if(redirect==null){
                            onWrongURL(uri.toString());
                        }else {
                            onWrongURL(redirect);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    onFailure();
                }finally {
                    if(connection!=null)connection.disconnect();
                }

            }
        }.start();
    }

    private void onParsed(String id){
        Uri uri = Uri.parse("line://ch/1412248049/#/event/detail/"+id);
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);
        finish();
    }

    private void onFailure(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.failed,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void onWrongURL(final String url){
        //Log.v("onWrongURL",url);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse(url);
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
                finish();
            }
        });
    }
}
