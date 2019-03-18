package com.example.michael.yell2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static int view = 0;
    static int SIGNUP = 1, MAIN = 2, DM = 3;
    boolean alreadySignedUp = true;
    Client client;
    String myName = "Anonymous";
    public static String PSS = "<<<";
    public static String LTS = ">>>";
    public static String GL = ">>>";
    public static String SPLIT = "><SPLIT><";
    public static Backend backend;
    public static String SL = "!" + GL;
    View vvvv;
    ActionBar mActionBar;
    static boolean isLocationAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        mActionBar = getSupportActionBar();
        initialize();
    }

    public void animate_in(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.in);
        v.startAnimation(anim);
    }

    public void mainScreen(boolean b) {
        if (!alreadySignedUp) {
            alreadySignedUp = true;
            //_onCreateOptionsMenu();
            _onCreateOptionsMenu();
        }
        view = MAIN;
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle("");
        View view = getLayoutInflater().inflate(R.layout.main_screen_actionbar, null);
        ImageView view1 = (ImageView) view.findViewById(R.id.button_);
        mActionBar.setCustomView(view);
        mActionBar.show();

        setContentView(R.layout.activity_main);
        backend.showLoad();
        loadInterface();
        backend.updateLayoutFromBackend();
    }

    public void DMActivityScreen() {
        view = DM;
        backend.customC1 = new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        initializeBackbutton();
                        showDMOverlay();
                    }
                });
            }
        };
        backend.dm.draw(mActionBar);

        mActionBar.setTitle("");
        View view = getLayoutInflater().inflate(R.layout.dm_actionbar, null);
        view.findViewById(R.id.button_e).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainScreen(false);
            }
        });
        mActionBar.setCustomView(view);
        mActionBar.hide();

        //dm_onCreateOptionsMenu();
    }

    public void showDMOverlay() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dm_overlay);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.layout_all);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        TextView text = (TextView) masterView.findViewById(R.id.title33);
        backend.c.format(text);
        dialog.show();
    }

    public void initializeBackbutton() {
        ImageView backImage = (ImageView) findViewById(R.id.button_exit);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainScreen(false);
            }
        });
        ;
    }

    public void initialize() {
        client = new Client.Builder(this).build();
        client.ping(new KinveyPingCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }

            @Override
            public void onFailure(Throwable throwable) {
                showDialog("Connection Failed", "Looks like your device failed to connect to the backend.\n" + throwable.toString());
            }
        });
        if (!client.user().isUserLoggedIn()) {
            alreadySignedUp = false;
            view = SIGNUP;
            showNameDialog();
        } else {
            alreadySignedUp = true;
            client.push().initialize(getApplication());
            backend = new Backend(MainActivity.this, client);
            mainScreen(false);
        }

        /*new Thread() {
            public void run() {
                while (true) {
                    try {
                        callLocationEndpoint();
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();*/
        defineLocationListener();
    }

    boolean pendingRequest = false;

    public void defineLocationListener() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                actuallyCallEndpointLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //pendingRequest = true;
            preReqP();
            isLocationAvailable = false;
            return;
        }
        //int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void preReqP() {
        /*backend.showSnackBar("Location permission is off, so you won't be able to message users", "TURN ON", new Runnable() {
            @Override
            public void run() {
                requestPermissions();
            }
        });*/
        isLocationAvailable = false;
    }

    final int MY_PERMISSIONS = 0;

    public void requestPermissions() {
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                backend.showSnackBar("For users to receive your messages, location should be enabled.", "GO", new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS);
                    }
                });
            } else {
                //String s = "For users to receive your messages, location should be enabled.";
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS);
            }*/
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("To be able to receive messages, we must be able to access your location.");
                builder.setTitle("Location Services");
                builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS);
                    }
                });

                builder.show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
            }
        }
        //}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Enabled! Yay!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "For users to receive your messages, location should be enabled.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    public void actuallyCallEndpointLocation(Location l) {
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("lat", l.getLatitude());
        j.put("lng", l.getLongitude());
        j.put("username", client.user().getUsername());
        endpoints.callEndpoint("update_location", j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
            }
            @Override
            public void onFailure(Throwable error) {

            }
        });
    }

    EditText usernameET;
    EditText pinET;

    public boolean contains_user(User[] o, String s) {
        for (int i = 0; i < o.length; i++) {
            if (o[i].getUsername().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public void showNameDialog() {
        TextView fii = (TextView)findViewById(R.id.title_signup);
        CardFactory.format(this, fii);
        TextView aeer = (TextView)findViewById(R.id.desc_signup);
        CardFactory.format(this, aeer);
        usernameET = (EditText)findViewById(R.id.username);
        CardFactory.format(this, usernameET);
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                client.userDiscovery().lookupByUserName(usernameET.getText().toString(), new KinveyUserListCallback() {
                    @Override
                    public void onSuccess(User[] users) {
                        if (users.length < 1) {

                        } else if (contains_user(users, usernameET.getText().toString())) {
                            usernameET.setError("That name is taken");
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (usernameET.getText().toString().length() > 24) {
                    usernameET.setError("Your name is too long");
                }
            }
        });
        pinET = (EditText)findViewById(R.id.password);
        //pinET.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        CardFactory.format(this, pinET);
        pinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (pinET.getText().toString().length() > 4) {
                    pinET.setError("Your PIN is too long");
                }
            }
        });
        Button proceed = (Button)findViewById(R.id.proceed_button);
        CardFactory.format(this, proceed);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "This", Toast.LENGTH_SHORT).show();
                if (!(usernameET.getText().toString().length() > 24) && !(usernameET.getText().toString().length() > 4)) {
                    String username = usernameET.getText().toString();
                    myName = username;
                    if (myName.contains(PSS)) {
                        myName = myName.replace(PSS, "");
                    } else if (myName.contains(LTS)) {
                        myName = myName.replace(LTS, "");
                    } else if (myName.contains(SPLIT)) {
                        myName = myName.replace(SPLIT, "");
                    } else if (myName.contains(GL)) {
                        myName = myName.replace(GL, "");
                    }

                    create(usernameET.getText().toString(), pinET.getText().toString());
                }
            }
        });
        //initializeBackbutton();
    }

    public void create(String u, String p) {
        client.user().create(u, p, new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (client.user().isUserLoggedIn()) {
                    userCollectionSetUpEndpoint(user);
                    client.push().initialize(getApplication());
                    backend = new Backend(MainActivity.this, client);
                    modifyThing();
                    //Toast.makeText(MainActivity.this, "Your identity has been set to \"" + myName + "\"", Toast.LENGTH_SHORT).show();
                    mainScreen(true);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                showDialog("Account Creation Failure", "Failed to create your account.\n" + throwable.toString());
            }
        });
    }

    public void modifyThing() {
        backend.client.user().put("p", 1);
        backend.client.user().update(new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable e) {

            }
            @Override
            public void onSuccess(User u) {

            }
        });
    }

    public void userCollectionSetUpEndpoint(User u) {
        AsyncCustomEndpoints endpoints = client.customEndpoints();
        GenericJson j = new GenericJson();
        j.put("name", u.getUsername());
        endpoints.callEndpoint("setup_account", j, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson result) {
            }

            @Override
            public void onFailure(Throwable error) {

            }
        });
    }

    public void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (view == DM) {
            mainScreen(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    SwipeRefreshLayout mSwipeRefreshLayout;

    public void loadInterface() {
        //Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
        backend.draw();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    preReqP();
                }
                backend.updateLayoutFromBackend(new Runnable() {
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        ImageView writePost = (ImageView)findViewById(R.id.writepost_image);
        writePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDialog();
            }
        });
        //backend.c.forceViewTimes();
        //backend.updateLayoutFromBackend();
    }

    Menu storedMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        storedMenu = menu;
        getMenuInflater().inflate(R.menu.menu_dm, menu);
        if (alreadySignedUp) {
            _onCreateOptionsMenu();
        }
        return true;
    }

    boolean searched = false;

    public boolean _onCreateOptionsMenu() {
        getMenuInflater().inflate(R.menu.menu_main, storedMenu);
        final MenuItem item = storedMenu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searched) {
                    backend.keepInTouchWithDataset_Seaching();
                }
                filterThat(query);
                searched = true;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filterThat(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                backend.updateLayoutFromBackend(new Runnable() {
                    @Override
                    public void run() {
                        //nothing
                    }
                });
                searched = false;
                return false;
            }
        });
        return true;
    }

    public boolean dm_onCreateOptionsMenu() {
        getMenuInflater().inflate(R.menu.menu_dm_actual, storedMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            /*case R.id.action_post:
                postDialog();
                return true;*/
            case R.id.action_DM:
                DMActivityScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    EditText postField;
    Button postButton;
    View dialog;
    AlertDialog alert;
    boolean prev = false;

    public void postDialog() {
        backend.c.resetCurrentTime();

        LayoutInflater inflater = getLayoutInflater();
        dialog = inflater.inflate(R.layout.dialog_post, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        postField = (EditText) dialog.findViewById(R.id.contentField);
        backend.c.format(postField);
        TextView th = (TextView) dialog.findViewById(R.id.content_);
        backend.c.format(th);
        th.setText("You're writing a post to everyone else currently using this app!\n\nBeware: the max length of your message is 250 characters.");
        postField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int length = postField.getText().toString().length();
                if (length > 250) {
                    prev = true;
                    postButton.setEnabled(false);
                } else if (length <= 250 && prev) {
                    postButton.setEnabled(true);
                }
                return false;
            }
        });

        postButton = (Button) dialog.findViewById(R.id.postButton);
        backend.c.format(postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = postField.getText().toString().length();
                if (length > 250) {
                    alert.cancel();
                    backend.showSnackBar("Your post was too long. Sorry!", "RE-DO", new Runnable() {
                        @Override
                        public void run() {
                            postDialog();
                        }
                    });
                } else {
                    alert.cancel();
                    String content = postField.getText().toString();
                    if (content.contains(PSS)) {
                        content = content.replace(PSS, "");
                    } else if (content.contains(LTS)) {
                        content = content.replace(LTS, "");
                    } else if (content.contains(SPLIT)) {
                        content = content.replace(SPLIT, "");
                    } else if (content.contains(GL)) {
                        content = content.replace(GL, "");
                    }
                    backend.setPendingUpdate(new Update(backend.client.user().getUsername(), backend.c.currentMs, content, makeRandomId()));
                }
            }
        });
        builder.setView(dialog);
        alert = builder.create();
        alert.show();
    }

    public String makeRandomId() {
        return client.user().getId() + "-" + randomString();
    }

    private String randomString() {
        Random rand = new Random();
        String fin = "";
        for (int i = 0; i < 15; i++) {
            fin += rand.nextInt(20);
        }
        return fin;
    }

    public void filterThat(String s) {
        List<Update> a = new ArrayList<Update>();
        for (Update u : backend.c.backupDataset) {
            if (u.content.contains(s)) {
                a.add(u);
            }
        }
        if (a.size() == 0) {
            //showDialog("Oops!", "Looks like there are no posts containing your search query.\n\n...But you can change that!");
        }
        backend.c.mAdapter.setFilter(a);
    }

    public void showPromptSnackbar() {
        backend.showSnackBar("Psst... You should write a post!", "SOUNDS GOOD", new Runnable() {
            @Override
            public void run() {
                postDialog();
            }
        });
    }

}
