
package com.photoselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jovision.xunwei.lib.photopicker.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.AlbumModel;
import com.photoselector.ui.PhotoSelectorActivity.OnLocalAlbumListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册选择类
 */
public class AlbumSelectorActivity extends Activity implements OnItemClickListener, OnClickListener {

    public static AlbumSelectorActivity mInstance;
    public static String RECCENT_PHOTO = null;
    private ListView lvAblum;
    private AlbumAdapter albumAdapter;
    private PhotoSelectorDomain photoSelectorDomain;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RECCENT_PHOTO = getResources().getString(R.string.recent_photos);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_albumselector);

        mInstance = this;
        /** 获取Intent信息 */
        intent = (getIntent() != null) ? getIntent() : null;
        if (intent == null) {
            throw new NullPointerException("getIntent is null!");
        }

        initUi();
    }

    private void initUi() {
        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());

        lvAblum = (ListView) findViewById(R.id.lv_ablum_ar);
        albumAdapter = new AlbumAdapter(getApplicationContext(),
                new ArrayList<AlbumModel>());
        lvAblum.setAdapter(albumAdapter);
        lvAblum.setOnItemClickListener(this);

        photoSelectorDomain.updateAlbum(albumListener); // 跟新相册信息

        // 返回
        findViewById(R.id.bv_back_lh).setOnClickListener(this);
    }

    private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
        @Override
        public void onAlbumLoaded(List<AlbumModel> albums) {
            albumAdapter.update(albums);
        }
    };

    /** 相册列表点击事件 */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
        for (int i = 0; i < parent.getCount(); i++) {
            AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
            if (i == position)
                album.setCheck(true);
            else
                album.setCheck(false);
        }
        albumAdapter.notifyDataSetChanged();

        Intent intent = new Intent(this, PhotoSelectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("albumName", current.getName());
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 动画
        overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bv_back_lh) {
            PhotoSelectorActivity.mInstance.finish();
            finish();
        }
    }

}
