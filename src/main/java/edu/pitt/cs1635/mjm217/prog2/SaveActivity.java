package edu.pitt.cs1635.mjm217.prog2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SaveActivity extends ListActivity {
    static int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_save);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels/3;

        //create listView of saved files found in internal directory
        final File saveDir = getFilesDir();
        final ArrayList<RecognizeView.DrawingSave> drawingsArray = new ArrayList<RecognizeView.DrawingSave>();
        final HashMap<Integer, File> filesMap = new HashMap<Integer, File>();

        int i = 0;
        for(File file : saveDir.listFiles()) {
            filesMap.put(i, file);
            i++;
            try {
                FileInputStream input = openFileInput(file.getName());
                ObjectInputStream objectInput = new ObjectInputStream(input);
                RecognizeView.DrawingSave savedDrawing = (RecognizeView.DrawingSave) objectInput.readObject();
                drawingsArray.add(savedDrawing);
                objectInput.close();
                input.close();
            }
            catch(Exception e) {

            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!sp.contains("learned_delete")) {
            Toast.makeText(getApplicationContext(), "Longpress item for more options", Toast.LENGTH_LONG).show();
            sp.edit().putBoolean("learned_delete", true).apply();
        }
        else if(filesMap.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Saved Drawings", Toast.LENGTH_SHORT).show();
        }

        final SavedFilesAdapter savedAdapter = new SavedFilesAdapter(this, width, drawingsArray);
        getListView().setAdapter(savedAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            int numberSelected;

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (b) {
                    numberSelected++;
                    savedAdapter.setNewSelection(i, b);
                } else {
                    numberSelected--;
                    savedAdapter.removeSelection(i);
                }
                actionMode.setTitle(numberSelected + " selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                numberSelected = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.delete_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Set<Integer> positions = savedAdapter.getCurrentCheckedPosition();
                File file;

                switch (menuItem.getItemId()) {
                    case R.id.item_delete:

                        for (Integer integer : positions) {
                            file = filesMap.get(integer);
                            boolean deleted = file.delete();
                        }
                        filesMap.clear();
                        drawingsArray.clear();

                        int i = 0;
                        for(File tempFile : saveDir.listFiles()) {
                            filesMap.put(i, tempFile);
                            i++;
                            try {
                                FileInputStream input = openFileInput(tempFile.getName());
                                ObjectInputStream objectInput = new ObjectInputStream(input);
                                RecognizeView.DrawingSave savedDrawing = (RecognizeView.DrawingSave) objectInput.readObject();
                                drawingsArray.add(savedDrawing);
                                objectInput.close();
                                input.close();
                            }
                            catch(Exception e) {

                            }
                        }

                        savedAdapter.notifyDataSetChanged();
                        actionMode.finish();
                        Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.item_delete_all:
                        for (Integer integer : filesMap.keySet()) {
                            file = filesMap.get(integer);
                            boolean deleted = file.delete();
                        }
                        filesMap.clear();
                        drawingsArray.clear();
                        savedAdapter.notifyDataSetChanged();
                        actionMode.finish();
                        Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                savedAdapter.clearSelection();
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SaveActivity.this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("Load?");
                builder.setMessage("Open this drawing?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent result = new Intent();
                        RecognizeView.DrawingSave savedDrawing = drawingsArray.get(position);
                        result.putExtra("drawing", savedDrawing);
                        SaveActivity.this.setResult(Activity.RESULT_OK, result);
                        SaveActivity.this.finish();
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

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                getListView().setItemChecked(i, !savedAdapter.isPositionChecked(i));
                return false;
            }
        });
    }
}
