package br.pucminas.stundentproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by luket on 03-Sep-16.
 */
public class AdapterListStudents extends BaseAdapter {
    private final MainActivity mainActivity;
    private List<Student> students;
    private LayoutInflater inflater;

    public AdapterListStudents(MainActivity mainActivity, List<Student> students) {
        this.mainActivity = mainActivity;
        this.students = students;

        //Inflate MainActivity
        inflater = mainActivity.getLayoutInflater();

    }

    @Override
    public int getCount() {

        return students != null ? students.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //Get student of position from list
        final Student student = students.get(position);

        //Instantiate holder (butterknife)
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.adapter_item_student, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        //Set student's info
        Picasso.with(mainActivity).load(student.getFotoUrl()).into(holder.imgStudent);
        holder.txtName.setText(student.getNome());
        holder.txtAge.setText(student.getIdade().toString());
        holder.txtAddress.setText(student.getEndereco());

        //Set click of delete button
        holder.imgDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                //Set confirmation message
                builder.setMessage("Do you wish to delete '" + student.getNome() + "'?");
                //Set positive button behavior, deleting student
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete student passing his Id
                        mainActivity.callDeleteStudent(student.getObjectId());
                    }
                });
                //Set negative button, cancel
                builder.setNegativeButton("No", null);
                AlertDialog dialog = builder.create();
                //Show dialog
                dialog.show();
            }
        });

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.imgStudent)
        ImageView imgStudent;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtAge)
        TextView txtAge;
        @BindView(R.id.txtAddress)
        TextView txtAddress;
        @BindView(R.id.imgDelete)
        ImageView imgDelete;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
