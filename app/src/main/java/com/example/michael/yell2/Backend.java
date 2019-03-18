package com.example.michael.yell2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 7/31/16.
 */
public class Backend {

    Runnable customC1;
    DMActivity dm;
    boolean isLoading = false;
    String postScriptAddress = "get_all_posts";
    List<Update> localDMArray;
    boolean firstTimeReversed = true;
    CardFactory c;
    public Update[] up = {/////////////////1470090680052
            /*
            new Update("trickshotter1245", "123423330033", "Hey, can anyone see this message?"),
            new Update("realDonaldTrump", "1470090680052", "Vote TRUMP!"),
            new Update("hillary_", "1470090680052", "Vote HILLARY!")
    */};
    public static String TAG = "app_backend";
    Client client;
    Activity activity;

    public Backend(Activity a, Client ccc) {
        activity = a;
        client = ccc;
        localDMArray = new ArrayList<Update>();
        if (!postScriptAddress.equals("get_all_posts")) {
            new recyclerScanner().start();
        }
        dm = new DMActivity();
        c = new CardFactory(activity, client);
    }

    ProgressDialog progressDialog;

    public void showLoad() {
        isLoading = true;
        progressDialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Loading...");
        progressDialog.create();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void cancelLoad() {
        isLoading = false;
        progressDialog.cancel();
    }

    public void draw() {
        //c.generate(up);
        c.setMyDataset(up);
        c.generate(false);
    }

    int pendingUpdates = 0;
    public void showSnackBar(String string) {
        View parentLayout = activity.findViewById(R.id.parent_view);
        Snackbar.make(parentLayout, string, Snackbar.LENGTH_LONG)
                .setAction("REFRESH", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(activity, "Refreshing...", Toast.LENGTH_SHORT).show();
                        pendingUpdates = 0;
                        updateLayoutFromBackend();
                        //c.update(); asdf
                    }
                })
                .setActionTextColor(activity.getResources().getColor(android.R.color.holo_blue_light))
                .show();
    }

    public void showSnackBar(String string, String button, final Runnable r) {
        View parentLayout = activity.findViewById(R.id.parent_view);
        Snackbar.make(parentLayout, string, Snackbar.LENGTH_LONG)
                .setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        r.run();
                    }
                })
                .setActionTextColor(activity.getResources().getColor(android.R.color.holo_blue_light))
                .show();
    }

    public void showCreepySnackBar(String string, String button, final Runnable r) {
        View parentLayout = activity.findViewById(R.id.parent_view);
        Snackbar.make(parentLayout, string, Snackbar.LENGTH_LONG)
                .setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        r.run();
                    }
                })
                .setActionTextColor(activity.getResources().getColor(android.R.color.holo_blue_light))
                .setDuration(Snackbar.LENGTH_LONG)
                .show();
    }

    public void onMessage(String message) {
        //Toast.makeText(activity, "Received a message", Toast.LENGTH_SHORT).show();
        if (message.startsWith(MainActivity.SL)) {
            String[] total = message.split(MainActivity.GL); // ! GL message GL username
            String mes = total[1];
            String us = total[2];
            addToLocalDMArray(mes, us);
            Log.v("app_push", "Got it");
            if (!activity.hasWindowFocus()) {
                //DMActivity.chatArrayAdapter.addItem(u);
            }
        } else {
            String[] a = uncode(message);
            Log.v(TAG, a[0] + "//" +  a[1] + "//" + a[2]);
            Update u = new Update(a[0], a[1], a[2], a[3]);
            c.addToDataset(u);
            pendingUpdates++;
            showSnackBar(pendingUpdates + " new posts available.");
        }
    }

    Update pendingUpdate;

    public void setPendingUpdate(Update t) {
        pendingUpdate = t;
        post();
    }

    public String[] uncode(String coded) { //....>>>....|||....
        /*String[] a = coded.split(MainActivity.LTS);
        Log.v(TAG, "a[0]=" + a[0] + "//a[1]=" + a[1]);
        String[] b = a[1].split(MainActivity.PSS);
        Log.v(TAG, "b[0]=" + b[0] + "//b[1]=" + b[1]);
        String name = a[0];
        String ms = b[0];
        String content = b[1];*/
        return coded.split(MainActivity.GL);
        //return new String[] {name, ms, content};
    }

    public List<Update> uncode_array(String mastercoded) { //....>>>....|||....
        List<Update> list = new ArrayList<Update>();
        String[] posts = split(mastercoded, MainActivity.SPLIT);
        c.reverse();
        /*for (int i = 0; i < posts.length; i++) {
            String coded = posts[i];
            String[] a = coded.split(MainActivity.LTS);
            Log.v(TAG, "a[0]=" + a[0] + "//a[1]=" + a[1]);
            String[] b = a[1].split(MainActivity.PSS);
            Log.v(TAG, "b[0]=" + b[0] + "//b[1]=" + b[1]);
            String name = a[0];
            String ms = b[0];
            String content = b[1];
            list.add(new Update(name, ms, content));
        }*/
        for (String s : posts) {
            if (!s.equals("start")) {
                String[] x = s.split(MainActivity.GL);
                list.add(new Update(x[0], x[1], x[2], x[3]));
            }
        }
        //list.remove(0);
        return list;
    }

    public String[] split(String coded, String splitAt) {
        return coded.split(splitAt);
    }

    private void addToList(List<String> list, String[] a) {
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
    }

    public void post() {
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("content", pendingUpdate.content);
        j.put("ms", pendingUpdate.ms);
        j.put("from", pendingUpdate.from);
        j.put("id", pendingUpdate.id);
        endpoints.callEndpoint("write_post", j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                showSnackBar("Your post is live! Refresh the app to view it in action!", "REFRESH", new Runnable() {
                    @Override
                    public void run() {
                        finishPost();
                    }
                });
            }
            @Override
            public void onFailure(final Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "error posting: " + error.toString());
                        post();
                    }
                });
                if (error.toString().contains("BLRuntime")) {
                    finishPost();
                }
            }
        });
    }

    public void finishPost() {
        c.addToDataset(pendingUpdate);
        c.update();
    }

    String mostRecentOrder;
    String toOrder;
    String recentInstanceOf = "";
    BigDecimal mostRecentOrderb;
    BigDecimal toOrderb;
    int bigDifference = 0;
    public static String RECENT = "recent";

    public void updateLayoutFromBackend(final int since, final int to) {
        checkSearching();
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("from", since); //this means the loop: to and since represent the lowest distance and highest distance form the current order, respectively
        j.put("to", to);
        endpoints.callEndpoint(postScriptAddress, j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                updateLayoutWithList((String) result.get("message"));
                if (result.get("currentOrder") instanceof BigDecimal) {
                    mostRecentOrderb = (BigDecimal) result.get("currentOrder");
                    toOrderb = (BigDecimal) result.get("usedOrder");
                    recentInstanceOf = "b";
                    bigDifference = Integer.parseInt(mostRecentOrderb.toString()) - Integer.parseInt(toOrderb.toString());
                } else {
                    mostRecentOrder = (String) result.get("currentOrder");
                    toOrder = (String) result.get("usedOrder");
                    recentInstanceOf = "s";
                    bigDifference = Integer.parseInt(mostRecentOrder) - Integer.parseInt(toOrder);
                }
            }

            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        updateLayoutFromBackend(since, to);
                    }
                });
            }
        });
    }

    public void updateLayoutFromBackend() {
        checkSearching();
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("from", RECENT); //this means the loop: to and since represent the lowest distance and highest distance form the current order, respectively
        j.put("to", 0);
        endpoints.callEndpoint(postScriptAddress, j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                updateLayoutWithList((String) result.get("message"));
                if (result.get("currentOrder") instanceof BigDecimal) {
                    mostRecentOrderb = (BigDecimal)result.get("currentOrder");
                    toOrderb = (BigDecimal)result.get("usedOrder");
                    recentInstanceOf = "b";
                    bigDifference = Integer.parseInt(mostRecentOrderb.toString()) - Integer.parseInt(toOrderb.toString());
                } else {
                    mostRecentOrder = (String)result.get("currentOrder");
                    toOrder = (String)result.get("usedOrder");
                    recentInstanceOf = "s";
                    bigDifference = Integer.parseInt(mostRecentOrder) - Integer.parseInt(toOrder);
                }
                Log.v(TAG, "Completed updateLayoutFromBackend() request.");
            }

            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        updateLayoutFromBackend();
                    }
                });
            }
        });
    }

    public void updateLayoutFromBackend(final Runnable r) {
        checkSearching();
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("from", RECENT); //this means the loop: to and since represent the lowest distance and highest distance form the current order, respectively
        j.put("to", 0);
        endpoints.callEndpoint(postScriptAddress, j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                updateLayoutWithList((String) result.get("message"));
                if (result.get("currentOrder") instanceof BigDecimal) {
                    mostRecentOrderb = (BigDecimal)result.get("currentOrder");
                    toOrderb = (BigDecimal)result.get("usedOrder");
                    recentInstanceOf = "b";
                    bigDifference = Integer.parseInt(mostRecentOrderb.toString()) - Integer.parseInt(toOrderb.toString());
                } else {
                    mostRecentOrder = (String)result.get("currentOrder");
                    toOrder = (String)result.get("usedOrder");
                    recentInstanceOf = "s";
                    bigDifference = Integer.parseInt(mostRecentOrder) - Integer.parseInt(toOrder);
                }
                Log.v(TAG, "Completed updateLayoutFromBackend() request.");
                r.run();
            }

            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        updateLayoutFromBackend(r);
                    }
                });
            }
        });
    }

    public void updateLayoutWithList(String needToUncode) {
        checkSearching();
        Log.v(TAG, "Started uncoding array.");
        List<Update> list = uncode_array(needToUncode);
        if (c.myDataset != null && c.myDataset.length > 0) {
            //c.reverse();
        }
        for (Update u : list) {
            Log.v(TAG, "Update u = " + u.toString());
            if (!containsId(c.myDataset, u)) {
                c.addToDataset(u);
            }
        }
        if (firstTimeReversed) {
            //c.reverse();
            firstTimeReversed = false;
        }
        Log.v(TAG, "Finished uncoding the array.");
        c.sortDatasetByTime();
        Log.v(TAG, "Sorted the array by time.");
        c.update();
        if (isLoading) {
            cancelLoad();
        }
        Log.v(TAG, "Finished and canceled loading.");
        c.verifyDuplicates();
    }

    private boolean containsId(Update[] m, Update u) {
        for (int i = 0; i < m.length; i++) {
            if (m[i].id.equals(u.id)) {
                return true;
            }
        }
        return false;
    }

    public void checkSearching() {
        if (c.isSearching) {
            c.myDataset = c.backupDataset;
            c.isSearching = false;
        }
    }

    public void keepInTouchWithDataset_Seaching() {
        c.myDataset = c.backupDataset;
        //c.isSearching = false;
    }

    public boolean isRecyclerAtBottom() {
        checkSearching();
        if (c.mRecyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) c.mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == c.mRecyclerView.getAdapter().getItemCount() - 1)
                return true;
            else
                return false;
        }
        return false;
    }

    public class recyclerScanner extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                    if (isRecyclerAtBottom()) {
                        if (bigDifference != 1) {
                            Log.v(TAG, bigDifference + " == 1");
                            if (recentInstanceOf.equals("b")) {
                                updateLayoutFromBackend(Integer.parseInt(mostRecentOrderb.toString()), Integer.parseInt(toOrderb.toString()));
                            } else {
                                updateLayoutFromBackend(Integer.parseInt(mostRecentOrder), Integer.parseInt(toOrder));
                            }
                        }
                    }
                }catch(Exception e){Log.v(TAG, "Inturrupted sleeping.");}
            }
        }
    }

    public void addToLocalDMArray(String mes, String us) {
        Update u = new Update(us, "0", mes, "null");
        localDMArray.add(u);
        if (activity.findViewById(R.id.recycler) == null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dm.chatArrayAdapter.notifyDataSetChanged();
                }
            });
        }
        //Local.DMArray = localDMArray;
    }

    public class DMActivity {

        //List<Update> myDataset;
        public String SELF = "_self";

        ChatArrayAdapter chatArrayAdapter;
        private ListView listView;
        private EditText chatText;
        private Button buttonSend;

        public DMActivity() {}

        public void draw(ActionBar ab) {
            activity.setContentView(R.layout.activity_dm);
            //myDataset = /*new ArrayList<Update>();*/ localDMArray;
            showList();
            initialize();
        }

        public void initialize() {
            /*new Thread() {
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateLayoutFromBackend();
                }
            }.start();*/
        }

        public void showList() {
            buttonSend = (Button) activity.findViewById(R.id.send);
            c.format(buttonSend);

            listView = (ListView) activity.findViewById(R.id.msgview);

            chatArrayAdapter = new ChatArrayAdapter(activity.getApplicationContext(), R.layout.list_row_layout_even);
            listView.setAdapter(chatArrayAdapter);

            chatText = (EditText) activity.findViewById(R.id.msg);
            c.format(chatText);
            chatText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        return sendChatMessage();
                    }
                    return false;
                }
            });
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    sendChatMessage();
                }
            });

            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            listView.setAdapter(chatArrayAdapter);

            //to scroll the list view to bottom on data change
            chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    //listView.setSelection(chatArrayAdapter.getCount() - 1);
                }
            });
            customC1.run();
        }

        private boolean sendChatMessage() {
            //chatArrayAdapter.add(new Update(SELF, "0", chatText.getText().toString(), "null"));
            addToLocalDMArray(chatText.getText().toString(), SELF);
            chatText.setText("");
            //side = !side;
            if (chatText.getText().toString().length() > 0) {
                doTheChatOperation(chatText.getText().toString(), client.user().getUsername());
            }
            return true;
        }

        class ChatArrayAdapter extends ArrayAdapter<Update> {

            private TextView chatText;
            //private List<Update> chatMessageList = new ArrayList<Update>();
            private Context context;

            //@Override
            /*public void add(final Update object) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addToThing(object);
                    }
                });
            }*/

            //private void addToThing(Update object) {
                //super.add(object);
                //addItem(object);
            //}

            public ChatArrayAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId);
                //chatMessageList = myDataset;
                this.context = context;
            }

            @Override
            public void add(Update u) {
                //chatMessageList.add(u);
                //localDMArray.add(u);
                notifyDataSetChanged();
            }

            public int getCount() {
                return localDMArray.size();
            }

            public Update getItem(int index) {
                return localDMArray.get(index);
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                Update chatMessageObj = getItem(position);
                View row = convertView;
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (chatMessageObj.from.equals(SELF)) {
                    row = inflater.inflate(R.layout.list_row_layout_even, parent, false);
                } else {
                    row = inflater.inflate(R.layout.list_row_layout_odd, parent, false);
                }
                chatText = (TextView) row.findViewById(R.id.msgr);
                c.format(chatText);
                chatText.setText(chatMessageObj.content);
                return row;
            }
        }
    }

    String final234;

    public void doTheChatOperation(String x, final String from) {
        //1. check string 2. call endpoint
        if (x.contains(MainActivity.GL)) {
            x = x.replace(MainActivity.GL, "");
        }
        final234 = x;
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("content", x);
        j.put("ms", "0");
        j.put("from", from);
        j.put("id", "null");
        endpoints.callEndpoint("send_message", j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {

            }
            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        doTheChatOperation(final234, from);
                    }
                });
            }
        });
    }

    public void updateLocation(final LatLng n) {
        String endpoint = "update_location";
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("lat", n.latitude);
        j.put("lng", n.longitude);
        j.put("username", client.user().getUsername());
        endpoints.callEndpoint(endpoint, j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {

            }

            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Connection to our servers has failed.", "RETRY", new Runnable() {
                    @Override
                    public void run() {
                        updateLocation(n);
                    }
                });
            }
        });
    }

}
