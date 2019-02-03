package com.example.admin.texturestock;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class StockActivity extends AppCompatActivity {

    private List<String> fileList = new ArrayList<String>();
    private ListView listview1;
    private File[] files;


    public void resetList(String type_name) {
        final String SAVE_DIR = "/TextureStock/";
        final java.io.File root_file = new File(Environment.getExternalStorageDirectory().getPath()+SAVE_DIR);
        try{
            if(!root_file.exists()){
                root_file.mkdir();
            }
        }catch(SecurityException e) {
            e.printStackTrace();
            throw e;
        }

        File[] textureFiles = root_file.listFiles();
        ArrayList<File> paths = new ArrayList<>();

        for(int i = 0; i < textureFiles.length; i++){
            if(textureFiles[i].isFile()){
                String filename = textureFiles[i].getName();
                String type = filename.split("_",0)[0];
                if(type_name == "all*"){
                    paths.add(textureFiles[i]);
                }
                else if(type.equals(type_name)) {
                    paths.add(textureFiles[i]);
                }
            }
        }
        ListAdapter adapter = new ListAdapter(this, 0, paths);

        listview1 = (ListView) findViewById(R.id.listView1);
        listview1.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        //TextureSock Folder
        final String SAVE_DIR = "/TextureStock/";
        final java.io.File root_file = new File(Environment.getExternalStorageDirectory().getPath()+SAVE_DIR);
        try{
            if(!root_file.exists()){
                root_file.mkdir();
            }
        }catch(SecurityException e) {
            e.printStackTrace();
            throw e;
        }

        //typeList
        File[] textureFiles = root_file.listFiles();
        List<String> typeList = new ArrayList<String>();
        ArrayList<File> paths = new ArrayList<>();

        typeList.add("all*");

        for(int i = 0; i < textureFiles.length; i++){
            if(textureFiles[i].isFile()){
                String filename = textureFiles[i].getName();
                paths.add(textureFiles[i]);
                fileList.add(filename);
                String type = textureFiles[i].getName().split("_",0)[0];
                if(!typeList.contains(type))
                    typeList.add(type);
            }
        }

        ListAdapter adapter = new ListAdapter(this, 0, paths);
        listview1 = (ListView) findViewById(R.id.listView1);
        listview1.setAdapter(adapter);

        Spinner spinner = findViewById(R.id.spinner);
        // ArrayAdapter
        ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, typeList);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapter to spinner
        spinner.setAdapter(spinnerAdapter);

        // リスナーを登録
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                resetList(item);
            }
            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public class UserAdapter extends ArrayAdapter<File> {
        private LayoutInflater layoutInflater;
        public UserAdapter(Context c, int id, ArrayList<File> users) {
            super(c, id, users);
            this.layoutInflater = (LayoutInflater) c.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.list,
                        parent,
                        false
                );
            }
            ((ImageView) convertView.findViewById(R.id.icon))
                    .setImageResource(R.drawable.karitop);
            ((TextView) convertView.findViewById(R.id.comment))
                    .setText("Comment");
            return convertView;
        }
    }
}