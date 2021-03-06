package edu.tjrac.swant.arcore.solar;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.yckj.baselib.common.base.BaseActivity;
import com.yckj.baselib.util.FileUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import edu.tjrac.swant.unicorn.R;

public class SolarActivity extends BaseActivity {

    private static final int RC_PERMISSIONS = 0x123;
    private boolean installRequested;

    private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;
    private ArSceneView arSceneView;

    private ModelRenderable sunRenderable, mercuryRenderable,
            venusRenderable, earthRenderable, lunaRenderable, marsRenderable,
            jupiterRenderable, saturnRenderable, uranusRenderable, neptuneRenderable;
    private ViewRenderable solarControlsRenderable;

    private final SolarSettings solarSettings = new SolarSettings();

    private boolean hasFinishedLoading = false;
    private boolean hasPlacedSolarSystem = false;
    private static final float AU_TO_METERS = 0.5f;


    String fileCachePath = FileUtils.getAppDataDir() + "arcore/solar";

    String sfbFileName[] = {"Sol.sfb", "Mercury.sfb", "Venus.sfb", "Earth.sfb", "Luna.sfb", "Mars.sfb",
            "Jupiter.sfb", "Saturn.sfb", "Uranus.sfb", "Neptune.sfb"
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar);
        arSceneView = findViewById(R.id.ar_scene_view);

        // Build all the planet models.
        CompletableFuture<ModelRenderable> sunStage =
                ModelRenderable.builder().setSource(this, getUri(0)).build();
        CompletableFuture<ModelRenderable> mercuryStage =
                ModelRenderable.builder().setSource(this, getUri(1)).build();
        CompletableFuture<ModelRenderable> venusStage =
                ModelRenderable.builder().setSource(this, getUri(2)).build();
        CompletableFuture<ModelRenderable> earthStage =
                ModelRenderable.builder().setSource(this, getUri(3)).build();
        CompletableFuture<ModelRenderable> lunaStage =
                ModelRenderable.builder().setSource(this, getUri(4)).build();
        CompletableFuture<ModelRenderable> marsStage =
                ModelRenderable.builder().setSource(this, getUri(5)).build();
        CompletableFuture<ModelRenderable> jupiterStage =
                ModelRenderable.builder().setSource(this, getUri(6)).build();
        CompletableFuture<ModelRenderable> saturnStage =
                ModelRenderable.builder().setSource(this, getUri(7)).build();
        CompletableFuture<ModelRenderable> uranusStage =
                ModelRenderable.builder().setSource(this, getUri(8)).build();
        CompletableFuture<ModelRenderable> neptuneStage =
                ModelRenderable.builder().setSource(this, getUri(9)).build();

        CompletableFuture<ViewRenderable> solarControlsStage =
                ViewRenderable.builder().setView(this, R.layout.solar_controls).build();

        CompletableFuture.allOf(
                sunStage,
                mercuryStage,
                venusStage,
                earthStage,
                lunaStage,
                marsStage,
                jupiterStage,
                saturnStage,
                uranusStage,
                neptuneStage,
                solarControlsStage
        ).handle((notUsed, throwable) -> {
            // When you build a Renderable, Sceneform loads its resources in the background while
            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
            // before calling get().

            if (throwable != null) {
                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                return null;
            }

            try {
                sunRenderable = sunStage.get();
                mercuryRenderable = mercuryStage.get();
                venusRenderable = venusStage.get();
                earthRenderable = earthStage.get();
                lunaRenderable = lunaStage.get();
                marsRenderable = marsStage.get();
                jupiterRenderable = jupiterStage.get();
                saturnRenderable = saturnStage.get();
                uranusRenderable = uranusStage.get();
                neptuneRenderable = neptuneStage.get();
                solarControlsRenderable = solarControlsStage.get();
                // Everything finished loading successfully.
                hasFinishedLoading = true;

            } catch (InterruptedException | ExecutionException ex) {
                DemoUtils.displayError(this, "Unable to load renderable", ex);
            }

            return null;
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                Log.i("on","singleTapUp");
                onSingleTap(e);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        arSceneView
                .getScene()
                .setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {
                            // If the solar system hasn't been placed yet, detect a tap and then check to see if
                            // the tap occurred on an ARCore plane to place the solar system.
                            if (!hasPlacedSolarSystem) {
                                return gestureDetector.onTouchEvent(event);
                            }

                            // Otherwise return false so that the touch event can propagate to the scene.
                            return false;
                        });

        arSceneView.getScene().setOnUpdateListener(frameTime -> {
            if (loadingMessageSnackbar == null) {
                return;
            }
            Frame frame = arSceneView.getArFrame();
            if (frame == null) {
                return;
            }
            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                return;
            }
            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    hideLoadingMessage();
                }
            }

        });

        // Lastly request CAMERA permission which is required by ARCore.
        DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);
    }

    private Uri getUri(int i) {

        File file = new File(fileCachePath, sfbFileName[i]);

        if (file.exists()) {
            if(Build.VERSION.SDK_INT>=24){
                return FileProvider.getUriForFile(mContext, "edu.tjrac.swant.unicorn.provider", file);
            }else {
                return Uri.fromFile(file);
            }
        } else {
            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = DemoUtils.hasCameraPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }
        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        arSceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!DemoUtils.hasCameraPermission(this)) {
            if (!DemoUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                DemoUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onSingleTap(MotionEvent tap) {
        if (!hasFinishedLoading) {
            // We can't do anything yet.
            return;
        }

        Frame frame = arSceneView.getArFrame();
        if (frame != null) {
            if (!hasPlacedSolarSystem && tryPlaceSolarSystem(tap, frame)) {
                hasPlacedSolarSystem = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean tryPlaceSolarSystem(MotionEvent tap, Frame frame) {
        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    // Create the Anchor.
                    Anchor anchor = hit.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arSceneView.getScene());
                    Node solarSystem = createSolarSystem();
                    anchorNode.addChild(solarSystem);
                    return true;
                }
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Node createSolarSystem() {
        Node base = new Node();

        Node sun = new Node();
        sun.setParent(base);
        sun.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));

        Node sunVisual = new Node();
        sunVisual.setParent(sun);
        sunVisual.setRenderable(sunRenderable);
        sunVisual.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        Node solarControls = new Node();
        solarControls.setParent(sun);
        solarControls.setRenderable(solarControlsRenderable);
        solarControls.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));

        View solarControlsView = solarControlsRenderable.getView();
        SeekBar orbitSpeedBar = solarControlsView.findViewById(R.id.orbitSpeedBar);
        orbitSpeedBar.setProgress((int) (solarSettings.getOrbitSpeedMultiplier() * 10.0f));
        orbitSpeedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float ratio = (float) progress / (float) orbitSpeedBar.getMax();
                        solarSettings.setOrbitSpeedMultiplier(ratio * 10.0f);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        SeekBar rotationSpeedBar = solarControlsView.findViewById(R.id.rotationSpeedBar);
        rotationSpeedBar.setProgress((int) (solarSettings.getRotationSpeedMultiplier() * 10.0f));
        rotationSpeedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float ratio = (float) progress / (float) rotationSpeedBar.getMax();
                        solarSettings.setRotationSpeedMultiplier(ratio * 10.0f);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        // Toggle the solar controls on and off by tapping the sun.
        sunVisual.setOnTapListener(
                (hitTestResult, motionEvent) -> solarControls.setEnabled(!solarControls.isEnabled()));

        createPlanet("Mercury", sun, 0.4f, 47f, mercuryRenderable, 0.019f);

        createPlanet("Venus", sun, 0.7f, 35f, venusRenderable, 0.0475f);

        Node earth = createPlanet("Earth", sun, 1.0f, 29f, earthRenderable, 0.05f);

        createPlanet("Moon", earth, 0.15f, 100f, lunaRenderable, 0.018f);

        createPlanet("Mars", sun, 1.5f, 24f, marsRenderable, 0.0265f);

        createPlanet("Jupiter", sun, 2.2f, 13f, jupiterRenderable, 0.16f);

        createPlanet("Saturn", sun, 3.5f, 9f, saturnRenderable, 0.1325f);

        createPlanet("Uranus", sun, 5.2f, 7f, uranusRenderable, 0.1f);

        createPlanet("Neptune", sun, 6.1f, 5f, neptuneRenderable, 0.074f);

        return base;
    }

    private Node createPlanet(
            String name,
            Node parent,
            float auFromParent,
            float orbitDegreesPerSecond,
            ModelRenderable renderable,
            float planetScale) {
        // Orbit is a rotating node with no renderable positioned at the sun.
        // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
        // This is done instead of making the sun rotate so each planet can orbit at its own speed.
        RotatingNode orbit = new RotatingNode(solarSettings, true);
        orbit.setDegreesPerSecond(orbitDegreesPerSecond);
        orbit.setParent(parent);

        // Create the planet and position it relative to the sun.
        Planet planet = new Planet(this, name, planetScale, renderable, solarSettings);
        planet.setParent(orbit);
        planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f));

        return planet;
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        SolarActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}
