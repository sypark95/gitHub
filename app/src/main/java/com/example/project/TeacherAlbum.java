package com.example.project;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.project.Holder.AlbumViewHolder;
import com.example.project.Model.Album;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TeacherAlbum extends AppCompatActivity{
    final int PICTURE_REQUEST_CODE=1;

    private RecyclerView album_recyclerview;

    FirebaseDatabase database;
    DatabaseReference albumdb;

    FirebaseRecyclerOptions<Album> options;
    FirebaseRecyclerAdapter<Album, AlbumViewHolder> adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_album);

        database = FirebaseDatabase.getInstance();
        albumdb = database.getReference("Album");

        album_recyclerview=(RecyclerView)findViewById(R.id.gallery1) ;
        album_recyclerview.setLayoutManager(new LinearLayoutManager(TeacherAlbum.this));

        showTask();



    }//onCreate end

    private void showTask(){
        options = new FirebaseRecyclerOptions.Builder<Album>()
                .setQuery(albumdb, Album.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Album, AlbumViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AlbumViewHolder holder, int i, @NonNull Album album) {
                Glide.with(holder.mAlbumImageView.getContext())
                        .load(album.albumImageUri)
                        .apply(new RequestOptions())
                        .into(holder.mAlbumImageView);
            }

            @NonNull
            @Override
            public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_album,parent,false);
                return new AlbumViewHolder(view);
            }
        };
        album_recyclerview.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //앨범 타이틀바에 album_btn_titlebar 메뉴를 이용해 추가버튼 추가
        getMenuInflater().inflate(R.menu.album_btn_titlebar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.album_write: //추가 버튼이 클릭됬을 때

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent,"사진 선택"),PICTURE_REQUEST_CODE);
                    }catch (Exception e){
                        Intent photoPickerIntent = new Intent(this,TeacherAlbum.class);
                        startActivityForResult(photoPickerIntent,PICTURE_REQUEST_CODE);
                    }
                }else{
                    Intent photoPickerIntent = new Intent(this,TeacherAlbum.class);
                    startActivityForResult(photoPickerIntent,PICTURE_REQUEST_CODE);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {

                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();
                //data.getClipdata() api 오류시 빌드그래들에서 defaultConfig{} 안 minSdkVersion 16으로 수정

                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if(clipData!=null)
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH");
                    Date now = new Date();
                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    for(int i = 0; i < clipData.getItemCount(); i++)
                    {
                        String filename = formatter.format(now)+"_"+(i+1)+ ".png";
                        StorageReference storageRef1 = storage.getReferenceFromUrl("gs://dolbomi1.appspot.com/").child("albumImages/"+filename);
                        Uri urione =  clipData.getItemAt(i).getUri();
                        storageRef1.putFile(urione);

                        FirebaseDatabase.getInstance().getReference().child("Album").push().setValue(filename);
                    } //포문end

                    Toast.makeText(this, "사진업로드 성공", Toast.LENGTH_SHORT).show();
                }
            }//리절트 ok end
        }//픽 리퀘스트 코드 end
    }//액티비티 리절트 end

}
