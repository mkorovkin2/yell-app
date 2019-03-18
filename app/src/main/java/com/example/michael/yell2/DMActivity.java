package com.example.michael.yell2;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DMActivity extends AppCompatActivity {

    static List<Update> myDataset;
    public static String SELF = "_self";
    private static final String TAG = "app_chat";

    ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);
        myDataset = new ArrayList<Update>();
        myDataset.add(new Update("them", "0", "Hi there", "null"));
        myDataset.add(new Update("them2", "0", "Heyyyy", "null"));
        myDataset.add(new Update("them3", "0", "wasssup", "null"));
        myDataset.add(new Update(SELF, "0", "stfu guys kys", "null"));

        showList();
    }

    public void showList() {
        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.list_row_layout_even);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);
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
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(new Update(SELF, "0", chatText.getText().toString(), "null"));
        chatText.setText("");
        //side = !side;
        return true;
    }

    class ChatArrayAdapter extends ArrayAdapter<Update> {

        private TextView chatText;
        private List<Update> chatMessageList = new ArrayList<Update>();
        private Context context;

        @Override
        public void add(Update object) {
            super.add(object);
            chatMessageList.add(object);
        }

        public ChatArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            chatMessageList = myDataset;
            this.context = context;
        }

        public void addItem(Update u) {
            chatMessageList.add(u);
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.chatMessageList.size();
        }

        public Update getItem(int index) {
            return this.chatMessageList.get(index);
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
            chatText.setText(chatMessageObj.content);
            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
