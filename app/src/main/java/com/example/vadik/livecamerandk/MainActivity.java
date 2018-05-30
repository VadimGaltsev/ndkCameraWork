package com.example.vadik.livecamerandk;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback{

    // Used to load the 'CameraLib' library on application startup.
    static {
        System.loadLibrary("camera_live");
    }

    private Camera _Camera;
    private TextureView _TextureView;
    private byte _View_Source[];
    private ImageView _ImageVR, _ImageVG, _ImageVB;
    private Bitmap _ImageR, _ImageG, _ImageB;
    private ViewGroup _VGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(_VGroup = new CameraViewClass(this).get_ViewLayout());
        injectViews(_VGroup);
        if (_TextureView != null) {
            _TextureView.setSurfaceTextureListener(this);
        }

    }

    /**
     * inject views from View Class
     * @param _vgroup - main view group from activity
     */
    private void injectViews(ViewGroup _vgroup) {
        if (_vgroup != null) {
            _ImageVB = _vgroup.findViewWithTag(CameraViewClass._IMAGE_B);
            _ImageVG = _vgroup.findViewWithTag(CameraViewClass._IMAGE_G);
            _ImageVR = _vgroup.findViewWithTag(CameraViewClass._IMAGE_R);
            _TextureView = _vgroup.findViewWithTag(CameraViewClass._TEXTURE_V);
        }
    }

    /**
     * Getting data from camera and then redraw texture
     *
     * @param data - data from camera in raw bytes
     * @param camera - camera instance
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (_Camera != null) {
            decode(_ImageR, data, 0xFFFF0000);
            decode(_ImageG, data, 0xFF00FF00);
            decode(_ImageB, data, 0XFF0000FF);
            _ImageVR.invalidate();
            _ImageVB.invalidate();
            _ImageVG.invalidate();
            _Camera.addCallbackBuffer(_View_Source);
        }
    }

    private native void decode(Bitmap imageB, byte[] data, int i);

    /**
     * Method for get information from camera
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        _Camera = Camera.open();
        try {
            _Camera.setPreviewTexture(surface);
            _Camera.setPreviewCallbackWithBuffer(this);
            //set orientation for landscape
            _Camera.setDisplayOrientation(Surface.ROTATION_0);
            Camera.Size _size = findBestResolution(width, height);
            //start setup camera params
            PixelFormat _p_format = new PixelFormat();
            PixelFormat.getPixelFormatInfo(_Camera.getParameters().getPreviewFormat(), _p_format);
            int _source_Size = _size.width * _size.height * _p_format.bitsPerPixel / 8;
            Camera.Parameters _parameters = _Camera.getParameters();
            _parameters.setPreviewSize(_size.width, _size.height);
            _parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            _Camera.setParameters(_parameters);
            _View_Source = new byte[_source_Size];
            _ImageR = createBitmap(_size); _ImageB = createBitmap(_size); _ImageG = createBitmap(_size);
            _ImageVR.setImageBitmap(_ImageR); _ImageVB.setImageBitmap(_ImageB); _ImageVG.setImageBitmap(_ImageG);
            _Camera.addCallbackBuffer(_View_Source);
            _Camera.startPreview();
        } catch (IOException io) {
            _Camera.release();

            if (_Camera != null) _Camera = null;
            io.printStackTrace();
        }
    }
    private Bitmap createBitmap(Camera.Size _size) {
        return Bitmap.createBitmap(_size.width, _size.height, Bitmap.Config.ARGB_8888);
    }

    /**
     * method to try find best resolution for our image from prepared textureView
     * @param width texture preview S_W
     * @param height texture preview S_H
     * @return
     */
    private Camera.Size findBestResolution(int width, int height) {
        List<Camera.Size> _size = _Camera.getParameters().getSupportedPictureSizes();
        //find max resolution
        Camera.Size _cSize = _Camera.new Size(0, 0);
        for (Camera.Size size : _size) {
            if ((size.width <= width) && (size.height <= height) &&
                    (size.width >= _cSize.width) && (size.height >= _cSize.height)) {
                _cSize = size;
            }
        }
        //try find size of preview thar will be <= window size
        if (_cSize.width == 0 || _cSize.height == 0) {
            _cSize = _size.get(0);
        }
        return _cSize;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (_Camera != null) {
            _Camera.stopPreview();
            _Camera.release();
            //delete references on objects
            _Camera = null;
            _View_Source = null;
            _ImageR.recycle(); _ImageG.recycle(); _ImageB.recycle();
            _ImageVG = null; _ImageVR = null; _ImageVB = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
