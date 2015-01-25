package edu.pitt.cs1635.mjm217.prog2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class RecognizeView extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    //loaded drawing
    DrawingSave loadedDrawing;

    //recognize network connectivity task, off main thread
    class RecognizeAsync extends AsyncTask<String, Void, String> {
        private View view = findViewById(R.id.recognized_text_view);
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //setup progress spinner
            progressDialog = new ProgressDialog(RecognizeView.this);
            progressDialog.setMessage("Recognizing");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    progressDialog.cancel();
                }
            });
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<Integer> strokeListScaled = new ArrayList<Integer>();
            float scale = (float) 254/(float) RecognizeView.width;
            for (Stroke stroke : drawSurface.strokeList) {
                for (Line line : stroke.lineStroke) {
                    strokeListScaled.add(Math.round(line.x1 * scale));
                    strokeListScaled.add(Math.round(line.y1 * scale));
                    strokeListScaled.add(Math.round(line.x2 * scale));
                    strokeListScaled.add(Math.round(line.y2 * scale));
                }
                strokeListScaled.add(255);
                strokeListScaled.add(0);
            }
            strokeListScaled.add(255);
            strokeListScaled.add(255);

            String inkData = "[";
            int i;
            for (i = 0; i < strokeListScaled.size() - 1; i++) {
                inkData += strokeListScaled.get(i) + ", ";
            }
            inkData += strokeListScaled.get(i) + "]";
            String retrievedText = "";

            try {
                HttpClient httpclient;
                HttpPost httppost;
                ArrayList<NameValuePair> postParameters;
                httpclient = new DefaultHttpClient();
                httppost = new HttpPost("http://cwritepad.appspot.com/reco/usen");

                postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("key", "11773edfd643f813c18d82f56a8104ed"));
                postParameters.add(new BasicNameValuePair("q", inkData));

                httppost.setEntity(new UrlEncodedFormEntity(postParameters));

                HttpResponse response = httpclient.execute(httppost);

                BufferedReader download = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder builder = new StringBuilder();
                String temp = "";

                while ((temp = download.readLine()) != null) {
                    builder.append(temp);
                }

                retrievedText = builder.toString();
            }
            catch(Exception e) {
                return e.toString();
            }

            return retrievedText;
        }

        @Override
        protected void onPostExecute(String result) {
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            TextView recognizedText = (TextView) view.findViewById(R.id.recognized_text_view);
            if(result.length() > 3) {
                recognizedText.setText("Recognized Text: " + result);
            }
            else {
                recognizedText.setText("No Text Recognized");
                Toast.makeText(getApplicationContext(), "No Text Recognized", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //picture compression/sharing preparation
    class ShareAsync extends AsyncTask<String, Void, Intent> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //setup progress spinner
            progressDialog = new ProgressDialog(RecognizeView.this);
            progressDialog.setMessage("Saving");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    progressDialog.cancel();
                }
            });
            progressDialog.show();
        }

        @Override
        protected Intent doInBackground(String... params) {
            drawSurface.setDrawingCacheEnabled(true);
            Bitmap picture = Bitmap.createBitmap(drawSurface.getDrawingCache());
            drawSurface.setDrawingCacheEnabled(false);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int numDrawings = sp.getInt("num_drawings", 1);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File saved = new File(directory + File.separator + "drawing" + numDrawings + ".jpg");

            while(saved.exists() == true) {
                numDrawings++;
                saved = new File(directory + File.separator + "drawing" + numDrawings + ".jpg");
            }

            try {
                directory.mkdirs();
                saved.createNewFile();
                FileOutputStream output = new FileOutputStream(saved);
                output.write(bytes.toByteArray());
                output.close();
            }
            catch (Exception e) {

            }
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + directory + File.separator + "drawing" + numDrawings + ".jpg"));
            numDrawings++;
            sp.edit().putInt("num_drawings", numDrawings).apply();
            startActivity(Intent.createChooser(share, "Share Drawing"));

            return share;
        }

        @Override
        protected void onPostExecute(Intent result) {
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Toast.makeText(getApplicationContext(), "JPEG saved in Pictures directory", Toast.LENGTH_SHORT).show();
        }
    }

    static class DrawingSave implements Serializable {
        public ArrayList<Stroke> savedStrokes;
        public String recognized;

        public DrawingSave(ArrayList<Stroke> savedStrokes, String recognized) {
            this.savedStrokes = new ArrayList<Stroke>();

            for (Stroke stroke : savedStrokes) {
                Stroke tempStroke = new Stroke(stroke.lineStroke);
                tempStroke.paintColor = stroke.paintColor;
                this.savedStrokes.add(tempStroke);
            }
            
            this.recognized = recognized;
        }
    }

    static DrawWidget drawSurface;
    static int width;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_view);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.recognize_view, menu);
            restoreActionBar();
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_recognize:
                new RecognizeAsync().execute();
                return true;

            case R.id.item_share:
                new ShareAsync().execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if(position == 1) {
            Intent saveActivity = new Intent(this, SaveActivity.class);
            startActivityForResult(saveActivity, 1);
        }
        else {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, RecognizeFragment.newInstance(position + 1))
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                loadedDrawing = (DrawingSave) data.getSerializableExtra("drawing");
                drawSurface.clear();
                SerializePaint paint = new SerializePaint();
                paint.setAntiAlias(true);
                paint.setStrokeWidth(20);
                paint.setStrokeCap(Paint.Cap.ROUND);

                for (Stroke stroke : loadedDrawing.savedStrokes) {
                    ArrayList<Line> tempLineArray = new ArrayList<Line>();

                    for (Line line : stroke.lineStroke) {
                        Line tempLine = new Line(line.x1, line.x2, line.y1, line.y2);
                        tempLineArray.add(tempLine);
                    }

                    Stroke tempStroke = new Stroke(tempLineArray);
                    tempStroke.paintColor = stroke.paintColor;
                    tempStroke.strokeColor = paint;
                    drawSurface.strokeList.add(tempStroke);
                }

                drawSurface.invalidate();
                TextView recognizedText = (TextView) findViewById(R.id.recognized_text_view);
                recognizedText.setText(loadedDrawing.recognized);
            }
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * Recognize fragment, containing Handwriting paint/recognition widget, as well as clear/paint selection buttons
     */
    public static class RecognizeFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static RecognizeFragment newInstance(int sectionNumber) {
            RecognizeFragment fragment = new RecognizeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public RecognizeFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_recognize_view, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            // Add draw widget to view, make square with screen width dimensions
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);

            final RelativeLayout drawLayout = (RelativeLayout) getView().findViewById(R.id.draw_layout);
            drawSurface = new DrawWidget(getActivity());
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            drawSurface.setLayoutParams(params);
            drawSurface.setBackgroundColor(Color.WHITE);
            drawSurface.setId(1);
            drawLayout.addView(drawSurface);

            //add clear, undo, paint, and save selection buttons under draw widget
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, width/7);
            LinearLayout buttonBar = (LinearLayout) getView().findViewById(R.id.button_bar);
            buttonBar.setMinimumWidth(width);
            params.addRule(RelativeLayout.BELOW, 1);
            buttonBar.setLayoutParams(params);

            final TextView recognizedText = (TextView) getView().findViewById(R.id.recognized_text_view);
            ImageButton clearButton = (ImageButton) getView().findViewById(R.id.clear_button);
            clearButton.setPadding(0, width/60, 0 , width/60);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawSurface.clear();
                    recognizedText.setText("No Text Recognized");
                }
            });

            ImageButton undoButton = (ImageButton) getView().findViewById(R.id.undo_button);
            undoButton.setPadding(0, width/60, 0 , width/60);
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawSurface.undo();
                    recognizedText.setText("No Text Recognized");
                }
            });

            ImageButton paintColor = (ImageButton) getView().findViewById(R.id.paint_color);
            Drawable colorIcon = getResources().getDrawable(R.drawable.coloricon);
            ColorFilter color = new LightingColorFilter(Color.BLACK, Color.BLACK);
            colorIcon.setColorFilter(color);
            paintColor.setImageDrawable(colorIcon);
            paintColor.setPadding(0, width/60, 0 , width/60);

            ImageButton paintButton = (ImageButton) getView().findViewById(R.id.paint_button);
            paintButton.setPadding(0, width/60, 0 , width/60);
            final GridView paintSelect = new GridView(getActivity());
            ColorPickerAdapter colorsAdapter = new ColorPickerAdapter(getActivity(), drawSurface, drawLayout, paintSelect, paintColor, width);
            paintSelect.setBackgroundColor(Color.LTGRAY);
            paintSelect.setNumColumns(3);
            paintSelect.setColumnWidth(GridView.AUTO_FIT);
            paintSelect.setGravity(Gravity.CENTER);
            paintSelect.setPadding(0, width/20, 0, 0);
            paintSelect.setVerticalSpacing(width/20);
            paintSelect.setAdapter(colorsAdapter);

            paintButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(paintSelect.getParent() == null) {
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width/2, width/2);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                        params.addRule(RelativeLayout.ABOVE, R.id.button_bar);
                        drawLayout.addView(paintSelect, params);
                    }
                    else {
                        drawLayout.removeView(paintSelect);
                        drawSurface.invalidate();
                    }
                }
            });

            paintColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(paintSelect.getParent() == null) {
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width/2, width/2);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                        params.addRule(RelativeLayout.ABOVE, R.id.button_bar);
                        drawLayout.addView(paintSelect, params);
                    }
                    else {
                        drawLayout.removeView(paintSelect);
                        drawSurface.invalidate();
                    }
                }
            });

            ImageButton saveButton = (ImageButton) getView().findViewById(R.id.save_button);
            saveButton.setPadding(0, width/60, 0 , width/60);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setTitle("Save?");
                    builder.setMessage("Save this drawing?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            TextView recognizedView = (TextView) getView().findViewById(R.id.recognized_text_view);
                            File saveDir = getActivity().getFilesDir();
                            String fileName = "" + saveDir.listFiles().length;
                            String recognized = recognizedView.getText().toString();

                            DrawingSave toSave = new DrawingSave(drawSurface.strokeList, recognized);

                            try {
                                FileOutputStream saveFile = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
                                ObjectOutputStream saveObject = new ObjectOutputStream(saveFile);
                                saveObject.writeObject(toSave);
                                saveObject.close();
                                saveFile.close();

                                Toast.makeText(getActivity(), "Drawing Saved!", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                Toast.makeText(getActivity(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog saved = builder.create();
                    saved.show();
                }
            });

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((RecognizeView) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
