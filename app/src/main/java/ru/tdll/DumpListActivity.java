package ru.tdll;

import android.app.AlertDialog;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import android.content.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DumpListActivity extends AppCompatActivity {
	
	AlertDialog.Builder ad;
	Context context;
	protected String dumpd;
	protected File dumpsDir;
	protected String selectedFilename;
	protected String title;
	protected String message;
	protected String button1String;
	protected String button2String;
    protected Toolbar toolbar;
    protected ListView dumpListView;
    protected ArrayAdapter<DumpListAdapter.DumpListFilename> dumpListAdapter;
	
	
   @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump_list);
		/*context = DumpListActivity.this;
		title = "Действие с дампом";
        message = "Удалить или открыть?";
        button1String = "Открыть";
        button2String = "Удалить";
		
		ad = new AlertDialog.Builder(context);
		ad.setTitle(title);  // заголовок
		ad.setMessage(message); // сообщение
		ad.setPositiveButton(button1String, new OnClickListener() {

				@Override

				public void onClick(DialogInterface dialog, int arg1) {
					Intent intent = new Intent(MainActivity.INTENT_READ_DUMP);
					intent.putExtra("filename", selectedFilename);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		ad.setNegativeButton(button2String, new OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					File file = new File("/sdcard/Android/data/cc.troikadumper/files/" + selectedFilename);
					if(file.delete()){
						Toast.makeText(context, "Дамп удален",
									   Toast.LENGTH_LONG).show();
					   Intent intent = new Intent(DumpListActivity.this, DumpListActivity.class);
					   startActivity(intent);
									  finish();
					}else Toast.makeText(context, "Нет такого дампа или нет доступа к дампу",
										 Toast.LENGTH_LONG).show();
				}
			});
		ad.setCancelable(true);
		ad.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					Toast.makeText(context, "Выбор отменен",
								   Toast.LENGTH_LONG).show();
				}
			}); */

        File dumpsDir = getApplicationContext().getExternalFilesDir(null);
        String[] filenames = dumpsDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.matches(Dump.FILENAME_REGEXP);
            }
        });

        // setup toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.dumplist_title);
            toolbar.setSubtitle(R.string.dumplist_subtitle);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // setup list
        dumpListView = (ListView) findViewById(R.id.dumpListView);
        dumpListAdapter = new DumpListAdapter(getApplicationContext(), filenames);
        dumpListView.setAdapter(dumpListAdapter);
        dumpListView.setClickable(true);
        dumpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				context = DumpListActivity.this;
				title = "Действие с дампом";
				message = "Удалить или открыть?";
				button1String = "Открыть";
				button2String = "Удалить";

				ad = new AlertDialog.Builder(context);
				ad.setTitle(title);  // заголовок
				ad.setMessage(message); // сообщение
				ad.setPositiveButton(button1String, new OnClickListener() {

						@Override

						public void onClick(DialogInterface dialog, int arg1) {
							Intent intent = new Intent(MainActivity.INTENT_READ_DUMP);
							intent.putExtra("filename", selectedFilename);
							setResult(RESULT_OK, intent);
							finish();
						}
					});
				ad.setNegativeButton(button2String, new OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							File file = new File("/sdcard/Android/data/ru.tdll/files/" + selectedFilename);
							if(file.delete()){
								Toast.makeText(context, "Дамп удален",
											   Toast.LENGTH_LONG).show();
								Intent intent = new Intent(DumpListActivity.this, DumpListActivity.class);
								startActivity(intent);
								finish();
							}else Toast.makeText(context, "Нет такого дампа или нет доступа к дампу",
												 Toast.LENGTH_LONG).show();
						}
					});
				ad.setCancelable(true);
				ad.setOnCancelListener(new OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							Toast.makeText(context, "Выбор отменен",
										   Toast.LENGTH_LONG).show();
						}
					}); 
				
				
                DumpListAdapter.DumpListFilename filename = (DumpListAdapter.DumpListFilename) dumpListView.getItemAtPosition(position);
               selectedFilename = filename.getFilename();
				ad.show();
				
			   //Intent intent = new Intent(MainActivity.INTENT_READ_DUMP);
			   //intent.putExtra("filename", selectedFilename);
			   //setResult(RESULT_OK, intent);
			   //finish();
           } 
        });
    }
}
