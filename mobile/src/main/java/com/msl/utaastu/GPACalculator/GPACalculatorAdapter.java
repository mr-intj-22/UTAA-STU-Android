package com.msl.utaastu.GPACalculator;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

public class GPACalculatorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int SEMESTER = 1;
    private final int CALC_HEADER = 2;
    private final int ITEM = 3;
    private final int BUTTON = 5;

    private RecyclerView rv;
    private List<String> grade_letters = new ArrayList<>();
    private List<String> credit_values = new ArrayList<>();
    private List<GradeItem> gradeItems = new ArrayList<>();
    private List<SemesterItem> semesterItems = new ArrayList<>();
    private Context c;
    private LayoutInflater inflater;
    private int prev, prev_older, semester;  //  semester start from 1;

    private boolean edited = false;
    private EditedListener editedListener;

    public GPACalculatorAdapter(Context c, RecyclerView rv) {
        this.c = c;
        this.rv = rv;
        inflater = LayoutInflater.from(c);
    }

    public void setDataString(List<String> grades, List<String> credits) {
        this.grade_letters = grades;
        this.credit_values = credits;
    }

    public void setData(List<GradeItem> gradeItems, List<SemesterItem> semesterItems) {
        this.gradeItems = gradeItems;
        this.semesterItems = semesterItems;
        notifyItemRangeChanged(0, gradeItems.size() + semesterItems.size() * 3);
    }

    @Override
    public int getItemViewType(int position) {
        setSemester(position);

        if (isSemester(position)) {
            return SEMESTER;
        } else if (isHeader(position)) {
            return CALC_HEADER;
        } else if (isButton(position)) {
            return BUTTON;
        }
        return ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case SEMESTER:
                return new SemesterHolder(inflater.inflate(R.layout.calculator_semester, parent, false));
            case CALC_HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.calculator_header, parent, false));
            case BUTTON:
                return new ButtonHolder(inflater.inflate(R.layout.calculator_item_add, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.calculator_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SemesterHolder) {   // here's our day
            ((SemesterHolder) holder).semester.setText(semesterItems.get(semester - 1).getSemester());
        } else if (holder instanceof HeaderHolder) {  // here's our colorful header

        } else if (holder instanceof ItemHolder) {   // here's our item
            int pos = position - (semester * 3 - 1);
            int grade_pos = grade_letters.indexOf(gradeItems.get(pos).getGrade());
            int credit_pos = credit_values.indexOf(gradeItems.get(pos).getCredit());
            ((ItemHolder) holder).grades.setSelection(grade_pos >= 0 ? grade_pos : 0);
            ((ItemHolder) holder).credits.setSelection(credit_pos >= 0 ? credit_pos : 0);
            ((ItemHolder) holder).name.setText(gradeItems.get(pos).getName());
        } else {  // here's our add button

        }
    }

    @Override
    public int getItemCount() {
        return gradeItems.size() + semesterItems.size() * 3;
    }

    // View Holders

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatEditText name;
        AppCompatSpinner credits, grades;
        View remove;

        public ItemHolder(View itemView) {
            super(itemView);
            name = (AppCompatEditText) itemView.findViewById(R.id.name);
            credits = (AppCompatSpinner) itemView.findViewById(R.id.credits);
            grades = (AppCompatSpinner) itemView.findViewById(R.id.grades);
            name.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        name.setText(name.getText().delete(start, start + count));
                        name.setSelection(name.getText().length());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int semester = getSemester(getAdapterPosition());
                    int pos = getAdapterPosition() - (semester * 3 - 1);
                    gradeItems.get(pos).setName(s.toString());
                }
            });
            remove = itemView.findViewById(R.id.remove);
            remove.setOnClickListener(this);
            credits.setAdapter(getAdapter(R.array.credits));
            credits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int semester = getSemester(getAdapterPosition());
                    int data_position = getAdapterPosition() - (semester * 3 - 1);
                    gradeItems.get(data_position).setCredit(credit_values.get(position));
                    onEdit();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            grades.setAdapter(getAdapter(R.array.grade_letters));
            grades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int semester = getSemester(getAdapterPosition());
                    int data_position = getAdapterPosition() - (semester * 3 - 1);
                    gradeItems.get(data_position).setGrade(grade_letters.get(position));
                    onEdit();
                    if (position == grade_letters.size() - 1) {
                        ((TextView) credits.getSelectedView()).setTextColor(Color.RED);
                        ((TextView) grades.getSelectedView()).setTextColor(Color.RED);
                        name.setTextColor(Color.RED);
                        name.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_gpa_calculator_text_red));
                    } else {
                        ((TextView) credits.getSelectedView()).setTextColor(ContextCompat.getColor(c, R.color.primary_text));
                        ((TextView) grades.getSelectedView()).setTextColor(ContextCompat.getColor(c, R.color.primary_text));
                        name.setTextColor(ContextCompat.getColor(c, R.color.primary_text));
                        name.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_gpa_calculator_text));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        private ArrayAdapter<CharSequence> getAdapter(int array) {
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c,
                    array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            return adapter;
        }

        @Override
        public void onClick(View v) {
            removeItem(getAdapterPosition(), rv);
        }
    }

    private class SemesterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AppCompatEditText semester;
        private View remove;

        public SemesterHolder(View itemView) {
            super(itemView);
            semester = (AppCompatEditText) itemView.findViewById(R.id.calculator_semester);
            remove = itemView.findViewById(R.id.remove);
            semester.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]")) {
                        semester.setText(semester.getText().delete(start, start + count));
                        semester.setSelection(semester.getText().length());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    semesterItems.get(GPACalculatorAdapter.this.semester - 1).setSemester(s.toString());
                }
            });
            semester.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            remove.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            removeSemester(getAdapterPosition());
        }
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    private class ButtonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button add;

        public ButtonHolder(View itemView) {
            super(itemView);
            add = (Button) itemView.findViewById(R.id.add);
            add.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            addItem(getAdapterPosition());
        }
    }

    // Calculations

    private void setSemester(int pos) {
        prev = 0;
        semester = 0;
        boolean found = false;
        while (!found && semester < semesterItems.size()) {
            semester++;
            found = (pos >= ((semester - 1) * 3 + prev) &&
                    pos < (semester * 3 + prev + semesterItems.get(semester - 1).getCourses()));
            prev_older = prev;
            prev += semesterItems.get(semester - 1).getCourses();
        }
    }

    private int getSemester(int pos) {
        int prev = 0;
        int semester = 0;
        boolean found = false;
        while (!found && semester < semesterItems.size()) {
            semester++;
            found = (pos >= ((semester - 1) * 3 + prev) &&
                    pos < (semester * 3 + prev + semesterItems.get(semester - 1).getCourses()));
            prev_older = prev;
            prev += semesterItems.get(semester - 1).getCourses();
        }
        return semester;
    }

    private boolean isSemester(int pos) {
        return pos == ((semester - 1) * 3 + prev_older);
    }

    private boolean isHeader(int pos) {
        return pos == ((semester - 1) * 3 + 1 + prev_older);
    }

    private boolean isButton(int pos) {
        return pos == (semester * 3 + prev - 1);
    }

    //  add / remove

    private void addItem(int pos) {
        onEdit();
        int semester = getSemester(pos);
        int position = pos - (semester * 3 - 1);
        gradeItems.add(position, new GradeItem().setCredit("1").setGrade("AA").setName("course " + (position + 1)));
        semesterItems.get(semester - 1).increaseCourses();
        notifyItemInserted(pos);
    }

    public void addSemester() {
        onEdit();
        semesterItems.add(new SemesterItem().setCourses(6).setSemester((semesterItems.size() + 1) + ". Semester"));
        for (int i = 0; i < 6; i++)
            gradeItems.add(new GradeItem().setCredit("1").setGrade("AA").setName("course " + (gradeItems.size() + 1)));
        notifyItemRangeInserted(getItemCount(), 9);
    }

    private void removeItem(int pos, RecyclerView rv) {
        onEdit();
        if (!edited)
            edited = true;
        int semester = getSemester(pos);
        int position = pos - (semester * 3 - 1);
        if (position < 0)
            return;
        gradeItems.remove(position);
        semesterItems.get(semester - 1).decreaseCourses();
        notifyItemRemoved(pos);
    }

    private void removeSemester(int pos) {
        onEdit();
        int semester = getSemester(pos + 2);
        int position = (pos + 2) - (semester * 3 - 1);
        if (position < 0 || semester - 1 < 0)
            return;
        int items = semesterItems.get(semester - 1).getCourses();
        semesterItems.remove(semester - 1);
        for (int i = 0; i < items; i++)
            gradeItems.remove(position);
        notifyItemRangeRemoved(pos, 3 + items);
        if (getItemCount() == 0)
            editedListener.isEmpty();
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean isEdited() {
        return edited;
    }

    public interface EditedListener {
        public void onEdit();

        public void isEmpty();
    }

    public void setEditedListener(EditedListener editedListener) {
        this.editedListener = editedListener;
    }

    private void onEdit() {
        if (!edited) {
            edited = true;
            editedListener.onEdit();
        }
    }
}
