package com.example.oscar.Sloncha;

import android.os.AsyncTask;
import android.os.StrictMode;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by oscar on 12/06/2017.
 */

public class Notification {

    public static void SendChat(final String Reciever_id, final String message) {


        final String username = getUserName();
        DatabaseReference myref;
        final String[] NotificationId = new String[1];

        myref = FirebaseDatabase.getInstance().getReference().child("Users").child(Reciever_id);
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NotificationId[0] = dataSnapshot.child("NotificationId").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_id;

                    //This is a Simple Logic to SendChat Notification different Device Programmatically....
                    if (Reciever_id != getUser_Id()) {
                        send_id = Reciever_id;
                    } else {
                        send_id = getUser_Id();
                    }

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic YjlkMjA5MDQtYTVmZS00MTIxLTg0MTQtZWE5NGM4ZmY2YWJk");
                        con.setRequestMethod("POST");

                        /*

                        String strJsonBody = "{"
                                + "'app_id': '45820594-aea7-4600-b522-b50a76699cb7',"
                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_id + "\"}],"
                                + "\"include_player_ids\": {\"en\": \"[" + NotificationId[0] + "] \"}"
                                + "\"data\": {'foo': \"bar\"},"
                                + "\"headings\": {\"en\": \" " + username + " send you a message:\",\"es\": \" " + username + " send you a message:\"},"
                                + "\"subtitle\": {\"en\": \" " + message + " \",\"es\": \" " + message + " \"},"
                                + "\"contents\": {\"en\": \" " + message + " \",\"es\": \" " + message + " \"},"
                                + "\"large_icon\": {\"notification_large_icon\"},"
                                + "\"android_group\": {\"chat_group\"},"
                                + "\"android_group_message\": {\"en\": \"You have $[notif_count] new messages\"},"
                                + "\"android_accent_color\": {\"FF0d47a1\"}"
                                + "}";
 */
                        String strJsonBody = "{"
                                + "\"app_id\": \"45820594-aea7-4600-b522-b50a76699cb7\","
                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_id + "\"}],"
                                + "\"data\": {\"foo\": \"bar\"},"

                                + "\"headings\": {\"en\": \"" + username + " send you a message \"},"
                                + "\"contents\": {\"en\": \"" + message + " \"}"

                                + "}";

                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

    }

    public static void SendNotification(final String Reciever_id, final String message) {

        final String username = getUserName();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_id;

                    //This is a Simple Logic to SendChat Notification different Device Programmatically....
                    if (Reciever_id != getUser_Id()) {
                        send_id = Reciever_id;
                    } else {
                        send_id = getUser_Id();
                    }

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic YjlkMjA5MDQtYTVmZS00MTIxLTg0MTQtZWE5NGM4ZmY2YWJk");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"45820594-aea7-4600-b522-b50a76699cb7\","
                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_id + "\"}],"
                                + "\"data\": {\"foo\": \"bar\"},"

                                + "\"headings\": {\"en\": \"Notification from " + username + "\"},"
                                + "\"contents\": {\"en\": \"" + message + " \"}"

                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

    }

    private static String getUserName() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getDisplayName();
    }

    private static String getUser_Id() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getUid();
    }
}
