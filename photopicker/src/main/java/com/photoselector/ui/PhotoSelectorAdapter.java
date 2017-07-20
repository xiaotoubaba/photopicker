
package com.photoselector.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.jovision.xunwei.lib.photopicker.R;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.onItemClickListener;
import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;

import java.util.ArrayList;

/**
 * 图片选择类adapter
 */

public class PhotoSelectorAdapter extends MBaseAdapter<PhotoModel> {

    private int itemWidth;
    private int horizentalNum = 3;
    private onPhotoItemCheckedListener listener;
    private LayoutParams itemLayoutParams;
    private onItemClickListener mCallback;
    private OnClickListener cameraListener;

    private PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models) {
        super(context, models);
    }

    public PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models, int screenWidth,
            onPhotoItemCheckedListener listener, onItemClickListener mCallback,
            OnClickListener cameraListener) {
        this(context, models);
        setItemWidth(screenWidth);
        this.listener = listener;
        this.mCallback = mCallback;
        this.cameraListener = cameraListener;
    }

    /** 设置图片的宽高 */
    public void setItemWidth(int screenWidth) {
        int horizentalSpace = context.getResources().getDimensionPixelSize(
                R.dimen.sticky_item_horizontalSpacing);
        this.itemWidth = (screenWidth - (horizentalSpace * (horizentalNum - 1))) / horizentalNum;
        this.itemLayoutParams = new LayoutParams(itemWidth, itemWidth);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoItem item = null;
        // 不要对Adapter中的 convertView对象进行任何样式变更操作
        if (convertView == null || !(convertView instanceof PhotoItem)) {
            item = new PhotoItem(context, listener);
            ImageView iv = (ImageView) item.findViewById(R.id.iv_photo_lpsi);
            iv.setLayoutParams(itemLayoutParams);
            convertView = item;
        } else {
            item = (PhotoItem) convertView;
        }
        item.setImageDrawable(models.get(position));
        item.setSelected(models.get(position).isChecked());
        item.setOnClickListener(mCallback, position);
        Log.e("test", "position:"+position);
        return convertView;
    }
}
