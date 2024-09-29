package com.photostickers.customComponents;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.photostickers.R;
import com.photostickers.helpers.Constants;
import com.photostickers.helpers.ExifUtil;
import com.photostickers.helpers.ImageHelper;
import com.photostickers.helpers.MultiTouchController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class StickerTextArea extends View implements MultiTouchController.MultiTouchObjectCanvas<StickerTextArea.Img> {


    public static final int TYPE_TEXT = 2;
    OnSelectInterface listener;

    public Context context;


    public ArrayList<Img> images = new ArrayList<>();

    int currentImageId = -1;

    int nextImageID = 0;

    // --
    private MultiTouchController<Img> multiTouchController = new MultiTouchController<>(this);

    // --
    private MultiTouchController.PointInfo currTouchPoint = new MultiTouchController.PointInfo();


    private int UIMode = UI_MODE_ROTATE;

    // --
    private Paint linePaintTouchPointCircle = new Paint();

    public void initSqareOverImage() {
//        linePaintTouchPointCircle.setColor(Color.TRANSPARENT); //Ovo je pravougaonik koji se iscrtava na ivicama slike
        linePaintTouchPointCircle = new Paint();
        linePaintTouchPointCircle.setColor(Color.RED);
        linePaintTouchPointCircle.setStrokeWidth(5);
        linePaintTouchPointCircle.setStyle(Paint.Style.STROKE);
        linePaintTouchPointCircle.setAntiAlias(true);
    }

    // ---------------------------------------------------------------------------------------------------
    float scale = 0;

    //Ovo je canvas koji crta
    public Canvas mCanvas;

    /******** FLEGOVI *********/
    private static final int UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;

    public boolean firstInit; //Ako se prvi put ubacuju slike na radnu povrsinu, treba ih rasporediti po nekom algoritmu

    public static boolean firstCanvasInit = true; //Samo prvi put, pri prvom pokretanju, u funkciji onSizeChange treba inicijalizovati canvas

    public static boolean isImageSelected = false; //Fleg koji ce nam pomoci u editorActivity da odkrijemo da li je nesto selectovano i time onemogucimo nullPointerException

    public static float lastX, lastY, lastScaleX = 1, lastScaleY = 1, lastAngle = 0f; //Ako treba postaviti sliku na mesto zadnje postavljene, ovim dobijamo poziciju

    public static boolean lookAtLastValues = false; //Da li treba da se postavi na mesto poslednje slike ili ne

    private static float SCREEN_MARGIN = 100;


    public StickerTextArea(Context context) {
        this(context, null);
        this.context = context;
        initSqareOverImage();
        flag = true;
    }


    public void setScale() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double inches = Math.sqrt((metrics.widthPixels * metrics.widthPixels) + (metrics.heightPixels * metrics.heightPixels)) / metrics.densityDpi;
        scale = metrics.density;

        if (inches > 9) {
            scale *= 2;
        } else if (inches > 6) {
            scale *= 1.5;
        }
        SCREEN_MARGIN = metrics.widthPixels / 10 - 10;
    }

    public StickerTextArea(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
        setScale();
        initSqareOverImage();
        flag = true;
    }

    public StickerTextArea(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setScale();
        initSqareOverImage();
        flag = true;
    }


    public void setWidth(float centerContainerWidth) {
        this.myWidth = centerContainerWidth;
    }

    public void setHeight(float centerContainerHeight) {
        this.myHeight = centerContainerHeight;
    }

    public float myWidth;
    public float myHeight;
    private Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        FaceDetector detector = new FaceDetector.Builder(getContext()).setTrackingEnabled(true).setLandmarkType(FaceDetector.ALL_LANDMARKS).setMode(FaceDetector.ACCURATE_MODE).build();

        if (detector.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            mFaces = detector.detect(frame);
            detector.release();
        }

        invalidate();
    }

    private SparseArray<Face> mFaces;

    public double drawBitmap(Canvas canvas) {
        return Math.min(canvas.getWidth() / mBitmap.getWidth(), canvas.getHeight() / mBitmap.getHeight());
    }

    boolean flag;

    private void drawFaceBox(double scale) {
        if (flag) {
            if (getResources().getBoolean(R.bool.useFaceDetect)) {
                for (int i = 0; i < mFaces.size(); i++) {
                    Face face = mFaces.valueAt(i);

                    if (Constants.getInstance().stickers != null && Constants.getInstance().stickers.size() > 0) {
                        addImage(Constants.getInstance().stickers.get(new Random().nextInt(Constants.getInstance().stickers.size())), (float) scale, face);
                    }
                }
                if (mFaces.size() == 0) {
                    CustomDialogOk cdd = new CustomDialogOk((Activity) context, context.getString(R.string.recoqnitionFailed));
                    cdd.show();
                }
            }
            flag = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            //drawFaceLandmarks(canvas, scale);
            drawFaceBox(scale);
        }
        if (images != null) {
            int n = images.size();
            for (int i = 0; i < n; i++) {
                //if (images.get(i).resourceType == TYPE_PHOTO) //Ako je Img SLIKA
                {
                    images.get(i).draw(canvas);
                }
            }
        }
    }


    /**
     * Pass touch events to the MT controller
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return multiTouchController.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (firstCanvasInit) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bitmap);
            firstCanvasInit = false;
        }
    }

    /**
     * Get the image that is under the single-touch point, or return null (canceling the drag op) if none
     */
    public Img getDraggableObjectAtPoint(MultiTouchController.PointInfo pt) {
        int n = images.size();

        float x = pt.getX(), y = pt.getY();
        for (int i = n - 1; i >= 0; i--) {
            Img im = images.get(i);
            if (im.containsPoint(x, y)) {
                return im;
            }
        }

        return null; //Ovo je null jer sam promenio MultiTouchController da ne postoji mogucnost da nesto nije selektovano
    }


    public Img getSelectedImage() {
        for (Img i : images) {
            if (i.id == currentImageId) {
                return i;
            }
        }
        return null;
    }

    public void selectLastImage() {
        int n = images.size();

        Img image = images.get(n - 1);
        currentImageId = image.id;//Ovde se dodeljuje id slike
        if (listener != null) {
            listener.onSelectImage(currentImageId);
        }
        images.remove(image);
        images.add(image);
        lastX = image.getCenterX();
        lastY = image.getCenterY();
        lastScaleX = image.getScaleX();
        lastScaleY = image.getScaleY();
        lastAngle = image.getAngle();
        isImageSelected = true;
    }


    /**
     * Select an object for dragging. Called whenever an object is found to be under the point (non-null is returned by getDraggableObjectAtPoint()) and a drag operation is starting. Called with
     * null when drag op ends.
     */

    public void selectObject(Img img, MultiTouchController.PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        if (img != null) {
            // Move image to the top of the stack when selected
            currentImageId = img.id;/**Ovde se dodeljuje id slike**/
            if (listener != null) {
                listener.onSelectImage(currentImageId);
            }
            /*images.remove(img);
            images.add(img);*/
            lastX = img.getCenterX();
            lastY = img.getCenterY();
            lastScaleX = img.getScaleX();
            lastScaleY = img.getScaleY();
            lastAngle = img.getAngle();

            isImageSelected = true;
        }
        invalidate();
    }

    /**
     * Get the current position and scale of the selected image. Called whenever a drag starts or is reset.
     */
    public void getPositionAndScale(Img img, MultiTouchController.PositionAndScale objPosAndScaleOut) {
        // FIXME affine-izem (and fix the fact that the anisotropic_scale part requires averaging the two scale factors)
        objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(), (UIMode & UI_MODE_ANISOTROPIC_SCALE) == 0, (img.getScaleX() + img.getScaleY()) / 2, (UIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(), img.getScaleY(), (UIMode & UI_MODE_ROTATE) != 0, img.getAngle());
    }


    /**
     * Set the position and scale of the dragged/stretched image.
     */
    public boolean setPositionAndScale(Img img, MultiTouchController.PositionAndScale newImgPosAndScale, MultiTouchController.PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        boolean ok = img.setPos(newImgPosAndScale);
        if (ok) {
            invalidate();
        }
        return ok;
    }
    // ----------------------------------------------------------------------------------------------


    //Ova pomocna klasa je jako bitna jer cuva informacije o slici koja je postavljena na ekran, njenu velicinu, poziciju, itd
    public class Img {

        private int id;
        int resourceType;//Ovaj flag ce pokazivati da li je u pitanju: 0-Sticker   , 1-Slika ,   2- Text
        Object characteristic;
        int filterPosition = 38; //Filter koji se primenjuje nad slikom ako je u pitanju slika uopste
        String path;
        private int resourceId;
        public Bitmap b; //U pocetku native, kasnije sa filterom ako je primenjen
        Bitmap nativeBitmap; //Prirodna bitmapa koja je ucitana

        public Drawable drawable;

        private boolean firstLoad = true;

        int width, height, displayWidth, displayHeight;

        float centerX, centerY, scaleX, scaleY, angle;

        float minX, maxX, minY, maxY;

        com.google.android.gms.vision.face.Face face;

        float myScale;

        boolean flipX;

        boolean flipY;

        public Matrix mMatrix;

        Img(int id, String path, int rId, Resources res, Face face, float myScale) {
            mMatrix = new Matrix();
            this.id = id;
            this.path = path;
            this.resourceId = rId;
            //this.firstLoad = true;
            this.characteristic = face;
            nativeBitmap = null;
            filterPosition = -1;
            this.face = face;
            this.myScale = myScale;
            getMetrics(res);
        }

        Img(int id, String path, int rId, Resources res, int t, Object characteristic) {
            mMatrix = new Matrix();
            this.id = id;
            this.path = path;
            this.resourceId = rId;
            //this.firstLoad = true;
            this.resourceType = t;
            this.characteristic = characteristic;
            flipX = flipY = false;
            nativeBitmap = null;
            filterPosition = 38;
            getMetrics(res);
        }

        Img(int id, Bitmap bmp, int rId, Resources res, int t, Object characteristic) {
            mMatrix = new Matrix();
            this.id = id;
            this.b = bmp;
            this.resourceId = rId;
            this.resourceType = t;
            this.firstLoad = true;
            this.characteristic = characteristic;
            flipX = flipY = false;
            nativeBitmap = null;
            filterPosition = 38;
            getMetrics(res);
        }


        private void getMetrics(Resources res) {
            DisplayMetrics metrics = res.getDisplayMetrics();

            this.displayWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.max(metrics.widthPixels, metrics.heightPixels) : Math.min(metrics.widthPixels, metrics.heightPixels);
            this.displayHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.min(metrics.widthPixels, metrics.heightPixels) : Math.max(metrics.widthPixels, metrics.heightPixels);
        }

        /**
         * Called by editorActivity's onResume() method to load the images
         */
        void load(Resources res) {
            if (firstLoad) {
                getMetrics(res);
                if (this.resourceId > 0) {
                    this.b = ImageHelper.decodeSampledBitmapFromResource(res, this.resourceId, (int) (250 * scale + 0.5f), (int) (250 * scale + 0.5f));
                    if (path != null) {
                        this.b = ExifUtil.rotateBitmap(path, getContext(), b);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        this.b = ImageHelper.decodeSampledBitmapFromResource(Uri.parse(path), getContext(), (int) (125 * scale + 0.5f), (int) (125 * scale + 0.5f));
                    else
                        this.b = ImageHelper.decodeSampledBitmapFromResource(path, (int) (125 * scale + 0.5f), (int) (125 * scale + 0.5f));
                    if (path != null) {
                        this.b = ExifUtil.rotateBitmap(path, getContext(), b);
                    }
                }
                this.drawable = new BitmapDrawable(getResources(), this.b);
                this.width = drawable.getIntrinsicWidth();
                this.height = drawable.getIntrinsicHeight();
                float cx, cy, sx, sy;
                float sc = 1f;

                if (face != null) {
                    float left, top, right, bottom;
                    left = face.getPosition().x * myScale;
                    top = face.getPosition().y * myScale;
                    right = myScale * (face.getPosition().x + face.getWidth());
                    bottom = myScale * (face.getPosition().y + face.getHeight());
                    cx = (right + left) / 2;
                    cy = (bottom + top) / 2;
                    sx = (right - left) / this.width;
                    sy = (bottom - top) / this.height;
                    sc = Math.max(sx, sy);

                } else {
                    cx = myWidth / 2;
                    cy = myHeight / 2;
//                    sc = Math.max(myWidth / (float) width, myHeight / (float) height);
                }

                sx = sy = sc;
                firstLoad = false;
                if (firstInit && lookAtLastValues) //U slucaju da prvi put ucittavamo slike, neophodno je slike postaviti po algoritmu koji nam je dao Boza
                {
                    setPos(lastX, lastY, sx, sy, 0.0f);
                    lookAtLastValues = false;
                } else if (lookAtLastValues) {
                    setPos(lastX, lastY, lastScaleX, lastScaleY, lastAngle);
                    lookAtLastValues = false;
                } else {
                    setPos(cx, cy, sx, sy, 0.0f);
                }
            }
        }

        void load(Bitmap bmp) {
            if (firstLoad) {
                //getMetrics(res);
                this.b = bmp;
                this.drawable = new BitmapDrawable(getResources(), this.b);
                this.width = drawable.getIntrinsicWidth();
                this.height = drawable.getIntrinsicHeight();
                float cx, cy, sx, sy;

                cx = myWidth / 2;
                cy = myHeight / 2;

                float sc = 1f;
//                sc = Math.max(myWidth / (float) width, myHeight / (float) height);
                sx = sy = sc;
                firstLoad = false;
                if (lookAtLastValues) {
                    setPos(lastX, lastY, lastScaleX, lastScaleY, lastAngle);
                    lookAtLastValues = false;
                } else {
                    setPos(cx, cy, sx, sy, 0.0f);
                }
            }
        }


        /**
         * Called by editorActivity's onPause() method to free memory used for loading the images
         */
        void unload() {
            this.drawable = null;
            if (this.b != null) {
                this.b.recycle();
                this.b = null;
            }
            if (this.nativeBitmap != null) {
                this.nativeBitmap.recycle();
                this.nativeBitmap = null;
            }
            characteristic = null;
            System.gc();
        }


        /**
         * Set the position and scale of an image in screen coordinates
         */
        boolean setPos(MultiTouchController.PositionAndScale newImgPosAndScale) {
            return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(), (UIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleX() : newImgPosAndScale.getScale(), (UIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleY() : newImgPosAndScale.getScale(), newImgPosAndScale.getAngle());
        }

        /**
         * Set the position and scale of an image in screen coordinates
         */
        boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) //Ovom funkcijom postavljamo poziciju slici, to jest gde treba da se iscrta
        {
            float ws = (width / 2) * scaleX, hs = (height / 2) * scaleY; //Dobijemo polovinu visine i sirine slike
            float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX + ws, newMaxY = centerY + hs; //minX,minY,maxX i maxY su najmanje vrednosti koje zauzme slika ako je ne rotiramo, i sluzi nam da proveravamo da li je slika totalno izasla iz radne povrsine

            float widthForCheck, heightForCheck;
            {
                widthForCheck = myWidth;
                heightForCheck = myHeight;
            }
            if (!firstInit && (newMinX > widthForCheck - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN || newMinY > heightForCheck - SCREEN_MARGIN || newMaxY < SCREEN_MARGIN)) {
                this.centerX = lastX;
                this.centerY = lastY;
                this.scaleX = lastScaleX;
                this.scaleY = lastScaleY;
                this.angle = lastAngle;
                return false;
            }
            float scaleValue = 3f; //Tekst ne sme mnogo da se povecava jer font onda ispadne krzav, iz tog razloga uvodimo ovu skalu
            if (((scaleX > scaleValue && !firstInit) || (scaleY > scaleValue && !firstInit) || (scaleX < 0.1 && !firstInit) || (scaleY < 0.1 && !firstInit))) //FIXME
            {
                this.centerX = lastX;
                this.centerY = lastY;
                this.scaleX = lastScaleX;
                this.scaleY = lastScaleY;
                this.angle = lastAngle;
                return false;
            }

            lastX = centerX; //Prvo pravimo kopiju vrednostima koje ce nam mozda trebati da pozicioniramo sledecu unetu sliku
            lastY = centerY;
            lastScaleX = scaleX;
            lastScaleY = scaleY;
            lastAngle = angle;

            this.centerX = centerX;
            this.centerY = centerY;
            this.scaleX = scaleX > 3f ? 3f : scaleX;
            this.scaleY = scaleY > 3f ? 3f : scaleY;
            this.scaleX = scaleX < 0.1f ? 0.1f : scaleX;
            this.scaleY = scaleY < 0.1f ? 0.1f : scaleY;
            this.angle = angle;
            this.minX = newMinX;
            this.minY = newMinY;
            this.maxX = newMaxX;
            this.maxY = newMaxY;
            return true;
        }


        boolean pointInRotatedRect(float[] point, RectF bound, float rot) {
            Matrix m = new Matrix();
            float[] p = Arrays.copyOf(point, 2);
            m.setRotate(rot, bound.centerX(), bound.centerY());
            Matrix m0 = new Matrix();
            if (!m.invert(m0)) {
                return false;
            }
            m0.mapPoints(p);
            return inclusiveContains(bound, p[0], p[1]);
        }

        boolean inclusiveContains(RectF r, float x, float y) {
            return !(x > r.right || x < r.left || y > r.bottom || y < r.top);
        }

        /**
         * Return whether or not the given screen coords are inside this image
         */
        /*LINK DO RESENA https://android.googlesource.com/platform/packages/apps/Camera2/+/idea133/src/com/android/camera/crop/CropMath.java*/
        boolean containsPoint(float scrnX, float scrnY) {
            return pointInRotatedRect(new float[]{scrnX, scrnY}, new RectF(minX, minY, maxX, maxY), (float) (angle * 180 / Math.PI));
        }

        void draw(Canvas canvas) {
            if (drawable != null) {
                canvas.save();
//                canvas.setMatrix(mMatrix);
                float dx = (maxX + minX) / 2;
                float dy = (maxY + minY) / 2;

                drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
                canvas.translate(dx, dy);
                canvas.rotate(angle * 180.0f / (float) Math.PI);
                canvas.translate(-dx, -dy);
                if (id == currentImageId) {
                    canvas.drawRect((int) minX, (int) minY, (int) maxX, (int) maxY, linePaintTouchPointCircle);
                }
                drawable.draw(canvas);
                canvas.restore();
            } else {
                Log.v("testLog", "drawable null");
            }

        }

        public Drawable getDrawable() {
            return drawable;
        }


        public int getHeight() {
            return height;
        }

        float getCenterX() {
            return centerX;
        }

        float getCenterY() {
            return centerY;
        }

        public float getScaleX() {
            return scaleX;
        }

        public float getScaleY() {
            return scaleY;
        }

        float getAngle() {
            return angle;
        }


    }

    public void addImage(int resId, float myScale, Face face) {
        nextImageID++;
        //images.add(new Img(nextImageID, null, resId, this.context.getResources(), resType,characteristic));
        images.add(new Img(nextImageID, "", resId, this.context.getResources(), face, myScale));
        images.get(images.size() - 1).load(context.getResources());
        invalidate();
    }

    public void addImage(int resId, int resType, Object characteristic) {
        nextImageID++;
        //images.add(new Img(nextImageID, null, resId, this.context.getResources(), resType,characteristic));
        images.add(new Img(nextImageID, "", resId, this.context.getResources(), resType, characteristic));
        images.get(images.size() - 1).load(context.getResources());
        invalidate();
    }


    public void addImage(Bitmap bmp, int resType, Object characteristic) {
        nextImageID++;
        images.add(new Img(nextImageID, bmp, 0, this.context.getResources(), resType, characteristic));
        images.get(images.size() - 1).load(bmp);
        invalidate();
    }

    public void removeSelected() {
        for (int i = 0; i < images.size(); i++) {
            if (currentImageId == images.get(i).id) {
                images.get(i).unload();
                images.remove(i);
                nothingSelected();
                break;
            }
        }

    }

    @Override
    public void nothingSelected() {
        // TODO Auto-generated method stub
        currentImageId = -1;
        if (listener != null) {
            listener.onSelectImage(currentImageId);
        }
        //PhotoStudio.getInstance().selectedPath="";
        isImageSelected = false;
        invalidate();
    }

    public interface OnSelectInterface {

        void onSelectImage(int currentImageId);
    }

    Bitmap bmpForText;

    public void makeBitmapByText(String t, int color, String font) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            display.getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            int dens = dm.densityDpi;
            double wi = (double) width / (double) dens;
            double hi = (double) height / (double) dens;
            double x = Math.pow(wi, 2);
            double y = Math.pow(hi, 2);
            int screenInches = (int) Math.sqrt(x + y);
            int textSize = (int) (context.getResources().getDimension(R.dimen.fontSize)) * 2;
            if (screenInches >= 9) {
                textSize *= 2; //Mozda ovde treba da se doda jos *2
            }

            Paint myPaint = new Paint();
            Typeface type2 = null;
            if (font != null) {
                switch (font) {
                    case "normal":
                        type2 = Typeface.create((Typeface) null, Typeface.NORMAL);
                        break;
                    case "bold":
                        type2 = Typeface.create((Typeface) null, Typeface.BOLD);
                        break;
                    case "italic":
                        type2 = Typeface.create((Typeface) null, Typeface.ITALIC);
                        break;
                    default:
                        try {
                            type2 = Typeface.createFromAsset(context.getAssets(), "fonts/" + font); //Uzimamo font koji nam je prosledjen
                        } catch (Exception ex) {
                            type2 = Typeface.create((Typeface) null, Typeface.NORMAL);
                        }
                        break;
                }

            }
            myPaint.setTypeface(type2);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeJoin(Paint.Join.ROUND);
            myPaint.setStrokeCap(Paint.Cap.ROUND);

            myPaint.setStrokeWidth(0);
            myPaint.setStyle(Paint.Style.FILL);
            if (color == 0) {
                myPaint.setColor(Color.CYAN);
            } else {
                myPaint.setColor(color);
            }

            myPaint.setTextSize(textSize);
            myPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            Rect bounds = new Rect();
            myPaint.getTextBounds(t, 0, t.length(), bounds);

            int maxlengthPerLine = 0; //Brojimo nejvecu liniju teksta
            for (String line : t.split("\n")) {
                maxlengthPerLine = maxlengthPerLine < line.length() ? line.length() : maxlengthPerLine;
            }
            Rect maxWidth = new Rect();
            myPaint.getTextBounds(t.substring(0, maxlengthPerLine), 0, maxlengthPerLine, maxWidth);

            if (maxWidth.width() == 0) //krajnji slucaj, kad sam obrisao sav tekst
            {
                bmpForText = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
            } else {
                try {
                    bmpForText = Bitmap.createBitmap((int) (maxWidth.width() + textSize / 1.3f), bounds.height() * t.split("\n").length + textSize / 2  /*+textSize * t.split("\n").length / 8*/, Bitmap.Config.ARGB_4444); //Pravljenje bitmape koja je koliko toliko srazmerna tekstu
                } catch (Exception ex) {
                    bmpForText = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
                }
            }
            Canvas myCanvas = new Canvas(bmpForText);
            int k = 0;
            double X = maxWidth.width() / 2f - maxWidth.width() / 2f - maxWidth.left;
            double Y = bounds.height() / 2f + bounds.height() / 2f - bounds.bottom;
            for (String line : t.split("\n")) {
                myCanvas.drawText(line, (int) X + textSize / 4, (int) Y + k++ * textSize + textSize / 4, myPaint); //Iscrtavamo outline
            }

            addImage(bmpForText.copy(bmpForText.getConfig(), true), TYPE_TEXT, null);

            invalidate();
            bmpForText.recycle();
            bmpForText = null;
        }
    }
}