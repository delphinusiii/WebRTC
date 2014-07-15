package avp8;

import com.google.libvpx.*;

import fm.*;

public class Decoder
{
	private LibVpxDec codec;
    public boolean needsKeyFrame;
	
	public Decoder()
	{
		try
		{
			// initialize decoder
            codec = new LibVpxDec();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}


    public byte[] decode(byte[] encodedFrame, Holder<Integer> width, Holder<Integer> height)
    {
        // decode
        byte[] data;
        try
        {
            int[] widthHeight = new int[2];
            int[] result = new int[1];
            data = codec.decodeFrameToBuffer(encodedFrame, widthHeight, result);
            if (result[0] == 5)
            {
                needsKeyFrame = true;
            }
            else
            {
                needsKeyFrame = false;

                // get frame
                if (data != null)
                {
                    width.setValue(widthHeight[0]);
                    height.setValue(widthHeight[1]);
                    return data;
                }
            }

        }
        catch (Exception ex)
        {
            Log.error("Could not decode frame.", ex);
        }

        width.setValue(0);
        height.setValue(0);
        return null;
    }

    public void destroy()
	{
		try
		{
            codec.close();
		}
		catch (Exception ex) { }
	}
}