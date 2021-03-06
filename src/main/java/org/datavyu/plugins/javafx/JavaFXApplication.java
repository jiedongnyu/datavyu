package org.datavyu.plugins.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by jesse on 10/21/14.
 */
public class JavaFXApplication extends Application {

    File dataFile;
    boolean init = false;
    MediaPlayer mp;
    MediaView mv;
    Stage stage;
    long duration = -1;
    long lastSeekTime = -1;

    public JavaFXApplication(File file) {
        dataFile = file;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void seek(long time) {
        if (lastSeekTime == time) {

        } else {
            System.out.println("SEEKING TO: " + time);

            mp.seek(Duration.millis(time));
            lastSeekTime = time;
        }
    }

    public void pause() {
        mp.pause();
    }

    public void play() {
        mp.play();
    }

    public void stop() {
        mp.stop();
    }

    public long getCurrentTime() {
        return (long) mp.getCurrentTime().toMillis();
    }

    public float getFrameRate() {
        return (float) 30;
    }

    public long getDuration() {
        if (duration == -1) {
            duration = (long) mp.getTotalDuration().toMillis();
        }
        return duration;
    }

    public float getRate() {
        return (float) mp.getRate();
    }

    public void setRate(float rate) {
        mp.setRate((double) rate);
    }

    public double getHeight() {
        return mp.getMedia().getHeight();
    }

    public double getAspectRatio() {
        return mp.getMedia().getWidth() / mp.getMedia().getHeight();
    }

    public void setScale(double scale) {
//        mv.setScaleX(scale);
//        mv.setScaleY(scale);

        stage.setHeight(mp.getMedia().getHeight() * scale);
        stage.setWidth(mp.getMedia().getWidth() * scale);
    }

    public void setVisible(final boolean visible) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mv.setVisible(visible);
                if (!visible) {
                    mp.setMute(true);
                    stage.hide();
                } else {
                    mp.setMute(false);
                    stage.show();
                }
            }
        });

    }

    public void setVolume(double volume) {
        mp.setVolume(volume);
    }

    public boolean isInit() {
        return init;
    }

    public void closeAndDestroy() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.close();
//                mp.dispose();
            }
        });

    }

    public void start(final Stage primaryStage) {
        stage = primaryStage;

        final Media m = new Media(dataFile.toURI().toString());
        mp = new MediaPlayer(m);
        mp.setOnReady(new Runnable() {
            @Override
            public void run() {
                mv = new MediaView(mp);


                final DoubleProperty width = mv.fitWidthProperty();
                final DoubleProperty height = mv.fitHeightProperty();

                width.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
                height.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));


                mv.setPreserveRatio(true);

                StackPane root = new StackPane();
                root.getChildren().add(mv);

                final Scene scene = new Scene(root, 960, 540);
                scene.setFill(Color.BLACK);

                primaryStage.setScene(scene);
                primaryStage.setTitle(dataFile.getName());
//        primaryStage.setFullScreen(true);
                primaryStage.show();


                System.out.println("Setting init to true");
                System.out.println(mp.getTotalDuration().toMillis());

                init = true;
                System.out.println(init);
            }
        });


    }

}
