package avp8;

import fm.*;
import fm.icelink.*;
import fm.icelink.webrtc.*;

import com.google.libvpx.*;

public class Vp8Codec extends VideoCodec
{
    private Vp8Padep padep;
    private Encoder encoder;
    private Decoder decoder;

    public Vp8Codec()
    {
        padep = new Vp8Padep();
    }

    /// <summary>
    /// Encodes a frame.
    /// </summary>
    /// <param name="videoBuffer">The video buffer.</param>
    /// <returns></returns>
    public byte[] encode(VideoBuffer videoBuffer)
    {
        if (encoder == null)
        {
            encoder = new Encoder();
        }

        // frame -> vp8
        int width, height;
        int rotate = videoBuffer.getRotate();
        if (rotate % 180 == 0)
        {
            width = videoBuffer.getWidth();
            height = videoBuffer.getHeight();
        }
        else
        {
            height = videoBuffer.getWidth();
            width = videoBuffer.getHeight();
        }
        return encoder.encode(width, height, videoBuffer.getPlane().getData(), LibVpxEnc.FOURCC_NV21, rotate);
    }

    /// <summary>
    /// Decodes an encoded frame.
    /// </summary>
    /// <param name="encodedFrame">The encoded frame.</param>
    /// <returns></returns>
    public VideoBuffer decode(byte[] encodedFrame)
    {
        if (decoder == null)
        {
            decoder = new Decoder();
        }

        if (padep.getSequenceNumberingViolated())
        {
            decoder.needsKeyFrame = true;
            return null;
        }

        // vp8 -> frame
        Holder<Integer> width = new Holder<Integer>(null);
        Holder<Integer> height = new Holder<Integer>(null);
        byte[] frame = decoder.decode(encodedFrame, width, height);
        if (frame == null)
        {
            return null;
        }
        try
        {
            return new VideoBuffer(width.getValue(), height.getValue(), new VideoPlane(frame, width.getValue()));
        }
        catch (Exception ex)
        { 
            Log.error("Could not convert decoded image to video buffer.", ex);
            return null;
        }
    }

    /// <summary>
    /// Gets whether the decoder needs a keyframe. This
    /// is checked after every failed Decode operation.
    /// </summary>
    /// <returns></returns>
    public boolean decoderNeedsKeyFrame()
    {
        if (decoder == null)
        {
            return false;
        }
        return decoder.needsKeyFrame;
    }

    /// <summary>
    /// Packetizes an encoded frame.
    /// </summary>
    /// <param name="encodedFrame">The encoded frame.</param>
    /// <returns></returns>
    public RTPPacket[] packetize(byte[] encodedFrame)
    {
        return padep.packetize(encodedFrame, getClockRate());
    }

    /// <summary>
    /// Depacketizes a packet.
    /// </summary>
    /// <param name="packet">The packet.</param>
    /// <returns></returns>
    public byte[] depacketize(RTPPacket packet)
    {
        return padep.depacketize(packet);
    }

    /// <summary>
    /// Processes RTCP packets.
    /// </summary>
    /// <param name="packets">The packets to process.</param>
    public void processRTCP(RTCPPacket[] packets)
    {
        for (int i = 0; i < packets.length; i++)
        {
            RTCPPacket packet = packets[i];
            if (packet instanceof RTCPPliPacket)
            {
                if (encoder != null)
                {
                    encoder.forceKeyframe();
                }
            }
        }
    }

    /// <summary>
    /// Destroys the codec.
    /// </summary>
    public void destroy()
    {
        if (encoder != null)
        {
            encoder.destroy();
            encoder = null;
        }

        if (decoder != null)
        {
            decoder.destroy();
            decoder = null;
        }
    }
}
