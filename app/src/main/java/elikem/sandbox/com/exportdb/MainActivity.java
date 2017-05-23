package elikem.sandbox.com.exportdb;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    TextView tableSize;
    Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        tableSize = (TextView) findViewById(R.id.table_size);
        int dbRows = getTableSize();
        tableSize.setText(String.valueOf(dbRows));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    TextView view  = (TextView) findViewById(R.id.table_size); // not important, created a random view just so i can pass it to the method
                    exportDB(view);

                }
               break;
            }
        }
    }


    public void addRowToDB(View view) {


        ContentValues values = new ContentValues();
        values.put("ROW_NUM", getTableSize() + 1);

        DatabaseHelper.getInstance(this).getWritableDatabase().insertOrThrow("SAMPLE", null, values);
        int dbSize = getTableSize();
        tableSize.setText(String.valueOf(dbSize));
    }

    public int getTableSize() {
        Cursor cursor = DatabaseHelper.getInstance(this).getReadableDatabase().rawQuery("SELECT * FROM SAMPLE",null);

        int result = cursor.getCount();
        cursor.close();

        return result;
    }

    public void openDB(View view) {
        Intent intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
    }

    public void exportDB(View view) {

        // check WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }

        else {

            Toast.makeText(this, "Starting Transfer...", Toast.LENGTH_SHORT).show();
            FileChannel source = null;
            FileChannel destination = null;


            String currentDBPath = this.getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath();
            File currentDB = new File(currentDBPath);


            File backupDBDir = Environment.getExternalStorageDirectory();
            File backupDBFile = null;


            try {
                boolean startTransfer = false;

                if(backupDBDir.exists()) {
                    backupDBFile = new File(backupDBDir.getAbsolutePath(), "backup_" + DatabaseHelper.DATABASE_NAME);
                    startTransfer = true;
                }
                else{
                    boolean successful = backupDBDir.mkdirs();

                    if(successful) {
                        backupDBFile  = new File(backupDBDir.getAbsolutePath(), "backup_" + DatabaseHelper.DATABASE_NAME);
                        startTransfer = backupDBFile.createNewFile();
                    }
                }


                if(startTransfer) {
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDBFile).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();

                    Toast.makeText(this, "Transfer Successful", Toast.LENGTH_SHORT).show();
                }

                else{
                    Toast.makeText(this, "Transfer Failed", Toast.LENGTH_SHORT).show();
                }


            }
            catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(this, "Transfer Failed", Toast.LENGTH_SHORT).show();
            }

        }


    }

}
