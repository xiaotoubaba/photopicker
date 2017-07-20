
package com.photoselector.ui;

/**
 * 
 * 图片预览类
 *
 */
import android.os.Bundle;

import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoSelectorActivity.OnLocalReccentListener;
import com.photoselector.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class PhotoPreviewActivity extends BasePhotoPreviewActivity implements
        OnLocalReccentListener {

    private PhotoSelectorDomain photoSelectorDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());

        init(getIntent().getExtras());
    }

    @SuppressWarnings("unchecked")
    protected void init(Bundle extras) {
        if (extras == null)
            return;

        String tag = extras.getString("tag"); // 标记
        if (tag.equals("priview")) { // 预览图片
            photos = (ArrayList<PhotoModel>) extras.getSerializable("photos");
            // 把photos中的数据复制到selected中
            selected.addAll(photos);
            current = extras.getInt("position", 0);
            setRightBtn(true);
            bindData();
        } else if (tag.equals("album")) { // 点击图片查看
            String albumName = extras.getString("album"); // 相册
            selected = (ArrayList<PhotoModel>) extras.getSerializable("photos");
            current = extras.getInt("position");
            if (!CommonUtils.isNull(albumName)
                    && albumName.equals(PhotoSelectorActivity.RECCENT_PHOTO)) {
                photoSelectorDomain.getReccent(this);
            } else {
                photoSelectorDomain.getAlbum(albumName, this);
            }
        } else if (tag.equals("choose_priview")) {// 从创建直播界面点击图片进来的时候
            selected = photos = (ArrayList<PhotoModel>) extras.getSerializable("photos");
            current = extras.getInt("position", 0);
            setRightBtn(false);
            bindData();
        }
    }

    @Override
    public void onPhotoLoaded(List<PhotoModel> photos) {
        // 检查图片有没有被选中
        int len = photos.size();
        for (int i = 0; i < len; i++) {
            PhotoModel model = photos.get(i);
            if (selected.contains(model)) {
                model.setChecked(true);
            }
        }

        this.photos = (ArrayList<PhotoModel>) photos;
        setRightBtn(true);
        bindData(); // 更新界面
    }

}
