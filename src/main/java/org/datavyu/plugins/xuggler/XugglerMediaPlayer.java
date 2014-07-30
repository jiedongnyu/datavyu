package org.datavyu.plugins.xuggler;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;
import com.xuggle.xuggler.demos.VideoImage;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jmlin_000 on 7/5/2014.
 */
public class XugglerMediaPlayer implements Runnable {

    private VideoImage mScreen;
    private IContainer container;
    private IMediaReader reader;
    private IMediaViewer viewer;
    private IPacket packet;

    private IVideoResampler resampler = null;
    private int videoStreamId;
    private int audioStreamId;
    private long mSystemVideoClockStartTime;
    private long mFirstVideoTimestampInStream;
    private File data;
    private boolean playing = false;

    private float fps;
    private double timebase;
    private IVideoPicture picture = null;


    private ConcurrentLinkedQueue<IPacket> videoQueue;
    private ConcurrentLinkedQueue<IPacket> audioQueue;

    private long lastFrameTime = 0;

    private SourceDataLine mLine;

    public XugglerMediaPlayer() {
        // Let's make sure that we can actually convert video pixel formats.
        if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
            throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");

        // Create a Xuggler container object
        container = IContainer.make();

        videoQueue = new ConcurrentLinkedQueue<IPacket>();
        audioQueue = new ConcurrentLinkedQueue<IPacket>();
    }

    public static void main(String[] argv) {
        XugglerMediaPlayer mp = new XugglerMediaPlayer();
        mp.setDataFeed(new File("C:/Users/jmlin_000/Desktop/h264_720p_mp_3.1_3mbps_aac_shrinkage.mp4"));
    }

    public void run() {

    }

    private long readFrame() {
        return readFrame(true);
    }

    private long readFrame(boolean display) {
//        System.out.println("READING NEXT PACKET");
        // We don't want decoding to get too far ahead
        if (videoQueue.size() > 50 || audioQueue.size() > 50) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1L;
        }

        if (container.readNextPacket(packet) < 0) {
            System.err.println("Error reading packet");
            return -1L;
        }
//        System.out.println("READ");


        if (packet.getStreamIndex() == videoStreamId) {
            try {
//                System.out.println("PUTTING");
                // TODO to solve memory issues here make a circular buffer of packets
                videoQueue.add(IPacket.make(packet, true));
//                System.out.println("PUT");
            } catch (Exception e) {
                e.printStackTrace();
            }
//            lastFrameTime = vp.getTimeStamp() / 1000;
//            if (vp.isComplete() && display) {
//                displayVideoFrame(vp);
//
//            }
//            return vp.getTimeStamp() / 1000;
        } else if (packet.getStreamIndex() == audioStreamId) {
            try {
                audioQueue.add(IPacket.make(packet, true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Frame isn't part of video, it is something else
            return lastFrameTime;
        }

        return 0L;
    }

    private void playLoop() {
        while (true) {
//            if (playing) {
            try {
                long timeStamp = readFrame();
                if (timeStamp != -1) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            } else {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    public void play() {
        // Reset display synchronization
        mFirstVideoTimestampInStream = Global.NO_PTS;

        // Set the playLoop to continue doing its thing
        playing = true;
    }

    public void stop() {
        playing = false;
    }

    public void seekTo(long position) {
        boolean wasPlaying = playing;
        stop();

        System.out.println("original position:" + position);
//        for(int i = 0; i < container.getNumStreams(); i++) {
        //        final double timebase = videoCoder.getTimeBase().getDouble();
//        final double timebase = 1.0 / getFps();
//        System.out.println(container.getStream(videoStreamId).getTimeBase());
        int i = videoStreamId;
        long newPosition = (long) (position / 1000.0 / container.getStream(i).getTimeBase().getDouble());
        final long min = Math.max(0, newPosition - 1000);
        final long max = newPosition;
        picture = null;

        System.out.println("new position: " + newPosition);

        // TODO move this call inside of the play loop so then we access nothing from outside
        // of the thread.
//        if(position < lastFrameTime) {
//            container.seekKeyFrame(videoStreamId, min, newPosition, max, IContainer.SEEK_FLAG_BACKWARDS);
//        } else {
//            container.seekKeyFrame(i, -1, 0);
        container.seekKeyFrame(i, -1, -1, -1, IContainer.SEEK_FLAG_BACKWARDS);

        if (position < lastFrameTime) {
        } else {
            container.seekKeyFrame(i, min, newPosition, max, IContainer.SEEK_FLAG_ANY);
        }
//        }


        // TODO Send seek notification events to our threads so they can throw away whatever they have


        lastFrameTime = position;
//        container.seekKeyFrame(audioStreamId, -1, 0);
//        container.seekKeyFrame(audioStreamId, min, newPosition, max, IContainer.SEEK_FLAG_ANY);
//        }
//        container.seekKeyFrame(audioStreamId, min, position, max, IContainer.SEEK_FLAG_ANY);

        videoQueue.clear();
        audioQueue.clear();
//        System.out.println("New Position:" + newPosition);

//        readFrame();
        System.out.println("Video time:" + lastFrameTime);
//        while (lastFrameTime <= position - frameTime) {
//            System.out.println("Position:" + position);
//            System.out.println("Last Frame:" + lastFrameTime);
//            readFrame(false);
//        }

        if (wasPlaying) {
            play();
        }
    }

    public void setVisible(boolean visible) {
        mScreen.setVisible(visible);
    }

    public long getCurrentTime() throws Exception {
        return lastFrameTime;
    }

    private long millisecondsUntilTimeToDisplay(IVideoPicture picture) {
        /**
         * We could just display the images as quickly as we decode them, but it turns
         * out we can decode a lot faster than you think.
         *
         * So instead, the following code does a poor-man's version of trying to
         * match up the frame-rate requested for each IVideoPicture with the system
         * clock time on your computer.
         *
         * Remember that all Xuggler IAudioSamples and IVideoPicture objects always
         * give timestamps in Microseconds, relative to the first decoded item.  If
         * instead you used the packet timestamps, they can be in different units depending
         * on your IContainer, and IStream and things can get hairy quickly.
         */
        long millisecondsToSleep = 0;
        if (mFirstVideoTimestampInStream == Global.NO_PTS) {
            // This is our first time through
            mFirstVideoTimestampInStream = picture.getTimeStamp();
            // get the starting clock time so we can hold up frames
            // until the right time.
            mSystemVideoClockStartTime = System.currentTimeMillis();
            millisecondsToSleep = 0;
        } else {
            long systemClockCurrentTime = System.currentTimeMillis();
            long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime - mSystemVideoClockStartTime;
            // compute how long for this frame since the first frame in the stream.
            // remember that IVideoPicture and IAudioSamples timestamps are always in MICROSECONDS,
            // so we divide by 1000 to get milliseconds.
            long millisecondsStreamTimeSinceStartOfVideo = (picture.getTimeStamp() - mFirstVideoTimestampInStream) / 1000;
            final long millisecondsTolerance = 50; // and we give ourselfs 50 ms of tolerance
            millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo -
                    (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
        }
        return millisecondsToSleep;
    }

    /**
     * Opens a Swing window on screen.
     */
    private void openJavaVideo() {
        launchEdtTaskLater(new Runnable() {
            @Override
            public void run() {
                mScreen = new VideoImage();
            }
        });
    }

    /**
     * Forces the swing thread to terminate; I'm sure there is a right
     * way to do this in swing, but this works too.
     */
    private void closeJavaVideo() {
//        System.exit(0);
    }

    private void openJavaSound(IStreamCoder aAudioCoder) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
                (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
                aAudioCoder.getChannels(),
                true, /* xuggler defaults to signed 16 bit samples */
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        // TODO add in switch here for if there is no audio

        mLine = (SourceDataLine) AudioSystem.getLine(info);
        /**
         * if that succeeded, try opening the line.
         */
        mLine.open(audioFormat);
        /**
         * And if that succeed, start the line.
         */
        mLine.start();


    }

    private void launchEdtTaskNow(Runnable edtTask) {
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(edtTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void launchEdtTaskLater(Runnable edtTask) {
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            try {
                SwingUtilities.invokeLater(edtTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDataFeed(final File dataFeed) {
//        String filename = "F:\\Movies\\Ascenseur pour l'échafaud (1958) Louis Malle\\Ascenseur pour lechafaud (1958) Louis Malle.avi";

        data = dataFeed;

        String filename = data.getAbsolutePath();

        reader = ToolFactory.makeReader(data.getAbsolutePath());

        viewer = com.xuggle.mediatool.ToolFactory.makeViewer();


        IStreamCoder videoCoder = null;
        IStreamCoder audioCoder = null;

        System.out.println(data.getAbsolutePath());
        // Open up the container
        System.out.println(container);
        if (container.open(data.getAbsolutePath(), IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("could not open file: " + data.getAbsolutePath());

        int numStreams = container.getNumStreams();

        // and iterate through the streams to find the first audio stream
        videoStreamId = -1;
        videoCoder = null;
        audioStreamId = -1;
        audioCoder = null;
        for (int i = 0; i < numStreams; i++) {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamId = i;
                videoCoder = coder;
            } else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                audioStreamId = i;
                audioCoder = coder;
            }
        }
        if (videoStreamId == -1 && audioStreamId == -1)
            throw new RuntimeException("could not find audio or video stream in container: " + filename);


        /*
     * Check if we have a video stream in this file.  If so let's open up our decoder so it can
     * do work.
     */
        if (videoCoder != null) {
            if (videoCoder.open() < 0)
                throw new RuntimeException("could not open video decoder for container: " + filename);

            if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
                // if this stream is not in BGR24, we're going to need to
                // convert it.  The VideoResampler does that for us.
                resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
                        videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                if (resampler == null)
                    throw new RuntimeException("could not create color space resampler for: " + filename);
            }
      /*
       * And once we have that, we draw a window on screen
       */
            openJavaVideo();
        }

        if (audioCoder != null) {
            if (audioCoder.open() < 0)
                throw new RuntimeException("could not open audio decoder for container: " + filename);

      /*
       * And once we have that, we ask the Java Sound System to get itself ready.
       */
            try {
                openJavaSound(audioCoder);
            } catch (LineUnavailableException ex) {
                throw new RuntimeException("unable to open sound device on your system when playing back container: " + filename);
            }
        }


        // Grab FPS and length


        fps = (float) videoCoder.getFrameRate().getValue();
        long length = container.getDuration();
        timebase = videoCoder.getTimeBase().getDouble();

        System.out.println(String.format("FPS: %f", fps));
        System.out.println(String.format("Length: %d", length));

        packet = IPacket.make();
        mFirstVideoTimestampInStream = Global.NO_PTS;
        mSystemVideoClockStartTime = 0;


        // Test to make sure we got the framerate.
        // If we didn't, alert the user that this
        // may not work right.
        if (fps < 1.0) {
            // VLC can't read the framerate for this video for some reason.
            // Set it to the fallback rate so it is still usable for coding.
//            fps = FALLBACK_FRAME_RATE;
            /*
            JOptionPane.showMessageDialog(vlcDialog,
                    "Warning: Unable to detect framerate in video.\n"
                            + "This video may not behave properly. "
                            + "Please try converting to H.264.\n\n"
                            + "This can be done under Controller->Convert Videos.\n"
                            + "Setting framerate to " + FALLBACK_FRAME_RATE);
                    */
        }

        playing = false;

        new Thread(new VideoPlayer(videoCoder)).start();
        new Thread(new AudioPlayer(audioCoder)).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                playLoop();
            }
        }).start();
    }

    public float getFps() {
        return (float) fps;
    }

    public long getLength() {
        System.out.println(container.getDuration());
        return container.getDuration() / 1000;
    }

    public boolean isPlaying() {
        return playing;
    }

    class VideoPlayer implements Runnable {

        IStreamCoder videoCoder = null;


        public VideoPlayer(IStreamCoder videoCoder) {
            this.videoCoder = videoCoder.copyReference();
        }

        private IVideoPicture readVideoFrame(IPacket packet) {
            if (picture == null) {
                picture = IVideoPicture.make(videoCoder.getPixelType(),
                        videoCoder.getWidth(), videoCoder.getHeight());
            }

//            System.out.println(packet.isComplete());

            int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
            if (bytesDecoded < 0)
                throw new RuntimeException("got error decoding video ");

    /*
     * Some decoders will consume data in a packet, but will not be able to construct
     * a full video picture yet.  Therefore you should always check if you
     * got a complete picture from the decoder
     */
            System.out.println(picture.isComplete());
            if (picture.isComplete()) {
                IVideoPicture newPic = picture.copyReference();
      /*
       * If the resampler is not null, that means we didn't get the video in BGR24 format and
       * need to convert it into BGR24 format.
       */
                if (resampler != null) {
                    // we must resample
                    newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
                    if (resampler.resample(newPic, picture) < 0)
                        throw new RuntimeException("could not resample video ");
                }
////            if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
////                    throw new RuntimeException("could not decode video as BGR 24 bit data ");

                picture = null;
                return newPic;
            } else {
                return picture;
            }
        }

        private void displayVideoFrame(IVideoPicture frame) {
            final BufferedImage bi = Utils.videoPictureToImage(frame);

            long delay = millisecondsUntilTimeToDisplay(frame);
            // if there is no audio stream; go ahead and hold up the main thread.  We'll end
            // up caching fewer video pictures in memory that way.
            try {
                if (delay > 0) {
//                    System.out.println("DELAYING");
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                return;
            }

//            launchEdtTaskLater(new Runnable() {
//                public void run() {
                    mScreen.setImage(bi);
//                }
//            });
        }

        @Override
        public void run() {
            while (true) {
                if (playing) {
                    try {
                        IPacket packet = videoQueue.poll();
                        System.out.println(videoQueue.size());
                        if (packet != null) {
                            IVideoPicture picture = readVideoFrame(packet);
                            if (picture.isComplete()) {
                                displayVideoFrame(picture);
                                lastFrameTime = picture.getTimeStamp() / 1000;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    class AudioPlayer implements Runnable {
        IStreamCoder audioCoder = null;

        public AudioPlayer(IStreamCoder audioCoder) {
            this.audioCoder = audioCoder;
        }

        private IAudioSamples readAudioFrame(IPacket packet) {
        /*
         * We allocate a set of samples with the same number of channels as the
         * coder tells us is in this buffer.
         *
         * We also pass in a buffer size (1024 in our example), although Xuggler
         * will probably allocate more space than just the 1024 (it's not important why).
         */
            IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());

        /*
         * A packet can actually contain multiple sets of samples (or frames of samples
         * in audio-decoding speak).  So, we may need to call decode audio multiple
         * times at different offsets in the packet's data.  We capture that here.
         */
            int offset = 0;

        /*
         * Keep going until we've processed all data
         */
            while (offset < packet.getSize()) {
                int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                if (bytesDecoded < 0)
                    throw new RuntimeException("got error decoding audio ");
                offset += bytesDecoded;
          /*
           * Some decoder will consume data in a packet, but will not be able to construct
           * a full set of samples yet.  Therefore you should always check if you
           * got a complete set of samples from the decoder
           */
                if (samples.isComplete()) {
                    // note: this call will block if Java's sound buffers fill up, and we're
                    // okay with that.  That's why we have the video "sleeping" occur
                    // on another thread.
//                playJavaSound(samples);
                }
            }

            return samples;
        }

        private void playJavaSound(IAudioSamples aSamples) {
            /**
             * We're just going to dump all the samples into the line.
             */
            byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
            mLine.write(rawBytes, 0, aSamples.getSize());
        }

        private void closeJavaSound() {
            if (mLine != null) {
      /*
       * Wait for the line to finish playing
       */
                mLine.drain();
      /*
       * Close the line.
       */
                mLine.close();
                mLine = null;
            }
        }

        @Override
        public void run() {
            while (true) {
                if (playing) {
                    try {
                        IPacket packet = audioQueue.poll();
                        if (packet != null) {
                            IAudioSamples audio = readAudioFrame(packet);
                            if (audio.isComplete()) {
                                playJavaSound(audio);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    class IOCommunicator implements Runnable {
        PipedOutputStream output;
        PipedInputStream input;

        public IOCommunicator(PipedOutputStream output) throws IOException {
            this.input = new PipedInputStream();
            this.output = output; // Output stream passed in from other process
            output.connect(input);
        }

        @Override
        public void run() {
            try {
                byte[] b = new byte[512];
                int read = 1;
                // As long as data is read; -1 means <a class="zem_slink" href="http://en.wikipedia.org/wiki/End-of-file" title="End-of-file" rel="wikipedia" target="_blank">EOF</a>
                while (read > -1) {
                    // Block until we get a signal
                    read = input.read(b, 0, b.length);

                    // Act on the read
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}