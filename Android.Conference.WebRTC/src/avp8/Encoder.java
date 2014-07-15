package avp8;

import com.google.libvpx.*;

import fm.*;

public class Encoder
{
    private LibVpxEnc codec;
    private LibVpxEncConfig config;
	private int frame_cnt;
	private boolean sendKeyFrame;
	
	public Encoder()
	{
        frame_cnt = 0;
    }
	
	public byte[] encode(int width, int height, byte[] frame, long fourcc, int rotation)
	{
        try
        {
            if (codec != null && (width != config.getWidth() || height != config.getHeight()))
            {
                if (codec != null)
                {
                    codec.close();
                    codec = null;
                }
                
                if (config != null)
                {
                    config.close();
                    config = null;
                }
            }
            
            if (codec == null)
            {
                // define configuration options
                config = new LibVpxEncConfig(width, height);
                config.setTimebase(1, 30);
                config.setRCTargetBitrate(width * height * 256 / 320 / 240);
                config.setRCEndUsage(1); // vpx_rc_mode.VPX_CBR
                config.setKFMode(1); // vpx_kf_mode.VPX_KF_AUTO
                config.setKFMinDist(30); // keyframe 1x / second
                config.setKFMaxDist(30);
                config.setErrorResilient(1);
                config.setLagInFrames(0);
                config.setPass(0); // vpx_enc_pass.VPX_RC_ONE_PASS
                config.setRCMinQuantizer(0);
                config.setRCMaxQuantizer(63);
                config.setProfile(0);
    
                // initialize encoder
                codec = new LibVpxEnc(config);
            }
            
    	    // set flags
    	    long flag = 0;
    	    if (sendKeyFrame)
    	    {
    	        flag |= (1<<0); // VPX_EFLAG_FORCE_KF;
    	        sendKeyFrame = false;
    	    }
    	    
            // encode
            long deadline = 1; // VPX_DL_REALTIME
            byte[] buffer = codec.convertByteEncodeFrame(
                    frame, frame_cnt, 1, flag, deadline, fourcc, rotation);

            frame_cnt++;

            // get frame
            return buffer;
        }
        catch (Exception ex)
        {
            Log.error("Could not encode frame.", ex);
        }

		return null;
	}
	
	public void forceKeyframe()
	{
		sendKeyFrame = true; 
	}

    public void destroy()
	{
		try
		{
		    if (codec != null)
		    {
		        codec.close();
		        codec = null;
		    }
		    
		    if (config != null)
		    {
		        config.close();
		        config = null;
		    }
		}
		catch (Exception ex) { }
	}
}