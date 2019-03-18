package com.example.michael.yell2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Michael on 3/25/16.
 */
public class CardFactory implements BasicImageDownloader.OnImageLoaderListener{

    boolean isSearching = false;
    private List<CardView> cardList;
    List<Boolean> bound;
    private Activity activity;
    Update[] myDataset;
    Client client;
    String currentMs;
    ImageView currentDownload;

    public void onError(BasicImageDownloader.ImageError error) {
        Toast.makeText(activity, "Error downloading images.", Toast.LENGTH_SHORT).show();
    }

    public void onProgressChange(int percent) {
        Log.v(Backend.TAG, "Image download progress changed: " + percent + "%");
    }

    public void onComplete(Bitmap result) {
        currentDownload.setImageBitmap(result);
    }


    public CardFactory(Activity activity, Client client) {
        this.activity = activity;
        cardList = new ArrayList<>();
        this.client = client;
        bound = new ArrayList<Boolean>();
    }

    public void setMyDataset(Update[] a) {
        myDataset = a;
    }

    public void generate(final boolean resetThing) {
        if (cardList != null) {
            cardList.clear();
        }
        if (resetThing) {
            Log.v(Backend.TAG, "Array length = " + myDataset.length + " // " + Arrays.toString(myDataset));
        }
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        endpoints.callEndpoint("cardfactory_aux", new GenericJson(), new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                String res = result.get("current").toString();
                currentMs = res;
                Log.v(Backend.TAG, "Successfully refreshed times :: " + currentMs);
                setTimes();
            }
            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Failed to connect to our database.", "RETRY", new Runnable() {
                    public void run() {
                        generate(resetThing);
                    }
                });
            }
        });
        actuallyGenerate();
    }

    public void resetCurrentTime() {
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        endpoints.callEndpoint("cardfactory_aux", new GenericJson(), new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
                String res = result.get("current").toString();
                currentMs = res;
                setTimes();
            }

            @Override
            public void onFailure(Throwable error) {
                showSnackBar("Failed to get current time.", "RETRY", new Runnable() {
                    public void run() {
                        resetCurrentTime();
                    }
                });
            }
        });
    }

    public void setTimes() {
        List<String> times = new ArrayList<String>();
        Log.v(Backend.TAG, "cardList = " + cardList.size() + " // myDataset = " + myDataset.length);
        for (int i = 0; i < myDataset.length; i++) {
            if (i < cardList.size()) {
                Log.v(Backend.TAG, "i = " + i + " // myDataset[" + i + "] = " + cardList.get(i).toString());
                TextView v = (TextView)cardList.get(i).findViewById(R.id.time_display);
                format(v);
                v.setText(parseTime(myDataset[i].ms));
            }
            times.add(parseTime(myDataset[i].ms));
        }
        timeArray = new String[times.size()];
        for (int i = 0; i < times.size(); i++) {
            timeArray[i] = times.get(i);
        }
    }

    private void showSnackBar(String string, String button, final Runnable r) {
        View parentLayout = activity.findViewById(R.id.parent_view);
        Snackbar.make(parentLayout, string, Snackbar.LENGTH_LONG)
                .setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        r.run();
                        Toast.makeText(activity, "Trying...", Toast.LENGTH_SHORT).show();

                    }
                })
                .setActionTextColor(activity.getResources().getColor(android.R.color.holo_blue_light ))
                .show();
    }

    MyAdapter2 mAdapter;
    RecyclerView mRecyclerView;


    private void actuallyGenerate() {
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter2(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        initializeFAB();
    }

    public void initializeFAB() {
        /*FloatingActionButton myFab = (FloatingActionButton) activity.findViewById(R.id.myFAB);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(activity, "Clicked.", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    String[] timeArray;

    List<Update> pendingUpdates = new ArrayList<>();
    Update[] backupDataset;

    public void addToDataset(Update u){
        pendingUpdates.add(u);
        bound.add(Boolean.FALSE);
        Update[] newDataset = new Update[myDataset.length + 1];
        for (int i = 1; i < newDataset.length; i++) {
            newDataset[i] = myDataset[i-1];
        }
        newDataset[0] = u;
        myDataset = newDataset;
        backupDataset = myDataset;
    }

    public void update() {
        int count = 0;
        for (Update u : pendingUpdates) {
            CardView card = (CardView) LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.card_default, mRecyclerView, false);
            formatCard(card, count);
            cardList.add(card);
            //mAdapter.notifyItemRangeChanged(0, myDataset.length);
            //mAdapter.notifyDataSetChanged();
            count++;
        }
        pendingUpdates.clear();
        finishUpdating();
    }

    private void finishUpdating() {
        generate(true);
    }

    private void formatCard(CardView mCardView, int count) {
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent DM = new Intent(activity, DMActivity.class);
                DM.putExtra("tag", mDataset[position].content);
                activity.startActivity(DM);*/

            }
        });
        Update update = pendingUpdates.get(count);
        TextView user = (TextView)mCardView.findViewById(R.id.username_display);
        format(user);
        TextView body = (TextView)mCardView.findViewById(R.id.body_display);
        format(body);
        //TextView upa = (TextView)mCardView.findViewById(R.id.in_chat_title);
        /*TextView counter = (TextView)mCardView.findViewById(R.id.in_chat_display);
        counter.setText("...");
        format(counter);
        ImageView upArrow = (ImageView)mCardView.findViewById(R.id.up_arrow);
        counterInit(counter, upArrow);*/
        user.setText(update.from);
        body.setText(update.content);
        //time.setText(parseTime(update.ms));
        TextView time = (TextView)mCardView.findViewById(R.id.time_display);
        format(time);
        time.setText("Loading...");
    }

    public void reverse() {
        //myDataset = myDataset;
        List<Update> x = Arrays.asList(myDataset);
        Collections.reverse(x);
        myDataset = (Update[])x.toArray();
    }

    public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.ViewHolder> {
        private Update[] mDataset;
        private List<Update> listDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView mCardView;
            //public TextView mTextView;

            public ViewHolder(CardView v/*, TextView v2*/) {
                super(v);
                mCardView = v;
                //mTextView = v2;
            }
        }

        public MyAdapter2(Update[] myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            /*View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_card, parent, false);
            View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text, parent, false);
            ViewHolder vh = new ViewHolder((CardView)v);
*/
            CardView card = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_default, parent, false);
            //TextView item = (TextView) LayoutInflater.from(card.getContext()).inflate(R.layout.text_content, card, false);
            //card.addView(item);
            //cardList.add(card);

            ViewHolder vh = new ViewHolder(card/*, item*/);

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent DM = new Intent(activity, DMActivity.class);
                    //DM.putExtra("tag", mDataset[position].content);
                    //activity.startActivity(DM);
                    //Toast.makeText(activity, "id=" + mDataset[position].id, Toast.LENGTH_SHORT).show();
                }
            });
            Update update = mDataset[position];
            //holder.mTextView = parseUpdate(mDataset[position], holder.mCardView);
            TextView user = (TextView)holder.mCardView.findViewById(R.id.username_display);
            format(user);
            TextView body = (TextView)holder.mCardView.findViewById(R.id.body_display);
            format(body);
            //TextView upa = (TextView)holder.mCardView.findViewById(R.id.in_chat_title);
            /*TextView counter = (TextView)holder.mCardView.findViewById(R.id.in_chat_display);
            counter.setText("...");
            format(counter);
            ImageView upArrow = (ImageView)holder.mCardView.findViewById(R.id.up_arrow);
            counterInit(counter, upArrow);*/
            user.setText(update.from);
            body.setText(update.content);
            //time.setText(parseTime(update.ms));
            TextView time = (TextView)holder.mCardView.findViewById(R.id.time_display);
            format(time);
            if (timeArray == null || timeArray.length == 0) {
                time.setText("Loading...");
            } else {
                //Log.v(Backend.TAG, "timeArray[" + position + "] = " + timeArray[position]);
                time.setText(timeArray[position]);
            }
            if (!bound.get(position).equals(Boolean.FALSE)) {
                cardList.add(holder.mCardView);
                bound.set(position, Boolean.TRUE);
            }
            Log.v(Backend.TAG, "card onBindViewHolder = " + position);
        }

        @Override
        public int getItemCount() {
            if (mDataset != null && mDataset.length != 0) {
                return mDataset.length;
            } else {
                return 0;
            }
        }

        public void setFilter(List<Update> all) {
            isSearching = true;
            listDataset = new ArrayList<Update>();
            listDataset.addAll(all);
            //mDataset = (Update[])listDataset.toArray();
            mDataset = new Update[all.size()];
            for (int i = 0; i < all.size(); i++) {
                mDataset[i] = all.get(i);
            }
            notifyDataSetChanged();
        }
    }

    public TextView parseUpdate(Update update, CardView card) {
        String o = update.content;
        TextView view;
        if (o != null) {
            view = (TextView) LayoutInflater.from(card.getContext()).inflate(R.layout.text_content, card, false);
            view.setText(o);
        }
        else {
            view = (TextView) LayoutInflater.from(card.getContext()).inflate(R.layout.text_content, card, false);
            view.setText("Unable to load content.");
        }
        format(view);
        return view;
    }

    public String parseTime(String t) {
        BigInteger c = new BigInteger(currentMs);
        BigInteger tb = c.subtract(new BigInteger(t));
        BigInteger seconds = new BigInteger(tb.divide(new BigInteger("1000")).toString());
        BigInteger minutes = new BigInteger(seconds.divide(new BigInteger("60")).toString());
        BigInteger hours = new BigInteger(minutes.divide(new BigInteger("60")).toString());
        BigInteger days = new BigInteger(hours.divide(new BigInteger("24")).toString());
        BigInteger weeks = new BigInteger(days.divide(new BigInteger("7")).toString());
        BigInteger years = new BigInteger(days.divide(new BigInteger("52")).toString());
        if (years.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(years.toString());
            if (z == 1) {
                return "1 year ago";
            } else {
                return years.toString() + " years ago";
            }
        } else if (weeks.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(weeks.toString());
            if (z > 52) {
                return "1 year ago";
            } else if (z == 1) {
                return "1 week ago";
            } else {
                return weeks.toString() + " weeks ago";
            }
        } else if (days.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(days.toString());
            if (z > 7) {
                return "1 week ago";
            } else if (z == 1) {
                return "1 day ago";
            } else {
                return days.toString() + " days ago";
            }
        } else if (hours.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(hours.toString());
            if (z > 24) {
                return "1 day ago";
            } else if (z == 1) {
                return "1 hour ago";
            } else {
                return hours.toString() + " hours ago";
            }
        } else if (minutes.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(minutes.toString());
            if (z > 60) {
                return "1 hour ago";
            } else if (z == 1) {
                return "1 minute ago";
            } else {
                return minutes.toString() + " minutes ago";
            }
        } else if (seconds.subtract(new BigInteger("1")).compareTo(new BigInteger("0")) > 0) {
            int z = Integer.parseInt(seconds.toString());
            if (z > 60) {
                return "1 minute ago";
            } else if (z == 1) {
                return "1 second ago";
            } else {
                return seconds.toString() + " seconds ago";
            }
        } else {
            return "Just now";
        }
    }

    public void sortDatasetByTime() {
        Arrays.sort(myDataset);
        Log.v(Backend.TAG, myDataset[0].toString() + "//first :: last//" + myDataset[myDataset.length - 1].toString());
    }

    public void verifyDuplicates() {
        int countFound = 0;
        for (int i = 0; i < myDataset.length; i++) {
            Update u = myDataset[i];
            for (int s = 0; s < myDataset.length; s++) {
                if (u.equals(myDataset[s])) {
                    countFound++;
                }
                if (countFound > 1) {
                    removeFromDataset(s);
                }
            }
            countFound = 0;
        }
    }

    public void removeFromDataset(int z) {
        pendingUpdates.remove(z);
        bound.remove(z);
        Update[] newDataset = new Update[myDataset.length - 1];
        int x = 0;
        for (int i = 0; i < myDataset.length; i++) {
            if (i == z) {
                x--;
            } else {
                newDataset[x] = myDataset[i];
            }
            x++;
        }
        myDataset = newDataset;
    }

    public void format(TextView text) {
        //Typeface myTypeface = Typeface.createFromAsset(activity.getAssets(), "fonts/segoe1.otf");
        Typeface myTypeface = Typeface.createFromAsset(activity.getAssets(), "fonts/segoe4.otf");
        text.setTypeface(myTypeface);
    }

    public static void format(Activity activity, TextView text) {
        //Typeface myTypeface = Typeface.createFromAsset(activity.getAssets(), "fonts/segoe1.otf");
        Typeface myTypeface = Typeface.createFromAsset(activity.getAssets(), "fonts/segoe4.otf");
        text.setTypeface(myTypeface);
    }

    public void counterInit(TextView counter, ImageView up) {
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
