package fm.android.conference.webrtc;

import fm.*;
import fm.icelink.webrtc.*;

public class LocalMedia
{
    // We're going to need both audio and video
    // for this example. We can constrain the
    // video slightly for performance benefits.
    private boolean audio          = true;
    private boolean video          = true;
    private int     videoWidth     = 320;
    private int     videoHeight    = 240;
    private int     videoFrameRate = 15;
    
    private LocalMediaStream localStream;
    public LocalMediaStream getLocalStream()
    {
        return localStream;
    }
    
    private Object localVideoControl;
    public Object getLocalVideoControl()
    {
        return localVideoControl;
    }
    
    private Exception lastStartException = null;
    public Exception getLastStartException()
    {
        return lastStartException;
    }
    
    public void start(final SingleAction<Exception> callback)
    {
        try
        {
            // Get a reference to the local media streams.
            UserMedia.getMedia(new GetMediaArgs(audio, video)
            {{
                setVideoWidth(videoWidth);         // optional
                setVideoHeight(videoHeight);       // optional
                setVideoFrameRate(videoFrameRate); // optional
                setOnSuccess(new SingleAction<GetMediaSuccessArgs>()
                {
                    public void invoke(GetMediaSuccessArgs e)
                    {
                        // Keep a reference to the local media
                        // and video preview control.
                        localStream = e.getLocalStream();
                        localVideoControl = e.getLocalVideoControl();
                        callback.invoke(null);
                    }
                });
                setOnFailure(new SingleAction<GetMediaFailureArgs>()
                {
                    public void invoke(GetMediaFailureArgs e)
                    {
                        lastStartException = e.getException();
                        callback.invoke(e.getException());
                    }
                });
            }});
        }
        catch (Exception ex)
        {
            lastStartException = ex;
            callback.invoke(ex);
        }
    }
    
    private Exception lastStopException = null;
    public Exception getLastStopException()
    {
        return lastStopException;
    }
    
    public void stop(final SingleAction<Exception> callback)
    {
        try
        {
            // Release our reference to the local media streams.
            if (localStream != null)
            {
                localStream.stop();
            }
            callback.invoke(null);
        }
        catch (Exception ex)
        {
            lastStopException = ex;
            callback.invoke(null);
        }
    }
    
    public void switchCamera()
    {
        if (localStream != null)
        {
            localStream.useNextVideoDevice();
        }
    }
}
