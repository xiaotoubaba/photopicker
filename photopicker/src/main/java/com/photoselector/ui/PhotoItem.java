
package com.photoselector.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.jovision.xunwei.lib.photopicker.R;
import com.photoselector.model.PhotoModel;

/**
 * 单个图片布局(一个GridView单元格)
 */

public class PhotoItem extends LinearLayout implements OnCheckedChangeListener,
        OnLongClickListener, OnClickListener {

    private ImageView ivPhoto;
    private CheckBox cbPhoto;
    private onPhotoItemCheckedListener listener;
    private PhotoModel photo;
    private boolean isCheckAll;
    private onItemClickListener l;
    private int position;

    private PhotoItem(Context context) {
        super(context);
    }

    public PhotoItem(Context context, onPhotoItemCheckedListener listener) {
        this(context);
        LayoutInflater.from(context).inflate(R.layout.layout_photoitem, this,
                true);
        this.listener = listener;

        setOnClickListener(this);
        // setOnLongClickListener(this);

        ivPhoto = (ImageView) findViewById(R.id.iv_photo_lpsi);
        cbPhoto = (CheckBox) findViewById(R.id.cb_photo_lpsi);

        cbPhoto.setOnCheckedChangeListener(this); // CheckBox选中状态改变监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isCheckAll) {
            // 返回false说明选择的图片已经到设置的最大数,不能再选择.
            boolean isCanChoose = listener.onCheckedChanged(photo, buttonView, isChecked); // 调用主界面回调函数
            if (!isCanChoose) {
                buttonView.setChecked(false);
                return;
            }
        }
        // 让图片变暗或者变亮
        if (isChecked) {
            setDrawingable();
            ivPhoto.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            ivPhoto.clearColorFilter();
        }
        photo.setChecked(isChecked);
    }

    /** 设置路径下的图片对应的缩略图 */
    public void setImageDrawable(final PhotoModel photo) {
        this.photo = photo;
        // You may need this setting form some custom ROM(s)
        /*
         * new Handler().postDelayed(new Runnable() {
         * @Override public void run() { ImageLoader.getInstance().displayImage(
         * "file://" + photo.getOriginalPath(), ivPhoto); } }, new
         * Random().nextInt(10));
         */

        ImageLoader.getInstance().displayImage(
                "file://" + photo.getOriginalPath(), ivPhoto);
    }

    private void setDrawingable() {
        ivPhoto.setDrawingCacheEnabled(true);
        ivPhoto.buildDrawingCache();
    }

    /**
     * 设置图片中CheckBox的状态
     */
    @Override
    public void setSelected(boolean selected) {
        if (photo == null) {
            return;
        }
        isCheckAll = true;
        cbPhoto.setChecked(selected);
        isCheckAll = false;
    }

    public void setOnClickListener(onItemClickListener l, int position) {
        this.l = l;
        this.position = position;
    }

    @Override
    public void onClick(View v) {
        if (l != null)
            l.onItemClick(position);
    }

    @Override
    public boolean onLongClick(View v) {
        if (l != null)
            l.onItemClick(position);
        return true;
    }

    /** 图片Item选中事件监听器 */
    public static interface onPhotoItemCheckedListener {
        public boolean onCheckedChanged(PhotoModel photoModel,
                                        CompoundButton buttonView, boolean isChecked);
    }

    /** 图片点击事件 */
    public interface onItemClickListener {
        public void onItemClick(int position);
    }

}
