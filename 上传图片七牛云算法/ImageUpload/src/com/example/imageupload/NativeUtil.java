package com.example.imageupload;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;

public class NativeUtil {
    private static int DEFAULT_QUALITY = 95;

    /**
     * @param bit      bitmap����
     * @param fileName ָ������Ŀ¼��
     * @param optimize �Ƿ���ù����������ݼ��� Ʒ�����5-10��
     * @Description: JNI����ѹ��
     */
    public static void compressBitmap(Bitmap bit, String fileName, boolean optimize) {
        saveBitmap(bit, DEFAULT_QUALITY, fileName, optimize);
    }

    /**
     * @param image    bitmap����
     * @param filePath Ҫ�����ָ��Ŀ¼
     * @Description: ͨ��JNIͼƬѹ����Bitmap���浽ָ��Ŀ¼
     */
    public static void compressBitmap(Bitmap image, String filePath) {
        // ���ͼƬ��С 1000KB
        int maxSize = 1000;
        // ��ȡ�ߴ�ѹ������
        int ratio = NativeUtil.getRatioSize(image.getWidth(), image.getHeight());
        // ѹ��Bitmap����Ӧ�ߴ�
        Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio, image.getHeight() / ratio, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
        canvas.drawBitmap(image, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
        int options = 100;
        result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // ѭ���ж����ѹ����ͼƬ�Ƿ�������ֵ,���ڼ���ѹ��
        while (baos.toByteArray().length / 1024  > maxSize) {
            // ����baos�����baos
            baos.reset();
            // ÿ�ζ�����10
            options -= 10;
            // ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
            result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        // JNI���ñ���ͼƬ��SD�� ����ؼ�
        NativeUtil.saveBitmap(result, options, filePath, true);
        // �ͷ�Bitmap
        if (result != null && !result.isRecycled()) {
            result.recycle();
            result = null;
        }
    }

    /**
     * �������ű�
     *
     * @param bitWidth  ��ǰͼƬ���
     * @param bitHeight ��ǰͼƬ�߶�
     * @return
     * @Description:��������
     */
    public static int getRatioSize(int bitWidth, int bitHeight) {
        // ͼƬ���ֱ���
        int imageHeight = 1920;
        int imageWidth = 1080;
        // ���ű�
        int ratio = 1;
        // ���ű�,�����ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
        if (bitWidth > bitHeight && bitWidth > imageHeight) {
            // ���ͼƬ��ȱȸ߶ȴ�,�Կ��Ϊ��׼
            ratio = bitWidth / imageHeight;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // ���ͼƬ�߶ȱȿ�ȴ��Ը߶�Ϊ��׼
            ratio = bitHeight / imageHeight;
        }
        // ��С����Ϊ1
        if (ratio <= 0)
            ratio = 1;
        return ratio;
    }

    /**
     * ����native����
     *
     * @param bit
     * @param quality
     * @param fileName
     * @param optimize
     * @Description:��������
     */
    private static void saveBitmap(Bitmap bit, int quality, String fileName, boolean optimize) {
        compressBitmap(bit, bit.getWidth(), bit.getHeight(), quality, fileName.getBytes(), optimize);
    }

    /**
     * ���õײ� bitherlibjni.c�еķ���
     *
     * @param bit
     * @param w
     * @param h
     * @param quality
     * @param fileNameBytes
     * @param optimize
     * @return
     * @Description:��������
     */
    private static native String compressBitmap(Bitmap bit, int w, int h, int quality, byte[] fileNameBytes,
                                                boolean optimize);

    /**
     * ����lib������so�ļ�
     */
    static {
        System.loadLibrary("jpegbither");
        System.loadLibrary("bitherjni");
    }
}