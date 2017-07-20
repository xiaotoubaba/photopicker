
package com.photoselector.ui;

/**
 * 
 *
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jovision.xunwei.lib.photopicker.R;
import com.photoselector.model.PhotoModel;
import com.photoselector.util.AnimationUtil;

import java.util.ArrayList;

public class BasePhotoPreviewActivity extends Activity implements OnPageChangeListener,
        OnClickListener {

    // 可以选择的图片最大数量
    private int MAX_IMAGE;
    private ViewPager mViewPager;
    private RelativeLayout layoutTop, layoutBottom;
    // 确定
    private LinearLayout okLyt;
    // 返回按钮
    private LinearLayout btnBack;
    // 第几页(格式:10/12)
    private TextView tvPercent;
    // 图片的选中状态
    private CheckBox cbPhoto;
    // 选中的图片数量
    private TextView tvNumber;
    // 删除按钮
    private ImageView ivDel;
    // 图片列表
    protected ArrayList<PhotoModel> photos;
    // 选中的图片列表
    protected ArrayList<PhotoModel> selected;
    // 当前图片的位置
    protected int current;
    // 图片是否点击
    protected boolean isItemClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_photopreview);

        MAX_IMAGE = PhotoSelectorActivity.getMaxImageNumber();
        selected = new ArrayList<PhotoModel>();
        photos = new ArrayList<PhotoModel>();

        initUi();
        initListener();

        overridePendingTransition(R.anim.activity_alpha_action_in, 0); // 渐入效果
    }

    /**
     * 初始化
     */
    private void initUi() {
        mViewPager = (ViewPager) findViewById(R.id.vp_base_app);
        layoutTop = (RelativeLayout) findViewById(R.id.layout_top_app);
        layoutBottom = (RelativeLayout) findViewById(R.id.layout_bottom_app);
        btnBack = (LinearLayout) findViewById(R.id.bv_back_app);
        tvPercent = (TextView) findViewById(R.id.tv_percent_app);
        cbPhoto = (CheckBox) findViewById(R.id.cb_photo);
        ivDel = (ImageView) findViewById(R.id.iv_del);
        tvNumber = (TextView) findViewById(R.id.tv_number);
        okLyt = (LinearLayout) findViewById(R.id.ll_ok);
    }

    /**
     * 监听处理
     */
    private void initListener() {
        mViewPager.setOnPageChangeListener(this);
        btnBack.setOnClickListener(this);
        okLyt.setOnClickListener(this);
        ivDel.setOnClickListener(this);
        cbPhoto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateItemState(buttonView, isChecked);
            }
        });
    }

    /** 绑定数据，更新界面 */
    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
        updatePageInfo();
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(getApplicationContext());
            ((ViewPager) container).addView(photoPreview);
            photoPreview.loadImage(photos.get(position));
            photoPreview.setOnClickListener(photoItemClickListener);
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bv_back_app) {// 返回
            setResult(RESULT_CANCELED);
            finish();
        } else if (v.getId() == R.id.ll_ok) {// 确定
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", selected);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
            finish();
        } else if (v.getId() == R.id.iv_del) {// 删除
            delItem();
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
        current = position;
        // 更新页面信息
        updatePageInfo();
    }

    /**
     * 设置右上角按钮的显示(checkbox|delete btn)
     * 
     * @param isCheckbox 右上角是否显示checkbox框 <br/>
     *            true:显示checkbox,false:显示删除按钮
     */
    protected void setRightBtn(boolean isCheckbox) {
        if (isCheckbox) {
            cbPhoto.setVisibility(View.VISIBLE);
            ivDel.setVisibility(View.GONE);
        } else {
            cbPhoto.setVisibility(View.GONE);
            ivDel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新页面信息(页数/图片状态/选中的图片数量)
     */
    protected void updatePageInfo() {
        updatePercent();
        updateSelectedCount();
        updateCheckboxState();
    }

    /**
     * 更新页数显示
     */
    protected void updatePercent() {
        tvPercent.setText((current + 1) + "/" + photos.size());
    }

    /**
     * 更新Checkbox的状态(选中|未选中)
     */
    protected void updateCheckboxState() {
        PhotoModel photoModel = photos.get(current);
        if (photoModel.isChecked()) {
            cbPhoto.setChecked(true);
        } else {
            cbPhoto.setChecked(false);
        }
    }

    /**
     * 更新选中图片的数量
     */
    protected void updateSelectedCount() {
        // 注意不要直接放入一个int型值,否则会把它当成资源ID去查找
        tvNumber.setText(selected.size() + "");
    }

    /**
     * 更新图片的状态(选中|未选中)
     */
    private void updateItemState(CompoundButton buttonView, boolean isChecked) {
        PhotoModel photoModel = photos.get(current);
        boolean isPrompt = checkIsPromptUpdate(photoModel, isChecked);
        if (!isPrompt) {
            buttonView.setChecked(false);
            return;
        }
        photoModel.setChecked(isChecked);
    }

    /**
     * 检查item是否可以勾选
     * 
     * @param photoModel
     * @param isChecked
     * @return
     */
    private boolean checkIsPromptUpdate(PhotoModel photoModel, boolean isChecked) {
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
        } else {
            selected.remove(photoModel);
        }

        updateSelectedCount();

        return isCanChoose;
    }

    /**
     * 删除item
     */
    private void delItem() {
        PhotoModel photoModel = photos.get(current);
        selected.remove(photoModel);
        updatePercent();
        updateSelectedCount();
        mPagerAdapter.notifyDataSetChanged();
    }

    /** 图片点击事件回调 */
    private OnClickListener photoItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isItemClick) {
                // 顶部
                new AnimationUtil(getApplicationContext(), R.anim.translate_up)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutTop);
                // 底部
                new AnimationUtil(getApplicationContext(), R.anim.translate_down)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutBottom);
                isItemClick = true;
            } else {
                // 顶部
                new AnimationUtil(getApplicationContext(), R.anim.translate_down_current)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutTop);
                // 底部
                new AnimationUtil(getApplicationContext(), R.anim.translate_up_current)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutBottom);

                isItemClick = false;
            }
        }
    };
}
