package com.example.sasha.todolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditorActivity extends Activity implements OnClickListener {

    private String action,
                    oldPriority,
                    oldDeadline,
                    oldText,
                    noteFilter;     // WHERE
    private EditText editor,
                    deadline,
                    priority;

    //UI References -----------------------
    private DatePickerDialog datePickerDialog;
    private Dialog numberPickerDialog;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);

        findViewsById();
        setPriorityField();
        setDeadlineField();

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle("New Note");
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS,
                    noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            oldPriority = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_PRIORITY));
            oldDeadline = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_DEADLINE));

            editor.setText(oldText);
            priority.setText(oldPriority);
            deadline.setText(oldDeadline);
            editor.requestFocus();
        }

    }

    private void findViewsById() {

        editor = (EditText) findViewById(R.id.editText);

        // DatePicker functionality ----------------------
        deadline = (EditText) findViewById(R.id.setDeadline);

        // NumberPicker functionality ----------------------
        priority = (EditText) findViewById(R.id.setPriority);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                finishEditing();
                break;
        }

        return true;
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
        String newPriority = String.valueOf(priority.getText());
        String newDeadline = String.valueOf(deadline.getText());


        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    Toast.makeText(this, R.string.not_created, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText, newPriority, newDeadline);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {
                    deleteNote();
                } else if (oldText.equals(newText)) {
                    if (oldDeadline.equals(newDeadline)) {
                        if (oldPriority.equals(newPriority)) {
                            Toast.makeText(this, R.string.not_changed, Toast.LENGTH_SHORT).show();
                            Log.d("EditActivity", "New note:  " + newText + "\n -> " + newPriority + "\n -> " + newDeadline);
                            setResult(RESULT_CANCELED);
                        }
                    }
                } else {
                    updateNote(newText, newPriority, newDeadline);
                }
        }

        Log.d("EditActivity", "New note:  " + newText + "\n\t -> " + newPriority + "\n\t -> " + newDeadline);
        finish();
    }

    private void deleteNote() {

        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);

        Toast.makeText(this, "Note DELETED!", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
    }

    private void updateNote(String text, String priority, String deadline) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, text);
        values.put(DBOpenHelper.NOTE_PRIORITY, priority);
        values.put(DBOpenHelper.NOTE_DEADLINE, deadline);

        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);

        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);

    }

    private void insertNote(String text, String priority, String deadline) {

        ContentValues values = new ContentValues();
        values.put(                             // add value to object
                DBOpenHelper.NOTE_TEXT,         // key (name of the column)
                text);                      // value

        values.put(DBOpenHelper.NOTE_PRIORITY, priority);
        values.put(DBOpenHelper.NOTE_DEADLINE, deadline);

        getContentResolver().insert(NotesProvider.CONTENT_URI, values);     // insert new row

        Toast.makeText(this, "New note created", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);

    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    // DatePicker functionality ----------------------------------------------
    private void setDeadlineField() {

        deadline.setInputType(InputType.TYPE_NULL);
        deadline.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                deadline.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }


    // NumberPicker functionality ----------------------------------------------
    private void setPriorityField() {
        priority.setInputType(InputType.TYPE_NULL);
        priority.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == deadline) {
            datePickerDialog.show();
        } else if (view == priority) {

            // ==========================
            numberPickerDialog = new Dialog(EditorActivity.this);
            numberPickerDialog.setTitle("Set priority");
            numberPickerDialog.setContentView(R.layout.dialog);
            Button b1 = (Button) numberPickerDialog.findViewById(R.id.button1);
            Button b2 = (Button) numberPickerDialog.findViewById(R.id.button2);
            final NumberPicker np = (NumberPicker) numberPickerDialog.findViewById(R.id.numberPicker1);
            np.setMaxValue(10); // max value 10
            np.setMinValue(1);   // min value 1
            np.setWrapSelectorWheel(false);
//            np.setOnValueChangedListener();
            b1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    priority.setText(String.valueOf(np.getValue()));
                    numberPickerDialog.dismiss();
                }
            });
            b2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    numberPickerDialog.dismiss(); // dismiss the dialog
                }
            });
            numberPickerDialog.show();
        }
    }
}
