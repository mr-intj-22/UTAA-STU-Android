package com.msl.utaastu.AcademicCalendar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.msl.utaastu.Firebase.FirebaseConstants.LAST_UPDATE_KEY;

/**
 * Created by Malek Shefat on 7/15/2017.
 * All rights preserved.
 */

public class AcademicCalendarActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    private final String FILE_NAME = "academic_calendar";
    private final int TITLE = 0;
    private final int ITEM = 1;

    private FirebaseStorage storage = MyApplication.getStorage();
    private StorageReference tr_ref = storage.getReference().child("academic_calendar/academic_calendar_tr.pdf");
    private StorageReference en_ref = storage.getReference().child("academic_calendar/academic_calendar_en.pdf");
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference last_update = database.getReference().child(LAST_UPDATE_KEY);

    private GetText getText;
    private String message;

    private List<CalendarItem> items = new ArrayList<>();

    private RecyclerView recyclerView;
    private AcademicCalendarAdapter adapter;
    private ProgressBar progress;
    private View empty;
    private Button download;

    private File rootPath, localFile;
    private MenuItem update_pdf_menu;
    private int lastVersion = 0;

    private boolean needs_update = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.academic_calendar_activity);

        rootPath = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getPackageName(), FILE_NAME);
        localFile = new File(rootPath, FILE_NAME + ".pdf");
        lastVersion = MyApplication.getLastPdf();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AcademicCalendarAdapter(this);
        progress = findViewById(R.id.progressbar_horizontal);
        download = findViewById(R.id.download);
        download.setOnClickListener(this);
        empty = findViewById(R.id.emptyView);

        if (localFile.exists()) {
            download.setEnabled(false);
            download.setText(getString(R.string.file_exists));
            getText = new GetText();
            getText.execute(localFile);
            progress.setVisibility(View.VISIBLE);
            last_update.addValueEventListener(this);
        }
    }

    private void downloadFile(int lang) {
        progress.setVisibility(View.VISIBLE);
        StorageReference reference = tr_ref;

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        if (localFile == null)
            localFile = new File(rootPath, FILE_NAME + ".pdf");

        reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                download.setEnabled(false);
                getText = new GetText();
                getText.execute(localFile);
                if (update_pdf_menu != null) update_pdf_menu.setVisible(false);
                MyApplication.setLastPdf(lastVersion);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progress.setVisibility(View.GONE);
                Log.d("UTAA-2", exception.getMessage());
            }
        });

    }

    private void stopAsyncTasks() {
        if (getText != null && getText.getStatus() == AsyncTask.Status.RUNNING)
            getText.cancel(true);
        getText = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download:
                downloadFile(0);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.academic_class_menu, menu);
        update_pdf_menu = menu.findItem(R.id.update_pdf);
        update_pdf_menu.setVisible(needs_update);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.update_pdf:
                downloadFile(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        int newVersion = dataSnapshot.getValue(Integer.class);

        if (needsUpdate(lastVersion, newVersion)) {
            if (update_pdf_menu != null)
                update_pdf_menu.setVisible(true);
            Toast.makeText(this, R.string.update_pdf, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        if (update_pdf_menu != null)
            update_pdf_menu.setVisible(false);
    }

    private boolean needsUpdate(int last_version, int new_version) {
        return new_version > last_version;
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public List<String> stripText(InputStream pdf) {
        String parsedText = "";
        try {
            PdfReader reader = new PdfReader(pdf);
            int n = reader.getNumberOfPages();
            for (int i = 0; i < n; i++) {
                parsedText = parsedText + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n"; //Extracting the content from the different pages
            }
            System.out.println(parsedText);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modifyList(Arrays.asList(parsedText.split(System.getProperty("line.separator"))));
    }

    private List<String> modifyList(List<String> list) {
        List<String> modified = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (!Character.isDigit(list.get(i).charAt(0)) && i > 0 &&
                    !list.get(i).contains("DÖNEMİ") && !list.get(i).contains("ÖĞRETİMİ")) {
                modified.set(modified.size() - 1, (modified.get(modified.size() - 1) + " " + list.get(i)).trim());
            } else {
                modified.add(list.get(i).trim());
            }
        }

        return modified;
    }

    private class GetText extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {

            if (isCancelled())
                return null;

            try {
                loadData(new FileInputStream(params[0]));
            } catch (Exception e) {
                e.printStackTrace();
                message = getString(R.string.was_not_extracted);
            }
            return null;
        }

        private void loadData(InputStream pdf) {

            List<CalendarItem> itemsTemp = new ArrayList<>();

            List<String> src = stripText(pdf);

            CalendarItem title;

            for (int i = 2; i < src.size(); i++) { //  get Schedule line by line
                if (TextUtils.isEmpty(src.get(i)) || src.get(i).trim().length() == 0) {
                    continue;
                }
                String[] line = src.get(i).split(" ");
                if (line.length == 2) {  //  section / donem
                    title = new CalendarItem().setTitle(src.get(i)).setType(TITLE);
                    itemsTemp.add(title);
                } else {
                    line = src.get(i).split("–", 2);
                    String[] first = line[0].split(" ");
                    String last = src.get(i);
                    if (first.length < 7) {
                        last = line[line.length - 1];
                    }
                    String rest[] = (Character.isWhitespace(last.charAt(0)) ? last.substring(1, last.length()) : last).split(" ");
                    String date_prev = "";
                    if (line.length == 2 && first.length < 7) {  // we have '-'
                        date_prev = arrToString(line[0].split(" "), " ") + " - ";
                    }
                    String[] dates = Arrays.copyOfRange(rest, 0, 3);
                    String date = date_prev + arrToString(dates, " ");
                    String[] events = Arrays.copyOfRange(rest, 3, rest.length);
                    String event = arrToString(events, " ");
                    CalendarItem item = new CalendarItem().setDate(date).setEvent(event).setType(ITEM);
                    itemsTemp.add(item);
                }
            }
            if (itemsTemp.size() > 0) {
                items = itemsTemp;
                message = getString(R.string.calendar_extracted_successfully);
            }
        }

        private String arrToString(String[] array, String delimiter) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (array[i].trim().length() > 0 && !TextUtils.isEmpty(array[i])) {
                    if (i > 0) {
                        sb.append(delimiter);
                    }
                    String item = array[i];
                    sb.append(item);
                }
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            super.onPostExecute(mVoid);
            if (items.size() > 0) {
                empty.setVisibility(View.GONE);
                adapter = new AcademicCalendarAdapter(AcademicCalendarActivity.this);
                adapter.setData(items);
                recyclerView.setAdapter(adapter);
            }
            Toast.makeText(AcademicCalendarActivity.this, message, Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }
}
