package com.example.vadik.livecamerandk;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CameraViewClass {
    public static final String _IMAGE_R = "imageR";
    public static final String _IMAGE_G = "imageR";
    public static final String _IMAGE_B = "imageR";
    public static final String _TEXTURE_V = "textureV";

    private AppCompatActivity _Context;
    private ViewGroup _ViewLayout;

    public CameraViewClass(AppCompatActivity _Context) {
        this._Context = _Context;
        initView(_Context);
    }

    private LinearLayout.LayoutParams generateLayoutParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public ViewGroup get_ViewLayout() {
        return _ViewLayout;
    }

    private void initView(AppCompatActivity context) {
        LinearLayout _Layout = new LinearLayout(context);
        _Layout.setLayoutParams(generateLayoutParams());
        _Layout.setBaselineAligned(true);
        _Layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout _ll = createContainer(context);
        _ll.addView(createTextureView(context, _TEXTURE_V));
        _ll.addView(createImageView(context, _IMAGE_R));
        LinearLayout __ll = createContainer(context);
        __ll.addView(createImageView(context, _IMAGE_G));
        __ll.addView(createImageView(context, _IMAGE_B));
        _Layout.addView(_ll);
        _Layout.addView(__ll);
        _ViewLayout = _Layout;
    }

    private LinearLayout createContainer(AppCompatActivity context) {
        LinearLayout _ll = new LinearLayout(context);
        _ll.setOrientation(LinearLayout.VERTICAL);
        _ll.setLayoutParams(generateLayoutParams());
        ((LinearLayout.LayoutParams)_ll.getLayoutParams()).weight = 1;
        return _ll;
    }

    private TextureView createTextureView(AppCompatActivity context, String _tag) {
        TextureView _TeView = new TextureView(context);
        _TeView.setLayoutParams(generateLayoutParams());
        ((LinearLayout.LayoutParams)_TeView.getLayoutParams()).weight = 1;
        return _TeView;
    }

    private ImageView createImageView(AppCompatActivity context, String _tag) {
        ImageView _TeView = new ImageView(context);
        _TeView.setLayoutParams(generateLayoutParams());
        _TeView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_launcher_background));
        ((LinearLayout.LayoutParams)_TeView.getLayoutParams()).weight = 1;
        return _TeView;
    }

}
