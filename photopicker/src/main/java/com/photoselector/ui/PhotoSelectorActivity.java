
package com.photoselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.jovision.xunwei.lib.photopicker.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.AlbumModel;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.onItemClickListener;
import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;
import com.photoselector.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择器类
 */
public class PhotoSelectorActivity extends Activity implements
        onItemClickListener, onPhotoItemCheckedListener, OnClickListener {

    public static final String KEY_MAX = "key_max";
    public static final String KEY_SELECTED = "key_selected";
    public static PhotoSelectorActivity mInstance;
    // 可以选择的图片最大数量
    private static int MAX_IMAGE;

    public static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_PRIVIEW = 2;

    public static String RECCENT_PHOTO = null;

    private TextView tvTitle;
    private GridView gvPhotos;
    private LinearLayout okLyt, clearLyt, previewLyt;
    private PhotoSelectorDomain photoSelectorDomain;
    private PhotoSelectorAdapter photoAdapter;
    private ArrayList<PhotoModel> selected;
    private TextView tvNumber;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RECCENT_PHOTO = getResources().getString(R.string.recent_photos);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_photoselector);

        mInstance = this;
        /** 获取Intent信息 */
        Intent intent = (getIntent() != null) ? getIntent() : null;
        if (intent == null) {
            throw new NullPointerException("getIntent is null!");
        }
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            MAX_IMAGE = extras.getInt(KEY_MAX, 1);
            selected = (ArrayList<PhotoModel>) extras.getSerializable(KEY_SELECTED);
        }
        if(MAX_IMAGE <= 0){
            MAX_IMAGE = 1;
        }
        if(selected == null){
            selected = new ArrayList<PhotoModel>();
        }
        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());
        // 更新最近照片
        photoSelectorDomain.getReccent(reccentListener);

        initImageLoader();
        initUi();

        photoAdapter = new PhotoSelectorAdapter(getApplicationContext(),
                new ArrayList<PhotoModel>(), CommonUtils.getWidthPixels(this),
                this, this, this);
        gvPhotos.setAdapter(photoAdapter);

    }

    private void initUi() {
        tvTitle = (TextView) findViewById(R.id.tv_title_lh);
        gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
        okLyt = (LinearLayout) findViewById(R.id.ll_ok);
        clearLyt = (LinearLayout) findViewById(R.id.ll_clear);
        previewLyt = (LinearLayout) findViewById(R.id.ll_preview);
        tvNumber = (TextView) findViewById(R.id.tv_number);

        okLyt.setOnClickListener(this);
        clearLyt.setOnClickListener(this);
        previewLyt.setOnClickListener(this);

        // 设置选中的图片数量
        updateSelectedCount();
        // 根据选中的图片数量判断预览功能是否有效
        if (selected.size() > 0) {
            previewLyt.setEnabled(true);
        } else {
            previewLyt.setEnabled(false);
        }

        // 返回
        findViewById(R.id.ll_back_album).setOnClickListener(this);
    }

    private void initImageLoader() {
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_picture_loading)
                .showImageOnFail(R.drawable.ic_picture_loadfailed)
                .cacheInMemory(true).cacheOnDisk(true)
                .resetViewBeforeLoading(true).considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .memoryCacheExtraOptions(400, 400)
                // default = device screen dimensions
                .diskCacheExtraOptions(400, 400, null)
                .threadPoolSize(5)
                // default Thread.NORM_PRIORITY - 1
                .threadPriority(Thread.NORM_PRIORITY)
                // default FIFO
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                // default
                .diskCache(
                        new UnlimitedDiskCache(StorageUtils.getCacheDirectory(
                                this, true)))
                // default
                .diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                // default
                .imageDownloader(new BaseImageDownloader(this))
                // default
                .imageDecoder(new BaseImageDecoder(false))
                // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                // default
                .defaultDisplayImageOptions(imageOptions).build();

        ImageLoader.getInstance().init(config);
    }

    public static int getMaxImageNumber() {
        return MAX_IMAGE;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_ok)
            ok(); // 确认
        // else if (v.getId() == R.id.tv_album_ar)
        // album();//
        else if (v.getId() == R.id.ll_clear)
            reset();// 清空选择
        else if (v.getId() == R.id.ll_preview)
            priview();// 预览
        else if (v.getId() == R.id.tv_camera_vc)
            catchPicture();// 拍照
        else if (v.getId() == R.id.ll_back_album)
            album();// 打开相册
    }

    /** 拍照 */
    private void catchPicture() {
        CommonUtils.launchActivityForResult(this, new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA);
    }

    /** 完成 */
    private void ok() {

        Intent data = new Intent();
        data.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", selected);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);

        // 结束相册Activity
        if (AlbumSelectorActivity.mInstance != null) {
            AlbumSelectorActivity.mInstance.finish();
        }

        finish();
    }

    /** 预览照片 */
    private void priview() {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle bundle = new Bundle();
        // 已经选择的图片
        bundle.putSerializable("photos", selected);
        bundle.putString("tag", "priview");
        intent.putExtras(bundle);
        CommonUtils.launchActivityForResult(this, intent, REQUEST_PRIVIEW);
    }

    /**
     * 打开相册
     */
    private void album() {
//        Intent intent = new Intent(this, AlbumSelectorActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        startActivity(intent);
        finish();
    }

    /** 清空选中的图片 */
    private void reset() {
        int len = selected.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                PhotoModel pm = selected.get(i);
                pm.setChecked(false);
            }
            selected.clear();
            tvNumber.setText("0");
            previewLyt.setEnabled(false);
            photoAdapter.notifyDataSetChanged();
        }
    }

    /** 点击查看照片 */
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle bundle = new Bundle();
        // 如果列表中有拍照功能position - 1
        if (tvTitle.getText().toString().equals(RECCENT_PHOTO)) {
            // bundle.putInt("position", position - 1);
            bundle.putInt("position", position);
        } else {
            bundle.putInt("position", position);
        }
        // 已经选择的图片
        bundle.putSerializable("photos", selected);
        bundle.putString("album", tvTitle.getText().toString());
        bundle.putString("tag", "album");
        intent.putExtras(bundle);
        CommonUtils.launchActivityForResult(this, intent, REQUEST_PRIVIEW);
    }

    /**
     * 照片选中状态改变之后<br/>
     * 返回值:是否响应照片的选择操作
     */
    @Override
    public boolean onCheckedChanged(PhotoModel photoModel,
            CompoundButton buttonView, boolean isChecked) {
        boolean isCanChoose = true;
        if (isChecked) {
            if (!selected.contains(photoModel)) {
                if (selected.size() >= MAX_IMAGE) {
                    Toast.makeText(
                            this,
                            String.format(
                                    getString(R.string.max_img_limit_reached),
                                    MAX_IMAGE), Toast.LENGTH_SHORT).show();
                    isCanChoose = false;
                    return isCanChoose;
                }
                selected.add(photoModel);
            }
            previewLyt.setEnabled(true);
        } else {
            selected.remove(photoModel);
        }

        // 设置选中的图片数量
        updateSelectedCount();

        if (selected.isEmpty()) {
            previewLyt.setEnabled(false);
        }

        return isCanChoose;
    }

    /**
     * 更新选中图片的数量
     */
    protected void updateSelectedCount() {
        // 注意不要直接放入一个int型值,否则会把它当成资源ID去查找
        tvNumber.setText(selected.size() + "");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {// 拍照
            PhotoModel photoModel = new PhotoModel(CommonUtils.query(
                    getApplicationContext(), data.getData()));
            // selected.clear();
            // //--keep all
            // selected photos
            // tvNumber.setText("(0)");
            // //--keep all
            // selected photos
            // ///////////////////////////////////////////////////////////////////////////////////////////
            if (selected.size() >= MAX_IMAGE) {
                Toast.makeText(
                        this,
                        String.format(
                                getString(R.string.max_img_limit_reached),
                                MAX_IMAGE), Toast.LENGTH_SHORT).show();
                photoModel.setChecked(false);
                photoAdapter.notifyDataSetChanged();
            } else {
                if (!selected.contains(photoModel)) {
                    selected.add(photoModel);
                }
            }
            // 完成
            ok();
        } else if (requestCode == REQUEST_PRIVIEW && resultCode == RESULT_OK) {// 预览
            ArrayList<PhotoModel> selectedPhotos = (ArrayList<PhotoModel>) data.getExtras()
                    .getSerializable("photos");
            // 清空原有选择的数据
            int oldSelectedLen = selected.size();
            for (int i = 0; i < oldSelectedLen; i++) {
                PhotoModel pm = selected.get(i);
                pm.setChecked(false);
            }
            selected.clear();

            // 重新选择数据
            List<PhotoModel> photos = photoAdapter.getItems();
            // 检查图片有没有被选中
            int len = photos.size();
            for (int i = 0; i < len; i++) {
                PhotoModel model = photos.get(i);
                if (selectedPhotos.contains(model)) {
                    model.setChecked(true);
                    // 追加选择的数据
                    selected.add(model);
                }
            }

            // 设置选中的图片数量
            updateSelectedCount();
            // 根据选中的图片数量判断预览功能是否有效
            if (selected.size() > 0) {
                previewLyt.setEnabled(true);
            } else {
                previewLyt.setEnabled(false);
            }
            photoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 动画
        overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
        String albumName = intent.getStringExtra("albumName");
        tvTitle.setText(albumName);
        // 更新照片列表
        if (albumName.equals(RECCENT_PHOTO)) {
            // 获取最近的照片
            photoSelectorDomain.getReccent(reccentListener);
        } else {
            // 获取选中相册的照片
            photoSelectorDomain.getAlbum(albumName, reccentListener);
        }
    }

    /** 获取本地图库照片回调 */
    public interface OnLocalReccentListener {
        public void onPhotoLoaded(List<PhotoModel> photos);
    }

    /** 获取本地相册信息回调 */
    public interface OnLocalAlbumListener {
        public void onAlbumLoaded(List<AlbumModel> albums);
    }

    /**
     * 照片更新后的回调函数
     */
    private OnLocalReccentListener reccentListener = new OnLocalReccentListener() {
        @Override
        public void onPhotoLoaded(List<PhotoModel> photos) {

            // 检查图片有没有被选中
            int len = photos.size();
            for (int i = 0; i < len; i++) {
                PhotoModel model = photos.get(i);
                int index = selected.indexOf(model);
                if (index != -1) {
                    model.setChecked(true);

                    // 更新对象(不更新的话,adapter数据更新以后,清空功能不起作用,很奇怪)
                    selected.add(index, model);
                    selected.remove(index + 1);
                }
            }

            photoAdapter.update(photos);
            gvPhotos.smoothScrollToPosition(0); // 滚动到顶端
            // reset(); //--keep selected photos
        }
    };
}
