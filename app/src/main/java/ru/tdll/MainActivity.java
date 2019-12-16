package ru.tdll;

import android.app.*;
import android.content.*;
import android.nfc.*;
import android.nfc.tech.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
	
	final String LOG_TAG = "myLogs";
	final String LOG_TAG2 = "myLogs2";
	final String LOG_TAG3 = "myLogs3";
	
	
    final static int REQUEST_OPEN_DUMP = 1;
    final static String INTENT_READ_DUMP = "cc.troikadumper.INTENT_READ_DUMP";

    protected FloatingActionButton btnLoad;
    protected FloatingActionButton btnWrite;
	protected FloatingActionButton btnInfo;
	protected FloatingActionButton btnObnov;
    protected TextView info;
	protected String number;
	protected ProgressDialog pdI;
	protected ProgressDialog pdO;

    protected NfcAdapter nfcAdapter;
    protected Dump dump;
    protected boolean writeMode = false;
    protected ProgressDialog pendingWriteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.textView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		


		
        NfcManager nfcManager = (NfcManager)getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();
        if (nfcAdapter == null) {
            info.setText(R.string.error_no_nfc);
        }

        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            info.setText(R.string.error_nfc_is_disabled);
        }

		
		
        pendingWriteDialog = new ProgressDialog(MainActivity.this);
        pendingWriteDialog.setIndeterminate(true);
        pendingWriteDialog.setMessage("Приложите карту...");
        pendingWriteDialog.setCancelable(true);
        pendingWriteDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                writeMode = false;
            }
        });

        btnWrite = (FloatingActionButton) findViewById(R.id.btn_write);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeMode = true;
                pendingWriteDialog.show();
            }
        });
		
        btnLoad = (FloatingActionButton) findViewById(R.id.btn_load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DumpListActivity.class);
                startActivityForResult(intent, REQUEST_OPEN_DUMP);
            }
        });

		
		
        Intent startIntent = getIntent();
        if (startIntent != null && startIntent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            handleIntent(startIntent);
        }
		}
		
	
	private static long back_pressed;

	@Override
	public void onBackPressed() {
		if (back_pressed + 1000 > System.currentTimeMillis())
			super.onBackPressed();
		else
			Toast.makeText(getBaseContext(), "Дважды нажмите кнопку \"назад\" для выхода из приложения",
						   Toast.LENGTH_SHORT).show();
		back_pressed = System.currentTimeMillis();
	}
		
	
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        if (nfcAdapter != null) {
            setupForegroundDispatch((Activity) this, nfcAdapter);
        }
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        if (nfcAdapter != null) {
            stopForegroundDispatch(this, nfcAdapter);
        }

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current- activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OPEN_DUMP && resultCode == RESULT_OK) {
            handleIntent(data);
        }
    }

    private void handleIntent(Intent intent) {
        info.setText("");
        File dumpsDir = getApplicationContext().getExternalFilesDir(null);
        String action = intent.getAction();
        boolean shouldSave = false;
        try {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				//call patch
				tag = patchTag(tag);
				//
                if (writeMode && dump != null) {
                    pendingWriteDialog.hide();
                    info.append("Записано на карту...");
                    dump.write(tag);
                } else {
                    info.append("Прочитано с карты...");
                    dump = Dump.fromTag(tag);
                    shouldSave = true;
                }
            } else if (INTENT_READ_DUMP.equals(action)) {
                File file = new File(dumpsDir, intent.getStringExtra("filename"));
                info.append("Прочитано из файла...");
                dump = Dump.fromFile(file);
            }

			info.append("\nФормат данных: " + dump.getFormatCardAsString());
            info.append("\nНомер карты (UID): " + dump.getUidAsString());
           
            info.append("\n\n  --- Извлеченные данные: ---\n");
            info.append("\nНомер карты:      " + dump.getCardNumberAsString());
			number = dump.getCardNumberAsString2();
            info.append("\nБаланс карты:  " + dump.getBalanceAsString());
            
            if (shouldSave) {
                info.append("\n\n Дамп сохранен... ");
                File save = dump.save(dumpsDir);
                info.append("\n " + save.getCanonicalPath());
            }
            if (writeMode) {
                info.append("\n\n Дамп успешно записан!");
            }
        } catch (IOException e) {
            info.append("\nОшибка: \n" + e.toString());
            dump = null;
        } finally {
            if (writeMode) {
                writeMode = false;
            }
        }

        btnWrite.setVisibility( (dump == null) ? View.GONE : View.VISIBLE );
		//btnInfo.setVisibility( (dump == null) ? View.GONE : View.VISIBLE );
		}
		
    
	
	//patch
	public Tag patchTag(Tag oTag)
    {
        if (oTag == null) 
            return null;

        String[] sTechList = oTag.getTechList();

        Parcel oParcel, nParcel;

        oParcel = Parcel.obtain();
        oTag.writeToParcel(oParcel, 0);
        oParcel.setDataPosition(0);

        int len = oParcel.readInt();
        byte[] id = null;
        if (len >= 0)
        {
            id = new byte[len];
            oParcel.readByteArray(id);
        }
        int[] oTechList = new int[oParcel.readInt()];
        oParcel.readIntArray(oTechList);
        Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oParcel.readInt();
        int isMock = oParcel.readInt();
        IBinder tagService;
        if (isMock == 0)
        {
            tagService = oParcel.readStrongBinder();
        }
        else
        {
            tagService = null;
        }
        oParcel.recycle();

        int nfca_idx=-1;
        int mc_idx=-1;

        for(int idx = 0; idx < sTechList.length; idx++)
        {
            if(sTechList[idx].equals(NfcA.class.getName()))
            {
                nfca_idx = idx;
            }
            else if(sTechList[idx].equals(MifareClassic.class.getName()))
            {
                mc_idx = idx;
            }
        }

        if(nfca_idx>=0&&mc_idx>=0&&oTechExtras[mc_idx]==null)
        {
            oTechExtras[mc_idx] = oTechExtras[nfca_idx];
        }
        else
        {
            return oTag;
        }

        nParcel = Parcel.obtain();
        nParcel.writeInt(id.length);
        nParcel.writeByteArray(id);
        nParcel.writeInt(oTechList.length);
        nParcel.writeIntArray(oTechList);
        nParcel.writeTypedArray(oTechExtras,0);
        nParcel.writeInt(serviceHandle);
        nParcel.writeInt(isMock);
        if(isMock==0)
        {
            nParcel.writeStrongBinder(tagService);
        }
        nParcel.setDataPosition(0);

        Tag nTag = Tag.CREATOR.createFromParcel(nParcel);

        nParcel.recycle();

        return nTag;
    }
	//end patch

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{
                new String[] {MifareClassic.class.getName()}
        };

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
	

	

    
	
	
	
	
	
	}
	
	
