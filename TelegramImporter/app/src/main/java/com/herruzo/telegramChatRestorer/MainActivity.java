package com.herruzo.telegramChatRestorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.herruzo.telegramChatRestorer.R;
import com.herruzo.telegramChatRestorer.databinding.ActivityMainBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {

    private ActivityMainBinding binding;
    Handler handler = new Handler();
    ProgressBar bar;
    TextView info;
    Spinner pack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bar = (ProgressBar) findViewById(R.id.progressBar2);
        info = (TextView) findViewById(R.id.textInfo);
        pack = (Spinner) findViewById(R.id.packName);
        pack.setOnItemSelectedListener(this);

        updateSpinner();


    }

    private void updateSpinner(){
        ArrayList<String> arr = new ArrayList<>();

        for(File dir : getFilesDir().listFiles() ){
            arr.add(dir.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,      arr   );
        pack.setAdapter(adapter);
    }
    // Request code for selecting a PDF document.
    private static final int PICK_ZIP_FILE = 1;
    private static final int SEND_TO_TELEGRAM = 2;

    private String selectedPack;
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        selectedPack = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void directSend(View view) {

        File dir = getFilesDir();
        sendImport(new File(dir + "/" + selectedPack));

        bar.setVisibility(View.VISIBLE);
        CountDownTimer timer = new CountDownTimer(270000, 1000) {

            public void onTick(long millisUntilFinished) {
                long totalSecs = millisUntilFinished / 1000;
                int hours = (int) totalSecs / 3600;
                int minutes = (int) (totalSecs % 3600) / 60;
                int seconds = (int) totalSecs % 60;

                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                info.setText(timeString);
            }

            public void onFinish() {
                bar.setVisibility(View.INVISIBLE);
                info.setText("");
            }
        }.start();
    }

    public void importZip(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        startActivityForResult(intent, PICK_ZIP_FILE);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP_FILE) {
            File dir = getFilesDir();


            bar.setVisibility(View.VISIBLE);
            info.setText("Unzipping...");
            new Thread(() -> {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    unzip(in, dir);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    public void run() {
                        bar.setVisibility(View.INVISIBLE);
                        info.setText("");
                        updateSpinner();
                    }
                });
            }).start();

        }
    }
    public void sendImports(View view) {
        sendImports();
    }

    private void sendImports() {
        File dir = getFilesDir();

        new Thread(() -> {
            int num_packs = dir.listFiles().length;
            for (int i = 1; i <= num_packs; i++) {
                File subdir = new File(dir + "/pack" + (num_packs - i));
                Log.e("FOLDER:", subdir.toString());

                sendImport(subdir);

                bar.setVisibility(View.VISIBLE);
                CountDownTimer timer = new CountDownTimer(270000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        long totalSecs = millisUntilFinished / 1000;
                        int hours = (int) totalSecs / 3600;
                        int minutes = (int) (totalSecs % 3600) / 60;
                        int seconds = (int) totalSecs % 60;

                        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        info.setText(timeString);
                    }

                    public void onFinish() {
                        info.setText("");
                    }
                }.start();
                try {
                    timer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }
        }).start();

    }

    private void sendImport(File dir) {

        ArrayList<Uri> urisForTelegram = new ArrayList<>();

        for (File f : Objects.requireNonNull(dir.listFiles())) {
            urisForTelegram.add(FileProvider.getUriForFile(getApplicationContext(), "com.herruzo.telegramChatRestorer.provider", f)); // Add your image URIs here
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, urisForTelegram);
        shareIntent.setType("text/*");
        shareIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(shareIntent, "Share images to.."),SEND_TO_TELEGRAM);
    }


    public static void unzip(InputStream zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(zipFile));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

    private void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

}

