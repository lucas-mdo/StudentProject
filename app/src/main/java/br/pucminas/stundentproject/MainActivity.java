package br.pucminas.stundentproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements Validator.ValidationListener {

    //Layout for main activity
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    //Progress bar
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    //List of students
    @BindView(R.id.listStudents)
    ListView listStudents;

    //New student input fields
    //Name
    @NotEmpty(messageResId = R.string.msg_error_not_empty)
    EditText edtName;
    //Age
    @NotEmpty(messageResId = R.string.msg_error_not_empty)
    EditText edtAge;
    //PicURL
    @NotEmpty(messageResId = R.string.msg_error_not_empty)
    EditText edtPicture;
    //Phone
    @NotEmpty(messageResId = R.string.msg_error_not_empty)
    EditText edtPhone;
    //Address
    @NotEmpty(messageResId = R.string.msg_error_not_empty)
    EditText edtAddress;

    //Instance of API service
    private APIService service;
    //Instance of list adapter
    private AdapterListStudents adapter;
    //Instance of validator fields
    private Validator validator;
    //Instance of dialog form (needed to be global for validation dismiss)
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Binding butterknife to this activity
        ButterKnife.bind(this);

        validator = new Validator(this);
        validator.setValidationListener(this);

        //Auto-generated FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show new student form
                showDialogAddStudent();
            }
        });

        //Creating Retrofit object with the BASE URL of the API
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Getting access to API methods
        service = retrofit.create(APIService.class);

        //Getting initial list of students
        callGetStudents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            callGetStudents();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidationSucceeded() {

        //Get contents from input fields
        String name = edtName.getText().toString();
        String age = edtAge.getText().toString();
        String picURL = edtPicture.getText().toString();
        String phone = edtPhone.getText().toString();
        String address = edtAddress.getText().toString();

        try {
            //Instantiate new student and save it
            callAddStudent(new Student(name, Integer.parseInt(age), picURL, phone, address));
            dialog.dismiss();
        }
        catch (Exception ex) { //Just a safeguard
            edtAge.setError("This field must be a number!");
        }

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    //Call to API to get the list of students
    private void callGetStudents(){
        //Make progressbar visible while populating the list
        progressBar.setVisibility(View.VISIBLE);
        //Call to get the list of Students
        Call<ResultStudents> request = service.getStudents();

        //Asynchronous call
        request.enqueue(new Callback<ResultStudents>() {
            @Override
            public void onResponse(Call<ResultStudents> call, Response<ResultStudents> response) {
                //Checking if communication with API was successful (200)
                if (response.isSuccessful()){
                    //Get the list from body
                    ResultStudents students = response.body();

                    //Instantiate adapter for list of students
                    adapter = new AdapterListStudents(MainActivity.this, students.getResults());
                    listStudents.setAdapter(adapter);
                    listStudents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                            //Get selected item as Student
                            Student student = (Student) adapter.getItem(position);
                            //Show address in google maps app
                            showInGoogleMapsApp(student.getEndereco());
                        }
                    });
                }else{
                    //Error while populating the list of students
                    showMessage(response.code() + " - " + response.message());
                }
                //Hide the progress bar
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResultStudents> call, Throwable t) {
                //Some error occurred, show what went wrong
                showMessage("Fail to return the list of students - " + t.getMessage());
                //Hide the progress bar
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Show selected student's address in google maps app
    private void showInGoogleMapsApp(String address){
        // Create a Uri from an intent string. Use the result to create an Intent.
        //geo:0,0?q=my+street+address
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    //Call to API to delete a student, passing his Id - delete icon onclick
    public void callDeleteStudent (final String idStudent){
        //Call to delete a student
        Call<ResponseBody> request = service.deleteStudent(idStudent);
        //Asynchronous call
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    //The return is irrelevant (not used)
                    ResponseBody result = response.body();

                    //Show sucessful message
                    showMessage(response.code() + " - Item deleted successfully!");

                    //Refresh list
                    callGetStudents();
                }else{
                    //Error while deleting the student
                    showMessage(response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Some error ocurred, show what went wrong
                showMessage("Fail to delete student - " + t.getMessage());
            }
        });
    }

    //Show custom dialog (form) to add a new student
    private void showDialogAddStudent(){
        //Create inflater
        LayoutInflater inflater = getLayoutInflater();
        //Inflate dialog
        final View view = inflater.inflate(R.layout.dialog_add_student, null);
        //Get edit picture field
        edtPicture = ButterKnife.findById(view, R.id.edtPicture);
        //Set to a random picture using robohash
        edtPicture.setText("https://robohash.org/"+ Math.random() +".png");

        //Get all fields from form
        edtName = ButterKnife.findById(view, R.id.edtName);
        edtAge = ButterKnife.findById(view, R.id.edtAge);
        //EditText edtPicture = ButterKnife.findById(view, R.id.edtName);
        edtPhone = ButterKnife.findById(view, R.id.edtPhone);
        edtAddress = ButterKnife.findById(view, R.id.edtAddress);

        //Create builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set custom view
        builder.setView(view);
        //Set icon
        builder.setIcon(R.drawable.ic_assignment_ind_blue_grey_900_24dp);
        //Set title
        builder.setTitle(R.string.dialog_title_new_student);
        //Set message
        builder.setMessage(R.string.dialog_msg_required);

        //Set positive button behavior (save)
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing here because we override this button later to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
            }
        });

        //Set negative button behavior (cancel)
        builder.setNegativeButton("Cancel", null);

        //Create and show dialog
        dialog = builder.create();
        //Prevent dismiss from outside touch
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        //Custom click (needed for avoiding dismissing the dialog when errors occurred)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Validate input fields
                validator.validate();
            }
        });
    }

    private void callAddStudent(final Student student){
        //Call to API to add a new student in the list
        Call<ResponseBody> request = service.addStudent(student);
        //Asynchronous call
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    //The return is irrelevant (not used)
                    ResponseBody result = response.body();

                    //Show sucessful message
                    showMessage(response.code() + " - Student " + student.getNome() + " created successfully!");

                    //Refresh list
                    callGetStudents();
                }else{
                    //Error while saving the student
                    showMessage(response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Some error ocurred, show what went wrong
                showMessage("Fail to add student - " + t.getMessage());
            }
        });
    }

    //Show messages for every call to the API, showing results and errors alike
    private void showMessage(String msg){
        Snackbar snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Refresh list
                        callGetStudents();
                    }
                });
        snackbar.show();
    }
}
