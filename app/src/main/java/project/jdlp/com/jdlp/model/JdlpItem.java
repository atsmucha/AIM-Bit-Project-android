package project.jdlp.com.jdlp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.Format;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import project.jdlp.com.jdlp.manager.Constants;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class JdlpItem {
    private int itemPosition;
    private String itemTitle;   // socket 보낼때 char[]로 보내자 max value is 64
    private int itemBitmapSize;
    private Bitmap itemBitmap;  //byte[] 보내기
    private String itemPassword;
    private int itemExist;

    public JdlpItem(int itemPosition) {
        this.itemPosition = positioning(itemPosition);
    }

    public JdlpItem(int itemPosition, String itemTitle) { this.itemPosition = positioning(itemPosition); this.itemTitle = itemTitle; }

    public JdlpItem(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public JdlpItem(Bitmap itemBitmap) {
        this.itemBitmap = itemBitmap;
    }

    public JdlpItem(int position, String itemTitle, Bitmap itemBitmap) {
        this.itemPosition = positioning(position);
        this.itemTitle = itemTitle;
        this.itemBitmap = itemBitmap;
    }

    public JdlpItem(int itemPosition, String itemTitle, Bitmap itemBitmap, String password) {
        this.itemPosition = positioning(itemPosition);
        this.itemTitle = itemTitle;
        this.itemBitmap = itemBitmap;
        this.itemPassword = password;
    }

    public int getItemPosition() { return itemPosition; }

    public String getItemTitle() {
        return itemTitle;
    }

    public Bitmap getItemBitmap() { return this.itemBitmap; }

    public int getItemExist() { return itemExist; }

    public String getItemPassword() { return itemPassword; }

    public int getItemBitmapSize() { return itemBitmapSize; }

    public void setItemPosition(int position) { this.itemPosition = position; }

    public boolean setItemTitle(String itemTitle) {
        if(itemTitle.length() <= 64) {
            this.itemTitle = itemTitle;
            return true;
        } else {
            Log.e("JdlpItem::", "title은 64이하로 설정");
            return false;
        }
    }

    public void setItemBitmap(Bitmap itemBitmap) {
        this.itemBitmap = itemBitmap;
        if(itemBitmap != null) this.setItemBitmapSize(bitmapToByteArray(itemBitmap).length);
    }

    public void setItemBitmapSize(int itemBitmapSize) {
        this.itemBitmapSize = itemBitmapSize;
    }

    public boolean setItemPassword(String password) {
        if(password.length() <= 64) {
            this.itemPassword = password;
            return true;
        } else {
            Log.e("JdlpItem::", "Pass는 64이하로 설정");
            return false;
        }
    }

    public void setItemExist(int itemExist) {
        this.itemExist = itemExist;
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] result = byteArrayOutputStream.toByteArray();
//        Log.e("test", "comecomecomecome");
//        for(int i = 0; i < 1000; i++) {
//            Log.e("itemBit->byte", "["+i+"]\t\t"+result[i] + "");
//        }
        return result;

//        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
//        bitmap.copyPixelsToBuffer(buffer);
//        return buffer.array();
    }

    public Bitmap byteArrayToBitmap(byte[] imgByteArray) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 3;
        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;
//        opt.inPurgeable = true;
//        opt.inJustDecodeBounds = true;
//        byte[] result = imgByteArray;
//        Log.e("test", "comecomecomecome");
//        for(int i = 0; i < 1000; i++) {
//            Log.e("itemBit->byte", "["+i+"]\t\t"+result[i] + "");
//        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length, opt);
        return bitmap;

//        byte[] bytesImage = imgByteArray;
//        int intByteCount = bytesImage.length;
//        int[] intColors = new int[intByteCount / 3];
//        int intWidth = 2;
//        int intHeight = 2;
//        final int intAlpha = 255;
//        if ((intByteCount / 3) != (intWidth * intHeight)) {
//            throw new ArrayStoreException();
//        }
//        for (int intIndex = 0; intIndex < intByteCount - 2; intIndex = intIndex + 3) {
//            intColors[intIndex / 3] = (intAlpha << 24) | (bytesImage[intIndex] << 16) | (bytesImage[intIndex + 1] << 8) | bytesImage[intIndex + 2];
//        }
//        Bitmap bmpImage = Bitmap.createBitmap(intColors, intWidth, intHeight, Bitmap.Config.ARGB_8888);
//        return bmpImage;
    }

    @Override
    public String toString() {
        return "JdlpItem{" +
                "itemPosition=" + itemPosition +
                ", itemTitle='" + itemTitle + '\'' +
                ", itemBitmapSize=" + itemBitmapSize +
                ", itemBitmap=" + itemBitmap +
                ", itemPassword='" + itemPassword + '\'' +
                ", itemExist=" + itemExist +
                '}';
    }

    private Integer positioning(int position) {
        switch(position) {
            case 0:
                return 6;
            case 1:
                return 7;
            case 2:
                return 8;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 0;
            case 7:
                return 1;
            case 8:
                return 2;
        }
        return null;
    }

}
