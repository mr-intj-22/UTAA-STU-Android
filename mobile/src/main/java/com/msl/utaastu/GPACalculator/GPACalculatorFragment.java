package com.msl.utaastu.GPACalculator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.Dialogs.CalculationsResultDialog;
import com.msl.utaastu.Interfaces.OnBackPressedListener;
import com.msl.utaastu.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.toptas.fancyshowcase.FancyShowCaseView;

import static android.app.Activity.RESULT_OK;
import static com.msl.utaastu.Firebase.FirebaseConstants.GPA_KEY;

/**
 * Created by Malek Shefat on 6/16/2017.
 * All rights preserved.
 */

public class GPACalculatorFragment extends Fragment implements OnBackPressedListener, ValueEventListener, GPACalculatorAdapter.EditedListener, View.OnClickListener {

    private final int REQUEST_CODE_PICK_PDF = 101;

    private FirebaseDatabase database = MyApplication.getDatabase();
    private FirebaseUser user = MyApplication.getUser();
    private DatabaseReference lectures;

    private RecyclerView recyclerView;
    private GPACalculatorAdapter adapter;

    private View empty;

    private View horizontal_pb;

    private List<GradeItem> grades = new ArrayList<>();
    private List<SemesterItem> semesters = new ArrayList<>();
    private List<String> grade_letters;
    private List<String> grade_values;
    private List<String> credit_values;

    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private LoadDataAsync loadDataAsync;
    private GetText getText;

    private String message = null;

    private boolean temp = false;
    private Uri fileUri;
    private FancyShowCaseView showCaseView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.calculator_fragment, container, false);

        if (getArguments() != null)
            temp = getArguments().getBoolean("isTemp", false);

        grade_letters = Arrays.asList(getResources().getStringArray(R.array.grade_letters));
        grade_values = Arrays.asList(getResources().getStringArray(R.array.grade_values));
        credit_values = Arrays.asList(getResources().getStringArray(R.array.credits));

        progressDialog = new ProgressDialog(getActivity());
        progressBar = root.findViewById(R.id.progressbar);

        FloatingActionButton load_fab = root.findViewById(R.id.fab_load);
        load_fab.setOnClickListener(this);
        horizontal_pb = root.findViewById(R.id.progressbar_horizontal);

        empty = root.findViewById(R.id.emptyView);
        Button add = root.findViewById(R.id.fill);
        add.setOnClickListener(this);

        lectures = database.getReference(GPA_KEY).child(user.getUid());

        recyclerView = root.findViewById(R.id.calculator_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(manager);
        adapter = new GPACalculatorAdapter(getActivity(), recyclerView);
        adapter.setEditedListener(this);
        adapter.setDataString(grade_letters, credit_values);
        recyclerView.setAdapter(adapter);

        if (temp) {
            load_fab.setVisibility(View.GONE);
            semesters.add(new SemesterItem().setCourses(6).setSemester("Temporary Calculations"));
            for (int i = 0; i < 6; i++)
                grades.add(new GradeItem().setCredit("1").setGrade("AA").setName("course " + (i + 1)));
            progressBar.setVisibility(View.GONE);
            adapter.setData(grades, semesters);
        } else
            lectures.addListenerForSingleValueEvent(this);

        showCaseView = new FancyShowCaseView.Builder(getActivity())
                .focusOn(load_fab)
                .backgroundColor(getResources().getColor(R.color.color_primary_trans))
                .title(getString(R.string.pick_gpa_pdf))
                .titleStyle(R.style.TipsTitleStyle, -1)
                .showOnce("pick_gpa_pdf")
                .build();
        showCaseView.show();

        setHasOptionsMenu(true);
        return root;
    }

    private void getData(DataSnapshot dataSnapshot) {
        for (DataSnapshot semester : dataSnapshot.getChildren()) {    // get the semesters one by one
            int items = 0;
            SemesterItem semesterItem = new SemesterItem();
            semesterItem.setSemester(semester.getKey().replace("-", ".").substring(2));
            for (DataSnapshot grade : semester.getChildren()) {    // get the grades one by one
                GradeItem item = grade.getValue(GradeItem.class);
                grades.add(item);
                items++;
            }
            semesterItem.setCourses(items);
            semesters.add(semesterItem);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calculator_menu, menu);
        MenuItem save_menu = menu.findItem(R.id.save);
        save_menu.setVisible(!temp);
        if (temp)
            menu.findItem(R.id.add_temp).setTitle(R.string.add_normal);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                uploadGrades();
                break;
            case R.id.calculation_result:
                if (grades.size() > 0)
                    showResults();
                break;
            case R.id.add_temp:
                Bundle args = new Bundle();
                args.putBoolean("isTemp", !temp);
                GPACalculatorFragment fragment = new GPACalculatorFragment();
                fragment.setArguments(args);
                showFragment(fragment, R.string.gpa_calc_temp);
                break;
            case R.id.add_semester:
                empty.setVisibility(View.GONE);
                adapter.addSemester();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadGrades() {
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.show();
        lectures.removeValue();
        int prev = 0;
        for (int i = 0; i < semesters.size(); i++) {
            if (semesters.get(i).getCourses() == 0)
                continue;
            for (int j = 0; j < semesters.get(i).getCourses(); j++) {
                DatabaseReference semester = database.getReference(GPA_KEY).child(user.getUid())
                        .child((i + 1) + " " + semesters.get(i).getSemester().replace(".", "-"))
                        .child(grades.get(j + prev).getName());
                semester.setValue(grades.get(j + prev));
            }
            prev += semesters.get(i).getCourses();
        }
        progressDialog.hide();
        adapter.setEdited(false);
        ((MainActivity) getActivity()).setOnBackPressedListener(null);
    }

    @Override
    public void doBack() {
        if (adapter.isEdited())
            showAlertDialog(getContext(), getString(R.string.unsaved_work), getString(R.string.changes_warning));
        else if (temp)
            showFragment(new GPACalculatorFragment(), R.string.gpa_calc);
    }

    public void showAlertDialog(Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                uploadGrades();

            }
        });
        // Setting cancel Button
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.discard_changes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                ((MainActivity) getActivity()).setOnBackPressedListener(null);
                adapter.setEdited(false);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showFragment(Fragment fragment, int title) {
        ((MainActivity) getActivity()).setOnBackPressedListener(null);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.container, fragment)
                .commit();
        animateTitleChange(title);
    }

    private void animateTitleChange(final int newTitle) {
        final View view = getToolbarTitle();

        if (view instanceof TextView) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    getActivity().setTitle(newTitle);

                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(250);
                    view.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(fadeOut);
        }
    }

    private View getToolbarTitle() {
        int childCount = ((MainActivity) getActivity()).getToolbar().getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = ((MainActivity) getActivity()).getToolbar().getChildAt(i);
            if (child instanceof TextView) {
                return child;
            }
        }

        return new View(getActivity());
    }

    private void stopAsyncTasks() {
        if (loadDataAsync != null && loadDataAsync.getStatus() == AsyncTask.Status.RUNNING)
            loadDataAsync.cancel(true);
        loadDataAsync = null;
        if (getText != null && getText.getStatus() == AsyncTask.Status.RUNNING)
            getText.cancel(true);
        getText = null;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() == 0) {
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            stopAsyncTasks();
            loadDataAsync = new LoadDataAsync();
            loadDataAsync.execute(dataSnapshot);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        empty.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onEdit() {
        ((MainActivity) getActivity()).setOnBackPressedListener(this);
    }

    @Override
    public void isEmpty() {
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fill:
                empty.setVisibility(View.GONE);
                adapter.addSemester();
                break;
            case R.id.fab_load:
                pickPDF();
                break;
        }
    }

    private class LoadDataAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            if (isCancelled())
                return null;
            getData(dataSnapshots[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled())
                return;
            progressBar.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            adapter.setData(grades, semesters);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }

    private void showResults() {
        float[] results = new float[semesters.size() + 1];
        List<String> failures = new ArrayList<>();
        results[0] = 0.0f;
        float credit = 0, grade = 0;
        int count = 0;
        for (int i = 0; i < semesters.size(); i++) {
            float numerator = 0, denominator = 0;
            for (int j = 0; j < semesters.get(i).getCourses(); j++) {
                numerator += (Float.valueOf(grade_values.get(grade_letters.indexOf(grades.get(j + count).getGrade())))
                        * Float.valueOf(grades.get(j + count).getCredit()));
                denominator += Float.valueOf(grades.get(j + count).getCredit());

                String name = grades.get(j + count).getName().replace("*", "").trim();
                String grade_letter = grades.get(j + count).getGrade();

                if (!failures.contains(name)) {
                    credit += Float.valueOf(grades.get(j + count).getCredit());
                }

                if (grade_letter.equalsIgnoreCase("FF") && !failures.contains(name)) {
                    failures.add(name);
                }
                grade += (Float.valueOf(grade_values.get(grade_letters.indexOf(grades.get(j + count).getGrade())))
                        * Float.valueOf(grades.get(j + count).getCredit()));
            }
            results[i + 1] = roundTwoDecimals(numerator / denominator);
            count += semesters.get(i).getCourses();
        }
        FragmentManager manager = getActivity().getSupportFragmentManager();
        CalculationsResultDialog calculationsResultDialog = new CalculationsResultDialog();
        calculationsResultDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
        calculationsResultDialog.setTargetFragment(this, 0);
        Bundle args = new Bundle();
        args.putFloatArray("results", results);
        args.putFloat("total", roundTwoDecimals(grade / credit));
        calculationsResultDialog.setArguments(args);
        calculationsResultDialog.show(manager, "result_dialog");
    }

    private float roundTwoDecimals(float num) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat twoDecForm = (DecimalFormat) nf;
        twoDecForm.applyPattern("#.##");
        return Float.valueOf(twoDecForm.format(num));
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public List<String> stripText(InputStream pdf, boolean b) {
        String parsedText = "";
        try {
            PdfReader reader = new PdfReader(pdf);
            int n = reader.getNumberOfPages();
            for (int i = 0; i < n; i++) {
                if (b) {
                    parsedText = parsedText + PdfTextExtractor.getTextFromPage(reader, i + 1, new SimpleTextExtractionStrategy()).trim() + "\n"; //Extracting the content from the different pages
                } else
                    parsedText = parsedText + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n"; //Extracting the content from the different pages
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(parsedText.split(System.getProperty("line.separator")));
    }

    private class GetText extends AsyncTask<InputStream, Void, Boolean> {

        int current = 0;

        @Override
        protected Boolean doInBackground(InputStream... params) {
            try {
                return loadData(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                message = getString(R.string.was_not_extracted);
                if (current == 0) {
                    current++;
                    if (fileUri != null) {
                        InputStream inputStream = getIO(fileUri);
                        return inputStream != null && loadDataII(inputStream);
                    }
                }
            }
            return false;
        }

        /* With SimpleTextExtractionStrategy */
        private boolean loadData(InputStream pdf) {
            List<GradeItem> gradeItemsTemp = new ArrayList<>();
            List<SemesterItem> semesterItemsTemp = new ArrayList<>();

            List<String> text = stripText(pdf, true);
            int count = 0;
            boolean isSemester = false, isCourse = false;
            SemesterItem semesterItem = null;
            for (int i = 1; i < text.size(); i++) {
                if ((text.get(i).equals("Kredi") && text.get(i - 1).equals("THKÜ")) ||
                        (text.get(i).equals("Credit") && text.get(i - 1).equals("UTAA"))) {  //  after this comes the name
                    isSemester = true;
                } else if (isSemester) {
                    count = 0;
                    semesterItem = new SemesterItem();
                    semesterItem.setSemester(text.get(i));
                    semesterItemsTemp.add(semesterItem);
                    isSemester = false;
                } else if (text.get(i).contains("Instead") || text.get(i).contains("Kredi")) {
                    isCourse = true;
                } else if (isCourse) {
                    if (text.get(i).contains("Semester Totals") ||
                            text.get(i).contains("Dönem Toplamları")) {   //  courses ends here
                        isCourse = false;
                        if (semesterItem != null) {
                            if (count == 0) {
                                if (fileUri != null) {
                                    InputStream inputStream = getIO(fileUri);
                                    return inputStream != null && loadDataII(inputStream);
                                }
                            }
                            semesterItem.setCourses(count);
                        }
                        continue;
                    }
                    if (text.get(i).contains("*")) {   //  repeated course, update origin
                        List<String> course = Arrays.asList(text.get(i).split(" "));
                        String name = course.get(0) + " " + course.get(1);
                        String grade = course.get(course.size() - 4);
                        String stars = course.get(course.size() - 1);
                        boolean upgraded = updateGrade(gradeItemsTemp, name, grade, stars);
                        if (!upgraded) {
                            GradeItem item = new GradeItem();
                            String credit = course.get(course.size() - 2);
                            item.setName(stars + " " + name);
                            item.setCredit(credit);
                            if (!grade_letters.contains(grade))
                                continue;
                            item.setGrade(grade);
                            gradeItemsTemp.add(item);
                            count++;
                        }
                        continue;
                    }
                    List<String> course = Arrays.asList(text.get(i).trim().split(" "));
                    if (course.size() == 1)
                        continue;
                    GradeItem item = new GradeItem();
                    item.setName(course.get(0) + " " + course.get(1));
                    String credit = course.get(course.size() - 1);
                    item.setCredit(credit);
                    String grade = course.get(course.size() - 3);
                    if (!grade_letters.contains(grade))
                        continue;
                    item.setGrade(grade);
                    gradeItemsTemp.add(item);
                    count++;
                }
            }

            //  remove empty items
            for (SemesterItem item : semesterItemsTemp)
                if (item.getCourses() == 0) semesterItemsTemp.remove(item);

            if (semesterItemsTemp.size() > 0 && gradeItemsTemp.size() > 0) {
                semesters = semesterItemsTemp;
                grades = gradeItemsTemp;
                message = getString(R.string.extracted_successfully);
                return semesterItemsTemp.size() > 0 && gradeItemsTemp.size() > 0;
            } else {
                current++;
                if (fileUri != null) {
                    InputStream inputStream = getIO(fileUri);
                    return inputStream != null && loadDataII(inputStream);
                }
            }
            return false;
        }

        /* Without SimpleTextExtractionStrategy */
        private boolean loadDataII(InputStream pdf) {
            List<GradeItem> gradeItemsTemp = new ArrayList<>();
            List<SemesterItem> semesterItemsTemp = new ArrayList<>();

            List<String> text = stripText(pdf, false);
            int count = 0;
            boolean isSemester = false, isCourse = false;
            SemesterItem semesterItem = null;
            for (int i = 1; i < text.size(); i++) {
                if (text.get(i).contains("PROFICIENCY") || text.get(i).contains("Grand Totals") ||
                        text.get(i).contains("Genel Toplamlar")) {  //  after this comes the name

                    isSemester = true;
                } else if (isSemester) {
                    count = 0;
                    semesterItem = new SemesterItem();
                    semesterItem.setSemester(text.get(i));
                    semesterItemsTemp.add(semesterItem);
                    isSemester = false;
                } else if (text.get(i).contains("Kredi Kredi") || text.get(i).contains("Instead")) {
                    isCourse = true;
                } else if (isCourse) {
                    if (text.get(i).contains("Semester Totals") ||
                            text.get(i).contains("Dönem Toplamları")) {   //  courses ends here
                        isCourse = false;
                        if (semesterItem != null) {
                            semesterItem.setCourses(count);
                        }
                        continue;
                    }
                    if (text.get(i).contains("*")) {   //  repeated course, update origin
                        List<String> course = Arrays.asList(text.get(i).split(" "));
                        String name = course.get(1) + " " + course.get(2);
                        String grade = course.get(course.size() - 4);
                        String stars = course.get(0);
                        boolean upgraded = updateGrade(gradeItemsTemp, name, grade, stars);
                        if (!upgraded) {
                            GradeItem item = new GradeItem();
                            String credit = course.get(course.size() - 2);
                            item.setName(stars + " " + name);
                            item.setCredit(credit);
                            if (!grade_letters.contains(grade))
                                continue;
                            item.setGrade(grade);
                            gradeItemsTemp.add(item);
                            count++;
                        }
                        continue;
                    }
                    List<String> course = Arrays.asList(text.get(i).trim().split(" "));
                    if (course.size() == 1)
                        continue;
                    GradeItem item = new GradeItem();
                    item.setName(course.get(0) + " " + course.get(1));
                    String credit = course.get(course.size() - 2);
                    item.setCredit(credit);
                    String grade = course.get(course.size() - 4);
                    if (!grade_letters.contains(grade))
                        continue;
                    item.setGrade(grade);
                    gradeItemsTemp.add(item);
                    count++;
                }
            }

            //  remove empty items
            for (SemesterItem item : semesterItemsTemp)
                if (item.getCourses() == 0) semesterItemsTemp.remove(item);

            if (semesterItemsTemp.size() > 0 && gradeItemsTemp.size() > 0) {
                semesters = semesterItemsTemp;
                grades = gradeItemsTemp;
                message = getString(R.string.extracted_successfully);
            } else {
                message = getString(R.string.was_not_extracted);
            }

            return semesterItemsTemp.size() > 0 && gradeItemsTemp.size() > 0;
        }

        private boolean updateGrade(List<GradeItem> temp, String name, String grade, String stars) {
            for (GradeItem item : temp) {
                if (item.getName().replace("*", "").trim().equalsIgnoreCase(name)
                        && !item.getGrade().equalsIgnoreCase("FF")) {
                    item.setGrade(grade);
                    item.setName(stars + " " + name);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean mBoolean) {
            super.onPostExecute(mBoolean);
            if (mBoolean) {
                adapter = new GPACalculatorAdapter(getActivity(), recyclerView);
                adapter.setEditedListener(GPACalculatorFragment.this);
                recyclerView.setAdapter(adapter);
                adapter.setDataString(grade_letters, credit_values);
                adapter.setData(grades, semesters);
                empty.setVisibility(View.GONE);
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            horizontal_pb.setVisibility(View.GONE);
        }
    }

    private void pickPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE_PICK_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_PDF:
                if (resultCode == RESULT_OK) {
                    horizontal_pb.setVisibility(View.VISIBLE);
                    fileUri = data.getData();
                    startAsync(fileUri);
                }
                break;
        }
    }

    private void startAsync(Uri uri) {
        try {
            String path = uri.getPath();
            File pdf = new File(path);
            FileInputStream pdfInputStream = new FileInputStream(pdf);
            stopAsyncTasks();
            getText = new GetText();
            getText.execute(pdfInputStream);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            stopAsyncTasks();
            getText = new GetText();
            getText.execute(getActivity().getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            message = getString(R.string.not_supported);
            horizontal_pb.setVisibility(View.GONE);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    private InputStream getIO(Uri uri) {
        try {
            String path = uri.getPath();
            File pdf = new File(path);
            return new FileInputStream(pdf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            return getActivity().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            message = getString(R.string.not_supported);
            horizontal_pb.setVisibility(View.GONE);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            return null;
        }
    }
}
